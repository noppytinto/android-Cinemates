package mirror42.dev.cinemates.ui.notification;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.ConnectException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.SerialDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import mirror42.dev.cinemates.api.tmdbAPI.TheMovieDatabaseApi;
import mirror42.dev.cinemates.dao.room.NotificationDao;
import mirror42.dev.cinemates.model.User;
import mirror42.dev.cinemates.model.list.CustomList;
import mirror42.dev.cinemates.model.notification.FollowRequestNotification;
import mirror42.dev.cinemates.model.notification.ListRecommendedNotification;
import mirror42.dev.cinemates.model.notification.Notification;
import mirror42.dev.cinemates.model.notification.PostCommentedNotification;
import mirror42.dev.cinemates.model.notification.PostLikedNotification;
import mirror42.dev.cinemates.model.notification.SubscribedToListNotification;
import mirror42.dev.cinemates.model.room.CinematesLocalDatabase;
import mirror42.dev.cinemates.model.tmdb.Movie;
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

public class NotificationsViewModel extends ViewModel {
    private final String TAG = getClass().getSimpleName();
    private RemoteConfigServer remoteConfigServer;
    private MutableLiveData<ArrayList<Notification>> notificationsList;
    private SerialDisposable notificationsSubscription;
    private MutableLiveData<NotificationsStatus> notificationsStatus;
    private long notificationID;
    private MutableLiveData<FetchStatus> fetchStatus;
    private MutableLiveData<CustomList> customList;

    public enum NotificationsStatus {
        NOTIFICATIONS_FETCHED,
        GOT_NEW_NOTIFICATIONS,
        ALL_NOTIFICATIONS_READ,
        NO_NOTIFICATIONS,
        NOTIFICATION_DELETED,
        NOTIFICATION_NOT_DELETED,
        IDLE
    }

    private final String FOLLOW_REQUEST_NOTIFICATION_TYPE = "FR";      //(Friend Request)
    private final String POST_LIKED_NOTIFICATION_TYPE = "PL";          //(Post Liked)
    private final String POST_COMMENTED_NOTIFICATION_TYPE = "PC";      //(Post Commented)
    private final String LIST_RECOMMENDED_NOTIFICATION_TYPE = "CR";    //(List recommendation)
    private final String SUBSCRIBED_TO_LIST_NOTIFICATION_TYPE = "CS";  //(user subscribed to your list)
    private final String MOVIE_RECOMMENDED_NOTIFICATION_TYPE = "MR";   //(user recommended a movie)




    //-------------------------------------------------------------------------- CONSTRUCTORS

    public NotificationsViewModel() {
        notificationsList = new MutableLiveData<>();
        remoteConfigServer = RemoteConfigServer.getInstance();
        notificationsStatus = new MutableLiveData<>(NotificationsStatus.NO_NOTIFICATIONS);
        fetchStatus = new MutableLiveData<>(FetchStatus.IDLE);
        customList = new MutableLiveData<>();
        notificationsSubscription = new SerialDisposable();

    }



    //-------------------------------------------------------------------------- GETTERS/SETTERS

    public void setNotificationsList(ArrayList<Notification> notifications) {
        this.notificationsList.postValue(notifications);
    }

    public void setNotificationsStatus(NotificationsStatus status) {
        this.notificationsStatus.postValue(status);
    }

    public LiveData<ArrayList<Notification>> getNotifications() {
        return notificationsList;
    }

    public LiveData<NotificationsStatus> getNotificationsStatus() {
        return notificationsStatus;
    }

    public long getCurrentNotificationID() {
        return notificationID;
    }

    public LiveData<FetchStatus> getObservableFetchStatus() {
        return fetchStatus;
    }

    public void setFetchStatus(FetchStatus fetchStatus) {
        this.fetchStatus.postValue(fetchStatus);
    }

    public void setCustomList(CustomList list) {
        this.customList.postValue(list);
    }

    public LiveData<CustomList> getObservableCustomList() {
        return customList;
    }


