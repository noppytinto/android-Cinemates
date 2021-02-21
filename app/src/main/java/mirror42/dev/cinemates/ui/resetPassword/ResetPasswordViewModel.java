package mirror42.dev.cinemates.ui.resetPassword;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Random;

import mirror42.dev.cinemates.api.mailAPI.JavaMailAPI;
import mirror42.dev.cinemates.utilities.HttpUtilities;
import mirror42.dev.cinemates.utilities.MyUtilities;
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

public class ResetPasswordViewModel extends ViewModel {
    private final String TAG = this.getClass().getSimpleName();
    private MutableLiveData<ResetResult> resetResult;
    private RemoteConfigServer remoteConfigServer;
    private JavaMailAPI javaMailAPI;

    public enum ResetResult {
        FAILED,
        SUCCESS,
        NONE
    }

   public ResetPasswordViewModel(){
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


    public void resetPassword(String email){
        Random rand = new Random();
        int randomPassword = rand.nextInt(10000) + 132986;
        String password = Integer.toString(randomPassword);
        resetPasswordToServer(email, password);
    }

    private void resetPasswordToServer(String email, String password){
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
                    .add("mail",email)
                    .add("newpassword",passwordHide)
                    .add("typeupdate","RESET")
                    .build();
            Request request = HttpUtilities.buildPostgresPOSTrequest(httpUrl,requestBody,remoteConfigServer.getGuestToken());

            Call call = httpClient.newCall(request);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    Log.v(TAG,"Fallimento reset password");
                    postResetStatus(ResetResult.FAILED);
                }

                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {

                    try{
                        Log.v(TAG,"response :" + response);
                        if(response.isSuccessful()) {
                            String responseData = response.body().toString();
                            Log.v(TAG,"Tutto ok reset password a db");
                            sendEmail(email,  password);
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


    private void sendEmail(String email, String password){

        Runnable task = sendNewPasswordEmailTask(email, password);
        ThreadManager t = ThreadManager.getInstance();
        try {
            t.runTaskInPool(task);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private Runnable sendNewPasswordEmailTask(String email,String newPassword) {
        return () -> {
            javaMailAPI = new JavaMailAPI(remoteConfigServer.getGmailUsername(), remoteConfigServer.getGmailPass());
            // supported max 29<= sdk
            String[] recipients = {email};
            javaMailAPI.set_from("Cinemates");
            javaMailAPI.setBody("nuova password: " + newPassword);
            javaMailAPI.set_to(recipients);
            javaMailAPI.set_subject("Richiesta reset password");

            try {
                if (javaMailAPI.send()) {
                    postResetStatus(ResetResult.SUCCESS);
                } else {
                    postResetStatus(ResetResult.FAILED);
                }
            } catch (Exception e) {
                e.printStackTrace();
                postResetStatus(ResetResult.FAILED);
            }
        };
    }

}
