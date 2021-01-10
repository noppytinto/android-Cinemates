package mirror42.dev.cinemates.utilities;

import android.app.Application;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CinematesApplication extends Application {

    ExecutorService executorService = Executors.newFixedThreadPool(4);




}