    // rxjava
    public Observable<ArrayList<Notification>> getObservableFollowNotifications(String email, String token) {
        return Observable.create(emitter->{
            Response response = null;
            ArrayList<Notification> tempResult = new ArrayList<>();

            try {
                // build httpurl and request for remote db
                final String dbFunctionName = "fn_select_notifications";
                HttpUrl httpUrl = HttpUtilities.buildHttpURL(dbFunctionName);
                final OkHttpClient httpClient = OkHttpSingleton.getClient();
                RequestBody requestBody = new FormBody.Builder()
                        .add("owner_email", email)
                        .add("notification_type", FOLLOW_REQUEST_NOTIFICATION_TYPE)
                        .build();
                Request request = HttpUtilities.buildPostgresPOSTrequest(httpUrl, requestBody, token);

                // executing request
                response = httpClient.newCall(request).execute();

                // check responses
                if (response.isSuccessful()) {
                    String responseData = response.body().string();

                    // if response contains valid data
                    if ( ! responseData.equals("null")) {
                        JSONArray jsonArray = new JSONArray(responseData);

                        for(int i=0; i<jsonArray.length(); i++) {
                            FollowRequestNotification notification = new FollowRequestNotification();
                            JSONObject jsonDBobj = jsonArray.getJSONObject(i);
                            User sender = new User();
                            sender.setFirstName(jsonDBobj.getString("sender_fname"));
                            sender.setLastName(jsonDBobj.getString("sender_lname"));
                            sender.setUsername(jsonDBobj.getString("sender_username"));
                            sender.setProfilePictureURL(remoteConfigServer.getCloudinaryDownloadBaseUrl() + jsonDBobj.getString("profile_picture_path"));
                            notification.setId(jsonDBobj.getLong("Id_Notification"));
                            notification.setIsNew(true);
                            notification.setSender(sender);
                            notification.setDateInMillis(jsonDBobj.getLong("Date_In_Millis"));

                            tempResult.add(notification);
                        }// for

                        // once finished set result
                        emitter.onNext(tempResult);
                        emitter.onComplete();
                    }
                    // if the response is null (no notifications)
                    else emitter.onError(new Exception("no notifications"));
                }
                // if the response is unsuccesfull
                else emitter.onError(new Exception("response unsuccesufull"));
            }
            catch (ConnectException ce) {
                emitter.onError(ce);
            }
            catch (Exception e) {
                emitter.onError(e);
            } finally {
                if (response != null) {
                    response.close();
                }
            }
        });
    }

    public Observable<ArrayList<Notification>> getObservableLikeNotifications(String email, String token) {
        return Observable.create(emitter->{
            Response response = null;
            ArrayList<Notification> tempResult = new ArrayList<>();

            try {
                // build httpurl and request for remote db
                final String dbFunctionName = "fn_select_notifications";
                HttpUrl httpUrl = HttpUtilities.buildHttpURL(dbFunctionName);
                final OkHttpClient httpClient = OkHttpSingleton.getClient();
                RequestBody requestBody = new FormBody.Builder()
                        .add("owner_email", email)
                        .add("notification_type", POST_LIKED_NOTIFICATION_TYPE)
                        .build();
                Request request = HttpUtilities.buildPostgresPOSTrequest(httpUrl, requestBody, token);

                // performing request
                response = httpClient.newCall(request).execute();

                // check responses
                if (response.isSuccessful()) {
                    String responseData = response.body().string();

                    // if response contains valid data
                    if ( ! responseData.equals("null")) {
                        JSONArray jsonArray = new JSONArray(responseData);

                        for (int i = 0; i < jsonArray.length(); i++) {
                            PostLikedNotification notification = new PostLikedNotification();
                            JSONObject jsonDBobj = jsonArray.getJSONObject(i);
                            User sender = new User();
                            sender.setFirstName(jsonDBobj.getString("sender_fname"));
                            sender.setLastName(jsonDBobj.getString("sender_lname"));
                            sender.setUsername(jsonDBobj.getString("sender_username"));
                            sender.setProfilePictureURL(remoteConfigServer.getCloudinaryDownloadBaseUrl() + jsonDBobj.getString("profile_picture_path"));
                            notification.setId(jsonDBobj.getLong("Id_Notification"));
                            notification.setIsNew(true);
                            notification.setSender(sender);
                            notification.setPostId(jsonDBobj.getLong("fk_Post"));
                            notification.setDateInMillis(jsonDBobj.getLong("Date_In_Millis"));

                            tempResult.add(notification);
                        }// for

                        // once finished set result
                        emitter.onNext(tempResult);
                        emitter.onComplete();
                    }
                    // if the response is null (no notifications)
                    else emitter.onError(new Exception("no notifications"));
                }
                // if the response is unsuccesfull
                else emitter.onError(new Exception("response unsuccesufull"));
            }
            catch (ConnectException ce) {
                emitter.onError(ce);
            }
            catch (Exception e) {
                emitter.onError(e);
            } finally {
                if (response != null) {
                    response.close();
                }
            }
        });
    }

