package mirror42.dev.cinemates.ui.home;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import mirror42.dev.cinemates.RemoteConfig;

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
        String value = RemoteConfig.getTest();
        mText.setValue(value);
    }












}// end HomeViewModel class
