package mirror42.dev.cinemates.ui.userprofile;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
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
import com.google.android.material.progressindicator.LinearProgressIndicator;

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
    private Button buttonDeleteImage;
    private Button buttonSaveImage;
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
    private Uri localImageUri;
    private String oldImage;
    private LinearProgressIndicator uploadProgressIndicator;
    private static boolean additionalActivationMailSent;

    private static int PICK_IMAGE = 30;
    private final int PERMISSION_CODE = 5;

    private PersonalProfileViewModel personalProfileViewModel;
    private final int SELECT_PICTURE = 200;

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
        buttonSaveImage = view.findViewById(R.id.button_saveNewImage_personalProfileFragment);
        buttonDeleteImage = view.findViewById(R.id.button_Delete_NewImage);
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
        uploadProgressIndicator = view.findViewById(R.id.progressIndicator_personalProfileFragment);

//        notificationsViewModel = new ViewModelProvider(requireActivity()).get(NotificationsViewModel.class);
        personalProfileViewModel =  new ViewModelProvider(this).get(PersonalProfileViewModel.class);  // on view created
        loginViewModel = new ViewModelProvider(requireActivity()).get(LoginViewModel.class);
        followingViewModel = new ViewModelProvider(this).get(FollowingViewModel.class);
        followersViewModel = new ViewModelProvider(this).get(FollowersViewModel.class);

        // setting listeners
        buttonLogout.setOnClickListener(this);

        buttonCustomLists.setOnClickListener(this);
        subscribedListsButton.setOnClickListener(this);
        followersButton.setOnClickListener(this);
        followingButton.setOnClickListener(this);
        User user = loginViewModel.getLoggedUser();

        if(!loginViewModel.getLoggedUser().getIsExternalUser()) {
            profilePicture.setOnClickListener(this);
            buttonChangePassword.setOnClickListener(this);
        }



        buttonResendEmail.setOnClickListener(this);

        buttonSaveImage.setOnClickListener(this);
        buttonDeleteImage.setOnClickListener(this);

        buttonDeleteImage.setVisibility(View.GONE);
        buttonSaveImage.setVisibility(View.GONE);

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
                        oldImage = profilePicturePath;

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

    private void showUploadProgressIndicator(){
        uploadProgressIndicator.setVisibility(View.VISIBLE);
    }

    private void hideUploadProgressIndicator(){
        uploadProgressIndicator.setVisibility(View.GONE);
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

        if(!loginViewModel.getLoggedUser().getIsExternalUser())
            buttonChangePassword.setVisibility(View.VISIBLE);
        else
            buttonChangePassword.setVisibility(View.GONE);
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
                if(additionalActivationMailSent) {
                    showCenteredToast("email attivazione gia' rininviata");
                    buttonResendEmail.setText("Email attivazione rinviata!");
                    buttonResendEmail.setEnabled(false);
                }
                else {
                    loginViewModel.resendVerificationEmail();
                    showCenteredToast("Email attivazione riniviata, era esegui un Logout e controlla la posta.");
                    buttonResendEmail.setText("Email attivazione rinviata!");
                    buttonResendEmail.setEnabled(false);
                    additionalActivationMailSent = true;
                }

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
//            pickImageFromGallery(v);
            imageChooser();
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
        }else if(v.getId() == buttonSaveImage.getId() ){
            changeImageToServer();
        }else if(v.getId() == buttonDeleteImage.getId()){
            showUploadProgressIndicator();
            usernameTextView.setVisibility(View.VISIBLE);
            buttonDeleteImage.setVisibility(View.GONE);
            buttonSaveImage.setVisibility(View.GONE);
            ImageUtilities.loadCircularImageInto(oldImage, profilePicture, getContext());
            loginViewModel.getLoggedUser().setProfilePictureURL(oldImage);
            hideUploadProgressIndicator();
            showCenteredToast("Cambio immagine annullato");
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data); //get the image’s file location
        if(requestCode==SELECT_PICTURE && resultCode==RESULT_OK){
            try {
                showUploadProgressIndicator();
                localImageUri = data.getData();
                personalProfileViewModel.changeProfilePicture(loginViewModel.getLoggedUser(), localImageUri, requireContext());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    //----------------------------------------------------------------------- METHODS

    private void changeImageToServer(){
        personalProfileViewModel.getUpdateImageToServer().observe(getViewLifecycleOwner(), changeImageToServerResult->{


            switch(changeImageToServerResult){

                case SUCCESS:
                    hideUploadProgressIndicator();
                    usernameTextView.setVisibility(View.VISIBLE);
                    buttonDeleteImage.setVisibility(View.GONE);
                    buttonSaveImage.setVisibility(View.GONE);
                    showCenteredToast( "Perfetto Cambio immagine completo");
                    break;

                case FAILED:
                    showCenteredToast( "cambio immagine profilo NON riuscito");
                    break;
            }
        });

        showUploadProgressIndicator();
        personalProfileViewModel.changeProfileImageToServer(loginViewModel.getLoggedUser(), personalProfileViewModel.getImageName());
    }
    void imageChooser() {
        personalProfileViewModel.getResetStatus().observe(getViewLifecycleOwner(), changeImageResult -> {
            hideUploadProgressIndicator();
            switch (changeImageResult) {
                case SUCCESS: {
                    Glide.with(this)  //2
                            .load(localImageUri) //3
                            .fallback(R.drawable.broken_image)
                            .placeholder(R.drawable.icon_user_dark_blue)
                            .circleCrop() //4
                            .into(profilePicture); //8
                    loginViewModel.updateProfileImageUrl(personalProfileViewModel.getImageName());
                    usernameTextView.setVisibility(View.GONE);
                    buttonDeleteImage.setVisibility(View.VISIBLE);
                    buttonSaveImage.setVisibility(View.VISIBLE);
                    showCenteredToast( "Cliccare su salva per rendere permanete il cambiamento ");
                }
                break;
                case FAILED:
                    showCenteredToast( "cambio immagine profilo NON riuscito");
                    break;
            }
        });

        // create an instance of the
        // intent of the type image
        Intent i = new Intent();
        i.setType("image/*");
        i.setAction(Intent.ACTION_GET_CONTENT);

        // pass the constant to compare it
        // with the returned requestCode
        startActivityForResult(Intent.createChooser(i, "Select Picture"), SELECT_PICTURE);
    }

//    @Override
//    public void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//
//        if (resultCode == RESULT_OK) {
//
//            // compare the resultCode with the
//            // SELECT_PICTURE constant
//            if (requestCode == SELECT_PICTURE) {
//                // Get the url of the image from data
//                Uri selectedImageUri = data.getData();
//                if (null != selectedImageUri) {
//                    // update the preview image in the layout
//
//                    filePath = "-";
//                    try {
////                        Log.d(TAG, "onActivityResult: " + getPath(selectedImageUri));
//                        filePath = RealPathUtil.getRealPath(requireContext(), selectedImageUri);
//
//                        if(filePath==null || filePath.isEmpty()) {
//                            personalProfileViewModel.setResetStatus(PersonalProfileViewModel.ChangeImageResult.FAILED);
//                            return;
//                        }
//
//                        // load thumbnail
//                        try {
//                            Glide.with(this)  //2
//                                    .load(filePath) //3
//                                    .fallback(R.drawable.broken_image)
//                                    .placeholder(R.drawable.broken_image)
//                                    .circleCrop() //4
//                                    .into(profilePicture); //8
//                        } catch (Exception e) {
//                            e.printStackTrace();
//                        }
//
//                        personalProfileViewModel.changeProfileImage(loginViewModel.getLoggedUser(), filePath);
//
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                }
//            }
//        }
//    }


    private String getRealPathFromUri(Uri imageUri){
        Cursor cursor = null;
        cursor = requireActivity().getContentResolver().query(imageUri, null,  null, null, null);
        if(cursor==null) {
            return imageUri.getPath();
        }else{
            cursor.moveToFirst();
            int idx =  cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            return cursor.getString(idx);
        }
    }

    void pickImageFromGallery(View view){
        personalProfileViewModel.getResetStatus().observe(getViewLifecycleOwner(), changeImageResult -> {
            switch (changeImageResult) {
                case SUCCESS: {
                    Glide.with(this)  //2
                            .load(localImageUri) //3
                            .fallback(R.drawable.broken_image)
                            .placeholder(R.drawable.icon_user_dark_blue)
                            .circleCrop() //4
                            .into(profilePicture); //8

                    showCenteredToast( "cambio immagine profilo riuscito ");
                }
                    break;
                case FAILED:
                    showCenteredToast( "cambio immagine profilo NON riuscito");
                    break;
            }
        });
        requestPermission();
    }

    private void requestPermission(){
        if(ContextCompat.checkSelfPermission(getContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
            accessTheGallery();
        } else {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_CODE);
        }
    }

    public void accessTheGallery(){
        Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        i.setType("image/*");
        startActivityForResult(i, SELECT_PICTURE);
    }

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