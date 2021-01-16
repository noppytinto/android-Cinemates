package mirror42.dev.cinemates.model;



public class Post {
    private User owner;
    private long publishDateMillis;
    private int likes;
    private int comments;
    private String description;
    private String postType;

    public enum PostType {
        ADD_TO_LIST_WATCHLIST,
        ADD_TO_LIST_WATCHED,
        ADD_TO_LIST_FAVORITE,
        ADD_TO_LIST_CUSTOM,
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




    //---------------------------------------------------------------------- GETTERS/SETTERS




    //---------------------------------------------------------------------- METHODS




}// end Post class
