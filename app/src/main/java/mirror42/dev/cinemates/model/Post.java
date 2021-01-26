package mirror42.dev.cinemates.model;


import java.util.ArrayList;

public class Post {
    private long postId;
    private User owner;
    private long publishDateMillis;
    private ArrayList<Like> likes;
    private ArrayList<Comment> comments;
    private String description;
    private PostType postType;
    private boolean isLikedByMe;
    private boolean isCommentedByMe;


    public enum PostType {
        ADD_TO_WATCHLIST,
        ADD_TO_WATCHED_LIST,
        ADD_TO_FAVORITES_LIST,
        ADD_TO_CUSTOM_LIST,
        CREATE_LIST,
        NEW_FRIEND,
        REVIEW,
        NONE
    }




    //---------------------------------------------------------------------- CONSTRUCTORS

    public Post() {
    }


    //---------------------------------------------------------------------- GETTERS/SETTERS
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

    public long getPostId() {
        return postId;
    }

    public void setPostId(long postId) {
        this.postId = postId;
    }




    //---------------------------------------------------------------------- METHODS


    public boolean isLikedByMe() {
        return isLikedByMe;
    }

    public void setLikedByMe(String loggedUser) {
        boolean res = false;
        if(likes!=null && likes.size()>0) {
            for(int i=0; i<likes.size(); i++) {
                String reactionOwnerUsername = likes.get(i).getOwner().getUsername();
                if(loggedUser.equals(reactionOwnerUsername)) {
                    res = true;
                    break;
                }
            }
        }

        isLikedByMe = res;
    }

    public boolean isCommentedByMe() {
        return isCommentedByMe;
    }

    public void setCommentedByMe() {
        boolean res = false;
        if(comments!=null && comments.size()>0) {
            String postOwnerUsername = this.getOwner().getUsername();
            for(int i=0; i<comments.size(); i++) {
                String reactionOwnerUsername = comments.get(i).getOwner().getUsername();
                if(postOwnerUsername.equals(reactionOwnerUsername)) {
                    comments.get(i).setIsMine(true);
                    res = true;
                }
            }
        }

        isCommentedByMe = res;
    }

    public int getCommentsCount() {
        int count = 0;
        if(comments!=null && comments.size()>0) {
            count = comments.size();
        }

        return count;
    }

    public int getLikesCount() {
        int count = 0;
        if(likes!=null && likes.size()>0) {
            count = likes.size();
        }

        return count;
    }

    public ArrayList<User> getLikesOwnersList() {
        ArrayList<User> list = new ArrayList<>();

        if(likes!=null) {
            for(Like l: likes)
                list.add(l.getOwner());
        }

        return list;
    }



}// end Post class
