package mirror42.dev.cinemates.model.notification;

import mirror42.dev.cinemates.model.Post;

public class PostLikedNotification extends Notification {
    private Post post;

    public PostLikedNotification() {
        super();
        notificationType = Notification.NotificationType.PL;
    }

    public Post getPost() {
        return post;
    }

    public void setPost(Post post) {
        this.post = post;
    }
}
