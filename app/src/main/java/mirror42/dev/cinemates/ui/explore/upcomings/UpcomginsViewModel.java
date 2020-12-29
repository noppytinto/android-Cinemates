package mirror42.dev.cinemates.ui.explore.upcomings;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;

import mirror42.dev.cinemates.MyValues;
import mirror42.dev.cinemates.asynctask.DownloadUpcomings;
import mirror42.dev.cinemates.api.tmdbAPI.Movie;

public class UpcomginsViewModel extends ViewModel implements DownloadUpcomings.DownloadListener {
    private MutableLiveData<ArrayList<Movie>> moviesList;

    //----------------------------------------------- CONSTRUCTORS
    public UpcomginsViewModel() {
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
        DownloadUpcomings downloadUpcomings = new DownloadUpcomings(this);
        downloadUpcomings.execute(1);
    }




}// end UpcomginsViewModel class
