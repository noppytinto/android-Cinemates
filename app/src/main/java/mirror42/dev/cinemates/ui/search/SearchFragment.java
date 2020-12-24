package mirror42.dev.cinemates.ui.search;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

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

import mirror42.dev.cinemates.MainFragmentDirections;
import mirror42.dev.cinemates.R;
import mirror42.dev.cinemates.adapters.RecycleAdapterSearchPage;
import mirror42.dev.cinemates.listeners.RecyclerSearchListener;
import mirror42.dev.cinemates.tmdbAPI.Movie;

//import mirror42.dev.cinemates.NavGraphDirections;

public class SearchFragment extends Fragment implements View.OnClickListener, RecyclerSearchListener.OnClick_RecycleSearchListener, CompoundButton.OnCheckedChangeListener {
    private FirebaseAnalytics mFirebaseAnalytics;

    private final String TAG = this.getClass().getSimpleName();
    private SearchViewModel searchViewModel;
    private ArrayList<Movie> moviesList;
    private Button buttonSearch;
    private EditText editText_search;
    private String query;
    private RecycleAdapterSearchPage recycleAdapterSearchPage;
    private View view;
    private ToggleButton buttonFilter;
    private Button buttonFilterUser;
    private Button buttonFilterDirector;
    private Button buttonFilterActor;
    private Button buttonFilterMovie;
    private Button buttonFilterSelected;
    private View outsideDetector;
    private TextView textViewFilterBy;

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
        this.view = view;
        Log.d(TAG, "onViewCreated() called");
        editText_search = view.findViewById(R.id.editText_searchFragment);
        buttonSearch = view.findViewById(R.id.button_search_searchFragment);
        buttonFilter = view.findViewById(R.id.button_filter_searchFragment);
        buttonFilterUser = view.findViewById(R.id.button_user_filter);
        buttonFilterDirector = view.findViewById(R.id.button_director_filter);
        buttonFilterActor = view.findViewById(R.id.button_actor_filter);
        buttonFilterMovie = view.findViewById(R.id.button_movie_filter);
        buttonFilterSelected = view.findViewById(R.id.button_selected_filter);
        outsideDetector = view.findViewById(R.id.dummyView);
        textViewFilterBy = view.findViewById(R.id.textView_filterBy_searchFragment);

        // setting visibility
        buttonFilterMovie.setVisibility(View.GONE);
        buttonFilterActor.setVisibility(View.GONE);
        buttonFilterDirector.setVisibility(View.GONE);
        buttonFilterUser.setVisibility(View.GONE);
        buttonFilterSelected.setVisibility(View.GONE);
        outsideDetector.setVisibility(View.GONE);


        // setting listeners
        buttonFilter.setOnCheckedChangeListener(this);
        buttonSearch.setOnClickListener(this);
        buttonFilterUser.setOnClickListener(this);
        buttonFilterDirector.setOnClickListener(this);
        buttonFilterActor.setOnClickListener(this);
        buttonFilterMovie.setOnClickListener(this);
        buttonFilterSelected.setOnClickListener(this);
        outsideDetector.setOnClickListener(this);


        //
        initRecycleView();


        //
        searchViewModel = new ViewModelProvider(this).get(SearchViewModel.class);
        searchViewModel.getMoviesList().observe(getViewLifecycleOwner(), new Observer<ArrayList<Movie>>() {
            @Override
            public void onChanged(@Nullable ArrayList<Movie> movies) {
                moviesList = movies;
                recycleAdapterSearchPage.loadNewData(movies);
            }
        });


        buttonFilterMovie.onWindowFocusChanged(false);

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


