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
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
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
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.SerialDisposable;
import mirror42.dev.cinemates.NavGraphDirections;
import mirror42.dev.cinemates.R;
import mirror42.dev.cinemates.adapter.RecyclerAdapterSearchPage;
import mirror42.dev.cinemates.exception.CurrentTermEqualsPreviousTermException;
import mirror42.dev.cinemates.exception.EmptyValueException;
import mirror42.dev.cinemates.model.User;
import mirror42.dev.cinemates.model.search.MovieSearchResult;
import mirror42.dev.cinemates.model.search.SearchResult;
import mirror42.dev.cinemates.model.search.SearchResult.SearchType;
import mirror42.dev.cinemates.model.search.UserSearchResult;
import mirror42.dev.cinemates.model.tmdb.Movie;
import mirror42.dev.cinemates.ui.login.LoginViewModel;
import mirror42.dev.cinemates.utilities.FirebaseAnalytics;



public class SearchFragment extends Fragment implements
        View.OnClickListener,
        ChipGroup.OnCheckedChangeListener,
        RecyclerAdapterSearchPage.SearchResultListener {
    private final String TAG = this.getClass().getSimpleName();
    private SearchViewModel searchViewModel;
    private FloatingActionButton buttonSearch;
    private TextInputEditText editTextSearch;
    private TextInputLayout textInputLayout;
    private String currentSearchTerm;
    private RecyclerAdapterSearchPage recyclerAdapterSearchPage;
    private ChipGroup chipGroup;
    private FirebaseAnalytics firebaseAnalytics;
    private SearchResult.SearchType searchType;
    private LoginViewModel loginViewModel;
    private Chip chipUsersFilter;
    private User loggedUser;
    private TextView textViewTitle;
    private RecyclerView recyclerView;
    private ProgressBar spinner;
    private SerialDisposable searchSubscription;



    //------------------------------------------------------------------------ ANDROID METHODS

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_search, container, false);
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(TAG, "onViewCreated() called");
        editTextSearch = view.findViewById(R.id.editText_searchFragment);
        textInputLayout = view.findViewById(R.id.editTextLayout_searchFragment);
        buttonSearch = view.findViewById(R.id.button_searchFragment_search);
        chipGroup = view.findViewById(R.id.chipGroup_searchFragment);
        chipUsersFilter = view.findViewById(R.id.include_searchFragment_searchBox).findViewById(R.id.chip_searchFragment_user);
        textViewTitle = view.findViewById(R.id.textView_searchFragment_title);
        spinner = view.findViewById(R.id.progressBar_searchFragment);
        buttonSearch.setOnClickListener(this);
        chipGroup.setOnCheckedChangeListener(this);
        firebaseAnalytics = FirebaseAnalytics.getInstance();
        searchType = SearchResult.SearchType.UNIVERSAL;

        //
        initRecycleView(view);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        //
        loginViewModel = new ViewModelProvider(requireActivity()).get(LoginViewModel.class);
        loginViewModel.getObservableLoginResult().observe(getViewLifecycleOwner(), loginResult -> {
            switch (loginResult) {
                case SUCCESS:
                case REMEMBER_ME_EXISTS: {
                    chipUsersFilter.setVisibility(View.VISIBLE);
                    loggedUser = loginViewModel.getLoggedUser();
                    searchViewModel.setLoggedUser(loggedUser);
                }
                break;
                default:
                    textInputLayout.setHint("Cerca film");
                    searchType = SearchType.MOVIE; // reset filters
                    chipUsersFilter.setVisibility(View.GONE);
            }
        });

        //
        searchViewModel = new ViewModelProvider(this).get(SearchViewModel.class);
        searchViewModel.getObservableDownloadStatus().observe(getViewLifecycleOwner(), downloadStatus -> {
            textViewTitle.setVisibility(View.VISIBLE);
            showLoadingSpinner(false);

            switch (downloadStatus) {
                case SUCCESS: {
                    ArrayList<SearchResult> searchResults = searchViewModel.getSearchResultList();
                    if (searchResults != null && searchResults.size()>0) {
                        textViewTitle.setText("Risultati per: " + currentSearchTerm);
                        recyclerAdapterSearchPage.loadNewData(searchResults);
                    }
                }
                    break;
                case NO_RESULT:
                    recyclerAdapterSearchPage.loadNewData(null);
                    textViewTitle.setText("Nessun risultato per: " + currentSearchTerm);
                    showCenteredToast("Nessun risultato per: " + currentSearchTerm);
                    break;
                case FAILED:

                    break;
            }
        });




        //
        enableSearchOnTyping(true);

    }// end onActivityCreated()

    @Override
    public void onClick(View v) {
        if(theButtonPressed_is(buttonSearch, v)) {
            animateButton(buttonSearch);
            showLoadingSpinner(true);
            enableSearchOnTyping(false);
            textInputLayout.setError(null);
            hideKeyboard();
            currentSearchTerm = editTextSearch.getText().toString();
            logSearchTerm(currentSearchTerm);

            //
            try {
                searchViewModel.search(currentSearchTerm, searchType);
            } catch (EmptyValueException e) {
                e.printStackTrace();
                Log.e(TAG, "onClick: ", e);
                textInputLayout.setError("Campo vuoto");
                showCenteredToast("Campo vuoto");
                showLoadingSpinner(false);
            } catch (CurrentTermEqualsPreviousTermException e) {
                Log.e(TAG, "onClick: ", e);
                showLoadingSpinner(false);
            }
        }
    }

    private void logSearchTerm(String searchTerm) {
        // don't log user searches
        if(chipGroup.getCheckedChipId() != R.id.chip_searchFragment_user)
            firebaseAnalytics.logSearchTerm(searchTerm, this, getContext());
    }

    private boolean theButtonPressed_is(FloatingActionButton buttonTarget, View actualButtonPressed) {
        return actualButtonPressed.getId() == buttonTarget.getId();
    }

    @Override
    public void onCheckedChanged(ChipGroup group, int checkedId) {
        //NOTE: checkedId is -1 if no chip is checked
        searchViewModel.resetPreviousSearchTerm(); // if the filter has been changed, then reset previous search term

        if(checkedId == R.id.chip_searchFragment_movie) {
            searchType = SearchType.MOVIE;
            textInputLayout.setHint("Cerca film");
        }
        else if(checkedId == R.id.chip_searchFragment_cast) {
            searchType = SearchType.CAST;
            textInputLayout.setHint("Cerca attore");
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





    //------------------------------------------------------------------------ MY METHODS

    private void initRecycleView(View view) {
        // defining Recycler view
        recyclerView = view.findViewById(R.id.recyclerView_searchFragment);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // adding recycle listener for touch detection
//        recyclerView.addOnItemTouchListener(new RecyclerListener(getContext(), recyclerView, this));
        recyclerAdapterSearchPage = new RecyclerAdapterSearchPage(new ArrayList<>(), getContext(), this);
        recyclerView.setAdapter(recyclerAdapterSearchPage);
    }

    private void enableSearchOnTyping(boolean enable) {
        // AUTOMATIC SEARCH on typing
        // triggered when:
        // at least 3 chars are typed in, and search button hasn't been pressed yet (filter)
        // 1 second later after the last typed character (debounce)

        if( ! enable) {
            if(searchSubscription!=null && !searchSubscription.isDisposed())
                searchSubscription.dispose();
            // ri-enable
        }

        searchSubscription = new SerialDisposable();
        Observable<String> searchTermObservable = RxTextView.textChanges(editTextSearch)
                .filter(text -> text.length()>=3)
                .debounce(1, TimeUnit.SECONDS) /*NOTES: 1 seconds seems to be the sweetspot*/
                .map(text -> text.toString())
                .observeOn(AndroidSchedulers.mainThread());

        searchSubscription.set(searchTermObservable
                .subscribe(this::autoSearch));
    }

    private void autoSearch(String searchQuery) {
        currentSearchTerm = searchQuery;

        try {
            showLoadingSpinner(true);
            searchViewModel.search(currentSearchTerm, searchType);
        } catch (EmptyValueException e) {
            Log.e(TAG, "autoSearch: ", e);
            //do nothing
        } catch (CurrentTermEqualsPreviousTermException e) {
            Log.e(TAG, "autoSearch: ", e);
            showLoadingSpinner(false);
        }
    }

    @Override
    public void onMovieSearchResultClicked(int position, View v) {
        MovieSearchResult itemSelected = (MovieSearchResult)  recyclerAdapterSearchPage.getItem(position);
        firebaseAnalytics.logSelectedSearchedMovie(itemSelected, "selected movie in search tab", this, getContext());
        Movie mv = new Movie();
        mv.setTmdbID(itemSelected.getTmdbID());
        NavGraphDirections.AnywhereToMovieDetailsFragment action = SearchFragmentDirections.anywhereToMovieDetailsFragment(mv);
        NavHostFragment.findNavController(SearchFragment.this).navigate(action);
    }

    @Override
    public void onCastSearchResultClicked(int position, View v) {

    }

    @Override
    public void onUserSearchResultClicked(int position, View v) {
        UserSearchResult itemSelected = (UserSearchResult) recyclerAdapterSearchPage.getItem(position);

        NavGraphDirections.ActionGlobalUserProfileFragment userProfileFragment =
                NavGraphDirections.actionGlobalUserProfileFragment(itemSelected.getUsername());
        NavHostFragment.findNavController(SearchFragment.this).navigate(userProfileFragment);
    }

    private void showCenteredToast(String msg) {
        final Toast toast = Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    private void hideKeyboard() {
        editTextSearch.onEditorAction(EditorInfo.IME_ACTION_DONE);
    }

    private void showLoadingSpinner(boolean show) {
        if(show) spinner.setVisibility(View.VISIBLE);
        else spinner.setVisibility(View.GONE);
    }

    private void animateButton(FloatingActionButton button) {
        Animation buttonAnim = AnimationUtils.loadAnimation(getContext(), R.anim.push_button_animation);
        button.startAnimation(buttonAnim);
    }

    //--- on testing

//    private void drawCastSearchResult(ArrayList<SearchResult> searchResults) {
//        recyclerAdapterSearchPage.loadNewData(searchResults);
//    }


//    private void search(String query, SearchType searchType) {
//        switch (searchType) {
//            case MOVIE:
//                break;
//            case CAST:
//                searchCast(currentSearchTerm);
//                break;
//            case USER:
//                break;
//            default:
//        }
//    }
//
//    private void searchCast(String searchTerm) {
////        Observable<ArrayList<SearchResult>> notificationsObservable =
////                searchViewModel.getCastSearchResultObservable(searchTerm, 1)
////                .subscribeOn(Schedulers.io())
////                .observeOn(AndroidSchedulers.mainThread());
////
////        searchSubscription.set(notificationsObservable
////                .subscribe( this::drawCastSearchResult,
////                            this::handleErrors));
//    }
//
//    private void handleErrors(Throwable e) {
//        Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
//    }
//    private void disposeSubscribers() {
//        if (disposable_2 != null && !disposable_2.isDisposed()) {
//            disposable_2.dispose();
//        }
//    }
//    private void updateResults(ArrayList<SearchResult> searchResults) {
//        recyclerAdapterSearchPage.loadNewData(searchResults);
//    }
//    private Observable<ArrayList<SearchResult>> createMoviesListObservable(String givenQuery) {
//        return Observable.create( emitter -> {
//            final int PAGE_1 = 1;
//            String movieTitle = givenQuery;
//            TheMovieDatabaseApi tmdb = TheMovieDatabaseApi.getInstance();
//            ArrayList<SearchResult> result = null;
//
//            movieTitle = movieTitle.trim();
//            result = new ArrayList<>();
//
//
//            try {
//                // querying TBDb
//                JSONObject jsonObj = tmdb.getJsonMoviesListByTitle(movieTitle, PAGE_1);
//                JSONArray resultsArray = jsonObj.getJSONArray("results");
//
//                // fetching results
//                for (int i = 0; i < resultsArray.length(); i++) {
//                    JSONObject x = resultsArray.getJSONObject(i);
//                    int id = x.getInt("id");
//                    String title = x.getString("title");
//
//                    // if overview is null
//                    // getString() will fail
//                    // (due to the unvailable defaultLanguage version)
//                    // that's why the try-catch
//                    String overview = null;
//                    try {
//                        overview = x.getString("overview");
//                        if ((overview == null) || (overview.isEmpty()))
//                            overview = "(trama non disponibile in italiano)";
//                    } catch (Exception e) {
//                        e.getMessage();
//                        e.printStackTrace();
//                        overview = "(trama in italiano non disp.)";
//                    }
//
//                    // if poster_path is null
//                    // getString() will fail
//                    // that's why the try-catch
//                    String posterURL = null;
//                    try {
//                        posterURL = x.getString("poster_path");
//                        posterURL = tmdb.buildPosterUrl(posterURL);
//                    } catch (Exception e) {
//                        e.getMessage();
//                        e.printStackTrace();
//                    }
//
//                    //
//                    MovieSearchResult mv = new MovieSearchResult(id, title, overview, posterURL);
//                    result.add(mv);
//                }// for
//                // once finished set results
//                emitter.onNext(result);
//                emitter.onComplete();
//
//            } catch (Exception e) {
//                // if the search returns nothing
//                // moviesList will be null
//                emitter.onError(e);
//            }
//        });
//    }

}// end SearchFragment class