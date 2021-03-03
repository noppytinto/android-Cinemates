package mirror42.dev.cinemates.ui.login;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Map;

import mirror42.dev.cinemates.model.User;
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


public class LoginViewModel extends ViewModel {
    private final String TAG = this.getClass().getSimpleName();
    private MutableLiveData<User> loggedUser;
    private MutableLiveData<LoginResult> loginResult;
    private RemoteConfigServer remoteConfigServer;
    private FirebaseAuth mAuth;
    private FirebaseUser currentFirebaseUser;
    private boolean rememberMeExists;
    private User basicPendingUser;

    public enum LoginResult {
        INVALID_REQUEST,
        FAILED,
        SUCCESS,
        INVALID_PASSWORD,
        USER_NOT_EXIST,
        LOGGED_OUT,
        REMEMBER_ME_EXISTS,
        IS_PENDING_USER,
        IS_NOT_PENDING_USER_ANYMORE,
        INVALID_CREDENTIALS,
        REMEMBER_ME_NOT_EXISTS,
        NONE
    }



    //----------------------------------------------------------------------- CONSTRUCTORS

    public LoginViewModel() {
        this.loggedUser = new MutableLiveData<>();
        loginResult = new MutableLiveData<>(LoginResult.NONE);
        remoteConfigServer = RemoteConfigServer.getInstance();
        mAuth = FirebaseAuth.getInstance();
    }




    //----------------------------------------------------------------------- GETTERS/SETTERS

    public LiveData<User> getObservableLoggedUser() {
        return loggedUser;
    }

    public User getLoggedUser() {
        return loggedUser.getValue();
    }


    public void postUser(User user) {
        this.loggedUser.postValue(user);
    }

    public void setUser(User user) {
        this.loggedUser.setValue(user);
    }

    public MutableLiveData<LoginResult> getObservableLoginResult() {
        return loginResult;
    }

    public LoginResult getLoginResult() {
        return loginResult.getValue();
    }

    public void setPostLoginResult(LoginResult loginResult) {
        this.loginResult.postValue(loginResult);
    }

    public void setLoginResult(LoginResult loginResult) {
        this.loginResult.setValue(loginResult);
    }

    public FirebaseUser getCurrentFirebaseUser() {
        return currentFirebaseUser;
    }

    public User getPendingUser() {
        return basicPendingUser;
    }


    //----------------------------------------------------------------------- METHODS

    public boolean rememberMeExists() {
        return rememberMeExists;

    }


    public void login(String email, String password) {
        //do login
//        Task<AuthResult> loginTask = mAuth.signInWithEmailAndPassword(email, password);
//
//        // check pending user exists in firebase DB
//        Task<AuthResult> loadPendingUserDetailsTask = null;
//        loginTask.continueWith(new Continuation<AuthResult, Object>() {
//            @Override
//            public Object then(@NonNull Task<AuthResult> task) throws Exception {
//                // if pending user exists
//                if(task.isSuccessful()) {
//                    loadPendingUserBasicData();
//                }
//                return null;
//            }
//        });





//        try {
//            loginTask.continueWithTask(new Continuation<AuthResult, Task<AuthResult>>() {
//                @Override
//                public Task<AuthResult> then(@NonNull Task<AuthResult> task) throws Exception {
//                    task.getException();
//                    return task;
//                }
//            }).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
//                @Override
//                public void onSuccess(AuthResult authResult) {
//                    authResult.getUser();
//                }
//
//            }).addOnFailureListener(new OnFailureListener() {
//                    @Override
//                    public void onFailure(@NonNull Exception e) {
//                        try {
//                            throw loginTask.getException();
//                        } catch (FirebaseAuthInvalidUserException faiue) {
//                            faiue.printStackTrace();
//                            setLoginResult(LoginResult.IS_NOT_PENDING_USER_ANYMORE);
//                        }  catch(Exception getException) {
//                            getException.printStackTrace();
//                        }
//                    }
//                });
//        } catch (Exception e) {
//            e.printStackTrace();
//        }


        //check if is a pending user first
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener( task-> {
            if (task.isSuccessful()) {
                // Sign in success, update UI with the signed-in user's information
                currentFirebaseUser = mAuth.getCurrentUser();
                loadPendingUserDetails();
            } else {
                // If sign in fails, display a message to the user.
                Log.w(TAG, "Autorization server: not a pending user", task.getException());
                setLoginResult(LoginResult.IS_NOT_PENDING_USER_ANYMORE);
            }
        });
    }// end checkIfIsPendingUser()

