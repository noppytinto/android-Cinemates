package mirror42.dev.cinemates.ui.login;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.work.Operation;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

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
import okhttp3.ResponseBody;

public class GoogleLoginViewModel extends ViewModel {

    private final String TAG = this.getClass().getSimpleName();
    private MutableLiveData<ResultOperation> userCollisionCheckResult;
    private MutableLiveData<ResultOperation> externalUser;
    private RemoteConfigServer remoteConfigServer;


    public enum ResultOperation{
        NONE,
        FAILED,
        SUCCESS
    }

    public enum OperationTypeWithGoogle{
        LOGIN,
        REGISTRATION
    }

    public GoogleLoginViewModel(){
        userCollisionCheckResult = new MutableLiveData<>(ResultOperation.NONE);
        externalUser = new MutableLiveData<>(ResultOperation.NONE);
        remoteConfigServer = RemoteConfigServer.getInstance();
    }



    // getter and setter
    public void postUserCollisionCheckResult(ResultOperation userCollisionCheckResult) {
        this.userCollisionCheckResult.postValue(userCollisionCheckResult);
    }

    public LiveData<ResultOperation> getUserCollisionCheckResult() {
        return userCollisionCheckResult;
    }

    public void postExternalUser(ResultOperation externalUser) {
        this.externalUser.postValue(externalUser);
    }

    public LiveData<ResultOperation> getExternalUser() {
        return externalUser;
    }


    //---------------------------------------------------My methods

    public void checkUserCollision(String username, String email) {
        HttpUrl httpUrl = null;
        final OkHttpClient httpClient = OkHttpSingleton.getClient();
        final String dbFunction = "fn_check_user_collision_ignore_active";
        try {
            httpUrl = new HttpUrl.Builder()
                    .scheme("https")
                    .host(remoteConfigServer.getAzureHostName())
                    .addPathSegments(remoteConfigServer.getPostgrestPath())
                    .addPathSegment(dbFunction)
                    .addQueryParameter("username", username)
                    .addQueryParameter("email", email)
                    .build();

            Request request = HttpUtilities.buildPostgresGETrequest(httpUrl, remoteConfigServer.getGuestToken());

            // performing call
            httpClient.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                    postUserCollisionCheckResult(ResultOperation.FAILED);
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    try (ResponseBody responseBody = response.body()) {
                        if (!response.isSuccessful()) {
                            postUserCollisionCheckResult(ResultOperation.FAILED);
                        }
                        try {
                            int responseCode = Integer.parseInt(responseBody.string());
                            switch (responseCode) {
                                case 1:
                                case 2:
                                case 3:
                                case 4:
                                    postUserCollisionCheckResult(ResultOperation.FAILED);
                                    break;
                                default:
                                    postUserCollisionCheckResult(ResultOperation.SUCCESS);
                            }
                        } catch (Exception e) {
                            postUserCollisionCheckResult(ResultOperation.FAILED);
                        }
                    } catch (Exception e) {
                        postUserCollisionCheckResult(ResultOperation.FAILED);
                    }
                }// end onResponse()
            });// end enquee()

        } catch (Exception e) {
            postUserCollisionCheckResult(ResultOperation.FAILED);
        }
    }// end checkUserCollision()

    public void checkExternalUser(String email){
        final OkHttpClient httpClient = OkHttpSingleton.getClient();
        final String dbFunction = "fn_check_external_user";
        try{
            HttpUrl httpUrl = new HttpUrl.Builder()
                    .scheme("https")
                    .host(remoteConfigServer.getAzureHostName())
                    .addPathSegments(remoteConfigServer.getPostgrestPath())
                    .addPathSegment(dbFunction)
                    .build();
            RequestBody requestBody = new FormBody.Builder()
                    .add("mail",email)
                    .build();
            Request request = HttpUtilities.buildPostgresPOSTrequest(httpUrl,requestBody,remoteConfigServer.getGuestToken());

            Call call = httpClient.newCall(request);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    Log.v(TAG,"errore nel trovare l'utente");
                    postExternalUser(ResultOperation.FAILED);
                }

                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {

                    try{
                        if(response.isSuccessful()) {
                            Log.d(TAG, "postgrest success");
                            try(ResponseBody responseBody = response.body()) {
                                boolean res = Boolean.parseBoolean(responseBody.string());
                                if(res) {
                                    Log.v(TAG,"Tutto ok utente trovato");
                                    postExternalUser(ResultOperation.SUCCESS);
                                }else
                                    postExternalUser(ResultOperation.FAILED);

                            } catch (Exception e) {
                                Log.d(TAG, "error postgrest");
                                e.printStackTrace();
                            }

                        }else{
                            Log.v(TAG,"errore nel trovare l'utente");
                            postExternalUser(ResultOperation.FAILED);
                        }


                    }catch(Exception e){
                        e.printStackTrace();
                        postExternalUser(ResultOperation.FAILED);
                    }

                }
            });
        }catch(Exception e){
            e.printStackTrace();
            postExternalUser(ResultOperation.FAILED);
        }
    }

}// end class
