package mirror42.dev.cinemates.ui.signup;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
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

public class SignUpViewModel extends ViewModel {
    private MutableLiveData<User> observableUser;
private final String TAG = this.getClass().getSimpleName();
    private RemoteConfigServer remoteConfigServer;
    private FirebaseAuth mAuth;
    private FirebaseUser firebaseUser;
    private MutableLiveData<FirebaseSignUpServerCodeState> firebaseAuthState;
    private User newUser;

    public enum FirebaseSignUpServerCodeState {
        SIGN_IN_SUCCESS,
        SIGN_IN_FAILURE,
        SIGN_UP_SUCCESS,
        SIGN_UP_FAILURE,
        INVALID_CREDENTIALS,
        PENDING_USER_COLLISION,
        NO_PENDING_USER_COLLISION,
        PENDING_USER_FOUND,
        PENDING_USER_NOT_FOUND,
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
        POSTGRES_USER_COLLISION,
        NO_POSTGRES_USER_COLLISION,
        NONE
    }






    //--------------------------------------------------------------- CONSTRUCTORS

    public SignUpViewModel() {
        this.observableUser = new MutableLiveData<>();
        this.firebaseAuthState = new MutableLiveData<>(FirebaseSignUpServerCodeState.NONE);
        remoteConfigServer = RemoteConfigServer.getInstance();
        mAuth = FirebaseAuth.getInstance();
    }






    //--------------------------------------------------------------- GETTERS/SETTERS

    public LiveData<User> getUser() {
        return observableUser;
    }

    public void setUser(User user) {
        this.observableUser.postValue(user);
    }

    public LiveData<FirebaseSignUpServerCodeState> getFirebaseAuthState() {
        return firebaseAuthState;
    }

    public void setFirebaseSignUpServerCodeStatus(FirebaseSignUpServerCodeState state) {
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
        newUser = new User();
        newUser.setUsername(username);
        newUser.setEmail(email);
        newUser.setPassword(password);
        newUser.setFirstName(firstName);
        newUser.setLastName(lastName);
        newUser.setBirthDate(birthDate);
        newUser.setProfilePicturePath(profilePicturePath);
        newUser.setPromo(promo);
        newUser.setAnalytics(analytics);

        checkUserCollision();
    }

