package mirror42.dev.cinemates.ui.explore.latest;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;

import mirror42.dev.cinemates.api.tmdbAPI.TheMovieDatabaseApi;
import mirror42.dev.cinemates.model.tmdb.Movie;
import mirror42.dev.cinemates.utilities.MyValues.DownloadStatus;
import mirror42.dev.cinemates.utilities.ThreadManager;

public class LatestReleasesViewModel extends ViewModel {
    private final String TAG = getClass().getSimpleName();
    private MutableLiveData<ArrayList<Movie>> moviesList;
    private MutableLiveData<DownloadStatus> downloadStatus;
    private static ArrayList<Movie> cachedLatest;

    //----------------------------------------------- CONSTRUCTORS
    public LatestReleasesViewModel() {
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
        Runnable task = createDownloadTask(givenPage);
        ThreadManager t = ThreadManager.getInstance();
        try {
            t.runTaskInPool(task);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Runnable createDownloadTask(int givenPage) {
        return ()-> {
            int page = givenPage;
            TheMovieDatabaseApi tmdb = TheMovieDatabaseApi.getInstance();
            ArrayList<Movie> result = null;
            try {
                // fetching results
                result = tmdb.getLatest(givenPage);

                // once finished set results
//                Collections.shuffle(result);
                cachedLatest = new ArrayList<>(result);
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
        postMoviesList(cachedLatest);
        postDownloadStatus(DownloadStatus.SUCCESS);
        downloadStatus = new MutableLiveData<>(DownloadStatus.IDLE);

    }
}// end LatestReleasesViewModel class
