package mirror42.dev.cinemates;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;

import javax.inject.Singleton;


public class RemoteConfig {

    private final String TAG = this.getClass().getSimpleName();

    private static RemoteConfig singletonInstance = null;
    private final FirebaseRemoteConfig mFirebaseRemoteConfig;
    private static String test;
    private static String azureBaseUrl;
    private static String guestToken;
    private static String cinematesAppSignature;
    private static RemoteConfigListener listener;

    public interface RemoteConfigListener {
        public void onRemoteConfigLoaded(boolean taskState);
    }



    //------------------------------------------------------- CONSTRUCTORS

    private RemoteConfig(RemoteConfigListener remoteConfigListener) {
        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder().build();
        mFirebaseRemoteConfig.setConfigSettingsAsync(configSettings);
        mFirebaseRemoteConfig.setDefaultsAsync(R.xml.remote_config_params);

    }



    //------------------------------------------------------- GETTERS/SETTERS

    public static RemoteConfig getInstance(RemoteConfigListener remoteConfigListener) {
        if (singletonInstance == null)
            singletonInstance = new RemoteConfig(remoteConfigListener);

        listener = remoteConfigListener;

        return singletonInstance;
    }

    public static String getTest() {
        Log.d("RemoteConfig: ", "getTest() called");
        return test;
    }

    public String getAzureBaseUrl() {
        return azureBaseUrl;
    }

    public String getGuestToken() {
        return guestToken;
    }

    public String getCinematesAppSignature() {
        return cinematesAppSignature;
    }



    //------------------------------------------------------- METHODS

    public void loadConfig() {
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

                    listener.onRemoteConfigLoaded(true);

                } else {
                    Log.d(TAG, "remote config task failed!");
                    listener.onRemoteConfigLoaded(false);
                }
            }
        });
    }// end loadConfig()

}// end RemoteConfig class
