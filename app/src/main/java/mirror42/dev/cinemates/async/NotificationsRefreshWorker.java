package mirror42.dev.cinemates.async;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;



public class NotificationsRefreshWorker extends Worker {




    //-------------------------------------------------------------------------- CONSTRUCTORS

    public NotificationsRefreshWorker(
            @NonNull Context context,
            @NonNull WorkerParameters params) {
        super(context, params);
    }



    //-------------------------------------------------------------------------- ANDROID METHODS

    @NonNull
    @Override
    public Result doWork() {
        return null;
    }






    //-------------------------------------------------------------------------- METHODS

    








}//end NotificationsRefreshWorker class
