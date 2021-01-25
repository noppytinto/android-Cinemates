package mirror42.dev.cinemates.ui.userprofile;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import mirror42.dev.cinemates.model.User;
import mirror42.dev.cinemates.utilities.RemoteConfigServer;

public class PersonalProfileViewModel extends ViewModel {
    private final String TAG = this.getClass().getSimpleName();
    private MutableLiveData<User> user;
    private RemoteConfigServer remoteConfigServer;
    private FirebaseAuth mAuth;
    private FirebaseUser firebaseUser;



    //----------------------------------------------------------------- CONSTRUCTORS

    public PersonalProfileViewModel() {
        this.user = new MutableLiveData<>();
        remoteConfigServer = RemoteConfigServer.getInstance();
        mAuth = FirebaseAuth.getInstance();
    }







    //----------------------------------------------------------------- GETTERS/SETTERS




    //----------------------------------------------------------------- METHODS










}// UserProfileViewModel class
