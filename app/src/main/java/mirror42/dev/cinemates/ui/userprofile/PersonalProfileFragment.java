package mirror42.dev.cinemates.ui.userprofile;

import android.os.Bundle;
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
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;

import mirror42.dev.cinemates.MainActivity;
import mirror42.dev.cinemates.R;
import mirror42.dev.cinemates.model.User;
import mirror42.dev.cinemates.ui.login.LoginViewModel;
import mirror42.dev.cinemates.ui.notification.NotificationsViewModel;
import mirror42.dev.cinemates.utilities.FirebaseAnalytics;
import mirror42.dev.cinemates.utilities.ImageUtilities;
import mirror42.dev.cinemates.utilities.RemoteConfigServer;


public class PersonalProfileFragment extends Fragment implements View.OnClickListener {
    private final String TAG = getClass().getSimpleName();
    private View view;
    private ImageView profilePicture;
    private TextView textViewEmail;
    private TextView textViewResendEmailMessage;
    private Button buttonLogout;
    private Button buttonResendEmail;
    private Button buttonChangePassword;
    private RemoteConfigServer remoteConfigServer;
    private LoginViewModel loginViewModel;
    private View includeAccountActivationView;
    private View includeUserProfileContent;
    private NotificationsViewModel notificationsViewModel;
    private Button buttonCustomLists;
    private Button followedListsButton;




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
        textViewResendEmailMessage = view.findViewById(R.id.textView_userProfileFragment_resendEmailMessage);
        buttonLogout = view.findViewById(R.id.button_personalProfileFragment_logout);
        buttonChangePassword = view.findViewById(R.id.button_personalProfileFragment_changePassword);
        buttonCustomLists = view.findViewById(R.id.button_personalProfileFragment_customLists);
        followedListsButton = view.findViewById(R.id.button_personalProfileFragment_followedLists);
        remoteConfigServer = RemoteConfigServer.getInstance();
        // setting listeners
        buttonLogout.setOnClickListener(this);
        buttonChangePassword.setOnClickListener(this);
        buttonCustomLists.setOnClickListener(this);
        followedListsButton.setOnClickListener(this);
        //
        buttonResendEmail = view.findViewById(R.id.button_personalProfileFragment_resendEmail);
        buttonResendEmail.setOnClickListener(this);
        includeAccountActivationView = view.findViewById(R.id.include_personalProfileFragment_accountVerification);
        includeUserProfileContent = view.findViewById(R.id.include_personalProfileFragment_myLists);
        notificationsViewModel = new ViewModelProvider(requireActivity()).get(NotificationsViewModel.class);

        hideResendEmail();


    }// end onViewCreated()

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // firebase logging
        FirebaseAnalytics firebaseAnalytics = FirebaseAnalytics.getInstance();
        firebaseAnalytics.logScreenEvent(this, "User profile page", getContext());
        notificationsViewModel = new ViewModelProvider(requireActivity()).get(NotificationsViewModel.class);
        notificationsViewModel.getNotificationsStatus().observe(getViewLifecycleOwner(), status -> {
            switch (status) {
                case GOT_NEW_NOTIFICATIONS:
                    ((MainActivity) getActivity()).activateNotificationsIcon();
                    break;
                case NO_NOTIFICATIONS:
                    break;
            }
        });


        //
        loginViewModel = new ViewModelProvider(requireActivity()).get(LoginViewModel.class);
        loginViewModel.getObservableLoginResult().observe(getViewLifecycleOwner(), loginResult -> {
            switch (loginResult) {
                case SUCCESS: {
                    User user = loginViewModel.getObservableLoggedUser().getValue();
                    String profilePicturePath = user.getProfilePicturePath();

                    ImageUtilities.loadCircularImageInto(profilePicturePath, profilePicture, getContext());
                    textViewEmail.setText(user.getEmail());
                }
                break;
                case LOGGED_OUT:
                    break;
                case REMEMBER_ME_EXISTS:
                    try {
                        User user = loginViewModel.getObservableLoggedUser().getValue();
                        String profilePicturePath = user.getProfilePicturePath();

                        ImageUtilities.loadCircularImageInto(profilePicturePath, profilePicture, getContext());
                        textViewEmail.setText(user.getEmail());
                    }  catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                case IS_PENDING_USER: {
                    showResendEmail();

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
                    hideResendEmail();
                    break;
            }// switch
        });

    }// end onActivityCreated()


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
            NavDirections customListBrowserFragment =
                    PersonalProfileFragmentDirections.actionPersonalProfileFragmentToCustomListBrowserFragment(fetchMode);
            Navigation.findNavController(v).navigate(customListBrowserFragment);
        }
        else if(v.getId() == followedListsButton.getId()){
            String fetchMode = "fetch_subscribed_lists";
            NavDirections customListBrowserFragment =
                    PersonalProfileFragmentDirections.actionPersonalProfileFragmentToCustomListBrowserFragment(fetchMode);
            Navigation.findNavController(v).navigate(customListBrowserFragment);
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

    private void showResendEmail() {
        includeAccountActivationView.setVisibility(View.VISIBLE);
        includeUserProfileContent.setVisibility(View.GONE);
    }

    private void hideResendEmail() {
        includeAccountActivationView.setVisibility(View.GONE);
        includeUserProfileContent.setVisibility(View.VISIBLE);
    }

    public void showCenteredToast(String message) {
        final Toast toast = Toast.makeText(getContext(), message, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    private void checkForNewNotifications() {
        if(notificationsViewModel!=null) {
            if(notificationsViewModel.getNotificationsStatus().getValue() == NotificationsViewModel.NotificationsStatus.GOT_NEW_NOTIFICATIONS) {
                ((MainActivity) requireActivity()).activateNotificationsIcon();
            }
            else {
                ((MainActivity) requireActivity()).deactivateNotificationsIcon();
            }
        }
    }




}// end PersonalProfileFragment class