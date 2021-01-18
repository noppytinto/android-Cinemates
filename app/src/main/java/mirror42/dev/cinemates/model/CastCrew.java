package mirror42.dev.cinemates.model;

import java.sql.Date;
import java.util.ArrayList;

import mirror42.dev.cinemates.tmdbAPI.model.Movie;

public class CastCrew {
    private int tmdbID;
    private String name;
    private Date birthDate;
    private int age;
    private String department;
    private String profilePictureUrl;
    private ArrayList<Movie> knownFor;



    //----------------------------------------------------------------- CONSTRUCTORS





    //----------------------------------------------------------------- GETTERS/SETTERS

    public int getTmdbID() {
        return tmdbID;
    }

    public void setTmdbID(int tmdbID) {
        this.tmdbID = tmdbID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(Date birthDate) {
        this.birthDate = birthDate;
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


    //----------------------------------------------------------------- METHODS










}// end CastCrew class
