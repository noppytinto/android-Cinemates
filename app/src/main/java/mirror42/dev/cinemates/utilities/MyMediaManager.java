package mirror42.dev.cinemates.utilities;

import android.content.Context;

import com.cloudinary.android.MediaManager;

import java.util.HashMap;
import java.util.Map;

public class MyMediaManager {
    private static MyMediaManager singletonInstance;

    private MyMediaManager(Context context, RemoteConfigServer remoteConfigServer) {
        Map config = new HashMap();
        config.put("cloud_name", remoteConfigServer.getCloudinaryName());
        config.put("api_key", remoteConfigServer.getCloudnaryApiKey());
        config.put("api_secret", remoteConfigServer.getCloudnaryApiSecret());

        MediaManager.init(context, config);
    }

    public static void init(Context context, RemoteConfigServer remoteConfigServer) {
        if(singletonInstance==null)
            singletonInstance = new MyMediaManager(context, remoteConfigServer);
    }

    public static MyMediaManager getInstance(Context context, RemoteConfigServer remoteConfigServer) {
        if(singletonInstance==null)
            singletonInstance = new MyMediaManager(context, remoteConfigServer);

        return singletonInstance;
    }





}// end MyMediaManager class
