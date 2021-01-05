package mirror42.dev.cinemates.ui.signup;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import mirror42.dev.cinemates.R;
import mirror42.dev.cinemates.model.User;
import mirror42.dev.cinemates.utilities.FirebaseEventsLogger;
import mirror42.dev.cinemates.utilities.RemoteConfigServer;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SignUpFragment extends Fragment implements
        View.OnClickListener, Callback {

    private final String TAG = this.getClass().getSimpleName();
    private TextInputLayout textInputLayoutEmail;
    private TextInputLayout textInputLayoutPassword;
    private TextInputEditText editTextEmail;
    private TextInputEditText editTextPassword;
    private Button buttonsignUp;
    private ProgressBar spinner;
    private SignUpViewModel signUpViewModel;
    private View view;
    private RemoteConfigServer remoteConfigServer;
    private FirebaseAuth mAuth;




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
        editTextEmail = (TextInputEditText) view.findViewById(R.id.editText_signUpFragment_email);
        editTextPassword = (TextInputEditText) view.findViewById(R.id.editText_signUpFragment_password);
        textInputLayoutEmail = (TextInputLayout) view.findViewById(R.id.textInputLayout_signUpFragment_email);
        textInputLayoutPassword = (TextInputLayout) view.findViewById(R.id.textInputLayout_signUpFragment_password);
        buttonsignUp = (Button) view.findViewById(R.id.button_signUpFragment_signUp);

        // setting listeners
        buttonsignUp.setOnClickListener(this);

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


        // ...
        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();


        FirebaseAuth.AuthStateListener mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    sendEmailVerificationTo(user);

                }
            }
        };

        mAuth.addAuthStateListener(mAuthListener);




    }

    @Override
    public void onClick(View v) {

        if(v.getId() == buttonsignUp.getId()) {
            HttpUrl httpUrl = null;
            final OkHttpClient httpClient = new OkHttpClient();

            try {

                String email = editTextEmail.getText().toString();
                String password = editTextPassword.getText().toString();

//                mStatusTextView.setText(getString(R.string.emailpassword_status_fmt,
//                        user.getEmail(), user.isEmailVerified()));

                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    // Sign in success, update UI with the signed-in user's information
                                    Log.d(TAG, "createUserWithEmail:success");
                                    FirebaseUser user = mAuth.getCurrentUser();
                                    Log.d(TAG, "welcome: " + user.getEmail());

                                    //
//                                    MyUtilities.showCenteredToastOnUiThread(getActivity(), "welcome: " + user.getEmail());





                                } else {
                                    // If sign in fails, display a message to the user.
                                    Log.w(TAG, "createUserWithEmail:failure", task.getException());
                                }

                            }
                        });

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
    }




    //--------------------------------------------------------------- METHODS

    private void sendEmailVerificationTo(FirebaseUser user) {
        user.sendEmailVerification()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "Verification email sent.");
                            deleteAccount(user);

                        }
                        else {
                            Log.d(TAG, "Verification email NOT sent.");

                        }
                    }
                });
    }

    private void deleteAccount(FirebaseUser user) {
        user.delete();
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