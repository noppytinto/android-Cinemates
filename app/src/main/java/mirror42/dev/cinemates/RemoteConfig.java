package mirror42.dev.cinemates;

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

    public RemoteConfig() {
        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(0)
                .build();
        mFirebaseRemoteConfig.setConfigSettingsAsync(configSettings);
        mFirebaseRemoteConfig.setDefaultsAsync(R.xml.remote_config_params);

    }

    public void loadConfig(MainActivity mainActivity, ProgressBar progressBar) {
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
                            Log.d(TAG, "test: " + test);
                            progressBar.setVisibility(View.GONE);

                        } else {
                            Log.d(TAG, "remote config task failed!");
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(mainActivity, "Fetch config data failed", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }



    public static String getTest() {
        Log.d("RemoteConfig: ", "getTest() called");
        return test;
    }


}
