package mirror42.dev.cinemates.ui.home.post;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import mirror42.dev.cinemates.R;

public class WatchlistPostFragment extends Fragment {

    private WatchlistPostViewModel mViewModel;

    public static WatchlistPostFragment newInstance() {
        return new WatchlistPostFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_watchlist_post, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(WatchlistPostViewModel.class);
        // TODO: Use the ViewModel
    }

}