package mirror42.dev.cinemates.ui.notification;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.ConnectException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.SerialDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import mirror42.dev.cinemates.model.User;
import mirror42.dev.cinemates.model.notification.FollowRequestNotification;
import mirror42.dev.cinemates.model.notification.Notification;
import mirror42.dev.cinemates.model.notification.PostCommentedNotification;
import mirror42.dev.cinemates.model.notification.PostLikedNotification;
import mirror42.dev.cinemates.model.room.CinematesLocalDatabase;
import mirror42.dev.cinemates.dao.room.NotificationDao;
import mirror42.dev.cinemates.utilities.HttpUtilities;
import mirror42.dev.cinemates.utilities.OkHttpSingleton;
import mirror42.dev.cinemates.utilities.RemoteConfigServer;
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



    public enum NotificationsStatus {
        NOTIFICATIONS_FETCHED,
        GOT_NEW_NOTIFICATIONS,
        ALL_NOTIFICATIONS_READ,
        NO_NOTIFICATIONS,
        NOTIFICATION_DELETED,
        NOTIFICATION_NOT_DELETED,
        IDLE
    }

    private final String FOLLOW_REQUEST_NOTIFICATION_TYPE = "FR";   //(Friend Request)
    private final String POST_LIKED_NOTIFICATION_TYPE = "PL";       //(Post Liked)
    private final String POST_COMMENTED_NOTIFICATION_TYPE = "PC";   //(Post Commented)




    //-------------------------------------------------------------------------- CONSTRUCTORS

    public NotificationsViewModel() {
        notificationsList = new MutableLiveData<>();
        remoteConfigServer = RemoteConfigServer.getInstance();
        notificationsSubscription = new SerialDisposable();
        notificationsStatus = new MutableLiveData<>(NotificationsStatus.NO_NOTIFICATIONS);

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

    public Observable<ArrayList<Notification>> getObservableCommentNotificaionts(String email, String token) {
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
                getObservableCommentNotificaionts(email, token);


        Observable<ArrayList<Notification>> combinedNotificationsObservable =
                Observable.combineLatest(
                        followNotificationsObservable.onErrorReturn(e -> new ArrayList<>()), // if some error occurs during pipeline,
                        likeNotificationsObservable.onErrorReturn(e-> new ArrayList<>()),    // don't block the chain,
                        commentNotificationsObservable.onErrorReturn(e-> new ArrayList<>()), // return an empty list instead
                        (followNotifications, likeNotifications, commentsNotifications) -> {
                            final ArrayList<Notification> combinedNotifications = new ArrayList<>();
                            combinedNotifications.addAll(followNotifications);
                            combinedNotifications.addAll(likeNotifications);
                            combinedNotifications.addAll(commentsNotifications);
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

        Observable<ArrayList<Notification>> observableNnotifications =
                getObservableNotifications(loggedUser.getEmail(), loggedUser.getAccessToken())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());

        notificationsSubscription.set(observableNnotifications
                .subscribe(notifications -> {
                    if(notifications != null && notifications.size()>0) {
                        saveOnLocalDatabase(notifications, context);
                        setNotificationsList(notifications);
                        setNotificationsStatus(NotificationsStatus.NOTIFICATIONS_FETCHED);
                    }
                    else {
                        setNotificationsStatus(NotificationsStatus.NO_NOTIFICATIONS);
                    }
                }));


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









}// end NotificationsViewModel class