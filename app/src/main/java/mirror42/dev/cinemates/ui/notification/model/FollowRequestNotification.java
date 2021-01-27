package mirror42.dev.cinemates.ui.notification.model;

import mirror42.dev.cinemates.model.User;

public class FollowRequestNotification extends Notification {
    private User sender;

    //------------------------------------------------------------------------------- CONSTRUCTORS

    public FollowRequestNotification() {
        notificationType = NotificationType.FOLLOW_REQUEST;
    }


    //------------------------------------------------------------------------------- GETTERS/SETTERS

    public User getSender() {
        return sender;
    }

    public void setSender(User sender) {
        this.sender = sender;
    }


    //------------------------------------------------------------------------------- METHODS


}// end FriendRequestNotification class
