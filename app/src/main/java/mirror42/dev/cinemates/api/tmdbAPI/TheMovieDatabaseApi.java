package mirror42.dev.cinemates.api.tmdbAPI;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.json.JSONObject;

import java.util.ArrayList;

import mirror42.dev.cinemates.model.tmdb.Cast;
import mirror42.dev.cinemates.model.tmdb.Movie;
import mirror42.dev.cinemates.utilities.HttpUtilities;
import mirror42.dev.cinemates.utilities.OkHttpSingleton;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;


/**
 * REQUIREMENTS:
 * 1. this class works together with the JsonUtilities class
 * 2. before use this class
 *    do a method call of loadImagesConfigurationData() in the Driver
 *    only the first time.
 *
 *    f.e.
 *    TheMovieDatabase tmdb = new TheMovieDatabase();
 * 	  tmdb.loadImagesConfigurationData();
 *
 * HOW IT WORKS:
 *  This class only uses movies' ID, to get movies/cast data.
 *
 *  You can use trySearch(String) and trySearch(String, int) methods
 *  if you want try this class on console,
 *  they will help you to see which film the id correspond to.
 *  Then you can use that id to try all getter methods
 *
 *
 * HOW TO USE IMAGES:
 *
 *  methods for images will return the remote image URL
 *  in String format
 *  then you can use it:
 *  - Java desktop:
 *    use this method
 *
 *    private Image getImageFromURL(String url) {
 *         Image result = null;
 *
 *         try {
 *             URL u = new URL(url);
 *             result = ImageIO.read(u);
 *         } catch (IOException ex) {
 *             System.out.println(ex.getMessage());
 *         }
 *
 *         return result;
 *     }
 *
 * - Android:
 *  using lgide libraries
 *    - Glide libraries (recommended)
 *	    - add glide dependecy in build.gradle (module)
 *        // glide dependency
 *        implementation 'com.github.bumptech.glide:glide:4.11.0'
 *         annotationProcessor 'com.github.bumptech.glide:compiler:4.11.0'
 *
 *     - then in async task use method
 *             Glide.with(imageView_poster)  //2
 *                     .load(posterURL) //3
 *                     .fitCenter() //4
 *                     .into(imageView_poster); //8
 *     src:
 *     https://www.raywenderlich.com/2945946-glide-tutorial-for-android-getting-started
 *
 *
 *
 */


public class TheMovieDatabaseApi {
    // api configuration data
    private final String BASE_URL = "https://api.themoviedb.org/";
    private static final String TMDB_API_KEY = "632f90b0d342606c53a9ffd5fc5ed32e";
    private JSONObject apiConfigurationObject;
    private static ArrayList<String> posterSizes;
    private static ArrayList<String> backgroundImageSizes;
    private static ArrayList<String> profileImageSizes;
    private static String defaultAdultContentFilter = "false";
    private static String baseImageUrl = "https://image.tmdb.org/t/p/";
    public final String SMALL_BACKGROUND_SIZE = "w300";
    public final String MEDIUM_BACKGROUND_SIZE = "w780";
    public final String LARGE_BACKGROUND_SIZE = "w1280";
    public final String SMALL_POSTER_SIZE = "w185";
    public final String MEDIUM_POSTER_SIZE = "w342";
    public final String LARGE_POSTER_SIZE = "w780";
    public final String SMALL_PROFILE_SIZE = "w45";
    public final String MEDIUM_PROFILE_SIZE = "w185";
    public final String BIG_PROFILE_SIZE = "h632";

    // TMDb keywords
    public final String RESULTS = "results";
    public final String TOTAL_PAGES = "total_pages";
    public final String PAGE = "page";
    public final String TOTAL_RESULTS = "total_results";
    // time windows
    public final String DAY = "day";
    public final String WEEK = "week";
    // ISO 639-1 language codes
    private static String defaultLanguage;
    public final String ITALIAN_LANGUAGE = "it";
    public final String ENGISH_LANGUAGE = "en-US";
    // media types
    public final String ALL = "all";
    public final String MOVIE = "movie";
    public final String TV = "tv";
    public final String PERSON = "person";

    //
    private String defaultPosterSize;
    private String defaultBackdropSize;
    private String defaultPersonImageSize;

    //
    private static TheMovieDatabaseApi singletonInstance;


    //--------------------------------------------------------------------------- CONSTRUCTORS
    private TheMovieDatabaseApi() {
        defaultLanguage = ITALIAN_LANGUAGE;
        defaultPosterSize = SMALL_POSTER_SIZE;
        defaultBackdropSize = MEDIUM_BACKGROUND_SIZE;
        defaultPersonImageSize = MEDIUM_PROFILE_SIZE;
    }

