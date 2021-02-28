package mirror42.dev.cinemates.model;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

public class User implements Serializable, Parcelable {
    private String username;
    private String email;
    private String password;
    private String firstName;
    private String lastName;
    private String birthDate;
    private boolean isExternalUser = false;
    private String profilePicturePath;
    private String accessToken;
    private boolean promo;
    private boolean analytics;
    private int followersCount;
    private int followingCount;




    //----------------------------------------------- CONSTRUCTORS

    public User() {
        // empty
    }

    public User(String email, String profilePicturePath) {
        this.email = email;
        this.profilePicturePath = profilePicturePath;
    }


    public User(String username, String email, String profilePicturePath) {
        this.username = username;
        this.profilePicturePath = profilePicturePath;
        this.email = email;
    }

    public User(String username, String email, String firstName, String lastName, String birthDate, String profilePicturePath, String accessToken, boolean analytics) {
        this.username = username;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.birthDate = birthDate;
        this.profilePicturePath = profilePicturePath;
        this.accessToken = accessToken;
        this.analytics = analytics;
    }

    public User(String username,String password ,String email, String firstName, String lastName, String birthDate, String profilePicturePath, String accessToken, boolean analytics) {
        this.username = username;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.birthDate = birthDate;
        this.profilePicturePath = profilePicturePath;
        this.accessToken = accessToken;
        this.analytics = analytics;
        this.password = password;
    }

//    public User(String username, String email, String firstName, String lastName, Date birthDate, String profilePicturePath, String accessToken, boolean promo, boolean analytics) {
//        this.username = username;
//        this.email = email;
//        this.firstName = firstName;
//        this.lastName = lastName;
//        this.birthDate = birthDate;
//        this.profilePicturePath = profilePicturePath;
//        this.accessToken = accessToken;
//        this.promo = promo;
//        this.analytics = analytics;
//    }


    //----------------------------------------------- GETTERS/SETTERS


    public boolean getIsExternalUser() {
        return isExternalUser;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getProfilePicturePath() {
        return profilePicturePath;
    }

    public String getBirthDate() {
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

    public void setProfilePictureURL(String profilePicturePath) {
        this.profilePicturePath = profilePicturePath;
    }

    public void setBirthDate(String birthDate) {

        this.birthDate = birthDate;
    }

    public void setExternalUser(boolean externalUser) {
        isExternalUser = externalUser;
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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean getPromo() {
        return promo;
    }

    public void setPromo(boolean promo) {
        this.promo = promo;
    }

    public boolean isAnalytics() {
        return analytics;
    }

    public String getFullName() {
        return firstName + " " + lastName;
    }

    public int getFollowersCount() {
        return followersCount;
    }

    public void setFollowersCount(int followersCount) {
        this.followersCount = followersCount;
    }

    public int getFollowingCount() {
        return followingCount;
    }

    public void setFollowingCount(int followingCount) {
        this.followingCount = followingCount;
    }

    //----------------------------------------------- METHODS

    public static User parseUserFromJsonObject(JSONObject jsonObject) {
        if(jsonObject==null)
            return null;

        User user = null;

        try {
            String username = jsonObject.getString("Username");
            String password = jsonObject.getString("Password");
            String email = jsonObject.getString("Email");
            String firstName = jsonObject.getString("Name");
            String secondName = jsonObject.getString("LastName");
            String birthDate = jsonObject.getString("BirthDate");
            String profilePicturePath = jsonObject.getString("ProfileImage");
            String accessToken = jsonObject.getString("AccessToken");
            boolean analytics = jsonObject.getBoolean("Analytics");
            int followersCount = jsonObject.getInt("followers_count");
            int followingCount = jsonObject.getInt("following_count");
            user = new User(username,password, email, firstName, secondName, birthDate, profilePicturePath, accessToken, analytics);
            user.setFollowersCount(followersCount);
            user.setFollowingCount(followingCount);
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

    protected User(Parcel in) {
        username = in.readString();
        email = in.readString();
        password = in.readString();
        firstName = in.readString();
        lastName = in.readString();
        birthDate = in.readString();
        followersCount = in.readInt();
        followingCount = in.readInt();
        profilePicturePath = in.readString();
        accessToken = in.readString();
        promo = in.readByte() != 0;
        analytics = in.readByte() != 0;
    }

    public static final Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(username);
        dest.writeString(email);
        dest.writeString(password);
        dest.writeString(firstName);
        dest.writeString(lastName);
        dest.writeString(birthDate);
        dest.writeString(profilePicturePath);
        dest.writeString(accessToken);
        dest.writeInt(followersCount);
        dest.writeInt(followingCount);
        dest.writeByte((byte) (promo ? 1 : 0));
        dest.writeByte((byte) (analytics ? 1 : 0));
    }
}// end User class
