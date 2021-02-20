package mirror42.dev.cinemates.model.tmdb;

import java.io.Serializable;

public class Genre implements Serializable {
    private int id;
    private String name;

    public Genre() {};

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
