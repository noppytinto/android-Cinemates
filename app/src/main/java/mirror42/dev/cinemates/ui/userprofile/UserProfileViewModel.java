package mirror42.dev.cinemates.ui.userprofile;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import io.reactivex.rxjava3.core.Observable;
import mirror42.dev.cinemates.utilities.HttpUtilities;
import mirror42.dev.cinemates.utilities.OkHttpSingleton;
import mirror42.dev.cinemates.utilities.RemoteConfigServer;
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

    public enum FollowStatus {
        I_FOLLOW_HIM,
        I_DONT_FOLLOW_HIM,
        REQUEST_SENT_SUCCESSFULLY,
        MY_FOLLOW_REQUEST_IS_PENDING,
        MY_FOLLOW_REQUEST_IS_NOT_PENDING,
        HIS_FOLLOW_REQUEST_IS_PENDING,
        HIS_FOLLOW_REQUEST_IS_NOT_PENDING,
        HIS_FOLLOW_REQUEST_HAS_BEEN_ACCEPTED,
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



    //-------------------------------------------------------------------------- METHODS

    // I follow him status

    public void checkIfollowHim(String senderUsername, String receiverUsername, String token) {
        Runnable task_1 = createCheckIfollowHimTask(senderUsername, receiverUsername, token);
        Thread t_1 = new Thread(task_1);
        t_1.start();
    }

    private Runnable createCheckIfollowHimTask(String senderUsername, String receiverUsername, String token) {
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
        Thread t = new Thread(task);
        t.start();
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
    public Observable<Boolean> getIfollowHimStatus(String senderUsername, String receiverUsername, String token) {
        return Observable.create( emitter -> {
            Response response = null;

            try {
                // build httpurl and request for remote db
                final String dbFunction = "fn_check_is_friend";
                HttpUrl httpUrl = buildHttpUrl(dbFunction);
                final OkHttpClient httpClient = OkHttpSingleton.getClient();
                RequestBody requestBody = new FormBody.Builder()
                        .add("sender_username", senderUsername)
                        .add("receiver_username", receiverUsername)
                        .build();
                Request request = HttpUtilities.buildPostgresPOSTrequest(httpUrl, requestBody, token);

                // execute request
                response = httpClient.newCall(request).execute();

                // check response
                if (response.isSuccessful()) {
                    String responseData = response.body().string();

                    // if response contains valid data
                    if (responseData.equals("true") || responseData.equals("false")) {
                        boolean result = Boolean.parseBoolean(responseData);
                        emitter.onNext(result);
                        emitter.onComplete();
                    }
                    // if response contains no data
                    emitter.onError(new Exception("response is not a boolean value"));
                }
                // if response is unsuccessful
                emitter.onError(new Exception("response is unsuccessful"));

            } catch (Exception e) {
                emitter.onError(e);
            } finally {
                if (response != null) response.close();
            }
        });
    }




    // his follow status

    public void checkHeFollowsMe(String senderUsername, String receiverUsername, String token) {
        Runnable task = createcheckHeFollowMeTask(senderUsername, receiverUsername, token);
        Thread t = new Thread(task);
        t.start();
    }

    private Runnable createcheckHeFollowMeTask(String senderUsername, String receiverUsername, String token) {
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
        Thread t = new Thread(task);
        t.start();
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
        Thread t = new Thread(task);
        t.start();
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
        Thread t = new Thread(task);
        t.start();
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




    private HttpUrl buildHttpUrl(String dbFunctionName) {
        return new HttpUrl.Builder()
                .scheme("https")
                .host(remoteConfigServer.getAzureHostName())
                .addPathSegments(remoteConfigServer.getPostgrestPath())
                .addPathSegment(dbFunctionName)
                .build();
    }

}// end UserProfileViewModel class