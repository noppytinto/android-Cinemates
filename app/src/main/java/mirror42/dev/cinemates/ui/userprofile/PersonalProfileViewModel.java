package mirror42.dev.cinemates.ui.userprofile;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Map;

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
    private MutableLiveData<ChangeImageResult> changeImageResult;
    private MutableLiveData<FetchStatus> fetchStatus;



    public enum ChangeImageResult {
        FAILED,
        SUCCESS,
        NONE
    }




    //----------------------------------------------------------------- CONSTRUCTORS

    public PersonalProfileViewModel() {
        this.user = new MutableLiveData<>();
        changeImageResult = new MutableLiveData<>(ChangeImageResult.NONE);
        remoteConfigServer = RemoteConfigServer.getInstance();
        mAuth = FirebaseAuth.getInstance();
        fetchStatus = new MutableLiveData<>(FetchStatus.IDLE);
    }



    //----------------------------------------------------------------- GETTERS/SETTERS

    public LiveData<ChangeImageResult> getResetStatus() {
        return changeImageResult;
    }

    public void postResetStatus(ChangeImageResult changeImageResult) {
        this.changeImageResult.postValue(changeImageResult);
    }

    public LiveData<FetchStatus> getFetchStatus() {
        return fetchStatus;
    }

    public void setFetchStatus(FetchStatus fetchStatus) {
        this.fetchStatus.postValue(fetchStatus);
    }



    //----------------------------------------------------------------- METHODS

    public void changeProfileImage(User user, String url){
        Runnable task = uploadImageAsync(user,url);
        ThreadManager t = ThreadManager.getInstance();
        try {
            t.runTaskInPool(task);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Runnable uploadImageAsync(User user, String url){
        // uploading promo image to the cloud
        return ()-> {
            String imageName = "-";
            Cloudinary cloudinary = new Cloudinary(remoteConfigServer.getCloudinaryUploadBaseUrl());
            Map<String, Object> uploadResult = null;
            try {
                uploadResult = cloudinary.uploader().upload(url, ObjectUtils.emptyMap());
            } catch (IOException e) {
                e.printStackTrace();
                Log.v(TAG,"upload su cloudinary non riuscito");
                postResetStatus(ChangeImageResult.FAILED);
            }
            imageName = (String) uploadResult.get("public_id");

            changeProfileImageToServer(user, imageName);
        };
    }

    private void changeProfileImageToServer(User user, String imageName){
        final OkHttpClient httpClient = OkHttpSingleton.getClient();
        final String dbFunction = "fn_update_profile_picture"; // vedi se è corretto
        try{
            HttpUrl httpUrl = HttpUtilities.buildHttpURL(dbFunction);
            RequestBody requestBody = new FormBody.Builder()
                    .add("email",user.getEmail())
                    .add("picture_path",imageName+".png")
                    .build();
            Request request = HttpUtilities.buildPostgresPOSTrequest(httpUrl,requestBody,user.getAccessToken());

            Call call = httpClient.newCall(request);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    Log.v(TAG,"cambio immagine fallito");
                    postResetStatus(ChangeImageResult.FAILED);
                }

                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {

                    try{
                        Log.v(TAG,"response :" + response);
                        if(response.isSuccessful()) {
                            String responseData = response.body().toString();
                            Log.v(TAG,"Tutto ok cambio immagine profilo avvenuto con successo");
                            user.setProfilePictureURL(remoteConfigServer.getCloudinaryDownloadBaseUrl() + imageName + ".png");
                            postResetStatus(ChangeImageResult.SUCCESS);
                        }else{
                            Log.v(TAG,"cambio immagine profilo fallito");
                            postResetStatus(ChangeImageResult.FAILED);
                        }


                    }catch(Exception e){
                        e.printStackTrace();
                        postResetStatus(ChangeImageResult.FAILED);
                    }

                }
            });
        }catch(Exception e){
            e.printStackTrace();
            postResetStatus(ChangeImageResult.FAILED);
        }
    }









}// UserProfileViewModel class
