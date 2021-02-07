package mirror42.dev.cinemates.model.list;

import java.io.Serializable;

public class WatchedList extends MoviesList implements Serializable {
    public WatchedList() {
        super(ListType.WD);
    }
}
