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
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import mirror42.dev.cinemates.model.User;
import mirror42.dev.cinemates.utilities.HttpUtilities;
import mirror42.dev.cinemates.utilities.MyUtilities;
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
    private MutableLiveData<User> user;
    private MutableLiveData<LoginResult> loginResult;
    private RemoteConfigServer remoteConfigServer;
    private FirebaseAuth mAuth;
    private FirebaseUser firebaseUser;
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
        IS_NOT_PENDING_USER,
        INVALID_CREDENTIALS,
        REMEMBER_ME_NOT_EXISTS,
        NONE
    }



    //--------------------------------------------------- CONSTRUCTORS

    public LoginViewModel() {
        this.user = new MutableLiveData<>();
        loginResult = new MutableLiveData<>();
        remoteConfigServer = RemoteConfigServer.getInstance();
        mAuth = FirebaseAuth.getInstance();
    }




    //--------------------------------------------------- GETTERS/SETTERS

    public LiveData<User> getUser() {
        return user;
    }

    public void setPostUser(User user) {
        this.user.postValue(user);
    }

    public void setUser(User user) {
        this.user.setValue(user);
    }

    public MutableLiveData<LoginResult> getLoginResult() {
        return loginResult;
    }

    public void setPostLoginResult(LoginResult loginResult) {
        this.loginResult.postValue(loginResult);
    }

    public void setLoginResult(LoginResult loginResult) {
        this.loginResult.setValue(loginResult);
    }

    public FirebaseUser getFirebaseUser() {
        return firebaseUser;
    }

    public User getPendingUser() {
        return basicPendingUser;
    }


    //--------------------------------------------------- METHODS

    public void standardLogin(String email, String password) {
        if(password==null) {
            // TODO handle failed password encryption
        }

        //
        HttpUrl httpUrl = null;

        // generating url request
        try {
            httpUrl = buildStandardLoginUrl(email, password);

        } catch (Exception e) {
            e.printStackTrace();
            setPostLoginResult(LoginResult.INVALID_REQUEST);
        }

        // performing http request
        final OkHttpClient httpClient = new OkHttpClient();

        try {
            Request request = HttpUtilities.buildPostgresGETrequest(httpUrl, remoteConfigServer.getGuestToken());

            //
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

                                //
                                setPostUser(user);
                                setPostLoginResult(LoginResult.SUCCESS);
                            }
                            else {
                                setPostUser(null);
                                setPostLoginResult(LoginResult.INVALID_CREDENTIALS);
                            }
                        }
                        else {
                            setPostUser(null);
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

    private HttpUrl buildStandardLoginUrl(String email, String password) throws Exception {
        final String dbFunction = "login";

        //
        HttpUrl httpUrl = new HttpUrl.Builder()
                .scheme("https")
                .host(remoteConfigServer.getAzureHostName())
                .addPathSegments(remoteConfigServer.getPostgrestPath())
                .addPathSegment(dbFunction)
                .addQueryParameter("mail", email)
                .addQueryParameter("pass", password)
                .build();

        return httpUrl;
    }

    public void checkIfIsPendingUser(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener( task-> {
            if (task.isSuccessful()) {
                // Sign in success, update UI with the signed-in user's information
                Log.d(TAG, "Autorization server: email ancora non approvata, controlla la tua posta");
                firebaseUser = mAuth.getCurrentUser();
                loadPendingUserBasicData();
            } else {
                // If sign in fails, display a message to the user.
                Log.w(TAG, "Autorization server: not a pending user", task.getException());
                setLoginResult(LoginResult.IS_NOT_PENDING_USER);
            }
        });
    }// end checkIfIsPendingUser()

    public void loadPendingUserBasicData() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference docRef = db.collection("pending_users").document(firebaseUser.getUid());
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                        try {
                            JSONObject jsonObject = new JSONObject(document.getData());
                            String email = jsonObject.getString("email");
                            String profilePicturePath = jsonObject.getString("profilePicturePath");

                            basicPendingUser = new User(email, profilePicturePath);

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
            }
        });
    }



    public boolean checkEmailVerificationState() {
        if(firebaseUser!=null) {
            return firebaseUser.isEmailVerified();
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
        DocumentReference docRef = db.collection("pending_users").document(firebaseUser.getUid());
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

            SimpleDateFormat format = null;
            Date parsed = null;
            try {
                format = new SimpleDateFormat("dd-MM-yyyy");
                parsed = format.parse(jsonObject.getString("birthdate"));
            } catch (Exception e) {
                e.printStackTrace();
                format = new SimpleDateFormat("dd/MM/yyyy");
                parsed = format.parse(jsonObject.getString("birthdate"));
            }
            java.sql.Date sqlStartDate = new java.sql.Date(parsed.getTime());
            String sqldate = String.valueOf(sqlStartDate);
            String profilePicturePath = jsonObject.getString("profilePicturePath");
            String promo = jsonObject.getString("promo");
            String analytics = jsonObject.getString("analytics");

            //
            RequestBody requestBody = buildRequestBody(username, email, password, firstName, lastName, sqldate, profilePicturePath, promo, analytics);

            //
            HttpUrl httpUrl = null;
            final OkHttpClient httpClient = new OkHttpClient();
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
        }
    }

    private void deleteFromPendings() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("pending_users").document(firebaseUser.getUid())
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "DocumentSnapshot successfully deleted!");
                        firebaseUser.delete();
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
                    MyUtilities.convertUserInJSonString(user.getValue()), context);
        }
    }

    public void deleteRememberMeData(Context context) {
        setUser(null);
        setLoginResult(LoginViewModel.LoginResult.LOGGED_OUT);
        MyUtilities.deletFile(remoteConfigServer.getCinematesData(), context);
    }



    public void resendVerificationEmail() {
        firebaseUser.sendEmailVerification()
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
