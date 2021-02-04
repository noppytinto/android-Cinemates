package mirror42.dev.cinemates.ui.list.watchlist;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import java.util.ArrayList;
import java.util.Collections;

import mirror42.dev.cinemates.R;
import mirror42.dev.cinemates.model.User;
import mirror42.dev.cinemates.model.tmdb.Movie;
import mirror42.dev.cinemates.ui.login.LoginViewModel;
import mirror42.dev.cinemates.ui.userprofile.PersonalProfileFragmentDirections;
import mirror42.dev.cinemates.utilities.ImageUtilities;


public class WatchlistThumbnailsFragment extends Fragment implements View.OnClickListener {
    private final String TAG = getClass().getSimpleName();
    private WatchlistThumbnailsViewModel watchlistThumbnailsViewModel;
    private LoginViewModel loginViewModel;
    private View view;
    private ArrayList<Movie> movies;
    private CardView cardView;
    private ProgressBar spinner;



    //--------------------------------------------------------------------------------------- ANDROID METHODS

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_watchlist_thumbnails, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.view = view;
        cardView = view.findViewById(R.id.cardView_watchlistThumbnailsFragment);
        cardView.setOnClickListener(this);
        spinner = view.findViewById(R.id.progressBar_watchlist);

        //2
        watchlistThumbnailsViewModel = new ViewModelProvider(this).get(WatchlistThumbnailsViewModel.class);
        watchlistThumbnailsViewModel.getMoviesList().observe(getViewLifecycleOwner(), moviesList -> {
            if(moviesList!=null) {
                // reverse list
                Collections.reverse(moviesList);
                movies = moviesList;

                // set thumbnails
                setThumbnails(moviesList);


            }
            else {
                Toast toast = Toast.makeText(getContext(), "errore caricamento Watchlist", Toast.LENGTH_SHORT);
                toast.show();
            }
        });

        //
        loginViewModel = new ViewModelProvider(requireActivity()).get(LoginViewModel.class);
        loginViewModel.getLoginResult().observe(getViewLifecycleOwner(), loginResult -> {
            switch (loginResult) {
                case SUCCESS: case REMEMBER_ME_EXISTS: {
                    User user = loginViewModel.getLiveLoggedUser().getValue();
                    if(user!=null) {
                        String email = user.getEmail();
                        watchlistThumbnailsViewModel.fetchData(email, user.getAccessToken());
                    }
                }
                break;
            }
        });

    }// end onViewCreated()





    //--------------------------------------------------------------------------------------- METHODS

    private void setThumbnails(ArrayList<Movie> moviesList) {
        ImageView thumbnail_1 = view.findViewById(R.id.imageView_watchlist_1);
        ImageView thumbnail_2 = view.findViewById(R.id.imageView_watchlist_2);
        ImageView thumbnail_3 = view.findViewById(R.id.imageView_watchlist_3);
        ImageView thumbnail_4 = view.findViewById(R.id.imageView_watchlist_4);
//        ImageView thumbnail_5 = view.findViewById(R.id.imageView_listStub_5);
//        ImageView thumbnail_6 = view.findViewById(R.id.imageView_listStub_6);
//        ImageView thumbnail_7 = view.findViewById(R.id.imageView_listStub_7);
//        ImageView thumbnail_8 = view.findViewById(R.id.imageView_listStub_8);

        ArrayList<ImageView> thumbnailsList = new ArrayList<>();
        thumbnailsList.add(thumbnail_1);
        thumbnailsList.add(thumbnail_2);
        thumbnailsList.add(thumbnail_3);
        thumbnailsList.add(thumbnail_4);
//        thumbnailsList.add(thumbnail_5);
//        thumbnailsList.add(thumbnail_6);
//        thumbnailsList.add(thumbnail_7);
//        thumbnailsList.add(thumbnail_8);

        for(int i=0; i<4 && i<moviesList.size(); i++) {
            ImageView t = thumbnailsList.get(i);
            String posterUrl = moviesList.get(i).getPosterURL();
            ImageUtilities.loadRectangularImageInto(posterUrl, t, getContext());
        }
        spinner.setVisibility(View.GONE);
    }// end setThumbnails()



    @Override
    public void onClick(View v) {
        int id = v.getId();

        if(id == R.id.cardView_watchlistThumbnailsFragment) {
            if(movies!=null) {
                Movie[] m = movies.toArray(new Movie[0]);

                PersonalProfileFragmentDirections.ActionPersonalProfileFragmentToListFragment action =
                        PersonalProfileFragmentDirections.actionPersonalProfileFragmentToListFragment(m);
                action.setListTitle("Watchlist");
                action.setListDescription("");
                NavHostFragment.findNavController(WatchlistThumbnailsFragment.this).navigate(action);
            }
            else {
                Toast toast = Toast.makeText(getContext(), "caricamento lista in corso...", Toast.LENGTH_SHORT);
                toast.show();
            }
        }
    }
}// end WatchlistFragment class