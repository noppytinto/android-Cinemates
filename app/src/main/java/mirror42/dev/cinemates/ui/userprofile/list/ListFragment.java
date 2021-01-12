package mirror42.dev.cinemates.ui.userprofile.list;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
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
    private RecyclerAdapterMoviesList recyclerAdapterMoviesList;
    private View view;







    //--------------------------------------------------------------------------------------- ANDROID METHODS

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

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
            ArrayList<Movie> moviesList = new ArrayList<>(Arrays.asList(ml));

            if(moviesList != null && moviesList.size()!=0) {
                recyclerAdapterMoviesList.loadNewData(moviesList);
            }

            String listTitle = args.getListTitle();
            if(listTitle != null) {
                TextView textView = view.findViewById(R.id.textView_listFragment_title);
                textView.setText(listTitle);
            }

            String listDescription = args.getListDescription();
            if(listDescription != null ) {
                if(!listDescription.isEmpty() || !listDescription.equals("")) {
                    TextView textView = view.findViewById(R.id.textView_listFragment_description);
                    textView.setText(listDescription);
                }
                else {
                    TextView textView = view.findViewById(R.id.textView_listFragment_description);
                    textView.setText(null);
                }
            }
            else {
                TextView textView = view.findViewById(R.id.textView_listFragment_description);
                textView.setText(null);
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
            firebaseAnalytics.logSelectedMovie(movieSelected, "selected movie in List page", this, getContext());

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

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();

        if(itemId == R.id.menu_item_login) {
            //ignore clicks on login menu item
            Navigation.findNavController(view).popBackStack();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu) {
        super.onPrepareOptionsMenu(menu);

        MenuItem menuItem = menu.getItem(1);
        menuItem.setEnabled(false);

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
        recyclerAdapterMoviesList = new RecyclerAdapterMoviesList(new ArrayList<>(), getContext());
        recyclerView.setAdapter(recyclerAdapterMoviesList);
    }



}// end ListFragment class