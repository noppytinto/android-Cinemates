package mirror42.dev.cinemates.ui.explore.popular;

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


public class PopularFragment extends Fragment  implements
        RecyclerSearchListener.OnClick_RecycleSearchListener {

    private PopularViewModel popularViewModel;
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
        return inflater.inflate(R.layout.fragment_popular, container, false);
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.view = view;

        //1
        initRecyclerView();

        //2
        popularViewModel = new ViewModelProvider(this).get(PopularViewModel.class);
        popularViewModel.getMoviesList().observe(getViewLifecycleOwner(), new Observer<ArrayList<Movie>>() {
            @Override
            public void onChanged(@Nullable ArrayList<Movie> movies) {
                if(movies!=null) {
                    recyclerAdapterExplorePage.loadNewData(movies);
                }
                else {
                    Toast toast = Toast.makeText(getContext(), "errore caricamento Popolari", Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                }
            }
        });

        // downloading data
        popularViewModel.downloadData();
    }// end onViewCreated()




//
//    @Override
//    public void onSaveInstanceState(@NonNull Bundle outState) {
//        super.onSaveInstanceState(outState);
//        outState.putParcelableArrayList(MOVIES_LIST_KEY, (ArrayList<Movie>) moviesList);
//
//    }






    //------------------------------------------------------------------------ METHODS
    public void initRecyclerView() {
        // defining HORIZONTAL layout manager for recycler
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);

        // defining Recycler view
        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.recyclerView_popular_newsPage);
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

            // sending movie clicked to movie details frag
            NavGraphDirections.AnywhereToMovieDetailsFragment
                    action = ExploreFragmentDirections.anywhereToMovieDetailsFragment(movieSelected);
            NavHostFragment.findNavController(PopularFragment.this).navigate(action);
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
        Bundle item1 = new Bundle();
        item1.putString(FirebaseAnalytics.Param.ITEM_NAME, movieSelected.getTitle());
        item1.putString(FirebaseAnalytics.Param.ITEM_CATEGORY, "selected movie in explore tab");
        Bundle params = new Bundle();
        params.putParcelableArray(FirebaseAnalytics.Param.ITEMS, new Bundle[]{item1});
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Param.ITEMS, params);
    }

}// end PopularFragment class