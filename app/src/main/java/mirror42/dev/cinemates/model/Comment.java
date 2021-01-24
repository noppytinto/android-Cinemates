package mirror42.dev.cinemates.model;

import java.io.Serializable;

public class Comment extends Reaction implements Serializable {
    private String text;
    private boolean isNewItem;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public boolean isNewItem() {
        return isNewItem;
    }

    public void setIsNewItem(boolean isNewItem) {
        this.isNewItem = isNewItem;
    }
}
