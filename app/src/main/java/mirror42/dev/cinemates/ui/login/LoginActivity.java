package mirror42.dev.cinemates.ui.login;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONObject;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import mirror42.dev.cinemates.R;
import mirror42.dev.cinemates.utilities.FirebaseEventsLogger;
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
        Callback {
    private TextInputLayout textInputLayoutEmail;
    private TextInputLayout textInputLayoutPassword;
    private TextInputEditText editTextEmail;
    private TextInputEditText editTextPassword;
    private Button buttonLogin;
    private ProgressBar spinner;
    private RemoteConfigServer remoteConfigServer;



    //---------------------------------------------------- LIFECYCLE METHODS

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

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

        //
        buttonLogin.setOnClickListener(this);

        // fast login
        editTextEmail.setText("dev.mirror42@gmail.com");
        editTextPassword.setText("a");

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

            try {
                textInputLayoutEmail.setError(null);
                textInputLayoutPassword.setError(null);
                login(email, toHexString(getSHA(password)));
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }

        }
    }

    public static byte[] getSHA(String input) throws NoSuchAlgorithmException {
        // Static getInstance method is called with hashing SHA
        MessageDigest md = MessageDigest.getInstance("SHA-256");

        // digest() method called
        // to calculate message digest of an input
        // and return array of byte
        return md.digest(input.getBytes(StandardCharsets.UTF_8));
    }

    public static String toHexString(byte[] hash) {
        // Convert byte array into signum representation
        BigInteger number = new BigInteger(1, hash);

        // Convert message digest into hex value
        StringBuilder hexString = new StringBuilder(number.toString(16));

        // Pad with leading zeros
        while (hexString.length() < 32)
        {
            hexString.insert(0, '0');
        }

        return hexString.toString();
    }



    private void login(String email, String password) {
        final String dbFunction = "login";
        final OkHttpClient httpClient = new OkHttpClient();
        RemoteConfigServer remoteConfigServer = RemoteConfigServer.getInstance();

        HttpUrl httpUrl = new HttpUrl.Builder()
                .scheme("https")
                .host(remoteConfigServer.getAzureHostName())
                .addPathSegments(remoteConfigServer.getPostgrestPath())
                .addPathSegment(dbFunction)
                .addQueryParameter("mail", email)
                .addQueryParameter("pass", password)
                .build();

        System.out.println(httpUrl);

        spinner.setVisibility(View.VISIBLE);


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


}// end LoginActivity class