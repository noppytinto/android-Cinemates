package mirror42.dev.cinemates.model;

public class Reaction {
    private long id;
    protected User owner;
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

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Reaction other = (Reaction) obj;
        if ( id != other.getId())
            return false;
        Reaction other2 = (Reaction) obj;
        if ( ! owner.getUsername().equals(other2.owner.getUsername()))
            return false;
        return true;
    }


}
