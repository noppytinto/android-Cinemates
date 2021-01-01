package mirror42.dev.cinemates.ui.home;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import mirror42.dev.cinemates.utilities.RemoteConfigServer;

public class HomeViewModel extends ViewModel {
    private MutableLiveData<String> mText;

    public HomeViewModel() {
        mText = new MutableLiveData<>();
    }

    public LiveData<String> getText() {
        return mText;
    }

    public void applyRemoteConfig() {

        //
        RemoteConfigServer remoteConfigServer = RemoteConfigServer.getInstance();
        String test = remoteConfigServer.getTest();

        mText.setValue(test);
    }












}// end HomeViewModel class
