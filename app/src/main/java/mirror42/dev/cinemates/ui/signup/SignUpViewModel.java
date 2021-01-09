package mirror42.dev.cinemates.ui.signup;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.IOException;
import java.util.HashMap;
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

public class SignUpViewModel extends ViewModel {
    private MutableLiveData<User> user;
private final String TAG = this.getClass().getSimpleName();
    private RemoteConfigServer remoteConfigServer;
    private FirebaseAuth mAuth;
    private FirebaseUser firebaseUser;
    private MutableLiveData<FirebaseSignUpServerCodeState> firebaseAuthState;

    public enum FirebaseSignUpServerCodeState {
        SIGN_IN_SUCCESS,
        SIGN_IN_FAILURE,
        SIGN_UP_SUCCESS,
        SIGN_UP_FAILURE,
        INVALID_CREDENTIALS,
        PENDING_USER_COLLISION,
        PASSWORD_MALFORMED,
        PASSWORD_INVALID,
        EMAIL_NOT_EXISTS,
        VERIFICATION_MAIL_SENT,
        VERIFICATION_MAIL_NOT_SENT,
        FIREBASE_GENERIC_ERROR,
        EMAIL_COLLISION,
        USERNAME_COLLISION,
        USERNAME_EMAIL_COLLISION,
        GENERIC_POSTGREST_ERROR,
        WEAK_PASSWORD,
        NONE
    }





    //--------------------------------------------------------------- CONSTRUCTORS

    public SignUpViewModel() {
        this.user = new MutableLiveData<>();
        this.firebaseAuthState = new MutableLiveData<>();
        remoteConfigServer = RemoteConfigServer.getInstance();
        mAuth = FirebaseAuth.getInstance();
    }






    //--------------------------------------------------------------- GETTERS/SETTERS

