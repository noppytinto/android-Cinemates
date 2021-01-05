package mirror42.dev.cinemates.ui.login;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import org.json.JSONObject;

import java.io.IOException;

import mirror42.dev.cinemates.model.User;
import mirror42.dev.cinemates.utilities.HttpUtilities;
import mirror42.dev.cinemates.utilities.MyUtilities;
import mirror42.dev.cinemates.utilities.MyUtilities.LoginResult;
import mirror42.dev.cinemates.utilities.RemoteConfigServer;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class LoginViewModel extends ViewModel implements Callback {
    private MutableLiveData<User> user;
    private MutableLiveData<LoginResult> loginResult;
    private RemoteConfigServer remoteConfigServer;
    private static boolean rememberMeIsActive;



    //--------------------------------------------------- CONSTRUCTORS

    public LoginViewModel() {
        this.user = new MutableLiveData<>();
        loginResult = new MutableLiveData<>();
        remoteConfigServer = RemoteConfigServer.getInstance();
    }




    //--------------------------------------------------- GETTERS/SETTERS

    public LiveData<User> getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user.postValue(user);
    }

    public MutableLiveData<LoginResult> getLoginResult() {
        return loginResult;
    }

    public void setLoginResult(LoginResult loginResult) {
        this.loginResult.postValue(loginResult);
    }


    public void setRememberMe(boolean value) {
        this.rememberMeIsActive = value;
    }



    //--------------------------------------------------- METHODS

    public void init(boolean rememberMeIsActive) {
        this.rememberMeIsActive = rememberMeIsActive;
    }

    public void standardLogin(String email, String password, Context context) {
        if(password==null) {
            // TODO handle failed password encryption
            return;
        }

        //
        HttpUrl httpUrl = null;

        // generating url request
        try {
            httpUrl = buildStandardLoginUrl(email, MyUtilities.SHA256encrypt(password));

        } catch (Exception e) {
            e.printStackTrace();
            setLoginResult(LoginResult.INVALID_REQUEST);
        }

        // performing http request
        final OkHttpClient httpClient = new OkHttpClient();

        try {
            Request request = HttpUtilities.buildGETrequest(httpUrl, remoteConfigServer.getGuestToken());

            //
            Call call = httpClient.newCall(request);
            call.enqueue(this);
        }catch(Exception e) {
            e.printStackTrace();
            setLoginResult(LoginResult.INVALID_REQUEST);
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



    //----------- callbacks

    @Override
    public void onFailure(Call call, IOException e) {
        setLoginResult(LoginResult.FAILED);
    }

    @Override
    public void onResponse(Call call, Response response) throws IOException {
        try {
            if (response.isSuccessful()) {
                String responseData = response.body().string();

                if( ! responseData.equals("null")) {
                    JSONObject jsonObject = new JSONObject(responseData);

                    //
                    String username = jsonObject.getString("Username");
                    String email = jsonObject.getString("Email");
                    String profileImagePath = jsonObject.getString("ProfileImage");
                    User user = new User(username, email, profileImagePath);

                    //
                    setUser(user);
                }
                else {
                    setUser(null);
                }
            }
            else {
                setLoginResult(LoginResult.FAILED);

                //TODO: should be logged
//                showToastOnUiThread("Authentication server:\n" +
//                        "message: " + response.header("message")
//                        + "errore code: " + response.header("code"));
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            setLoginResult(LoginResult.FAILED);
        }
    }





}// end LoginViewModel class
