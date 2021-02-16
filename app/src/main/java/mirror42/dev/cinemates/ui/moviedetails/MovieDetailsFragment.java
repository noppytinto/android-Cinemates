package mirror42.dev.cinemates.ui.moviedetails;

import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import mirror42.dev.cinemates.MainActivity;
import mirror42.dev.cinemates.R;
import mirror42.dev.cinemates.adapter.RecyclerAdapterActorsHorizontalList;
import mirror42.dev.cinemates.model.User;
import mirror42.dev.cinemates.model.list.MoviesList;
import mirror42.dev.cinemates.model.tmdb.Movie;
import mirror42.dev.cinemates.model.tmdb.Person;
import mirror42.dev.cinemates.ui.login.LoginViewModel;
import mirror42.dev.cinemates.ui.notification.NotificationsViewModel;
import mirror42.dev.cinemates.utilities.FirebaseAnalytics;


public class MovieDetailsFragment extends Fragment implements View.OnClickListener {
    private final String TAG = getClass().getSimpleName();
    private MovieDetailsViewModel movieDetailsViewModel;
    private RecyclerAdapterActorsHorizontalList recyclerAdapterActorsHorizontalList;
    private View view;
    private FloatingActionButton addToListButton;
    private LoginViewModel loginViewModel;
    private int currentMovieId;
    private NotificationsViewModel notificationsViewModel;
    private MaterialToolbar toolbar;
    private ChipGroup chipGroupLists;
    private Chip chipWatched;
    private Chip chipWatchlist;
    private Chip chipFavorites;
    private boolean isInWatchlist;
    private boolean isInWatchedList;
    private boolean isInFavoritesList;
    private LinkedHashMap<String, Boolean> customListsCheckMapping;



