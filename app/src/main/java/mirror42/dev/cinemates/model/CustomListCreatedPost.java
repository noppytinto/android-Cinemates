package mirror42.dev.cinemates.model;

public class CustomListCreatedPost extends Post {
    private String name;
    private String listDescription;

    public CustomListCreatedPost() {
        super(PostType.CC);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getListDescription() {
        return listDescription;
    }

    public void setListDescription(String listDescription) {
        this.listDescription = listDescription;
    }
}
