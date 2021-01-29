package mirror42.dev.cinemates.ui.notification.model;

import android.os.Parcel;
import android.os.Parcelable;

import mirror42.dev.cinemates.model.User;

public class Notification implements Parcelable, Comparable<Notification>{
    private long postId;
    private User owner;
    private long dateInMillis;
    protected NotificationType notificationType;
    private User sender;

    @Override
    public int compareTo(Notification another) {
        if(this.dateInMillis > another.dateInMillis)
            return -1;
        else if(this.dateInMillis < another.dateInMillis)
            return 1;

        return 0;

//        return Long.compare(this.dateInMillis, o.dateInMillis);
    }


    public enum NotificationType {
        FOLLOW_REQUEST, POST_COMMENTED, POST_LIKED, NONE
    }



    //------------------------------------------------------------------------------- CONSTRUCTORS

    public Notification() {

    }




    //------------------------------------------------------------------------------- GETTERS/SETTERS

    public User getSender() {
        return sender;
    }

    public void setSender(User sender) {
        this.sender = sender;
    }


    public long getPostId() {
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



    //------------------------------------------------------------------------------- METHODS

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