    //------------------------------------------------------------------------ ANDROID METHODS
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        ((MainActivity)requireActivity()).hideToolbar();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_movie_details, container, false);
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.view = view;
        addToListButton = view.findViewById(R.id.button_movieDetailsFragment_addToList);
        addToListButton.setOnClickListener(this);
        notificationsViewModel = new ViewModelProvider(requireActivity()).get(NotificationsViewModel.class);
        toolbar = view.findViewById(R.id.toolbar_movieDetailsFragment);
        toolbar.setNavigationIcon(R.drawable.ic_back_arrow_light_blue);
        toolbar.setNavigationOnClickListener(v -> requireActivity().onBackPressed());
        chipGroupLists = view.findViewById(R.id.chipGroup_movieDetailsFragment_lists);
        chipWatched = view.findViewById(R.id.chip_movieDetailsfragment_watched);
        chipWatchlist = view.findViewById(R.id.chip_movieDetailsfragment_watchlist);
        chipFavorites = view.findViewById(R.id.chip_movieDetailsfragment_favorites);

        //
        FirebaseAnalytics firebaseAnalytics = FirebaseAnalytics.getInstance();
        firebaseAnalytics.logScreenEvent(this, "Movie Details page", getContext());

        //
        if(getArguments() != null) {
            MovieDetailsFragmentArgs args = MovieDetailsFragmentArgs.fromBundle(getArguments());
            Movie movie = args.getMovie();

            if(movie != null) {
                int movieId = movie.getTmdbID();
                currentMovieId = movieId;
                movieDetailsViewModel = new ViewModelProvider(this).get(MovieDetailsViewModel.class);
                loginViewModel = new ViewModelProvider(requireActivity()).get(LoginViewModel.class);
                loginViewModel.getObservableLoginResult().observe(getViewLifecycleOwner(), loginResult -> {
                    switch (loginResult) {
                        case SUCCESS: case REMEMBER_ME_EXISTS: {
                            addToListButton.setVisibility(View.VISIBLE);
                            //
                            setListChips(movieId, loginViewModel.getLoggedUser());
                            setCustomList(movieId, loginViewModel.getLoggedUser());
                        }
                            break;
                        default:
                            addToListButton.setVisibility(View.GONE);
                    }
                });


                //1
                initRecyclerView();

                //2
                movieDetailsViewModel.getMovie().observe(getViewLifecycleOwner(), movie1 -> {
                    if(movie1 !=null) updateUI(movie1);
                    else showCenteredToast("errore caricamento dettagli Film");
                });

                // downloading data
                movieDetailsViewModel.downloadData(movieId);

                // observe add to list action status
                movieDetailsViewModel.getAddToListStatus().observe(getViewLifecycleOwner(), addToListStatus ->  {
                    switch (addToListStatus) {
                        case SUCCESS:
                            Toast.makeText(getContext(), "film aggiunto alla lista", Toast.LENGTH_SHORT).show();
                            break;
                        case FAILED:
                            Toast.makeText(getContext(), "film NON aggiunto alla lista", Toast.LENGTH_SHORT).show();
                            break;
                    }
                });



            }// inner if
        }// outer if



    }// end onViewCreated()

    private void showCenteredToast(String msg) {
        Toast toast = Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    @Override
    public void onClick(View v) {
        int buttonId = v.getId();

        ArrayList<String> availableLists = new ArrayList<>();
        ArrayList<Boolean> movieExists = new ArrayList<>();

        //build essential lists
        availableLists.add("Watchlist");
        movieExists.add(isInWatchlist);
        availableLists.add("Preferiti");
        movieExists.add(isInFavoritesList);
        availableLists.add("Visti");
        movieExists.add(isInWatchedList);

        // build custom lists list
        if(customListsCheckMapping!=null) {
            for (String s: customListsCheckMapping.keySet()) {
                availableLists.add(s);
                movieExists.add(customListsCheckMapping.get(s));
            }
        }


        // build checkbox dialog
        String[] choices = availableLists.toArray(new String[0]);
        boolean[] checkedItems = new boolean[movieExists.size()];
        for(int i=0; i<movieExists.size(); i++) {
            checkedItems[i] = movieExists.get(i);
        }

        ArrayList<Integer> checkedLists = new ArrayList<>();

        if(buttonId == R.id.button_movieDetailsFragment_addToList) {
            Animation buttonAnim = AnimationUtils.loadAnimation(getContext(), R.anim.push_button_animation);
            addToListButton.startAnimation(buttonAnim);

            // create a dialog with AlertDialog builder
            MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext()); //TODO: pass style here, once defined
            builder.setTitle("Scegli liste").setNeutralButton("Annulla", null);
            builder.setPositiveButton("Fatto", (dialog, which) -> {
                try {
                    for(Integer x: checkedLists) {
                        if(x == 0) {
                            // add movie to watchlist
                            User user = loginViewModel.getLoggedUser();
                            movieDetailsViewModel.addMovieToEssentialList(currentMovieId, MoviesList.ListType.WL ,user);
                        }
                        else if(x == 1){
                            User user = loginViewModel.getLoggedUser();
                            movieDetailsViewModel.addMovieToEssentialList(currentMovieId, MoviesList.ListType.FV, user);
                        }
                        else if(x == 2){
                            User user = loginViewModel.getLoggedUser();
                            movieDetailsViewModel.addMovieToEssentialList(currentMovieId, MoviesList.ListType.WD, user);
                        }
                        else if(x>2) {
                            String listName = choices[x];
                            User user = loginViewModel.getLoggedUser();
                            movieDetailsViewModel.addMovieToCustomList(currentMovieId, listName, user);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            builder.setMultiChoiceItems(choices, checkedItems, (dialog, which, isChecked) -> {
                if(isChecked) {
                    try {
                        checkedLists.add(which);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                else {
                    try {
                        checkedLists.remove(which);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            builder.show();

        }
        else if(buttonId == R.id.button_movieDetailsFragment_seeAllCast) {

        }

    }

    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu) {
        super.onPrepareOptionsMenu(menu);

        MenuItem menuItem = menu.getItem(1);
        menuItem.setVisible(false);

        checkForNewNotifications();
    }






    //------------------------------------------------------------- METHODS

    private void setListChips(int movieId, User loggedUser) {
        movieDetailsViewModel.checkIsInWatchedList(movieId, loggedUser);
        movieDetailsViewModel.checkIsInWatchlist(movieId, loggedUser);
        movieDetailsViewModel.checkIsInFavoritesList(movieId, loggedUser);

        movieDetailsViewModel.getWatchlistStatus().observe(getViewLifecycleOwner(), checkListStatus -> {
            switch (checkListStatus) {
                case IS_IN_WATCHLIST:
                    chipWatchlist.setVisibility(View.VISIBLE);
                    isInWatchlist = true;
                    break;
                case FAILED:
                    chipWatchlist.setVisibility(View.GONE);
                    isInWatchlist = false;
                    break;
            }
        });

        movieDetailsViewModel.getWatchedListStatus().observe(getViewLifecycleOwner(), checkListStatus -> {
            switch (checkListStatus) {
                case IS_IN_WATCHED_LIST:
                    chipWatched.setVisibility(View.VISIBLE);
                    isInWatchedList = true;
                    break;
                case FAILED:
                    chipWatched.setVisibility(View.GONE);
                    isInWatchedList = false;
                    break;
            }
        });

        movieDetailsViewModel.getFavoritesListStatus().observe(getViewLifecycleOwner(), checkListStatus -> {
            switch (checkListStatus) {
                case IS_IN_FAVORITES_LIST:
                    chipFavorites.setVisibility(View.VISIBLE);
                    isInFavoritesList = true;
                    break;
                case FAILED:
                    chipFavorites.setVisibility(View.GONE);
                    isInFavoritesList = false;
                    break;
            }
        });

    }// end setListChips()

    private void setCustomList(int movieId, User loggedUser) {
        movieDetailsViewModel.checkIsInCustomLists(movieId, loggedUser);
        movieDetailsViewModel.getCustomListsCheckStatus().observe(getViewLifecycleOwner(), checkListStatus -> {
            switch (checkListStatus) {
                case CUSTOM_LISTS_CHECKED:
                    customListsCheckMapping = movieDetailsViewModel.getCustomListsCheckResult().getValue();
                    break;
                case FAILED:
                    break;
            }
        });
    }

    private void buildCheckBox() {

    }

    private void initRecyclerView() {
        // defining HORIZONTAL layout manager for recycler
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);

        // defining Recycler view
        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.recyclerview_movieDetailsFragment_cast);
        recyclerView.setLayoutManager(linearLayoutManager);

        // assigning adapter to recycle
        recyclerAdapterActorsHorizontalList = new RecyclerAdapterActorsHorizontalList(new ArrayList<Person>(), getContext());
        recyclerView.setAdapter(recyclerAdapterActorsHorizontalList);
    }

    private void updateUI(Movie movie) {
        ImageView backdrop = view.findViewById(R.id.appBarImage_movieDetailsFragment);
        TextView releaseDate = view.findViewById(R.id.textView_movieDetailsFragment_releaseDate);
        TextView overview = view.findViewById(R.id.textView_movieDetailsFragment_overview);
        ImageView poster = view.findViewById(R.id.imageView_movieDetailsFragment_poster);
        TextView duration = view.findViewById(R.id.textView_movieDetailsFragment_duration);
        TextView genres = view.findViewById(R.id.textView_movieDetailsFragment_genres);
        TextView releaseStatus = view.findViewById(R.id.textView_movieDetailsFragment_releaseStatus);

        toolbar.setTitle(movie.getTitle());


        releaseDate.setText(movie.getReleaseDate());
        overview.setText(movie.getOverview());
        duration.setText(String.valueOf(movie.getDuration()) + " min");
        genres.setText(fixGenresPrint(movie));
        releaseStatus.setText(movie.getReleaseStatus());


        try {
            Glide.with(this)  //2
                    .load(movie.getPosterURL()) //3
                    .fallback(R.drawable.broken_image)
                    .placeholder(R.drawable.placeholder_image)
                    .fitCenter() //4
                    .into(poster); //8
        } catch (Exception e) {
            e.printStackTrace();
        }


        try {
            Glide.with(this)  //2
                    .load(movie.getBackdropURL()) //3
                    .fallback(R.drawable.backdrop_placeholder_small)
                    .placeholder(R.drawable.backdrop_placeholder_small)
                    .centerCrop() //4
                    .into(backdrop); //8
        } catch (Exception e) {
            e.printStackTrace();
        }


        // filling cast&crew list
        recyclerAdapterActorsHorizontalList.loadNewData(movie.getCastAndCrew());
    }

    public String fixGenresPrint(Movie in) {
        // delete square brackets
        String temp = in.getGenres().toString();
        if(temp == null || temp.isEmpty()) {
            return "";
        }
        else {
            temp = temp.substring(1, temp.length()-1);
        }

        return temp;
    }

    private void checkForNewNotifications() {
        if(notificationsViewModel!=null) {
            if(notificationsViewModel.getNotificationsStatus().getValue() == NotificationsViewModel.NotificationsStatus.GOT_NEW_NOTIFICATIONS) {
                ((MainActivity) getActivity()).activateNotificationsIcon();
            }
            else {
                ((MainActivity) getActivity()).deactivateNotificationsIcon();
            }
        }
    }


}// end MovieDetailsFragment class