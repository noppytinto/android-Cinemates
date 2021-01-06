package mirror42.dev.cinemates.ui.login;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONObject;

import mirror42.dev.cinemates.R;
import mirror42.dev.cinemates.model.User;
import mirror42.dev.cinemates.utilities.FirebaseEventsLogger;
import mirror42.dev.cinemates.utilities.MyUtilities;
import mirror42.dev.cinemates.utilities.RemoteConfigServer;


public class LoginFragment extends Fragment  implements
        View.OnClickListener,
        CompoundButton.OnCheckedChangeListener {
    private TextInputLayout textInputLayoutEmail;
    private TextInputLayout textInputLayoutPassword;
    private TextInputEditText editTextEmail;
    private TextInputEditText editTextPassword;
    private Button buttonStandardLogin;
    private Button buttonSignUp;
    private ProgressBar spinner;
    private CheckBox checkBoxRememberMe;
    private LoginViewModel loginViewModel;
    private View view;
    private RemoteConfigServer remoteConfigServer;
    private boolean rememberMeIsActive;
    private static boolean isLogged;
    private static User loggedUser;

    public interface ProfileImageListener {
        public void onProfileImageReady(String profileImagePath);
    }




    //----------------------------------------------------------------------

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_login, container, false);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.view = view;
        spinner = view.findViewById(R.id.progresBar_loginFragment);
        editTextEmail = (TextInputEditText) view.findViewById(R.id.editText_loginFragment_email);
        editTextPassword = (TextInputEditText) view.findViewById(R.id.editText_loginFragment_password);
        textInputLayoutEmail = (TextInputLayout) view.findViewById(R.id.textInputLayout_loginFragment_email);
        textInputLayoutPassword = (TextInputLayout) view.findViewById(R.id.textInputLayout_loginFragment_password);
        buttonStandardLogin = (Button) view.findViewById(R.id.button_loginFragment_standardLogin);
        checkBoxRememberMe = view.findViewById(R.id.checkBox_loginFragment_rememberMe);
        remoteConfigServer = RemoteConfigServer.getInstance();
        buttonSignUp = (Button) view.findViewById(R.id.button_loginFragment_signUp);

        // setting listeners
        buttonStandardLogin.setOnClickListener(this);
        buttonSignUp.setOnClickListener(this);
        checkBoxRememberMe.setOnCheckedChangeListener(this);


        // firebase logging
        FirebaseEventsLogger firebaseEventsLogger = FirebaseEventsLogger.getInstance();
        firebaseEventsLogger.logScreenEvent(this, "Login page", getContext());

        //
        FragmentActivity fa = requireActivity();
        loginViewModel = new ViewModelProvider(requireActivity()).get(LoginViewModel.class);
        loginViewModel.init(rememberMeIsActive);
        loginViewModel.getUser().observe(getViewLifecycleOwner(), new Observer<User>() {
            @Override
            public void onChanged(@Nullable User user) {
                spinner.setVisibility(View.GONE);

                if(user != null) {
                    isLogged = true;
                    loggedUser = user;
                    MyUtilities.showCenteredToast( "Authentication server:\nlogin successful\nwelcome: " + user.getEmail(), getContext());

                    // load profile picture
                    String profilePicturePath = user.getProfilePicturePath();
                    LoginActivity loginActivity = (LoginActivity) getActivity();
                    if(profilePicturePath == null || profilePicturePath.isEmpty()) {
//                        loginActivity.onProfileImageReady("LOGGED_BUT_NO_PICTURE");

                    }
                    else {
//                        ImageUtilities.loadCircularImageInto(remoteConfigServer.getCloudinaryDownloadBaseUrl() + profilePicturePath, profilePicture, getContext());
//                        loginActivity.onProfileImageReady(profilePicturePath);
                    }


                    // sending image back to login activity for main activity toolbar to be used

                    // eventually save login data on local store
                    if(rememberMeIsActive) {
                        remoteConfigServer = RemoteConfigServer.getInstance();
                        MyUtilities.encryptFile(remoteConfigServer.getCinematesData(),
                                                MyUtilities.convertUserInJSonString(user),
                                                getContext());
                    }


                }
                else {
                    notifyError();
                    MyUtilities.showCenteredToast("Authentication server:\ninvalid credentials! D:", getContext());
                }
            }
        });

        loginViewModel.getLoginResult().observe(getViewLifecycleOwner(), new Observer<MyUtilities.LoginResult>() {
            @Override
            public void onChanged(@Nullable MyUtilities.LoginResult loginResult) {
                if(loginResult == MyUtilities.LoginResult.FAILED) {
                    spinner.setVisibility(View.GONE);
                    MyUtilities.showCenteredToast("Authentication server:\nCannot establish connection! D:", getContext());

                }
                else if(loginResult == MyUtilities.LoginResult.INVALID_REQUEST) {
                    spinner.setVisibility(View.GONE);
                    MyUtilities.showCenteredToast("Autorization server:\ncannot make request! D:", getContext());
                }
            }
        });

        //
        init();

        // fast login
        editTextEmail.setText("dev.mirror42@gmail.com");
        editTextPassword.setText("a");





    }// end onViewCreated()





    //-------------------------------------------------------------------------------- METHODS

    //-------------------- REMEMBER ME methods

    private void init() {
        boolean thereIsArememberMe = checkAnyPreviousRememberMe();

        if(isLogged) {
//            ImageUtilities.loadCircularImageInto(remoteConfigServer.getCloudinaryDownloadBaseUrl() + loggedUser.getProfilePicturePath(), profilePicture, getContext());
//            hideLoginComponents();
        }
        else if(thereIsArememberMe) {
            loadRememberMeData();
            hideLoginComponents();
        }
        else {
            this.rememberMeIsActive = false;
            showLoginComponents();
        }
    }

    /**
     * read a previous remember me state (if any)
     * on local store
     */
    private boolean checkAnyPreviousRememberMe() {
        boolean result = MyUtilities.checkFileExists(remoteConfigServer.getCinematesData(), getContext());
        return result;
    }

    private void loadRememberMeData() {
        try {
            JSONObject jsonObject = new JSONObject(MyUtilities.decryptFile(remoteConfigServer.getCinematesData(), getContext()));

            loadProfilePicture(jsonObject.getString("ProfileImage"), getContext());
            this.rememberMeIsActive = true;

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void hideLoginComponents() {
        textInputLayoutEmail.setVisibility(View.GONE);
        textInputLayoutPassword.setVisibility(View.GONE);
        editTextEmail.setVisibility(View.GONE);
        editTextPassword.setVisibility(View.GONE);
        buttonStandardLogin.setVisibility(View.GONE);
        checkBoxRememberMe.setVisibility(View.GONE);
    }

    private void showLoginComponents() {
        textInputLayoutEmail.setVisibility(View.VISIBLE);
        textInputLayoutPassword.setVisibility(View.VISIBLE);
        editTextEmail.setVisibility(View.VISIBLE);
        editTextPassword.setVisibility(View.VISIBLE);
        buttonStandardLogin.setVisibility(View.VISIBLE);
        checkBoxRememberMe.setVisibility(View.VISIBLE);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if(isChecked) {
            rememberMeIsActive = true;
            loginViewModel.setRememberMe(true);
        }
        else {
            if(rememberMeIsActive) {
                rememberMeIsActive = false;
                editTextPassword.setText("");
//                setDefaultProfilePicture();

                // delete remember me data
                MyUtilities.deletFile(remoteConfigServer.getCinematesData(), getContext());

                //
                MyUtilities.showCenteredToast( "Credenziali eliminate", getContext());
            }
        }
    }





    //-------------------- LOGIN methods

    @Override
    public void onClick(View v) {
        if (v.getId() == buttonStandardLogin.getId()) {
            spinner.setVisibility(View.VISIBLE);

            // firebase logging
            FirebaseEventsLogger firebaseEventsLogger = FirebaseEventsLogger.getInstance();
            firebaseEventsLogger.logLoginEvent("email + password", getContext());


            // checks
            String email = editTextEmail.getText().toString();
            if (email.isEmpty()) {
                textInputLayoutEmail.setError("inserire mail!");
                return;
            }

            String password = editTextPassword.getText().toString();
            if (password.isEmpty()) {
                textInputLayoutPassword.setError("inserire password!");
                return;
            }

            //
            resetTextfieldAppearance();

            //

            loginViewModel.standardLogin(email, password, getContext());
        }
        else if (v.getId() == buttonSignUp.getId()) {

            Navigation.findNavController(v).navigate(R.id.action_loginFragment_to_signUpFragment);

//            // Choose authentication providers
//            List<AuthUI.IdpConfig> providers = Arrays.asList(
//                    new AuthUI.IdpConfig.EmailBuilder().build(),
//                    new AuthUI.IdpConfig.GoogleBuilder().build());
//
//            int RC_SIGN_IN = 99;
//            // Create and launch sign-in intent
//            startActivityForResult(
//                    AuthUI.getInstance()
//                            .createSignInIntentBuilder()
//                            .setAvailableProviders(providers)
//                            .build(),
//                    RC_SIGN_IN);


        }
    }

//    @Override
//    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//
//        if (requestCode == 99) {
//            IdpResponse response = IdpResponse.fromResultIntent(data);
//
//            if (resultCode == RESULT_OK) {
//                // Successfully signed in
//                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
//                FirebaseAuth auth = FirebaseAuth.getInstance();
//                user.sendEmailVerification()
//                        .addOnCompleteListener(new OnCompleteListener<Void>() {
//                            @Override
//                            public void onComplete(@NonNull Task<Void> task) {
//                                if (task.isSuccessful()) {
//                                    Log.d(TAG, "Email sent.");
//                                }
//                            }
//                        });
//                MyUtilities.showCenteredToast(user.getEmail(), getContext());
//                // ...
//            } else {
//                // Sign in failed. If response is null the user canceled the
//                // sign-in flow using the back button. Otherwise check
//                // response.getError().getErrorCode() and handle the error.
//                // ...
//            }
//        }
//    }





    private void resetTextfieldAppearance() {
        textInputLayoutEmail.setError(null);
        textInputLayoutPassword.setError(null);

    }


    private void loadProfilePicture(String imagePath, Context context) {
        RemoteConfigServer remoteConfigServer = RemoteConfigServer.getInstance();

        if(imagePath!=null || ( ! imagePath.isEmpty())) {
            // load profile picture in login activity
//            ImageUtilities.loadCircularImageInto(remoteConfigServer.getCloudinaryDownloadBaseUrl() + imagePath, profilePicture, context);
        }
    }

    private void notifyError() {
        textInputLayoutEmail.setError("credenziali non valide!");
        textInputLayoutPassword.setError("credenziali non valide!");
    }



}// end LoginFragment class