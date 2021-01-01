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
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener, Callback,
        RemoteConfig.RemoteConfigListener {
    private EditText editTextEmail;
    private EditText editTextPassword;
    private Button buttonLogin;
    private ProgressBar spinner;
    private final OkHttpClient client = new OkHttpClient();

    public LoginActivity() {
    }


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



    @Override
    public void onClick(View v) {
        if(v.getId() == buttonLogin.getId()) {
            String email = editTextEmail.getText().toString();
            String password = editTextPassword.getText().toString();

            establisAzureRemoteConnection();

        }
    }


    private void establisAzureRemoteConnection() {
        final String checkSignatureFunction = "check_app_signature?signature=";

        try {
            Request request = new Request.Builder()
                    .url(RemoteConfig.getAzureBaseUrl()+ checkSignatureFunction + RemoteConfig.getCinematesAppSignature())
                    .header("User-Agent", "OkHttp Headers.java")
                    .addHeader("Accept", "application/json; q=0.5")
                    .addHeader("Accept", "application/vnd.github.v3+json")
                    .addHeader("Authorization", "Bearer " + RemoteConfig.getGuestToken())
                    .build();

            Call call = client.newCall(request);
            call.enqueue(this);

        }catch(Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onRemoteConfigLoaded(boolean taskIsSuccess) {
        if(taskIsSuccess) {
            spinner.setVisibility(View.GONE);
            editTextEmail.setVisibility(View.VISIBLE);
            editTextPassword.setVisibility(View.VISIBLE);
            buttonLogin.setVisibility(View.VISIBLE);
            Toast.makeText(this, "Fetch config data completed", Toast.LENGTH_SHORT).show();
        }
        else {
            spinner.setVisibility(View.GONE);
            Toast.makeText(this, "Fetch config data failed", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public void onFailure(Call call, IOException e) {
        final Toast toast = Toast.makeText(this, "Cannot establish remote connection! D:", Toast.LENGTH_SHORT);
        toast.show();
    }

    @Override
    public void onResponse(Call call, Response response) {
        try (ResponseBody responseBody = response.body()) {
            if (!response.isSuccessful()) {
                final Toast toast = Toast.makeText(this, "code: " + response.code() + " | message:" +  response.message(), Toast.LENGTH_SHORT);
                toast.show();
                return;
            }

            // print response
            final Toast toast = Toast.makeText(this, "code: " + response.code() + " | message:" +  response.message(), Toast.LENGTH_SHORT);
            toast.show();
        }
        catch (Exception e) {
            e.getMessage();
            e.printStackTrace();
            final Toast toast = Toast.makeText(this, "Cannot establish remote connection! D:", Toast.LENGTH_SHORT);
            toast.show();
        }
    }
}