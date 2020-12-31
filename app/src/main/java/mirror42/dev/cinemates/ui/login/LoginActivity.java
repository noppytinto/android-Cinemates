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

            remoteConnectionIsEstablished();

        }
    }


    private boolean remoteConnectionIsEstablished() {
        boolean result = false;

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


//            HttpClient client = HttpClient.newHttpClient();
//            HttpRequest request = HttpRequest.newBuilder()
//                    .uri(URI.create(ConfigurationManager.getHttpsBaseUrl() + ConfigurationManager.getCheckSignature() + ConfigurationManager.getCinematesAppSignature()))
//                    .header("Authorization", "Bearer " + ConfigurationManager.getGuestToken())
//                    .header("Content-Type", "application/json")
//                    .build();
//
//            // getting http response
//            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
//            result = Boolean.parseBoolean(response.body());


        }catch(Exception e) {
            e.printStackTrace();
        }

        return result;
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

    }

    @Override
    public void onResponse(Call call, Response response) {
        try (ResponseBody responseBody = response.body()) {
            if (!response.isSuccessful()) {
                System.out.println("Unexpected code " + response);
                Toast.makeText(this, "Remote connection failed! D:", Toast.LENGTH_SHORT).show();
                return;
            }

            // print response
            Headers responseHeaders = response.headers();
            for (int i = 0, size = responseHeaders.size(); i < size; i++) {
                System.out.println(responseHeaders.name(i) + ": " + responseHeaders.value(i));
            }
            System.out.println(responseBody.string());


            //
        }
        catch (Exception e) {
            e.getMessage();
            e.printStackTrace();
        }
    }
}