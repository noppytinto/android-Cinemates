package mirror42.dev.cinemates.ui.explore.upcomings;

import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import mirror42.dev.cinemates.NavGraphDirections;
import mirror42.dev.cinemates.R;
import mirror42.dev.cinemates.adapter.RecyclerAdapterExploreUpcoming;
import mirror42.dev.cinemates.listener.RecyclerListener;
import mirror42.dev.cinemates.model.tmdb.Movie;
import mirror42.dev.cinemates.ui.explore.ExploreFragmentDirections;
import mirror42.dev.cinemates.utilities.FirebaseAnalytics;


public class UpcomginsFragment extends Fragment implements
        RecyclerListener.OnClick_RecyclerListener {

    private final int PAGE_1 = 1;
    private UpcomginsViewModel upcomginsViewModel;
    private RecyclerAdapterExploreUpcoming recyclerAdapterExploreUpcoming;
    private View view;
    private static boolean stopFetchingUpcomings;




    //------------------------------------------------------------------------ LIFECYCLE METHODS

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_upcomgins, container, false);
    }


    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.view = view;

        //1
        initRecyclerView();

        //2
        upcomginsViewModel = new ViewModelProvider(this).get(UpcomginsViewModel.class);
        upcomginsViewModel.getMoviesList().observe(getViewLifecycleOwner(), new Observer<ArrayList<Movie>>() {
            @Override
            public void onChanged(@Nullable ArrayList<Movie> movies) {
                if(movies!=null) {
                    recyclerAdapterExploreUpcoming.loadNewData(movies);
                }
                else {
                    Toast toast = Toast.makeText(getContext(), "errore caricamento Prossimamente", Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                }
            }
        });

        // downloading data
        if(!stopFetchingUpcomings) {
            upcomginsViewModel.downloadData(PAGE_1);
            stopFetchingUpcomings = true;
        }
        else {
            upcomginsViewModel.loadCached();
        }
    }// end onViewCreated()

//    @Override
//    public void onSaveInstanceState(@NonNull Bundle outState) {
//        super.onSaveInstanceState(outState);
//        outState.putParcelableArrayList(MOVIES_LIST_KEY, (ArrayList<Movie>) moviesList);
//    }







    //------------------------------------------------------------------------ METHODS

    public void initRecyclerView() {
        // defining HORIZONTAL layout manager for recycler
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);

        // defining Recycler view
        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.recyclerView_explorePage_upcoming);
        recyclerView.setLayoutManager(linearLayoutManager);

        // adding recycle listener for touch detection
        recyclerView.addOnItemTouchListener(new RecyclerListener(getContext(), recyclerView, this));

        // assigning adapter to recycle
        recyclerAdapterExploreUpcoming = new RecyclerAdapterExploreUpcoming(new ArrayList<Movie>(), getContext());
        recyclerView.setAdapter(recyclerAdapterExploreUpcoming);
    }






    //------------------------------------------------------ RECYCLER VIEW LISTENER METHODS
    @Override
    public void onItemClick(View view, int position) {
        try {
            //
            Movie movieSelected = recyclerAdapterExploreUpcoming.getMoviesList(position);

            //
            FirebaseAnalytics firebaseAnalytics = FirebaseAnalytics.getInstance();
            firebaseAnalytics.logSelectedMovie(movieSelected, "selected movie in explore tab", this, getContext());

            NavGraphDirections.AnywhereToMovieDetailsFragment
                    action = ExploreFragmentDirections.anywhereToMovieDetailsFragment(movieSelected);
            NavHostFragment.findNavController(UpcomginsFragment.this).navigate(action);
        } catch (Exception e) {
            e.getMessage();
            e.printStackTrace();
        }
    }

    @Override
    public void onItemLongClick(View view, int position) {
//        Toast.makeText(getContext(), "item "+position+" long clicked", Toast.LENGTH_SHORT).show();

    }

}// end UpcomginsFragment class