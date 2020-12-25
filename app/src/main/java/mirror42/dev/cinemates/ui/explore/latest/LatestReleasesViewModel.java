package mirror42.dev.cinemates.ui.explore.latest;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;

import mirror42.dev.cinemates.MyValues;
import mirror42.dev.cinemates.asyncTasks.DownloadLatestReleases;
import mirror42.dev.cinemates.tmdbAPI.Movie;

public class LatestReleasesViewModel extends ViewModel implements DownloadLatestReleases.DownloadListener {
    private MutableLiveData<ArrayList<Movie>> moviesList;

    //----------------------------------------------- CONSTRUCTORS
    public LatestReleasesViewModel() {
        moviesList = new MutableLiveData<>();
    }


    //----------------------------------------------- GETTERS/SETTERS

    public void setMoviesList(ArrayList<Movie> moviesList) {
        this.moviesList.setValue(moviesList);
    }

    public LiveData<ArrayList<Movie>> getMoviesList() {
        return moviesList;
    }




    //----------------------------------------------- METHODS

    @Override
    public void onDownloadComplete(ArrayList<Movie> moviesList, MyValues.DownloadStatus status) {
        if (status == MyValues.DownloadStatus.OK) {
            setMoviesList(moviesList);
        } else {
            setMoviesList(null);
        }
    }

    public void downloadData() {
        // starting async task here because of google suggestions
        DownloadLatestReleases downloadLatestReleases = new DownloadLatestReleases(this);
        downloadLatestReleases.execute(1);
    }
}// end LatestReleasesViewModel class
