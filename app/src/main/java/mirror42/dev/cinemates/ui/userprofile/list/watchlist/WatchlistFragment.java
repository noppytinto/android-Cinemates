package mirror42.dev.cinemates.ui.userprofile.list.watchlist;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import mirror42.dev.cinemates.NavGraphDirections;
import mirror42.dev.cinemates.R;
import mirror42.dev.cinemates.listener.RecyclerListener;
import mirror42.dev.cinemates.model.User;
import mirror42.dev.cinemates.tmdbAPI.model.Movie;
import mirror42.dev.cinemates.ui.explore.ExploreFragmentDirections;
import mirror42.dev.cinemates.ui.login.LoginViewModel;
import mirror42.dev.cinemates.ui.userprofile.UserProfileFragmentDirections;
import mirror42.dev.cinemates.utilities.FirebaseAnalytics;
import mirror42.dev.cinemates.utilities.ImageUtilities;


public class WatchlistFragment extends Fragment implements
        RecyclerListener.OnClick_RecyclerListener, View.OnClickListener {
    private final String TAG = getClass().getSimpleName();
    private WatchlistViewModel watchlistViewModel;
    private LoginViewModel loginViewModel;
    private RecyclerAdapterMoviesList recyclerAdapterMoviesList;
    private View view;
    private ArrayList<Movie> movies;
    private CardView cardView;
    private boolean enableClick;



    //--------------------------------------------------------------------------------------- ANDROID METHODS

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_watchlist, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.view = view;
        cardView = view.findViewById(R.id.cardView_listStub_watchlist);
        cardView.setOnClickListener(this);

        //1
//        initRecyclerView();

        //2
        watchlistViewModel = new ViewModelProvider(this).get(WatchlistViewModel.class);
        watchlistViewModel.getMoviesList().observe(getViewLifecycleOwner(), moviesList -> {
            if(moviesList!=null) {
                // reverse list
                Collections.reverse(moviesList);
                movies = moviesList;

                // set thumbnails
                setThumbnails(moviesList);


            }
            else {
                Toast toast = Toast.makeText(getContext(), "errore caricamento Watchlist", Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
            }
        });

        //
        loginViewModel = new ViewModelProvider(requireActivity()).get(LoginViewModel.class);
        loginViewModel.getLoginResult().observe(getViewLifecycleOwner(), loginResult -> {
            switch (loginResult) {
                case SUCCESS: {
                    User user = loginViewModel.getUser().getValue();
                    if(user!=null) {
                        String email = user.getEmail();
                        watchlistViewModel.fetchData(email, user.getAccessToken());

                    }
                }
                break;
            }
        });







    }// end onViewCreated()





    //--------------------------------------------------------------------------------------- METHODS

    private void setThumbnails(ArrayList<Movie> moviesList) {
        ImageView thumbnail_1 = view.findViewById(R.id.imageView_listStub_1);
        ImageView thumbnail_2 = view.findViewById(R.id.imageView_listStub_2);
        ImageView thumbnail_3 = view.findViewById(R.id.imageView_listStub_3);
        ImageView thumbnail_4 = view.findViewById(R.id.imageView_listStub_4);
        ImageView thumbnail_5 = view.findViewById(R.id.imageView_listStub_5);
        ImageView thumbnail_6 = view.findViewById(R.id.imageView_listStub_6);
        ImageView thumbnail_7 = view.findViewById(R.id.imageView_listStub_7);
        ImageView thumbnail_8 = view.findViewById(R.id.imageView_listStub_8);

        ArrayList<ImageView> thumbnailsList = new ArrayList<>();
        thumbnailsList.add(thumbnail_1);
        thumbnailsList.add(thumbnail_2);
        thumbnailsList.add(thumbnail_3);
        thumbnailsList.add(thumbnail_4);
        thumbnailsList.add(thumbnail_5);
        thumbnailsList.add(thumbnail_6);
        thumbnailsList.add(thumbnail_7);
        thumbnailsList.add(thumbnail_8);

        for(int i=0; i<8 && i<moviesList.size(); i++) {
            ImageView t = thumbnailsList.get(i);
            String posterUrl = moviesList.get(i).getPosterURL();

            ImageUtilities.loadRectangularImageInto(posterUrl, t, getContext());
        }
    }// end setThumbnails()








    @Override
    public void onItemClick(View view, int position) {
        try {
            //
            Movie movieSelected = recyclerAdapterMoviesList.getMoviesList(position);

            //
            FirebaseAnalytics firebaseAnalytics = FirebaseAnalytics.getInstance();
            firebaseAnalytics.logSelectedMovie(movieSelected, "selected movie in Watchlist", this, getContext());

            //
            NavGraphDirections.AnywhereToMovieDetailsFragment
                    action = ExploreFragmentDirections.anywhereToMovieDetailsFragment(movieSelected);
            NavHostFragment.findNavController(WatchlistFragment.this).navigate(action);
        } catch (Exception e) {
            e.getMessage();
            e.printStackTrace();
        }
    }

    @Override
    public void onItemLongClick(View view, int position) {
        //ignored
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        if(id == R.id.cardView_listStub_watchlist) {
            if(movies!=null) {
                Movie[] m = movies.toArray(new Movie[0]);

                UserProfileFragmentDirections.ActionUserProfileFragmentToListFragment action =
                        UserProfileFragmentDirections.actionUserProfileFragmentToListFragment(m);
                NavHostFragment.findNavController(WatchlistFragment.this).navigate(action);
            }
            else {
                Toast toast = Toast.makeText(getContext(), "caricamento Watchlist in corso...", Toast.LENGTH_SHORT);
                toast.show();
            }
        }
    }
}// end WatchlistFragment class