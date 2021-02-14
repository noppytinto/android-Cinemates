package mirror42.dev.cinemates.model;

import java.io.Serializable;

public class CustomListPost extends Post implements Serializable {
    public CustomListPost() {
        super(PostType.CL);
    }
}
