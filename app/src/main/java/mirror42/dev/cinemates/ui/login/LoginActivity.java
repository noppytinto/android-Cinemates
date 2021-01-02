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
import com.google.firebase.analytics.FirebaseAnalytics;

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
import okhttp3.ResponseBody;

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
    private boolean rememberMeIsActive;


    //---------------------------------------------------- LIFECYCLE METHODS

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        remoteConfigServer = RemoteConfigServer.getInstance();

        //
        FirebaseEventsLogger firebaseEventsLogger = FirebaseEventsLogger.getInstance();
        firebaseEventsLogger.logScreenEvent(this, "Login page", this);

        //
        spinner = findViewById(R.id.progresBar_loginActivity);


        //
        editTextEmail = (TextInputEditText) findViewById(R.id.editTextText_loginActivity_email);
        editTextPassword = (TextInputEditText) findViewById(R.id.editTextText_loginActivity_password);
        textInputLayoutEmail = (TextInputLayout) findViewById(R.id.textInputLayout_loginActivity_email);
        textInputLayoutPassword = (TextInputLayout) findViewById(R.id.textInputLayout_loginActivity_password);
        buttonLogin = (Button) findViewById(R.id.button_loginActivity_login);
        checkBoxRememberMe = findViewById(R.id.checkBox_loginActivity_rememberMe);
        checkBoxRememberMe.setOnCheckedChangeListener(this);

        //
        buttonLogin.setOnClickListener(this);

        //
//        MyUtilities.deletFile(remoteConfigServer.getCinematesData(), this);

        //
        rememberMeIsActive = checkRememberMe();

        // fast login
//        editTextEmail.setText("dev.mirror42@gmail.com");
//        editTextPassword.setText("a");

    }





    //---------------------------------------------------- METHODS

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



    public boolean checkRememberMe() {
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
        }

        return result;
    }


    private void standardLogin(String email, String password) {
        if(password==null) {
            // TODO handle password encrypt null result
            return;
        }

        //
        FirebaseAnalytics mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.METHOD, "email + password");
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.LOGIN, bundle);

        //
        final String dbFunction = "login";
        final OkHttpClient httpClient = new OkHttpClient();
        HttpUrl httpUrl = null;


        if(rememberMeIsActive) {
            // decryptFile
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
            }


            System.out.println(httpUrl);
            spinner.setVisibility(View.VISIBLE);
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

            System.out.println(httpUrl);
            spinner.setVisibility(View.VISIBLE);
        }





//
//        Context context = this;
//        SharedPreferences sharedPref = context.getSharedPreferences("cinemates_app_signature", Context.MODE_PRIVATE);


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
        }
    }


    @Override
    public void onFailure(Call call, IOException e) {
        showToastOnUiThread("Authentication server:\nCannot establish connection! D:");
    }

    @Override
    public void onResponse(Call call, Response response) throws IOException {
        try (ResponseBody responseBody = response.body()) {
            if ( ! response.isSuccessful()) {
                showToastOnUiThread("Authentication server:\nCannot establish connection! D:");
                return;
            }
//            System.out.println(response.body().string());
            String responseData = response.body().string();
            if( ! responseData.equals("null")) {
                JSONObject jsonObject = new JSONObject(responseData);
                showToastOnUiThread( "Authentication server:\nlogin successful\nwelcome: " + jsonObject.getString("Email"));
                resetTextLayout();
                //TODO: handle login response
                // encrypting

                if(checkBoxRememberMe.isChecked()) {
                    MyUtilities.encryptFile(remoteConfigServer.getCinematesData(), jsonObject.toString(), this);

                    // decryptFile
//                    String data = MyUtilities.decryptFile(remoteConfigServer.getCinematesData(), this);
//                    showToastOnUiThread(data);
                }

            }
            else {
                notifyError();
                showToastOnUiThread( "Authentication server:\ninvalid credentials");
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


    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if(isChecked) {
//            Toast.makeText(this, "Credenziali salvate", Toast.LENGTH_SHORT).show();
        }
        else {
            rememberMeIsActive = false;
            editTextPassword.setText("");
            MyUtilities.deletFile(remoteConfigServer.getCinematesData(), this);
            Toast.makeText(this, "Credenziali eliminate", Toast.LENGTH_SHORT).show();
        }
    }
}// end LoginActivity class