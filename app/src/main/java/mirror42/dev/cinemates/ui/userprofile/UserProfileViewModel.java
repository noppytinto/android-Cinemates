package mirror42.dev.cinemates.ui.userprofile;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import mirror42.dev.cinemates.model.User;
import mirror42.dev.cinemates.ui.reaction.CommentsViewModel.TaskStatus;
import mirror42.dev.cinemates.utilities.HttpUtilities;
import mirror42.dev.cinemates.utilities.MyValues.FetchStatus;
import mirror42.dev.cinemates.utilities.OkHttpSingleton;
import mirror42.dev.cinemates.utilities.RemoteConfigServer;
import mirror42.dev.cinemates.utilities.ThreadManager;
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
    private MutableLiveData<FollowStatus> myFollowStatus;
    private MutableLiveData<FollowStatus> hisFollowStatus;
    private MutableLiveData<FollowStatus> mySendFollowStatus;
    private MutableLiveData<FollowStatus> hisSendFollowStatus;
    private RemoteConfigServer remoteConfigServer;
    private MutableLiveData<TaskStatus> taskStatus;
    private MutableLiveData<FetchStatus> fetchStatus;
    private MutableLiveData<User> fetchedUser;

    public enum FollowStatus {
        I_FOLLOW_HIM,
        I_DONT_FOLLOW_HIM,
        REQUEST_SENT_SUCCESSFULLY,
        MY_FOLLOW_REQUEST_IS_PENDING,
        MY_FOLLOW_REQUEST_IS_NOT_PENDING,
        HIS_FOLLOW_REQUEST_IS_PENDING,
        HIS_FOLLOW_REQUEST_IS_NOT_PENDING,
        HIS_FOLLOW_REQUEST_HAS_BEEN_ACCEPTED,
        HIS_FOLLOW_REQUEST_HAS_BEEN_DECLINED,
        HE_FOLLOWS_ME,
        HE_DOESNT_FOLLOW_ME,
        FAILED,
        IDLE
    }



    //-------------------------------------------------------------------------- CONSTRUCTORS

    public UserProfileViewModel() {
        myFollowStatus = new MutableLiveData<>(FollowStatus.IDLE);
        hisFollowStatus = new MutableLiveData<>(FollowStatus.IDLE);
        mySendFollowStatus = new MutableLiveData<>(FollowStatus.IDLE);
        hisSendFollowStatus = new MutableLiveData<>(FollowStatus.IDLE);
        remoteConfigServer = RemoteConfigServer.getInstance();
        taskStatus = new MutableLiveData<>(TaskStatus.IDLE);
        fetchStatus = new MutableLiveData<>(FetchStatus.IDLE);
        fetchedUser = new MutableLiveData<>();

    }


    //-------------------------------------------------------------------------- GETTERS/SETTERS

    public LiveData<FollowStatus> getMyFollowStatus() {
        return myFollowStatus;
    }

    public void setMyFollowStatus(FollowStatus followStatus) {
        this.myFollowStatus.postValue(followStatus);
    }

    public LiveData<FollowStatus> getHisFollowStatus() {
        return hisFollowStatus;
    }

    public void setHisFollowStatus(FollowStatus followStatus) {
        this.hisFollowStatus.postValue(followStatus);
    }

    public LiveData<FollowStatus> getMySendFollowStatus() {
        return mySendFollowStatus;
    }

    public void setMySendFollowStatuss(FollowStatus followStatus) {
        this.mySendFollowStatus.postValue(followStatus);
    }

    public LiveData<FollowStatus> getHisSendFollowStatus() {
        return hisSendFollowStatus;
    }

    public void setHisSendFollowStatus(FollowStatus followStatus) {
        this.hisSendFollowStatus.postValue(followStatus);
    }

    public LiveData<FetchStatus> getFetchStatus() {
        return fetchStatus;
    }

    public void setFetchStatus(FetchStatus fetchStatus) {
        this.fetchStatus.postValue(fetchStatus);
    }

    public LiveData<TaskStatus> getObservableTaskStatus() {
        return taskStatus;
    }

    public void setTaskStatus(TaskStatus taskStatus) {
        this.taskStatus.postValue(taskStatus);
    }

    public LiveData<User> getObservableFetchedUser() {
        return fetchedUser;
    }

    public void setFetchedUser(User user) {
        this.fetchedUser.postValue(user);
    }




    //-------------------------------------------------------------------------- METHODS


    // fetch user profile data
    public void fetchUserProfileData(String username, User loggedUser) {
        try {
            // build httpurl and request for remote db
            final String dbFunction = "fn_select_user_profile_details";
            //
            HttpUrl httpUrl = HttpUtilities.buildHttpURL(dbFunction);
            final OkHttpClient httpClient = OkHttpSingleton.getClient();
            RequestBody requestBody = new FormBody.Builder()
                    .add("target_username", username)
                    .add("requester_email", loggedUser.getEmail())
                    .build();
            Request request = HttpUtilities.buildPostgresPOSTrequest(httpUrl, requestBody, loggedUser.getAccessToken());

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

                            // if response is true
                            if ( ! responseData.equals("null")) {
                                JSONObject jsonObject = new JSONObject(responseData);
                                User user = buildUser(jsonObject);
                                setFetchedUser(user);
                                setFetchStatus(FetchStatus.SUCCESS);
                            }
                            else {
                                setFetchStatus(FetchStatus.NOT_EXISTS);
                            }
                        } // if response is unsuccessful
                        else {
                            setFetchStatus(FetchStatus.FAILED);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        setFetchStatus(FetchStatus.FAILED);
                    }
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            setFetchStatus(FetchStatus.FAILED);
        }
    }


    private User buildUser(JSONObject jsonObject) throws JSONException {
        User user = new User();
        user.setUsername(jsonObject.getString("Username"));
        user.setFirstName(jsonObject.getString("Name"));
        user.setLastName(jsonObject.getString("LastName"));
        user.setBirthDate(jsonObject.getString("BirthDate"));
        user.setProfilePictureURL(remoteConfigServer.getCloudinaryDownloadBaseUrl() + jsonObject.getString("ProfileImage"));
        int followersCount = jsonObject.getInt("followers_count");
        int followingCount = jsonObject.getInt("following_count");
        user.setFollowersCount(followersCount);
        user.setFollowingCount(followingCount);
        return user;
    }















    // I follow him status

    public void checkIfollowHim(String senderUsername, String receiverUsername, String token) {
        Runnable task = createCheckIfollowHimTask(senderUsername, receiverUsername, token);
        ThreadManager t = ThreadManager.getInstance();
        try {
            t.runTaskInPool(task);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Runnable createCheckIfollowHimTask(String myUsername, String hisUsername, String token) {
        return ()-> {
            Response response = null;

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
                        .add("his_username", hisUsername)
                        .add("my_username", myUsername)
                        .build();
                Request request = HttpUtilities.buildPostgresPOSTrequest(httpUrl, requestBody, token);

                // performing request
                response = httpClient.newCall(request).execute();

                // check response
                if (response.isSuccessful()) {
                    String responseData = response.body().string();

                    // if response contains valid data
                    if (responseData.equals("true") || responseData.equals("false")) {
                        boolean iFollowHim = Boolean.parseBoolean(responseData);

                        if(iFollowHim) setMyFollowStatus(FollowStatus.I_FOLLOW_HIM);
                        else setMyFollowStatus(FollowStatus.I_DONT_FOLLOW_HIM);
                    }
                    // if response contains no data
                    else setMyFollowStatus(FollowStatus.FAILED);
                }
                // if response is unsuccessful
                else setMyFollowStatus(FollowStatus.FAILED);

            } catch (Exception e) {
                e.printStackTrace();
                setMyFollowStatus(FollowStatus.FAILED);
            } finally {
                if (response != null) {
                    response.close();
                }
            }
        };
    }// end createCheckIfollowHimTask()

    public void checkMyFollowIsPending(String senderUsername, String receiverUsername, String token) {
        Runnable task = createCheckMyFollowIsPendingTask(senderUsername, receiverUsername, token);
        ThreadManager t = ThreadManager.getInstance();
        try {
            t.runTaskInPool(task);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Runnable createCheckMyFollowIsPendingTask(String senderUsername, String receiverUsername, String token) {
        return ()-> {
            Response response = null;

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
                response = httpClient.newCall(request).execute();

                // check responses
                if (response.isSuccessful()) {
                    String responseData = response.body().string();

                    // if response contains valid data
                    if (responseData.equals("true") || responseData.equals("false")) {
                        boolean myfollowIsPending = Boolean.parseBoolean(responseData);

                        if(myfollowIsPending)
                            setMyFollowStatus(FollowStatus.MY_FOLLOW_REQUEST_IS_PENDING);
                        else
                            setMyFollowStatus(FollowStatus.MY_FOLLOW_REQUEST_IS_NOT_PENDING);

                    }
                    // if response contains no data
                    else setMyFollowStatus(FollowStatus.FAILED);

                } // if response is unsuccessful
                else setMyFollowStatus(FollowStatus.FAILED);

            } catch (Exception e) {
                e.printStackTrace();
                setMyFollowStatus(FollowStatus.FAILED);
            } finally {
                if (response != null) {
                    response.close();
                }
            }
        };
    }// end createCheckMyFollowIsPendingTask()


    // rxjava
//    public Observable<Boolean> getIfollowHimStatus(String senderUsername, String receiverUsername, String token) {
//        return Observable.create( emitter -> {
//            Response response = null;
//
//            try {
//                // build httpurl and request for remote db
//                final String dbFunction = "fn_check_is_friend";
//                HttpUrl httpUrl = buildHttpUrl(dbFunction);
//                final OkHttpClient httpClient = OkHttpSingleton.getClient();
//                RequestBody requestBody = new FormBody.Builder()
//                        .add("sender_username", senderUsername)
//                        .add("receiver_username", receiverUsername)
//                        .build();
//                Request request = HttpUtilities.buildPostgresPOSTrequest(httpUrl, requestBody, token);
//
//                // execute request
//                response = httpClient.newCall(request).execute();
//
//                // check response
//                if (response.isSuccessful()) {
//                    String responseData = response.body().string();
//
//                    // if response contains valid data
//                    if (responseData.equals("true") || responseData.equals("false")) {
//                        boolean result = Boolean.parseBoolean(responseData);
//                        emitter.onNext(result);
//                        emitter.onComplete();
//                    }
//                    // if response contains no data
//                    emitter.onError(new Exception("response is not a boolean value"));
//                }
//                // if response is unsuccessful
//                emitter.onError(new Exception("response is unsuccessful"));
//
//            } catch (Exception e) {
//                emitter.onError(e);
//            } finally {
//                if (response != null) response.close();
//            }
//        });
//    }




    // his follow status

    public void checkHeFollowsMe(String senderUsername, String receiverUsername, String token) {
        Runnable task = createcheckHeFollowMeTask(senderUsername, receiverUsername, token);
        ThreadManager t = ThreadManager.getInstance();
        try {
            t.runTaskInPool(task);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Runnable createcheckHeFollowMeTask(String myUsername, String hisUsername, String token) {
        return ()-> {
            Response response = null;

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
                        .add("his_username", hisUsername)
                        .add("my_username", myUsername)
                        .build();
                Request request = HttpUtilities.buildPostgresPOSTrequest(httpUrl, requestBody, token);

                // performing request
                response = httpClient.newCall(request).execute();

                // check responses
                if (response.isSuccessful()) {
                    String responseData = response.body().string();

                    // if response contains valid data
                    if (responseData.equals("true") || responseData.equals("false")) {
                        boolean heFollowsMe = Boolean.parseBoolean(responseData);

                        if(heFollowsMe)
                            setHisFollowStatus(FollowStatus.HE_FOLLOWS_ME);
                        else
                            setHisFollowStatus(FollowStatus.HE_DOESNT_FOLLOW_ME);
                    }
                    // if response contains no data
                    else {
                        setHisFollowStatus(FollowStatus.FAILED);
                    }
                } // if response is unsuccessful
                else {
                    setHisFollowStatus(FollowStatus.FAILED);
                }

            } catch (Exception e) {
                e.printStackTrace();
                setHisFollowStatus(FollowStatus.FAILED);
            } finally {
                if (response != null) {
                    response.close();
                }
            }
        };
    }// end createcheckHeFollowMeTask()

    public void checkHisFollowIsPending(String senderUsername, String receiverUsername, String token) {
        Runnable task = createCheckHisFollowIsPendingTask(senderUsername, receiverUsername, token);
        ThreadManager t = ThreadManager.getInstance();
        try {
            t.runTaskInPool(task);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Runnable createCheckHisFollowIsPendingTask(String senderUsername, String receiverUsername, String token) {
        return ()-> {
            Response response = null;

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
                response = httpClient.newCall(request).execute();

                // check response
                if (response.isSuccessful()) {
                    String responseData = response.body().string();

                    // if response contains valid data
                    if (responseData.equals("true") || responseData.equals("false")) {
                        boolean hisFollowIsPending = Boolean.parseBoolean(responseData);
                        if(hisFollowIsPending)
                            setHisFollowStatus(FollowStatus.HIS_FOLLOW_REQUEST_IS_PENDING);
                        else
                            setHisFollowStatus(FollowStatus.HIS_FOLLOW_REQUEST_IS_NOT_PENDING);

                    }
                    // if response contains no data
                    else setHisFollowStatus(FollowStatus.FAILED);
                }
                // if response is unsuccessful
                else setHisFollowStatus(FollowStatus.FAILED);

            } catch (Exception e) {
                e.printStackTrace();
                setHisFollowStatus(FollowStatus.FAILED);
            } finally {
                if (response != null) {
                    response.close();
                }
            }
        };
    }// end createCheckHisFollowIsPendingTask()



    // send/accept follow requests

    public void sendFollowRequest(String senderUsername, String receiverUsername, String token) {
        Runnable task = createSendFollowRequestTask(senderUsername, receiverUsername, token);
        ThreadManager t = ThreadManager.getInstance();
        try {
            t.runTaskInPool(task);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Runnable createSendFollowRequestTask(String senderUsername, String receiverUsername, String token) {
        return ()-> {
            Response response = null;

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
                response = httpClient.newCall(request).execute();

                // check responses
                if (response.isSuccessful()) {
                    String responseData = response.body().string();

                    // if response contains valid data
                    if (responseData.equals("true") || responseData.equals("false")) {
                        boolean requestSentSuccessfully = Boolean.parseBoolean(responseData);

                        if(requestSentSuccessfully)
                            setMySendFollowStatuss(FollowStatus.REQUEST_SENT_SUCCESSFULLY);
                        else
                            setMySendFollowStatuss(FollowStatus.FAILED);
                    }
                    // if response contains no data
                    else setMySendFollowStatuss(FollowStatus.FAILED);
                }
                // if response is unsuccessful
                else setMySendFollowStatuss(FollowStatus.FAILED);

            } catch (Exception e) {
                e.printStackTrace();
                setMySendFollowStatuss(FollowStatus.FAILED);
            } finally {
                if (response != null) {
                    response.close();
                }
            }
        };
    }// end createSendFollowRequestTask()

    public void acceptFollowRequest(String senderUsername, String receiverUsername, String token) {
        Runnable task = createAcceptFollowRequestTask(senderUsername, receiverUsername, token);
        ThreadManager t = ThreadManager.getInstance();
        try {
            t.runTaskInPool(task);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Runnable createAcceptFollowRequestTask(String senderUsername, String receiverUsername, String token) {
        return ()-> {
            Response response = null;

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
                response = httpClient.newCall(request).execute();

                // check responses
                if (response.isSuccessful()) {
                    String responseData = response.body().string();

                    // if response contains valid data
                    if (responseData.equals("true") || responseData.equals("false")) {
                        boolean requestSentSuccessfully = Boolean.parseBoolean(responseData);

                        if(requestSentSuccessfully)
                            setHisSendFollowStatus(FollowStatus.HIS_FOLLOW_REQUEST_HAS_BEEN_ACCEPTED);
                        else
                            setHisSendFollowStatus(FollowStatus.FAILED);
                    }
                    // if response contains no data
                    else setHisSendFollowStatus(FollowStatus.FAILED);
                }
                // if response is unsuccessful
                else setHisSendFollowStatus(FollowStatus.FAILED);
            } catch (Exception e) {
                e.printStackTrace();
                setHisSendFollowStatus(FollowStatus.FAILED);
            }  finally {
                if (response != null) {
                    response.close();
                }
            }
        };
    }// end createAcceptFollowRequestTask()


    public void declineFollowRequest(String senderUsername, String receiverUsername, String token) {
        Runnable task = createDeclineFollowRequestTask(senderUsername, receiverUsername, token);
        ThreadManager t = ThreadManager.getInstance();
        try {
            t.runTaskInPool(task);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Runnable createDeclineFollowRequestTask(String senderUsername, String receiverUsername, String token) {
        return ()-> {
            Response response = null;

            try {
                // build httpurl and request for remote db
                final String dbFunction = "fn_delete_follow_request";
                HttpUrl httpUrl = HttpUtilities.buildHttpURL(dbFunction);
                final OkHttpClient httpClient = OkHttpSingleton.getClient();
                RequestBody requestBody = new FormBody.Builder()
                        .add("sender_username", senderUsername)
                        .add("receiver_username", receiverUsername)
                        .build();
                Request request = HttpUtilities.buildPostgresPOSTrequest(httpUrl, requestBody, token);

                // performing request
                response = httpClient.newCall(request).execute();

                // check responses
                if (response.isSuccessful()) {
                    String responseData = response.body().string();

                    // if response contains valid data
                    if (responseData.equals("true") || responseData.equals("false")) {
                        boolean operationSuccessful = Boolean.parseBoolean(responseData);

                        if(operationSuccessful)
                            setHisSendFollowStatus(FollowStatus.HIS_FOLLOW_REQUEST_HAS_BEEN_DECLINED);
                        else
                            setHisSendFollowStatus(FollowStatus.FAILED);
                    }
                    // if response contains no data
                    else setHisSendFollowStatus(FollowStatus.FAILED);
                }
                // if response is unsuccessful
                else setHisSendFollowStatus(FollowStatus.FAILED);
            } catch (Exception e) {
                e.printStackTrace();
                setHisSendFollowStatus(FollowStatus.FAILED);
            }  finally {
                if (response != null) {
                    response.close();
                }
            }
        };
    }// end createAcceptFollowRequestTask()





}// end UserProfileViewModel class