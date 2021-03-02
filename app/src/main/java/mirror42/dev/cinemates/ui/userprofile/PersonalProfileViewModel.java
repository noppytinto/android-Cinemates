package mirror42.dev.cinemates.ui.userprofile;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

import mirror42.dev.cinemates.model.User;
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

public class PersonalProfileViewModel extends ViewModel {
    private final String TAG = this.getClass().getSimpleName();
    private MutableLiveData<User> user;
    private RemoteConfigServer remoteConfigServer;
    private FirebaseAuth mAuth;
    private FirebaseUser firebaseUser;
    private MutableLiveData<UploadStatus> uploadToCloudinaryStatus;
    private MutableLiveData<UploadStatus> changeImageToServerResult;
    private MutableLiveData<FetchStatus> fetchStatus;
    private String imageName;


    public enum UploadStatus {
        FAILED,
        SUCCESS,
        IDLE
    }


    //----------------------------------------------------------------- CONSTRUCTORS

    public PersonalProfileViewModel() {
        this.user = new MutableLiveData<>();
        uploadToCloudinaryStatus = new MutableLiveData<>(UploadStatus.IDLE);
        changeImageToServerResult = new MutableLiveData<>(UploadStatus.IDLE);
        remoteConfigServer = RemoteConfigServer.getInstance();
        mAuth = FirebaseAuth.getInstance();
        fetchStatus = new MutableLiveData<>(FetchStatus.IDLE);
    }


    //----------------------------------------------------------------- GETTERS/SETTERS

    public LiveData<UploadStatus> getUploadToCloudinaryStatus() {
        return uploadToCloudinaryStatus;
    }

    public void setUploadToCloudinaryStatus(UploadStatus uploadStatus) {
        this.uploadToCloudinaryStatus.postValue(uploadStatus);
    }

    public LiveData<UploadStatus> getUpdateImageToServerStatus() {
        return changeImageToServerResult;
    }

    public void setUpdateImageToServerStatus(UploadStatus changeImageToServerResult) {
        this.changeImageToServerResult.postValue(changeImageToServerResult);
    }

    public LiveData<FetchStatus> getFetchStatus() {
        return fetchStatus;
    }

    public void setFetchStatus(FetchStatus fetchStatus) {
        this.fetchStatus.postValue(fetchStatus);
    }

    public String getImageName() {
        return imageName;
    }

    //----------------------------------------------------------------- METHODS

    public void uploadImageToCloudinary(User user, Uri localImageUri, Context context) {
        try {
            Runnable task = createUploadImageToCloudinaryTask(user, localImageUri, context);
            ThreadManager t = ThreadManager.getInstance();
            t.runTaskInPool(task);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Runnable createUploadImageToCloudinaryTask(User user, Uri localImageUri, Context context) {
        // uploading promo image to the cloud
        return () -> {

//            try {
//                imageName = DocumentFile.fromSingleUri(context, localImageUri).getName();
//                imageName = imageName.toLowerCase();
//            } catch (Exception e) {
//                e.printStackTrace();
//            }

            String randomImageName = UUID.randomUUID().toString();
            imageName = randomImageName + ".png";

            try {
                String requestId = MediaManager.get().upload(localImageUri)
                        .callback(new UploadCallback() {
                            @Override
                            public void onStart(String requestId) {

                            }

                            @Override
                            public void onProgress(String requestId, long bytes, long totalBytes) {

                            }

                            @Override
                            public void onSuccess(String requestId, Map resultData) {

                                // getting name from uri

                                setUploadToCloudinaryStatus(UploadStatus.SUCCESS);
                                //changeProfileImageToServer(user, randomImageName);
                            }

                            @Override
                            public void onError(String requestId, ErrorInfo error) {
                                Log.v(TAG, "upload su cloudinary non riuscito");
                                setUploadToCloudinaryStatus(UploadStatus.FAILED);
                            }

                            @Override
                            public void onReschedule(String requestId, ErrorInfo error) {

                            }
                        })
                        .option("public_id", randomImageName)
                        .unsigned("qvrfptez")
                        .dispatch();


//                uploadResult = cloudinary.uploader().upload(url, ObjectUtils.emptyMap());
//                imageName = (String) uploadResult.get("public_id");
            } catch (Exception e) {
                e.printStackTrace();
                Log.v(TAG, "upload su cloudinary non riuscito");
                setUploadToCloudinaryStatus(UploadStatus.FAILED);
            }
        };
    }

    public void changeProfilePictureToServer(User user) {
        final OkHttpClient httpClient = OkHttpSingleton.getClient();
        final String dbFunction = "fn_update_profile_picture"; // vedi se è corretto
        try {
            HttpUrl httpUrl = HttpUtilities.buildHttpURL(dbFunction);
            RequestBody requestBody = new FormBody.Builder()
                    .add("email", user.getEmail())
                    .add("picture_path", imageName)
                    .build();
            Request request = HttpUtilities.buildPostgresPOSTrequest(httpUrl, requestBody, user.getAccessToken());

            Call call = httpClient.newCall(request);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    Log.v(TAG, "cambio immagine fallito");
                    setUpdateImageToServerStatus(UploadStatus.FAILED);
                }

                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {

                    try {
                        Log.v(TAG, "response :" + response);
                        if (response.isSuccessful()) {
                            String responseData = response.body().toString();
                            Log.v(TAG, "Tutto ok cambio immagine profilo avvenuto con successo");
                            user.setProfilePictureURL(remoteConfigServer.getCloudinaryDownloadBaseUrl() + imageName);
                            setUpdateImageToServerStatus(UploadStatus.SUCCESS);
                        } else {
                            Log.v(TAG, "cambio immagine profilo fallito");
                            setUploadToCloudinaryStatus(UploadStatus.FAILED);
                        }


                    } catch (Exception e) {
                        e.printStackTrace();
                        setUploadToCloudinaryStatus(UploadStatus.FAILED);
                    }

                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            setUploadToCloudinaryStatus(UploadStatus.FAILED);
        }
    }


}// UserProfileViewModel class
