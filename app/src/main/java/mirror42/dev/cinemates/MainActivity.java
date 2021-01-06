package mirror42.dev.cinemates;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import org.json.JSONObject;

import java.io.IOException;

import mirror42.dev.cinemates.ui.login.LoginActivity;
import mirror42.dev.cinemates.ui.login.LoginViewModel;
import mirror42.dev.cinemates.ui.login.UserViewModel;
import mirror42.dev.cinemates.utilities.FirebaseEventsLogger;
import mirror42.dev.cinemates.utilities.ImageUtilities;
import mirror42.dev.cinemates.utilities.MyUtilities;
import mirror42.dev.cinemates.utilities.RemoteConfigServer;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;


public class MainActivity extends AppCompatActivity implements Callback,
        RemoteConfigServer.RemoteConfigListener {
    private static final String EXTRA_MESSAGE = " ";
    private final String TAG = this.getClass().getSimpleName();
    private NavController navController;
    private RemoteConfigServer remoteConfigServer;
    public MenuItem loginItemMenu;
    private static boolean rememberMeExists;
    private String rememberMeData;
    private UserViewModel userViewModel;
    private LoginViewModel loginViewModel;


    //-------------------------------------------------------- LIFECYCLE METHODS
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate() called");
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar_mainActivity);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        // init firebase logger
        FirebaseEventsLogger firebaseEventsLogger = FirebaseEventsLogger.getInstance();
        firebaseEventsLogger.setUserConsensus(true); //TODO: fetch user consensus from DB

        // get remote params
        remoteConfigServer = RemoteConfigServer.getInstance();
        remoteConfigServer.setListener(this);
        remoteConfigServer.loadConfigParams();

        //
        navController = Navigation.findNavController(this, R.id.nav_host_fragment_main);
        NavigationUI.setupActionBarWithNavController(this, navController);




        // observe login activity changes
        loginViewModel = new ViewModelProvider(this).get(LoginViewModel.class);
        loginViewModel.getLoginResult().observe(this, loginResult -> {
            if (loginResult == MyUtilities.LoginResult.SUCCESS) {
                String profilePicturePath = loginViewModel.getUser().getValue().getProfilePicturePath();
                ImageUtilities.loadCircularImageInto(remoteConfigServer.getCloudinaryDownloadBaseUrl() + profilePicturePath, loginItemMenu, this);

            }
            else if(loginResult == MyUtilities.LoginResult.FAILED) {

            }
            else if(loginResult == MyUtilities.LoginResult.INVALID_REQUEST) {
            }
        });


        // observe logged user profile image changes
        loginViewModel.getUser().observe(this, user -> {

        });



    }// end onCreate()




    //-------------------------------------------------------- METHODS

    public void checkAnyPreviousRememberMe() {
        if(MyUtilities.checkFileExists(remoteConfigServer.getCinematesData(), this)) {
            rememberMeExists = true;
            try {
                JSONObject jsonObject = new JSONObject(MyUtilities.decryptFile(remoteConfigServer.getCinematesData(), this));
                rememberMeData = jsonObject.toString();

                String imagePath = jsonObject.getString("ProfileImage");

                if(imagePath!=null || (! imagePath.isEmpty())) {
                    ImageUtilities.loadCircularImageInto(remoteConfigServer.getCloudinaryDownloadBaseUrl() + imagePath, loginItemMenu, this);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        else {
            rememberMeData = null;
            rememberMeExists = false;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_activity_menu, menu);
        loginItemMenu = menu.getItem(1);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_notifications) {
//            Toast.makeText(this, "notifications", Toast.LENGTH_LONG).show();

//            try {
//                //
//                BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this, R.style.bottom_sheet_dialog_theme);
//                bottomSheetDialog.setDismissWithAnimation(true);
//                bottomSheetDialog.setTitle("test");
//                View bottomSheetView = LayoutInflater.from(getApplicationContext()).inflate(R.layout.bottom_sheet_layout, (ConstraintLayout)findViewById(R.id.bottom_sheet_container));
//                bottomSheetDialog.setContentView(bottomSheetView);
//
//                bottomSheetDialog.show();
//            } catch (Exception e) {
//                e.getMessage();
//                e.printStackTrace();
//            }
        }
        else if (id == R.id.action_login) {
//            intent.putExtra(EXTRA_MESSAGE, message);

            if(rememberMeExists) {
//                int LAUNCH_LOGIN_ACTIVITY = 1;
//                Intent intent = new Intent(this, LoginActivity.class);
//                intent.putExtra("mainActivityRememberMeData", rememberMeData);
//                startActivityForResult(intent, LAUNCH_LOGIN_ACTIVITY);
            }
            else {
                Intent intent = new Intent(this, LoginActivity.class);
                startActivity(intent);
            }


        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        int LAUNCH_SECOND_ACTIVITY = 1;

        if (requestCode ==  LAUNCH_SECOND_ACTIVITY) {
            if (resultCode == Activity.RESULT_OK) {
                //Get result
                String profileImagePath = data.getStringExtra("impagePathFromLoginFragment");

                if(profileImagePath.equals("LOGOUT")) {
                    loginItemMenu.setIcon(ImageUtilities.getDefaultProfilePictureIcon(this));
                }
                else if(profileImagePath.equals("LOGGED_BUT_NO_PICTURE")) {
                    // TODO: set fname/lname initilas
                }
                else if(profileImagePath.equals("-")) {
                    // ignore
                }
                else {
                    ImageUtilities.loadCircularImageInto(remoteConfigServer.getCloudinaryDownloadBaseUrl() + profileImagePath, loginItemMenu, this);
                }


            }
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        navController.navigateUp();
        return super.onSupportNavigateUp();
    }

    private void establishAzureConnection() {
        final String checkSignatureFunction = "check_app_signature?signature=";
        final OkHttpClient httpClient = new OkHttpClient();

//
//        Context context = this;
//        SharedPreferences sharedPref = context.getSharedPreferences("cinemates_app_signature", Context.MODE_PRIVATE);


        RemoteConfigServer remoteConfigServer = RemoteConfigServer.getInstance();
        try {
            Request request = new Request.Builder()
                    .url(remoteConfigServer.getAzureBaseUrl()+ checkSignatureFunction + remoteConfigServer.getCinematesAppSignature())
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
    public void onRemoteConfigLoaded(boolean taskIsSuccessful) {
        if(taskIsSuccessful) {
            Toast.makeText(this, "Firebase remote config:\nfetching config data completed", Toast.LENGTH_SHORT).show();
            establishAzureConnection();
            checkAnyPreviousRememberMe();
        }
        else {
            Toast.makeText(this, "Firebase remote config:\nfetching config data failed", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onFailure(Call call, IOException e) {
        showToastOnUiThread("Azure connection:\nCannot establish remote connection! D:");
    }

    @Override
    public void onResponse(Call call, Response response) {
        try (ResponseBody responseBody = response.body()) {
            if (!response.isSuccessful()) {
                showToastOnUiThread("Azure connection:\ncode: " + response.code() + " | message:" + response.message());
                
                return;
            }

            showToastOnUiThread( "Azure connection:\ncode: " + response.code() + " | message:" +  response.message());
        }
        catch (Exception e) {
            e.printStackTrace();
            showToastOnUiThread("Azure connection:\nCannot establish connection! D:");
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

    public static boolean getRememberMeState() {
        return rememberMeExists;
    }


}// end MainActivity class