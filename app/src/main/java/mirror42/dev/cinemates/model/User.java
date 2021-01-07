package mirror42.dev.cinemates.model;

import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Date;

public class User {
    private String username;
    private String email;
    private String password;
    private String firstName;
    private String lastName;
    private Date birthDate;
    private String profilePicturePath;
    private String accessToken;
    private boolean promo;
    private boolean analytics;




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


    public User(String username, String email, String firstName, String lastName, Date birthDate, String profilePicturePath, String accessToken) {
        this.username = username;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.birthDate = birthDate;
        this.profilePicturePath = profilePicturePath;
        this.accessToken = accessToken;
    }

    public User(String username, String email, String firstName, String lastName, Date birthDate, String profilePicturePath, String accessToken, boolean analytics) {
        this.username = username;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.birthDate = birthDate;
        this.profilePicturePath = profilePicturePath;
        this.accessToken = accessToken;
        this.analytics = analytics;
    }

    public User(String username, String email, String firstName, String lastName, Date birthDate, String profilePicturePath, String accessToken, boolean promo, boolean analytics) {
        this.username = username;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.birthDate = birthDate;
        this.profilePicturePath = profilePicturePath;
        this.accessToken = accessToken;
        this.promo = promo;
        this.analytics = analytics;
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

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public boolean getAnalytics() {
        return analytics;
    }

    public void setAnalytics(boolean analytics) {
        this.analytics = analytics;
    }

    //----------------------------------------------- METHODS

    public static User parseUserFromJsonObject(JSONObject jsonObject) {
        if(jsonObject==null)
            return null;

        User user = null;

        try {
            String username = jsonObject.getString("Username");
            String email = jsonObject.getString("Email");
            String firstName = jsonObject.getString("Name");
            String secondName = jsonObject.getString("LastName");
            Date birthDate = Date.valueOf(jsonObject.getString("BirthDate"));
            String profilePicturePath = jsonObject.getString("ProfileImage");
            String accessToken = jsonObject.getString("AccessToken");
            boolean analytics = jsonObject.getBoolean("Analytics");

            user = new User(username, email, firstName, secondName, birthDate, profilePicturePath, accessToken, analytics);
        } catch (JSONException e) {
            e.printStackTrace();
        }


        return user;
    }

    public static User parseUserFromJsonString(String jsonString) {

        if(jsonString==null || jsonString.isEmpty())
            return null;




        return null;
    }









}// end User class
