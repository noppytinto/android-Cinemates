package mirror42.dev.cinemates.ui.notification;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.ConnectException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.reactivex.rxjava3.core.Observable;
import mirror42.dev.cinemates.model.User;
import mirror42.dev.cinemates.ui.notification.model.FollowRequestNotification;
import mirror42.dev.cinemates.ui.notification.model.Notification;
import mirror42.dev.cinemates.ui.notification.model.PostCommentedNotification;
import mirror42.dev.cinemates.ui.notification.model.PostLikedNotification;
import mirror42.dev.cinemates.utilities.HttpUtilities;
import mirror42.dev.cinemates.utilities.MyValues.FetchStatus;
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
    private MutableLiveData<ArrayList<Notification>> notificationsList;
    private MutableLiveData<FetchStatus> fetchStatus;
    private RemoteConfigServer remoteConfigServer;
    private ArrayList<Notification> tempRes;

    private final String FOLLOW_REQUEST_NOTIFICATION_TYPE = "FR";
    private final String POST_LIKED_NOTIFICATION_TYPE = "PL";
    private final String POST_COMMENTED_NOTIFICATION_TYPE = "PC";



    //-------------------------------------------------------------------------- CONSTRUCTORS

    public NotificationsViewModel() {
        notificationsList = new MutableLiveData<>();
        fetchStatus = new MutableLiveData<>(FetchStatus.IDLE);
        remoteConfigServer = RemoteConfigServer.getInstance();
        tempRes = new ArrayList<>();
    }



    //-------------------------------------------------------------------------- GETTERS/SETTERS

    public LiveData<ArrayList<Notification>> getNotificationsList() {
        return notificationsList;
    }

    public void setNotificationsList(ArrayList<Notification> notificationsList) {
        this.notificationsList.postValue(notificationsList);
    }

    public LiveData<FetchStatus> getFetchStatus() {
        return fetchStatus;
    }

    public void setFetchStatus(FetchStatus fetchStatus) {
        this.fetchStatus.postValue(fetchStatus);
    }



    //-------------------------------------------------------------------------- MY METHODS

    public void fetchNotifications(String email, String token) {
        Runnable task_1 = createFetchFollowNotificationsTask(email, token);
        Runnable task_2 = createFetchLikeNotificationsTask(email, token);
        Runnable task_3 = createFetchCommentsNotificationsTask(email, token);
        Thread t1 = new Thread(task_1);
        Thread t2 = new Thread(task_2);
        Thread t3 = new Thread(task_3);

        try {
            t1.start();
            t1.join();
            t2.start();
            t2.join();
            t3.start();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private Runnable createFetchFollowNotificationsTask(String email, String token) {
        return ()-> {
            Response response = null;

            try {
                // build httpurl and request for remote db
                final String dbFunction = "fn_select_notifications";

                //
                HttpUrl httpUrl = new HttpUrl.Builder()
                        .scheme("https")
                        .host(remoteConfigServer.getAzureHostName())
                        .addPathSegments(remoteConfigServer.getPostgrestPath())
                        .addPathSegment(dbFunction)
                        .build();
                final OkHttpClient httpClient = OkHttpSingleton.getClient();
                RequestBody requestBody = new FormBody.Builder()
                        .add("owner_email", email)
                        .add("notification_type", FOLLOW_REQUEST_NOTIFICATION_TYPE)
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
                        tempRes = new ArrayList<>();

                        for(int i=0; i<jsonArray.length(); i++) {
                            FollowRequestNotification followRequestNotification = new FollowRequestNotification();
                            JSONObject jsonDBobj = jsonArray.getJSONObject(i);
                            User sender = new User();
                            sender.setFirstName(jsonDBobj.getString("sender_fname"));
                            sender.setLastName(jsonDBobj.getString("sender_lname"));
                            sender.setUsername(jsonDBobj.getString("sender_username"));
                            sender.setProfilePicturePath(remoteConfigServer.getCloudinaryDownloadBaseUrl() + jsonDBobj.getString("profile_picture_path"));
                            followRequestNotification.setSender(sender);
                            followRequestNotification.setDateInMillis(jsonDBobj.getLong("Date_In_Millis"));

                            tempRes.add(followRequestNotification);
                        }// for
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (response != null) {
                    response.close();
                }
            }
        };
    }// end createFetchFollowNotificationsTask()

    private Runnable createFetchLikeNotificationsTask(String email, String token) {
        return ()-> {
            Response response = null;

            try {
                // build httpurl and request for remote db
                final String dbFunction = "fn_select_notifications";

                //
                HttpUrl httpUrl = new HttpUrl.Builder()
                        .scheme("https")
                        .host(remoteConfigServer.getAzureHostName())
                        .addPathSegments(remoteConfigServer.getPostgrestPath())
                        .addPathSegment(dbFunction)
                        .build();
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
                            PostLikedNotification postLikedNotification = new PostLikedNotification();
                            JSONObject jsonDBobj = jsonArray.getJSONObject(i);
                            User sender = new User();
                            sender.setFirstName(jsonDBobj.getString("sender_fname"));
                            sender.setLastName(jsonDBobj.getString("sender_lname"));
                            sender.setUsername(jsonDBobj.getString("sender_username"));
                            sender.setProfilePicturePath(remoteConfigServer.getCloudinaryDownloadBaseUrl() + jsonDBobj.getString("profile_picture_path"));
                            postLikedNotification.setSender(sender);
                            postLikedNotification.setPostId(jsonDBobj.getLong("fk_Post"));
                            postLikedNotification.setDateInMillis(jsonDBobj.getLong("Date_In_Millis"));

                            tempRes.add(postLikedNotification);
                        }// for
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (response != null) {
                    response.close();
                }
            }
        };
    }// end createFetchLikeNotificationsTask()

    private Runnable createFetchCommentsNotificationsTask(String email, String token) {
        return ()-> {
            Response response = null;

            try {
                // build httpurl and request for remote db
                final String dbFunction = "fn_select_notifications";

                //
                HttpUrl httpUrl = new HttpUrl.Builder()
                        .scheme("https")
                        .host(remoteConfigServer.getAzureHostName())
                        .addPathSegments(remoteConfigServer.getPostgrestPath())
                        .addPathSegment(dbFunction)
                        .build();
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
                            PostCommentedNotification postCommentedNotification = new PostCommentedNotification();
                            JSONObject jsonDBobj = jsonArray.getJSONObject(i);
                            User sender = new User();
                            sender.setFirstName(jsonDBobj.getString("sender_fname"));
                            sender.setLastName(jsonDBobj.getString("sender_lname"));
                            sender.setUsername(jsonDBobj.getString("sender_username"));
                            sender.setProfilePicturePath(remoteConfigServer.getCloudinaryDownloadBaseUrl() + jsonDBobj.getString("profile_picture_path"));
                            postCommentedNotification.setSender(sender);
                            postCommentedNotification.setPostId(jsonDBobj.getLong("fk_Post"));
                            postCommentedNotification.setDateInMillis(jsonDBobj.getLong("Date_In_Millis"));

                            tempRes.add(postCommentedNotification);
                        }// for

                        // once finished set result
                        Collections.sort(tempRes);
                        Collections.reverse(tempRes);
                        setNotificationsList(tempRes);
                        setFetchStatus(FetchStatus.SUCCESS);

                    }
                    // if response contains no data
                    else {
                        setNotificationsList(null);
                        setFetchStatus(FetchStatus.EMPTY);
                    }
                } // if response is unsuccessful
                else {
                    setNotificationsList(null);
                    setFetchStatus(FetchStatus.FAILED);
                }

            } catch (Exception e) {
                e.printStackTrace();
                setNotificationsList(null);
                setFetchStatus(FetchStatus.FAILED);
            } finally {
                if (response != null) {
                    response.close();
                }
            }
        };
    }// end createFetchCommentsNotificationsTask()

    private HttpUrl buildHttpUrl(String dbFunctionName) {
        return new HttpUrl.Builder()
                .scheme("https")
                .host(remoteConfigServer.getAzureHostName())
                .addPathSegments(remoteConfigServer.getPostgrestPath())
                .addPathSegment(dbFunctionName)
                .build();
    }



    // rxjava
    public Observable<ArrayList<Notification>> getFollowNotifications(String email, String token) {
        return Observable.create(emitter->{
            Response response = null;
            ArrayList<Notification> tempResult = new ArrayList<>();

            try {
                // build httpurl and request for remote db
                final String dbFunctionName = "fn_select_notifications";
                HttpUrl httpUrl = buildHttpUrl(dbFunctionName);
                final OkHttpClient httpClient = OkHttpSingleton.getClient();
                RequestBody requestBody = new FormBody.Builder()
                        .add("owner_email", email)
                        .add("notification_type", FOLLOW_REQUEST_NOTIFICATION_TYPE)
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
                            FollowRequestNotification followRequestNotification = new FollowRequestNotification();
                            JSONObject jsonDBobj = jsonArray.getJSONObject(i);
                            User sender = new User();
                            sender.setFirstName(jsonDBobj.getString("sender_fname"));
                            sender.setLastName(jsonDBobj.getString("sender_lname"));
                            sender.setUsername(jsonDBobj.getString("sender_username"));
                            sender.setProfilePicturePath(remoteConfigServer.getCloudinaryDownloadBaseUrl() + jsonDBobj.getString("profile_picture_path"));
                            followRequestNotification.setSender(sender);
                            followRequestNotification.setDateInMillis(jsonDBobj.getLong("Date_In_Millis"));

                            tempResult.add(followRequestNotification);
                        }// for
                    }

                    // once finished set result
                    emitter.onNext(tempResult);
                    emitter.onComplete();
                }
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

    public Observable<ArrayList<Notification>> getLikeNotifications(String email, String token) {
        return Observable.create(emitter->{
            Response response = null;
            ArrayList<Notification> tempResult = new ArrayList<>();

            try {
                // build httpurl and request for remote db
                final String dbFunctionName = "fn_select_notifications";
                HttpUrl httpUrl = buildHttpUrl(dbFunctionName);
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
                            PostLikedNotification postLikedNotification = new PostLikedNotification();
                            JSONObject jsonDBobj = jsonArray.getJSONObject(i);
                            User sender = new User();
                            sender.setFirstName(jsonDBobj.getString("sender_fname"));
                            sender.setLastName(jsonDBobj.getString("sender_lname"));
                            sender.setUsername(jsonDBobj.getString("sender_username"));
                            sender.setProfilePicturePath(remoteConfigServer.getCloudinaryDownloadBaseUrl() + jsonDBobj.getString("profile_picture_path"));
                            postLikedNotification.setSender(sender);
                            postLikedNotification.setPostId(jsonDBobj.getLong("fk_Post"));
                            postLikedNotification.setDateInMillis(jsonDBobj.getLong("Date_In_Millis"));

                            tempResult.add(postLikedNotification);
                        }// for
                    }

                    emitter.onNext(tempResult);
                    emitter.onComplete();
                }
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

    public Observable<ArrayList<Notification>> getCommentNotificaionts(String email, String token) {
        return Observable.create(emitter->{
            Response response = null;
            ArrayList<Notification> tempResult = new ArrayList<>();

            try {
                // build httpurl and request for remote db
                final String dbFunctionName = "fn_select_notifications";
                HttpUrl httpUrl = buildHttpUrl(dbFunctionName);
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
                            PostCommentedNotification postCommentedNotification = new PostCommentedNotification();
                            JSONObject jsonDBobj = jsonArray.getJSONObject(i);
                            User sender = new User();
                            sender.setFirstName(jsonDBobj.getString("sender_fname"));
                            sender.setLastName(jsonDBobj.getString("sender_lname"));
                            sender.setUsername(jsonDBobj.getString("sender_username"));
                            sender.setProfilePicturePath(remoteConfigServer.getCloudinaryDownloadBaseUrl() + jsonDBobj.getString("profile_picture_path"));
                            postCommentedNotification.setSender(sender);
                            postCommentedNotification.setPostId(jsonDBobj.getLong("fk_Post"));
                            postCommentedNotification.setDateInMillis(jsonDBobj.getLong("Date_In_Millis"));

                            tempResult.add(postCommentedNotification);
                        }// for
                    }

                    // once finished set result
                    emitter.onNext(tempResult);
                    emitter.onComplete();
                }
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
    public Observable<ArrayList<Notification>> getNotifications(String email, String token) {
        Observable<ArrayList<Notification>> followNotificationsObservable =
                getFollowNotifications(email, token);

        Observable<ArrayList<Notification>> likeNotificationsObservable =
                getLikeNotifications(email, token);


        Observable<ArrayList<Notification>> commentNotificationsObservable =
                getCommentNotificaionts(email, token);


        Observable<ArrayList<Notification>> combinedNotificationsObservable =
                Observable.combineLatest(
                        followNotificationsObservable, likeNotificationsObservable, commentNotificationsObservable,
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
                        .map(this::sortNotificationsList);

        return sortedCombinedNotificationsObservable;
    }


    /**
     * sorting in DESC order
     * @param list
     * @return
     */
    ArrayList<Notification> sortNotificationsList(List<Notification> list) {
        ArrayList<Notification> sortedList = new ArrayList<>();
        sortedList.addAll(list);
        Collections.sort(sortedList);
        return sortedList;
    }

}// end NotificationsViewModel class