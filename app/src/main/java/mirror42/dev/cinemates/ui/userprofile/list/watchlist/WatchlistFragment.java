package mirror42.dev.cinemates.ui.userprofile.list.watchlist;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Arrays;

import mirror42.dev.cinemates.MainActivity;
import mirror42.dev.cinemates.R;
import mirror42.dev.cinemates.listener.RecyclerListener;
import mirror42.dev.cinemates.model.User;
import mirror42.dev.cinemates.tmdbAPI.model.Movie;
import mirror42.dev.cinemates.ui.login.LoginViewModel;
import mirror42.dev.cinemates.utilities.FirebaseAnalytics;

public class WatchlistFragment extends Fragment implements
        RecyclerListener.OnClick_RecyclerListener {
    private final String TAG = getClass().getSimpleName();
    private RecyclerAdapterMoviesList recyclerAdapterMoviesList;
    private View view;
    private boolean isEnabled;
    private ArrayList<Movie> selectedMovies;
    private boolean isSelectAll;
    private ArrayList<Movie> moviesList;
    private WatchlistViewModel watchlistViewModel;
    private LoginViewModel loginViewModel;
    private boolean listIsEmpty;




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
        selectedMovies = new ArrayList<>();

        //
        FirebaseAnalytics firebaseAnalytics = FirebaseAnalytics.getInstance();
        firebaseAnalytics.logScreenEvent(this, "Watchlist page", getContext());

        //
        initRecyclerView();

        //
        if(getArguments() != null) {
            WatchlistFragmentArgs args = WatchlistFragmentArgs.fromBundle(getArguments());
            Movie[] ml = args.getMoviesList();
            moviesList = new ArrayList<>(Arrays.asList(ml));

            if(moviesList != null && moviesList.size()!=0) {
                // loading data into recycler
                recyclerAdapterMoviesList.loadNewData(moviesList);
                listIsEmpty = false;

                // observing selected movies list
                watchlistViewModel = new ViewModelProvider(requireActivity()).get(WatchlistViewModel.class);
                watchlistViewModel.getSelectedMovies().observe(getViewLifecycleOwner(), selectedMovies-> {
                    if(selectedMovies!=null) {

                    }
                });
            }//if

            // setting list name
            String listTitle = args.getListTitle();
            if(listTitle != null) {
                TextView textView = view.findViewById(R.id.textView_listFragment_title);
                textView.setText(listTitle);
            }

            // setting list description
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

            loginViewModel = new ViewModelProvider(requireActivity()).get(LoginViewModel.class);
            loginViewModel.getLoginResult().observe(getViewLifecycleOwner(), loginResult -> {
//                switch (loginResult) {
//                    case SUCCESS: case REMEMBER_ME_EXISTS:
//                        addToListButton.setVisibility(View.VISIBLE);
//                        break;
//                    default:
//                        addToListButton.setVisibility(View.GONE);
//                }

            });


        }//if
    }// end onViewCreated()

    @Override
    public void onItemClick(View view, int position) {

    }// end onItemClick()

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

    @Override
    public void onDetach() {
        super.onDetach();
        recyclerAdapterMoviesList.releaseFragment();
    }

    public void removeMoviesFromList(ArrayList<Movie> moviesToRemove) {
        User user = loginViewModel.getLoggedUser().getValue();
        watchlistViewModel.removeMoviesFromList(moviesToRemove, user.getEmail(), user.getAccessToken());
        listIsEmpty = true;
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
//        recyclerView.addOnItemTouchListener(new RecyclerListener(getContext(), recyclerView, this));

        // assigning adapter to recycle
        recyclerAdapterMoviesList = new RecyclerAdapterMoviesList(new ArrayList<>(), getContext(), this);
        recyclerView.setAdapter(recyclerAdapterMoviesList);
    }

    public void hideMainToolbar() {
        MainActivity mainActivity = (MainActivity) getActivity();
        try {
            mainActivity.hideToolbar();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void showMainToolbar() {
        MainActivity mainActivity = (MainActivity) getActivity();
        try {
            mainActivity.showToolbar();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}// end ListFragment class