    public static TheMovieDatabaseApi getInstance() {
        if(singletonInstance==null)
            return new TheMovieDatabaseApi();

        return singletonInstance;
    }



    //--------------------------------------------------------------------------- GETTERS/SETTERS

    public String getBaseURL() {
        return BASE_URL;
    }


    //--------------------------------------------------------------------------- PUBLIC METHODS

    public String buildPosterUrl(String imagePath) {
        if(imagePath==null || imagePath.isEmpty())
            return null;

        return baseImageUrl + defaultPosterSize + imagePath;
    }

    public String buildBackdropUrl(String imagePath) {
        if(imagePath==null || imagePath.isEmpty())
            return null;

        return baseImageUrl + defaultBackdropSize + imagePath;
    }

    public String buildPersonImageUrl(String imagePath) {
        if(imagePath==null || imagePath.isEmpty())
            return null;

        return baseImageUrl + defaultPersonImageSize + imagePath;
    }



    //--------------------------------- retrofit versions
    private final static String TMDB_API_VERSION = "3";
    private final static String DEFAULT_LANGUAGE = "it";
    private final static String INCLUDE_ADULT = "false";
    private final static String SEARCH_ACTORS_URL = TMDB_API_VERSION +  "/search/person?" +
                                                    "api_key=" + TMDB_API_KEY +
                                                    "&language=" + DEFAULT_LANGUAGE +
                                                    "&include_adult=" + INCLUDE_ADULT;
    private final static String SEARCH_MOVIES_URL = TMDB_API_VERSION +  "/search/movie?" +
                                                    "api_key=" + TMDB_API_KEY +
                                                    "&language=" + DEFAULT_LANGUAGE +
                                                    "&include_adult=" + INCLUDE_ADULT;


    private final static String GET_MOVIE_DETAILS_URL = TMDB_API_VERSION +
                                                        "/movie/{movieid}?" +
                                                        "api_key=" + TMDB_API_KEY +
                                                        "&language=" + DEFAULT_LANGUAGE;

    private final static String GET_MOVIE_CREDITS_URL = TMDB_API_VERSION +  "/movie/{movieid}/credits?api_key=" + TMDB_API_KEY;

    private final static String GET_UPCOMINGS_URL = TMDB_API_VERSION +  "/movie/upcoming?api_key=" + TMDB_API_KEY +
                                                    "&language=" + "it";

    private final static String GET_LATEST_URL = TMDB_API_VERSION +  "/movie/now_playing?api_key=" + TMDB_API_KEY +
                                                    "&language=" + "en";
    private final static String GET_POPULAR_URL = TMDB_API_VERSION +  "/movie/popular?api_key=" + TMDB_API_KEY +
                                                "&language=" + "it" +
                                                "&region=" + "it";


    private interface RetrofitAPI {
        @GET(SEARCH_ACTORS_URL)
        Call<ActorsSearchResult> searchActorsByName(@Query("query") String query,
                                                    @Query("page") int page);

        @GET(SEARCH_MOVIES_URL)
        Call<MoviesSearchResult> searchMoviesByTitle(@Query("query") String query,
                                                     @Query("page") int page);

        @GET(GET_MOVIE_DETAILS_URL)
        Call<Movie> getMoviesDetailsById(@Path("movieid") int movieid);

        @GET(GET_MOVIE_CREDITS_URL)
        Call<CreditsCast> buildServiceGetMovieCreditsById(@Path("movieid") int movieid);

        @GET(GET_UPCOMINGS_URL)
        Call<Upcomings> buildServiceGetUpcomings(@Query("page") int page);

        @GET(GET_POPULAR_URL)
        Call<Popular> buildServiceGetPopular(@Query("page") int page);

        @GET(GET_LATEST_URL)
        Call<Latest> buildServiceGetLatest(@Query("page") int page);

    }

    private class ActorsSearchResult {
        @SerializedName("results")
        @Expose
        private ArrayList<Cast> result;
        public ArrayList<Cast> getData() {
            return result;
        }
    }

    private class MoviesSearchResult {
        @SerializedName("results")
        @Expose
        private ArrayList<Movie> result;
        public ArrayList<Movie> getData() {
            return result;
        }
    }

    private class CreditsCast {
        @SerializedName("cast")
        @Expose
        private ArrayList<Cast> result;
        public ArrayList<Cast> getData() {
            return result;
        }
    }

