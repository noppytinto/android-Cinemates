package mirror42.dev.cinemates.ui.list;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.view.ActionMode;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import mirror42.dev.cinemates.MainActivity;
import mirror42.dev.cinemates.NavGraphDirections;
import mirror42.dev.cinemates.R;
import mirror42.dev.cinemates.adapter.RecyclerAdapterMoviesList;
import mirror42.dev.cinemates.model.User;
import mirror42.dev.cinemates.model.list.MoviesList;
import mirror42.dev.cinemates.model.tmdb.Movie;
import mirror42.dev.cinemates.ui.explore.ExploreFragmentDirections;
import mirror42.dev.cinemates.ui.login.LoginViewModel;
import mirror42.dev.cinemates.utilities.FirebaseAnalytics;

public class ListFragment extends Fragment implements
        RecyclerAdapterMoviesList.ClickAdapterListener{
    private final String TAG = getClass().getSimpleName();
    private RecyclerAdapterMoviesList recyclerAdapterMoviesList;
    private View view;
    private ArrayList<Movie> selectedMovies;
    private ListViewModel listViewModel;
    private LoginViewModel loginViewModel;
    private ActionModeCallback actionModeCallback;
    private ActionMode actionMode;
    private MoviesList.ListType listType;




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
        initRecyclerView();

    }// end onViewCreated()

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        selectedMovies = new ArrayList<>();

        //na
        FirebaseAnalytics firebaseAnalytics = FirebaseAnalytics.getInstance();
        firebaseAnalytics.logScreenEvent(this, "List page", getContext());

        //
        if(getArguments() != null) {
            ListFragmentArgs args = ListFragmentArgs.fromBundle(getArguments());
            MoviesList list = args.getList();
            listType = list.getListType();

            //
            switch (listType) {
                case WL:
                    populateEssentialList("Watchlist", list.getMovies());
                    break;
                case FV:
                    populateEssentialList("Preferiti", list.getMovies());
                    break;
                case WD:
                    populateEssentialList("Visti", list.getMovies());
                    break;
                case CL:
                    //TODO
                    break;
            }
        }
//            loginViewModel = new ViewModelProvider(requireActivity()).get(LoginViewModel.class);
////            WatchlistFragmentArgs args = WatchlistFragmentArgs.fromBundle(getArguments());
////            Movie[] ml = args.getMoviesList();
////            moviesList = new ArrayList<>(Arrays.asList(ml));
//

