package mirror42.dev.cinemates.ui.reaction;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import mirror42.dev.cinemates.model.User;
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

public class CommentsViewModel extends ViewModel {
    private final String TAG = getClass().getSimpleName();
    private MutableLiveData<TaskStatus> taskStatus;
    private RemoteConfigServer remoteConfigServer;
    private long reactionID;
    public enum TaskStatus {SUCCESS,FAILED,IDLE}





    //---------------------------------------------------------------------- CONSTRUCTORS

    public CommentsViewModel() {
        taskStatus = new MutableLiveData<>(TaskStatus.IDLE);
        remoteConfigServer = RemoteConfigServer.getInstance();
    }



    //---------------------------------------------------------------------- GETTERS/SETTERS

    public LiveData<TaskStatus> getObservableTaskStatus() {
        return taskStatus;
    }

    public void setFetchStatus(TaskStatus taskStatus) {
        this.taskStatus.postValue(taskStatus);
    }

    public TaskStatus getTaskStatus() {
        return taskStatus.getValue();
    };



    //---------------------------------------------------------------------- MY METHODS

    public void deleteComment(long commentID, User loggedUser) {
        //TODO: handle commentID and logged user errors

        Runnable task = createDeleteCommentTask(commentID, loggedUser);
        Thread t = new Thread(task);
        t.start();
    }


    /**
     * PRECONDITIONS:
     * - commentID must be >=0
     * - loggedUser must be not null
     * @param commentID
     * @param loggedUser
     * @return
     */
    private Runnable createDeleteCommentTask(long commentID, User loggedUser) {
        return ()-> {
            try {
                // build httpurl and request for remote db
                final String dbFunction = "fn_delete_reaction_comment_by_id";
                //
                HttpUrl httpUrl = HttpUtilities.buildHttpURL(dbFunction);
                final OkHttpClient httpClient = OkHttpSingleton.getClient();
                RequestBody requestBody = new FormBody.Builder()
                        .add("reaction_id", String.valueOf(commentID))
                        .add("email_reaction_owner", loggedUser.getEmail())
                        .build();
                Request request = HttpUtilities.buildPostgresPOSTrequest(httpUrl, requestBody, loggedUser.getAccessToken());

                // performing request
                Call call = httpClient.newCall(request);
                call.enqueue(new Callback() {
                    @Override
                    public void onFailure(@NotNull Call call, @NotNull IOException e) {
                        setFetchStatus(TaskStatus.FAILED);
                    }

                    @Override
                    public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                        try {
                            // check responses
                            if (response.isSuccessful()) {
                                String responseData = response.body().string();

                                // if response is true
                                if (responseData.equals("true")) {
                                    setFetchStatus(TaskStatus.SUCCESS);
                                }
                                else {
                                    setFetchStatus(TaskStatus.FAILED);
                                }
                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
            }
        };
    }







}// end CommentsViewModel class
