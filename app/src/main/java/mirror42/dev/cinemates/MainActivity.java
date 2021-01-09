package mirror42.dev.cinemates;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import mirror42.dev.cinemates.model.User;
import mirror42.dev.cinemates.ui.login.LoginViewModel;
import mirror42.dev.cinemates.utilities.FirebaseAnalytics;
import mirror42.dev.cinemates.utilities.ImageUtilities;
import mirror42.dev.cinemates.utilities.RemoteConfigServer;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;


public class MainActivity extends AppCompatActivity implements RemoteConfigServer.RemoteConfigListener {
    private final String TAG = this.getClass().getSimpleName();
    private NavController navController;
    private RemoteConfigServer remoteConfigServer;
    public MenuItem loginMenuItem;
    public MenuItem notificationMenuItem;
    private static boolean rememberMeExists;
    private LoginViewModel loginViewModel;
    private ImageView toolbarLogo;


    //-------------------------------------------------------- ANDROID METHODS
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar_mainActivity);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbarLogo = findViewById(R.id.imageView_mainActivity_logo);
        //
        navController = Navigation.findNavController(this, R.id.nav_host_fragment_main);
        NavigationUI.setupActionBarWithNavController(this, navController);

        // init firebase analytics
        FirebaseAnalytics firebaseAnalytics = FirebaseAnalytics.getInstance();
        firebaseAnalytics.setUserConsent(true); //TODO: fetch user consensus from DB

        // observe activity about login
        loginViewModel = new ViewModelProvider(this).get(LoginViewModel.class);
        loginViewModel.getLoginResult().observe(this, loginResult -> {
            switch (loginResult) {
                case SUCCESS: {
                    User user = loginViewModel.getUser().getValue();
                    String profilePicturePath = user.getProfilePicturePath();
                    ImageUtilities.loadCircularImageInto(remoteConfigServer.getCloudinaryDownloadBaseUrl() + profilePicturePath, loginMenuItem, this);
                    notificationMenuItem.setVisible(true);
                    }
                    break;
                case LOGOUT:
                    showToolbarElements();
                    ImageUtilities.loadDefaultProfilePictureInto(loginMenuItem, this);
                    notificationMenuItem.setVisible(false);
                    rememberMeExists = false;
                    break;
                case REMEMBER_ME_EXISTS:
                    try {
                        User remeberMeUser = loginViewModel.getUser().getValue();

                        // set profile picture
                        String imagePath = remeberMeUser.getProfilePicturePath();
                        ImageUtilities.loadCircularImageInto(remoteConfigServer.getCloudinaryDownloadBaseUrl() + imagePath, loginMenuItem, this);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                }
        });



        // get remote params
        init();

    }// end onCreate()

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_activity_menu, menu);
        loginMenuItem = menu.getItem(1);
        notificationMenuItem = menu.getItem(0);

        loginMenuItem.setVisible(true);

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
            if(rememberMeExists || (loginViewModel.getLoginResult().getValue() == LoginViewModel.LoginResult.SUCCESS)) {
                navController.navigate(R.id.action_main_fragment_to_userProfileFragment);
            }
            else {
                try {
                    navController.navigate(R.id.action_main_fragment_to_loginFragment);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem notificationMenuItem = menu.getItem(0);

        // if is logged
        if(rememberMeExists) {
            notificationMenuItem.setVisible(true);
        }
        else {
            notificationMenuItem.setVisible(false);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onSupportNavigateUp() {
        navController.navigateUp();
        return super.onSupportNavigateUp();
    }




    //-------------------------------------------------------- METHODS

    private void init() {
        remoteConfigServer = RemoteConfigServer.getInstance();
        remoteConfigServer.setListener(this);
        remoteConfigServer.loadConfigParams();
    }

    private void establishAzureConnection() {
        final String checkSignatureFunction = "check_app_signature?signature=";
        final OkHttpClient httpClient = new OkHttpClient();

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
            call.enqueue(new Callback() {
                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    showToastOnUiThread("Azure connection:\nCannot establish remote connection! D:");

                }

                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                    try (ResponseBody responseBody = response.body()) {
                        if (response.isSuccessful()) {
                            showToastOnUiThread( "Azure connection:\ncode: " + response.code() + " | message:" +  response.message());
                        }
                        else {
                            showToastOnUiThread("Azure connection:\ncode: " + response.code() + " | message:" + response.message());
                        }
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                        showToastOnUiThread("Azure connection:\nCannot establish connection! D:");
                    }
                }
            });

        }catch(Exception e) {
            e.printStackTrace();
            showToastOnUiThread("Azure connection:\nCannot establish connection! D:");
        }
    }

    @Override
    public void onRemoteConfigLoaded(boolean taskIsSuccessful) {
        if(taskIsSuccessful) {
            Toast.makeText(this, "Firebase remote config:\nfetching config data completed", Toast.LENGTH_SHORT).show();
            establishAzureConnection();
            loginViewModel.checkRememberMeData(this);
        }
        else {
            Toast.makeText(this, "Firebase remote config:\nfetching config data failed", Toast.LENGTH_SHORT).show();
        }
    }

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

    public void hideToolbarElements() {
        loginMenuItem.setVisible(false);
        toolbarLogo.setVisibility(View.INVISIBLE);
    }

    public void showToolbarElements() {
        loginMenuItem.setVisible(true);
        toolbarLogo.setVisibility(View.VISIBLE);
    }

}// end MainActivity class