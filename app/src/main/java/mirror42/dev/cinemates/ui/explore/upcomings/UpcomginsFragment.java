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

import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.ArrayList;

import mirror42.dev.cinemates.NavGraphDirections;
import mirror42.dev.cinemates.R;
import mirror42.dev.cinemates.adapters.RecyclerAdapterExplorePage;
import mirror42.dev.cinemates.listeners.RecyclerSearchListener;
import mirror42.dev.cinemates.tmdbAPI.Movie;
import mirror42.dev.cinemates.ui.explore.ExploreFragmentDirections;


public class UpcomginsFragment extends Fragment implements
        RecyclerSearchListener.OnClick_RecycleSearchListener {

    private UpcomginsViewModel upcomginsViewModel;
    private FirebaseAnalytics mFirebaseAnalytics;
    private RecyclerAdapterExplorePage recyclerAdapterExplorePage;
    private View view;
//    private ArrayList<Movie> moviesList;
//    private final String MOVIES_LIST_KEY = "MOVIE_LIST_KEY";



    //------------------------------------------------------------------------ LIFECYCLE METHODS

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(getContext());

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
                    recyclerAdapterExplorePage.loadNewData(movies);
                }
                else {
                    Toast toast = Toast.makeText(getContext(), "errore caricamento Prossimamente", Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                }
            }
        });

        // downloading data
        upcomginsViewModel.downloadData();
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
        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.recyclerView_upcoming_newsPage);
        recyclerView.setLayoutManager(linearLayoutManager);

        // adding recycle listener for touch detection
        recyclerView.addOnItemTouchListener(new RecyclerSearchListener(getContext(), recyclerView, this));

        // assigning adapter to recycle
        recyclerAdapterExplorePage = new RecyclerAdapterExplorePage(new ArrayList<Movie>(), getContext());
        recyclerView.setAdapter(recyclerAdapterExplorePage);
    }






    //------------------------------------------------------ RECYCLER VIEW LISTENER METHODS
    @Override
    public void onItemClick(View view, int position) {
        try {
            //
            Movie movieSelected = recyclerAdapterExplorePage.getMoviesList(position);

            //
            logMovieSelected(movieSelected);

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

    private void logMovieSelected(Movie movieSelected) {
        // send to firebase analytics
        Bundle item = new Bundle();
        item.putString(FirebaseAnalytics.Param.ITEM_CATEGORY, "selected movie in explore tab");
        item.putString(FirebaseAnalytics.Param.ITEM_ID, String.valueOf(movieSelected.getTmdbID()));
        item.putString(FirebaseAnalytics.Param.ITEM_NAME, movieSelected.getTitle());
        item.putString(FirebaseAnalytics.Param.SCREEN_CLASS, getClass().getSimpleName());
//        Bundle params = new Bundle();
//        params.putParcelableArray(FirebaseAnalytics.Param.ITEMS, new Bundle[]{item1});
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_ITEM, item);
    }


}// end UpcomginsFragment class