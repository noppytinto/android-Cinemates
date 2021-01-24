package mirror42.dev.cinemates.model;

public class Like extends Reaction {

    public Like() {
        super();

    }


    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Like other = (Like) obj;
        if ( ! owner.getUsername().equals(other.owner.getUsername()))
            return false;
        return true;
    }

}