    public Observable<ArrayList<Notification>> getObservableCommentNotifications(String email, String token) {
        return Observable.create(emitter->{
            Response response = null;
            ArrayList<Notification> tempResult = new ArrayList<>();

            try {
                // build httpurl and request for remote db
                final String dbFunctionName = "fn_select_notifications";
                HttpUrl httpUrl = HttpUtilities.buildHttpURL(dbFunctionName);
                final OkHttpClient httpClient = OkHttpSingleton.getClient();
                RequestBody requestBody = new FormBody.Builder()
                        .add("owner_email", email)
                        .add("notification_type", POST_COMMENTED_NOTIFICATION_TYPE)
                        .build();
                Request request = HttpUtilities.buildPostgresPOSTrequest(httpUrl, requestBody, token);

                // performing request
                response = httpClient.newCall(request).execute();

                // check responses
                if (response.isSuccessful()) {
                    String responseData = response.body().string();

                    // if response contains valid data
                    if ( ! responseData.equals("null")) {
                        JSONArray jsonArray = new JSONArray(responseData);

                        for(int i=0; i<jsonArray.length(); i++) {
                            PostCommentedNotification notification = new PostCommentedNotification();
                            JSONObject jsonDBobj = jsonArray.getJSONObject(i);
                            User sender = new User();
                            sender.setFirstName(jsonDBobj.getString("sender_fname"));
                            sender.setLastName(jsonDBobj.getString("sender_lname"));
                            sender.setUsername(jsonDBobj.getString("sender_username"));
                            sender.setProfilePictureURL(remoteConfigServer.getCloudinaryDownloadBaseUrl() + jsonDBobj.getString("profile_picture_path"));
                            notification.setId(jsonDBobj.getLong("Id_Notification"));
                            notification.setIsNew(true);
                            notification.setSender(sender);
                            notification.setPostId(jsonDBobj.getLong("fk_Post"));
                            notification.setDateInMillis(jsonDBobj.getLong("Date_In_Millis"));

                            tempResult.add(notification);
                        }// for

                        // once finished set result
                        emitter.onNext(tempResult);
                        emitter.onComplete();
                    }
                    // if the response is null (no notifications)
                    else emitter.onError(new Exception("no notifications"));
                }
                // if the response is unsuccesfull
                else emitter.onError(new Exception("response unsuccesufull"));
            }
            catch (ConnectException ce) {
                emitter.onError(ce);
            }
            catch (Exception e) {
                emitter.onError(e);
            } finally {
                if (response != null) {
                    response.close();
                }
            }
        });
    }

    public Observable<ArrayList<Notification>> getObservableListRecommendedNotifications(String email, String token) {
        return Observable.create(emitter->{
            Response response = null;
            ArrayList<Notification> tempResult = new ArrayList<>();

            try {
                // build httpurl and request for remote db
                final String dbFunctionName = "fn_select_notifications";
                HttpUrl httpUrl = HttpUtilities.buildHttpURL(dbFunctionName);
                final OkHttpClient httpClient = OkHttpSingleton.getClient();
                RequestBody requestBody = new FormBody.Builder()
                        .add("owner_email", email)
                        .add("notification_type", LIST_RECOMMENDED_NOTIFICATION_TYPE)
                        .build();
                Request request = HttpUtilities.buildPostgresPOSTrequest(httpUrl, requestBody, token);

                // performing request
                response = httpClient.newCall(request).execute();

                // check responses
                if (response.isSuccessful()) {
                    String responseData = response.body().string();

                    // if response contains valid data
                    if ( ! responseData.equals("null")) {
                        JSONArray jsonArray = new JSONArray(responseData);

                        for(int i=0; i<jsonArray.length(); i++) {
                            ListRecommendedNotification notification = new ListRecommendedNotification();
                            JSONObject jsonDBobj = jsonArray.getJSONObject(i);
                            User sender = new User();
                            sender.setFirstName(jsonDBobj.getString("sender_fname"));
                            sender.setLastName(jsonDBobj.getString("sender_lname"));
                            sender.setUsername(jsonDBobj.getString("sender_username"));
                            sender.setProfilePictureURL(remoteConfigServer.getCloudinaryDownloadBaseUrl() + jsonDBobj.getString("profile_picture_path"));
                            notification.setId(jsonDBobj.getLong("Id_Notification"));
                            notification.setIsNew(true);
                            notification.setSender(sender);
                            notification.getCustomList().setName(jsonDBobj.getString("list_name"));
                            notification.getCustomList().setDescription(jsonDBobj.getString("list_description"));
                            notification.getCustomList().setOwner(sender);
                            notification.setDateInMillis(jsonDBobj.getLong("Date_In_Millis"));

                            tempResult.add(notification);
                        }// for

                        // once finished set result
                        emitter.onNext(tempResult);
                        emitter.onComplete();
                    }
                    // if the response is null (no notifications)
                    else emitter.onError(new Exception("no notifications"));
                }
                // if the response is unsuccesfull
                else emitter.onError(new Exception("response unsuccesufull"));
            }
            catch (ConnectException ce) {
                emitter.onError(ce);
            }
            catch (Exception e) {
                emitter.onError(e);
            } finally {
                if (response != null) {
                    response.close();
                }
            }
        });
    }

