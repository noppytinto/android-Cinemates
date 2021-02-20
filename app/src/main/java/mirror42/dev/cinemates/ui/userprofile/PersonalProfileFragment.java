package mirror42.dev.cinemates.ui.userprofile;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;

import java.io.IOException;

import mirror42.dev.cinemates.R;
import mirror42.dev.cinemates.model.User;
import mirror42.dev.cinemates.ui.login.LoginViewModel;
import mirror42.dev.cinemates.utilities.FirebaseAnalytics;
import mirror42.dev.cinemates.utilities.ImageUtilities;
import mirror42.dev.cinemates.utilities.RemoteConfigServer;

import static android.app.Activity.RESULT_OK;


public class PersonalProfileFragment extends Fragment implements
        View.OnClickListener {
    private final String TAG = getClass().getSimpleName();
    private View view;
    private ImageView profilePicture;
    private TextView textViewEmail;
    private TextView fullNameTextView;
    private TextView usernameTextView;
    private TextView textViewResendEmailMessage;
    private Button buttonLogout;
    private Button buttonResendEmail;
    private Button buttonChangePassword;
    private RemoteConfigServer remoteConfigServer;
    private LoginViewModel loginViewModel;
    private View includeAccountActivationView;
    private View includePersonalProfileContent;
//    private NotificationsViewModel notificationsViewModel;
    private Button buttonCustomLists;
    private Button subscribedListsButton;
    private Button followersButton;
    private Button followingButton;
    private TextView listLabel;
    private FollowingViewModel followingViewModel;
    private FollowersViewModel followersViewModel;

    private static int PICK_IMAGE = 30;
    private final int PERMISSION_CODE = 5;

    private String filePath;
    private PersonalProfileViewModel personalProfileViewModel;


    //----------------------------------------------------------------------- ANDROID METHODS

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_personal_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.view = view;
        profilePicture = view.findViewById(R.id.imageView_personalProfileFragment_profilePicture);
        textViewEmail = view.findViewById(R.id.textView_personalProfileFragment_email);
        fullNameTextView = view.findViewById(R.id.textView_personalProfileFragment_fullName);
        usernameTextView = view.findViewById(R.id.textView_personalProfileFragment_username);
        textViewResendEmailMessage = view.findViewById(R.id.textView_userProfileFragment_resendEmailMessage);
        buttonLogout = view.findViewById(R.id.button_personalProfileFragment_logout);
        buttonChangePassword = view.findViewById(R.id.button_personalProfileFragment_changePassword);
        buttonCustomLists = view.findViewById(R.id.button_personalProfileFragment_customLists);
        subscribedListsButton = view.findViewById(R.id.button_personalProfileFragment_subscribedLists);
        remoteConfigServer = RemoteConfigServer.getInstance();
        followersButton = view.findViewById(R.id.button_personalProfileFragment_followers);
        followingButton = view.findViewById(R.id.button_personalProfileFragment_following);
        listLabel = view.findViewById(R.id.textView_personalProfileFragment_label1);
        includeAccountActivationView = view.findViewById(R.id.include_personalProfileFragment_accountVerification);
        includePersonalProfileContent = view.findViewById(R.id.include_personalProfileFragment_myLists);
        buttonResendEmail = view.findViewById(R.id.button_personalProfileFragment_resendEmail);
//        notificationsViewModel = new ViewModelProvider(requireActivity()).get(NotificationsViewModel.class);
        personalProfileViewModel =  new ViewModelProvider(this).get(PersonalProfileViewModel.class);  // on view created
        loginViewModel = new ViewModelProvider(requireActivity()).get(LoginViewModel.class);
        followingViewModel = new ViewModelProvider(this).get(FollowingViewModel.class);
        followersViewModel = new ViewModelProvider(this).get(FollowersViewModel.class);

        // setting listeners
        buttonLogout.setOnClickListener(this);
        buttonChangePassword.setOnClickListener(this);
        buttonCustomLists.setOnClickListener(this);
        subscribedListsButton.setOnClickListener(this);
        followersButton.setOnClickListener(this);
        followingButton.setOnClickListener(this);
        profilePicture.setOnClickListener(this);
        buttonResendEmail.setOnClickListener(this);

        //
        FirebaseAnalytics firebaseAnalytics = FirebaseAnalytics.getInstance();
        firebaseAnalytics.logScreenEvent(this, "User profile page", getContext());

        hidePendingUserContent();
        hideLoggedUserContent();

    }// end onViewCreated()

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        //
        checkLoginStatus();