    private class CreditsCrewt {
        @SerializedName("crew")
        @Expose
        private ArrayList<Cast> result;
        public ArrayList<Cast> getData() {
            return result;
        }
    }

    private class Upcomings {
        @SerializedName("results")
        @Expose
        private ArrayList<Movie> result;
        public ArrayList<Movie> getData() {
            return result;
        }
    }


    private class Popular {
        @SerializedName("results")
        @Expose
        private ArrayList<Movie> result;
        public ArrayList<Movie> getData() {
            return result;
        }
    }


    private class Latest {
        @SerializedName("results")
        @Expose
        private ArrayList<Movie> result;
        public ArrayList<Movie> getData() {
            return result;
        }
    }

    private RetrofitAPI buildRetrofitService() {
        final Retrofit retrofitClient = new Retrofit.Builder()
                .client(OkHttpSingleton.getClient())
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(getBaseURL())
                .build();

        return retrofitClient.create(RetrofitAPI.class);
    }


    public ArrayList<Cast> getCastByName(String query, int page) {
        ArrayList<Cast> result = new ArrayList<>();
        if(query==null || query.isEmpty() || page<=0)
            return result;

        final RetrofitAPI tmdbAPI = buildRetrofitService();

        try {
            Call<ActorsSearchResult> call = tmdbAPI.searchActorsByName(query, page);
            Response<ActorsSearchResult> response = call.execute();

            if(response.isSuccessful()) {
                ActorsSearchResult temp = response.body();
                result = temp.getData();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }// end getCastByName()


    // tested
    public ArrayList<Movie> getMoviesByTitle(String query, int page) {
        ArrayList<Movie> results = new ArrayList<>();
        if(query==null || query.isEmpty() || page<=0)
            return results;

        final RetrofitAPI apiService = buildRetrofitService();

        try {
            Call<MoviesSearchResult> call = apiService.searchMoviesByTitle(query, page);
            Response<MoviesSearchResult> response = call.execute();

            if(response.isSuccessful()) {
                MoviesSearchResult temp = response.body();
                results = temp.getData();

                //building full poster url
                if(results!=null) {
                    for(Movie mv: results) {
                        try {
                            String relativePath = mv.getPosterURL();
                            mv.setPosterURL(buildPosterUrl(relativePath));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return results;
    }// end getMoviesByTitle()

    public Movie getMoviesDetailsById(int movieId) {
        Movie result = null;
        if(movieId<=0)
            return result;

        final RetrofitAPI apiService = buildRetrofitService();

        try {
            Call<Movie> call = apiService.getMoviesDetailsById(movieId);
            Response<Movie> response = call.execute();

            if(response.isSuccessful()) {
                Movie temp = response.body();

                //building full poster/backdrop url
                if(temp!=null) {
                    try {
                        String relativePath = temp.getPosterURL();
                        temp.setPosterURL(buildPosterUrl(relativePath));
                        result = temp;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    try {
                        String relativePath = temp.getBackdropURL();
                        temp.setBackdropURL(buildBackdropUrl(relativePath));
                        result = temp;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }// end getMoviesDetailsById()

    public ArrayList<Cast> getMovieCastById(int movieId) {
        ArrayList<Cast> result = null;
        if(movieId<=0)
            return result;

        final RetrofitAPI apiService = buildRetrofitService();

        try {
            Call<CreditsCast> call = apiService.buildServiceGetMovieCreditsById(movieId);
            Response<CreditsCast> response = call.execute();

            if(response.isSuccessful()) {
                CreditsCast creditsCast = response.body();
                ArrayList<Cast> temp = creditsCast.getData();

                //building full pictures url
                if(temp!=null) {
                    for(Cast c: temp) {
                        try {
                            String relativePath = c.getProfilePictureUrl();
                            c.setProfilePictureUrl(buildPersonImageUrl(relativePath));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }

                result = temp;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }// end getMovieCastById()

    public ArrayList<Cast> getMovieCrewById(int movieId) {
        ArrayList<Cast> result = null;
        if(movieId<=0)
            return result;

        final RetrofitAPI apiService = buildRetrofitService();

        try {
            Call<CreditsCast> call = apiService.buildServiceGetMovieCreditsById(movieId);
            Response<CreditsCast> response = call.execute();

            if(response.isSuccessful()) {
                CreditsCast creditsCast = response.body();
                ArrayList<Cast> temp = creditsCast.getData();

                //building full pictures url
                if(temp!=null) {
                    for(Cast c: temp) {
                        try {
                            String relativePath = c.getProfilePictureUrl();
                            c.setProfilePictureUrl(buildPersonImageUrl(relativePath));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }

                result = temp;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }// end getMovieCrewById()

    public ArrayList<Movie> getUpcomgins(int page) {
        ArrayList<Movie> results = new ArrayList<>();
        if(page<=0)
            page = 1;

        final RetrofitAPI apiService = buildRetrofitService();

        try {
            Call<Upcomings> call = apiService.buildServiceGetUpcomings(page);
            Response<Upcomings> response = call.execute();

            if(response.isSuccessful()) {
                Upcomings temp = response.body();
                results = temp.getData();

                //building full poster url
                if(results!=null) {
                    for(Movie mv: results) {
                        try {
                            String relativePath = mv.getPosterURL();
                            mv.setPosterURL(buildPosterUrl(relativePath));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        try {
                            String relativePath = mv.getBackdropURL();
                            mv.setBackdropURL(buildBackdropUrl(relativePath));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return results;
    }// end getUpcomgins()

    public ArrayList<Movie> getPopular(int page) {
        ArrayList<Movie> results = new ArrayList<>();
        if(page<=0)
            page = 1;

        final RetrofitAPI apiService = buildRetrofitService();

        try {
            Call<Popular> call = apiService.buildServiceGetPopular(page);
            Response<Popular> response = call.execute();

            if(response.isSuccessful()) {
                Popular temp = response.body();
                results = temp.getData();

                //building full poster url
                if(results!=null) {
                    for(Movie mv: results) {
                        try {
                            String relativePath = mv.getPosterURL();
                            mv.setPosterURL(buildPosterUrl(relativePath));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        try {
                            String relativePath = mv.getBackdropURL();
                            mv.setBackdropURL(buildBackdropUrl(relativePath));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return results;
    }// end getPopular()

    public ArrayList<Movie> getLatest(int page) {
        ArrayList<Movie> results = new ArrayList<>();
        if(page<=0)
            page = 1;

        final RetrofitAPI apiService = buildRetrofitService();

        try {
            Call<Latest> call = apiService.buildServiceGetLatest(page);
            Response<Latest> response = call.execute();

            if(response.isSuccessful()) {
                Latest temp = response.body();
                results = temp.getData();

                //building full poster url
                if(results!=null) {
                    for(Movie mv: results) {
                        try {
                            String relativePath = mv.getPosterURL();
                            mv.setPosterURL(buildPosterUrl(relativePath));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        try {
                            String relativePath = mv.getBackdropURL();
                            mv.setBackdropURL(buildBackdropUrl(relativePath));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return results;
    }// end getLatest()

    public Integer getMaxNumPageLatest() {
        JSONObject jsonObj = null;
        int maxPageNum = new Integer(0);
        try{
            String myUrl  = BASE_URL +  "3/movie/now_playing?api_key=" + TMDB_API_KEY +
                    "&language=" + "en" +
                    "&page=" + 1;
            jsonObj = HttpUtilities.getJsonObjectFromUrl(myUrl);
            maxPageNum = new Integer(jsonObj.getInt("total_pages"));
        } catch (Exception e) {
            e.getMessage();
            e.printStackTrace();
        }

        return maxPageNum;
    }// end getMaxNumPageLatest()



//----------------------------

    public Integer getMaxNumPageUpcoming() {
        JSONObject jsonObj = null;
        Integer maxPageNum = new Integer(0);
        try{
            String myUrl  = BASE_URL +  "3/movie/upcoming?api_key=" + TMDB_API_KEY +
                    "&language=" + defaultLanguage +
                    "&page=" + 1;
            jsonObj = HttpUtilities.getJsonObjectFromUrl(myUrl);
            maxPageNum = new Integer(jsonObj.getInt("total_pages"));
        } catch (Exception e) {
            e.getMessage();
            e.printStackTrace();
        }

        return maxPageNum;
    }// end getMaxNumPageUpcoming()



    //----------------------------
    public Integer getMaxNumPagePopular() {
        JSONObject jsonObj = null;
        Integer maxPageNum = new Integer(0);
        try{
            String myUrl  = BASE_URL +  "3/movie/popular?api_key=" + TMDB_API_KEY +
                    "&language=" + defaultLanguage +
                    "&page=" + 1 +
                    "&region=" + defaultLanguage;

            jsonObj = HttpUtilities.getJsonObjectFromUrl(myUrl);
            maxPageNum = new Integer(jsonObj.getInt("total_pages"));
        } catch (Exception e) {
            e.getMessage();
            e.printStackTrace();
        }

        return maxPageNum;
    }// end getMaxNumPagePopular()




}// end TheMovieDatabaseAPI class
