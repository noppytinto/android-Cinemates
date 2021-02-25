package mirror42.dev.cinemates.ui.viewAllMovies;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import mirror42.dev.cinemates.R;
import mirror42.dev.cinemates.ui.explore.ExploreFragment;
import mirror42.dev.cinemates.utilities.FirebaseAnalytics;

public class AllMoviesForTypeFragment extends Fragment {

    private final String TAG = getClass().getSimpleName();
    private FirebaseAnalytics firebaseAnalytics;

    private ExploreFragment.MovieCategory movieCategoryToLoad;

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

        // firebase logging
        firebaseAnalytics = FirebaseAnalytics.getInstance();
        firebaseAnalytics.logScreenEvent(this, getString(R.string.viewAllMovie_page_firebase_viewAllMovie), getContext());

        initCategoryMovie();

        switch(movieCategoryToLoad){
            case POPULAR:
                showCenteredToast("carico popolari");
                break;
            case UPCOMINGS:
                showCenteredToast("carico in usicta");
                break;
            case LATEST:
                showCenteredToast("carico ultimi");
                break;
        }
    }



    private void initCategoryMovie(){
        try {
            movieCategoryToLoad  = AllMoviesForTypeFragmentArgs.fromBundle(getArguments()).getMovieCategory();
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }


    public void showCenteredToast(String message) {
        final Toast toast = Toast.makeText(getContext(), message, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

}