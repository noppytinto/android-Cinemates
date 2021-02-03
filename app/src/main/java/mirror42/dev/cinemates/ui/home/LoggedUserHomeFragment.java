package mirror42.dev.cinemates.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import mirror42.dev.cinemates.R;

public class LoggedUserHomeFragment extends Fragment {

    private LoggedUserHomeViewModel mViewModel;

    public static LoggedUserHomeFragment newInstance() {
        return new LoggedUserHomeFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.logged_user_home_fragment, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(LoggedUserHomeViewModel.class);
        // TODO: Use the ViewModel
    }

}