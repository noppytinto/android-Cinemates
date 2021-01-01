package mirror42.dev.cinemates.ui.explore.popular;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;

import mirror42.dev.cinemates.utilities.MyValues;
import mirror42.dev.cinemates.asynctask.DownloadPopular;
import mirror42.dev.cinemates.tmdbAPI.model.Movie;

public class PopularViewModel extends ViewModel implements DownloadPopular.DownloadListener {
    private MutableLiveData<ArrayList<Movie>> moviesList;

    //----------------------------------------------- CONSTRUCTORS
    public PopularViewModel() {
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
        DownloadPopular downloadPopular = new DownloadPopular(this);
        downloadPopular.execute(1);
    }



}// end PopularFragmentViewModel class
