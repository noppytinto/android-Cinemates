package mirror42.dev.cinemates.ui.search;

import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.ChipGroup;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.ArrayList;

import mirror42.dev.cinemates.NavGraphDirections;
import mirror42.dev.cinemates.R;
import mirror42.dev.cinemates.adapter.RecyclerAdapterSearchPage;
import mirror42.dev.cinemates.listener.RecyclerSearchListener;
import mirror42.dev.cinemates.tmdbAPI.Movie;

//import mirror42.dev.cinemates.NavGraphDirections;

public class SearchFragment extends Fragment implements View.OnClickListener,
        RecyclerSearchListener.OnClick_RecycleSearchListener {

    private FirebaseAnalytics mFirebaseAnalytics;
    private final String TAG = this.getClass().getSimpleName();
    private SearchViewModel searchViewModel;
    private Button buttonSearch;
    private EditText editText_search;
    private String currentSearchTerm;
    private RecyclerAdapterSearchPage recyclerAdapterSearchPage;
    private View view;
    private ChipGroup chipGroup;



    //------------------------------------------------------------------------ LIFECYCLE METHODS

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
//        final EditText editText = view.findViewById(R.id.editText_searchFragment);
//        searchViewModel.getQuery().observe(getViewLifecycleOwner(), new Observer<String>() {
//            @Override
//            public void onChanged(@Nullable String s) {
//                editText.setText(s);
//            }
//        });

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(getContext());

        return inflater.inflate(R.layout.fragment_search, container, false);
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(TAG, "onViewCreated() called");
        this.view = view;
        editText_search = view.findViewById(R.id.editText_searchFragment);
        buttonSearch = view.findViewById(R.id.button_search_searchFragment);
        chipGroup = view.findViewById(R.id.chipGroup_searchFragment);

        // setting listeners
        buttonSearch.setOnClickListener(this);

        //
        initRecycleView();

        //
        searchViewModel = new ViewModelProvider(this).get(SearchViewModel.class);
        searchViewModel.getMoviesList().observe(getViewLifecycleOwner(), new Observer<ArrayList<Movie>>() {
            @Override
            public void onChanged(@Nullable ArrayList<Movie> movies) {
                if(movies!=null) {
                    recyclerAdapterSearchPage.loadNewData(movies);
                }
                else {
                    Toast toast = Toast.makeText(getContext(), "Nessun risultato per: " + currentSearchTerm, Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                }
            }
        });


        logFirebaseScreenEvent();

        // assigning adapter to recycle


//        //restoring state if configuration changes
//        restoreStateIfRotated(savedInstanceState);
//        restoreStateIfReplaced();
    }

//    @Override
//    public void onSaveInstanceState(@NonNull Bundle outState) {
//        super.onSaveInstanceState(outState);
//        Log.d(TAG, "onSaveInstanceState() called");
//
//        outState.putString(QUERY_KEY, query);
//        outState.putParcelableArrayList(MOVIES_LIST_KEY, moviesList);
//    }
//
//    @Override
//    public void onPause() {
//        super.onPause();
//        Log.d(TAG, "onPause() called");
//    }
//
//    @Override
//    public void onStop() {
//        super.onStop();
//        savedState = new Bundle();
//        savedState.putString(QUERY_KEY, query);
//        savedState.putParcelableArrayList(MOVIES_LIST_KEY, moviesList);
//        Log.d(TAG, "onStop() called");
//
//    }
//
//    @Override
//    public void onDestroyView() {
//        super.onDestroyView();
//        Log.d(TAG, "onDestroyView() called");
//
//    }
//
//    @Override
//    public void onDestroy() {
//        super.onDestroy();
//        Log.d(TAG, "onDestroy() called");
//
//    }
//
//    @Override
//    public void onDetach() {
//        super.onDetach();
//        Log.d(TAG, "onDetach() called");
//
//    }
//
//    private void restoreStateIfRotated(Bundle savedInstanceState) {
//        if(savedInstanceState != null) {
//            query = savedInstanceState.getString(QUERY_KEY);
//            moviesList = savedInstanceState.getParcelableArrayList(MOVIES_LIST_KEY);
//
//            //
//            editText_search.setText(query);
//            recycleAdapterSearchPage.loadNewData(moviesList);
//        }
//    }
//
//    private void restoreStateIfReplaced() {
//        if(savedState!=null) {
//            try {
//                query = savedState.getString(QUERY_KEY);
//                moviesList = savedState.getParcelableArrayList(MOVIES_LIST_KEY);
//
//                //
//                editText_search.setText(query);
//                recycleAdapterSearchPage.loadNewData(moviesList);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
//    }




    //------------------------------------------------------------------------ METHODS

    @Override
    public void onClick(View v) {
        if(v.getId() == buttonSearch.getId()) {
            Animation buttonAnim = AnimationUtils.loadAnimation(getContext(), R.anim.push_button_animation);
            buttonSearch.startAnimation(buttonAnim);
            currentSearchTerm = editText_search.getText().toString();

            if( ! currentSearchTerm.isEmpty()) {
                logSearchTerm(currentSearchTerm);

                //

                //
                searchViewModel.init(currentSearchTerm);
            }
            else {
                Toast.makeText(getContext(), "Campo Cerca vuoto", Toast.LENGTH_SHORT).show();

            }
        }
    }



    //--------------------------------- RECYCLER METHODS

    private void initRecycleView() {
        // defining Recycler view
        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.recyclerView_searchFragment);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // adding recycle listener for touch detection
        recyclerView.addOnItemTouchListener(new RecyclerSearchListener(getContext(), recyclerView, this));
        recyclerAdapterSearchPage = new RecyclerAdapterSearchPage(new ArrayList<Movie>(), getContext());
        recyclerView.setAdapter(recyclerAdapterSearchPage);
    }

    @Override
    public void onItemClick(View view, int position) {
        try {
            Movie movieSelected = recyclerAdapterSearchPage.getMoviesList(position);

            logMovieSelected(movieSelected);

//            NavHostFragment.findNavController(FirstFragment.this)
//                    .navigate(R.id.action_FirstFragment_to_SecondFragment);

            // passing movie to MovieDetailsFragment
//            MainFragmentDirections.ActionMainFragmentToMovieDetailsFragment
//                    action = MainFragmentDirections.actionMainFragmentToMovieDetailsFragment(recycleAdapterSearchPage.getMoviesList(position));
//            NavHostFragment.findNavController(SearchFragment.this).navigate(action);


            NavGraphDirections.AnywhereToMovieDetailsFragment action = SearchFragmentDirections.anywhereToMovieDetailsFragment(movieSelected);
            NavHostFragment.findNavController(SearchFragment.this).navigate(action);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onItemLongClick(View view, int position) {
        Toast.makeText(getContext(), "item "+position+" long clicked", Toast.LENGTH_SHORT).show();

    }

    private void logMovieSelected(Movie movieSelected) {
        // send to firebase analytics
        Bundle item = new Bundle();
        item.putString(FirebaseAnalytics.Param.ITEM_CATEGORY, "selected movie in search tab");
        item.putString(FirebaseAnalytics.Param.ITEM_ID, String.valueOf(movieSelected.getTmdbID()));
        item.putString(FirebaseAnalytics.Param.ITEM_NAME, movieSelected.getTitle());
        item.putString(FirebaseAnalytics.Param.SCREEN_CLASS, getClass().getSimpleName());
//        Bundle params = new Bundle();
//        params.putParcelableArray(FirebaseAnalytics.Param.ITEMS, new Bundle[]{item1});
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_ITEM, item);
    }

    private void logSearchTerm(String term) {
        // send to firebase analytics
        //throw new RuntimeException("Test Crash"); // Force a crash for Crashlytics
        Bundle params = new Bundle();
        params.putString(FirebaseAnalytics.Param.SEARCH_TERM, term);
        params.putString(FirebaseAnalytics.Param.SCREEN_CLASS, getClass().getSimpleName());
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SEARCH, params);
    }

    private void logFirebaseScreenEvent() {
        // send to firebase analytics
        Bundle item = new Bundle();
        item.putString(FirebaseAnalytics.Param.SCREEN_CLASS, getClass().getSimpleName());
        item.putString(FirebaseAnalytics.Param.SCREEN_NAME, "Explore tab");
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, item);
    }


}// end SearchFragment class