    public LiveData<User> getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user.postValue(user);
    }

    public LiveData<FirebaseSignUpServerCodeState> getFirebaseAuthState() {
        return firebaseAuthState;
    }

    public void setFirebaseSignUpServerCodeState(FirebaseSignUpServerCodeState state) {
        this.firebaseAuthState.postValue(state);
    }




    //--------------------------------------------------------------- METHODS

    public void signUpAsPendingUser(
                                    String username,
                                    String email,
                                    String password,
                                    String firstName,
                                    String lastName,
                                    String birthDate,
                                    String profilePicturePath,
                                    boolean promo,
                                    boolean analytics) {
        checkUserCollision(username, email, password, firstName, lastName, birthDate, profilePicturePath, promo, analytics);

    }

    private void checkUserCollision(String username, String email, String password, String firstName, String lastName, String birthDate, String profilePicturePath, boolean promo, boolean analytics) {
        HttpUrl httpUrl = null;
        final OkHttpClient httpClient = new OkHttpClient();

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



            Request request = HttpUtilities.buildGETrequest(httpUrl, remoteConfigServer.getGuestToken());

            // performing call
            httpClient.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                    setFirebaseSignUpServerCodeState(FirebaseSignUpServerCodeState.GENERIC_POSTGREST_ERROR);
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    try (ResponseBody responseBody = response.body()) {
                        if (!response.isSuccessful()) {
                            setFirebaseSignUpServerCodeState(FirebaseSignUpServerCodeState.GENERIC_POSTGREST_ERROR);
                            throw new IOException("Unexpected code " + response);
                        }

                        try {
                            int responseCode = Integer.parseInt(responseBody.string());
                            if (responseCode == 1) {
                                setFirebaseSignUpServerCodeState(FirebaseSignUpServerCodeState.USERNAME_EMAIL_COLLISION);
                            } else if (responseCode == 2) {
                                setFirebaseSignUpServerCodeState(FirebaseSignUpServerCodeState.USERNAME_COLLISION);
                            } else if (responseCode == 3) {
                                setFirebaseSignUpServerCodeState(FirebaseSignUpServerCodeState.EMAIL_COLLISION);
                            } else if (responseCode == 4) {
                                setFirebaseSignUpServerCodeState(FirebaseSignUpServerCodeState.GENERIC_POSTGREST_ERROR);
                            } else {
                                // if not exist in postgres db
                                // add as pending user
                                checkPendingUserCollision(username, email, password, firstName, lastName, birthDate, profilePicturePath,  promo, analytics);
                            }

                        } catch (Exception e) {
                            setFirebaseSignUpServerCodeState(FirebaseSignUpServerCodeState.GENERIC_POSTGREST_ERROR);
                        }

                    } catch (Exception e) {
                        setFirebaseSignUpServerCodeState(FirebaseSignUpServerCodeState.GENERIC_POSTGREST_ERROR);
                    }
                }// end onResponse()
            });// end enquee()

        } catch (Exception e) {
            setFirebaseSignUpServerCodeState(FirebaseSignUpServerCodeState.GENERIC_POSTGREST_ERROR);
        }
    }// end checkUserCollision()

    private void checkPendingUserCollision(String username, String email, String password, String firstName, String lastName, String birthDate, String profilePicturePath, boolean promo, boolean analytics) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) { // sign in success
                            Log.d(TAG, "createUserWithEmail:success");
                            firebaseUser = mAuth.getCurrentUser();

                            // add pending user data in firebase DB
                            FirebaseFirestore firebaseDB = FirebaseFirestore.getInstance();
                            Map<String, Object> newDBuser = composeDBuserData(username, email, password, firstName, lastName, birthDate, profilePicturePath, promo, analytics);
                            // Add a new document with a the user UID
                            insertUserInDB(newDBuser, firebaseDB);

                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            try {
                                throw task.getException();
                            } catch (FirebaseAuthUserCollisionException e) {
                                e.printStackTrace();
                                setFirebaseSignUpServerCodeState(FirebaseSignUpServerCodeState.PENDING_USER_COLLISION);
                            } catch (FirebaseAuthWeakPasswordException e) {
                                e.printStackTrace();
                                setFirebaseSignUpServerCodeState(FirebaseSignUpServerCodeState.WEAK_PASSWORD);
                            } catch (Exception e) {
                                e.printStackTrace();
                                setFirebaseSignUpServerCodeState(FirebaseSignUpServerCodeState.FIREBASE_GENERIC_ERROR);
                            }
                        }
                    }
                });
    }// end checkPendingUSerCollision()

    private void insertUserInDB(Map<String, Object> newDBuser, FirebaseFirestore firebaseDB) {
        try {
            firebaseDB.collection("pending_users").document(firebaseUser.getUid())
                    .set(newDBuser)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d(TAG, "DocumentSnapshot added with ID: " + firebaseUser.getUid());
                            Log.d(TAG, "welcome: " + firebaseUser.getEmail());
                            setFirebaseSignUpServerCodeState(FirebaseSignUpServerCodeState.SIGN_UP_SUCCESS);

                            //
                            firebaseUser = mAuth.getCurrentUser();
                            sendEmailVerificationTo(firebaseUser);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w(TAG, "Error adding document", e);
                            setFirebaseSignUpServerCodeState(FirebaseSignUpServerCodeState.SIGN_UP_FAILURE);
                        }
                    });
        } catch (Exception e) {
            Log.w(TAG, "Error adding document", e);
            setFirebaseSignUpServerCodeState(FirebaseSignUpServerCodeState.SIGN_UP_FAILURE);
            e.printStackTrace();
        }

    }

    private Map<String, Object> composeDBuserData(String username,
                                                  String email,
                                                  String password,
                                                  String firstName,
                                                  String lastName,
                                                  String birthDate,
                                                  String profilePicturePath,
                                                  boolean promo,
                                                  boolean analytics) {

        Map<String, Object> dbUser = new HashMap<>();
        dbUser.put("username", username);
        dbUser.put("email", email);
        dbUser.put("password", MyUtilities.SHA256encrypt(password));
        dbUser.put("firstname", firstName);
        dbUser.put("lastname", lastName);
        dbUser.put("birthdate", birthDate);
        dbUser.put("profilePicturePath", profilePicturePath);
        dbUser.put("promo", promo);
        dbUser.put("analytics", analytics);

        return dbUser;

    }

    private void sendEmailVerificationTo(FirebaseUser user) {
        user.sendEmailVerification()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "Verification email sent.");
                            setFirebaseSignUpServerCodeState(FirebaseSignUpServerCodeState.VERIFICATION_MAIL_SENT);

                            //
                            mAuth.signOut();
                        }
                        else {
                            Log.d(TAG, "Verification email NOT sent. " + task.getException());
                            setFirebaseSignUpServerCodeState(FirebaseSignUpServerCodeState.VERIFICATION_MAIL_NOT_SENT);
                        }
                    }
                });
    }



    private RequestBody buildRequestBody(String username, String email) throws Exception {
        RequestBody requestBody = new FormBody.Builder()
                .add("mail", email)
                .add("username", username)
                .build();
        return requestBody;
    }




    private void deleteAccount(FirebaseUser user) {
        user.delete();
    }

}// end SignUpViewModel class
