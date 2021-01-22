package mirror42.dev.cinemates.model;

public class Reaction {
    private User owner;
    private long publishDateMillis;

    public Reaction() {

    }


    public Reaction(User owner, long publishDateMillis) {
        this.owner = owner;
        this.publishDateMillis = publishDateMillis;
    }


    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public long getPublishDateMillis() {
        return publishDateMillis;
    }

    public void setPublishDateMillis(long publishDateMillis) {
        this.publishDateMillis = publishDateMillis;
    }
}
