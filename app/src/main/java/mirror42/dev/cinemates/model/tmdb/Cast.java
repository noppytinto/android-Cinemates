package mirror42.dev.cinemates.model.tmdb;


import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;

public class Cast implements Parcelable, Serializable {
    private final static long serialVersionUID = 4553482479516231157L;
    @SerializedName("id")
    @Expose
    private int tmdbID;
    @SerializedName("name")
    @Expose
    private String fullName;
    @SerializedName("birthday")
    @Expose
    private String birthday;
    @SerializedName("known_for_department")
    @Expose
    private String department;
    @SerializedName("profile_path")
    @Expose
    private String profilePictureUrl;
    @SerializedName("known_for")
    @Expose
    private ArrayList<Movie> knownFor;
    @SerializedName("biography")
    @Expose
    private String biography;
    @SerializedName("place_of_birth")
    @Expose
    private String placeOfBirth;
    private int age;




    //----------------------------------------------------------------- CONSTRUCTORS

    /**
     * No args constructor for use in serialization
     */
    public Cast() {
    }






    //----------------------------------------------------------------- GETTERS/SETTERS

    public int getTmdbID() {
        return tmdbID;
    }

    public void setTmdbID(int tmdbID) {
        this.tmdbID = tmdbID;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getBirthDate() {
        return birthday;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getProfilePictureUrl() {
        return profilePictureUrl;
    }

    public void setProfilePictureUrl(String profilePictureUrl) {
        this.profilePictureUrl = profilePictureUrl;
    }

    public ArrayList<Movie> getKnownFor() {
        return knownFor;
    }

    public void setKnownFor(ArrayList<Movie> knownFor) {
        this.knownFor = knownFor;
    }

    public String getBiography() {
        return biography;
    }

    public void setBiography(String biography) {
        this.biography = biography;
    }

    public String getPlaceOfBirth() {
        return placeOfBirth;
    }

    public void setPlaceOfBirth(String placeOfBirth) {
        this.placeOfBirth = placeOfBirth;
    }



    //----------------------------------------------------------------- PARCELABLE METHODS
    public final static Parcelable.Creator<Cast> CREATOR = new Parcelable.Creator<Cast>() {
        public Cast createFromParcel(Parcel in) {
            return new Cast(in);
        }
        public Cast[] newArray(int size) {
            return (new Cast[size]);
        }
    };


    protected Cast(Parcel in) {
        this.tmdbID = ((Integer) in.readValue((Integer.class.getClassLoader())));
        in.readList(this.knownFor, (Movie.class.getClassLoader()));
        this.department = ((String) in.readValue((String.class.getClassLoader())));
        this.fullName = ((String) in.readValue((String.class.getClassLoader())));
        this.profilePictureUrl = ((String) in.readValue((String.class.getClassLoader())));
        this.biography = ((String) in.readValue((String.class.getClassLoader())));
        this.birthday = ((String) in.readValue((String.class.getClassLoader())));
        this.placeOfBirth = ((String) in.readValue((String.class.getClassLoader())));
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(tmdbID);
        dest.writeList(knownFor);
        dest.writeValue(fullName);
        dest.writeValue(profilePictureUrl);
        dest.writeValue(biography);
        dest.writeValue(placeOfBirth);
        dest.writeValue(birthday);
    }

    public int describeContents() {
        return 0;
    }



}// end Actor class