    public Observable<ArrayList<Notification>> getObservableSubscribedToListNotifications(String email, String token) {
        return Observable.create(emitter->{
            Response response = null;
            ArrayList<Notification> tempResult = new ArrayList<>();

            try {
                // build httpurl and request for remote db
                final String dbFunctionName = "fn_select_notifications";
                HttpUrl httpUrl = HttpUtilities.buildHttpURL(dbFunctionName);
                final OkHttpClient httpClient = OkHttpSingleton.getClient();
                RequestBody requestBody = new FormBody.Builder()
                        .add("owner_email", email)
                        .add("notification_type", SUBSCRIBED_TO_LIST_NOTIFICATION_TYPE)
                        .build();
                Request request = HttpUtilities.buildPostgresPOSTrequest(httpUrl, requestBody, token);

                // performing request
                response = httpClient.newCall(request).execute();

                // check responses
                if (response.isSuccessful()) {
                    String responseData = response.body().string();

                    // if response contains valid data
                    if ( ! responseData.equals("null")) {
                        JSONArray jsonArray = new JSONArray(responseData);

                        for(int i=0; i<jsonArray.length(); i++) {
                            SubscribedToListNotification notification = new SubscribedToListNotification();
                            JSONObject jsonDBobj = jsonArray.getJSONObject(i);
                            User sender = new User();
                            sender.setFirstName(jsonDBobj.getString("sender_fname"));
                            sender.setLastName(jsonDBobj.getString("sender_lname"));
                            sender.setUsername(jsonDBobj.getString("sender_username"));
                            sender.setProfilePictureURL(remoteConfigServer.getCloudinaryDownloadBaseUrl() + jsonDBobj.getString("profile_picture_path"));
                            notification.setId(jsonDBobj.getLong("Id_Notification"));
                            notification.setIsNew(true);
                            notification.setSender(sender);
                            notification.setListName(jsonDBobj.getString("list_name"));
                            notification.setDateInMillis(jsonDBobj.getLong("Date_In_Millis"));

                            tempResult.add(notification);
                        }// for

                        // once finished set result
                        emitter.onNext(tempResult);
                        emitter.onComplete();
                    }
                    // if the response is null (no notifications)
                    else emitter.onError(new Exception("no notifications"));
                }
                // if the response is unsuccesfull
                else emitter.onError(new Exception("response unsuccesufull"));
            }
            catch (ConnectException ce) {
                emitter.onError(ce);
            }
            catch (Exception e) {
                emitter.onError(e);
            } finally {
                if (response != null) {
                    response.close();
                }
            }
        });
    }


