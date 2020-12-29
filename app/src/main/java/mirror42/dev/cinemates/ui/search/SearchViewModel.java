package mirror42.dev.cinemates.ui.search;


import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;

import mirror42.dev.cinemates.MyValues;
import mirror42.dev.cinemates.asynctask.DownloadMoviesList;
import mirror42.dev.cinemates.tmdbAPI.Movie;


/**
 * - MutableLiveData objects can be changed(directy and undirectly)
 * - LiveData objects cannot be changed directly!
 *   can only be observed! if changed undirectly!
 *   that's why we have
 *   searchViewModel.getText().observe(...)
 *
 *
 */
public class SearchViewModel extends ViewModel implements DownloadMoviesList.DownloadListener {
    private MutableLiveData<ArrayList<Movie>> moviesList;



    //----------------------------------------------- CONSTRUCTORS

    public SearchViewModel() {
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

    public void init(String query) {
        downloadData(query);
    }

    @Override
    public void onDownloadComplete(ArrayList<Movie> moviesList, MyValues.DownloadStatus status) {
        if (status == MyValues.DownloadStatus.OK) {
            setMoviesList(moviesList);
        } else {
            setMoviesList(null);
        }
    }


    private void downloadData(String query) {
        // starting async task here because of google suggestions
        DownloadMoviesList downloadMoviesList = new DownloadMoviesList(this);
        downloadMoviesList.execute(query);
    }
}// end SearchViewModel class
