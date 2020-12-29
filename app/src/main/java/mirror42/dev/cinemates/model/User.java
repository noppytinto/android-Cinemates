package mirror42.dev.cinemates.model;

public class User {
    private String email;
    private String username;
    private String password;

    //----------------------------------------------------------- CONSTRUCTORS

    public User(String email, String username, String password) {
        this.email = email;
        this.username = username;
        this.password = password;
    }


    //----------------------------------------------------------- GETTERS/SETTERS

    public String getEmail() {
        return email;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }


    //----------------------------------------------------------- METHODS



}// end User class
