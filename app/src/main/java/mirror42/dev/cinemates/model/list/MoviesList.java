package mirror42.dev.cinemates.model.list;

import java.io.Serializable;
import java.util.ArrayList;

import mirror42.dev.cinemates.model.User;
import mirror42.dev.cinemates.model.tmdb.Movie;

public class MoviesList implements Serializable {
    private long id;
    private ArrayList<Movie> movies;
    private ListType listType;
    private User owner;

    /**
     * WL - watchlist
     * FV - favorites list
     * WD - watched list
     * CL - custom list
     */
    public enum ListType {WL, FV, WD, CL}

    public MoviesList(ListType listType) {
        this.listType = listType;
    }



    //---------------------------------------------- GETTERS/SETTERS

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public ArrayList<Movie> getMovies() {
        return movies;
    }

    public void setMovies(ArrayList<Movie> movies) {
        this.movies = movies;
    }

    public ListType getListType() {
        return listType;
    }

    public void setListType(ListType listType) {
        this.listType = listType;
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public boolean isEmpty() {
        return movies==null || movies.size()==0;
    }
}// end List class
