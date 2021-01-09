package mirror42.dev.cinemates.ui.login;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import mirror42.dev.cinemates.MainActivity;
import mirror42.dev.cinemates.R;
import mirror42.dev.cinemates.model.User;
import mirror42.dev.cinemates.utilities.FirebaseAnalytics;
import mirror42.dev.cinemates.utilities.MyUtilities;
import mirror42.dev.cinemates.utilities.RemoteConfigServer;


public class LoginFragment extends Fragment  implements
        View.OnClickListener,
        CompoundButton.OnCheckedChangeListener{
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



    //---------------------------------------------------------------------- ANDROID METHODS
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        MainActivity mainActivity = (MainActivity) getActivity();
        mainActivity.hideLogo();
    }

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
        editTextEmail =  view.findViewById(R.id.editText_loginFragment_email);
        editTextPassword = view.findViewById(R.id.editText_loginFragment_password);
        textInputLayoutEmail = view.findViewById(R.id.textInputLayout_loginFragment_email);
        textInputLayoutPassword = view.findViewById(R.id.textInputLayout_loginFragment_password);
        buttonStandardLogin = view.findViewById(R.id.button_loginFragment_standardLogin);
        checkBoxRememberMe = view.findViewById(R.id.checkBox_loginFragment_rememberMe);
        remoteConfigServer = RemoteConfigServer.getInstance();
        buttonSignUp = view.findViewById(R.id.button_loginFragment_signUp);
        // setting listeners
        buttonStandardLogin.setOnClickListener(this);
        buttonSignUp.setOnClickListener(this);
        checkBoxRememberMe.setOnCheckedChangeListener(this);

        //


        // firebase logging
        FirebaseAnalytics firebaseAnalytics = FirebaseAnalytics.getInstance();
        firebaseAnalytics.logScreenEvent(this, getString(R.string.login_page_firebase_login_page), getContext());

        //
        loginViewModel = new ViewModelProvider(requireActivity()).get(LoginViewModel.class);
        loginViewModel.getLoginResult().observe(getViewLifecycleOwner(), loginResult -> {
            switch (loginResult) {
                case SUCCESS: {
                    User user = loginViewModel.getUser().getValue();
                    try {
                        MyUtilities.showCenteredToast( "Authentication server:\nlogin successful\nwelcome: " + user.getEmail(), getContext());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    // save remember me data only if is not pending user anymore
                    if(loginResult != LoginViewModel.LoginResult.IS_PENDING_USER)
                        loginViewModel.saveRememberMeDataIfChecked(checkBoxRememberMe.isChecked(), getContext());

                    NavController navController = Navigation.findNavController(view);
                    navController.popBackStack();
                    navController.navigate(R.id.userProfileFragment);
                }
                    break;
                case INVALID_CREDENTIALS:
                    spinner.setVisibility(View.GONE);
                    MyUtilities.showCenteredToast("Authentication server:\nCredenziali non valide\no utente inesistente", getContext());
                    break;
                case FAILED:
                    spinner.setVisibility(View.GONE);
                    MyUtilities.showCenteredToast("Authentication server:\nCannot establish connection! D:", getContext());
                    break;
                case INVALID_REQUEST:
                    spinner.setVisibility(View.GONE);
                    MyUtilities.showCenteredToast("Authentication server:\ncannot make request! D:", getContext());
                    break;
                case IS_PENDING_USER: {
                    spinner.setVisibility(View.GONE);

                    // checking if email verification has been clicked
                    boolean accountEnabled = loginViewModel.checkEmailVerificationState();
                    if(accountEnabled) {
                        // insert into postgrest database
                        // and show new user profile page
                        loginViewModel.insertIntoPostgres();
                    }
                    else {
                        // show restricted user profile page
                        MyUtilities.showCenteredToast("Authentication server:\nemail ancora non approvata\ncontrolla la tua posta", getContext());
                        NavController navController = Navigation.findNavController(view);
                        navController.popBackStack();
                        navController.navigate(R.id.userProfileFragment);
                    }
                }
                    break;
                case IS_NOT_PENDING_USER: {
                    String email = editTextEmail.getText().toString();
                    String password = editTextPassword.getText().toString();
                    loginViewModel.standardLogin(email, MyUtilities.SHA256encrypt(password));
                }
                    break;
            }// switch
        });

        // fast login
        editTextEmail.setText(R.string.login_page_email_demo);
        editTextPassword.setText(R.string.login_page_password_demo);

    }// end onViewCreated()

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        MenuItem item = menu.findItem(R.id.menu_item_login);
        if(item!=null)
            item.setVisible(false);

        MainActivity mainActivity = (MainActivity) getActivity();
        mainActivity.hideLogo();
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if(isChecked) {
            rememberMeIsActive = true;
        }
        else {
            rememberMeIsActive = false;
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == buttonStandardLogin.getId()) {
            spinner.setVisibility(View.VISIBLE);

            // firebase logging
            FirebaseAnalytics firebaseAnalytics = FirebaseAnalytics.getInstance();
            firebaseAnalytics.logLoginEvent(getString(R.string.login_page_firebase_standard_login_method), getContext());

            // checks
            boolean allFieldsAreOk = checkFields();

            //
            if(allFieldsAreOk) {
                String email = editTextEmail.getText().toString();
                String password = editTextPassword.getText().toString();

                //
                loginViewModel.checkIfIsPendingUser(email, password);
            }
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
    }// end onClick()

    @Override
    public void onDetach() {
        super.onDetach();
        MainActivity mainActivity = (MainActivity) getActivity();
        mainActivity.showLogo();
    }


    //-------------------------------------------------------------------------------- METHODS

    private boolean checkFields() {
        String email = editTextEmail.getText().toString();

        if (email.isEmpty()) {
            textInputLayoutEmail.setError(getString(R.string.login_page_no_email_error));
            return false;
        }

        String password = editTextPassword.getText().toString();
        if (password.isEmpty()) {
            textInputLayoutPassword.setError(getString(R.string.login_page_no_password_error));
            return false;
        }

        resetTextfieldAppearance();

        return true;
    }

    private void saveRememberMeDataIfChecked(User user) {

    }

    private void resetTextfieldAppearance() {
        textInputLayoutEmail.setError(null);
        textInputLayoutPassword.setError(null);

    }

}// end LoginFragment class