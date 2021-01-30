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

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.disposables.SerialDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import mirror42.dev.cinemates.NavGraphDirections;
import mirror42.dev.cinemates.R;
import mirror42.dev.cinemates.model.User;
import mirror42.dev.cinemates.tmdbAPI.TheMovieDatabaseApi;
import mirror42.dev.cinemates.tmdbAPI.model.Cast;
import mirror42.dev.cinemates.tmdbAPI.model.Movie;
import mirror42.dev.cinemates.ui.login.LoginViewModel;
import mirror42.dev.cinemates.ui.search.model.MovieSearchResult;
import mirror42.dev.cinemates.ui.search.model.SearchResult;
import mirror42.dev.cinemates.ui.search.model.SearchResult.SearchType;
import mirror42.dev.cinemates.ui.search.model.UserSearchResult;
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
    private ProgressBar spinner;
    private SerialDisposable searchSubscription;
    private Disposable disposable_2;
    private ArrayList<Cast> actorsList;



    //------------------------------------------------------------------------ ANDROID METHODS

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
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
        spinner = view.findViewById(R.id.progressBar_searchFragment);
        buttonSearch.setOnClickListener(this);
        chipGroup.setOnCheckedChangeListener(this);
        firebaseAnalytics = FirebaseAnalytics.getInstance();
        searchType = SearchResult.SearchType.UNIVERSAL;

        //
        initRecycleView();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        searchSubscription = new SerialDisposable();

        //
        searchViewModel = new ViewModelProvider(this).get(SearchViewModel.class);
        searchViewModel.getSearchResultList().observe(getViewLifecycleOwner(), searchResults -> {
            textViewTitle.setVisibility(View.VISIBLE);
            spinner.setVisibility(View.GONE);
            searchButtonPressed = false;

            if (searchResults != null && searchResults.size()>0) {
                textViewTitle.setText("Risultati per: " + currentSearchTerm);
                recyclerAdapterSearchPage.loadNewData(searchResults);
            }
            else {
                recyclerAdapterSearchPage.loadNewData(null);
                textViewTitle.setText("Nessun risultato per: " + currentSearchTerm);
                final Toast toast = Toast.makeText(getContext(), "Nessun risultato per: " + currentSearchTerm, Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
            }
        });

        //
        loginViewModel = new ViewModelProvider(requireActivity()).get(LoginViewModel.class);
        loginViewModel.getLoginResult().observe(getViewLifecycleOwner(), loginResult -> {
            switch (loginResult) {
                case SUCCESS:
                case REMEMBER_ME_EXISTS: {
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

        //
        enableSearchOnTyping();

    }// end onActivityCreated()

    @Override
    public void onClick(View v) {
        if(v.getId() == buttonSearch.getId()) {
            spinner.setVisibility(View.VISIBLE);
            searchButtonPressed = true;
            Animation buttonAnim = AnimationUtils.loadAnimation(getContext(), R.anim.push_button_animation);
            buttonSearch.startAnimation(buttonAnim);
            currentSearchTerm = editTextSearch.getText().toString();

            if( ! currentSearchTerm.isEmpty()) {
                if( ! currentSearchTerm.equals(previousSearchTerm)) {
                    previousSearchTerm = currentSearchTerm;

                    // don't log user searches
                    if(chipGroup.getCheckedChipId() != R.id.chip_searchFragment_user)
                        firebaseAnalytics.logSearchTerm(currentSearchTerm, this, getContext());

                    //
                    textInputLayout.setError(null);
                    editTextSearch.onEditorAction(EditorInfo.IME_ACTION_DONE); // hide keyboard on search button press

                    //
                    searchViewModel.fetchResults(currentSearchTerm, searchType, loggedUser);

                }
                else searchButtonPressed = false;
            }
            else {
                textInputLayout.setError("Campo vuoto");
                final Toast toast = Toast.makeText(getContext(),"Campo vuoto", Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
                searchButtonPressed = false;
            }
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
            searchType = SearchType.CAST;
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

    @Override
    public void onDetach() {
        super.onDetach();
        disposeSubscribers();
    }



    //------------------------------------------------------------------------ MY METHODS

    private void initRecycleView() {
        // defining Recycler view
        recyclerView = view.findViewById(R.id.recyclerView_searchFragment);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // adding recycle listener for touch detection
//        recyclerView.addOnItemTouchListener(new RecyclerListener(getContext(), recyclerView, this));
        recyclerAdapterSearchPage = new RecyclerAdapterSearchPage(new ArrayList<>(), getContext(), this);
        recyclerView.setAdapter(recyclerAdapterSearchPage);
    }

    private void search(String query, SearchType searchType) {
        switch (searchType) {
            case MOVIE:
                break;
            case CAST:
                searchCast(currentSearchTerm);
                break;
            case USER:
                break;
            default:
        }
    }

    private void searchCast(String searchTerm) {
        Observable<ArrayList<SearchResult>> notificationsObservable =
                searchViewModel.getCastSearchResultObservable(searchTerm, 1)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());

        searchSubscription.set(notificationsObservable
                .subscribe( this::drawCastSearchResult,
                            this::handleErrors));
    }

    private void handleErrors(Throwable e) {
        Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
    }


    private void enableSearchOnTyping() {
        // AUTOMATIC SEARCH on typing
        // triggered when:
        // at least 3 chars are typed in, and search button hasn't been pressed yet (filter)
        // 1 second later after the last typed character (debounce)
        Observable<CharSequence> searchTermObservable = RxTextView.textChanges(editTextSearch)
                                                    .filter(text -> text.length()>=3 && !searchButtonPressed)
                                                    .debounce(1, TimeUnit.SECONDS) /*NOTES: 1 seconds seems to be the sweetspot*/
                                                    .observeOn(AndroidSchedulers.mainThread());

        searchSubscription.set(searchTermObservable.subscribe(this::search));
    }

    private void search(CharSequence searchQuery) {
        currentSearchTerm = searchQuery.toString();

        if( ! currentSearchTerm.equals(previousSearchTerm)) {
            spinner.setVisibility(View.VISIBLE);
            previousSearchTerm = currentSearchTerm;
            searchViewModel.fetchResults(currentSearchTerm, searchType, loggedUser);
        }
    }

    private void drawCastSearchResult(ArrayList<SearchResult> searchResults) {
        recyclerAdapterSearchPage.loadNewData(searchResults);
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
    public void onCastSearchResultClicked(int position, View v) {

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





    //--- on testing
    private void disposeSubscribers() {
        if (disposable_2 != null && !disposable_2.isDisposed()) {
            disposable_2.dispose();
        }
    }
    private void updateResults(ArrayList<SearchResult> searchResults) {
        recyclerAdapterSearchPage.loadNewData(searchResults);
    }
    private Observable<ArrayList<SearchResult>> createMoviesListObservable(String givenQuery) {
        return Observable.create( emitter -> {
            final int PAGE_1 = 1;
            String movieTitle = givenQuery;
            TheMovieDatabaseApi tmdb = TheMovieDatabaseApi.getInstance();
            ArrayList<SearchResult> result = null;

            movieTitle = movieTitle.trim();
            result = new ArrayList<>();


            try {
                // querying TBDb
                JSONObject jsonObj = tmdb.getJsonMoviesListByTitle(movieTitle, PAGE_1);
                JSONArray resultsArray = jsonObj.getJSONArray("results");

                // fetching results
                for (int i = 0; i < resultsArray.length(); i++) {
                    JSONObject x = resultsArray.getJSONObject(i);
                    int id = x.getInt("id");
                    String title = x.getString("title");

                    // if overview is null
                    // getString() will fail
                    // (due to the unvailable defaultLanguage version)
                    // that's why the try-catch
                    String overview = null;
                    try {
                        overview = x.getString("overview");
                        if ((overview == null) || (overview.isEmpty()))
                            overview = "(trama non disponibile in italiano)";
                    } catch (Exception e) {
                        e.getMessage();
                        e.printStackTrace();
                        overview = "(trama in italiano non disp.)";
                    }

                    // if poster_path is null
                    // getString() will fail
                    // that's why the try-catch
                    String posterURL = null;
                    try {
                        posterURL = x.getString("poster_path");
                        posterURL = tmdb.buildPosterUrl(posterURL);
                    } catch (Exception e) {
                        e.getMessage();
                        e.printStackTrace();
                    }

                    //
                    MovieSearchResult mv = new MovieSearchResult(id, title, overview, posterURL);
                    result.add(mv);
                }// for
                // once finished set results
                emitter.onNext(result);
                emitter.onComplete();

            } catch (Exception e) {
                // if the search returns nothing
                // moviesList will be null
                emitter.onError(e);
            }
        });
    }

}// end SearchFragment class