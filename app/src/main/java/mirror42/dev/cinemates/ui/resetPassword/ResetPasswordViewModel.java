package mirror42.dev.cinemates.ui.resetPassword;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Random;

import mirror42.dev.cinemates.mailAPI.EmailException;
import mirror42.dev.cinemates.mailAPI.MailSender;
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

public class ResetPasswordViewModel extends ViewModel {

    private final String TAG = this.getClass().getSimpleName();
    private MutableLiveData<ResetResult> resetResult;
    private RemoteConfigServer remoteConfigServer;

    public enum ResetResult {
        FAILED,
        SUCCESS,
        NONE
    }

   public ResetPasswordViewModel(){
       resetResult = new MutableLiveData<>(ResetResult.NONE);
       remoteConfigServer = RemoteConfigServer.getInstance();
   }



    public void postResetStatus(ResetResult resetResult) {
        this.resetResult.postValue(resetResult);
    }

    public LiveData<ResetResult> getResetStatus() {
        return resetResult;
    }

    public void resetPassword(String email){


        Random rand = new Random();
        int randomPassword = rand.nextInt(10000) + 132986;
        //String password = Integer.toString(randomPassword);
        String password = "aaaaaa";
        Runnable emailSendTask = sendNewPasswordEmailTask(email,password);
        Thread t = new Thread(emailSendTask, "THREAD: RESET PASSWORD - SEND EMAIL");
        t.start();
        //resetPasswordToServer(email, MyUtilities.SHA256encrypt(password));
    }


    private Runnable sendNewPasswordEmailTask(String email,String newPassword) {
        return () -> {
            MailSender sendMail = new MailSender();
            try {
               boolean isSent =  sendMail.sendAnEmail(email, "Questa è la nuova password" + newPassword);
               if(isSent)
                   postResetStatus(ResetResult.SUCCESS);
               else
                   postResetStatus(ResetResult.FAILED);
            } catch (EmailException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }

        };
    }

    private void resetPasswordToServer(String email, String password){
        final OkHttpClient httpClient = OkHttpSingleton.getClient();
        final String dbFunction = "fn_update_password";
        try{
            HttpUrl httpUrl = new HttpUrl.Builder()
                    .scheme("https")
                    .host(remoteConfigServer.getAzureHostName())
                    .addPathSegments(remoteConfigServer.getPostgrestPath())
                    .addPathSegment(dbFunction)
                    .build();
            RequestBody requestBody = new FormBody.Builder()
                    .add("mail",email)
                    .add("newpassword",password)
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
                            Log.v(TAG,"Tutto ok reset password");
                            postResetStatus(ResetResult.SUCCESS);
                        }else{
                            Log.v(TAG,"fallimento");
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
