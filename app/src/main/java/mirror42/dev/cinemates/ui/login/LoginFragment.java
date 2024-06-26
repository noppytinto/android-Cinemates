package mirror42.dev.cinemates.ui.login;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
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
    private final String TAG = getClass().getSimpleName();

    private GoogleSignInClient mGoogleSignInClient;
    private int RC_SIGN_IN = 0;

    private TextInputLayout textInputLayoutEmail;
    private TextInputLayout textInputLayoutPassword;
    private TextInputEditText editTextEmail;
    private TextInputEditText editTextPassword;
    private TextView textViewResetPassword;
    private Button buttonStandardLogin;
    private Button buttonSignUp;
    private Button buttonLoginGoogle;
    private CheckBox checkBoxRememberMe;
    private LoginViewModel loginViewModel;
    private View view;
    private RemoteConfigServer remoteConfigServer;
    private boolean rememberMeIsActive;
    private ProgressDialog progressDialog;






    //---------------------------------------------------------------------- ANDROID METHODS

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
       // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(getContext(),gso);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleGoogleAccess(task);
        }
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
        editTextEmail =  view.findViewById(R.id.editText_loginFragment_email);
        editTextPassword = view.findViewById(R.id.editText_loginFragment_password);
        textInputLayoutEmail = view.findViewById(R.id.textInputLayout_loginFragment_email);
        textInputLayoutPassword = view.findViewById(R.id.textInputLayout_loginFragment_password);
        buttonStandardLogin = view.findViewById(R.id.button_loginFragment_standardLogin);
        checkBoxRememberMe = view.findViewById(R.id.checkBox_loginFragment_rememberMe);
        remoteConfigServer = RemoteConfigServer.getInstance();
        buttonSignUp = view.findViewById(R.id.button_loginFragment_signUp);
        textViewResetPassword = view.findViewById(R.id.textView_loginFragment_resetPassword);
        buttonLoginGoogle = view.findViewById(R.id.button_loginFragment_googleLogin);

        // setting listeners
        buttonStandardLogin.setOnClickListener(this);
        buttonSignUp.setOnClickListener(this);
        buttonLoginGoogle.setOnClickListener(this);
        checkBoxRememberMe.setOnCheckedChangeListener(this);
        textViewResetPassword.setOnClickListener(this);
        //


        // firebase logging
        FirebaseAnalytics firebaseAnalytics = FirebaseAnalytics.getInstance();
        firebaseAnalytics.logScreenEvent(this, getString(R.string.login_page_firebase_login_page), getContext());

        //
        loginViewModel = new ViewModelProvider(requireActivity()).get(LoginViewModel.class);
        loginViewModel.getObservableLoginResult().observe(getViewLifecycleOwner(), loginResult -> {
            hideProgressDialog();

            switch (loginResult) {
                case SUCCESS: {
                    User user = loginViewModel.getObservableLoggedUser().getValue();
                    try {
                        showCenteredToast( "Authentication server:\nlogin successful\nwelcome: " + user.getEmail());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    // save remember me data only if is not pending user anymore
                    if(loginResult != LoginViewModel.LoginResult.IS_PENDING_USER)
                        loginViewModel.saveRememberMeDataIfChecked(checkBoxRememberMe.isChecked(), getContext());

                    NavController navController = Navigation.findNavController(view);
                    navController.popBackStack();
                    navController.navigate(R.id.personalProfileFragment);
                }
                    break;
                case INVALID_CREDENTIALS:
                    showCenteredToast("Authentication server:\nCredenziali non valide\no utente inesistente");
                    break;
                case FAILED:
                    showCenteredToast("Authentication server:\nCannot establish connection! D:");
                    break;
                case INVALID_REQUEST:
                    showCenteredToast("Authentication server:\ncannot make request! D:");
                    break;
                case IS_PENDING_USER: {
                    // checking if email verification has been clicked
                    boolean accountEnabled = loginViewModel.checkEmailVerificationState();
                    if(accountEnabled) {
                        // insert into postgrest database
                        // and show new user profile page
                        loginViewModel.insertIntoPostgres();
                    }
                    else {
                        // show restricted user profile page
                        showCenteredToast("Authentication server:\nemail ancora non approvata\ncontrolla la tua posta");
                        Navigation.findNavController(view).popBackStack();
                        Navigation.findNavController(view).navigate(R.id.action_global_personalProfileFragment);
                    }
                }
                    break;
                case IS_NOT_PENDING_USER_ANYMORE: {
                    String email = editTextEmail.getText().toString();
                    String password = editTextPassword.getText().toString();
                    loginViewModel.standardLogin(email, MyUtilities.SHA256encrypt(password));
                }
                    break;
            }// switch
        });

        // fast login
//        editTextEmail.setText(R.string.login_page_email_demo);
//        editTextPassword.setText(R.string.login_page_password_demo);

    }// end onViewCreated()

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: ");
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        MenuItem item = menu.findItem(R.id.menu_item_login);
        if(item!=null)
            item.setVisible(false);
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
            showProgressDialog();

            // firebase logging
            FirebaseAnalytics firebaseAnalytics = FirebaseAnalytics.getInstance();
            firebaseAnalytics.logLoginEvent(getString(R.string.login_page_firebase_standard_login_method), getContext());

            // checks
            boolean allFieldsAreFilled = checkFieldsAreFilled();

            //
            if(allFieldsAreFilled) {
                // get data and trim text
                String email = editTextEmail.getText().toString().trim();
                String password = editTextPassword.getText().toString().trim();

                if( ! android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    textInputLayoutEmail.setError("Il formato mail non e' valido!");
                }
                else {
                    textInputLayoutEmail.setError(null);
                    loginViewModel.login(email, password);
                }
            }
            else {
                hideProgressDialog();
            }
        }
        else if (v.getId() == buttonSignUp.getId()) {

            Navigation.findNavController(v).navigate(R.id.action_loginFragment_to_signUpFragment);
        }
        else if(v.getId() == textViewResetPassword.getId()){
            Navigation.findNavController(v).navigate(R.id.action_loginFragment_to_resetPasswordFragment);
        }else if (v.getId() == buttonLoginGoogle.getId()){
            accessWithGoogle();
        }
    }// end onClick()



    //-------------------------------------------------------------------------------- MY METHODS

    private void accessWithGoogle(){
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void handleGoogleAccess(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            // Signed in successfully, show authenticated UI.
            Navigation.findNavController(view).navigate(R.id.action_loginFragment_to_googleLoginFragment);
        } catch (ApiException e) {
            Log.v(TAG, "google access:failed code=" + e.getStatusCode());
        } catch (Exception e) {
           e.printStackTrace();
        }
    }

    private void showProgressDialog() {
        //notes: Declare progressDialog before so you can use .hide() later!
        try {
            progressDialog = new ProgressDialog(requireContext());
            progressDialog.setMessage("Login in corso...");
            progressDialog.setCancelable(false);
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void hideProgressDialog() {
        if(progressDialog!=null)
            progressDialog.hide();
    }

    private boolean checkFieldsAreFilled() {
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

    public void showCenteredToast(String message) {
        final Toast toast = Toast.makeText(getContext(), message, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }


}// end LoginFragment class