package mirror42.dev.cinemates.model;

import java.io.Serializable;

import mirror42.dev.cinemates.model.tmdb.Movie;


public class CustomListPost extends Post implements Serializable {
    private String listOwnerEmail;
    private Movie movie;
    private String listName;
    private String listDescription;

    public CustomListPost() {
        super(PostType.CL);
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

    public String getListName() {
        return listName;
    }

    public void setListName(String listName) {
        this.listName = listName;
    }

    public String getListDescription() {
        return listDescription;
    }

    public void setListDescription(String listDescription) {
        this.listDescription = listDescription;
    }

    public String getMovieTitle() {
        return movie.getTitle();
    }

    public String getMovieOverview() {
        return movie.getOverview();
    }


}
