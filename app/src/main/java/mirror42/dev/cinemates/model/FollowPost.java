package mirror42.dev.cinemates.model;

import java.io.Serializable;

public class FollowPost extends Post implements Serializable {
    User followed;

    public FollowPost() {
        super(PostType.FW);
    }

    public User getFollowed() {
        return followed;
    }

    public void setFollowed(User followed) {
        this.followed = followed;
    }
}
