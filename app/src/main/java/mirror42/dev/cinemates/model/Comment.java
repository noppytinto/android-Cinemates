package mirror42.dev.cinemates.model;

public class Comment extends Reaction{
    private String text;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
