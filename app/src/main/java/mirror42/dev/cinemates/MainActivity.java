package mirror42.dev.cinemates;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import mirror42.dev.cinemates.async.NotificationsRefreshWorker;
import mirror42.dev.cinemates.model.User;
import mirror42.dev.cinemates.ui.login.LoginViewModel;
import mirror42.dev.cinemates.utilities.FirebaseAnalytics;
import mirror42.dev.cinemates.utilities.ImageUtilities;
import mirror42.dev.cinemates.utilities.OkHttpSingleton;
import mirror42.dev.cinemates.utilities.RemoteConfigServer;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;


public class MainActivity extends AppCompatActivity implements RemoteConfigServer.RemoteConfigListener {
    private final String TAG = this.getClass().getSimpleName();
    public static final String CHANNEL_ID = "15";
    private NavController navController;
    private RemoteConfigServer remoteConfigServer;
    public MenuItem loginMenuItem;
    public MenuItem menuItemNotifications;
    private static boolean rememberMeExists;
    private LoginViewModel loginViewModel;
    private ImageView toolbarLogo;
    private Toolbar toolbar;
    private ProgressDialog progressDialog;



    //---------------------------------------------------------------------------------------------- ANDROID METHODS

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = findViewById(R.id.toolbar_mainActivity);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        toolbarLogo = findViewById(R.id.imageView_mainActivity_logo);
//        toolbarLogo.setVisibility(View.GONE);
        navController = Navigation.findNavController(this, R.id.nav_host_fragment_main);
        NavigationUI.setupActionBarWithNavController(this, navController);
        // init firebase analytics
        FirebaseAnalytics firebaseAnalytics = FirebaseAnalytics.getInstance();
        firebaseAnalytics.setUserConsent(true); //TODO: fetch user consensus from DB
        //init okhttp
        OkHttpSingleton.getInstance(getApplicationContext());

        // observe login acitivty
        loginViewModel = new ViewModelProvider(this).get(LoginViewModel.class);
        loginViewModel.getLoginResult().observe(this, loginResult -> {
            hideProgressDialog();
            switch (loginResult) {
                case SUCCESS: {
                    User user = loginViewModel.getLoggedUser().getValue();
                    String profilePicturePath = user.getProfilePicturePath();
                    ImageUtilities.loadCircularImageInto(profilePicturePath, loginMenuItem, this);
                    invalidateOptionsMenu();
                    }
                    break;
                case LOGGED_OUT:
                    ImageUtilities.loadDefaultProfilePictureInto(loginMenuItem, this);
                    invalidateOptionsMenu();
                    rememberMeExists = false;
                    break;
                case REMEMBER_ME_EXISTS:
                    rememberMeExists = true;
                    invalidateOptionsMenu();
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

        loginMenuItem = menu.findItem(R.id.menu_item_login);
        menuItemNotifications = menu.findItem(R.id.menu_item_notifications);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.menu_item_notifications) {
            navController.navigate(R.id.action_global_notificationsFragment);
        }
        else if(item.getItemId() == R.id.menu_item_login) {
            if(rememberMeExists || (loginViewModel.getLoginResult().getValue() == LoginViewModel.LoginResult.SUCCESS)) {
                navController.navigate(R.id.action_global_personalProfileFragment);
            }
            else {
                try {
                    navController.navigate(R.id.action_global_loginFragment);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // if is logged
        if(rememberMeExists || loginViewModel.getLoginResult().getValue() == LoginViewModel.LoginResult.SUCCESS) {
            menuItemNotifications.setVisible(true);

            // set profile picture
            User remeberMeUser = loginViewModel.getLoggedUser().getValue();
            String imagePath = null;
            try {
                imagePath = remeberMeUser.getProfilePicturePath();
            } catch (Exception e) {
                e.printStackTrace();
            }
            ImageUtilities.loadCircularImageInto(imagePath, loginMenuItem, this);
        }
        else {
            menuItemNotifications.setVisible(false);
            loginMenuItem.setVisible(true);
        }

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onSupportNavigateUp() {
        navController.navigateUp();
        return super.onSupportNavigateUp();
    }





    //---------------------------------------------------------------------------------------------- MY METHODS

    private void init() {
        loadRemoteParams();

        // on testing
//        startNotificationRefreshWorker();
//        startFCM();
    }

    private void loadRemoteParams() {
        showProgressDialog();
        remoteConfigServer = RemoteConfigServer.getInstance();
        remoteConfigServer.setListener(this);
        remoteConfigServer.loadConfigParams();
    }

    @Override
    public void onRemoteParamsLoaded(boolean taskIsSuccessful) {
        if(taskIsSuccessful) {
            Toast.makeText(this, "Firebase remote config:\nfetching config data completed", Toast.LENGTH_SHORT).show();
            establishAzureConnection();
            loginViewModel.checkRememberMeData(this);
        }
        else
            Toast.makeText(this, "Firebase remote config:\nfetching config data failed", Toast.LENGTH_SHORT).show();

        remoteConfigServer.releaseListener();
    }

    private void establishAzureConnection() {
        final String checkSignatureFunction = "check_app_signature?signature=";
        final OkHttpClient httpClient = OkHttpSingleton.getClient();

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
                        if (response.isSuccessful())
                            showToastOnUiThread( "Azure connection:\ncode: " + response.code() + " | message:" +  response.message());
                        else
                            showToastOnUiThread("Azure connection:\ncode: " + response.code() + " | message:" + response.message());

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

    private void showToastOnUiThread(String toastMessage) {
        runOnUiThread(() -> {
            // print response
            final Toast toast = Toast.makeText(getApplicationContext(), toastMessage, Toast.LENGTH_SHORT);
            toast.show();
        });
    }// end showToastOnUiThread()

    public void hideLogo() {
        toolbarLogo.setVisibility(View.GONE);
    }

    public void showLogo() {
        toolbarLogo.setVisibility(View.VISIBLE);

    }

    public void hideToolbar() {
        toolbar.setVisibility(View.GONE);
    }

    public void showToolbar() {
        toolbar.setVisibility(View.VISIBLE);
    }

    private void showProgressDialog() {
        //notes: Declare progressDialog before so you can use .hide() later!
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Avvio in corso...");
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.show();
    }

    private void hideProgressDialog() {
        if(progressDialog!=null)
            progressDialog.hide();
    }


    // on testing
    private void startNotificationRefreshWorker() {
        WorkRequest notificationRefreshRequest =
                new PeriodicWorkRequest.Builder(NotificationsRefreshWorker.class, 15, TimeUnit.MINUTES)
                        .build();

        WorkManager
                .getInstance(getApplicationContext())
                .enqueue(notificationRefreshRequest);

        createNotificationChannel();
    }
    private void startFCM() {
//        FirebaseMessaging.getInstance().getToken()
//                .addOnCompleteListener(new OnCompleteListener<String>() {
//                    @Override
//                    public void onComplete(@NonNull Task<String> task) {
//                        if (!task.isSuccessful()) {
//                            Log.w(TAG, "Fetching FCM registration token failed", task.getException());
//                            return;
//                        }
//
//                        // Get new FCM registration token
//                        String token = task.getResult();
//
//                        // Log and toast
//                        String msg = "FCM: " + token;
//                        Log.d(TAG, msg);
//                        Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
//                    }
//                });


        FirebaseMessaging.getInstance().subscribeToTopic("notifications")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        String msg = "subscribed to receive notifications";
                        if (!task.isSuccessful()) {
                            msg = "notification subscription failed! :(";
                        }
                        Log.d(TAG, msg);
                        Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
                    }
                });
    }
    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

}// end MainActivity class