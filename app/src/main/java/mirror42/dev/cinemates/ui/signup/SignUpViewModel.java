package mirror42.dev.cinemates.ui.signup;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import mirror42.dev.cinemates.model.User;
import mirror42.dev.cinemates.utilities.RemoteConfigServer;

public class SignUpViewModel extends ViewModel {
    private MutableLiveData<User> user;
    private RemoteConfigServer remoteConfigServer;







    //--------------------------------------------------------------- CONSTRUCTORS
    public SignUpViewModel() {
        this.user = new MutableLiveData<>();
        remoteConfigServer = RemoteConfigServer.getInstance();
    }





    //--------------------------------------------------------------- GETTERS/SETTERS

    public LiveData<User> getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user.postValue(user);
    }





    //--------------------------------------------------------------- METHODS









}// end SignInViewModel class
