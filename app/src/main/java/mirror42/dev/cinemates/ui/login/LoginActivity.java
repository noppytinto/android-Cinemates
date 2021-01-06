package mirror42.dev.cinemates.ui.login;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import mirror42.dev.cinemates.R;

public class LoginActivity extends AppCompatActivity{
    private String profileImagePath = "-";
    private boolean rememberMeExists;




    //-------------------------------------------------------------------------------- LIFECYCLE METHODS

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
//        rememberMeExists = MainActivity.getRememberMeState();

//        //
//        if(rememberMeExists) {
//            String rememberMeData = getIntent().getStringExtra("mainActivityRememberMeData");
//
//            // start profile fragment
//            Intent intent = new Intent(this, UserProfileFragment.class);
//            intent.putExtra("loginActivityRememberMeData", rememberMeData);
//            startActivity(intent);
//
//        }
//        else {
//            // start login fragment
//            Intent intent = new Intent(this, LoginFragment.class);
//            startActivity(intent);
//        }

    }


}// end LoginActivity class