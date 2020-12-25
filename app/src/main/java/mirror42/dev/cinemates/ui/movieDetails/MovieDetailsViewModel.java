package mirror42.dev.cinemates.ui.movieDetails;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import mirror42.dev.cinemates.MyValues;
import mirror42.dev.cinemates.asyncTasks.DownloadMovieDetails;
import mirror42.dev.cinemates.tmdbAPI.Movie;

public class MovieDetailsViewModel extends ViewModel implements DownloadMovieDetails.DownloadListener {
    private MutableLiveData<Movie> movie;

    //----------------------------------------------- CONSTRUCTORS
    public MovieDetailsViewModel() {
        movie = new MutableLiveData<>();
    }


    //----------------------------------------------- GETTERS/SETTERS

    public void setMovie(Movie movie) {
        this.movie.setValue(movie);
    }

    public LiveData<Movie> getMovie() {
        return movie;
    }




    //----------------------------------------------- METHODS
    @Override
    public void onDownloadComplete(Movie movie, MyValues.DownloadStatus status) {
        if (status == MyValues.DownloadStatus.OK) {
            setMovie(movie);
        } else {
            setMovie(null);
        }
    }

    public void downloadData(int movieId) {
        // starting async task here because of google suggestions
        DownloadMovieDetails downloadMovieDetails = new DownloadMovieDetails(this);
        downloadMovieDetails.execute(movieId);
    }



}// end MovieDetailsViewModel class
