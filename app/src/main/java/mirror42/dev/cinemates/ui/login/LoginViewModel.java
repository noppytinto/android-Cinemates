package mirror42.dev.cinemates.ui.login;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.io.IOException;

import mirror42.dev.cinemates.model.User;
import mirror42.dev.cinemates.utilities.HttpUtilities;
import mirror42.dev.cinemates.utilities.MyUtilities;
import mirror42.dev.cinemates.utilities.RemoteConfigServer;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class LoginViewModel extends ViewModel {
    private final String TAG = this.getClass().getSimpleName();
    private MutableLiveData<User> user;
    private MutableLiveData<LoginResult> loginResult;
    private RemoteConfigServer remoteConfigServer;
    private FirebaseAuth mAuth;
    private FirebaseUser firebaseUser;

    public enum LoginResult {
        INVALID_REQUEST,
        FAILED,
        SUCCESS,
        INVALID_PASSWORD,
        USER_NOT_EXIST,
        LOGOUT,
        REMEMBER_ME,
        IS_PENDING_USER,
        IS_NOT_PENDING_USER
    }



    //--------------------------------------------------- CONSTRUCTORS

    public LoginViewModel() {
        this.user = new MutableLiveData<>();
        loginResult = new MutableLiveData<>();
        remoteConfigServer = RemoteConfigServer.getInstance();
        mAuth = FirebaseAuth.getInstance();
    }




    //--------------------------------------------------- GETTERS/SETTERS

    public LiveData<User> getUser() {
        return user;
    }

    public void setPostUser(User user) {
        this.user.postValue(user);
    }

    public void setUser(User user) {
        this.user.setValue(user);
    }

    public MutableLiveData<LoginResult> getLoginResult() {
        return loginResult;
    }

    public void setPostLoginResult(LoginResult loginResult) {
        this.loginResult.postValue(loginResult);
    }

    public void setLoginResult(LoginResult loginResult) {
        this.loginResult.setValue(loginResult);
    }



    //--------------------------------------------------- METHODS

    public void standardLogin(String email, String password) {
        if(password==null) {
            // TODO handle failed password encryption
        }

        //
        HttpUrl httpUrl = null;

        // generating url request
        try {
            httpUrl = buildStandardLoginUrl(email, MyUtilities.SHA256encrypt(password));

        } catch (Exception e) {
            e.printStackTrace();
            setPostLoginResult(LoginResult.INVALID_REQUEST);
        }

        // performing http request
        final OkHttpClient httpClient = new OkHttpClient();

        try {
            Request request = HttpUtilities.buildGETrequest(httpUrl, remoteConfigServer.getGuestToken());

            //
            Call call = httpClient.newCall(request);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    setPostLoginResult(LoginResult.FAILED);

                }

                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                    try {
                        if (response.isSuccessful()) {
                            String responseData = response.body().string();

                            if( ! responseData.equals("null")) {
                                JSONObject jsonObject = new JSONObject(responseData);

                                //
                                User user = User.parseUserFromJsonObject(jsonObject);

                                //
                                setPostUser(user);
                                setPostLoginResult(LoginResult.SUCCESS);
                            }
                            else {
                                setPostUser(null);
                                setPostLoginResult(LoginResult.FAILED);
                            }
                        }
                        else {
                            setPostUser(null);
                            setPostLoginResult(LoginResult.FAILED);

                            //TODO: should be logged
//                showToastOnUiThread("Authentication server:\n" +
//                        "message: " + response.header("message")
//                        + "errore code: " + response.header("code"));
                        }
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                        setPostLoginResult(LoginResult.FAILED);
                    }
                }
            });
        }catch(Exception e) {
            e.printStackTrace();
            setPostLoginResult(LoginResult.INVALID_REQUEST);
        }
    }// end standardLogin()

    private HttpUrl buildStandardLoginUrl(String email, String password) throws Exception {
        final String dbFunction = "login";

        //
        HttpUrl httpUrl = new HttpUrl.Builder()
                .scheme("https")
                .host(remoteConfigServer.getAzureHostName())
                .addPathSegments(remoteConfigServer.getPostgrestPath())
                .addPathSegment(dbFunction)
                .addQueryParameter("mail", email)
                .addQueryParameter("pass", password)
                .build();

        return httpUrl;
    }

    public void checkIfIsPendingUser(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "Autorization server: email ancora non approvata, controlla la tua posta");
                    firebaseUser = mAuth.getCurrentUser();
                    setLoginResult(LoginResult.IS_PENDING_USER);
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "Autorization server: not a pending user", task.getException());
                    setLoginResult(LoginResult.IS_NOT_PENDING_USER);

                    // ...
                }
            }
        });
    }// end checkIfIsPendingUser()


    //----------- callbacks





}// end LoginViewModel class
