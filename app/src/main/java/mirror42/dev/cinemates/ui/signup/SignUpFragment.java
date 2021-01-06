package mirror42.dev.cinemates.ui.signup;

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
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import mirror42.dev.cinemates.R;
import mirror42.dev.cinemates.model.User;
import mirror42.dev.cinemates.utilities.FirebaseEventsLogger;
import mirror42.dev.cinemates.utilities.MyUtilities;
import mirror42.dev.cinemates.utilities.RemoteConfigServer;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class SignUpFragment extends Fragment implements
        View.OnClickListener,
        CompoundButton.OnCheckedChangeListener,
        Callback {
    private final String TAG = this.getClass().getSimpleName();
    //
    private TextInputLayout textInputLayoutUsername;
    private TextInputLayout textInputLayoutEmail;
    private TextInputLayout textInputLayoutPassword;
    private TextInputLayout textInputLayoutRepeatPassword;
    private TextInputLayout textInputLayoutFirstName;
    private TextInputLayout textInputLayoutLastName;
    private TextInputLayout textInputLayoutBirthDate;
    //
    private TextInputEditText editTextUsername;
    private TextInputEditText editTextEmail;
    private TextInputEditText editTextPassword;
    private TextInputEditText editTextRepeatPassword;
    private TextInputEditText editTextFirstName;
    private TextInputEditText editTextLastName;
    private TextInputEditText editTextBirthDate;
    //
    private CheckBox checkBoxPromo;
    private CheckBox checkBoxAnalytics;
    private CheckBox checkBoxTermsAndConditions;

    //
    private Button buttonsignUp;
    private ProgressBar spinner;
    private SignUpViewModel signUpViewModel;
    private View view;
    private RemoteConfigServer remoteConfigServer;





    //--------------------------------------------------------------- ANDROID METHODS

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_sign_up, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.view = view;
        remoteConfigServer = RemoteConfigServer.getInstance();

//        spinner = view.findViewById(R.id.progresBar_signUpFragment);
        textInputLayoutUsername = (TextInputLayout) view.findViewById(R.id.textInputLayout_signUpFragment_username);
        textInputLayoutEmail = (TextInputLayout) view.findViewById(R.id.textInputLayout_signUpFragment_email);
        textInputLayoutPassword = (TextInputLayout) view.findViewById(R.id.textInputLayout_signUpFragment_password);
        textInputLayoutRepeatPassword = (TextInputLayout) view.findViewById(R.id.textInputLayout_signUpFragment_repeatPassword);
        textInputLayoutFirstName = (TextInputLayout) view.findViewById(R.id.textInputLayout_signUpFragment_firstName);
        textInputLayoutLastName = (TextInputLayout) view.findViewById(R.id.textInputLayout_signUpFragment_lastName);
        textInputLayoutBirthDate = (TextInputLayout) view.findViewById(R.id.textInputLayout_signUpFragment_birthDate);
        //
        editTextUsername = (TextInputEditText) view.findViewById(R.id.editText_signUpFragment_username);
        editTextEmail = (TextInputEditText) view.findViewById(R.id.editText_signUpFragment_email);
        editTextPassword = (TextInputEditText) view.findViewById(R.id.editText_signUpFragment_password);
        editTextRepeatPassword = (TextInputEditText) view.findViewById(R.id.editText_signUpFragment_repeatPassword);
        editTextFirstName = (TextInputEditText) view.findViewById(R.id.editText_signUpFragment_firstName);
        editTextLastName = (TextInputEditText) view.findViewById(R.id.editText_signUpFragment_lastName);
        editTextBirthDate = (TextInputEditText) view.findViewById(R.id.editText_signUpFragment_birthDate);
        //
        buttonsignUp = (Button) view.findViewById(R.id.button_signUpFragment_signUp);
        checkBoxPromo = (CheckBox) view.findViewById(R.id.checkBox_loginFragment_promo);
        checkBoxAnalytics = (CheckBox) view.findViewById(R.id.checkBox_loginFragment_analytics);
        checkBoxTermsAndConditions = (CheckBox) view.findViewById(R.id.checkBox_loginFragment_termsAndConditions);
        // setting listeners
        buttonsignUp.setOnClickListener(this);
        checkBoxPromo.setOnCheckedChangeListener(this);
        checkBoxAnalytics.setOnCheckedChangeListener(this);
        checkBoxTermsAndConditions.setOnCheckedChangeListener(this);
//        editTextUsername.setOnEditorActionListener(this);

        // firebase logging
        FirebaseEventsLogger firebaseEventsLogger = FirebaseEventsLogger.getInstance();
        firebaseEventsLogger.logScreenEvent(this, "Sign-In page", getContext());

        //
        signUpViewModel = new ViewModelProvider(this).get(SignUpViewModel.class);
        signUpViewModel.getUser().observe(getViewLifecycleOwner(), new Observer<User>() {
            @Override
            public void onChanged(@Nullable User user) {
                if (user != null) {


                } else {

                }
            }
        });


        signUpViewModel.getFirebaseAuthState().observe(getViewLifecycleOwner(), new Observer<SignUpViewModel.FirebaseSignUpServerCodeState>() {
            @Override
            public void onChanged(@Nullable SignUpViewModel.FirebaseSignUpServerCodeState firebaseSignUpServerCodeState) {

                switch (firebaseSignUpServerCodeState) {
                    case SIGN_UP_SUCCESS:
                        MyUtilities.showCenteredToast("Firebase sign-up server:\ncreateUserWithEmail:success" , getContext());
                        break;
                    case SIGN_UP_FAILURE:
                        MyUtilities.showCenteredToast("Firebase sign-up server:\ncreateUserWithEmail:failure", getContext());
                        break;
                    case VERIFICATION_MAIL_SENT:
                        MyUtilities.showCenteredToast("Firebase sign-up server:\nRiceverai a breve un link di attivazione account nella tua posta", getContext());
                        break;
                    case VERIFICATION_MAIL_NOT_SENT:
                        MyUtilities.showCenteredToast("Firebase sign-up server:\nVerification email NOT sent", getContext());
                        break;
                    case PENDING_USER_COLLISION:
                        MyUtilities.showCenteredToast("Firebase sign-up server:\nemail ancora non approvata\ncontrolla la tua posta\"", getContext());
                        textInputLayoutEmail.setError("email ancora non approvata, controlla la tua posta");
                        break;
                    case USERNAME_EMAIL_COLLISION:
                        MyUtilities.showCenteredToast("Firebase sign-up server:\nusername+email gia' presente", getContext());
                        textInputLayoutUsername.setError("username+email gia' presente");
                        textInputLayoutEmail.setError("username+email gia' presente");
                        break;
                    case USERNAME_COLLISION:
                        MyUtilities.showCenteredToast("Firebase sign-up server:\nusername gia' presente", getContext());
                        textInputLayoutUsername.setError("username gia' presente");
                        break;
                    case EMAIL_COLLISION:
                        MyUtilities.showCenteredToast("Firebase sign-up server:\nemail gia' presente", getContext());
                        textInputLayoutEmail.setError("email gia' presente");
                        break;
                    case GENERIC_POSTGREST_ERROR:
                        MyUtilities.showCenteredToast("Firebase sign-up server:\nerrore postgrest", getContext());
                        textInputLayoutEmail.setError("email gia' presente");
                        break;
                    case WEAK_PASSWORD:
                        MyUtilities.showCenteredToast("Firebase sign-up server:\nla password deve contenere un numero di caratteri superiori a 5", getContext());
                        textInputLayoutEmail.setError("la password deve contenere un numero di caratteri superiori a 5");
                        break;


                }
            }
        });

        // Initialize Firebase Auth

//        FirebaseAuth.AuthStateListener mAuthListener = new FirebaseAuth.AuthStateListener() {
//            @Override
//            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
//                FirebaseUser user = firebaseAuth.getCurrentUser();
//                if (user != null) {
//                    sendEmailVerificationTo(user);
//
//                }
//            }
//        };
//
//        mAuth.addAuthStateListener(mAuthListener);


        // fast sign up
        fastSignUp();



    }// end onViewCreated class


    @Override
    public void onClick(View v) {

        if(v.getId() == buttonsignUp.getId()) {
            // getting data
            String username = editTextUsername.getText().toString();
            String email = editTextEmail.getText().toString();
            String password = editTextPassword.getText().toString();
            String repeatPassword = editTextRepeatPassword.getText().toString();
            String firstName = editTextFirstName.getText().toString();
            String lastName = editTextLastName.getText().toString();
            String birthDate = editTextBirthDate.getText().toString();
            boolean promo = checkBoxPromo.isChecked();
            boolean analytics = checkBoxAnalytics.isChecked();
            boolean termsAndConditions = checkBoxTermsAndConditions.isChecked();



            // check fields
            boolean allFieldsAreFilled = checkAllFieldsAreFilled();
            boolean repeatPasswordMatches = checkRepeatPasswordMatch();

            if(allFieldsAreFilled && repeatPasswordMatches) {
                signUpViewModel.signUpAsPendingUser(username, email, password, firstName, lastName, birthDate, promo, analytics);
            }
            else {
                MyUtilities.showCenteredToast("Completare prima tutti i campi evidenziati in rosso.", getContext());
            }



            try {

                //

//                mStatusTextView.setText(getString(R.string.emailpassword_status_fmt,
//                        user.getEmail(), user.isEmailVerified()));

//
//                //
//                httpUrl = new HttpUrl.Builder()
//                        .scheme("https")
//                        .host(remoteConfigServer.getAzureHostName())
//                        .addPathSegments(remoteConfigServer.getPostgrestPath())
//                        .addPathSegment(dbFunction)
//                        .build();
//

//
//                Request request = HttpUtilities.buildPOSTrequest(httpUrl, requestBody, remoteConfigServer.getGuestToken());
//
//                //
//                Call call = httpClient.newCall(request);
//                call.enqueue(this);

            } catch (Exception e) {
                System.out.println("failed");
                e.printStackTrace();
            }
        }
    }// end onClick()




    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

    }





    //--------------------------------------------------------------- METHODS

    private boolean checkAllFieldsAreFilled() {
        boolean res = true;
        if(editTextUsername.getText().toString().isEmpty()) {
            textInputLayoutUsername.setError("*");
            res = false;
        }
        else {
            textInputLayoutUsername.setError(null);
        }

        if(editTextEmail.getText().toString().isEmpty()) {
            textInputLayoutEmail.setError("*");
            res = false;
        }
        else {
            textInputLayoutEmail.setError(null);
        }

        if(editTextPassword.getText().toString().isEmpty()) {
            textInputLayoutPassword.setError("*");
            res = false;
        }
        else {
            textInputLayoutPassword.setError(null);
        }

        if(editTextRepeatPassword.getText().toString().isEmpty()) {
            textInputLayoutRepeatPassword.setError("*");
            res = false;
        }
        else {
            textInputLayoutRepeatPassword.setError(null);
        }

        if(editTextFirstName.getText().toString().isEmpty()) {
            textInputLayoutFirstName.setError("*");
            res = false;
        }
        else {
            textInputLayoutFirstName.setError(null);
        }

        if(editTextLastName.getText().toString().isEmpty()) {
            textInputLayoutLastName.setError("*");
            res = false;
        }
        else {
            textInputLayoutLastName.setError(null);
        }

        if(editTextBirthDate.getText().toString().isEmpty()) {
            textInputLayoutBirthDate.setError("*");
            res = false;
        }
        else {
            textInputLayoutBirthDate.setError(null);
        }

        if( ! checkBoxTermsAndConditions.isChecked()) {
            checkBoxTermsAndConditions.setTextColor(getResources().getColor(R.color.red));
            res = false;
        }
        else {
            checkBoxTermsAndConditions.setTextColor(getResources().getColor(R.color.light_blue));
        }

        return res;
    }

    private boolean checkRepeatPasswordMatch() {
        if( ! editTextRepeatPassword.getText().toString().equals(editTextPassword.getText().toString())) {
            textInputLayoutRepeatPassword.setError(getString(R.string.passwords_dont_match));
            return false;
        }
        else {
            textInputLayoutRepeatPassword.setError(null);
        }

        return true;
    }



    private void fastSignUp() {
        editTextUsername.setText("foo");
        editTextEmail.setText("dev.mirror42@gmail.com");
        editTextPassword.setText("aaaaaaa");
        editTextRepeatPassword.setText("aaaaaaa");
        editTextFirstName.setText("mrfoo");
        editTextLastName.setText("bar");
        editTextBirthDate.setText("1/1/1970");
    }




    @Override
    public void onFailure(@NotNull Call call, @NotNull IOException e) {
        System.out.println("failed");

    }

    @Override
    public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
        String responseData = response.body().string();
        if (response.isSuccessful()) {
            System.out.println(responseData);
        }
        else {
            System.out.println(responseData);
        }
    }


}// end signUpFragment class