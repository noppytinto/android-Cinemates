package mirror42.dev.cinemates.utilities;

import android.util.Log;

import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;

import mirror42.dev.cinemates.R;


public class RemoteConfigServer {
    private final String TAG = this.getClass().getSimpleName();
    private static RemoteConfigServer singletonInstance = null;
    private final FirebaseRemoteConfig mFirebaseRemoteConfig;
    private RemoteConfigListener listener;

    public String getCloudinaryName() {
        return mFirebaseRemoteConfig.getString("cloudinary_name");
    }

    public Object getCloudnaryApiKey() {
        return mFirebaseRemoteConfig.getString("cloudinary_api_key");
    }

    public Object getCloudnaryApiSecret() {
        return mFirebaseRemoteConfig.getString("cloudinary_api_secret");
    }

    public interface RemoteConfigListener {
        public void onRemoteParamsLoaded(boolean taskIsSuccessful);
    }



    //------------------------------------------------------- CONSTRUCTORS

    private RemoteConfigServer() {
        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .build();
        mFirebaseRemoteConfig.setConfigSettingsAsync(configSettings);
        mFirebaseRemoteConfig.setDefaultsAsync(R.xml.remote_config_params);

        //.setMinimumFetchIntervalInSeconds(0)
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

    public String getGmailUsername() {
        return mFirebaseRemoteConfig.getString("gmail_username");
    }

    public String getGmailAccount() {
        return mFirebaseRemoteConfig.getString("gmail_account");
    }

    public String getGmailPass() {
        return mFirebaseRemoteConfig.getString("gmail_pass");
    }



    //------------------------------------------------------- METHODS

    public void loadConfigParams() {
//        mFirebaseRemoteConfig.fetch(0);
//        mFirebaseRemoteConfig.activate();

        mFirebaseRemoteConfig.fetchAndActivate().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                boolean updated = task.getResult();
                // notify listener
                listener.onRemoteParamsLoaded(true);
            } else
                listener.onRemoteParamsLoaded(false);
        });
    }// end loadConfigParams()

    public void releaseListener() {
        listener = null;
    }

}// end RemoteConfig class
