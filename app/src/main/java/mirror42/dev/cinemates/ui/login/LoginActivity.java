package mirror42.dev.cinemates.ui.login;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;

import mirror42.dev.cinemates.R;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {
    private EditText editTextEmail;
    private EditText editTextPassword;
    private Button buttonLogin;
    private ProgressBar spinner;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

//        //
//        spinner = findViewById(R.id.progress_bar);
//        spinner.setVisibility(View.VISIBLE);
//
//        // get remote params
//        RemoteConfig remoteConfig = new RemoteConfig();
//        remoteConfig.loadConfig(this, spinner);
//
//        //
//        editTextEmail = (EditText) findViewById(R.id.loginActivity_editTextText_email);
//        editTextPassword = (EditText) findViewById(R.id.loginActivity_editTextText_password);
//        buttonLogin = findViewById(R.id.loginActivity_button_login);
//
//        //
//        buttonLogin.setOnClickListener(this);

    }



    @Override
    public void onClick(View v) {
        if(v.getId() == buttonLogin.getId()) {
            String email = editTextEmail.getText().toString();
            String password = editTextPassword.getText().toString();


        }

    }


    private boolean remoteConnectionIsEstablished() {
        boolean result = false;

        try {
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


}