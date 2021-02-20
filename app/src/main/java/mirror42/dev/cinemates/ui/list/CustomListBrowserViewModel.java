package mirror42.dev.cinemates.ui.list;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import mirror42.dev.cinemates.api.tmdbAPI.TheMovieDatabaseApi;
import mirror42.dev.cinemates.model.User;
import mirror42.dev.cinemates.model.list.CustomList;
import mirror42.dev.cinemates.model.tmdb.Movie;
import mirror42.dev.cinemates.ui.reaction.CommentsViewModel.TaskStatus;
import mirror42.dev.cinemates.utilities.HttpUtilities;
import mirror42.dev.cinemates.utilities.MyValues.FetchStatus;
import mirror42.dev.cinemates.utilities.OkHttpSingleton;
import mirror42.dev.cinemates.utilities.RemoteConfigServer;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class CustomListBrowserViewModel extends ViewModel {
    private final String TAG = this.getClass().getSimpleName();
    private MutableLiveData<ArrayList<CustomList>> customLists;
    private MutableLiveData<TaskStatus> taskStatus;
    private MutableLiveData<FetchStatus> fetchStatus;
    private RemoteConfigServer remoteConfigServer;
    private MutableLiveData<FetchStatus> publicFetchStatus;
    private MutableLiveData<FetchStatus> subscribedFetchStatus;




    //----------------------------------------------------------------------- CONSTRUCTORS

    public CustomListBrowserViewModel() {
        customLists = new MutableLiveData<>();
        taskStatus = new MutableLiveData<>(TaskStatus.IDLE);
        fetchStatus = new MutableLiveData<>(FetchStatus.IDLE);
        publicFetchStatus = new MutableLiveData<>(FetchStatus.IDLE);
        subscribedFetchStatus = new MutableLiveData<>(FetchStatus.IDLE);

        remoteConfigServer = RemoteConfigServer.getInstance();
    }



    //----------------------------------------------------------------------- GETTERS/SETTERS

    public LiveData<ArrayList<CustomList>> getObservableCustomList() {
        return customLists;
    }

    public ArrayList<CustomList> getCustomList() {
        return customLists.getValue();
    }

    public void setCustomLists(ArrayList<CustomList> customLists) {
        this.customLists.postValue(customLists);
    }

    public LiveData<TaskStatus> getObservableTaskStatus() {
        return taskStatus;
    }

    public void setTaskStatus(TaskStatus taskStatus) {
        this.taskStatus.postValue(taskStatus);
    }

    public LiveData<FetchStatus> getObservableFetchStatus() {
        return fetchStatus;
    }

    public void setFetchStatus(FetchStatus fetchStatus) {
        this.fetchStatus.postValue(fetchStatus);
    }

    public LiveData<FetchStatus> getObservablePublicFetchStatus() {
        return publicFetchStatus;
    }

    public void setPublicFetchStatus(FetchStatus fetchStatus) {
        this.publicFetchStatus.postValue(fetchStatus);
    }

    public LiveData<FetchStatus> getObservableSubscribedFetchStatus() {
        return subscribedFetchStatus;
    }

    public void setSubscribedFetchStatus(FetchStatus fetchStatus) {
        this.subscribedFetchStatus.postValue(fetchStatus);
    }


    //----------------------------------------------------------------------- METHODS

    public void fetchPublicLists(String targetUsername, User loggedUser) {
        Runnable task = createFetchPublicListsTask(loggedUser.getEmail(), loggedUser.getAccessToken(), targetUsername);
        Thread t_1 = new Thread(task);
        t_1.start();
    }

    private Runnable createFetchPublicListsTask(String requesterEmail, String token, String targetUsername) {
        return ()-> {
            try {
                // build httpurl and request for remote db
                final String dbFunction = "fn_select_all_custom_lists";
                HttpUrl httpUrl = HttpUtilities.buildHttpURL(dbFunction);
                final OkHttpClient httpClient = OkHttpSingleton.getClient();
                RequestBody requestBody = new FormBody.Builder()
                        .add("target_username", targetUsername)
                        .add("requester_email", requesterEmail)
                        .build();
                Request request = HttpUtilities.buildPostgresPOSTrequest(httpUrl, requestBody, token);

                // executing request
                try (Response response = httpClient.newCall(request).execute()) {
                    if (!response.isSuccessful()) {
                        setCustomLists(null);
                        setPublicFetchStatus(FetchStatus.FAILED);
                    }

                    //
                    String responseData = response.body().string();
                    ArrayList<CustomList> customLists = new ArrayList<>();

                    // if response contains valid data
                    if ( ! responseData.equals("null")) {
                        JSONArray jsonArray = new JSONArray(responseData);

                        for(int i=0; i<jsonArray.length(); i++) {
                            JSONObject jsonDBobj = jsonArray.getJSONObject(i);
                            CustomList customList = buildCustomList(jsonDBobj, token, targetUsername);
                            customLists.add(customList);
                        }// for

                        // once finished set result
//                                    Collections.reverse(postsList);
                        setCustomLists(customLists);
                        setPublicFetchStatus(FetchStatus.SUCCESS);

                    }
                    // if response contains no data
                    else {
                        setCustomLists(null);
                        setPublicFetchStatus(FetchStatus.NOT_EXISTS);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    setCustomLists(null);
                    setPublicFetchStatus(FetchStatus.FAILED);
                }


            } catch (Exception e) {
                e.printStackTrace();
                setCustomLists(null);
                setPublicFetchStatus(FetchStatus.FAILED);
            }
        };
    }// end createFetchPublicListsTask()


    public void fetchMyCustomLists(User loggedUser) {
        Runnable task = createFetchMyCustomListsTask(loggedUser.getEmail(), loggedUser.getAccessToken(), loggedUser.getUsername());
        Thread t_1 = new Thread(task);
        t_1.start();
    }

    private Runnable createFetchMyCustomListsTask(String email, String token, String loggedUsername) {
        return ()-> {
            try {
                // build httpurl and request for remote db
                final String dbFunction = "fn_select_all_custom_lists";
                HttpUrl httpUrl = HttpUtilities.buildHttpURL(dbFunction);
                final OkHttpClient httpClient = OkHttpSingleton.getClient();
                RequestBody requestBody = new FormBody.Builder()
                        .add("target_username", loggedUsername)
                        .add("requester_email", email)
                        .build();
                Request request = HttpUtilities.buildPostgresPOSTrequest(httpUrl, requestBody, token);

                // executing request
                try (Response response = httpClient.newCall(request).execute()) {
                    if (!response.isSuccessful()) {
                        setCustomLists(null);
                        setFetchStatus(FetchStatus.FAILED);
                    }

                    //
                    String responseData = response.body().string();
                    ArrayList<CustomList> customLists = new ArrayList<>();

                    // if response contains valid data
                    if ( ! responseData.equals("null")) {
                        JSONArray jsonArray = new JSONArray(responseData);

                        for(int i=0; i<jsonArray.length(); i++) {
                            JSONObject jsonDBobj = jsonArray.getJSONObject(i);
                            CustomList customList = buildCustomList(jsonDBobj, token, loggedUsername);
                            customLists.add(customList);
                        }// for

                        // once finished set result
//                                    Collections.reverse(postsList);
                        setCustomLists(customLists);
                        setFetchStatus(FetchStatus.SUCCESS);

                    }
                    // if response contains no data
                    else {
                        setCustomLists(null);
                        setFetchStatus(FetchStatus.NOT_EXISTS);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    setCustomLists(null);
                    setFetchStatus(FetchStatus.FAILED);
                }


            } catch (Exception e) {
                e.printStackTrace();
                setCustomLists(null);
                setFetchStatus(FetchStatus.FAILED);
            }
        };
    }// end createFetchMyCustomListsTask()

    public void fetchSubscribedLists(User loggedUser) {
        Runnable task = createFetchSubscribedListsTask(loggedUser.getEmail(), loggedUser.getAccessToken(), loggedUser.getUsername());
        Thread t_1 = new Thread(task);
        t_1.start();
    }

    //TODO
    private Runnable createFetchSubscribedListsTask(String email, String token, String loggedUsername) {
        return ()-> {
            try {
                // build httpurl and request for remote db
                final String dbFunction = "fn_select_all_subscribed_lists";
                HttpUrl httpUrl = HttpUtilities.buildHttpURL(dbFunction);
                final OkHttpClient httpClient = OkHttpSingleton.getClient();
                RequestBody requestBody = new FormBody.Builder()
                        .add("target_username", loggedUsername)
                        .add("requester_email", email)
                        .build();
                Request request = HttpUtilities.buildPostgresPOSTrequest(httpUrl, requestBody, token);

                // executing request
                try (Response response = httpClient.newCall(request).execute()) {
                    if (!response.isSuccessful()) {
                        setCustomLists(null);
                        setSubscribedFetchStatus(FetchStatus.FAILED);
                    }

                    //
                    String responseData = response.body().string();
                    ArrayList<CustomList> customLists = new ArrayList<>();

                    // if response contains valid data
                    if ( ! responseData.equals("null")) {
                        JSONArray jsonArray = new JSONArray(responseData);

                        for(int i=0; i<jsonArray.length(); i++) {
                            JSONObject jsonDBobj = jsonArray.getJSONObject(i);
                            String listOwnerUsername = jsonDBobj.getString("Username");
                            CustomList customList = buildCustomList(jsonDBobj, token, listOwnerUsername);
                            customLists.add(customList);
                        }// for

                        // once finished set result
//                                    Collections.reverse(postsList);
                        setCustomLists(customLists);
                        setSubscribedFetchStatus(FetchStatus.SUCCESS);

                    }
                    // if response contains no data
                    else {
                        setCustomLists(null);
                        setSubscribedFetchStatus(FetchStatus.NOT_EXISTS);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    setCustomLists(null);
                    setSubscribedFetchStatus(FetchStatus.FAILED);
                }


            } catch (Exception e) {
                e.printStackTrace();
                setCustomLists(null);
                setSubscribedFetchStatus(FetchStatus.FAILED);
            }
        };
    }// end createFetchMyCustomListsTask()
















    private CustomList buildCustomList(JSONObject jsonDBobj, String token, String targetUsername) throws Exception{
        // getting post owner data
        User user = buildOwner(jsonDBobj);
        // assembling post
        CustomList customList = new CustomList();
        customList.setId(jsonDBobj.getLong("Id_Custom_List"));
        customList.setOwner(user);
        customList.setName(jsonDBobj.getString("list_name"));
        customList.setDescription(jsonDBobj.getString("Description"));
        customList.setIsPrivate(jsonDBobj.getBoolean("Private"));
        ArrayList<Movie> moviesList = new ArrayList<>();
        moviesList = fetchMovies(jsonDBobj.getString("list_name"), targetUsername, token);
        customList.setMovies(moviesList);
        return customList;
    }

    private ArrayList<Movie> fetchMovies(String listName, String ownerUsername, String token) {
        ArrayList<Movie> moviesList = new ArrayList<>();

        // build httpurl and request for remote db
        final String dbFunction = "fn_select_custom_list";
        HttpUrl httpUrl = HttpUtilities.buildHttpURL(dbFunction);
        final OkHttpClient httpClient = OkHttpSingleton.getClient();
        RequestBody requestBody = new FormBody.Builder()
                .add("list_name", listName)
                .add("owner_username", ownerUsername)
                .build();
        Request request = HttpUtilities.buildPostgresPOSTrequest(httpUrl, requestBody, token);
        try (Response response = httpClient.newCall(request).execute()) {
            String responseData = null;

            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response);
            }

            responseData = response.body().string();
            if ( ! responseData.equals("null")) {
                JSONArray jsonArray = new JSONArray(responseData);
                moviesList = new ArrayList<>();

                for(int i=0; i<jsonArray.length(); i++) {
                    JSONObject jsonDBobj = jsonArray.getJSONObject(i);
                    Movie movie = buildMovie(jsonDBobj);
                    moviesList.add(movie);
                }// for
            }

            }catch (Exception e) {
            e.printStackTrace();
        }



        return moviesList;
    }

    private User buildOwner(JSONObject jsonObject) throws JSONException {
        User user = new User();
        user.setUsername(jsonObject.getString("Username"));
        user.setFirstName(jsonObject.getString("owner_first_name"));
        user.setLastName(jsonObject.getString("LastName"));
        user.setProfilePictureURL(remoteConfigServer.getCloudinaryDownloadBaseUrl() + jsonObject.getString("ProfileImage"));
        return user;
    }

    private Movie buildMovie(JSONObject jsonObject) throws JSONException {
        TheMovieDatabaseApi tmdb = TheMovieDatabaseApi.getInstance();
        int movieId = jsonObject.getInt("fk_movie");
        Movie movie = tmdb.getMoviesDetailsById(movieId);
        return movie;
    }

    public void createNewList(String name, String description, boolean isPrivate, User loggedUser) {
        try {
            // build httpurl and request for remote db
            final String dbFunction = "fn_create_custom_list";
            HttpUrl httpUrl = HttpUtilities.buildHttpURL(dbFunction);
            final OkHttpClient httpClient = OkHttpSingleton.getClient();
            RequestBody requestBody = new FormBody.Builder()
                    .add("name", name)
                    .add("description", description)
                    .add("is_private", String.valueOf(isPrivate))
                    .add("email", loggedUser.getEmail())
                    .build();
            Request request = HttpUtilities.buildPostgresPOSTrequest(httpUrl, requestBody, loggedUser.getAccessToken());

            // executing request
            Call call = httpClient.newCall(request);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    setTaskStatus(TaskStatus.FAILED);
                }

                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                    try {
                        // check responses
                        if (response.isSuccessful()) {
                            String responseData = response.body().string();

                            // if response contains valid data
                            if (responseData.equals("true")) {
                                setTaskStatus(TaskStatus.SUCCESS);
                            }
                            // if response contains no data
                            else {
                                setTaskStatus(TaskStatus.FAILED);
                            }
                        } // if response is unsuccessful
                        else {
                            setTaskStatus(TaskStatus.FAILED);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        setTaskStatus(TaskStatus.FAILED);
                    }
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            setTaskStatus(TaskStatus.FAILED);
        }
    }


}// end CustomListBrowserViewModel class