package mirror42.dev.cinemates.tmdbAPI;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import mirror42.dev.cinemates.utilities.HttpUtilities;


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
    private final String MY_API_KEY = "632f90b0d342606c53a9ffd5fc5ed32e";
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
        posterSizes = new ArrayList<>();
        backgroundImageSizes = new ArrayList<>();
        profileImageSizes = new ArrayList<>();
        defaultPosterSize = SMALL_POSTER_SIZE;
        defaultBackdropSize = MEDIUM_BACKGROUND_SIZE;
        defaultPersonImageSize = MEDIUM_PROFILE_SIZE;
        apiConfigurationObject = null;
    }

    public static TheMovieDatabaseApi getInstance() {
        if(singletonInstance==null)
            return new TheMovieDatabaseApi();

        //TODO: should load image configuration data

        return singletonInstance;
    }



    //--------------------------------------------------------------------------- GETTERS/SETTERS

    /**
     * this method is very important,
     * because set the default language for retrieved TBDb movies data
     * TO THE ENTIRE APPLICATION.
     * Changing this, will change the localisation for retrieved data
     * (title, descriptions, poster language, ...) from TMBd.
     *
     * @param   lang   the language must be ISO_639-1 compliant,
     *                 see url link below.
     *                 An invalid language code yield to an invalid url,
     *                 an invalid url yield to unexpected results.
     *                 Be sure to give the correct language code!
     *
     * @see     <a href="https://en.wikipedia.org/wiki/List_of_ISO_639-1_codes">ISO_639-1 language codes</a>
     * @see     <a href="https://developers.themoviedb.org/3/getting-started/languages">TMDb API - languages section</a>
     */
    public void setDefaultLanguage(String lang) {
        defaultLanguage = lang;
    }

    public void enableAdultContent(boolean value) {
        if(value==true) defaultAdultContentFilter = "true";
        else defaultAdultContentFilter = "false";
    }

    public void setDefaultPosterSize(String defaultPosterSize) {
        //use default public sizes above
        this.defaultPosterSize = defaultPosterSize;
    }

    public void setDefaultBackdropSize(String defaultBackdropSize) {
        //use default public sizes above
        this.defaultBackdropSize = defaultBackdropSize;
    }

    public void setDefaultPersonImageSize(String defaultPersonImageSize) {
        //use default public sizes above
        this.defaultPersonImageSize = defaultPersonImageSize;
    }

    public String getDefaultPosterSize() {
        return defaultPosterSize;
    }

    // TODO: should be static
    public String getBaseImageUrl() {
        return baseImageUrl;
    }



    //--------------------------------------------------------------------------- PUBLIC METHODS
    public void trySearch(String movieTitle){
        // a result can span across multiple pages
        // this method return only the first result page.
        // For a specific page use search(String, int)

        ArrayList<Integer> idList = null;
        movieTitle = movieTitle.trim();

        if( ! movieTitle.isEmpty()) {
            // creating TBDb url
            String url = BASE_URL +  "3/search/movie?api_key=" + MY_API_KEY +
                    "&language=" + defaultLanguage +
                    "&query=" + movieTitle +
                    "&include_adult" + defaultAdultContentFilter;

            try {
                // querying TBDb
                JSONObject json = HttpUtilities.getJsonObjectFromUrl(url);
                JSONArray results = json.getJSONArray("results");

                // fetching results
                for(int i=0; i<results.length(); i++) {
                    // fetching results
                    JSONObject x = results.getJSONObject(i);
                    int id = x.getInt("id");
                    String title = x.getString("title");

                    System.out.println(title + " (ID: " + id + " )");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }// outer if
    }

    public void trySearch(String movieTitle, int page) {
        movieTitle = movieTitle.trim();

        if( ! movieTitle.isEmpty()) {
            // creating TBDb url
            String url = BASE_URL +  "3/search/movie?api_key=" + MY_API_KEY +
                    "&language=" + defaultLanguage +
                    "&query=" + movieTitle +
                    "&page=" + page +
                    "&include_adult=" + defaultAdultContentFilter;
            try {
                // querying TBDb
                JSONObject json = HttpUtilities.getJsonObjectFromUrl(url);
                JSONArray results = json.getJSONArray("results");

                // fetching results
                for(int i=0; i<results.length(); i++) {
                    // fetching results
                    JSONObject x = results.getJSONObject(i);
                    int id = x.getInt("id");
                    String title = x.getString("title");

                    System.out.println(title + " (ID: " + id + " )");
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }// outer if

    }

    public ArrayList<Integer> getMovieIdListByTitle(String movieTitle) {
        // a result can span across multiple pages
        // this method return only the first result page.
        // For a specific page use getMovieIDListByTitle(String, int)

        ArrayList<Integer> idList = null;
        movieTitle = movieTitle.trim();

        if( ! movieTitle.isEmpty()) {
            // creating TBDb url
            String url = BASE_URL +  "3/search/movie?api_key=" + MY_API_KEY +
                    "&language=" + defaultLanguage +
                    "&query=" + movieTitle +
                    "&include_adult" + defaultAdultContentFilter;

            try {
                idList = new ArrayList<>();

                // querying TBDb
                JSONObject json = HttpUtilities.getJsonObjectFromUrl(url);
                JSONArray results = json.getJSONArray("results");

                // fetching results
                for(int i=0; i<results.length(); i++) {
                    // fetching results
                    JSONObject x = results.getJSONObject(i);
                    int id = x.getInt("id");
                    idList.add(id);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }// outer if

        return idList;
    }

    public ArrayList<Integer> getMovieIdListByTitle(String movieTitle, int page) {
        ArrayList<Integer> idList = null;
        movieTitle = movieTitle.trim();

        if( ! movieTitle.isEmpty()) {
            // creating TBDb url
            String url = BASE_URL +  "3/search/movie?api_key=" + MY_API_KEY +
                    "&language=" + defaultLanguage +
                    "&query=" + movieTitle +
                    "&page=" + page +
                    "&include_adult=" + defaultAdultContentFilter;
            try {
                idList = new ArrayList<>();

                // querying TBDb
                JSONObject json = HttpUtilities.getJsonObjectFromUrl(url);
                JSONArray results = json.getJSONArray("results");

                // fetching results
                for(int i=0; i<results.length(); i++) {
                    // fetching results
                    JSONObject x = results.getJSONObject(i);
                    int id = x.getInt("id");
                    idList.add(id);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }// outer if

        return idList;
    }

    public int getNumOfResultPages(String movieTitle) {
        int result = 0;

        movieTitle = movieTitle.trim();
        if( ! movieTitle.isEmpty()) {
            // creating TBDb url
            String url = BASE_URL +  "3/search/movie?api_key=" + MY_API_KEY +
                    "&language=" + defaultLanguage +
                    "&query=" + movieTitle +
                    "&include_adult" + defaultAdultContentFilter;

            try {
                // querying TBDb
                JSONObject jsonObj = HttpUtilities.getJsonObjectFromUrl(url);
                result = jsonObj.getInt("total_pages");

            } catch (Exception e) {
                e.printStackTrace();
            }
        }// outer if

        return result;
    }

    /**
     * @return   ID list of trending movies of the day,
     *           if some exception happens returns null
     */
    public ArrayList<Integer> getTrendingMoviesOfTheDay(){
        ArrayList<Integer> IDList = null;

        try {
            IDList = new ArrayList<>();

            // querying TBDb
            JSONObject json = getJsonTrending(MOVIE, DAY);
            JSONArray results = json.getJSONArray(RESULTS);

            // fetching results
            for(int i=0; i<results.length(); i++) {
                JSONObject x = results.getJSONObject(i);
                int id = x.getInt("id");
                IDList.add(id);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return IDList;
    }

    /**
     * @return   ID list of trending movies of the week,
     *           if some exception happens returns null
     */
    public ArrayList<Integer> getTrendingMoviesOfTheWeek(){
        ArrayList<Integer> IDList = null;

        try {
            IDList = new ArrayList<>();

            // querying TBDb
            JSONObject json = getJsonTrending(MOVIE, WEEK);
            JSONArray results = json.getJSONArray(RESULTS);

            // fetching results
            for(int i=0; i<results.length(); i++) {
                JSONObject x = results.getJSONObject(i);
                int id = x.getInt("id");
                IDList.add(id);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return IDList;
    }


    public String getMovieTitleById(int movieId) {
        String result = null;
        JSONObject jsonObj = getJsonMovieDetailsById(movieId);

        // parsing json object
        try {
            if(jsonObj != null)
                result = jsonObj.getString("title");
        }
        catch(Exception e) {
            e.getMessage();
            e.printStackTrace();
        }

        return result;
    }

    public String getOriginalTitleById(int movieId) {
        String result = null;
        JSONObject jsonObj = getJsonMovieDetailsById(movieId);

        // parsing json object
        try {
            if(jsonObj != null)
                result = jsonObj.getString("original_title");
        }
        catch(Exception e) {
            e.getMessage();
            e.printStackTrace();
        }

        return result;
    }

    public String getPosterById(int movieId) {
        String result = null;

        // getting json object
        JSONObject jsonObj = getJsonMovieDetailsById(movieId);

        // parsing json object
        try {
            if(jsonObj != null) {
                String imagePath = jsonObj.getString("poster_path");

                // fetching image by the image URL
                result = baseImageUrl + defaultPosterSize + imagePath;
            }
        }
        catch(Exception e) {
            e.getMessage();
            e.printStackTrace();
        }

        return result;
    }

    public String getBackgroundImageById(int movieId) {
        String result = null;

        // getting json object
        JSONObject jsonObj = getJsonMovieDetailsById(movieId);

        // parsing json object
        try {
            if(jsonObj != null) {
                String imagePath = jsonObj.getString("backdrop_path");

                // fetching image by the image URL
                result = baseImageUrl + defaultBackdropSize + imagePath;
            }
        }
        catch(Exception e) {
            e.getMessage();
            e.printStackTrace();
        }

        return result;
    }

    public ArrayList<String> getMovieImageListById(int movieId) {
        ArrayList<String> result = null;

        // getting json object
        JSONObject jsonObj = getJsonMovieImageListById(movieId);

        // parsing json object
        try {
            JSONArray images = jsonObj.getJSONArray("backdrops");
            result = new ArrayList<>();

            if(images != null) {
                for(int i=0; i<images.length(); i++) {
                    JSONObject tempObj = images.getJSONObject(i);
                    String tempImageUrl = tempObj.getString("file_path");

                    result.add(baseImageUrl + defaultBackdropSize + tempImageUrl);
                }
            }
        }
        catch(Exception e) {
            e.getMessage();
            e.printStackTrace();
        }

        return result;
    }

    public String getShortDescriptionById(int movieId) {
        String result = null;

        // getting json object
        JSONObject jsonObj = getJsonMovieDetailsById(movieId);

        // parsing json object
        try {
            if(jsonObj != null) {
                result = jsonObj.getString("overview");

                if((result==null) || (result.isEmpty())) {
                    JSONObject jsonObj2 = getJsonMovieDetailsByIdFallback(movieId);
                    try {
                        if(jsonObj != null) {
                            //TODO
                            result = "(trama non disponibile in italiano)\n" + jsonObj2.getString("overview");

                        }
                    } catch(Exception e) {
                        e.getMessage();
                        e.printStackTrace();
                    }
                }
            }
        }
        catch(Exception e) {
            e.getMessage();
            e.printStackTrace();
        }

        return result;
    }

    public double getAverageVote(int movieId) {
        double result = 0.0;

        // getting json object
        JSONObject jsonObj = getJsonMovieDetailsById(movieId);

        // parsing json object
        try {
            if(jsonObj != null) {
                result = jsonObj.getDouble("vote_average");
            }
        }
        catch(Exception e) {
            e.getMessage();
            e.printStackTrace();
        }

        return result;
    }

    public String getReleaseDate(int movieId) {
        String result = null;

        // getting json object
        JSONObject jsonObj = getJsonMovieDetailsById(movieId);

        // parsing json object
        try {
            if(jsonObj != null) {
                result = jsonObj.getString("release_date");
            }
        }
        catch(Exception e) {
            e.getMessage();
            e.printStackTrace();
        }

        return result;
    }

    public String getWebsite(int movieId) {
        String result = null;

        // getting json object
        JSONObject jsonObj = getJsonMovieDetailsById(movieId);

        // parsing json object
        try {
            if(jsonObj != null) {
                result = jsonObj.getString("homepage");
            }
        }
        catch(Exception e) {
            e.getMessage();
            e.printStackTrace();
        }

        return result;
    }

    public ArrayList<String> getActorsNameListById(int movieId) {
        ArrayList<String> result = null;

        // parsing json object
        try {
            // getting json objects
            JSONObject parentObj = getJsonCreditsById(movieId);
            JSONArray castObj = parentObj.getJSONArray("cast");

            if(castObj != null) {
                result = new ArrayList<>();

                for(int i=0; i<castObj.length(); i++) {
                    JSONObject tempCastMember = castObj.getJSONObject(i);
                    String tempRole = tempCastMember.getString("known_for_department");

                    if(tempRole.equals("Acting"))
                        result.add(tempCastMember.getString("name"));
                }
            }
        }
        catch(Exception e) {
            e.getMessage();
            e.printStackTrace();
        }

        return result;
    }

    public ArrayList<String> getDirectorsNameListById(int movieId) {
        ArrayList<String> result = null;

        // parsing json object
        try {
            // getting json objects
            JSONObject parentObj = getJsonCreditsById(movieId);
            JSONArray castObj = parentObj.getJSONArray("cast");

            if(castObj != null) {
                result = new ArrayList<>();

                for(int i=0; i<castObj.length(); i++) {
                    JSONObject tempCastMember = castObj.getJSONObject(i);
                    String tempRole = tempCastMember.getString("known_for_department");

                    if(tempRole.equals("Directing"))
                        result.add(tempCastMember.getString("name"));
                }
            }
        }
        catch(Exception e) {
            e.getMessage();
            e.printStackTrace();
        }

        return result;
    }

    public ArrayList<Integer> getActorsIDListByMovieId(int movieId) {
        ArrayList<Integer> result = null;

        // parsing json object
        try {
            // getting json objects
            JSONObject parentObj = getJsonCreditsById(movieId);
            JSONArray castObj = parentObj.getJSONArray("cast");

            if(castObj != null) {
                result = new ArrayList<>();

                for(int i=0; i<castObj.length(); i++) {
                    JSONObject tempCastMember = castObj.getJSONObject(i);
                    String tempRole = tempCastMember.getString("known_for_department");

                    if(tempRole.equals("Acting"))
                        result.add(tempCastMember.getInt("id"));
                }
            }
        }
        catch(Exception e) {
            e.getMessage();
            e.printStackTrace();
        }

        return result;
    }

    public ArrayList<Integer> getDirectorsIDListByMovieId(int movieId) {
        ArrayList<Integer> result = null;

        // parsing json object
        try {
            // getting json objects
            JSONObject parentObj = getJsonCreditsById(movieId);
            JSONArray castObj = parentObj.getJSONArray("cast");

            if(castObj != null) {
                result = new ArrayList<>();

                for(int i=0; i<castObj.length(); i++) {
                    JSONObject tempCastMember = castObj.getJSONObject(i);
                    String tempRole = tempCastMember.getString("known_for_department");

                    if(tempRole.equals("Directing"))
                        result.add(tempCastMember.getInt("id"));
                }
            }
        }
        catch(Exception e) {
            e.getMessage();
            e.printStackTrace();
        }

        return result;
    }

    public String getPersonNameById(int personID) {
        String result = null;

        // parsing json object
        try {
            // getting json objects
            JSONObject personObj = getJsonPersonById(personID);

            if(personObj != null) {
                result = personObj.getString("name");
            }
        }
        catch(Exception e) {
            e.getMessage();
            e.printStackTrace();
        }

        return result;
    }

    public String getPersonProfileImageById(int personID) {
        String result = null;

        // parsing json object
        try {
            // getting json objects
            JSONObject personObj = getJsonPersonById(personID);

            if(personObj != null) {
                String imagePath = personObj.getString("profile_path");
                result = baseImageUrl + defaultPosterSize + imagePath;
            }
        }
        catch(Exception e) {
            e.getMessage();
            e.printStackTrace();
        }

        return result;
    }

    public int getTotalPages(JSONObject jsonObj) {
        int tot = 0;


        try {
            if(jsonObj!=null) {
                tot = jsonObj.getInt(TOTAL_PAGES);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return tot;
    }

    public int getTotalResults(JSONObject jsonObj) {
        int tot = 0;

        try {
            if(jsonObj!=null) {
                tot = jsonObj.getInt(TOTAL_RESULTS);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return tot;
    }


    //--------------------------------------------------------------------------- PRIVATE METHODS
    // methods used by the class to perform internal computations

    //------ build methods

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

    public String buildURLforSearchMoviesByTitle(String movieTitle) {
        String result = null;
        try{
            // creating TBDb url
            result = BASE_URL +  "3/search/movie?api_key=" + MY_API_KEY +
                    "&language=" + defaultLanguage +
                    "&query=" + movieTitle +
                    "&include_adult=" + defaultAdultContentFilter;

            return result;
        } catch (Exception e) {
            e.getMessage();
            e.printStackTrace();
        }

        return result;
    }

    public String buildURLforMovieDetailsById(int movieId) {
        String result = null;
        try{
            result = BASE_URL + "3/movie/" + movieId + "?api_key=" + MY_API_KEY + "&language=" + defaultLanguage;
            return result;
        } catch (Exception e) {
            e.getMessage();
            e.printStackTrace();
        }

        return result;
    }

    private JSONObject buildJsonObject(String url) {
        JSONObject jsonObj = null;
        try{
            jsonObj = HttpUtilities.getJsonObjectFromUrl(url);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return jsonObj;
    }

    //------ end build methods



    //------

    public JSONObject getJsonMoviesListByTitle(String movieTitle, int page) {
        JSONObject jsonObj = null;
        try{
            String myUrl  = BASE_URL +  "3/search/movie?api_key=" + MY_API_KEY +
                    "&language=" + defaultLanguage +
                    "&query=" + movieTitle +
                    "&page=" + page +
                    "&include_adult=" + defaultAdultContentFilter;

            jsonObj = HttpUtilities.getJsonObjectFromUrl(myUrl);
        } catch (Exception e) {
            e.getMessage();
            e.printStackTrace();
        }

        return jsonObj;
    }

    public JSONObject getJsonActorsListByName(String name, int page) {
        JSONObject jsonObj = null;
        try{
            String myUrl  = BASE_URL +  "3/search/person?api_key=" + MY_API_KEY +
                    "&language=" + defaultLanguage +
                    "&query=" + name +
                    "&page=" + page +
                    "&include_adult=" + defaultAdultContentFilter;

            jsonObj = HttpUtilities.getJsonObjectFromUrl(myUrl);
        } catch (Exception e) {
            e.getMessage();
            e.printStackTrace();
        }

        return jsonObj;
    }


    public JSONObject getJsonMovieDetailsById(int movieId) {
        JSONObject jsonObj = null;
        try{
            String myUrl = BASE_URL + "3/movie/" +
                    movieId +
                    "?api_key=" + MY_API_KEY +
                    "&language=" + defaultLanguage;
            jsonObj = HttpUtilities.getJsonObjectFromUrl(myUrl);
        } catch (Exception e) {
            e.getMessage();
            e.printStackTrace();
        }

        return jsonObj;
    }

    public String getJsonMovieDetailsById_urlString(int movieId) {
        String myUrl = null;
        try{
            myUrl = BASE_URL + "3/movie/" +
                    movieId +
                    "?api_key=" + MY_API_KEY +
                    "&language=" + defaultLanguage;
        } catch (Exception e) {
            e.getMessage();
            e.printStackTrace();
        }

        return myUrl;
    }

    public JSONObject getJsonCreditsById(int movieId) {
        JSONObject jsonObj = null;
        try{
            String myUrl = BASE_URL + "3/movie/" + movieId + "/credits?api_key=" + MY_API_KEY;
            jsonObj = HttpUtilities.getJsonObjectFromUrl(myUrl);
        } catch (Exception e) {
            e.getMessage();
            e.printStackTrace();
        }

        return jsonObj;
    }

    public JSONObject getJsonLatestReleases(int page) {
        JSONObject jsonObj = null;

        if(page<=0) {
            page = 1; //default page
        }

        try{
            String myUrl  = BASE_URL +  "3/movie/now_playing?api_key=" + MY_API_KEY +
                    "&language=" + "en" +
                    "&page=" + page;
            jsonObj = HttpUtilities.getJsonObjectFromUrl(myUrl);
        } catch (Exception e) {
            e.getMessage();
            e.printStackTrace();
        }

        return jsonObj;
    }

    public JSONObject getJsonUpcoming(int page) {
        JSONObject jsonObj = null;

        if(page<=0) {
            page = 1; //default page
        }

        try{
            String myUrl  = BASE_URL +  "3/movie/upcoming?api_key=" + MY_API_KEY +
                    "&language=" + defaultLanguage +
                    "&page=" + page;
            jsonObj = HttpUtilities.getJsonObjectFromUrl(myUrl);
        } catch (Exception e) {
            e.getMessage();
            e.printStackTrace();
        }


        return jsonObj;
    }

    /**
     *
     * Get a list of the current popular movies on TMDb. This list updates daily.
     */
    public JSONObject getJsonPopular(int page) {
        JSONObject jsonObj = null;

        if(page<=0) {
            page = 1; //default page
        }

        try{
            String myUrl  = BASE_URL +  "3/movie/popular?api_key=" + MY_API_KEY +
                    "&language=" + defaultLanguage +
                    "&page=" + page +
                    "&region=" + defaultLanguage;

            jsonObj = HttpUtilities.getJsonObjectFromUrl(myUrl);
        } catch (Exception e) {
            e.getMessage();
            e.printStackTrace();
        }


        return jsonObj;
    }

    public JSONObject getJsonTopRated(int page) {
        JSONObject jsonObj = null;

        if(page<=0) {
            page = 1; //default page
        }

        try{
            String myUrl  = BASE_URL +  "3/movie/top_rated?api_key=" + MY_API_KEY +
                    "&language=" + defaultLanguage +
                    "&page=" + page +
                    "&region=" + defaultLanguage;

            jsonObj = HttpUtilities.getJsonObjectFromUrl(myUrl);
        } catch (Exception e) {
            e.getMessage();
            e.printStackTrace();
        }


        return jsonObj;
    }


    // Retrieve all movie genres
    public JSONObject getAllJsonMoviesGenres() {
        JSONObject jsonObj = null;
        try{
            String myUrl  = BASE_URL +  "3/genre/movie/list?api_key=" + MY_API_KEY +
                    "&language=" + defaultLanguage;

            jsonObj = HttpUtilities.getJsonObjectFromUrl(myUrl);
        } catch (Exception e) {
            e.getMessage();
            e.printStackTrace();
        }

        return jsonObj;
    }


    //------

    private JSONObject getJsonMovieDetailsByIdFallback(int movieId) {
        JSONObject jsonObj = null;
        try{
            String myUrl = BASE_URL + "3/movie/" + movieId + "?api_key=" + MY_API_KEY + "&language=" + ENGISH_LANGUAGE;
            jsonObj = HttpUtilities.getJsonObjectFromUrl(myUrl);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return jsonObj;
    }

    private JSONObject getJsonMovieImageListById(int movieId) {
        JSONObject jsonObj = null;
        try{
            String myUrl = BASE_URL + "3/movie/" + movieId + "/images?api_key=" + MY_API_KEY;
            jsonObj = HttpUtilities.getJsonObjectFromUrl(myUrl);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return jsonObj;
    }

    private JSONObject getJsonPersonById(int personID) {
        JSONObject jsonObj = null;
        try{
            String myUrl = BASE_URL + "3/person/" + personID + "?api_key=" + MY_API_KEY;
            jsonObj = HttpUtilities.getJsonObjectFromUrl(myUrl);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return jsonObj;
    }

    /**
     * retrieve trending media of the day/week
     *
     * @param mediaType    can be: 'all', 'movie', 'tv', 'person'
     * @param timeWindow   can be: 'day' or 'week'
     * @return             trending media in {@code JSONObject} format
     */
    private JSONObject getJsonTrending(String mediaType, String timeWindow) {
        JSONObject jsonObj = null;
        try{
            String myUrl = BASE_URL + "3/trending/" + mediaType + "/" + timeWindow + "?api_key=" + MY_API_KEY;
            jsonObj = HttpUtilities.getJsonObjectFromUrl(myUrl);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return jsonObj;
    }

    /**
     * this is a very important method!!!!
     * must be called first(f.e. in Driver class),
     * before any uses of this class.
     * It loads some configuration data for images.
     *
     * @see     <a href="https://developers.themoviedb.org/3/configuration/get-api-configuration">TMDb API - configuration section</a>
     */
    public void loadImagesConfigurationData() {
        try{
            // getting api configuration object
            String myUrl = BASE_URL + "3/configuration?api_key=" + MY_API_KEY;
            JSONObject obj = HttpUtilities.getJsonObjectFromUrl(myUrl);
            apiConfigurationObject = obj.getJSONObject("images");

            // loading configuration data
            loadBaseImageUrl();
            loadPosterImageSizes();
            loadBackgroundImageSizes();
            loadProfileImageSizes();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadBaseImageUrl() {
        try{
            baseImageUrl = (String) apiConfigurationObject.get("secure_base_url"); // secure=https
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * those methods loads all possible sizes
     * for poster and background images (aka backdrop images)
     * into:
     * {@link #posterSizes} String arraylist,
     * {@link #backgroundImageSizes} String arraylist.
     *
     * Although default sizes(small, medium, big)
     * {@link #SMALL_POSTER_SIZE},
     * {@link #MEDIUM_POSTER_SIZE},
     * {@link #LARGE_POSTER_SIZE},
     * {@link #SMALL_BACKGROUND_SIZE},
     * {@link #MEDIUM_BACKGROUND_SIZE},
     * {@link #LARGE_BACKGROUND_SIZE}
     * are already set,
     * we can be much more granular
     * using other possible sizes
     * listed in posterSizes and backgroundImageSizes arraylist.
     *
     * @see     <a href="https://developers.themoviedb.org/3/configuration/get-api-configuration">TMDb API - configuration section</a>
     */
    private void loadPosterImageSizes() {
        try{
            JSONArray jsonArray = apiConfigurationObject.getJSONArray("poster_sizes");

            for(int i=0; i<jsonArray.length(); i++) {
                posterSizes.add(jsonArray.get(i).toString());
            }

//            smallPosterSize = posterSizes.get(0);
//            mediumPosterSize = posterSizes.get(4);
//            if(posterSizes.size()>2)
//                bigPosterSize = posterSizes.get(posterSizes.size()-2);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadBackgroundImageSizes() {
        try{
            JSONArray jsonArray = apiConfigurationObject.getJSONArray("backdrop_sizes");

            for(int i=0; i<jsonArray.length(); i++) {
                backgroundImageSizes.add(jsonArray.get(i).toString());
            }

//            smallBackgroundImageSize = backgroundImageSizes.get(0);
//            mediumBackgroundImageSize = backgroundImageSizes.get(1);
//            if(posterSizes.size()>2)
//                bigBackgroundImageSize = backgroundImageSizes.get(backgroundImageSizes.size()-2);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadProfileImageSizes() {
        try{
            JSONArray jsonArray = apiConfigurationObject.getJSONArray("profile_sizes");

            for(int i=0; i<jsonArray.length(); i++) {
                profileImageSizes.add(jsonArray.get(i).toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }




    //--------------------------------- retrofit versions




}// end TheMovieDatabaseAPI class
