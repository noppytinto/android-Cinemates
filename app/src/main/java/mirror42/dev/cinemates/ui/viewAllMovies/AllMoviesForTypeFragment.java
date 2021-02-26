package mirror42.dev.cinemates.ui.viewAllMovies;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import mirror42.dev.cinemates.MainActivity;
import mirror42.dev.cinemates.NavGraphDirections;
import mirror42.dev.cinemates.R;
import mirror42.dev.cinemates.adapter.RecyclerAdapterViewAllMovieForType;
import mirror42.dev.cinemates.adapter.RecyclerViewClickListener;
import mirror42.dev.cinemates.model.User;
import mirror42.dev.cinemates.model.search.MovieSearchResult;
import mirror42.dev.cinemates.model.tmdb.Movie;
import mirror42.dev.cinemates.ui.explore.ExploreFragment;
import mirror42.dev.cinemates.ui.home.HomeFragmentDirections;
import mirror42.dev.cinemates.ui.login.LoginViewModel;
import mirror42.dev.cinemates.ui.notification.NotificationsViewModel;
import mirror42.dev.cinemates.ui.search.SearchFragment;
import mirror42.dev.cinemates.ui.search.SearchFragmentDirections;
import mirror42.dev.cinemates.utilities.FirebaseAnalytics;

public class AllMoviesForTypeFragment extends Fragment  implements View.OnClickListener {

    private final String TAG = getClass().getSimpleName();

    private FirebaseAnalytics firebaseAnalytics;
    private ExploreFragment.MovieCategory movieCategoryToLoad;

    private AllMoviesForTypeViewModel allMovieViewModel;

    private RecyclerAdapterViewAllMovieForType recycleAdapter;
    private RecyclerView recyclerView;

    int currentPage = 1;
    Integer maxPage;

    private ArrayList<Movie> movies = new ArrayList<Movie>();

    private View view;
    private TextView title;
    private TextView pageLoadedInfo;
    private Button loadMoreButton;
    private ProgressBar spinner;



    public AllMoviesForTypeFragment() {
        // Required empty public constructor
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_all_movies_for_type, container, false);
    }

    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu) {
        super.onPrepareOptionsMenu(menu);
        MenuItem userIcon = menu.getItem(1);
        MenuItem notifyIcon = menu.getItem(0);
        userIcon.setVisible(false);
        notifyIcon.setVisible(false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        this.view = view;

        // firebase logging
        firebaseAnalytics = FirebaseAnalytics.getInstance();
        firebaseAnalytics.logScreenEvent(this, getString(R.string.viewAllMovie_page_firebase_viewAllMovie), getContext());

        title = view.findViewById(R.id.textView_title_allMovieForTypeFragment);
        spinner = view.findViewById(R.id.progressBar_AllMoviesForTypeFragment);
        pageLoadedInfo = view.findViewById(R.id.textView_pageLoaded_allMovieForTypeFragment);
        loadMoreButton= view.findViewById(R.id.button_AllMoviesForTypeFragment_viewAll);
        loadMoreButton.setOnClickListener(this);

        initCategoryMovie();
        setTitleSearch();
        initRecycleView();

        showLoadingSpinner(true);

        allMovieViewModel = new ViewModelProvider(this).get(AllMoviesForTypeViewModel.class);
        allMovieViewModel.getObservableDownloadMaxPageStatus().observe(getViewLifecycleOwner(), downloadMaxPageStatus -> {

            switch (downloadMaxPageStatus) {
                case SUCCESS:
                    maxPage = allMovieViewModel.getMaxPage();
                    setMovies();
                break;
                case FAILED:
                    Log.v(TAG, "massimo numero pagine per categoria di film non estratto impossibile continuare ad operare sulla pagina");
                    break;
            }
        });

        allMovieViewModel.getObservableDownloadSearchResultStatus().observe(getViewLifecycleOwner(), downloadSearchResultStatus -> {

            switch (downloadSearchResultStatus) {
                case SUCCESS:
                    ArrayList<Movie> currentMovie = allMovieViewModel.getSearchResult();
                    movies.addAll(currentMovie);
                    showLoadingSpinner(false);
                    recycleAdapter.loadNewData(movies);
                    pageLoadedInfo.setText("Pagine:" + currentPage + "/" + maxPage);
                    break;
                case FAILED:
                    showLoadingSpinner(false);
                    showCenteredToast("Caricamento film fallito");
                    break;
            }
        });

        allMovieViewModel.findMaxPage(movieCategoryToLoad);
    }

    @Override
    public void onClick(View v) {
        showLoadingSpinner(true);
        if(v.getId() == loadMoreButton.getId()){
            currentPage++;
            setMovies();
        }
    }


    //--------------------------------------------------My methods

    private void initCategoryMovie(){
        try {
            movieCategoryToLoad  = AllMoviesForTypeFragmentArgs.fromBundle(getArguments()).getMovieCategory();
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }

    private void setTitleSearch(){
        switch(movieCategoryToLoad){
            case POPULAR:
                title.setText("Film popolari:");
                break;
            case UPCOMINGS:
                title.setText("Film in prossima uscita:");
                break;
            case LATEST:
                title.setText("Ultimi film usciti:");
                break;
        }
    }

    private void setMovies(){
        if(maxPage >= currentPage)
             allMovieViewModel.searchMovie(currentPage, movieCategoryToLoad);
        else
            showCenteredToast("Ci dispiace non ci sono più film da caricare per la categoria indicata");
    }

    private void  initRecycleView(){

        // recycle
        recyclerView = (RecyclerView)  view.findViewById(R.id.recyclerView_AllMoviesForTypeFragment);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        RecyclerViewClickListener listener = new RecyclerViewClickListener() {
            @Override
            public void onClick(View v, int position) {
                Movie itemSelected = (Movie)  recycleAdapter.getMovie(position);
                firebaseAnalytics.logSelectedViewAllMovie(itemSelected, "selected movie in search tab", this, getContext());
                Movie mv = new Movie();
                mv.setTmdbID(itemSelected.getTmdbID());
                NavGraphDirections.AnywhereToMovieDetailsFragment action = SearchFragmentDirections.anywhereToMovieDetailsFragment(mv);
                NavHostFragment.findNavController(AllMoviesForTypeFragment.this).navigate(action);

            }
        };

        recycleAdapter = new RecyclerAdapterViewAllMovieForType(new ArrayList<Movie>() ,view.getContext() ,listener  );
        recyclerView.setAdapter(recycleAdapter);

    }

    public void showCenteredToast(String message) {
        final Toast toast = Toast.makeText(getContext(), message, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    private void showLoadingSpinner(boolean show) {
        if(show) spinner.setVisibility(View.VISIBLE);
        else spinner.setVisibility(View.GONE);
    }



}