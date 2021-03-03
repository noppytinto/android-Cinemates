package mirror42.dev.cinemates.ui.dialog;

import android.util.Log;

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

public class RecommendListDialogViewModel extends ViewModel {
    private final String TAG = getClass().getSimpleName();
    private MutableLiveData<FetchStatus> fetchStatus;
    private MutableLiveData<TaskStatus> taskStatus;
    private MutableLiveData<ArrayList<User>> followers;
    private RemoteConfigServer remoteConfigServer;





    //----------------------------------------------------------------------------------------- CONSTRUCTORS
    public RecommendListDialogViewModel() {
        followers = new MutableLiveData<>();
        fetchStatus = new MutableLiveData<>(FetchStatus.IDLE);
        taskStatus = new MutableLiveData<>(TaskStatus.IDLE);
        remoteConfigServer = RemoteConfigServer.getInstance();
    }

    //----------------------------------------------------------------------------------------- GETTERS/SETTERS

    public void setFetchStatus(FetchStatus fetchStatus) {
        this.fetchStatus.postValue(fetchStatus);
    }

    public LiveData<FetchStatus> getObservableFetchStatus() {
        return fetchStatus;
    }

    public void setTaskStatus(TaskStatus taskStatus) {
        this.taskStatus.postValue(taskStatus);
    }

    public LiveData<TaskStatus> getObservableTaskStatus() {
        return taskStatus;
    }



    public void setFollowers(ArrayList<User> followers) {
        this.followers.postValue(followers);
    }

    public LiveData<ArrayList<User>> getObservableFollowers() {
        return followers;
    }




    //----------------------------------------------------------------------------------------- METHODS

    public void fetchFollowers(User loggedUser) {
        final OkHttpClient httpClient = OkHttpSingleton.getClient();
        try {
            // generating url request
            final String dbFunction = "fn_select_followers";
            HttpUrl httpUrl = HttpUtilities.buildHttpURL(dbFunction);
            RequestBody requestBody = new FormBody.Builder()
                    .add("requester_email", loggedUser.getEmail())
                    .add("target_username", loggedUser.getUsername())
                    .build();
            Request request = HttpUtilities.buildPostgresPOSTrequest(httpUrl, requestBody, loggedUser.getAccessToken());

            // performing http request
            Call call = httpClient.newCall(request);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {
//                        postDownloadStatus(DownloadStatus.FAILED_OR_EMPTY);
                    Log.d(TAG, "onFailure: ");
                }

                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                    try {
                        if (response.isSuccessful()) {
                            String responseData = response.body().string();

                            if ( ! responseData.equals("null")) {
                                ArrayList<User> result = new ArrayList<>();
                                try {
                                    JSONArray jsonArray = new JSONArray(responseData);


                                    for(int i=0; i<jsonArray.length(); i++) {
                                        JSONObject jsonDBobj = jsonArray.getJSONObject(i);

                                        User user = buildUser(jsonDBobj);
                                        result.add(user);
                                    }// for
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }

                                setFollowers(result);
                                setFetchStatus(FetchStatus.SUCCESS);
                            }
                            else {
                                setFetchStatus(FetchStatus.FAILED);
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
       user.setExternalUser(jsonObject.getBoolean("ExternalAccount"));
        if(user.getIsExternalUser())
            user.setProfilePictureURL(jsonObject.getString("ProfileImage"));
        else
            user.setProfilePictureURL(remoteConfigServer.getCloudinaryDownloadBaseUrl() + jsonObject.getString("ProfileImage"));
        return user;
    }

    public void recommendList(String listName, User selectedUser, User loggedUser) {
        final OkHttpClient httpClient = OkHttpSingleton.getClient();
        try {
            // generating url request
            final String dbFunction = "fn_recommend_custom_list";
            HttpUrl httpUrl = HttpUtilities.buildHttpURL(dbFunction);
            RequestBody requestBody = new FormBody.Builder()
                    .add("list_name", listName)
                    .add("list_owner_email", loggedUser.getEmail())
                    .add("receiver_username", selectedUser.getUsername())
                    .build();
            Request request = HttpUtilities.buildPostgresPOSTrequest(httpUrl, requestBody, loggedUser.getAccessToken());

            // performing http request
            Call call = httpClient.newCall(request);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    setTaskStatus(TaskStatus.FAILED);
                }

                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                    try {
                        if (response.isSuccessful()) {
                            String responseData = response.body().string();

                            if (responseData.equals("true")) {
                                setTaskStatus(TaskStatus.SUCCESS);
                            }
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
}// end RecommendListDialogViewModel class
