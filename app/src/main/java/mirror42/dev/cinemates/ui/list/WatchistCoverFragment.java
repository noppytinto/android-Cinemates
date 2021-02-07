package mirror42.dev.cinemates.ui.list;

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

import java.util.ArrayList;
import java.util.Collections;

import mirror42.dev.cinemates.R;
import mirror42.dev.cinemates.model.User;
import mirror42.dev.cinemates.model.tmdb.Movie;
import mirror42.dev.cinemates.ui.login.LoginViewModel;
import mirror42.dev.cinemates.utilities.ImageUtilities;


public class WatchistCoverFragment extends Fragment implements View.OnClickListener {
    private final String TAG = getClass().getSimpleName();
    private ListCoverViewModel listCoverViewModel;
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
        return inflater.inflate(R.layout.layout_list_cover, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.view = view;
        cardView = view.findViewById(R.id.cardView_listCover);
        cardView.setOnClickListener(this);
        spinner = view.findViewById(R.id.progressBar_listCover);

        //2
        listCoverViewModel = new ViewModelProvider(this).get(ListCoverViewModel.class);
        listCoverViewModel.getObservableMoviesList().observe(getViewLifecycleOwner(), moviesList -> {
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
                        listCoverViewModel.fetchWatchlist(email, user.getAccessToken());
                    }
                }
                break;
            }
        });

    }// end onViewCreated()





    //--------------------------------------------------------------------------------------- METHODS

    private void setThumbnails(ArrayList<Movie> moviesList) {
        ImageView thumbnail_1 = view.findViewById(R.id.imageView_listCover_1);
        ImageView thumbnail_2 = view.findViewById(R.id.imageView_listCover_2);
        ImageView thumbnail_3 = view.findViewById(R.id.imageView_listCover_3);
        ImageView thumbnail_4 = view.findViewById(R.id.imageView_listCover_4);

        ArrayList<ImageView> thumbnailsList = new ArrayList<>();
        thumbnailsList.add(thumbnail_1);
        thumbnailsList.add(thumbnail_2);
        thumbnailsList.add(thumbnail_3);
        thumbnailsList.add(thumbnail_4);

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

        if(id == R.id.cardView_listCover) {
            if(movies!=null) {
                Movie[] m = movies.toArray(new Movie[0]);
n
//                PersonalProfileFragmentDirections.ActionPersonalProfileFragmentToListFragment listFragment =
//                        PersonalProfileFragmentDirections.actionPersonalProfileFragmentToListFragment(ml, 0, )
//                PersonalProfileFragmentDirections.ActionPersonalProfileFragmentToListFragment action =
//                        PersonalProfileFragmentDirections.actionPersonalProfileFragmentToListFragment(m);
//                action.setListTitle("Watchlist");
//                action.setListDescription("");
//                NavHostFragment.findNavController(BaseListThumbnailsFragment.this).navigate(action);
            }
            else {
                Toast toast = Toast.makeText(getContext(), "caricamento lista in corso...", Toast.LENGTH_SHORT);
                toast.show();
            }
        }
    }
}// end WatchlistFragment class