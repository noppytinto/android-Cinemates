package mirror42.dev.cinemates.model.list;

import java.io.Serializable;

public class CustomList extends MoviesList implements Serializable, Comparable<CustomList> {
    private String name;
    private String description;
    private Boolean isPrivate;
    private long dateInMillis;

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

    public void setIsPrivate(Boolean value) {
        isPrivate = value;
    }

    public long getDateInMillis() {
        return dateInMillis;
    }

    public void setDateInMillis(long dateInMillis) {
        this.dateInMillis = dateInMillis;
    }

    @Override
    public int compareTo(CustomList another) {
        if(this.dateInMillis > another.dateInMillis)
            return -1;
        else if(this.dateInMillis < another.dateInMillis)
            return 1;

        return 0;

//        return Long.compare(this.dateInMillis, o.dateInMillis);
    }
}// end CustomList class
