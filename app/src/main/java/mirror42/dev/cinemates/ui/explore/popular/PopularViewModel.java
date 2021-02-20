package mirror42.dev.cinemates.ui.explore.popular;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;

import mirror42.dev.cinemates.api.tmdbAPI.TheMovieDatabaseApi;
import mirror42.dev.cinemates.model.tmdb.Movie;
import mirror42.dev.cinemates.utilities.MyValues.DownloadStatus;

public class PopularViewModel extends ViewModel {
    private final String TAG = getClass().getSimpleName();
    private MutableLiveData<ArrayList<Movie>> moviesList;
    private MutableLiveData<DownloadStatus> downloadStatus;
    private static ArrayList<Movie> cachedPopular;



    //----------------------------------------------- CONSTRUCTORS
    public PopularViewModel() {
        moviesList = new MutableLiveData<>();
        downloadStatus = new MutableLiveData<>(DownloadStatus.IDLE);
    }


    //----------------------------------------------- GETTERS/SETTERS

    public void postMoviesList(ArrayList<Movie> moviesList) {
        this.moviesList.postValue(moviesList);
    }

    public LiveData<ArrayList<Movie>> getMoviesList() {
        return moviesList;
    }

    public void postDownloadStatus(DownloadStatus downloadStatus) {
        this.downloadStatus.postValue(downloadStatus);
    }

    public LiveData<DownloadStatus> getDownloadStatus() {
        return downloadStatus;
    }



    //----------------------------------------------- METHODS

    public void downloadData(int givenPage) {
        Runnable downloadTask = createDownloadTask(givenPage);
        Thread t = new Thread(downloadTask);
        t.start();
    }

    private Runnable createDownloadTask(int givenPage) {
        return ()-> {
            int page = givenPage;
            TheMovieDatabaseApi tmdb = TheMovieDatabaseApi.getInstance();
            ArrayList<Movie> result = null;

            result = new ArrayList<>();

            try {
                // querying TBDb
                result = tmdb.getPopular(givenPage);

                // once finished set results
//                Collections.shuffle(result);
                cachedPopular = new ArrayList<>(result);
                postMoviesList(result);
                postDownloadStatus(DownloadStatus.SUCCESS);
            } catch (Exception e) {
                // if the search returns nothing
                // moviesList will be null
                e.printStackTrace();
                postMoviesList(null);
                postDownloadStatus(DownloadStatus.FAILED);
            }
        };
    }// end createDownloadTask()

    public void loadCached() {
        postMoviesList(cachedPopular);
        postDownloadStatus(DownloadStatus.SUCCESS);
        downloadStatus = new MutableLiveData<>(DownloadStatus.IDLE);

    }


}// end PopularFragmentViewModel class
