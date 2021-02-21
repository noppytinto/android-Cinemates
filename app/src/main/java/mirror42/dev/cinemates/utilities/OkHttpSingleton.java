package mirror42.dev.cinemates.utilities;

import android.content.Context;

import java.io.File;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

import okhttp3.Cache;
import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;

public class OkHttpSingleton {
    private static OkHttpSingleton singletonInstance;
    private static OkHttpClient client;



    //-------------------------------------------------------------------- CONSTRUCTORS

    private OkHttpSingleton(Context context) {
        File httpCacheDirectory = new File(context.getCacheDir(), "http-cache");
        int cacheSize = 100 * 1024 * 1024; // 50 MB cache
        Cache cache = new Cache(httpCacheDirectory, cacheSize);
        client = new OkHttpClient.Builder()
                .cache(cache)
                .connectTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .connectionPool(new ConnectionPool(0, 5, TimeUnit.MINUTES))
                .protocols(Collections.singletonList(Protocol.HTTP_1_1))
                .build();

        // DON'T FORGET TO MAKE RETROFIT TO USE IT, OTW A SOCKET EXCEPTION CAN OCCUR
//        final Retrofit retrofitClient = new Retrofit.Builder()
//                .client(OkHttpSingleton.getClient())
//                .addConverterFactory(GsonConverterFactory.create())
//                .baseUrl(getBaseURL())
//                .build();


        // in case of SocketTimoutException, increase timeouts
//        client = new OkHttpClient.Builder()
//                .cache(cache)
//                .connectTimeout(60, TimeUnit.SECONDS)
//                .writeTimeout(60, TimeUnit.SECONDS)
//                .readTimeout(60, TimeUnit.SECONDS)
//                .build();

        // in case the soluton above doesn't fix the problem
//        client = new OkHttpClient.Builder()
//                .cache(cache)
//                .connectTimeout(60, TimeUnit.SECONDS)
//                .writeTimeout(60, TimeUnit.SECONDS)
//                .readTimeout(60, TimeUnit.SECONDS)
//                .retryOnConnectionFailure(true)
//                .connectionPool(new ConnectionPool(0, 5, TimeUnit.MINUTES))
//                .protocols(Collections.singletonList(Protocol.HTTP_1_1))
//                .build();
    }




    //-------------------------------------------------------------------- MY METHODS

    public static OkHttpSingleton getInstance(Context context) {
        if(singletonInstance==null)
            return new OkHttpSingleton(context);

        return singletonInstance;
    }


    public static OkHttpClient getClient() {
        return client;
    }



}// end OkHttpSingleton class
