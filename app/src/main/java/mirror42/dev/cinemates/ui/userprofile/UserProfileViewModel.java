package mirror42.dev.cinemates.ui.userprofile;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import mirror42.dev.cinemates.utilities.HttpUtilities;
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

public class UserProfileViewModel extends ViewModel {
    private final String TAG = getClass().getSimpleName();
    private MutableLiveData<TaskStatus> taskStatus;
    private RemoteConfigServer remoteConfigServer;
    private boolean isFriend;

    public enum TaskStatus {
        REQUEST_SENT_SUCCESSFULLY,
        MY_FOLLOW_REQUEST_PENDING,
        MY_FOLLOW_REQUEST_NOT_PENDING,
        HIS_FOLLOW_REQUEST_IS_PENDING,
        HIS_FOLLOW_REQUEST_IS_NOT_PENDING,
        HIS_FOLLOW_REQUEST_ACCEPTED,
        HE_FOLLOWS_YOU,
        HE_DOESNT_FOLLOw_YOU,
        FRIEND_CHECK_COMPLETE, FAILED, IDLE
    }

    //-------------------------------------------------------------------------- CONSTRUCTORS

    public UserProfileViewModel() {
        taskStatus = new MutableLiveData<>(TaskStatus.IDLE);
        remoteConfigServer = RemoteConfigServer.getInstance();
    }


    //-------------------------------------------------------------------------- GETTERS/SETTERS

    public LiveData<TaskStatus> getTaskStatus() {
        return taskStatus;
    }

    public void setTaskStatus(TaskStatus taskStatus) {
        this.taskStatus.postValue(taskStatus);
    }

    public boolean isFriend() {
        return isFriend;
    }

    public void setIsFriend(boolean friend) {
        isFriend = friend;
    }

//-------------------------------------------------------------------------- METHODS

    public void checkYouFollowHim(String senderUsername, String receiverUsername, String token) {
        Runnable task = createCheckIsFriendTask(senderUsername, receiverUsername, token);
        Thread t = new Thread(task);
        t.start();
    }

