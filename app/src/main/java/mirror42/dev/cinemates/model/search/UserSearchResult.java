package mirror42.dev.cinemates.model.search;

import java.util.List;

public class UserSearchResult extends SearchResult{
    private String profilePictureUrl;
    private String firstName;
    private String lastName;
    private String username;


    public UserSearchResult() {
        super(SearchType.CAST);
    }

    public String getProfilePictureUrl() {
        return profilePictureUrl;
    }

    public void setProfilePictureUrl(String profilePictureUrl) {
        this.profilePictureUrl = profilePictureUrl;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }


    @Override
    public Object buildResult(Object arg) {
        return null;
    }

    @Override
    public List buildResultList(List args) {
        return null;
    }
}