//
//            // setting list name
//            String listTitle = args.getListTitle();
//            if(listTitle != null) {
//                TextView textView = view.findViewById(R.id.textView_listFragment_title);
//                textView.setText(listTitle);
//            }
//
//            // setting list description
//            String listDescription = args.getListDescription();
//            if(listDescription != null ) {
//                if(!listDescription.isEmpty() || !listDescription.equals("")) {
//                    TextView textView = view.findViewById(R.id.textView_listFragment_description);
//                    textView.setText(listDescription);
//                }
//                else {
//                    TextView textView = view.findViewById(R.id.textView_listFragment_description);
//                    textView.setText(null);
//                }
//            }
//            else {
//                TextView textView = view.findViewById(R.id.textView_listFragment_description);
//                textView.setText(null);
//            }
//
//
//        }//if

    }

    private void populateEssentialList(String listName, ArrayList<Movie> movies) {
        // set list name
        TextView textView = view.findViewById(R.id.textView_listFragment_title);
        textView.setText(listName);

        // set movies list
        if(movies != null && movies.size()!=0) {
            // loading data into recycler
            recyclerAdapterMoviesList.loadNewData(movies);

            // observing selected movies list
            listViewModel = new ViewModelProvider(requireActivity()).get(ListViewModel.class);
            loginViewModel = new ViewModelProvider(requireActivity()).get(LoginViewModel.class);
        }//if
    }

    @Override
    public void onResume() {
        super.onResume();
        hideMainToolbar();
    }

    @Override
    public void onPause() {
        super.onPause();
        showMainToolbar();
    }




    //--------------------------------------------------------------------------------------- METHODS

    void initRecyclerView() {
        // defining HORIZONTAL layout manager for recycler
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 3);
        gridLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);

        // defining Recycler view
        RecyclerView recyclerView = view.findViewById(R.id.recyclerView_listFragment);
        recyclerView.setLayoutManager(gridLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        actionModeCallback = new ActionModeCallback();

        // adding recycle listener for touch detection
//        recyclerView.addOnItemTouchListener(new RecyclerListener(getContext(), recyclerView, this));

        // assigning adapter to recycle
        recyclerAdapterMoviesList = new RecyclerAdapterMoviesList(new ArrayList<>(), getContext(), this);
        recyclerView.setAdapter(recyclerAdapterMoviesList);
    }






    //---------------------- listeners methods

    @Override
    public void onItemClicked(int position) {
        //if actionmode is diabled, just navigate to the movie details page
        if(actionMode==null) {
            // enable movie details navigation
            try {
                //
                Movie movieSelected = recyclerAdapterMoviesList.getMovie(position);

                //
                FirebaseAnalytics firebaseAnalytics = FirebaseAnalytics.getInstance();
                firebaseAnalytics.logSelectedMovie(movieSelected, "selected movie in List page", this, getContext());

                //
                showMainToolbar();
                NavGraphDirections.AnywhereToMovieDetailsFragment
                        movieDetailsFragment = ExploreFragmentDirections.anywhereToMovieDetailsFragment(movieSelected);
                NavHostFragment.findNavController(this).navigate(movieDetailsFragment);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } //otw select item during actionmode enabled
        else {
            enableActionMode(position);
        }
    }

    @Override
    public void onItemLongClicked(int position) {
        enableActionMode(position);
    }





    //---------------------- action mode

    public class ActionModeCallback implements ActionMode.Callback {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            mode.getMenuInflater().inflate(R.menu.menu_movies_list, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            hideMainToolbar();
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {

                case R.id.menuItem_listMenu_delete:
                    // delete all the selected rows
                    deleteItems();
                    mode.finish();
                    return true;

//                case R.id.action_color:
//                    updateColoredRows();
//                    mode.finish();
//                    return true;

                case R.id.menuItem_listMenu_selectAll:
                    selectAll();
                    return true;

//                case R.id.action_refresh:
//                    populateDataAndSetAdapter();
//                    mode.finish();
//                    return true;

                default:
                    return false;
            }
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            recyclerAdapterMoviesList.clearSelections();
            actionMode = null;
        }
    }

    private void enableActionMode(int position) {
        if (actionMode == null) {
            actionMode = ((MainActivity)getActivity()).startSupportActionMode(actionModeCallback);
        }
        toggleSelection(position);
    }

    private void toggleSelection(int position) {
        recyclerAdapterMoviesList.toggleSelection(position);

        int count = recyclerAdapterMoviesList.getSelectedItemCount();
        if (count == 0) {
            actionMode.finish();
            actionMode = null;
        } else {
            actionMode.setTitle("selezionati: " + count);
            actionMode.invalidate();
        }
    }

    private void selectAll() {
        // select all items in recycler
        recyclerAdapterMoviesList.selectAll();

        // specify actionmode behaviour on items selected
        int count = recyclerAdapterMoviesList.getSelectedItemCount();
        try {
            if (count == 0) {
                actionMode.finish();
            }
            else {
                actionMode.setTitle("selezionati: " + String.valueOf(count));
                actionMode.invalidate();
            }
        } catch (Exception e) {
            // TODO: handle actionmode null
            e.printStackTrace();
        }

        // todo: else if(true /*TODO: count==selectedITems.size()*/) { }

        actionMode = null;
    }

    private void deleteItems() {
        // get selected item (get indexes)
        ArrayList<Integer> selectedItemPositions = recyclerAdapterMoviesList.getSelectedItems();
        selectedMovies = new ArrayList<>();

        // remove all items in the movies list
        for (int i = selectedItemPositions.size() - 1; i >= 0; i--) {
            recyclerAdapterMoviesList.removeData(selectedItemPositions.get(i));
        }
        //
        selectedMovies = recyclerAdapterMoviesList.getcurrentSelectedMovies();
        User loggedUser = loginViewModel.getObservableLoggedUser().getValue();
        listViewModel.removeMoviesFromList(selectedMovies, listType, loggedUser);

        // and notify recycler
        recyclerAdapterMoviesList.notifyDataSetChanged();


//        if (recyclerAdapterMoviesList.getItemCount() == 0)
//            fab.setVisibility(View.VISIBLE);

        actionMode = null;
    }

    public void hideMainToolbar() {
        try {
            ((MainActivity) getActivity()).hideToolbar();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void showMainToolbar() {
        try {
            ((MainActivity) getActivity()).showToolbar();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}// end ListFragment class