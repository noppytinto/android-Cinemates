package mirror42.dev.cinemates.model.tmdb;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;

public class Movie implements Parcelable, Serializable {
    private final static long serialVersionUID = 2241477881506269391L;
    @SerializedName("id")
    @Expose
    private int tmdbID;
    @SerializedName("title")
    @Expose
    private String title;
    @SerializedName("overview")
    @Expose
    private String overview;
    @SerializedName("poster_path")
    @Expose
    private String posterURL;
    @SerializedName("backdrop_path")
    @Expose
    private String backdropURL;
    @SerializedName("runtime")
    @Expose
    private int duration;
    @SerializedName("status")
    @Expose
    private String releaseStatus;
    @SerializedName("release_date")
    @Expose
    private String releaseDate;
    @SerializedName("genres")
    @Expose
    private ArrayList<Genre> genres;
    private ArrayList<Cast> castAndCrew;
    private boolean isSelected;




    //------------------------------------------------------------------- CONSTRUCTORS

    public Movie() {

    }

    public Movie(int tmdbID) {
        this.tmdbID = tmdbID;
    }


    public Movie(int tmdbID, String title, String posterURL, String overview) {
        this.tmdbID = tmdbID;
        this.title = title;
        this.posterURL = posterURL;
        this.overview = overview;
    }

    public Movie(int tmdbID,
                 String title,
                 String overview,
                 String posterURL,
                 String backdropURL,
                 int duration,
                 String releaseStatus,
                 String releaseDate,
                 ArrayList<Genre> genres,
                 ArrayList<Cast> castAndCrew) {
        this(tmdbID, title, posterURL, overview);
        this.backdropURL = backdropURL;
        this.duration = duration;
        this.releaseStatus = releaseStatus;
        this.releaseDate = releaseDate;
        this.genres = genres;
        this.castAndCrew = castAndCrew;
    }

//    public Movie(int id, String posterURL) {
//        this.tmdbID = id;
//        this.posterURL = posterURL;
//
//
//    }

    public Movie(int id, String title, String posterURL) {
        this.tmdbID = id;
        this.title = title;
        this.posterURL = posterURL;
    }

    //------------------------------------------------------- SETTERS/GETTERS


    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPosterURL() {
        return posterURL;
    }

    public void setPosterURL(String posterURL) {
        this.posterURL = posterURL;
    }

    public String getOverview() {
        if(overview==null || overview.isEmpty())
            return "(trama non disponibile in italiano)";

        return overview;
    }

    public void setOverview(String overview) {
        this.overview = overview;
    }

    public int getTmdbID() {
        return tmdbID;
    }

    public void setTmdbID(int tmdbID) {
        this.tmdbID = tmdbID;
    }

    public String getBackdropURL() {
        return backdropURL;
    }

    public void setBackdropURL(String backdropURL) {
        this.backdropURL = backdropURL;
    }

    public String getReleaseStatus() {
        return releaseStatus;
    }

    public void setReleaseStatus(String releaseStatus) {
        this.releaseStatus = releaseStatus;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(String releaseDate) {
        this.releaseDate = releaseDate;
    }

    public ArrayList<Genre> getGenres() {
        return genres;
    }

    public void setGenres(ArrayList<Genre> genres) {
        this.genres = genres;
    }

    public ArrayList<Cast> getCastAndCrew() {
        return castAndCrew;
    }

    public void setCastAndCrew(ArrayList<Cast> castAndCrew) {
        this.castAndCrew = castAndCrew;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    //------------------------------------------------------------------- METHODS

    @Override
    public String toString() {
        return "Movie{" +
                "title='" + title + '\'' +
                '}';
    }


    //------------------------------------------------------------------- PARCELLABLE METHODS

    public Movie(Parcel source) {
        this.tmdbID = source.readInt();
        this.title = source.readString();
        this.posterURL = source.readString();
        this.overview = source.readString();
        this.backdropURL = source.readString();
        this.duration = source.readInt();
        this.releaseStatus = source.readString();
        this.releaseDate = source.readString();
        this.genres = new ArrayList<Genre>();
        this.castAndCrew = new ArrayList<Cast>();
        source.readList(genres, Genre.class.getClassLoader());
        source.readList(castAndCrew, Person.class.getClassLoader());
    }
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(tmdbID);
        dest.writeString(title);
        dest.writeString(overview);
        dest.writeString(posterURL);
        dest.writeString(backdropURL);
        dest.writeInt(duration);
        dest.writeString(releaseStatus);
        dest.writeString(releaseDate);
        dest.writeList(genres);
        dest.writeList(castAndCrew);
    }

    // Method to recreate a Question from a Parcel
    public static Creator<Movie> CREATOR = new Creator<Movie>() {

        @Override
        public Movie createFromParcel(Parcel source) {
            return new Movie(source);
        }

        @Override
        public Movie[] newArray(int size) {
            return new Movie[size];
        }

    };


    @Override
    public int describeContents() {
        return 0;
    }

}// end Movie class
