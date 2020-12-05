package mirror42.dev.cinemates.tmdbAPI;

public class Image implements TheMovieDatabase{
    String url;

    public Image(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    // null posters
    // 4240

    // null backdrops
    // 4240

}
