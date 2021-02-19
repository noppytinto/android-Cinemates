package mirror42.dev.cinemates.ui.userprofile;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import mirror42.dev.cinemates.model.User;
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

public class FollowingViewModel extends ViewModel {
    private final String TAG = this.getClass().getSimpleName();
    private MutableLiveData<FetchStatus> fetchStatus;
    private MutableLiveData<ArrayList<User>> following;
    private RemoteConfigServer remoteConfigServer;
    private MutableLiveData<TaskStatus> taskStatus;


    //------------------------------------------------------------------------ CONSTRUCTORS

    public FollowingViewModel() {
        fetchStatus = new MutableLiveData<>(FetchStatus.IDLE);
        following = new MutableLiveData<>();
        remoteConfigServer = RemoteConfigServer.getInstance();
        taskStatus = new MutableLiveData<>(TaskStatus.IDLE);

    }




    //------------------------------------------------------------------------ GETTERS/SETTERS

    public LiveData<FetchStatus> getFetchStatus() {
        return fetchStatus;
    }

    public void setFetchStatus(FetchStatus fetchStatus) {
        this.fetchStatus.postValue(fetchStatus);
    }

    public LiveData<ArrayList<User>> getObservableFollowing() {
        return following;
    }

    public void setFollowing(ArrayList<User> users) {
        this.following.postValue(users);
    }

    public LiveData<TaskStatus> getObservableTaskStatus() {
        return taskStatus;
    }

    public void setTaskStatus(TaskStatus taskStatus) {
        this.taskStatus.postValue(taskStatus);
    }



    //------------------------------------------------------------------------ METHODS

    private User buildUser(JSONObject jsonObject) throws JSONException {
        User user = new User();
        user.setUsername(jsonObject.getString("Username"));
        user.setFirstName(jsonObject.getString("Name"));
        user.setLastName(jsonObject.getString("LastName"));
        user.setProfilePictureURL(remoteConfigServer.getCloudinaryDownloadBaseUrl() + jsonObject.getString("ProfileImage"));
        return user;
    }


    public void fetchFollowing(String targetUsername, User loggedUser) {
        try {
            // build httpurl and request for remote db
            final String dbFunction = "fn_select_following";
            //
            HttpUrl httpUrl = HttpUtilities.buildHttpURL(dbFunction);
            final OkHttpClient httpClient = OkHttpSingleton.getClient();
            RequestBody requestBody = new FormBody.Builder()
                    .add("target_username", targetUsername)
                    .add("requester_email", loggedUser.getEmail())
                    .build();
            Request request = HttpUtilities.buildPostgresPOSTrequest(httpUrl, requestBody, loggedUser.getAccessToken());

            // performing request
            Call call = httpClient.newCall(request);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    setFetchStatus(FetchStatus.FOLLOWING_FETCH_FAILED);
                }

                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                    try {
                        // check responses
                        if (response.isSuccessful()) {
                            String responseData = response.body().string();

                            // if response is true
                            if ( ! responseData.equals("null")) {
                                ArrayList<User> result = new ArrayList<>();
                                JSONArray jsonArray = new JSONArray(responseData);

                                for(int i=0; i<jsonArray.length(); i++) {
                                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                                    User user = buildUser(jsonObject);
                                    result.add(user);
                                }// for

                                setFollowing(result);
                                setFetchStatus(FetchStatus.FOLLOWING_FETCHED    );
                            }
                            else {
                                setFetchStatus(FetchStatus.NO_FOLLOWING);
                            }
                        } // if response is unsuccessful
                        else {
                            setFetchStatus(FetchStatus.FOLLOWING_FETCH_FAILED);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        setFetchStatus(FetchStatus.FOLLOWING_FETCH_FAILED);
                    }
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            setFetchStatus(FetchStatus.FOLLOWING_FETCH_FAILED);
        }

    }// end fetchFollowing()

    public void removeFollowing(String targetUsername, User loggedUser) {
        try {
            // build httpurl and request for remote db
            final String dbFunction = "fn_delete_following";
            //
            HttpUrl httpUrl = HttpUtilities.buildHttpURL(dbFunction);
            final OkHttpClient httpClient = OkHttpSingleton.getClient();
            RequestBody requestBody = new FormBody.Builder()
                    .add("target_username", targetUsername)
                    .add("requester_email", loggedUser.getEmail())
                    .build();
            Request request = HttpUtilities.buildPostgresPOSTrequest(httpUrl, requestBody, loggedUser.getAccessToken());

            // performing request
            Call call = httpClient.newCall(request);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    setTaskStatus(TaskStatus.FOLLOWING_REMOVED_FAIL);
                }

                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                    try {
                        // check responses
                        if (response.isSuccessful()) {
                            String responseData = response.body().string();

                            // if response is true
                            if (responseData.equals("true")) {
                                setTaskStatus(TaskStatus.FOLLOWING_REMOVED);
                            }
                            else {
                                setTaskStatus(TaskStatus.FOLLOWING_REMOVED_FAIL);
                            }
                        } // if response is unsuccessful
                        else {
                            setTaskStatus(TaskStatus.FOLLOWING_REMOVED_FAIL);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        setTaskStatus(TaskStatus.FOLLOWING_REMOVED_FAIL);
                    }
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            setTaskStatus(TaskStatus.FOLLOWING_REMOVED_FAIL);
        }

    }// end removeFollowing()


}// end FollowingViewModel class