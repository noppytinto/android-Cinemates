package mirror42.dev.cinemates.model;

public class WatchlistPost extends Post {
    private String listOwnerEmail;
    private String thumbnail_1_url;
    private String thumbnail_2_url;
    private String thumbnail_3_url;


    public String getThumbnail_1_url() {
        return thumbnail_1_url;
    }

    public void setThumbnail_1_url(String thumbnail_1_url) {
        this.thumbnail_1_url = thumbnail_1_url;
    }

    public String getThumbnail_2_url() {
        return thumbnail_2_url;
    }

    public void setThumbnail_2_url(String thumbnail_2_url) {
        this.thumbnail_2_url = thumbnail_2_url;
    }

    public String getThumbnail_3_url() {
        return thumbnail_3_url;
    }

    public void setThumbnail_3_url(String thumbnail_3_url) {
        this.thumbnail_3_url = thumbnail_3_url;
    }
}
