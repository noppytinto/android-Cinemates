package mirror42.dev.cinemates.ui.login;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import mirror42.dev.cinemates.R;
import mirror42.dev.cinemates.utilities.FirebaseEventsLogger;
import mirror42.dev.cinemates.utilities.MyUtilities;
import mirror42.dev.cinemates.utilities.RemoteConfigServer;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class LoginActivity extends AppCompatActivity implements
        View.OnClickListener,
        Callback,
        CompoundButton.OnCheckedChangeListener {
    private TextInputLayout textInputLayoutEmail;
    private TextInputLayout textInputLayoutPassword;
    private TextInputEditText editTextEmail;
    private TextInputEditText editTextPassword;
    private Button buttonLogin;
    private ProgressBar spinner;
    private RemoteConfigServer remoteConfigServer;
    private CheckBox checkBoxRememberMe;
    private static boolean rememberMeIsActive;


    

    //-------------------------------------------------------------------------------- LIFECYCLE METHODS

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        remoteConfigServer = RemoteConfigServer.getInstance();
        spinner = findViewById(R.id.progresBar_loginActivity);
        editTextEmail = (TextInputEditText) findViewById(R.id.editTextText_loginActivity_email);
        editTextPassword = (TextInputEditText) findViewById(R.id.editTextText_loginActivity_password);
        textInputLayoutEmail = (TextInputLayout) findViewById(R.id.textInputLayout_loginActivity_email);
        textInputLayoutPassword = (TextInputLayout) findViewById(R.id.textInputLayout_loginActivity_password);
        buttonLogin = (Button) findViewById(R.id.button_loginActivity_login);
        checkBoxRememberMe = findViewById(R.id.checkBox_loginActivity_rememberMe);

        //
        buttonLogin.setOnClickListener(this);
        checkBoxRememberMe.setOnCheckedChangeListener(this);

        //
        FirebaseEventsLogger firebaseEventsLogger = FirebaseEventsLogger.getInstance();
        firebaseEventsLogger.logScreenEvent(this, "Login page", this);

        //
        boolean thereIsArememberMe = checkAnyPreviousRememberMe();
        if(thereIsArememberMe) {
            updateRememberMeState(true);
        }
        else {
            updateRememberMeState(false);
        }

        // fast login
//        editTextEmail.setText("dev.mirror42@gmail.com");
//        editTextPassword.setText("a");

    }




    //-------------------------------------------------------------------------------- GETTERS/SETTERS

    private void updateRememberMeState(Boolean rememberMeState) {
        this.rememberMeIsActive = rememberMeState;
    }

    /**
     * read a previous remember me state (if any)
     * on local store
     */
    public boolean checkAnyPreviousRememberMe() {
        boolean result = false;
        if(MyUtilities.checkFileExists(remoteConfigServer.getCinematesData(), this)) {
            try {
                JSONObject jsonObject = new JSONObject(MyUtilities.decryptFile(remoteConfigServer.getCinematesData(), this));
                editTextEmail.setText(jsonObject.getString("Email"));
                editTextPassword.setText("********");
//                showToastOnUiThread( "Authentication server:\nwelcome back: " + jsonObject.getString("Email"));
                checkBoxRememberMe.setChecked(true);
                result = true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }// if

        return result;
    }



    //-------------------------------------------------------------------------------- METHODS

    private void standardLogin(String email, String password) {
        if(password==null) {
            // TODO handle failed password encryption
            return;
        }

        //
        FirebaseEventsLogger firebaseEventsLogger = FirebaseEventsLogger.getInstance();
        firebaseEventsLogger.logLoginEvent("email + password", this);
        spinner.setVisibility(View.VISIBLE);

        //
        final String dbFunction = "login";
        final OkHttpClient httpClient = new OkHttpClient();
        HttpUrl httpUrl = null;


        // generating url request
        if(rememberMeIsActive) {
            // decrypt rememberme file
            String data = MyUtilities.decryptFile(remoteConfigServer.getCinematesData(), this);
            JSONObject jsonObject = null;

            try {
                jsonObject = new JSONObject(data);
                httpUrl = new HttpUrl.Builder()
                        .scheme("https")
                        .host(remoteConfigServer.getAzureHostName())
                        .addPathSegments(remoteConfigServer.getPostgrestPath())
                        .addPathSegment(dbFunction)
                        .addQueryParameter("mail", email)
                        .addQueryParameter("pass", jsonObject.getString("Password"))
                        .build();

            } catch (JSONException e) {
                e.printStackTrace();
                Toast.makeText(this, "Autorization server:\ncannot making request! D:", Toast.LENGTH_LONG).show();
            }
        }
        else {
            httpUrl = new HttpUrl.Builder()
                    .scheme("https")
                    .host(remoteConfigServer.getAzureHostName())
                    .addPathSegments(remoteConfigServer.getPostgrestPath())
                    .addPathSegment(dbFunction)
                    .addQueryParameter("mail", email)
                    .addQueryParameter("pass", MyUtilities.SHAencrypt(password))
                    .build();
        }

        // performing  request
        try {
            Request request = new Request.Builder()
                    .url(httpUrl)
                    .header("User-Agent", "OkHttp Headers.java")
                    .addHeader("Accept", "application/json; q=0.5")
                    .addHeader("Accept", "application/vnd.github.v3+json")
                    .addHeader("Authorization", "Bearer " + remoteConfigServer.getGuestToken())
                    .build();

            Call call = httpClient.newCall(request);
            call.enqueue(this);

        }catch(Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Autorization server:\ncannot making request! D:", Toast.LENGTH_LONG).show();
        }
    }// end standardLogin()


    @Override
    public void onClick(View v) {
        if(v.getId() == buttonLogin.getId()) {
            String email = editTextEmail.getText().toString();
            String password = editTextPassword.getText().toString();

            if(email.isEmpty()) {
                textInputLayoutEmail.setError("inserire mail!");
                return;
            }

            if(password.isEmpty()) {
                textInputLayoutPassword.setError("inserire password!");
                return;
            }

            textInputLayoutEmail.setError(null);
            textInputLayoutPassword.setError(null);
            standardLogin(email, password);
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if(isChecked) {
//            Toast.makeText(this, "Credenziali salvate", Toast.LENGTH_SHORT).show();
        }
        else {
            if(rememberMeIsActive) {
                rememberMeIsActive = false;
                editTextPassword.setText("");
                MyUtilities.deletFile(remoteConfigServer.getCinematesData(), this);
                Toast.makeText(this, "Credenziali eliminate", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onFailure(Call call, IOException e) {
        showToastOnUiThread("Authentication server:\nCannot establish connection! D:");
    }

    @Override
    public void onResponse(Call call, Response response) throws IOException {
        try {
            if (response.isSuccessful()) {
                String responseData = response.body().string();

                if( ! responseData.equals("null")) {
                    JSONObject jsonObject = new JSONObject(responseData);
                    showToastOnUiThread( "Authentication server:\nlogin successful\nwelcome: " + jsonObject.getString("Email"));
                    resetTextLayout();
                    //TODO: handle login response
                    // encrypting

                    // eventually save login data on local store
                    if(checkBoxRememberMe.isChecked()) {
                        MyUtilities.encryptFile(remoteConfigServer.getCinematesData(), jsonObject.toString(), this);
                    }
                }
                else {
                    notifyError();
                    showToastOnUiThread( "Authentication server:\ninvalid credentials");
                }
            }
            else {
                showToastOnUiThread("Authentication server:\n" +
                        "message: " + response.header("message")
                        + "errore code: " + response.header("code"));
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            showToastOnUiThread("Authentication server:\nCannot establish connection! D:");
        }
    }


    private void resetTextLayout() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // print response
                textInputLayoutEmail.setError(null);
                textInputLayoutPassword.setError(null);
            }
        });
    }

    private void notifyError() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // print response
                textInputLayoutEmail.setError("credenziali non valide!");
                textInputLayoutPassword.setError("credenziali non valide!");
            }
        });
    }

    private void showToastOnUiThread(String toastMessage) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // print response
                spinner.setVisibility(View.GONE);
                final Toast toast = Toast.makeText(getApplicationContext(), toastMessage, Toast.LENGTH_SHORT);
                toast.show();
            }
        });
    }// end showToastOnUiThread()

}// end LoginActivity class