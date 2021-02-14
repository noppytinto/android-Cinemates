package mirror42.dev.cinemates.model;

import java.io.Serializable;

public class FollowPost extends Post implements Serializable {
    public FollowPost() {
        super(PostType.FW);
    }
}
