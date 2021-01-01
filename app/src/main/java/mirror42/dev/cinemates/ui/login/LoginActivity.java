package mirror42.dev.cinemates.ui.login;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.io.IOException;
import mirror42.dev.cinemates.R;
import mirror42.dev.cinemates.RemoteConfig;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class LoginActivity extends AppCompatActivity implements
        View.OnClickListener,
        Callback,
        RemoteConfig.RemoteConfigListener {
    private EditText editTextEmail;
    private EditText editTextPassword;
    private Button buttonLogin;
    private ProgressBar spinner;



    //---------------------------------------------------- LIFECYCLE METHODS

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //
        spinner = findViewById(R.id.progresBar_loginActivity);
        spinner.setVisibility(View.VISIBLE);

        // get remote params
        RemoteConfig remoteConfig = new RemoteConfig(this);
        remoteConfig.loadConfig();

        //
        editTextEmail = (EditText) findViewById(R.id.editTextText_loginActivity_email);
        editTextPassword = (EditText) findViewById(R.id.editTextText_loginActivity_password);
        buttonLogin = findViewById(R.id.button_loginActivity_login);

        //
        buttonLogin.setOnClickListener(this);

    }





    //---------------------------------------------------- METHODS

    @Override
    public void onClick(View v) {
        if(v.getId() == buttonLogin.getId()) {
            String email = editTextEmail.getText().toString();
            String password = editTextPassword.getText().toString();

            establishAzureConnection();

        }
    }


    private void establishAzureConnection() {
        final String checkSignatureFunction = "check_app_signature?signature=";
        final OkHttpClient httpClient = new OkHttpClient();

        try {
            Request request = new Request.Builder()
                    .url(RemoteConfig.getAzureBaseUrl()+ checkSignatureFunction + RemoteConfig.getCinematesAppSignature())
                    .header("User-Agent", "OkHttp Headers.java")
                    .addHeader("Accept", "application/json; q=0.5")
                    .addHeader("Accept", "application/vnd.github.v3+json")
                    .addHeader("Authorization", "Bearer " + RemoteConfig.getGuestToken())
                    .build();

            Call call = httpClient.newCall(request);
            call.enqueue(this);

        }catch(Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onRemoteConfigLoaded(boolean taskIsSuccessful) {
        if(taskIsSuccessful) {
            spinner.setVisibility(View.GONE);
            editTextEmail.setVisibility(View.VISIBLE);
            editTextPassword.setVisibility(View.VISIBLE);
            buttonLogin.setVisibility(View.VISIBLE);
            Toast.makeText(this, "HTTP response\nFetching config data completed", Toast.LENGTH_SHORT).show();
        }
        else {
            spinner.setVisibility(View.GONE);
            Toast.makeText(this, "HTTP response\nFetching config data failed", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public void onFailure(Call call, IOException e) {
        showToastOnUiThread("HTTP response\nCannot establish remote connection! D:");
    }

    @Override
    public void onResponse(Call call, Response response) {
        try (ResponseBody responseBody = response.body()) {
            if (!response.isSuccessful()) {
                showToastOnUiThread("HTTP response\ncode: " + response.code() + " | message:" +  response.message());
                return;
            }

            showToastOnUiThread("HTTP response\ncode: " + response.code() + " | message:" +  response.message());
        }
        catch (Exception e) {
            e.printStackTrace();
            showToastOnUiThread("HTTP response\nCannot establish remote connection! D:");
        }
    }// end onResponse()


    private void showToastOnUiThread(String toastMessage) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // print response
                final Toast toast = Toast.makeText(getApplicationContext(), toastMessage, Toast.LENGTH_SHORT);
                toast.show();
            }
        });
    }// end showToastOnUiThread()




}// end LoginActivity class