package mirror42.dev.cinemates.utilities;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;

import mirror42.dev.cinemates.R;


public class RemoteConfigServer {
    private final String TAG = this.getClass().getSimpleName();
    private static RemoteConfigServer singletonInstance = null;
    private final FirebaseRemoteConfig mFirebaseRemoteConfig;
    private RemoteConfigListener listener;

    public interface RemoteConfigListener {
        public void onRemoteConfigLoaded(boolean taskIsSuccessful);
    }



    //------------------------------------------------------- CONSTRUCTORS

    private RemoteConfigServer() {
        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .build();
        mFirebaseRemoteConfig.setConfigSettingsAsync(configSettings);
        mFirebaseRemoteConfig.setDefaultsAsync(R.xml.remote_config_params);
    }



    //------------------------------------------------------- GETTERS/SETTERS

    public static RemoteConfigServer getInstance() {
        if (singletonInstance == null)
            singletonInstance = new RemoteConfigServer();

        return singletonInstance;
    }

    public void setListener(RemoteConfigListener remoteConfigListener) {
        listener = remoteConfigListener;
    }

    public String getTest() {
        Log.d("RemoteConfig: ", "getTest() called");
        return mFirebaseRemoteConfig.getString("test");
    }

    public String getAzureBaseUrl() {
        return mFirebaseRemoteConfig.getString("azure_base_url");
    }

    public String getGuestToken() {
        return mFirebaseRemoteConfig.getString("guest_token");
    }

    public String getCinematesAppSignature() {
        return mFirebaseRemoteConfig.getString("cinemates_app_signature");
    }

    public String getAzureHostName() {
        return mFirebaseRemoteConfig.getString("azure_host_name");
    }

    public String getPostgrestPath() {
        return mFirebaseRemoteConfig.getString("postgrest_path_segment");
    }

    public String getCloudinaryUploadBaseUrl() {
        return mFirebaseRemoteConfig.getString("cloudinary_upload_base_url");
    }

    public String getCloudinaryDownloadBaseUrl() {
        return mFirebaseRemoteConfig.getString("cloudinary_download_base_url");
    }

    public String getCinematesData() {
        return mFirebaseRemoteConfig.getString("cinemates_data");
    }



    //------------------------------------------------------- METHODS

    public void loadConfigParams() {
//        mFirebaseRemoteConfig.fetch(0);
//        mFirebaseRemoteConfig.activate();

        mFirebaseRemoteConfig.fetchAndActivate().addOnCompleteListener(new OnCompleteListener<Boolean>() {
            @Override
            public void onComplete(@NonNull Task<Boolean> task) {
                if (task.isSuccessful()) {
                    Log.d(TAG, "remote config task successful");
                    boolean updated = task.getResult();

                    // notify listener
                    listener.onRemoteConfigLoaded(true);

                } else {
                    Log.d(TAG, "remote config task failed!");
                    listener.onRemoteConfigLoaded(false);
                }
            }
        });
    }// end loadConfigParams()

}// end RemoteConfig class
