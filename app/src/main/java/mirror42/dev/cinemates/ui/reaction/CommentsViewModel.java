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
import mirror42.dev.cinemates.utilities.ThreadManager;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static mirror42.dev.cinemates.ui.reaction.CommentsViewModel.TaskStatus.COMMENT_NOT_DELETED;
import static mirror42.dev.cinemates.ui.reaction.CommentsViewModel.TaskStatus.COMMNET_NOT_POSTED;

public class CommentsViewModel extends ViewModel {
    private final String TAG = getClass().getSimpleName();
    private MutableLiveData<TaskStatus> taskStatus;
    private RemoteConfigServer remoteConfigServer;
    private long reactionID;
    private String commentText;


    public enum TaskStatus {
        COMMENT_DELETED,
        COMMENT_NOT_DELETED,
        COMMENT_POSTED,
        COMMNET_NOT_POSTED,
        SUCCESS,
        FAILED,
        SUBSCRIBED,
        UNSUBSCRIBED,
        FAILED_SUBSCRIPTION,
        FAILED_UNSUBSCRIPTION,
        ALREADY_SUBSCRIBED,
        NOT_SUBSCRIBED,
        SUBSCRIPTION_CHECK_FAILED,
        COMMENT_ADDED,
        COMMENT_NOT_ADDED,
        COMMENT_FAILED,
        LIKE_ADDED,
        LIKE_NOT_ADDED,
        LIKE_ADDED_FAIL,
        LIKE_FAILED,
        LIKE_REMOVED,
        LIKE_NOT_REMOVED,
        LIKE_REMOVED_FAIL,
        FOLLOWER_REMOVED,
        FOLLOWING_REMOVED,
        FOLLOWER_REMOVED_FAIL,
        FOLLOWING_REMOVED_FAIL,
        IDLE
    }





    //---------------------------------------------------------------------- CONSTRUCTORS

    public CommentsViewModel() {
        taskStatus = new MutableLiveData<>(TaskStatus.IDLE);
        remoteConfigServer = RemoteConfigServer.getInstance();
    }



    //---------------------------------------------------------------------- GETTERS/SETTERS

    public LiveData<TaskStatus> getObservableTaskStatus() {
        return taskStatus;
    }

    public void setTaskStatus(TaskStatus taskStatus) {
        this.taskStatus.postValue(taskStatus);
    }

    public TaskStatus getTaskStatus() {
        return taskStatus.getValue();
    };

    public String getCommentText() {
        return commentText;
    }



    //---------------------------------------------------------------------- MY METHODS

    public void deleteComment(long commentID, User loggedUser) {
        //TODO: handle commentID and logged user errors

        Runnable task = createDeleteCommentTask(commentID, loggedUser);
        ThreadManager t = ThreadManager.getInstance();
        try {
            t.runTaskInPool(task);
        } catch (Exception e) {
            e.printStackTrace();
        }
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
                        setTaskStatus(COMMENT_NOT_DELETED);
                    }

                    @Override
                    public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                        try {
                            // check responses
                            if (response.isSuccessful()) {
                                String responseData = response.body().string();

                                // if response is true
                                if (responseData.equals("true")) {
                                    setTaskStatus(TaskStatus.COMMENT_DELETED);
                                }
                                else {
                                    setTaskStatus(COMMENT_NOT_DELETED);
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

    public void postComment(long postId, String commentText, User loggedUser) {
        this.commentText = commentText;
        Runnable task = creatPostCommentTask(postId, commentText, loggedUser);
        ThreadManager t = ThreadManager.getInstance();
        try {
            t.runTaskInPool(task);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Runnable creatPostCommentTask(long postId, String commentText, User loggedUser) {
        return ()-> {
            try {
                // build httpurl and request for remote db
                final String dbFunction = "fn_add_reaction_comment";
                //
                HttpUrl httpUrl = HttpUtilities.buildHttpURL(dbFunction);
                final OkHttpClient httpClient = OkHttpSingleton.getClient();
                RequestBody requestBody = new FormBody.Builder()
                        .add("target_post_id", String.valueOf(postId))
                        .add("email_reaction_owner", loggedUser.getEmail())
                        .add("comment_text", commentText)
                        .build();
                Request request = HttpUtilities.buildPostgresPOSTrequest(httpUrl, requestBody, loggedUser.getAccessToken());

                // performing request
                Call call = httpClient.newCall(request);
                call.enqueue(new Callback() {
                    @Override
                    public void onFailure(@NotNull Call call, @NotNull IOException e) {
                        setTaskStatus(COMMNET_NOT_POSTED);
                    }

                    @Override
                    public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                        try {
                            // check responses
                            if (response.isSuccessful()) {
                                String responseData = response.body().string();

                                // if response is true
                                if (responseData.equals("true")) {
                                    setTaskStatus(TaskStatus.COMMENT_POSTED);
                                }
                                else {
                                }
                            } // if response is unsuccessful
                            else {
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
