package mirror42.dev.cinemates.model;

import java.io.Serializable;

public class Comment extends Reaction implements Serializable {
    private String text;
    private boolean isNewItem;
    private boolean isMine;

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

    public boolean isMine() {
        return isMine;
    }

    public void setIsMine(boolean mine) {
        isMine = mine;
    }
}
