package mirror42.dev.cinemates.ui.userprofile;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import mirror42.dev.cinemates.R;
import mirror42.dev.cinemates.model.User;
import mirror42.dev.cinemates.ui.login.LoginViewModel;
import mirror42.dev.cinemates.utilities.FirebaseAnalytics;
import mirror42.dev.cinemates.utilities.ImageUtilities;
import mirror42.dev.cinemates.utilities.MyUtilities;
import mirror42.dev.cinemates.utilities.RemoteConfigServer;


public class UserProfileFragment extends Fragment implements View.OnClickListener {
    private final String TAG = getClass().getSimpleName();
    private View view;
    private ImageView profilePicture;
    private TextView textViewEmail;
    private TextView textViewResendEmailMessage;
    private Button buttonLogout;
    private Button buttonResendEmail;
    private RemoteConfigServer remoteConfigServer;
    private LoginViewModel loginViewModel;
    private View includeAccountActivationView;
    private View includeUserProfileContent;



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
        return inflater.inflate(R.layout.fragment_user_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.view = view;
        profilePicture = view.findViewById(R.id.imageView_userProfileFragment_profilePicture);
        textViewEmail = view.findViewById(R.id.textView_userProfileFragment_email);
        textViewResendEmailMessage = view.findViewById(R.id.textView_userProfileFragment_resendEmailMessage);
        buttonLogout = view.findViewById(R.id.button_userProfileFragment_logout);
        remoteConfigServer = RemoteConfigServer.getInstance();
        // setting listeners
        buttonLogout.setOnClickListener(this);
        //
        buttonResendEmail = view.findViewById(R.id.button_userProfileFragment_resendEmail);
        buttonResendEmail.setOnClickListener(this);
        includeAccountActivationView = view.findViewById(R.id.include_userProfileFragment_accountVerification);
        includeUserProfileContent = view.findViewById(R.id.include_userProfileFragment_content);
        hideResendEmail();

        // firebase logging
        FirebaseAnalytics firebaseAnalytics = FirebaseAnalytics.getInstance();
        firebaseAnalytics.logScreenEvent(this, "User profile page", getContext());

        //
        loginViewModel = new ViewModelProvider(requireActivity()).get(LoginViewModel.class);
        loginViewModel.getLoginResult().observe(getViewLifecycleOwner(), loginResult -> {
            switch (loginResult) {
                case SUCCESS: {
                    User user = loginViewModel.getUser().getValue();
                    String profilePicturePath = user.getProfilePicturePath();

                    ImageUtilities.loadCircularImageInto(remoteConfigServer.getCloudinaryDownloadBaseUrl() + profilePicturePath, profilePicture, getContext());
                    textViewEmail.setText(user.getEmail());
                }
                    break;
                case LOGGED_OUT:
                    break;
                case REMEMBER_ME_EXISTS:
                    try {
                        User user = loginViewModel.getUser().getValue();
                        String profilePicturePath = user.getProfilePicturePath();

                        ImageUtilities.loadCircularImageInto(remoteConfigServer.getCloudinaryDownloadBaseUrl() + profilePicturePath, profilePicture, getContext());
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

                            ImageUtilities.loadCircularImageInto(remoteConfigServer.getCloudinaryDownloadBaseUrl() + profilePicturePath, profilePicture, getContext());
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
    }// end onViewCreated()

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: ");
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
                MyUtilities.showCenteredToast("Email attivazione riniviata, era esegui un Logout e controlla la posta.", getContext());
                buttonResendEmail.setText("Email attivazione reinviata!");
                buttonResendEmail.setEnabled(false);
            }
        }
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        MenuItem item = menu.findItem(R.id.menu_item_login);
        if(item!=null)
            item.setVisible(false);
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

}// end UserProfileFragment class