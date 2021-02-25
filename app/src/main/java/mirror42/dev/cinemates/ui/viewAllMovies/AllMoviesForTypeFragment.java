package mirror42.dev.cinemates.ui.viewAllMovies;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import mirror42.dev.cinemates.R;
import mirror42.dev.cinemates.model.tmdb.Movie;
import mirror42.dev.cinemates.ui.explore.ExploreFragment;
import mirror42.dev.cinemates.utilities.FirebaseAnalytics;

public class AllMoviesForTypeFragment extends Fragment {

    private final String TAG = getClass().getSimpleName();

    private FirebaseAnalytics firebaseAnalytics;
    private ExploreFragment.MovieCategory movieCategoryToLoad;

    private AllMoviesForTypeViewModel allMovieViewModel;

    int currentPage = 1;
    Integer maxPage;

    private ArrayList<Movie> movies = new ArrayList<Movie>();
    private View view;
    private TextView title;



    public AllMoviesForTypeFragment() {
        // Required empty public constructor
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_all_movies_for_type, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        this.view = view;

        // firebase logging
        firebaseAnalytics = FirebaseAnalytics.getInstance();
        firebaseAnalytics.logScreenEvent(this, getString(R.string.viewAllMovie_page_firebase_viewAllMovie), getContext());

        title = view.findViewById(R.id.textView_title_allMovieForTypeFragment);

        initCategoryMovie();
        setTitleSearch();

        allMovieViewModel = new ViewModelProvider(this).get(AllMoviesForTypeViewModel.class);
        allMovieViewModel.getObservableDownloadMaxPageStatus().observe(getViewLifecycleOwner(), downloadMaxPageStatus -> {

            switch (downloadMaxPageStatus) {
                case SUCCESS:
                    maxPage = allMovieViewModel.getMaxPage();
                    setMovies();
                    showCenteredToast("ecco le pagine caricate" + maxPage );
                break;
                case FAILED:
                    Log.v(TAG, "massimo numero pagine per categoria di film non estratto impossibile continuare ad operare sulla pagina");
                    break;
            }
        });

        allMovieViewModel.getObservableDownloadSearchResultStatus().observe(getViewLifecycleOwner(), downloadSearchResultStatus -> {

            switch (downloadSearchResultStatus) {
                case SUCCESS:
                    showCenteredToast("bravissimo film cricati");
                    movies = allMovieViewModel.getSearchResult();
                    break;
                case FAILED:
                    Log.v(TAG, "Errore caricamernto film");
                    break;
            }
        });

        allMovieViewModel.findMaxPage(movieCategoryToLoad);
    }


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


    public void showCenteredToast(String message) {
        final Toast toast = Toast.makeText(getContext(), message, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

}