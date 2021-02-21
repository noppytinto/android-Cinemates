package mirror42.dev.cinemates.model.notification;

public class MovieRecommendedNotification extends Notification {
    private int movieId;

    public MovieRecommendedNotification() {
        super();
        notificationType = NotificationType.MR;
    }

    public int getMovieId() {
        return movieId;
    }

    public void setMovieId(int movieId) {
        this.movieId = movieId;
    }
}
