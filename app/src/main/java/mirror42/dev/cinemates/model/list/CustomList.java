package mirror42.dev.cinemates.model.list;

import java.io.Serializable;

public class CustomList extends MoviesList implements Serializable {
    private String name;
    private String description;

    public CustomList() {
        super(ListType.CM);
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

}// end CustomList class
