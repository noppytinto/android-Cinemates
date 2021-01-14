package mirror42.dev.cinemates.utilities;

import android.content.Context;

import java.io.File;

import okhttp3.Cache;
import okhttp3.OkHttpClient;

public class OkHttpSingleton {
    private static OkHttpSingleton singletonInstance;
    private static OkHttpClient client;


    private OkHttpSingleton(Context context) {
        File httpCacheDirectory = new File(context.getCacheDir(), "http-cache");
        int cacheSize = 50 * 1024 * 1024; // 50 MiB
        Cache cache = new Cache(httpCacheDirectory, cacheSize);
        client = new OkHttpClient.Builder()
                .cache(cache)
                .build();
    }




    //-------------------------------------------------------- METHODS

    public static OkHttpSingleton getInstance(Context context) {
        if(singletonInstance==null)
            return new OkHttpSingleton(context);

        return singletonInstance;
    }

    public static OkHttpClient getClient() {
        return client;
    }









}// end OkHttpSingleton class