    private Runnable createCheckIsFriendTask(String senderUsername, String receiverUsername, String token) {
        return ()-> {
            try {
                // build httpurl and request for remote db
                final String dbFunction = "fn_check_is_friend";
                HttpUrl httpUrl = new HttpUrl.Builder()
                        .scheme("https")
                        .host(remoteConfigServer.getAzureHostName())
                        .addPathSegments(remoteConfigServer.getPostgrestPath())
                        .addPathSegment(dbFunction)
                        .build();
                final OkHttpClient httpClient = OkHttpSingleton.getClient();
                RequestBody requestBody = new FormBody.Builder()
                        .add("sender_username", senderUsername)
                        .add("receiver_username", receiverUsername)
                        .build();
                Request request = HttpUtilities.buildPostgresPOSTrequest(httpUrl, requestBody, token);

                // performing request
                Call call = httpClient.newCall(request);
                call.enqueue(new Callback() {
                    @Override
                    public void onFailure(@NotNull Call call, @NotNull IOException e) {
                        Log.d(TAG, "onFailure: ");
                        setTaskStatus(TaskStatus.FAILED);
                    }

                    @Override
                    public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                        try {
                            // check responses
                            if (response.isSuccessful()) {
                                String responseData = response.body().string();

                                // if response contains valid data
                                if ( ! responseData.equals("null")) {
                                    try {
                                        boolean res = Boolean.parseBoolean(responseData);
                                        setIsFriend(res);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                    setTaskStatus(TaskStatus.FRIEND_CHECK_COMPLETE);
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
        };
    }// end createCheckIsFriendTask()

    public void checkHeFollowsYou(String senderUsername, String receiverUsername, String token) {
        Runnable task = createcheckHeFollowYouTask(senderUsername, receiverUsername, token);
        Thread t = new Thread(task);
        t.start();
    }

    private Runnable createcheckHeFollowYouTask(String senderUsername, String receiverUsername, String token) {
        return ()-> {
            try {
                // build httpurl and request for remote db
                final String dbFunction = "fn_check_is_friend";
                HttpUrl httpUrl = new HttpUrl.Builder()
                        .scheme("https")
                        .host(remoteConfigServer.getAzureHostName())
                        .addPathSegments(remoteConfigServer.getPostgrestPath())
                        .addPathSegment(dbFunction)
                        .build();
                final OkHttpClient httpClient = OkHttpSingleton.getClient();
                RequestBody requestBody = new FormBody.Builder()
                        .add("sender_username", senderUsername)
                        .add("receiver_username", receiverUsername)
                        .build();
                Request request = HttpUtilities.buildPostgresPOSTrequest(httpUrl, requestBody, token);

                // performing request
                Call call = httpClient.newCall(request);
                call.enqueue(new Callback() {
                    @Override
                    public void onFailure(@NotNull Call call, @NotNull IOException e) {
                        Log.d(TAG, "onFailure: ");
                        setTaskStatus(TaskStatus.FAILED);
                    }

                    @Override
                    public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                        try {
                            // check responses
                            if (response.isSuccessful()) {
                                String responseData = response.body().string();

                                // if response contains valid data
                                if ( ! responseData.equals("null")) {
                                    boolean res = false;
                                    try {
                                        res = Boolean.parseBoolean(responseData);
                                        setIsFriend(res);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }

                                    if(res) {
                                        setTaskStatus(TaskStatus.HE_FOLLOWS_YOU);

                                    }
                                    else {
                                        setTaskStatus(TaskStatus.HE_DOESNT_FOLLOw_YOU);

                                    }
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
        };
    }// end createCheckIsFriendTask()

    public void checkMyFollowIsPending(String senderUsername, String receiverUsername, String token) {
        Runnable task = createCheckMyFollowIsPendingTask(senderUsername, receiverUsername, token);
        Thread t = new Thread(task);
        t.start();
    }

    private Runnable createCheckMyFollowIsPendingTask(String senderUsername, String receiverUsername, String token) {
        return ()-> {
            try {
                // build httpurl and request for remote db
                final String dbFunction = "fn_check_follow_request_sent";
                HttpUrl httpUrl = new HttpUrl.Builder()
                        .scheme("https")
                        .host(remoteConfigServer.getAzureHostName())
                        .addPathSegments(remoteConfigServer.getPostgrestPath())
                        .addPathSegment(dbFunction)
                        .build();
                final OkHttpClient httpClient = OkHttpSingleton.getClient();
                RequestBody requestBody = new FormBody.Builder()
                        .add("sender_username", senderUsername)
                        .add("receiver_username", receiverUsername)
                        .build();
                Request request = HttpUtilities.buildPostgresPOSTrequest(httpUrl, requestBody, token);

                // performing request
                Call call = httpClient.newCall(request);
                call.enqueue(new Callback() {
                    @Override
                    public void onFailure(@NotNull Call call, @NotNull IOException e) {
                        Log.d(TAG, "onFailure: ");
                        setTaskStatus(TaskStatus.FAILED);
                    }

                    @Override
                    public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                        try {
                            // check responses
                            if (response.isSuccessful()) {
                                String responseData = response.body().string();

                                // if response contains valid data
                                if ( ! responseData.equals("null")) {
                                    boolean res = false;
                                    try {
                                        res = Boolean.parseBoolean(responseData);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                    if(res) {
                                        setTaskStatus(TaskStatus.MY_FOLLOW_REQUEST_PENDING);
                                    }
                                    else {
                                        setTaskStatus(TaskStatus.MY_FOLLOW_REQUEST_NOT_PENDING);
                                    }
                                }
                                // if response contains no data
                                else {
//                                    setTaskStatus(TaskStatus.FAILED);
                                }
                            } // if response is unsuccessful
                            else {
//                                setTaskStatus(TaskStatus.FAILED);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
//                            setTaskStatus(TaskStatus.FAILED);
                        }
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
                setTaskStatus(TaskStatus.FAILED);
            }
        };
    }// end createFollowIsPendingTask()

    public void checkHisFollowIsPendingTask(String senderUsername, String receiverUsername, String token) {
        Runnable task = createCheckHisFollowIsPendingTask(senderUsername, receiverUsername, token);
        Thread t = new Thread(task);
        t.start();
    }

    private Runnable createCheckHisFollowIsPendingTask(String senderUsername, String receiverUsername, String token) {
        return ()-> {
            try {
                // build httpurl and request for remote db
                final String dbFunction = "fn_check_follow_request_sent";
                HttpUrl httpUrl = new HttpUrl.Builder()
                        .scheme("https")
                        .host(remoteConfigServer.getAzureHostName())
                        .addPathSegments(remoteConfigServer.getPostgrestPath())
                        .addPathSegment(dbFunction)
                        .build();
                final OkHttpClient httpClient = OkHttpSingleton.getClient();
                RequestBody requestBody = new FormBody.Builder()
                        .add("sender_username", senderUsername)
                        .add("receiver_username", receiverUsername)
                        .build();
                Request request = HttpUtilities.buildPostgresPOSTrequest(httpUrl, requestBody, token);

                // performing request
                Call call = httpClient.newCall(request);
                call.enqueue(new Callback() {
                    @Override
                    public void onFailure(@NotNull Call call, @NotNull IOException e) {
                        Log.d(TAG, "onFailure: ");
                        setTaskStatus(TaskStatus.FAILED);
                    }

                    @Override
                    public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                        try {
                            // check responses
                            if (response.isSuccessful()) {
                                String responseData = response.body().string();

                                // if response contains valid data
                                if ( ! responseData.equals("null")) {
                                    boolean res = false;
                                    try {
                                        res = Boolean.parseBoolean(responseData);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                    if(res) {
                                        setTaskStatus(TaskStatus.HIS_FOLLOW_REQUEST_IS_PENDING);
                                    }
                                    else {
                                        setTaskStatus(TaskStatus.HIS_FOLLOW_REQUEST_IS_NOT_PENDING);
                                    }
                                }
                                // if response contains no data
                                else {
//                                    setTaskStatus(TaskStatus.FAILED);
                                }
                            } // if response is unsuccessful
                            else {
//                                setTaskStatus(TaskStatus.FAILED);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
//                            setTaskStatus(TaskStatus.FAILED);
                        }
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
                setTaskStatus(TaskStatus.FAILED);
            }
        };
    }// end createFollowIsPendingTask()


    public void sendFollowRequest(String senderUsername, String receiverUsername, String token) {
        Runnable task = createSendFollowRequestTask(senderUsername, receiverUsername, token);
        Thread t = new Thread(task);
        t.start();
    }

    private Runnable createSendFollowRequestTask(String senderUsername, String receiverUsername, String token) {
        return ()-> {
            try {
                // build httpurl and request for remote db
                final String dbFunction = "fn_send_follow_request";
                HttpUrl httpUrl = new HttpUrl.Builder()
                        .scheme("https")
                        .host(remoteConfigServer.getAzureHostName())
                        .addPathSegments(remoteConfigServer.getPostgrestPath())
                        .addPathSegment(dbFunction)
                        .build();
                final OkHttpClient httpClient = OkHttpSingleton.getClient();
                RequestBody requestBody = new FormBody.Builder()
                        .add("sender_username", senderUsername)
                        .add("receiver_username", receiverUsername)
                        .build();
                Request request = HttpUtilities.buildPostgresPOSTrequest(httpUrl, requestBody, token);

                // performing request
                Call call = httpClient.newCall(request);
                call.enqueue(new Callback() {
                    @Override
                    public void onFailure(@NotNull Call call, @NotNull IOException e) {
                        Log.d(TAG, "onFailure: ");
                        setTaskStatus(TaskStatus.FAILED);
                    }

                    @Override
                    public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                        try {
                            // check responses
                            if (response.isSuccessful()) {
                                String responseData = response.body().string();

                                // if response contains valid data
                                if ( ! responseData.equals("null")) {
                                    boolean res = false;
                                    try {
                                        res = Boolean.parseBoolean(responseData);

                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                    if(res) {
                                        setTaskStatus(TaskStatus.REQUEST_SENT_SUCCESSFULLY);
                                    }
                                    else {
                                        setTaskStatus(TaskStatus.FAILED);
                                    }
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
        };
    }// end createSendFollowRequestTask()

    public void acceptFollowRequest(String senderUsername, String receiverUsername, String token) {
        Runnable task = createAcceptFollowRequestTask(senderUsername, receiverUsername, token);
        Thread t = new Thread(task);
        t.start();
    }

    private Runnable createAcceptFollowRequestTask(String senderUsername, String receiverUsername, String token) {
        return ()-> {
            try {
                // build httpurl and request for remote db
                final String dbFunction = "fn_accept_follow_request";
                HttpUrl httpUrl = new HttpUrl.Builder()
                        .scheme("https")
                        .host(remoteConfigServer.getAzureHostName())
                        .addPathSegments(remoteConfigServer.getPostgrestPath())
                        .addPathSegment(dbFunction)
                        .build();
                final OkHttpClient httpClient = OkHttpSingleton.getClient();
                RequestBody requestBody = new FormBody.Builder()
                        .add("sender_username", senderUsername)
                        .add("receiver_username", receiverUsername)
                        .build();
                Request request = HttpUtilities.buildPostgresPOSTrequest(httpUrl, requestBody, token);

                // performing request
                Call call = httpClient.newCall(request);
                call.enqueue(new Callback() {
                    @Override
                    public void onFailure(@NotNull Call call, @NotNull IOException e) {
                        Log.d(TAG, "onFailure: ");
                        setTaskStatus(TaskStatus.FAILED);
                    }

                    @Override
                    public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                        try {
                            // check responses
                            if (response.isSuccessful()) {
                                String responseData = response.body().string();

                                // if response contains valid data
                                if ( ! responseData.equals("null")) {
                                    setTaskStatus(TaskStatus.HIS_FOLLOW_REQUEST_ACCEPTED);
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
        };
    }// end createAcceptFollowRequestTask()


}// end UserProfileViewModel class