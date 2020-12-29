package mirror42.dev.cinemates.tmdbAPI;

public class Person {
    private int tmdbID; // 6193
    private String fullName; // Leonardo DiCaprio
    private String character; // Jack Dawson
    private String department; //Acting, director, screenwriter, ...
    private String profileImageURL;


    //--------------------------------------------- CONSTRUCTORS
    public Person(int tmdbID, String fullName, String character) {
        this.tmdbID = tmdbID;
        this.fullName = fullName;
        this.character = character;
    }

    public Person(int tmdbID, String fullName, String character, String department, String profileImageURL) {
        this(tmdbID, fullName, character);
        this.department = department;
        this.profileImageURL = profileImageURL;
    }



    //--------------------------------------------- GETTER/SETTERS

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

    public String getCharacter() {
        return character;
    }

    public void setCharacter(String character) {
        this.character = character;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getProfileImageURL() {
        return profileImageURL;
    }

    public void setProfileImageURL(String profileImageURL) {
        this.profileImageURL = profileImageURL;
    }

}// end Person class
