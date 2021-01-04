package mirror42.dev.cinemates.ui.login;

import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONObject;

import mirror42.dev.cinemates.R;
import mirror42.dev.cinemates.model.User;
import mirror42.dev.cinemates.utilities.FirebaseEventsLogger;
import mirror42.dev.cinemates.utilities.ImageUtilities;
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
    private Button buttonLogout;
    private ProgressBar spinner;
    private CheckBox checkBoxRememberMe;
    private ImageView profilePicture;
    private LoginViewModel loginViewModel;
    private View view;
    private RemoteConfigServer remoteConfigServer;
    private boolean rememberMeIsActive;
    private static boolean isLogged;
    private static User loggedUser;
    private TextView textViewEmail;

    public interface ProfileImageListener {
        public void onProfileImageReady(String profileImagePath);
    }




    //----------------------------------------------------------------------

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
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
        profilePicture = view.findViewById(R.id.imageView_loginFragment_profilePicture);
        buttonLogout = view.findViewById(R.id.button_loginFragment_logout);
        remoteConfigServer = RemoteConfigServer.getInstance();
        textViewEmail = (TextView) view.findViewById(R.id.textView_loginFragment_email);

        // setting listeners
        buttonStandardLogin.setOnClickListener(this);
        buttonLogout.setOnClickListener(this);
        checkBoxRememberMe.setOnCheckedChangeListener(this);


        // firebase logging
        FirebaseEventsLogger firebaseEventsLogger = FirebaseEventsLogger.getInstance();
        firebaseEventsLogger.logScreenEvent(this, "Login page", getContext());

        //
        loginViewModel = new ViewModelProvider(this).get(LoginViewModel.class);
        loginViewModel.init(rememberMeIsActive);
        loginViewModel.getUser().observe(getViewLifecycleOwner(), new Observer<User>() {
            @Override
            public void onChanged(@Nullable User user) {
                spinner.setVisibility(View.GONE);

                if(user != null) {
                    isLogged = true;
                    loggedUser = user;
                    MyUtilities.showCenteredToast( "Authentication server:\nlogin successful\nwelcome: " + user.getEmail(), getContext());
                    hideLoginComponents();
                    showLogoutComponents(user.getEmail());

                    // load profile picture
                    String profilePicturePath = user.getProfilePicturePath();
                    LoginActivity loginActivity = (LoginActivity) getActivity();
                    if(profilePicturePath == null || profilePicturePath.isEmpty()) {
                        loginActivity.onProfileImageReady("LOGGED_BUT_NO_PICTURE");
                    }
                    else {
                        ImageUtilities.loadCircularImageInto(remoteConfigServer.getCloudinaryDownloadBaseUrl() + profilePicturePath, profilePicture, getContext());
                        loginActivity.onProfileImageReady(profilePicturePath);
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
//        editTextEmail.setText("dev.mirror42@gmail.com");
//        editTextPassword.setText("a");





    }// end onViewCreated()





    //-------------------------------------------------------------------------------- METHODS

    //-------------------- REMEMBER ME methods

    private void init() {
        boolean thereIsArememberMe = checkAnyPreviousRememberMe();

        if(isLogged) {
            ImageUtilities.loadCircularImageInto(remoteConfigServer.getCloudinaryDownloadBaseUrl() + loggedUser.getProfilePicturePath(), profilePicture, getContext());
            hideLoginComponents();
            showLogoutComponents(loggedUser.getEmail());
        }
        else if(thereIsArememberMe) {
            loadRememberMeData();
            hideLoginComponents();
        }
        else {
            this.rememberMeIsActive = false;
            showLoginComponents();
            hideLogoutComponents();
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

            showLogoutComponents(jsonObject.getString("Email"));

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

    private void hideLogoutComponents() {
        textViewEmail.setVisibility(View.GONE);
        buttonLogout.setVisibility(View.GONE);
    }

    private void showLogoutComponents(String email) {
        textViewEmail.setVisibility(View.VISIBLE);
        textViewEmail.setText(email);
        buttonLogout.setVisibility(View.VISIBLE);
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
                setDefaultProfilePicture();

                // delete remember me data
                MyUtilities.deletFile(remoteConfigServer.getCinematesData(), getContext());

                //
                MyUtilities.showCenteredToast( "Credenziali eliminate", getContext());
            }
        }
    }

    private void setDefaultProfilePicture() {
        try {
            profilePicture.setImageDrawable(ImageUtilities.getDefaultProfilePictureIcon(getContext()));

            // sending image back to login activity for main activity toolbar to be used
            LoginActivity loginActivity = (LoginActivity) getActivity();
            loginActivity.onProfileImageReady("LOGOUT");

        } catch (Resources.NotFoundException e) {
            e.printStackTrace();
        }
    }



    //-------------------- LOGIN methods

    @Override
    public void onClick(View v) {
        if(v.getId() == buttonStandardLogin.getId()) {
            spinner.setVisibility(View.VISIBLE);

            // firebase logging
            FirebaseEventsLogger firebaseEventsLogger = FirebaseEventsLogger.getInstance();
            firebaseEventsLogger.logLoginEvent("email + password", getContext());


            // checks
            String email = editTextEmail.getText().toString();
            if(email.isEmpty()) {
                textInputLayoutEmail.setError("inserire mail!");
                return;
            }

            String password = editTextPassword.getText().toString();
            if(password.isEmpty()) {
                textInputLayoutPassword.setError("inserire password!");
                return;
            }

            //
            resetTextfieldAppearance();

            //

            loginViewModel.standardLogin(email, password, getContext());
        }
        else if(v.getId() == buttonLogout.getId()) {
            isLogged = false;
            showLoginComponents();
            hideLogoutComponents();
            rememberMeIsActive = false;
            editTextPassword.setText("");
            try {
                setDefaultProfilePicture();

            } catch (Resources.NotFoundException e) {
                e.printStackTrace();
            }
            MyUtilities.deletFile(remoteConfigServer.getCinematesData(), getContext());
            checkBoxRememberMe.setChecked(false);
        }
    }



    private void resetTextfieldAppearance() {
        textInputLayoutEmail.setError(null);
        textInputLayoutPassword.setError(null);

    }


    private void loadProfilePicture(String imagePath, Context context) {
        RemoteConfigServer remoteConfigServer = RemoteConfigServer.getInstance();

        if(imagePath!=null || ( ! imagePath.isEmpty())) {
            // load profile picture in login activity
            ImageUtilities.loadCircularImageInto(remoteConfigServer.getCloudinaryDownloadBaseUrl() + imagePath, profilePicture, context);
        }
    }

    private void notifyError() {
        textInputLayoutEmail.setError("credenziali non valide!");
        textInputLayoutPassword.setError("credenziali non valide!");
    }



}// end LoginFragment class