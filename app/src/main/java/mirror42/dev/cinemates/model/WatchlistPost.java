package mirror42.dev.cinemates.model;

import java.util.ArrayList;

import mirror42.dev.cinemates.tmdbAPI.model.Movie;

public class WatchlistPost extends Post {
    private String listOwnerEmail;
    private String thumbnail_1_url;
    private String thumbnail_2_url;
    private String thumbnail_3_url;
    private ArrayList<Movie> addedMovies;
    private Movie movie;


    public String getThumbnail_1_url() {
        return thumbnail_1_url;
    }

    public void setThumbnail_1_url(String thumbnail_1_url) {
        this.thumbnail_1_url = thumbnail_1_url;
    }

    public String getThumbnail_2_url() {
        return thumbnail_2_url;
    }

    public void setThumbnail_2_url(String thumbnail_2_url) {
        this.thumbnail_2_url = thumbnail_2_url;
    }

    public String getThumbnail_3_url() {
        return thumbnail_3_url;
    }

    public void setThumbnail_3_url(String thumbnail_3_url) {
        this.thumbnail_3_url = thumbnail_3_url;
    }

    public String getListOwnerEmail() {
        return listOwnerEmail;
    }

    public void setListOwnerEmail(String listOwnerEmail) {
        this.listOwnerEmail = listOwnerEmail;
    }

    public ArrayList<Movie> getAddedMovies() {
        return addedMovies;
    }

    public void setAddedMovies(ArrayList<Movie> addedMovies) {
        this.addedMovies = addedMovies;
    }

    public Movie getMovie() {
        return movie;
    }

    public void setMovie(Movie movie) {
        this.movie = movie;
    }

}// end WatchlistPost class
