package mirror42.dev.cinemates.model.list;

import java.io.Serializable;

public class Watchlist extends MoviesList implements Serializable {
    public Watchlist() {
        super(ListType.WL);
    }
}