    /**
     * notes: notifications are ordered by date (DESC order)
     * @param email
     * @param token
     * @return
     */
    public Observable<ArrayList<Notification>> getObservableNotifications(String email, String token) {
        Observable<ArrayList<Notification>> followNotificationsObservable =
                getObservableFollowNotifications(email, token);

        Observable<ArrayList<Notification>> likeNotificationsObservable =
                getObservableLikeNotifications(email, token);


        Observable<ArrayList<Notification>> commentNotificationsObservable =
                getObservableCommentNotifications(email, token);

        Observable<ArrayList<Notification>> listRecommendedNotificationsObservable =
                getObservableListRecommendedNotifications(email, token);

        Observable<ArrayList<Notification>> subscribedToListNotificationsObservable =
                getObservableSubscribedToListNotifications(email, token);


        Observable<ArrayList<Notification>> combinedNotificationsObservable =
                Observable.combineLatest(
                        followNotificationsObservable.onErrorReturn(e -> new ArrayList<>()), // if some error occurs during pipeline,
                        likeNotificationsObservable.onErrorReturn(e-> new ArrayList<>()),    // don't block the chain,
                        commentNotificationsObservable.onErrorReturn(e-> new ArrayList<>()), // return an empty list instead
                        listRecommendedNotificationsObservable.onErrorReturn(e-> new ArrayList<>()),
                        subscribedToListNotificationsObservable.onErrorReturn(e-> new ArrayList<>()),
                        (followNotifications, likeNotifications, commentsNotifications, listRecommendedNotifications, subscribedToListNotifications) -> {
                            final ArrayList<Notification> combinedNotifications = new ArrayList<>();
                            combinedNotifications.addAll(followNotifications);
                            combinedNotifications.addAll(likeNotifications);
                            combinedNotifications.addAll(commentsNotifications);
                            combinedNotifications.addAll(listRecommendedNotifications);
                            combinedNotifications.addAll(subscribedToListNotifications);
                            return combinedNotifications;
                        }
                );


        Observable<ArrayList<Notification>> sortedCombinedNotificationsObservable =
                combinedNotificationsObservable
                        .map(this::sortNotificationsByDate);



        return sortedCombinedNotificationsObservable;
    }





    //-------------------------------------------------------------------------- MY METHODS

    public void fetchNotifications(User loggedUser, Context context) {
        if(loggedUser==null) return;

        Observable<ArrayList<Notification>> observableNotifications =
                getObservableNotifications(loggedUser.getEmail(), loggedUser.getAccessToken())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());

