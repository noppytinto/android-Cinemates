package mirror42.dev.cinemates.ui.explore.latest;

import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import mirror42.dev.cinemates.NavGraphDirections;
import mirror42.dev.cinemates.R;
import mirror42.dev.cinemates.adapter.RecyclerAdapterExploreLatest;
import mirror42.dev.cinemates.listener.RecyclerListener;
import mirror42.dev.cinemates.model.tmdb.Movie;
import mirror42.dev.cinemates.ui.explore.ExploreFragmentDirections;
import mirror42.dev.cinemates.utilities.FirebaseAnalytics;

public class LatestReleasesFragment extends Fragment implements
        RecyclerListener.OnClick_RecyclerListener {
    private final String TAG = this.getClass().getSimpleName();
    private final int PAGE_1 = 1;
    private LatestReleasesViewModel latestReleasesViewModel;
    private RecyclerAdapterExploreLatest recyclerAdapterExploreLatest;
    private View view;



    //------------------------------------------------------------------------ LIFECYLE METHODS

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "//------------------------------------ onCreateView() called");
        return inflater.inflate(R.layout.fragment_latest_releases, container, false);
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(TAG, "//------------------------- onViewCreated() called");
        this.view = view;

        //1
        initRecyclerView();

        //2
        latestReleasesViewModel = new ViewModelProvider(this).get(LatestReleasesViewModel.class);
        latestReleasesViewModel.getMoviesList().observe(getViewLifecycleOwner(), moviesList -> {
            if(moviesList!=null) {
                recyclerAdapterExploreLatest.loadNewData(moviesList);
            }
            else {
                Toast toast = Toast.makeText(getContext(), "errore caricamento Ultime Uscite", Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
            }
        });

        //3 downloading data
        latestReleasesViewModel.downloadData(PAGE_1);
    }// end onViewCreated()





    //------------------------------------------------------------------------ METHODS

    void initRecyclerView() {
        // defining HORIZONTAL layout manager for recycler
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);

        // defining Recycler view
        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.recyclerView_explorePage_latestReleases);
//        recyclerView.setItemAnimator(new DefaultItemAnimator()); //TODO: should investigate
        recyclerView.setLayoutManager(linearLayoutManager);

        // adding recycle listener for touch detection
        recyclerView.addOnItemTouchListener(new RecyclerListener(getContext(), recyclerView, this));

        // assigning adapter to recycle
        recyclerAdapterExploreLatest = new RecyclerAdapterExploreLatest(new ArrayList<>(), getContext());
        recyclerView.setAdapter(recyclerAdapterExploreLatest);
    }




    //------------------------------------------------------ RECYCLER LISTENER METHODS
    @Override
    public void onItemClick(View view, int position) {
        Log.d(TAG, "onItemClick() called");

        try {
            //
            Movie movieSelected = recyclerAdapterExploreLatest.getMoviesList(position);

            //
            FirebaseAnalytics firebaseAnalytics = FirebaseAnalytics.getInstance();
            firebaseAnalytics.logSelectedMovie(movieSelected, "selected movie in explore tab", this, getContext());

            //
            NavGraphDirections.AnywhereToMovieDetailsFragment
                    action = ExploreFragmentDirections.anywhereToMovieDetailsFragment(movieSelected);
            NavHostFragment.findNavController(LatestReleasesFragment.this).navigate(action);
        } catch (Exception e) {
            e.getMessage();
            e.printStackTrace();
        }

        Log.d(TAG, "onItemClick() ended");
    }

    @Override
    public void onItemLongClick(View view, int position) {
//        Toast.makeText(getContext(), "item "+position+" long clicked", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "onItemLongClick() called");
    }

}// end LatestReleasesFragment class