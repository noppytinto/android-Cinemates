package mirror42.dev.cinemates.model.notification;

import mirror42.dev.cinemates.model.list.CustomList;

public class ListRecommendedNotification extends Notification {
    private CustomList recommendedList;

    public ListRecommendedNotification() {
        super();
        recommendedList = new CustomList();
        notificationType = NotificationType.CR;

    }

    public CustomList getCustomList() {
        return recommendedList;
    }

    public void setCustomList(CustomList list) {
        this.recommendedList = list;
    }
}
