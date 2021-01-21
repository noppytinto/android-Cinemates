package mirror42.dev.cinemates.model;


import java.util.ArrayList;

public class Post {
    private User owner;
    private long publishDateMillis;
    private ArrayList<Like> likes;
    private ArrayList<Comment> comments;
    private String description;
    private PostType postType;

    public enum PostType {
        ADD_TO_WATCHLIST,
        ADD_TO_WATCHED_LIST,
        ADD_TO_FAVORITES_LIST,
        ADD_TO_CUSTOM_LIST,
        CREATE_LIST,
        NEW_FRIEND,
        REVIEW
    }




    //---------------------------------------------------------------------- CONSTRUCTORS

    public User getOwner() {
        return owner;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public long getPublishDateMillis() {
        return publishDateMillis;
    }

    public void setPublishDateMillis(long publishDateMillis) {
        this.publishDateMillis = publishDateMillis;
    }

    public ArrayList<Like> getLikes() {
        return likes;
    }

    public void setLikes(ArrayList<Like> likes) {
        this.likes = likes;
    }

    public ArrayList<Comment> getComments() {
        return comments;
    }

    public void setComments(ArrayList<Comment> comments) {
        this.comments = comments;
    }

    public PostType getPostType() {
        return postType;
    }

    public void setPostType(PostType postType) {
        this.postType = postType;
    }

//---------------------------------------------------------------------- GETTERS/SETTERS




    //---------------------------------------------------------------------- METHODS




}// end Post class
