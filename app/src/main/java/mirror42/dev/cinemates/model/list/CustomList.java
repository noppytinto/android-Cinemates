package mirror42.dev.cinemates.model.list;

import java.io.Serializable;

public class CustomList extends MoviesList implements Serializable {
    private String name;
    private String description;
    private Boolean isPrivate;

    public CustomList() {
        super(ListType.CL);
    }



    public CustomList(CustomList newList) {
        // only for list details editing
        this();
        name = newList.getName();
        description = newList.getDescription();
        isPrivate = newList.isPrivate();
    }


    //------------------------------------------------- GETTERS/SETTERS

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean isPrivate() {
        return isPrivate;
    }

    public void setPrivate(Boolean aPrivate) {
        isPrivate = aPrivate;
    }
}// end CustomList class
