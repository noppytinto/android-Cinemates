package mirror42.dev.cinemates.ui.userprofile.list;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import mirror42.dev.cinemates.NavGraphDirections;
import mirror42.dev.cinemates.R;
import mirror42.dev.cinemates.listener.RecyclerListener;
import mirror42.dev.cinemates.model.User;
import mirror42.dev.cinemates.tmdbAPI.model.Movie;
import mirror42.dev.cinemates.ui.explore.ExploreFragmentDirections;
import mirror42.dev.cinemates.ui.login.LoginViewModel;
import mirror42.dev.cinemates.ui.moviedetails.MovieDetailsFragmentArgs;
import mirror42.dev.cinemates.ui.moviedetails.MovieDetailsViewModel;
import mirror42.dev.cinemates.ui.userprofile.list.watchlist.RecyclerAdapterMoviesList;
import mirror42.dev.cinemates.utilities.FirebaseAnalytics;

public class ListFragment extends Fragment implements
        RecyclerListener.OnClick_RecyclerListener {
    private final String TAG = getClass().getSimpleName();
    private ListViewModel listViewModel;
    private ListViewModel loginViewModel;
    private RecyclerAdapterMoviesList recyclerAdapterMoviesList;
    private View view;







    //--------------------------------------------------------------------------------------- ANDROID METHODS


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_list, container, false);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.view = view;

        //
        FirebaseAnalytics firebaseAnalytics = FirebaseAnalytics.getInstance();
        firebaseAnalytics.logScreenEvent(this, "List page", getContext());

        //
        initRecyclerView();

        //
        if(getArguments() != null) {
            ListFragmentArgs args = ListFragmentArgs.fromBundle(getArguments());
            Movie[] ml = args.getMoviesList();
            ArrayList<Movie> moviesList = new ArrayList<Movie>(Arrays.asList(ml));

            if(moviesList != null && moviesList.size()!=0) {
                recyclerAdapterMoviesList.loadNewData(moviesList);
            }
        }


    }// end onViewCreated()

    @Override
    public void onItemClick(View view, int position) {
        try {
            //
            Movie movieSelected = recyclerAdapterMoviesList.getMoviesList(position);

            //
            FirebaseAnalytics firebaseAnalytics = FirebaseAnalytics.getInstance();
            firebaseAnalytics.logSelectedMovie(movieSelected, "selected movie in Watchlist", this, getContext());

            //
            NavGraphDirections.AnywhereToMovieDetailsFragment
                    action = ExploreFragmentDirections.anywhereToMovieDetailsFragment(movieSelected);
            NavHostFragment.findNavController(ListFragment.this).navigate(action);
        } catch (Exception e) {
            e.getMessage();
            e.printStackTrace();
        }
    }

    @Override
    public void onItemLongClick(View view, int position) {
        //ignored
    }




    //--------------------------------------------------------------------------------------- METHODS




    void initRecyclerView() {
        // defining HORIZONTAL layout manager for recycler
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 3);
        gridLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);

        // defining Recycler view
        RecyclerView recyclerView = view.findViewById(R.id.recyclerView_listFragment);
        recyclerView.setLayoutManager(gridLayoutManager);

        // adding recycle listener for touch detection
        recyclerView.addOnItemTouchListener(new RecyclerListener(getContext(), recyclerView, this));

        // assigning adapter to recycle
        recyclerAdapterMoviesList = new RecyclerAdapterMoviesList(new ArrayList<Movie>(), getContext());
        recyclerView.setAdapter(recyclerAdapterMoviesList);
    }



}// end ListFragment class