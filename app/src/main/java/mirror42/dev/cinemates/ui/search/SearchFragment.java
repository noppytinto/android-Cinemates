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
import android.widget.TextView;
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
import mirror42.dev.cinemates.model.User;
import mirror42.dev.cinemates.tmdbAPI.model.Movie;
import mirror42.dev.cinemates.ui.login.LoginViewModel;
import mirror42.dev.cinemates.ui.search.model.MovieSearchResult;
import mirror42.dev.cinemates.ui.search.model.SearchResult;
import mirror42.dev.cinemates.ui.search.model.SearchResult.SearchType;
import mirror42.dev.cinemates.ui.search.model.UserSearchResult;
import mirror42.dev.cinemates.utilities.FirebaseAnalytics;


public class SearchFragment extends Fragment implements View.OnClickListener,
        ChipGroup.OnCheckedChangeListener,
        RecyclerAdapterSearchPage.SearchResultListener {
    private final String TAG = this.getClass().getSimpleName();
    private SearchViewModel searchViewModel;
    private FloatingActionButton buttonSearch;
    private TextInputEditText editTextSearch;
    private TextInputLayout textInputLayout;
    private String currentSearchTerm;
    private RecyclerAdapterSearchPage recyclerAdapterSearchPage;
    private View view;
    private ChipGroup chipGroup;
    private FirebaseAnalytics firebaseAnalytics;
    private SearchResult.SearchType searchType;
    private LoginViewModel loginViewModel;
    private Chip chipUsersFilter;
    private User loggedUser;
    private TextView textViewTitle;
    private RecyclerView recyclerView;
    private String previousSearchTerm; //if the new search term equals the previous one, then don't start any search
    private boolean searchButtonPressed;


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

        return inflater.inflate(R.layout.fragment_search, container, false);
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(TAG, "onViewCreated() called");
        this.view = view;
        editTextSearch = view.findViewById(R.id.editText_searchFragment);
        textInputLayout = view.findViewById(R.id.editTextLayout_searchFragment);
        buttonSearch = view.findViewById(R.id.button_searchFragment_search);
        chipGroup = view.findViewById(R.id.chipGroup_searchFragment);
        chipUsersFilter = view.findViewById(R.id.include_searchFragment_searchBox).findViewById(R.id.chip_searchFragment_user);
        textViewTitle = view.findViewById(R.id.textView_searchFragment_title);

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
        searchType = SearchResult.SearchType.UNIVERSAL;

        //
        searchViewModel = new ViewModelProvider(this).get(SearchViewModel.class);
        searchViewModel.getSearchResultList().observe(getViewLifecycleOwner(), new Observer<ArrayList<SearchResult>>() {
            @Override
            public void onChanged(@Nullable ArrayList<SearchResult> results) {
                textViewTitle.setVisibility(View.VISIBLE);
                searchButtonPressed = false;
                if (results != null && results.size()>0) {
                    textViewTitle.setText("Risultati per: " + currentSearchTerm);
                    recyclerAdapterSearchPage.loadNewData(results);
                }
                else {
                    recyclerAdapterSearchPage.loadNewData(null);
                    textViewTitle.setText("Nessun risultato per: " + currentSearchTerm);
                    final Toast toast = Toast.makeText(getContext(), "Nessun risultato per: " + currentSearchTerm, Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                }
            }
        });

        //
        loginViewModel = new ViewModelProvider(requireActivity()).get(LoginViewModel.class);
        loginViewModel.getLoginResult().observe(getViewLifecycleOwner(), loginResult -> {
            switch (loginResult) {
                case SUCCESS: case REMEMBER_ME_EXISTS: {
                    chipUsersFilter.setVisibility(View.VISIBLE);
                    loggedUser = loginViewModel.getLoggedUser().getValue();
                }
                break;
                default:
                    textInputLayout.setHint("Cerca tutto");
                    searchType = SearchType.UNIVERSAL;
                    chipUsersFilter.setVisibility(View.GONE);
            }
        });

        // AUTOMATIC SEARCH
        // triggered when:
        // at least 3 chars are typed in, and search button hasn't been pressed yet (filter)
        // 1 second later after the last typed character (debounce)
        RxTextView.textChanges(editTextSearch)
                .filter(text -> text.length()>=3 && !searchButtonPressed)
                .debounce(1, TimeUnit.SECONDS) /*NOTES: 1 seconds seems to be the sweetspot*/
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::updateSearchResults);

    }// end onActivityCreated()

    private void updateSearchResults(CharSequence searchQuery) {
        currentSearchTerm = searchQuery.toString();
        if( ! currentSearchTerm.equals(previousSearchTerm)) {
            previousSearchTerm = currentSearchTerm;
            searchViewModel.fetchResults(currentSearchTerm, searchType, loggedUser);
        }
    }


    @Override
    public void onClick(View v) {
        if(v.getId() == buttonSearch.getId()) {
            searchButtonPressed = true;
            Animation buttonAnim = AnimationUtils.loadAnimation(getContext(), R.anim.push_button_animation);
            buttonSearch.startAnimation(buttonAnim);
            currentSearchTerm = editTextSearch.getText().toString();

            if( ! currentSearchTerm.isEmpty()) {
                if( ! currentSearchTerm.equals(previousSearchTerm)) {
                    previousSearchTerm = currentSearchTerm;
                    firebaseAnalytics.logSearchTerm(currentSearchTerm, this, getContext());

                    //
                    textInputLayout.setError(null);
                    editTextSearch.onEditorAction(EditorInfo.IME_ACTION_DONE); // hide keyboard on search button press

                    //
                    searchViewModel.fetchResults(currentSearchTerm, searchType, loggedUser);
                }
                else {
                    searchButtonPressed = false;
                }
            }
            else {
                textInputLayout.setError("Campo vuoto");
                final Toast toast = Toast.makeText(getContext(),"Campo vuoto", Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
                searchButtonPressed = false;
            }
        }
        else {
        }
    }

    @Override
    public void onCheckedChanged(ChipGroup group, int checkedId) {
        //NOTE: checkedId is -1 if no chip is checked
        previousSearchTerm = "-"; // if the filter has been changed, then reset previous search term

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

    private void initRecycleView() {
        // defining Recycler view
        recyclerView = view.findViewById(R.id.recyclerView_searchFragment);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // adding recycle listener for touch detection
//        recyclerView.addOnItemTouchListener(new RecyclerListener(getContext(), recyclerView, this));
        recyclerAdapterSearchPage = new RecyclerAdapterSearchPage(new ArrayList<>(), getContext(), this);
        recyclerView.setAdapter(recyclerAdapterSearchPage);
    }

    @Override
    public void onMovieSearchResultClicked(int position, View v) {
        SearchResult itemSelected = recyclerAdapterSearchPage.getSearchResult(position);
        firebaseAnalytics.logSelectedSearchedMovie((MovieSearchResult) itemSelected, "selected movie in search tab", this, getContext());
        Movie mv = new Movie();
        mv.setTmdbID(((MovieSearchResult) itemSelected).getTmdbID());
        NavGraphDirections.AnywhereToMovieDetailsFragment action = SearchFragmentDirections.anywhereToMovieDetailsFragment(mv);
        NavHostFragment.findNavController(SearchFragment.this).navigate(action);
    }

    @Override
    public void onUserSearchResultClicked(int position, View v) {
        SearchResult itemSelected = recyclerAdapterSearchPage.getSearchResult(position);
//        TextView textViewUsername = recyclerView.getLayoutManager().findViewByPosition(position).findViewById(R.id.textView_userListItem_username);
//        String username = textViewUsername.getText().toString();

        String username = ((UserSearchResult) recyclerAdapterSearchPage.getSearchResult(position)).getUsername();
        String firstName = ((UserSearchResult) recyclerAdapterSearchPage.getSearchResult(position)).getFirstName();
        String lastName = ((UserSearchResult) recyclerAdapterSearchPage.getSearchResult(position)).getLastName();
        String profilePictureUrl = ((UserSearchResult) recyclerAdapterSearchPage.getSearchResult(position)).getProfilePictureUrl();
        User user = new User();
        user.setUsername(username);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setProfilePicturePath(profilePictureUrl);

        NavGraphDirections.ActionGlobalUserProfileFragment action = NavGraphDirections.actionGlobalUserProfileFragment(user);
        NavHostFragment.findNavController(SearchFragment.this).navigate(action);
    }

}// end SearchFragment class