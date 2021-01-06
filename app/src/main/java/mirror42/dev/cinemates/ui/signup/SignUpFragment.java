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
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
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
    private TextInputLayout textInputLayoutFirstName;
    private TextInputLayout textInputLayoutLastName;
    private TextInputLayout textInputLayoutBirthDate;
    //
    private TextInputEditText editTextUsername;
    private TextInputEditText editTextEmail;
    private TextInputEditText editTextPassword;
    private TextInputEditText editTextFirstName;
    private TextInputEditText editTextLastName;
    private TextInputEditText editTextBirthDate;
    //
    private CheckBox checkBoxPromo;
    private CheckBox checkBoxAnalytics;
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
        textInputLayoutFirstName = (TextInputLayout) view.findViewById(R.id.textInputLayout_signUpFragment_firstName);
        textInputLayoutLastName = (TextInputLayout) view.findViewById(R.id.textInputLayout_signUpFragment_lastName);
        textInputLayoutBirthDate = (TextInputLayout) view.findViewById(R.id.textInputLayout_signUpFragment_birthDate);
        //
        editTextUsername = (TextInputEditText) view.findViewById(R.id.editText_signUpFragment_username);
        editTextEmail = (TextInputEditText) view.findViewById(R.id.editText_signUpFragment_email);
        editTextPassword = (TextInputEditText) view.findViewById(R.id.editText_signUpFragment_password);
        editTextFirstName = (TextInputEditText) view.findViewById(R.id.editText_signUpFragment_firstName);
        editTextLastName = (TextInputEditText) view.findViewById(R.id.editText_signUpFragment_lastName);
        editTextBirthDate = (TextInputEditText) view.findViewById(R.id.editText_signUpFragment_birthDate);
        //
        buttonsignUp = (Button) view.findViewById(R.id.button_signUpFragment_signUp);
        checkBoxPromo = (CheckBox) view.findViewById(R.id.checkBox_loginFragment_promo);
        checkBoxAnalytics = (CheckBox) view.findViewById(R.id.checkBox_loginFragment_analytics);
        // setting listeners
        buttonsignUp.setOnClickListener(this);
        checkBoxPromo.setOnCheckedChangeListener(this);
        checkBoxAnalytics.setOnCheckedChangeListener(this);

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


        signUpViewModel.getFirebaseAuthState().observe(getViewLifecycleOwner(), new Observer<SignUpViewModel.FirebaseAuthState>() {
            @Override
            public void onChanged(@Nullable SignUpViewModel.FirebaseAuthState firebaseAuthState) {

                switch (firebaseAuthState) {
                    case SIGN_UP_SUCCESS:
                        MyUtilities.showCenteredToast("Firebase sign-up server:\ncreateUserWithEmail:success" , getContext());
                        break;
                    case SIGN_UP_FAILURE:
                        MyUtilities.showCenteredToast("Firebase sign-up server:\ncreateUserWithEmail:failure", getContext());
                        break;
                    case VERIFICATION_MAIL_SENT:
                        MyUtilities.showCenteredToast("Firebase sign-up server:\nRiceverai a breve un link di attivazione account nella tua casella postale", getContext());
                        break;
                    case VERIFICATION_MAIL_NOT_SENT:
                        MyUtilities.showCenteredToast("Firebase sign-up server:\nVerification email NOT sent", getContext());
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
            HttpUrl httpUrl = null;
            final OkHttpClient httpClient = new OkHttpClient();

            try {
                // getting data
                String username = editTextUsername.getText().toString();
                String email = editTextEmail.getText().toString();
                String password = editTextPassword.getText().toString();
                String firstName = editTextFirstName.getText().toString();
                String lastName = editTextLastName.getText().toString();
                String birthDate = editTextBirthDate.getText().toString();
                boolean promo = checkBoxPromo.isChecked();
                boolean analytics = checkBoxAnalytics.isChecked();

                //
                signUpViewModel.signUpAsPendingUser(username, email, password, firstName, lastName, birthDate, promo, analytics);

//                mStatusTextView.setText(getString(R.string.emailpassword_status_fmt,
//                        user.getEmail(), user.isEmailVerified()));



//                final String dbFunction = "fn_register_new_user";
//
//                //
//                httpUrl = new HttpUrl.Builder()
//                        .scheme("https")
//                        .host(remoteConfigServer.getAzureHostName())
//                        .addPathSegments(remoteConfigServer.getPostgrestPath())
//                        .addPathSegment(dbFunction)
//                        .build();
//
//                RequestBody requestBody = buildRequestBody(
//                        "foo",
//                        "foo@mail.com",
//                        "aaaa",
//                        "mrfoo",
//                        "bar",
//                        "1970-1-1",
//                        "foo.jpg",
//                        String.valueOf(true),
//                        String.valueOf(true));
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

    private void fastSignUp() {
        editTextUsername.setText("foo");
        editTextEmail.setText("noto42@outlook.com");
        editTextPassword.setText("aaaaaaa");
        editTextFirstName.setText("mrfoo");
        editTextLastName.setText("bar");
        editTextBirthDate.setText("1/1/1970");
    }


    private RequestBody buildRequestBody(String username,
                                   String email,
                                   String password,
                                   String firstName,
                                   String lastName,
                                   String birthday,
                                   String profilePicturePath,
                                   String promo,
                                   String analytics) throws Exception {



        RequestBody requestBody = new FormBody.Builder()
                .add("mail", email)
                .add("username", username)
                .add("pass", password)
                .add("firstname", firstName)
                .add("lastname", lastName)
                .add("birthday", birthday)
                .add("profilepicturepath", profilePicturePath)
                .add("promo", promo)
                .add("analytics", analytics)
                .build();

        return requestBody;
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