//        observeNotifications();
    }// end onActivityCreated()

    private void observeNotifications() {
//        notificationsViewModel.getNotificationsStatus().observe(getViewLifecycleOwner(), status -> {
//            switch (status) {
//                case GOT_NEW_NOTIFICATIONS:
//                    ((MainActivity) getActivity()).activateNotificationsIcon();
//                    break;
//                case NO_NOTIFICATIONS:
//                    break;
//            }
//        });
    }

    private void checkLoginStatus() {
        loginViewModel.getObservableLoginResult().observe(getViewLifecycleOwner(), loginResult -> {
            switch (loginResult) {
                case SUCCESS:
                case REMEMBER_ME_EXISTS:
                    try {
                        showLoggedUserContent();
                        hidePendingUserContent();
                        User user = loginViewModel.getLoggedUser();
                        String profilePicturePath = user.getProfilePicturePath();
                        ImageUtilities.loadCircularImageInto(profilePicturePath, profilePicture, getContext());
                        textViewEmail.setText(user.getEmail());
                        fullNameTextView.setText(user.getFullName());
                        usernameTextView.setText("@" + user.getUsername());
                        loadSocialStatistics();
                    }  catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                case IS_PENDING_USER: {
                    hideLoggedUserContent();
                    showPendingUserContent();

                    // get pending-user basic data
                    User user = loginViewModel.getPendingUser();
                    if(user!=null) {
                        try {
                            String profilePicturePath = user.getProfilePicturePath();

                            ImageUtilities.loadCircularImageInto(profilePicturePath, profilePicture, getContext());
                            textViewEmail.setText(user.getEmail());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
                break;
                case IS_NOT_PENDING_USER_ANYMORE:
                    hidePendingUserContent();
                    break;
            }// switch
        });
    }


    private void loadSocialStatistics() {
        int followersCount = loginViewModel.getLoggedUser().getFollowersCount();
        int followingCount = loginViewModel.getLoggedUser().getFollowingCount();
        followingButton.setText("Seguiti\n" + followingCount);
        followersButton.setText("Follower\n" + followersCount);
    }

    private void showLoggedUserContent() {
        listLabel.setVisibility(View.VISIBLE);
        followersButton.setVisibility(View.VISIBLE);
        followingButton.setVisibility(View.VISIBLE);
        buttonChangePassword.setVisibility(View.VISIBLE);
        buttonCustomLists.setVisibility(View.VISIBLE);
        subscribedListsButton.setVisibility(View.VISIBLE);
        includePersonalProfileContent.setVisibility(View.VISIBLE);

    }

    private void hideLoggedUserContent() {
        listLabel.setVisibility(View.GONE);
        followersButton.setVisibility(View.GONE);
        followingButton.setVisibility(View.GONE);
        buttonChangePassword.setVisibility(View.GONE);
        buttonCustomLists.setVisibility(View.GONE);
        subscribedListsButton.setVisibility(View.GONE);
        includePersonalProfileContent.setVisibility(View.GONE);
        includeAccountActivationView.setVisibility(View.GONE);

    }

    private void showPendingUserContent() {
        includeAccountActivationView.setVisibility(View.VISIBLE);

    }

    private void hidePendingUserContent() {
        includeAccountActivationView.setVisibility(View.GONE);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data); //get the image’s file location

        Bitmap thumbnail = null;
        if(requestCode==PICK_IMAGE && resultCode==RESULT_OK){
            try {
                //set picked image to the mProfile
                thumbnail = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), data.getData());
//                mProfile.setImageBitmap(bitmap);

            } catch (IOException e) {
                e.printStackTrace();
            }

            filePath = "-";
            try {
                filePath = getRealPathFromUri(data.getData(), getActivity());

                // load thumbnail
                Glide.with(this)  //2
                        .load(filePath) //3
                        .fallback(R.drawable.broken_image)
                        .placeholder(R.drawable.placeholder_image)
                        .circleCrop() //4
                        .into(profilePicture); //8


                personalProfileViewModel.changeProfileImage(loginViewModel.getLoggedUser(),filePath);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == buttonLogout.getId()) {
            loginViewModel.deleteLoggedUserLocalData(getContext());

            //
            NavController navController = Navigation.findNavController(v);
            navController.popBackStack();
            navController.navigate(R.id.loginFragment);
        }
        else if(v.getId() == buttonResendEmail.getId()) {
            // checking if email verification has been clicked
            boolean accountEnabled = loginViewModel.checkEmailVerificationState();
            if( ! accountEnabled) {
                // insert into postgrest database
                // and show new user profile page
                loginViewModel.resendVerificationEmail();
                showCenteredToast("Email attivazione riniviata, era esegui un Logout e controlla la posta.");
                buttonResendEmail.setText("Email attivazione rinviata!");
                buttonResendEmail.setEnabled(false);
            }
        }
        else if(v.getId() == buttonChangePassword.getId()){
            NavController navController = Navigation.findNavController(v);
            navController.navigate(R.id.changePasswordFragment);

        }
        else if(v.getId() == buttonCustomLists.getId()){
            String fetchMode = "fetch_my_custom_lists";
            String ownerUsername = loginViewModel.getLoggedUser().getUsername();
            NavDirections customListBrowserFragment =
                    PersonalProfileFragmentDirections.actionPersonalProfileFragmentToCustomListBrowserFragment(fetchMode, ownerUsername);
            Navigation.findNavController(v).navigate(customListBrowserFragment);
        }
        else if(v.getId() == subscribedListsButton.getId()){
            String fetchMode = "fetch_subscribed_lists";
            String ownerUsername = loginViewModel.getLoggedUser().getUsername();

            NavDirections customListBrowserFragment =
                    PersonalProfileFragmentDirections.actionPersonalProfileFragmentToCustomListBrowserFragment(fetchMode, ownerUsername);
            Navigation.findNavController(v).navigate(customListBrowserFragment);
        }
        else if(v.getId() == profilePicture.getId()){
            pickImageFromGallery(v);
        }
        else if(v.getId() == followersButton.getId()){
            NavDirections followersFragment =
                    PersonalProfileFragmentDirections.actionPersonalProfileFragmentToFollowersFragment(loginViewModel.getLoggedUser().getUsername());
            Navigation.findNavController(v).navigate(followersFragment);
        }
        else if(v.getId() == followingButton.getId()){
            NavDirections followersFragment =
                    PersonalProfileFragmentDirections.actionPersonalProfileFragmentToFollowingFragment(loginViewModel.getLoggedUser().getUsername());
            Navigation.findNavController(v).navigate(followersFragment);
        }
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        MenuItem item = menu.findItem(R.id.menu_item_login);
        if(item!=null)
            item.setVisible(false);

        checkForNewNotifications();
    }



    //----------------------------------------------------------------------- METHODS

    private String getRealPathFromUri(Uri imageUri, Activity activity){
        Cursor cursor = activity.getContentResolver().query(imageUri, null,  null, null, null); if(cursor==null) {
            return imageUri.getPath();
        }else{
            cursor.moveToFirst();
            int idx =  cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            return cursor.getString(idx);
        }
    }

    void pickImageFromGallery(View view){
        personalProfileViewModel.getResetStatus().observe(getViewLifecycleOwner(), changeImageResult -> {
            if(changeImageResult == PersonalProfileViewModel.ChangeImageResult.SUCCESS)
                showCenteredToast( "cambio immagine profilo riuscito ");
            else if(changeImageResult == PersonalProfileViewModel.ChangeImageResult.FAILED)
                showCenteredToast( "cambio immagine profilo NON riuscito");
        });
        requestPermission();
    }

    private void requestPermission(){
        if(ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
            accessTheGallery();
        } else {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_CODE);
        }
    }

    public void accessTheGallery(){
        Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        i.setType("image/*");
        startActivityForResult(i, PICK_IMAGE);
    }
//
//    private void showResendEmail() {
//        includeAccountActivationView.setVisibility(View.VISIBLE);
//        includePersonalProfileContent.setVisibility(View.GONE);
//    }
//
//    private void hideResendEmail() {
//        includeAccountActivationView.setVisibility(View.GONE);
//        includePersonalProfileContent.setVisibility(View.VISIBLE);
//    }

    public void showCenteredToast(String message) {
        final Toast toast = Toast.makeText(getContext(), message, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    private void checkForNewNotifications() {
//        if(notificationsViewModel!=null) {
//            if(notificationsViewModel.getNotificationsStatus().getValue() == NotificationsViewModel.NotificationsStatus.GOT_NEW_NOTIFICATIONS) {
//                ((MainActivity) requireActivity()).activateNotificationsIcon();
//            }
//            else {
//                ((MainActivity) requireActivity()).deactivateNotificationsIcon();
//            }
//        }
    }




}// end PersonalProfileFragment class