package mirror42.dev.cinemates.ui.login;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import mirror42.dev.cinemates.R;

public class LoginActivity extends AppCompatActivity implements LoginFragment.ProfileImageListener {
    private String profileImagePath = "-";




    //-------------------------------------------------------------------------------- LIFECYCLE METHODS

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

    }


    @Override
    public void onBackPressed() {
        Intent returnIntent = new Intent();
        returnIntent.putExtra("impagePathFromLoginFragment", profileImagePath);
        setResult(Activity.RESULT_OK,returnIntent);
        finish();
    }


    @Override
    public void onProfileImageReady(String profileImagePath) {
        this.profileImagePath = profileImagePath;
    }

}// end LoginActivity class