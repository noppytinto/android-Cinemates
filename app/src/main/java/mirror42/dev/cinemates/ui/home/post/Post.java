package mirror42.dev.cinemates.ui.home.post;

import mirror42.dev.cinemates.model.User;

public class Post {
    private User owner;
    private String publishDate;
    private int likes;
    private int comments;
    private String description;


    public User getOwner() {
        return owner;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