        notificationsSubscription.set(observableNotifications
                .subscribe(notifications -> {
                    if(notifications != null && notifications.size()>0) {
                        saveOnLocalDatabase(notifications, context);
                        setNotificationsList(notifications);
                        setNotificationsStatus(NotificationsStatus.NOTIFICATIONS_FETCHED);
                    }
                    else {
                        setNotificationsStatus(NotificationsStatus.NO_NOTIFICATIONS);
                    }
                }, throwable -> {
                    Log.d(TAG, "fetchNotifications: " + throwable.getMessage());
                }));


    }


    public void stopFetchingNotifications() {
//        if(notificationsSubscription!=null && (!notificationsSubscription.isDisposed()))
//            notificationsSubscription.dispose();
    }

    private void handleFetchErrors(Throwable e) {
//        Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
    }

    /**
     * sorting in DESC order
     * @param list
     * @return
     */
    ArrayList<Notification> sortNotificationsByDate(List<Notification> list) {
        ArrayList<Notification> sortedList = new ArrayList<>(); // create a copy for immutability principle
        sortedList.addAll(list);
        Collections.sort(sortedList);
        return sortedList;
    }

    /**
     * PRECONDITIONS:
     *  - notifications must be != null
     * @param notifications
     * @param context
     */
    public void saveOnLocalDatabase(@NotNull ArrayList<Notification> notifications, Context context) {
        NotificationDao notificationDao = getNotificationDao(context);

        new Thread(() -> notificationDao.insertAll(notifications)).start();
    }

    public void setNotificationsAsOld(ArrayList<Notification> notifications, Context context) {
        if(notifications==null) return;
        NotificationDao notificationDao = getNotificationDao(context);

        new Thread(() -> {
            for (Notification x: notifications) {
                x.setIsNew(false);
                try {
                    notificationDao.setNotificationAsOld(x);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            setNotificationsStatus(NotificationsStatus.ALL_NOTIFICATIONS_READ);
        }).start();

    }

    public void checkForNewNotifications(Context context) {
        NotificationDao notificationDao = getNotificationDao(context);

        new Thread(() -> {
            boolean gotNewNotifications = notificationDao.checkForNewNotifications();

            if(gotNewNotifications)
                setNotificationsStatus(NotificationsStatus.GOT_NEW_NOTIFICATIONS);
            else
                setNotificationsStatus(NotificationsStatus.NO_NOTIFICATIONS);
        }).start();


    }

    private NotificationDao getNotificationDao(Context context) {
        CinematesLocalDatabase localDatabase = CinematesLocalDatabase.getInstance(context);
        NotificationDao notificationDao = localDatabase.getNotificationDao();

        return notificationDao;
    }



    // deletion

    public void deleteNotificationFromRemoteDB(long id, User loggedUser) {
        // TODO: handle errors
        this.notificationID = id;

        Runnable task = createDeletionTask(id, loggedUser);
        Thread thread = new Thread(task);
        thread.start();
    }

    private Runnable createDeletionTask(long id, User loggedUser) {
        return () -> {
            Response response = null;

            try {
                // build httpurl and request for remote db
                final String dbFunctionName = "fn_delete_notification";
                HttpUrl httpUrl = HttpUtilities.buildHttpURL(dbFunctionName);
                final OkHttpClient httpClient = OkHttpSingleton.getClient();
                RequestBody requestBody = new FormBody.Builder()
                        .add("owner_email", loggedUser.getEmail())
                        .add("notification_id", String.valueOf(id))
                        .build();
                Request request = HttpUtilities.buildPostgresPOSTrequest(httpUrl, requestBody, loggedUser.getAccessToken());

                // executing request
                response = httpClient.newCall(request).execute();

                // check responses
                if (response.isSuccessful()) {
                    String responseData = response.body().string();

                    // if response contains valid data
                    if (responseData.equals("true")) {
                        setNotificationsStatus(NotificationsStatus.NOTIFICATION_DELETED);
                    }
                    // if the response is null (no notifications)
                    else setNotificationsStatus(NotificationsStatus.NOTIFICATION_NOT_DELETED);

                }
                // if the response is unsuccesfull
                else setNotificationsStatus(NotificationsStatus.NOTIFICATION_NOT_DELETED);
            }
            catch (ConnectException ce) {
                setNotificationsStatus(NotificationsStatus.NOTIFICATION_NOT_DELETED);
            }
            catch (Exception e) {
                setNotificationsStatus(NotificationsStatus.NOTIFICATION_NOT_DELETED);
            } finally {
                if (response != null) {
                    response.close();
                }
            }

        };
    }

    public void deleteNotificationFromLocalDB(long id, Context context) {
        NotificationDao notificationDao = getNotificationDao(context);
        new Thread(() -> notificationDao.deleteByID(id)).start();
    }


    // custom lists

    public void fetchCustomListMovies(String listOwnerUsername, String listName, String description, User loggedUser) {
        try {
            // build httpurl and request for remote db
            final String dbFunction = "fn_select_custom_list";
            HttpUrl httpUrl = HttpUtilities.buildHttpURL(dbFunction);
            final OkHttpClient httpClient = OkHttpSingleton.getClient();
            RequestBody requestBody = new FormBody.Builder()
                    .add("list_name", listName)
                    .add("owner_username", listOwnerUsername)
                    .build();
            Request request = HttpUtilities.buildPostgresPOSTrequest(httpUrl, requestBody, loggedUser.getAccessToken());

            // executing request
            Call call = httpClient.newCall(request);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    setFetchStatus(FetchStatus.FAILED);
                }

                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                    try {
                        if (response.isSuccessful()) {
                            String responseData = response.body().string();
                            ArrayList<Movie> moviesList;

                            if ( ! responseData.equals("null")) {
                                JSONArray jsonArray = new JSONArray(responseData);
                                moviesList = new ArrayList<>();

                                for(int i=0; i<jsonArray.length(); i++) {
                                    JSONObject jsonDBobj = jsonArray.getJSONObject(i);
                                    Movie movie = buildMovie(jsonDBobj);
                                    moviesList.add(movie);
                                }// for

                                CustomList customList = new CustomList();
                                customList.setMovies(moviesList);
                                customList.setName(listName);
                                customList.setDescription(description);
                                setCustomList(customList);
                                setFetchStatus(FetchStatus.SUCCESS);

                            } else {
                                // response equals false
                                CustomList customList = new CustomList();
                                customList.setMovies(new ArrayList<>());
                                customList.setName(listName);
                                customList.setDescription(description);
                                setCustomList(customList);
                                setFetchStatus(FetchStatus.FAILED);

                            }
                        } else {
                            // response unsuccessful
                            setFetchStatus(FetchStatus.FAILED);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        setFetchStatus(FetchStatus.FAILED);
                    }
                }
            });

        }  catch (Exception e) {
            e.printStackTrace();
            setFetchStatus(FetchStatus.FAILED);
        }
    }// end createFetchListTask()

    private Movie buildMovie(JSONObject jsonObject) throws JSONException {
        TheMovieDatabaseApi tmdb = TheMovieDatabaseApi.getInstance();
        int movieId = jsonObject.getInt("fk_movie");
        Movie movie = tmdb.getMoviesDetailsById(movieId);
        return movie;
    }




}// end NotificationsViewModel class