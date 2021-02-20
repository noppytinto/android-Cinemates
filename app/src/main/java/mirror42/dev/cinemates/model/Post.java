package mirror42.dev.cinemates.model;


import java.util.ArrayList;

public class Post implements Comparable<Post> {
    private long postId;
    private User owner;
    private long publishDateMillis;
    private ArrayList<Like> likes;
    private ArrayList<Comment> comments;
    private String description;
    protected PostType postType;
    private boolean isLikedByMe;
    private boolean isCommentedByMe;
    private int commentsCount;
    private int likesCount;

    @Override
    public int compareTo(Post another) {
        if(this.publishDateMillis > another.publishDateMillis)
            return -1;
        else if(this.publishDateMillis < another.publishDateMillis)
            return 1;

        return 0;
    }


    public enum PostType {
        WL,
        WD,
        FV,
        CL,
        CC,
        FW,
        NONE
    }




    //---------------------------------------------------------------------- CONSTRUCTORS

    public Post(PostType postType) {
        this.postType = postType;
    }


    //---------------------------------------------------------------------- GETTERS/SETTERS


    public void setCommentsCount(int commentsCount) {
        this.commentsCount = commentsCount;
    }

    public void setLikesCount(int likesCount) {
        this.likesCount = likesCount;
    }

    public int getCommentsCount() {
        return commentsCount;
    }

    public int getLikesCount() {
        return likesCount;
    }

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

    public void setIsLikedByMe(boolean value) {
        this.isLikedByMe = value;
    }

    public boolean isCommentedByMe() {
        return isCommentedByMe;
    }

    public void setIsCommentedByMe(String loggedUsername) {
        boolean res = false;
        if(comments!=null && comments.size()>0) {
            for(int i=0; i<comments.size(); i++) {
                String reactionOwnerUsername = comments.get(i).getOwner().getUsername();
                if(loggedUsername.equals(reactionOwnerUsername)) {
                    comments.get(i).setIsMine(true);
                    res = true;
                }
            }
        }

        isCommentedByMe = res;
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
