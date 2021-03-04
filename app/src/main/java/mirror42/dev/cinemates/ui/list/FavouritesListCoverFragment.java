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

import static androidx.appcompat.content.res.AppCompatResources.getDrawable;

public class FavouritesListCoverFragment extends Fragment implements View.OnClickListener {
    private final String TAG = getClass().getSimpleName();
    private ListCoverViewModel listCoverViewModel;
    private LoginViewModel loginViewModel;
    private View view;
    private ArrayList<Movie> movies;
    private MoviesList list;
    private CardView cardView;
    private LinearProgressIndicator progressIndicator;
    private String targetUsername;
    private boolean isMyList;


    //--------------------------------------------------------------------------------------- ANDROID METHODS

    public static FavouritesListCoverFragment newInstance() {
        return new FavouritesListCoverFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_essential_list_cover, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.view = view;
        init(view);
        observeFetchListTask();

        //
        isMyList = true;
        if(getArguments()!=null) {
            isMyList = (boolean) getArguments().getSerializable("list_ownership");
            targetUsername = (String) getArguments().getSerializable("list_owner_username");

            if(isMyList) fetcMyList();
            else fetOtherUserList(targetUsername);
        }
        else {
            if(isMyList) fetcMyList();
        }

    }// end onViewCreated()



    //--------------------------------------------------------------------------------------- METHODS

    private void fetOtherUserList(String targetUsername) {
        loginViewModel.getObservableLoginResult().observe(getViewLifecycleOwner(), loginResult -> {
            switch (loginResult) {
                case SUCCESS:
                case REMEMBER_ME_EXISTS: {
                    User loggedUser = loginViewModel.getLoggedUser();
                    if(loggedUser!=null) {
                        listCoverViewModel.fetchOtherUserList(targetUsername, loggedUser, MoviesList.ListType.FV);
                    }
                }
                break;
            }
        });
    }


    private void observeFetchListTask() {
        listCoverViewModel.getObservableFavoritesList().observe(getViewLifecycleOwner(), favoritesList -> {
            progressIndicator.setVisibility(View.GONE);
            if(favoritesList!=null) {
                // set cover
                list = favoritesList;
                setCover(favoritesList.getMovies());
            }
            else {
                //                showCenteredToast("errore caricamento Watchlist");
            }
        });
    }

    private void init(View view) {
        cardView = view.findViewById(R.id.cardView_essentialListCover);
        cardView.setOnClickListener(this);
        progressIndicator = view.findViewById(R.id.progressIndicator_essentialListCover);
        TextView listTitle = view.findViewById(R.id.textView_essentialListCover_title);
        listTitle.setText("Preferiti");
        listTitle.setTextColor(getResources().getColor(R.color.yellow));
        ImageView imageViewListIcon = view.findViewById(R.id.imageView_essentialListCover_icon);
        imageViewListIcon.setImageDrawable(getDrawable(requireContext(), R.drawable.ic_star_yellow));

        //2
        listCoverViewModel = new ViewModelProvider(this).get(ListCoverViewModel.class);
        loginViewModel = new ViewModelProvider(requireActivity()).get(LoginViewModel.class);
    }

    private void fetcMyList() {
        loginViewModel.getObservableLoginResult().observe(getViewLifecycleOwner(), loginResult -> {
            switch (loginResult) {
                case SUCCESS: case REMEMBER_ME_EXISTS: {
                    User loggedUser = loginViewModel.getLoggedUser();
                    if(loggedUser!=null) {
                        listCoverViewModel.fetchMyList(loggedUser, MoviesList.ListType.FV);
                    }
                }
                break;
            }
        });
    }



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
                User owner = new User();
                if( ! isMyList) {
                    owner.setUsername(targetUsername);
                    list.setOwner(owner);
                }
                else {
                    owner.setUsername(loginViewModel.getLoggedUser().getUsername());
                    list.setOwner(owner);
                }

                NavGraphDirections.ActionGlobalListFragment listFragment =
                        NavGraphDirections.actionGlobalListFragment(list, "", "");
                NavHostFragment.findNavController(FavouritesListCoverFragment.this).navigate(listFragment);
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