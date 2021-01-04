package mirror42.dev.cinemates.model;

import java.sql.Date;

public class User {
    private String firstName;
    private String lastName;
    private String profilePicturePath;
    private Date birthDate;
    private String username;
    private String email;
    private String password;



    //----------------------------------------------- CONSTRUCTORS

    public User() {
        // empty
    }

    public User(String username, String profilePicturePath) {
        this.username = username;
        this.profilePicturePath = profilePicturePath;
    }

    public User(String username, String email, String profilePicturePath) {
        this(username, profilePicturePath);
        this.email = email;
    }

    //----------------------------------------------- GETTERS/SETTERS

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getProfilePicturePath() {
        return profilePicturePath;
    }

    public Date getBirthDate() {
        return birthDate;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setProfilePicturePath(String profilePicturePath) {
        this.profilePicturePath = profilePicturePath;
    }

    public void setBirthDate(Date birthDate) {
        this.birthDate = birthDate;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    //----------------------------------------------- METHODS







}// end User class
