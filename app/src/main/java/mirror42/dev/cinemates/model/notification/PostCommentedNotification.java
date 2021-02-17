package mirror42.dev.cinemates.model.notification;

import mirror42.dev.cinemates.model.Post;

public class PostCommentedNotification extends Notification{
    private Post post;

    public PostCommentedNotification() {
        super();
        notificationType = NotificationType.PC;

    }

    public Post getPost() {
        return post;
    }

    public void setPost(Post post) {
        this.post = post;
    }
}
