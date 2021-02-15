package mirror42.dev.cinemates.model;

import java.io.Serializable;

import mirror42.dev.cinemates.model.tmdb.Movie;

public class FavoritesPost extends Post implements Serializable {
    private String listOwnerEmail;
    private Movie movie;

    public FavoritesPost() {
        super(PostType.FV);
    }


    public String getListOwnerEmail() {
        return listOwnerEmail;
    }

    public void setListOwnerEmail(String listOwnerEmail) {
        this.listOwnerEmail = listOwnerEmail;
    }

    public Movie getMovie() {
        return movie;
    }

    public void setMovie(Movie movie) {
        this.movie = movie;
    }

    public String getMovieTitle() {
        return movie.getTitle();
    }

    public String getMovieOverview() {
        return movie.getOverview();
    }
}
