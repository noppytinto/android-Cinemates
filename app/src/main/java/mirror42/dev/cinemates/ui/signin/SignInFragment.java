package mirror42.dev.cinemates.ui.signin;

import android.os.Bundle;
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

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import mirror42.dev.cinemates.R;
import mirror42.dev.cinemates.model.User;
import mirror42.dev.cinemates.utilities.FirebaseEventsLogger;
import mirror42.dev.cinemates.utilities.HttpUtilities;
import mirror42.dev.cinemates.utilities.RemoteConfigServer;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SignInFragment extends Fragment implements
        View.OnClickListener, Callback {

    private TextInputLayout textInputLayoutEmail;
    private TextInputLayout textInputLayoutPassword;
    private TextInputEditText editTextEmail;
    private TextInputEditText editTextPassword;
    private Button buttonSignIn;
    private ProgressBar spinner;
    private SignInViewModel signInViewModel;
    private View view;
    private RemoteConfigServer remoteConfigServer;




    //--------------------------------------------------------------- ANDROID METHODS

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_sign_in, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.view = view;
        remoteConfigServer = RemoteConfigServer.getInstance();

//        spinner = view.findViewById(R.id.progresBar_signInFragment);
        editTextEmail = (TextInputEditText) view.findViewById(R.id.editText_signInFragment_email);
        editTextPassword = (TextInputEditText) view.findViewById(R.id.editText_signInFragment_password);
        textInputLayoutEmail = (TextInputLayout) view.findViewById(R.id.textInputLayout_signInFragment_email);
        textInputLayoutPassword = (TextInputLayout) view.findViewById(R.id.textInputLayout_signInFragment_password);
        buttonSignIn = (Button) view.findViewById(R.id.button_signInFragment_signIn);

        // setting listeners
        buttonSignIn.setOnClickListener(this);

        // firebase logging
        FirebaseEventsLogger firebaseEventsLogger = FirebaseEventsLogger.getInstance();
        firebaseEventsLogger.logScreenEvent(this, "Sign-In page", getContext());

        //
        signInViewModel = new ViewModelProvider(this).get(SignInViewModel.class);
        signInViewModel.getUser().observe(getViewLifecycleOwner(), new Observer<User>() {
            @Override
            public void onChanged(@Nullable User user) {
                if(user != null) {



                }
                else {

                }
            }
        });




    }

    @Override
    public void onClick(View v) {

        if(v.getId() == buttonSignIn.getId()) {
            HttpUrl httpUrl = null;
            final OkHttpClient httpClient = new OkHttpClient();

            try {

                final String dbFunction = "fn_register_new_user";

                //
                httpUrl = new HttpUrl.Builder()
                        .scheme("https")
                        .host(remoteConfigServer.getAzureHostName())
                        .addPathSegments(remoteConfigServer.getPostgrestPath())
                        .addPathSegment(dbFunction)
                        .build();

                RequestBody requestBody = buildRequestBody(
                        "foo",
                        "foo@mail.com",
                        "aaaa",
                        "mrfoo",
                        "bar",
                        "1970-1-1",
                        "foo.jpg",
                        String.valueOf(true),
                        String.valueOf(true));

                Request request = HttpUtilities.buildPOSTrequest(httpUrl, requestBody, remoteConfigServer.getGuestToken());

                //
                Call call = httpClient.newCall(request);
                call.enqueue(this);

            } catch (Exception e) {
                System.out.println("failed");
                e.printStackTrace();
            }

        }
    }




    //--------------------------------------------------------------- METHODS




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
}// end SignInFragment class