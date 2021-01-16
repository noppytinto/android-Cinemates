package mirror42.dev.cinemates.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import mirror42.dev.cinemates.R;
import mirror42.dev.cinemates.ui.home.post.WatchlistPostAdapter;

public class LoggedUserHomeFragment extends Fragment {
    private LoggedUserHomeViewModel loggedUserHomeViewModel;
    private WatchlistPostAdapter watchlistPostAdapter;
    private View view;



    //------------------------------------------------------------------- ANDROID METHODS

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.logged_user_home_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.view = view;


    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        loggedUserHomeViewModel = new ViewModelProvider(this).get(LoggedUserHomeViewModel.class);
        // TODO: Use the ViewModel


    }




    //------------------------------------------------------------------- METHODS


    private void initRecycleView() {
        // defining Recycler view
        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.recyclerView_loggedUserHomeFragment);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // adding recycle listener for touch detection
        watchlistPostAdapter = new WatchlistPostAdapter(new ArrayList<>(), getContext());
        recyclerView.setAdapter(watchlistPostAdapter);
    }










}// end LoggedUserHomeFragment class