    private void checkUserCollision() {
        HttpUrl httpUrl = null;
        final OkHttpClient httpClient = OkHttpSingleton.getClient();

        final String dbFunction = "fn_check_user_collision_ignore_active";
        try {
            httpUrl = new HttpUrl.Builder()
                    .scheme("https")
                    .host(remoteConfigServer.getAzureHostName())
                    .addPathSegments(remoteConfigServer.getPostgrestPath())
                    .addPathSegment(dbFunction)
                    .addQueryParameter("username", newUser.getUsername())
                    .addQueryParameter("email", newUser.getEmail())
                    .build();

            Request request = HttpUtilities.buildPostgresGETrequest(httpUrl, remoteConfigServer.getGuestToken());

            // performing call
            httpClient.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                    setFirebaseSignUpServerCodeStatus(FirebaseSignUpServerCodeState.GENERIC_POSTGREST_ERROR);
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    try (ResponseBody responseBody = response.body()) {
                        if (!response.isSuccessful()) {
                            setFirebaseSignUpServerCodeStatus(FirebaseSignUpServerCodeState.GENERIC_POSTGREST_ERROR);
                            throw new IOException("Unexpected code " + response);
                        }

                        try {
                            int responseCode = Integer.parseInt(responseBody.string());
                            switch (responseCode) {
                                case 1:
                                    setFirebaseSignUpServerCodeStatus(FirebaseSignUpServerCodeState.USERNAME_EMAIL_COLLISION);
                                    break;
                                case 2:
                                    setFirebaseSignUpServerCodeStatus(FirebaseSignUpServerCodeState.USERNAME_COLLISION);
                                    break;
                                case 3:
                                    setFirebaseSignUpServerCodeStatus(FirebaseSignUpServerCodeState.EMAIL_COLLISION);
                                    break;
                                case 4:
                                    setFirebaseSignUpServerCodeStatus(FirebaseSignUpServerCodeState.GENERIC_POSTGREST_ERROR);
                                    break;
                                default:
                                    // if not exist in postgres db
                                    // add as pending user
                                    setFirebaseSignUpServerCodeStatus(FirebaseSignUpServerCodeState.NO_POSTGRES_USER_COLLISION);
                            }
                        } catch (Exception e) {
                            setFirebaseSignUpServerCodeStatus(FirebaseSignUpServerCodeState.GENERIC_POSTGREST_ERROR);
                        }
                    } catch (Exception e) {
                        setFirebaseSignUpServerCodeStatus(FirebaseSignUpServerCodeState.GENERIC_POSTGREST_ERROR);
                    }
                }// end onResponse()
            });// end enquee()

        } catch (Exception e) {
            setFirebaseSignUpServerCodeStatus(FirebaseSignUpServerCodeState.GENERIC_POSTGREST_ERROR);
        }
    }// end checkUserCollision()

    public void checkPendingUserCollision() {
        mAuth.createUserWithEmailAndPassword(newUser.getEmail(), newUser.getPassword())
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) { // sign in success
                            Log.d(TAG, "createUserWithEmail:success");
                            firebaseUser = mAuth.getCurrentUser();

                            // fetch profile picture
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    String imageName = "-";
                                    try {
                                        // uploading promo image to the cloud
                                        Cloudinary cloudinary = new Cloudinary(remoteConfigServer.getCloudinaryUploadBaseUrl());
                                        Map<String, Object> uploadResult = cloudinary.uploader().upload(newUser.getProfilePicturePath(), ObjectUtils.emptyMap());
                                        imageName = (String) uploadResult.get("public_id");

                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }

                                    newUser.setProfilePicturePath(imageName+".png");
                                    setFirebaseSignUpServerCodeStatus(FirebaseSignUpServerCodeState.NO_PENDING_USER_COLLISION);
                                }
                            }).start();

                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            try {
                                throw task.getException();
                            } catch (FirebaseAuthUserCollisionException e) {
                                e.printStackTrace();
                                setFirebaseSignUpServerCodeStatus(FirebaseSignUpServerCodeState.PENDING_USER_COLLISION);
                            } catch (FirebaseAuthWeakPasswordException e) {
                                e.printStackTrace();
                                setFirebaseSignUpServerCodeStatus(FirebaseSignUpServerCodeState.WEAK_PASSWORD);
                            } catch (Exception e) {
                                e.printStackTrace();
                                setFirebaseSignUpServerCodeStatus(FirebaseSignUpServerCodeState.FIREBASE_GENERIC_ERROR);
                            }
                        }
                    }
                });
    }// end checkPendingUSerCollision()

    public void insertUserInFirebaseDB() {
        // add pending user data in firebase DB
        FirebaseFirestore firebaseDB = FirebaseFirestore.getInstance();
        Map<String, Object> newDBuser = composeDBuserData();

        try {
            firebaseDB.collection("pending_users").document(firebaseUser.getUid())
                    .set(newDBuser)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d(TAG, "DocumentSnapshot added with ID: " + firebaseUser.getUid());
                            Log.d(TAG, "welcome: " + firebaseUser.getEmail());
                            setFirebaseSignUpServerCodeStatus(FirebaseSignUpServerCodeState.SIGN_UP_SUCCESS);

                            //
                            firebaseUser = mAuth.getCurrentUser();
                            sendEmailVerificationTo(firebaseUser);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w(TAG, "Error adding document", e);
                            setFirebaseSignUpServerCodeStatus(FirebaseSignUpServerCodeState.SIGN_UP_FAILURE);
                        }
                    });
        } catch (Exception e) {
            Log.w(TAG, "Error adding document", e);
            setFirebaseSignUpServerCodeStatus(FirebaseSignUpServerCodeState.SIGN_UP_FAILURE);
            e.printStackTrace();
        }
    }

    private Map<String, Object> composeDBuserData() {

        Map<String, Object> dbUser = new HashMap<>();
        dbUser.put("username", newUser.getUsername());
        dbUser.put("email", newUser.getEmail());
        dbUser.put("password", MyUtilities.SHA256encrypt(newUser.getPassword()));
        dbUser.put("firstname", newUser.getFirstName());
        dbUser.put("lastname", newUser.getLastName());
        dbUser.put("birthdate", newUser.getBirthDate());
        dbUser.put("profilePicturePath", newUser.getProfilePicturePath());
        dbUser.put("promo", newUser.getPromo());
        dbUser.put("analytics", newUser.getAnalytics());

        return dbUser;

    }

    private void sendEmailVerificationTo(FirebaseUser user) {
        user.sendEmailVerification()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "Verification email sent.");
                            setFirebaseSignUpServerCodeStatus(FirebaseSignUpServerCodeState.VERIFICATION_MAIL_SENT);

                            //
                            mAuth.signOut();
                        }
                        else {
                            Log.d(TAG, "Verification email NOT sent. " + task.getException());
                            setFirebaseSignUpServerCodeStatus(FirebaseSignUpServerCodeState.VERIFICATION_MAIL_NOT_SENT);
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
