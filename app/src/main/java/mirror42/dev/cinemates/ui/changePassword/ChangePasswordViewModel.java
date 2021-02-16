package mirror42.dev.cinemates.ui.changePassword;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import mirror42.dev.cinemates.api.mailAPI.JavaMailAPI;
import mirror42.dev.cinemates.model.User;
import mirror42.dev.cinemates.ui.resetPassword.ResetPasswordViewModel;
import mirror42.dev.cinemates.utilities.HttpUtilities;
import mirror42.dev.cinemates.utilities.MyUtilities;
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

public class ChangePasswordViewModel extends ViewModel {

    private final String TAG = this.getClass().getSimpleName();
    private MutableLiveData<ChangePasswordViewModel.ResetResult> resetResult;
    private RemoteConfigServer remoteConfigServer;

    public enum ResetResult {
        FAILED,
        SUCCESS,
        NONE
    }
    public ChangePasswordViewModel(){
        resetResult = new MutableLiveData<>(ResetResult.NONE);
        remoteConfigServer = RemoteConfigServer.getInstance();
    }

    // getter and setter
    public void postResetStatus(ResetResult resetResult) {
        this.resetResult.postValue(resetResult);
    }

    public LiveData<ResetResult> getResetStatus() {
        return resetResult;
    }


    public void changePassword(User userLogged, String password){
        final OkHttpClient httpClient = OkHttpSingleton.getClient();
        final String dbFunction = "fn_update_password";
        final String passwordHide = MyUtilities.SHA256encrypt(password);
        try{
            HttpUrl httpUrl = new HttpUrl.Builder()
                    .scheme("https")
                    .host(remoteConfigServer.getAzureHostName())
                    .addPathSegments(remoteConfigServer.getPostgrestPath())
                    .addPathSegment(dbFunction)
                    .build();
            RequestBody requestBody = new FormBody.Builder()
                    .add("mail",userLogged.getEmail())
                    .add("newpassword",passwordHide)
                    .add("typeupdate","CHANGE")
                    .build();
            Request request = HttpUtilities.buildPostgresPOSTrequest(httpUrl,requestBody,userLogged.getAccessToken());

            Call call = httpClient.newCall(request);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    Log.v(TAG,"cambio password fallito");
                    postResetStatus(ResetResult.FAILED);
                }

                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {

                    try{
                        Log.v(TAG,"response :" + response);
                        if(response.isSuccessful()) {
                            String responseData = response.body().toString();
                            Log.v(TAG,"cambio avvenuto");
                            updateToken(userLogged);
                        }else{
                            Log.v(TAG,"mail non trovata ");
                            postResetStatus(ResetResult.FAILED);
                        }


                    }catch(Exception e){
                        e.printStackTrace();
                        postResetStatus(ResetResult.FAILED);
                    }

                }
            });
        }catch(Exception e){
            e.printStackTrace();
            postResetStatus(ResetResult.FAILED);
        }
    }

    private void updateToken(User userLogged){
        final OkHttpClient httpClient = OkHttpSingleton.getClient();
        final String dbFunction = "fn_select_accesstoken";
        try{
            HttpUrl httpUrl = new HttpUrl.Builder()
                    .scheme("https")
                    .host(remoteConfigServer.getAzureHostName())
                    .addPathSegments(remoteConfigServer.getPostgrestPath())
                    .addPathSegment(dbFunction)
                    .build();
            RequestBody requestBody = new FormBody.Builder()
                    .add("mail",userLogged.getEmail())
                    .build();
            Request request = HttpUtilities.buildPostgresPOSTrequest(httpUrl,requestBody,remoteConfigServer.getGuestToken());

            Call call = httpClient.newCall(request);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    Log.v(TAG,"recupero nuovo access token fallito");
                    postResetStatus(ResetResult.FAILED);
                }

                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {

                    try{
                        Log.v(TAG,"response :" + response);
                        if(response.isSuccessful()) {

                            String responseData = response.body().string();

                            if( ! responseData.equals("null")) {
                                JSONObject jsonObject = new JSONObject(responseData);
                                String accessToken = jsonObject.getString("AccessToken");
                                Log.v(TAG, "access token recuperato  " + accessToken);
                                userLogged.setAccessToken(accessToken);
                                postResetStatus(ResetResult.SUCCESS);
                            }else{
                                Log.v(TAG,"errore recupero access token ");
                                postResetStatus(ResetResult.FAILED);
                            }
                        }else{
                            Log.v(TAG,"errore recupero access token ");
                            postResetStatus(ResetResult.FAILED);
                        }


                    }catch(Exception e){
                        e.printStackTrace();
                        postResetStatus(ResetResult.FAILED);
                    }

                }
            });
        }catch(Exception e){
            e.printStackTrace();
            postResetStatus(ResetResult.FAILED);
        }
    }



}