    public void loadPendingUserDetails() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference docRef = db.collection("pending_users").document(currentFirebaseUser.getUid());
        docRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                    try {
                        JSONObject jsonObject = new JSONObject(document.getData());
                        String email = jsonObject.getString("email");
                        String profilePicturePath = jsonObject.getString("profilePicturePath");
                        String firstName = jsonObject.getString("firstname");
                        String lastName = jsonObject.getString("lastname");
                        String username = jsonObject.getString("username");

                        basicPendingUser = new User(email, remoteConfigServer.getCloudinaryDownloadBaseUrl() + profilePicturePath);
                        basicPendingUser.setFirstName(firstName);
                        basicPendingUser.setLastName(lastName);
                        basicPendingUser.setUsername(username);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    setLoginResult(LoginResult.IS_PENDING_USER);
                } else {
                    // NOTE: should not enter this case
                    //       because of preconditions
                    Log.d(TAG, "No such document");
                }

            } else {
                Log.d(TAG, "get failed with ", task.getException());
            }
        });
    }// end loadPendingUserBasicData()

    public void updateProfileImageUrl(String imageName) {
        loggedUser.getValue().setProfilePictureURL(remoteConfigServer.getCloudinaryDownloadBaseUrl() + imageName);

    }

    public void standardLogin(String email, String password) {
        if(password==null) {
            // TODO handle failed password encryption
        }


        final OkHttpClient httpClient = OkHttpSingleton.getClient();

        try {
            // generating url request
            final String dbFunction = "login";
            HttpUrl httpUrl = HttpUtilities.buildHttpURL(dbFunction);
            RequestBody requestBody = new FormBody.Builder()
                .add("mail", email)
                .add("pass", password)
                .build();
            Request request = HttpUtilities.buildPostgresPOSTrequest(httpUrl, requestBody, remoteConfigServer.getGuestToken());

            // performing http request
            Call call = httpClient.newCall(request);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    setPostLoginResult(LoginResult.FAILED);
                }

                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                    try {
                        if (response.isSuccessful()) {
                            String responseData = response.body().string();

                            if( ! responseData.equals("null")) {
                                JSONObject jsonObject = new JSONObject(responseData);

                                //
                                User user = User.parseUserFromJsonObject(jsonObject);
                                String profilePictureUrl = remoteConfigServer.getCloudinaryDownloadBaseUrl() + user.getProfilePicturePath();
                                user.setProfilePictureURL(profilePictureUrl);

                                //
                                postUser(user);
                                setPostLoginResult(LoginResult.SUCCESS);
                            }
                            else {
                                postUser(null);
                                setPostLoginResult(LoginResult.INVALID_CREDENTIALS);
                            }
                        }
                        else {
                            postUser(null);
                            setPostLoginResult(LoginResult.FAILED);

                            //TODO: should be logged
//                showToastOnUiThread("Authentication server:\n" +
//                        "message: " + response.header("message")
//                        + "errore code: " + response.header("code"));
                        }
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                        setPostLoginResult(LoginResult.FAILED);
                    }
                }
            });
        }catch(Exception e) {
            e.printStackTrace();
            setPostLoginResult(LoginResult.INVALID_REQUEST);
        }
    }// end standardLogin()



    public boolean checkEmailVerificationState() {
        if(currentFirebaseUser !=null) {
            return currentFirebaseUser.isEmailVerified();
        }

        return false;
    }

    /**
     * PRECONDITIONS:
     * - pending user must exist
     * - therefore document in pending_users must exists
     *   with UID as name
     *
     */
    public void insertIntoPostgres() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference docRef = db.collection("pending_users").document(currentFirebaseUser.getUid());
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Log.d(TAG, "DocumentSnapshot data: " + document.getData());

                        // insert into postgres
                        insert(document.getData());

                    } else {
                        // NOTE: should not enter this case
                        //       because of preconditions
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });
    }

    private RequestBody buildRequestBody(String username,
                                     String email,
                                     String password,
                                     String firstName,
                                     String lastName,
                                     String birthday,
                                     String profilePicturePath,
                                     String promo,
                                     String analytics) throws Exception {
    RequestBody requestBody = new FormBody.Builder()
            .add("mail", email)
            .add("username", username)
            .add("pass", password)
            .add("firstname", firstName)
            .add("lastname", lastName)
            .add("birthday", birthday)
            .add("profilepicturepath", profilePicturePath)
            .add("promo", promo)
            .add("analytics", analytics)
            .build();



    return requestBody;
}


    private void insert(Map<String, Object> document) {
        try {
            JSONObject jsonObject = new JSONObject(document);

            String username = jsonObject.getString("username");
            String email = jsonObject.getString("email");
            String password = jsonObject.getString("password");
            String firstName = jsonObject.getString("firstname");
            String lastName = jsonObject.getString("lastname");
            String date = jsonObject.getString("birthdate");
            String sqldate = MyUtilities.convertStringDateToStringSqlDate(date);
            String profilePicturePath = jsonObject.getString("profilePicturePath");
            String promo = jsonObject.getString("promo");
            String analytics = jsonObject.getString("analytics");

            //
            RequestBody requestBody = buildRequestBody(username, email, password, firstName, lastName, sqldate, profilePicturePath, promo, analytics);

            //
            HttpUrl httpUrl = null;
            final OkHttpClient httpClient = OkHttpSingleton.getClient();
            final String dbFunction = "fn_register_new_user";
            //
            httpUrl = new HttpUrl.Builder()
                    .scheme("https")
                    .host(remoteConfigServer.getAzureHostName())
                    .addPathSegments(remoteConfigServer.getPostgrestPath())
                    .addPathSegment(dbFunction)
                    .build();

            Request request = HttpUtilities.buildPostgresPOSTrequest(httpUrl, requestBody, remoteConfigServer.getGuestToken());

            httpClient.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    Log.d(TAG, "error postgrest");
                }

                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                    if(response.isSuccessful()) {
                        Log.d(TAG, "postgrest success");
                        try(ResponseBody responseBody = response.body()) {
                            boolean res = Boolean.parseBoolean(responseBody.string());

                            if(res) {
                                deleteFromPendings();
                                standardLogin(email, password);
                            }


                        } catch (Exception e) {
                            Log.d(TAG, "error postgrest");
                            e.printStackTrace();
                        }

                    }
                    else {
                        ResponseBody responseBody = response.body();
                        String res1 = responseBody.string();

                        Log.d(TAG, "error postgrest");

                    }

                }
            });


        } catch (Exception e) {
            e.printStackTrace();
            setLoginResult(LoginResult.FAILED);
        }
    }

    private void deleteFromPendings() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("pending_users").document(currentFirebaseUser.getUid())
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "DocumentSnapshot successfully deleted!");
                        currentFirebaseUser.delete();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error deleting document", e);
                    }
                });
    }

    public void checkRememberMeData(Context context) {
        rememberMeExists = MyUtilities.checkFileExists(remoteConfigServer.getCinematesData(), context);

        if(rememberMeExists) {
            try {
                // decrypt remember me data
                JSONObject jsonObject = new JSONObject(MyUtilities.decryptFile(remoteConfigServer.getCinematesData(), context));

                // create remember me user
                User remeberMeUser = User.parseUserFromJsonObject(jsonObject);

                //
                setUser(remeberMeUser);
                setLoginResult(LoginResult.REMEMBER_ME_EXISTS);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void saveRememberMeDataIfChecked(boolean checked, Context context) {
        if(checked) {
            remoteConfigServer = RemoteConfigServer.getInstance();
            MyUtilities.encryptFile(remoteConfigServer.getCinematesData(),
                    MyUtilities.convertUserInJSonString(loggedUser.getValue()), context);
        }
    }

    public void deleteLoggedUserLocalData(Context context) {
        try {
            invalidateCurrentAccessToken();
        } catch (Exception e) {
            e.printStackTrace();
        }
        setLoginResult(LoginViewModel.LoginResult.LOGGED_OUT);
        MyUtilities.deletFile(remoteConfigServer.getCinematesData(), context);
    }

    private void invalidateCurrentAccessToken() {
        HttpUrl httpUrl = null;
        final String dbFunction = "fn_invalidate_access_token";
        final OkHttpClient httpClient = OkHttpSingleton.getClient();

        //
        httpUrl = new HttpUrl.Builder()
                .scheme("https")
                .host(remoteConfigServer.getAzureHostName())
                .addPathSegments(remoteConfigServer.getPostgrestPath())
                .addPathSegment(dbFunction)
                .build();

        RequestBody requestBody = new FormBody.Builder()
                .add("email", loggedUser.getValue().getEmail())
                .add("access_token", loggedUser.getValue().getAccessToken())
                .build();

        Request request = HttpUtilities.buildPostgresPOSTrequest(httpUrl, requestBody, loggedUser.getValue().getAccessToken());

        Call call = httpClient.newCall(request);

        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                //TODO
                postUser(null);
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {

                if (response.isSuccessful()) { }
                else {
                    //TODO
                }

                postUser(null);

            }
        });

    }// end invalidateCurrentAccessToken();

    public void resendVerificationEmail() {
        currentFirebaseUser.sendEmailVerification()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "Verification email sent.");
                            //
                        }
                        else {
                            Log.d(TAG, "Verification email NOT sent. " + task.getException());
                        }
                    }
                });
    }

}// end LoginViewModel class
