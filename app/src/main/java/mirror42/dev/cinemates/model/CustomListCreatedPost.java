package mirror42.dev.cinemates.model;

import java.io.Serializable;

public class CustomListCreatedPost extends Post implements Serializable {
    private String name;
    private String listDescription;

    public CustomListCreatedPost() {
        super(PostType.CC);
    }

    public String getName() {
        return name;
    }

    public void setListName(String name) {
        this.name = name;
    }

    public String getListDescription() {
        return listDescription;
    }

    public void setListDescription(String listDescription) {
        this.listDescription = listDescription;
    }
}
