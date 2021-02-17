package mirror42.dev.cinemates.model.notification;

public class SubscribedToListNotification extends Notification {
    private String listName;

    public SubscribedToListNotification() {
        super();
        notificationType = NotificationType.CS;

    }

    public String getListName() {
        return listName;
    }

    public void setListName(String listName) {
        this.listName = listName;
    }
}
