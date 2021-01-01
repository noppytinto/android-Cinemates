package mirror42.dev.cinemates.ui.login;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;

import mirror42.dev.cinemates.R;
import mirror42.dev.cinemates.utilities.FirebaseEventsLogger;
import mirror42.dev.cinemates.utilities.RemoteConfigServer;

public class LoginActivity extends AppCompatActivity implements
        View.OnClickListener{
    private EditText editTextEmail;
    private EditText editTextPassword;
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
        spinner.setVisibility(View.VISIBLE);


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
        }
    }








}// end LoginActivity class