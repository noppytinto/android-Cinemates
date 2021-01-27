package mirror42.dev.cinemates.ui.notification;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

import mirror42.dev.cinemates.model.User;
import mirror42.dev.cinemates.ui.notification.model.FollowRequestNotification;
import mirror42.dev.cinemates.ui.notification.model.PostCommentedNotification;
import mirror42.dev.cinemates.ui.notification.model.PostLikedNotification;
import mirror42.dev.cinemates.ui.notification.model.Notification;
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
    private MutableLiveData<ArrayList<Notification>> notificationsList;
    private MutableLiveData<FetchStatus> fetchStatus;
    private RemoteConfigServer remoteConfigServer;
    private ArrayList<Notification> tempResult;

    private final String FOLLOW_REQUEST_NOTIFICATION_TYPE = "FR";
    private final String POST_LIKED_NOTIFICATION_TYPE = "PL";
    private final String POST_COMMENTED_NOTIFICATION_TYPE = "PC";



    //-------------------------------------------------------------------------- CONSTRUCTORS

    public NotificationsViewModel() {
        notificationsList = new MutableLiveData<>();
        fetchStatus = new MutableLiveData<>(FetchStatus.IDLE);
        remoteConfigServer = RemoteConfigServer.getInstance();
        tempResult = new ArrayList<>();
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
        Runnable task = createFetchFollowNotificationsTask(email, token);
        Thread t = new Thread(task);
        t.start();
        try {
            t.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Runnable task2 = createFetchLikeNotificationsTask(email, token);
        Thread t2 = new Thread(task2);
        t2.start();

        try {
            t2.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Runnable task3 = createFetchCommentsNotificationsTask(email, token);
        Thread t3 = new Thread(task3);
        t3.start();

    }

    private Runnable createFetchFollowNotificationsTask(String email, String token) {
        return ()-> {
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
                Call call = httpClient.newCall(request);
                call.enqueue(new Callback() {
                    @Override
                    public void onFailure(@NotNull Call call, @NotNull IOException e) {
                        Log.d(TAG, "onFailure: ");
                        setFetchStatus(FetchStatus.FAILED);
                    }

                    @Override
                    public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                        try {
                            // check responses
                            if (response.isSuccessful()) {
                                String responseData = response.body().string();

                                // if response contains valid data
                                if ( ! responseData.equals("null")) {
                                    JSONArray jsonArray = new JSONArray(responseData);
                                    tempResult = new ArrayList<>();

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

                                    // once finished set result
//                                    Collections.reverse(tempResult);
//                                    setNotificationsList(tempResult);
//                                    setFetchStatus(FetchStatus.SUCCESS);

                                }
                                // if response contains no data
                                else {
//                                    setNotificationsList(null);
//                                    setFetchStatus(FetchStatus.EMPTY);
                                }
                            } // if response is unsuccessful
                            else {
//                                setNotificationsList(null);
//                                setFetchStatus(FetchStatus.FAILED);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
//                            setNotificationsList(null);
//                            setFetchStatus(FetchStatus.FAILED);
                        }
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
//                setNotificationsList(null);
//                setFetchStatus(FetchStatus.FAILED);
            }
        };
    }// end createFetchFollowNotificationsTask()

    private Runnable createFetchLikeNotificationsTask(String email, String token) {
        return ()-> {
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
                Call call = httpClient.newCall(request);
                call.enqueue(new Callback() {
                    @Override
                    public void onFailure(@NotNull Call call, @NotNull IOException e) {
                        Log.d(TAG, "onFailure: ");
                        setFetchStatus(FetchStatus.FAILED);
                    }

                    @Override
                    public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                        try {
                            // check responses
                            if (response.isSuccessful()) {
                                String responseData = response.body().string();

                                // if response contains valid data
                                if ( ! responseData.equals("null")) {
                                    JSONArray jsonArray = new JSONArray(responseData);

                                    for(int i=0; i<jsonArray.length(); i++) {
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
                                // if response contains no data
                                else {
//                                    setNotificationsList(null);
//                                    setFetchStatus(FetchStatus.EMPTY);
                                }
                            } // if response is unsuccessful
                            else {
//                                setNotificationsList(null);
//                                setFetchStatus(FetchStatus.FAILED);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
//                            setNotificationsList(null);
//                            setFetchStatus(FetchStatus.FAILED);
                        }
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
//                setNotificationsList(null);
//                setFetchStatus(FetchStatus.FAILED);
            }
        };
    }// end createFetchLikeNotificationsTask()

    private Runnable createFetchCommentsNotificationsTask(String email, String token) {
        return ()-> {
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
                Call call = httpClient.newCall(request);
                call.enqueue(new Callback() {
                    @Override
                    public void onFailure(@NotNull Call call, @NotNull IOException e) {
                        Log.d(TAG, "onFailure: ");
                        setFetchStatus(FetchStatus.FAILED);
                    }

                    @Override
                    public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                        try {
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

                                    // once finished set result
                                    Collections.sort(tempResult);
                                    Collections.reverse(tempResult);
                                    setNotificationsList(tempResult);
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
                        }
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
                setNotificationsList(null);
                setFetchStatus(FetchStatus.FAILED);
            }
        };
    }// end createFetchCommentsNotificationsTask()

}// end NotificationsViewModel class