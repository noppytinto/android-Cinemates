package mirror42.dev.cinemates.utilities;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;

import mirror42.dev.cinemates.R;


public class RemoteConfig {
    private final String TAG = this.getClass().getSimpleName();
    private static RemoteConfig singletonInstance = null;
    private final FirebaseRemoteConfig mFirebaseRemoteConfig;
    private String test;
    private String azureBaseUrl;
    private String guestToken;
    private String cinematesAppSignature;
    private RemoteConfigListener listener;

    public interface RemoteConfigListener {
        public void onRemoteConfigLoaded(boolean taskIsSuccessful);
    }



    //------------------------------------------------------- CONSTRUCTORS

    private RemoteConfig() {
        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder().build();
        mFirebaseRemoteConfig.setConfigSettingsAsync(configSettings);
        mFirebaseRemoteConfig.setDefaultsAsync(R.xml.remote_config_params);
    }



    //------------------------------------------------------- GETTERS/SETTERS

    public static RemoteConfig getInstance() {
        if (singletonInstance == null)
            singletonInstance = new RemoteConfig();

        return singletonInstance;
    }

    public void setListener(RemoteConfigListener remoteConfigListener) {
        listener = remoteConfigListener;
    }

    public String getTest() {
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

                    // notify listener
                    listener.onRemoteConfigLoaded(true);

                } else {
                    Log.d(TAG, "remote config task failed!");
                    listener.onRemoteConfigLoaded(false);
                }
            }
        });
    }// end loadConfig()

}// end RemoteConfig class
