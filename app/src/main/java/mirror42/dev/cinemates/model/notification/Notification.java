package mirror42.dev.cinemates.model.notification;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import mirror42.dev.cinemates.model.User;

@Entity
public class Notification implements Parcelable, Comparable<Notification>{
    @PrimaryKey private long id;
    private boolean isNew;
    @Ignore private long postId;
    @Ignore private User owner;
    @Ignore private long dateInMillis;
    @Ignore protected NotificationType notificationType;
    @Ignore private User sender;

    public enum NotificationType {
        FR, PC, PL, CR, CS, NONE
    }



    //------------------------------------------------------------------------------- CONSTRUCTORS

    public Notification() {
        isNew = true;
    }




    //------------------------------------------------------------------------------- GETTERS/SETTERS

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public User getSender() {
        return sender;
    }

    public void setSender(User sender) {
        this.sender = sender;
    }

    public long getPostID() {
        return postId;
    }

    public void setPostId(long postId) {
        this.postId = postId;
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public long getDateInMillis() {
        return dateInMillis;
    }

    public void setDateInMillis(long dateInMillis) {
        this.dateInMillis = dateInMillis;
    }

    public NotificationType getNotificationType() {
        return notificationType;
    }

    public void setNotificationType(NotificationType notificationType) {
        this.notificationType = notificationType;
    }

    public boolean isNew() {
        return isNew;
    }

    public void setIsNew(boolean isNew) {
        this.isNew = isNew;
    }



    //------------------------------------------------------------------------------- METHODS

    @Override
    public int compareTo(Notification another) {
        if(this.dateInMillis > another.dateInMillis)
            return -1;
        else if(this.dateInMillis < another.dateInMillis)
            return 1;

        return 0;

//        return Long.compare(this.dateInMillis, o.dateInMillis);
    }

    public static final Creator<Notification> CREATOR = new Creator<Notification>() {
        @Override
        public Notification createFromParcel(Parcel in) {
            return new Notification(in);
        }

        @Override
        public Notification[] newArray(int size) {
            return new Notification[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    protected Notification(Parcel in) {
        postId = in.readLong();
        dateInMillis = in.readLong();
        owner = in.readParcelable(User.class.getClassLoader());
        notificationType = NotificationType.values()[in.readInt()];
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(postId);
        dest.writeLong(dateInMillis);
        dest.writeParcelable(owner, flags);
        dest.writeInt(notificationType.ordinal());
    }


}// end Notification class

