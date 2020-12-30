package mirror42.dev.cinemates;

import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;

public class RemoteConfig {
    private final String TAG = this.getClass().getSimpleName();
    private final FirebaseRemoteConfig mFirebaseRemoteConfig;
    private static String test;
    private static String azureBaseUrl;
    private static String guestToken;
    private static String cinematesAppSignature;

    public RemoteConfig() {
        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .build();
        mFirebaseRemoteConfig.setConfigSettingsAsync(configSettings);
        mFirebaseRemoteConfig.setDefaultsAsync(R.xml.remote_config_params);

    }

    public void loadConfig(Activity activity, ProgressBar progressBar) {
        test = mFirebaseRemoteConfig.getString("test");


//        mFirebaseRemoteConfig.fetch(0);
//        mFirebaseRemoteConfig.activate();

        mFirebaseRemoteConfig.fetchAndActivate().addOnCompleteListener(new OnCompleteListener<Boolean>() {
                    @Override
                    public void onComplete(@NonNull Task<Boolean> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "remote config task successful");
                            boolean updated = task.getResult();
                            test = mFirebaseRemoteConfig.getString("test");
                            azureBaseUrl = mFirebaseRemoteConfig.getString("azure_base_url");
                            guestToken = mFirebaseRemoteConfig.getString("guest_token");
                            cinematesAppSignature = mFirebaseRemoteConfig.getString("cinemates_app_signature");

                            progressBar.setVisibility(View.GONE);

                        } else {
                            Log.d(TAG, "remote config task failed!");
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(activity, "Fetch config data failed", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }



    public static String getTest() {
        Log.d("RemoteConfig: ", "getTest() called");
        return test;
    }

    public static String getAzureBaseUrl() {
        return azureBaseUrl;
    }

    public static String getGuestToken() {
        return guestToken;
    }

    public static String getCinematesAppSignature() {
        return cinematesAppSignature;
    }
}