    /*
                NavHostFragment.findNavController(FirstFragment.this)
                    .navigate(R.id.action_FirstFragment_to_SecondFragment2);
 */
    @Override
    public void onClick(View v) {
        if(v.getId() == buttonSearch.getId()) {
            Animation buttonAnim = AnimationUtils.loadAnimation(getContext(), R.anim.push_button_animation);
            buttonSearch.startAnimation(buttonAnim);
            query = editText_search.getText().toString();
            if( ! query.isEmpty()) {

                // send to firebase analytics
                //throw new RuntimeException("Test Crash"); // Force a crash for Crashlytics
                Bundle params = new Bundle();
                params.putString(FirebaseAnalytics.Param.SEARCH_TERM, query);

                //
                searchViewModel.init(query);
            }
            else {
                Toast.makeText(getContext(), "Campo Cerca vuoto", Toast.LENGTH_SHORT).show();

            }


        }
        else if(v.getId() == buttonFilterMovie.getId()) {
            buttonFilter.setVisibility(View.GONE);
            setupSelectedFiler(getResources().getString(R.string.movie));
            outsideDetector.setVisibility(View.GONE);
            buttonFilter.setChecked(false);
        }
        else if(v.getId() == buttonFilterActor.getId()) {
            buttonFilter.setVisibility(View.GONE);
            setupSelectedFiler(getResources().getString(R.string.actor));
            outsideDetector.setVisibility(View.GONE);
            buttonFilter.setChecked(false);
        }
        else if(v.getId() == buttonFilterDirector.getId()) {
            buttonFilter.setVisibility(View.GONE);
            setupSelectedFiler(getResources().getString(R.string.director));
            outsideDetector.setVisibility(View.GONE);
            buttonFilter.setChecked(false);
        }
        else if(v.getId() == buttonFilterUser.getId()) {
            buttonFilter.setVisibility(View.GONE);
            setupSelectedFiler(getResources().getString(R.string.user));
            outsideDetector.setVisibility(View.GONE);
            buttonFilter.setChecked(false);
        }
        else if(v.getId() == buttonFilterSelected.getId()) {
            buttonFilterSelected.setVisibility(View.GONE);
            buttonFilter.setVisibility(View.VISIBLE);
            buttonFilter.setChecked(false);
        }
        else if(v.getId() == outsideDetector.getId()) {
            hideFilters();
        }




    }

    private void hideFilters() {
        outsideDetector.setVisibility(View.GONE);
        buttonFilter.setChecked(false);
    }


    private void setupSelectedFiler(String filterName) {
        buttonFilterSelected.setText(filterName);
        buttonFilterSelected.setVisibility(View.VISIBLE);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (isChecked) {
            // The toggle is enabled
            buttonFilterMovie.setVisibility(View.VISIBLE);
            buttonFilterActor.setVisibility(View.VISIBLE);
            buttonFilterDirector.setVisibility(View.VISIBLE);
            buttonFilterUser.setVisibility(View.VISIBLE);
            outsideDetector.setVisibility(View.VISIBLE);
            textViewFilterBy.setVisibility(View.VISIBLE);
        } else {
            // The toggle is disabled
            buttonFilterMovie.setVisibility(View.GONE);
            buttonFilterActor.setVisibility(View.GONE);
            buttonFilterDirector.setVisibility(View.GONE);
            buttonFilterUser.setVisibility(View.GONE);
            textViewFilterBy.setVisibility(View.GONE);
        }
    }





    //--------------------------------- RECYCLER METHODS

    private void initRecycleView() {
        // defining Recycler view
        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.recyclerView_searchFragment);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // adding recycle listener for touch detection
        recyclerView.addOnItemTouchListener(new RecyclerSearchListener(getContext(), recyclerView, this));
        recycleAdapterSearchPage = new RecycleAdapterSearchPage(new ArrayList<Movie>(), getContext());
        recyclerView.setAdapter(recycleAdapterSearchPage);
    }


    @Override
    public void onItemClick(View view, int position) {
        try {

            Movie movieSelected = recycleAdapterSearchPage.getMoviesList(position);

            // firebase analytics
            Bundle item1 = new Bundle();
            item1.putString(FirebaseAnalytics.Param.ITEM_NAME, movieSelected.getTitle());
            item1.putString(FirebaseAnalytics.Param.ITEM_CATEGORY, "selected movie in search tab");
            Bundle params = new Bundle();
            params.putParcelableArray(FirebaseAnalytics.Param.ITEMS, new Bundle[]{item1});

            //
            MainFragmentDirections.ActionMainFragmentToMovieDetailsFragment
                    action = MainFragmentDirections.actionMainFragmentToMovieDetailsFragment(recycleAdapterSearchPage.getMoviesList(position));
            NavHostFragment.findNavController(SearchFragment.this).navigate(action);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onItemLongClick(View view, int position) {
        Toast.makeText(getContext(), "item "+position+" long clicked", Toast.LENGTH_SHORT).show();

    }


}// end SearchFragment class