package mirror42.dev.cinemates.ui.search;

import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.jakewharton.rxbinding4.widget.RxTextView;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import mirror42.dev.cinemates.NavGraphDirections;
import mirror42.dev.cinemates.R;
import mirror42.dev.cinemates.adapter.RecyclerAdapterSearchPage;
import mirror42.dev.cinemates.listener.RecyclerListener;
import mirror42.dev.cinemates.tmdbAPI.model.Movie;
import mirror42.dev.cinemates.ui.search.SearchViewModel.SearchType;
import mirror42.dev.cinemates.utilities.FirebaseAnalytics;


public class SearchFragment extends Fragment implements View.OnClickListener,
        RecyclerListener.OnClick_RecyclerListener,
        ChipGroup.OnCheckedChangeListener {

    private final String TAG = this.getClass().getSimpleName();
    private SearchViewModel searchViewModel;
    private FloatingActionButton buttonSearch;
    private TextInputEditText editText_search;
    private TextInputLayout textInputLayout;
    private String currentSearchTerm;
    private RecyclerAdapterSearchPage recyclerAdapterSearchPage;
    private View view;
    private ChipGroup chipGroup;
    private Chip chipMovie;
    private Chip chipActor;
    private Chip chipDirector;
    private Chip chipUser;
    private FirebaseAnalytics firebaseAnalytics;
    private SearchType searchType;



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

        View view = inflater.inflate(R.layout.fragment_search, container, false);
        return view;
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(TAG, "onViewCreated() called");
        this.view = view;
        editText_search = view.findViewById(R.id.editText_searchFragment);
        textInputLayout = view.findViewById(R.id.editTextLayout_searchFragment);
        buttonSearch = view.findViewById(R.id.button_searchFragment_search);
        chipGroup = view.findViewById(R.id.chipGroup_searchFragment);
        chipMovie = view.findViewById(R.id.chip_searchFragment_movie);
        chipActor = view.findViewById(R.id.chip_searchFragment_actor);
        chipDirector = view.findViewById(R.id.chip_searchFragment_director);
        chipUser = view.findViewById(R.id.chip_searchFragment_user);

        // setting listeners
        buttonSearch.setOnClickListener(this);
        chipGroup.setOnCheckedChangeListener(this);

        //
        initRecycleView();

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        firebaseAnalytics = FirebaseAnalytics.getInstance();
        searchType = SearchType.UNIVERSAL;

        //
        searchViewModel = new ViewModelProvider(this).get(SearchViewModel.class);
        searchViewModel.getMoviesList().observe(getViewLifecycleOwner(), new Observer<ArrayList<Movie>>() {
            @Override
            public void onChanged(@Nullable ArrayList<Movie> movies) {
                if (movies != null) {
                    recyclerAdapterSearchPage.loadNewData(movies);
                } else {
                    Toast toast = Toast.makeText(getContext(), "Nessun risultato per: " + currentSearchTerm, Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                }
            }
        });

        // AUTOMATIC SEARCH
        // triggered when:
        // at least 3 chars are typed in (filter)
        // 1 second later after the last typed character (debounce)
        RxTextView.textChanges(editText_search)
                .filter(text -> text.length()>=3)
                .debounce(1, TimeUnit.SECONDS) /*NOTES: 1 seconds seems to be the sweetspot*/
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::updateSearchResults);

    }// end onActivityCreated()

    private void updateSearchResults(CharSequence searchQuery) {
        currentSearchTerm = searchQuery.toString();
        searchViewModel.fetchResults(currentSearchTerm, searchType);
    }


    @Override
    public void onClick(View v) {
        if(v.getId() == buttonSearch.getId()) {
            Animation buttonAnim = AnimationUtils.loadAnimation(getContext(), R.anim.push_button_animation);
            buttonSearch.startAnimation(buttonAnim);
            currentSearchTerm = editText_search.getText().toString();

            if( ! currentSearchTerm.isEmpty()) {
                firebaseAnalytics.logSearchTerm(currentSearchTerm, this, getContext());

                //
                textInputLayout.setError(null);
                editText_search.onEditorAction(EditorInfo.IME_ACTION_DONE); // hide keyboard on search button press

                //
                searchViewModel.fetchResults(currentSearchTerm, searchType);
            }
            else {
                textInputLayout.setError("Campo vuoto");
                Toast toast = Toast.makeText(getContext(),"Campo vuoto", Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
            }
        }
    }

    @Override
    public void onCheckedChanged(ChipGroup group, int checkedId) {
        //NOTE: checkedId is -1 no chip is checked
        if(checkedId == R.id.chip_searchFragment_movie) {
            searchType = SearchType.MOVIE;
            textInputLayout.setHint("Cerca film");
        }
        else if(checkedId == R.id.chip_searchFragment_actor) {
            searchType = SearchType.ACTOR;
            textInputLayout.setHint("Cerca attore");
        }
        else if(checkedId == R.id.chip_searchFragment_director) {
            searchType = SearchType.DIRECTOR;
            textInputLayout.setHint("Cerca regista");
        }
        else if(checkedId == R.id.chip_searchFragment_user) {
            searchType = SearchType.USER;
            textInputLayout.setHint("Cerca utente");
        }
        else {
            searchType = SearchType.UNIVERSAL;
            textInputLayout.setHint("Cerca tutto");
        }

    }// end onCheckedChanged()





    //------------------------------------------------------------------------ METHODS





    //--------------------------------- RECYCLER METHODS

    private void initRecycleView() {
        // defining Recycler view
        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.recyclerView_searchFragment);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // adding recycle listener for touch detection
        recyclerView.addOnItemTouchListener(new RecyclerListener(getContext(), recyclerView, this));
        recyclerAdapterSearchPage = new RecyclerAdapterSearchPage(new ArrayList<>(), getContext());
        recyclerView.setAdapter(recyclerAdapterSearchPage);
    }

    @Override
    public void onItemClick(View view, int position) {
        try {
            Movie movieSelected = recyclerAdapterSearchPage.getMoviesList(position);

            firebaseAnalytics.logSelectedMovie(movieSelected, "selected movie in search tab", this, getContext());

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



}// end SearchFragment class