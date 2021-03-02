package mirror42.dev.cinemates.ui.login;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.io.IOException;

import mirror42.dev.cinemates.model.User;
import mirror42.dev.cinemates.ui.userprofile.PersonalProfileViewModel;
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
    private MutableLiveData<ResultOperation> registerUserResult;
    private MutableLiveData<ResultOperation> loginUserResult;
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
        registerUserResult = new MutableLiveData<>(ResultOperation.NONE);
        loginUserResult = new MutableLiveData<>(ResultOperation.NONE);
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


    public void postRegisterUserResult(ResultOperation registerUserResult) {
        this.registerUserResult.postValue(registerUserResult);
    }

    public LiveData<ResultOperation> getRegisterUserResult() {
        return registerUserResult;
    }

    // getter and setter
    public void postLoginUserResult(ResultOperation loginUserResult) {
        this.loginUserResult.postValue(loginUserResult);
    }

    public LiveData<ResultOperation> getLoginUserResult() {
        return loginUserResult;
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

    public void insertUserWithGoogleCredential(User user) {
        final OkHttpClient httpClient = OkHttpSingleton.getClient();
        final String dbFunction = "fn_register_new_user_google";
        try{
            HttpUrl httpUrl = new HttpUrl.Builder()
                    .scheme("https")
                    .host(remoteConfigServer.getAzureHostName())
                    .addPathSegments(remoteConfigServer.getPostgrestPath())
                    .addPathSegment(dbFunction)
                    .build();
            RequestBody requestBody = new FormBody.Builder()
                    .add("mail",user.getEmail())
                    .add("username",user.getUsername())
                    .add("firstname",user.getFirstName())
                    .add("lastname",user.getLastName())
                    .add("birthday",MyUtilities.convertStringDateToStringSqlDate(user.getBirthDate()))
                    .add("promo", String.valueOf(user.getPromo()))
                    .add("analytics",  String.valueOf(user.getAnalytics()))
                    .build();
            Request request = HttpUtilities.buildPostgresPOSTrequest(httpUrl,requestBody,remoteConfigServer.getGuestToken());

            Call call = httpClient.newCall(request);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    Log.v(TAG,"errore nel trovare l'utente");
                    postRegisterUserResult(ResultOperation.FAILED);
                }

                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {

                    try{
                        if(response.isSuccessful()) {
                            Log.d(TAG, "postgrest success");
                            try(ResponseBody responseBody = response.body()) {
                                boolean res = Boolean.parseBoolean(responseBody.string());
                                if(res) {
                                    Log.v(TAG,"Tutto ok utente inserito");
                                    selectUserInfo(user, OperationTypeWithGoogle.REGISTRATION);
                                }else
                                    postRegisterUserResult(ResultOperation.FAILED);

                            } catch (Exception e) {
                                Log.d(TAG, "error postgrest");
                                e.printStackTrace();
                            }

                        }else{
                            Log.v(TAG,"errore nel trovare l'utente");
                            postRegisterUserResult(ResultOperation.FAILED);
                        }


                    }catch(Exception e){
                        e.printStackTrace();
                        postRegisterUserResult(ResultOperation.FAILED);
                    }

                }
            });
        }catch(Exception e){
            e.printStackTrace();
            postRegisterUserResult(ResultOperation.FAILED);
        }
    }

    public void selectUserInfo(User user, OperationTypeWithGoogle op ){

        final OkHttpClient httpClient = OkHttpSingleton.getClient();

        try {
            // generating url request
            final String dbFunction = "login_google";
            HttpUrl httpUrl = HttpUtilities.buildHttpURL(dbFunction);
            RequestBody requestBody = new FormBody.Builder()
                    .add("mail", user.getEmail())
                    .build();
            Request request = HttpUtilities.buildPostgresPOSTrequest(httpUrl, requestBody, remoteConfigServer.getGuestToken());

            // performing http request
            Call call = httpClient.newCall(request);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    if (op == OperationTypeWithGoogle.REGISTRATION)
                        postRegisterUserResult(ResultOperation.FAILED);
                    else
                        postLoginUserResult(ResultOperation.FAILED);
                }

                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                    try {
                        if (response.isSuccessful()) {
                            String responseData = response.body().string();

                            if( ! responseData.equals("null")) {
                                JSONObject jsonObject = new JSONObject(responseData);

                                user.setAccessToken(jsonObject.getString("AccessToken"));
                                user.setFollowersCount(jsonObject.getInt("followers_count"));
                                user.setFollowingCount(jsonObject.getInt("following_count"));
                                user.setBirthDate(jsonObject.getString("BirthDate"));
                                user.setAnalytics(jsonObject.getBoolean("Analytics"));
                                user.setUsername(jsonObject.getString("Username"));
                                changeProfilePictureToServer(user);
                                if (op == OperationTypeWithGoogle.REGISTRATION)
                                    postRegisterUserResult(ResultOperation.SUCCESS);
                                else
                                    postLoginUserResult(ResultOperation.SUCCESS);
                            }
                        }
                        else{
                            if (op == OperationTypeWithGoogle.REGISTRATION)
                                postRegisterUserResult(ResultOperation.FAILED);
                            else
                                postLoginUserResult(ResultOperation.FAILED);
                        }


                    }
                    catch (Exception e) {
                        e.printStackTrace();
                        if (op == OperationTypeWithGoogle.REGISTRATION)
                            postRegisterUserResult(ResultOperation.FAILED);
                        else
                            postLoginUserResult(ResultOperation.FAILED);
                    }
                }
            });
        }catch(Exception e) {
            e.printStackTrace();
            if (op == OperationTypeWithGoogle.REGISTRATION)
                postRegisterUserResult(ResultOperation.FAILED);
            else
                postLoginUserResult(ResultOperation.FAILED);
        }
    }

    private void changeProfilePictureToServer(User user) {
        final OkHttpClient httpClient = OkHttpSingleton.getClient();
        final String dbFunction = "fn_update_profile_picture"; // vedi se è corretto
        try {
            HttpUrl httpUrl = HttpUtilities.buildHttpURL(dbFunction);
            RequestBody requestBody = new FormBody.Builder()
                    .add("email", user.getEmail())
                    .add("picture_path", user.getProfilePicturePath())
                    .build();
            Request request = HttpUtilities.buildPostgresPOSTrequest(httpUrl, requestBody, user.getAccessToken());

            Call call = httpClient.newCall(request);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    Log.v(TAG, "cambio immagine fallito");

                }

                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {

                    try {
                        Log.v(TAG, "response :" + response);
                        if (response.isSuccessful()) {
                            String responseData = response.body().toString();
                            Log.v(TAG, "Tutto ok cambio immagine profilo avvenuto con successo");
                        } else {
                            Log.v(TAG, "cambio immagine profilo fallito");
                        }


                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}// end class
