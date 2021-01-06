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
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

import mirror42.dev.cinemates.model.User;
import mirror42.dev.cinemates.utilities.MyUtilities;
import mirror42.dev.cinemates.utilities.RemoteConfigServer;

public class SignUpViewModel extends ViewModel {
    private MutableLiveData<User> user;
private final String TAG = this.getClass().getSimpleName();
    private RemoteConfigServer remoteConfigServer;
    private FirebaseAuth mAuth;
    private FirebaseUser firebaseUser;
    private MutableLiveData<FirebaseAuthState> firebaseAuthState;


    /**
     * by EMAIL_COLLISION we mean:
     *  the email is already in use by another user
     *
     * by X_X_FAILURE we mean:
     *  usually server errors
     *
     */
    public enum FirebaseAuthState {
        SIGN_IN_SUCCESS,
        SIGN_IN_FAILURE,
        SIGN_UP_SUCCESS,
        SIGN_UP_FAILURE,
        INVALID_CREDENTIALS,
        EMAIL_COLLISION,
        PASSWORD_MALFORMED,
        PASSWORD_INVALID,
        EMAIL_NOT_EXISTS,
        VERIFICATION_MAIL_SENT,
        VERIFICATION_MAIL_NOT_SENT

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

    public LiveData<FirebaseAuthState> getFirebaseAuthState() {
        return firebaseAuthState;
    }

    public void setFirebaseAuthState(FirebaseAuthState state) {
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
                                    boolean promo,
                                    boolean analytics) {

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) { // sign in success
                            Log.d(TAG, "createUserWithEmail:success");

                            // add pending user data in firebase DB
                            FirebaseFirestore firebaseDB = FirebaseFirestore.getInstance();
                            Map<String, Object> newDBuser = composeDBuserData(email, password, username, firstName, lastName, birthDate, promo, analytics);
                            // Add a new document with a the user UID
                            insertUserInDB(newDBuser, firebaseDB);

                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            setFirebaseAuthState(FirebaseAuthState.SIGN_UP_FAILURE);
                        }

                    }
                });
    }



    private void sendEmailVerificationTo(FirebaseUser user) {
        user.sendEmailVerification()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "Verification email sent.");
                            setFirebaseAuthState(FirebaseAuthState.VERIFICATION_MAIL_SENT);
                        }
                        else {
                            Log.d(TAG, "Verification email NOT sent. " + task.getException());
                            setFirebaseAuthState(FirebaseAuthState.VERIFICATION_MAIL_NOT_SENT);
                        }
                    }
                });
    }

    private void deleteAccount(FirebaseUser user) {
        user.delete();
    }

    private Map<String, Object> composeDBuserData(String email,
                                                    String password,
                                                    String username,
                                                    String firstName,
                                                    String lastName,
                                                    String birthDate,
                                                    boolean promo,
                                                    boolean analytics) {

        Map<String, Object> dbUser = new HashMap<>();
        dbUser.put("username", username);
        dbUser.put("email", email);
        dbUser.put("password", MyUtilities.SHA256encrypt(password));
        dbUser.put("firstname", firstName);
        dbUser.put("lastname", lastName);
        dbUser.put("birthdate", birthDate);
        dbUser.put("promo", promo);
        dbUser.put("analytics", analytics);

        return dbUser;

    }


    private void insertUserInDB(Map<String, Object> newDBuser, FirebaseFirestore firebaseDB) {
        firebaseDB.collection("pending_users").document(firebaseUser.getUid())
                .set(newDBuser)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "DocumentSnapshot added with ID: " + firebaseUser.getUid());
                        Log.d(TAG, "welcome: " + firebaseUser.getEmail());
                        setFirebaseAuthState(FirebaseAuthState.SIGN_UP_SUCCESS);

                        //
                        firebaseUser = mAuth.getCurrentUser();
                        sendEmailVerificationTo(firebaseUser);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error adding document", e);
                        setFirebaseAuthState(FirebaseAuthState.SIGN_UP_FAILURE);
                    }
                });

    }





}// end SignUpViewModel class
