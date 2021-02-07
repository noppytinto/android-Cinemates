package mirror42.dev.cinemates.model.list;

import java.io.Serializable;

public class FavoritesList extends MoviesList implements Serializable {
    public FavoritesList() {
        super(ListType.FV);
    }
}
