package mirror42.dev.cinemates.ui.list;

import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.progressindicator.LinearProgressIndicator;

import java.util.ArrayList;

import mirror42.dev.cinemates.NavGraphDirections;
import mirror42.dev.cinemates.R;
import mirror42.dev.cinemates.model.User;
import mirror42.dev.cinemates.model.list.MoviesList;
import mirror42.dev.cinemates.model.tmdb.Movie;
import mirror42.dev.cinemates.ui.login.LoginViewModel;
import mirror42.dev.cinemates.utilities.ImageUtilities;


public class WatchistCoverFragment extends Fragment implements View.OnClickListener {
    private final String TAG = getClass().getSimpleName();
    private ListCoverViewModel listCoverViewModel;
    private LoginViewModel loginViewModel;
    private View view;
    private ArrayList<Movie> movies;
    private MoviesList list;
    private CardView cardView;
    private LinearProgressIndicator progressIndicator;



    //--------------------------------------------------------------------------------------- ANDROID METHODS

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.layout_essential_list_cover, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.view = view;
        cardView = view.findViewById(R.id.cardView_essentialListCover);
        cardView.setOnClickListener(this);
        progressIndicator = view.findViewById(R.id.progressIndicator_essentialListCover);

        // setup list appearance
        TextView listTitle = view.findViewById(R.id.textView_essentialListCover_title);
        listTitle.setText("Watchlist");
        listTitle.setTextColor(getResources().getColor(R.color.netflix_red));

        //2
        listCoverViewModel = new ViewModelProvider(this).get(ListCoverViewModel.class);
        listCoverViewModel.getObservableWatchlist().observe(getViewLifecycleOwner(), watchlist -> {
            progressIndicator.setVisibility(View.GONE);
            if(watchlist!=null) {
                // set cover
                list = watchlist;
                setCover(watchlist.getMovies());
            }
            else {
                //                showCenteredToast("errore caricamento Watchlist");
            }
        });

        //
        loginViewModel = new ViewModelProvider(requireActivity()).get(LoginViewModel.class);
        loginViewModel.getObservableLoginResult().observe(getViewLifecycleOwner(), loginResult -> {
            switch (loginResult) {
                case SUCCESS: case REMEMBER_ME_EXISTS: {
                    User loggedUser = loginViewModel.getLoggedUser();
                    if(loggedUser!=null) {
                        listCoverViewModel.fetchList(loggedUser, MoviesList.ListType.WL);
                    }
                }
                break;
            }
        });

    }// end onViewCreated()



    //--------------------------------------------------------------------------------------- METHODS

    private void setCover(ArrayList<Movie> moviesList) {
        ImageView thumbnail_1 = view.findViewById(R.id.imageView_essentialListCover_1);
        ImageView thumbnail_2 = view.findViewById(R.id.imageView_essentialListCover_2);
        ImageView thumbnail_3 = view.findViewById(R.id.imageView_essentialListCover_3);
        ImageView thumbnail_4 = view.findViewById(R.id.imageView_essentiallistCover_4);

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
        progressIndicator.setVisibility(View.GONE);
    }// end setThumbnails()



    @Override
    public void onClick(View v) {
        int viewId = v.getId();

        if(viewId == R.id.cardView_essentialListCover) {
            if(list!=null && list.getMovies() != null && list.getMovies().size()>0) {
                NavGraphDirections.ActionGlobalListFragment listFragment =
                        NavGraphDirections.actionGlobalListFragment(list, "", "");
                NavHostFragment.findNavController(WatchistCoverFragment.this).navigate(listFragment);
            }
            else showCenteredToast("lista vuota");
        }
    }

    private void showCenteredToast(String s) {
        final Toast toast = Toast.makeText(getContext(), s, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0,0);
        toast.show();
    }

}// end WatchlistFragment class