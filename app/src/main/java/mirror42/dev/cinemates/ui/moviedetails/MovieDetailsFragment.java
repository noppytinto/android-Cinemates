package mirror42.dev.cinemates.ui.moviedetails;

import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
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
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;

import mirror42.dev.cinemates.MainActivity;
import mirror42.dev.cinemates.R;
import mirror42.dev.cinemates.adapter.RecyclerAdapterActorsHorizontalList;
import mirror42.dev.cinemates.model.User;
import mirror42.dev.cinemates.tmdbAPI.model.Movie;
import mirror42.dev.cinemates.tmdbAPI.model.Person;
import mirror42.dev.cinemates.ui.login.LoginViewModel;
import mirror42.dev.cinemates.utilities.FirebaseAnalytics;


public class MovieDetailsFragment extends Fragment implements View.OnClickListener {
    private final String TAG = getClass().getSimpleName();
    private MovieDetailsViewModel movieDetailsViewModel;
    private RecyclerAdapterActorsHorizontalList recyclerAdapterActorsHorizontalList;
    private View view;
    private FloatingActionButton addToListButton;
    private LoginViewModel loginViewModel;
    private int currentMovieId;




    //------------------------------------------------------------------------ ANDROID METHODS

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

//        // This callback will only be called when MyFragment is at least Started.
//        OnBackPressedCallback callback = new OnBackPressedCallback(true /* enabled by default */) {
//            @Override
//            public void handleOnBackPressed() {
//                // Handle the back button event
//
//                // getting main activity
//                // and hding action bar
//                MainActivity main = (MainActivity) getActivity();
//                if(main!=null && main.getSupportActionBar()!=null)
//                    main.getSupportActionBar().show();
//                NavHostFragment.findNavController(MovieDetails_fragment.this)
//                        .navigate(R.id.action_movieDetails_fragment_to_mainFragment);
//            }
//        };
//        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), callback);
//        // The callback can be enabled or disabled here or in handleOnBackPressed()


        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_movie_details, container, false);
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.view = view;
        addToListButton = view.findViewById(R.id.button_movieDetailsFragment_addToList);
        addToListButton.setOnClickListener(this);

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

                // getting main activity
                // and hiding action bar
//                MainActivity main = (MainActivity) getActivity();
//                if(main!=null && main.getSupportActionBar()!=null) {
//                    main.getSupportActionBar().hide();

//                }

                loginViewModel = new ViewModelProvider(requireActivity()).get(LoginViewModel.class);
                loginViewModel.getLoginResult().observe(getViewLifecycleOwner(), loginResult -> {
                    switch (loginResult) {
                        case SUCCESS: case REMEMBER_ME_EXISTS:
                            addToListButton.setVisibility(View.VISIBLE);
                            break;
                        default:
                            addToListButton.setVisibility(View.GONE);
                    }

                });


                //1
                initRecyclerView();

                //2
                movieDetailsViewModel = new ViewModelProvider(this).get(MovieDetailsViewModel.class);
                movieDetailsViewModel.getMovie().observe(getViewLifecycleOwner(), new Observer<Movie>() {
                    @Override
                    public void onChanged(@Nullable Movie movie) {
                        if(movie!=null) {
                            updateUI(movie);
                        }
                        else {
                            Toast toast = Toast.makeText(getContext(), "errore caricamento dettagli Film", Toast.LENGTH_SHORT);
                            toast.setGravity(Gravity.CENTER, 0, 0);
                            toast.show();
                        }
                    }
                });

                // downloading data
                movieDetailsViewModel.downloadData(movieId);

                // observe add to list action status
                movieDetailsViewModel.getAddToListStatus().observe(getViewLifecycleOwner(), addToListStatus ->  {
                    switch (addToListStatus) {
                        case SUCCESS:
                            Toast.makeText(getContext(), "film aggiunto a Watchlist", Toast.LENGTH_LONG).show();
                            break;
                        case FAILED:
                            Toast.makeText(getContext(), "film NON aggiunto a watchlist", Toast.LENGTH_LONG).show();
                            break;
                    }
                });
            }// inner if
        }// outer if



    }// end onViewCreated()

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: ");
        MainActivity mainActivity = (MainActivity) getActivity();
        mainActivity.hideLogo();
        mainActivity = null;
    }

    @Override
    public void onClick(View v) {
        int buttonId = v.getId();

        final String[] choices = {"Watchlist", "2", "3"};
        final boolean[] checkedItems = {false, false, false};
        ArrayList<Integer> res = new ArrayList<>();

        if(buttonId == R.id.button_movieDetailsFragment_addToList) {
            Animation buttonAnim = AnimationUtils.loadAnimation(getContext(), R.anim.push_button_animation);
            addToListButton.startAnimation(buttonAnim);

            // create a dialog with AlertDialog builder
            MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getContext()); //TODO: pass style here, once defined
            builder.setTitle("Scegli liste").setNeutralButton("Annulla", null);
            builder.setPositiveButton("Fatto", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    try {
                        for(Integer x: res) {
                            Log.d(TAG, "clicked item index is " + x);

                            if(x==0) {
                                // add movie to watchlist
                                User user = loginViewModel.getLoggedUser().getValue();
                                movieDetailsViewModel.addMovieToWatchList(currentMovieId, user.getEmail(), user.getAccessToken());
                            }

    //                        Toast.makeText(getContext(), "lista selezionata: " + choices[x], Toast.LENGTH_LONG).show();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            builder.setMultiChoiceItems(choices, checkedItems, new DialogInterface.OnMultiChoiceClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                    if(isChecked) {
                        try {
                            res.add(which);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    else {
                        try {
                            res.remove(which);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
            builder.show();




//            MaterialAlertDialogBuilder(getContext())
//                    .setTitle("Tue liste")
//                    .setNeutralButton(" ")){ dialog, which ->
//                // Respond to neutral button press
//            }
//        .setPositiveButton(resources.getString(R.string.ok)) { dialog, which ->
//                // Respond to positive button press
//            }
//            // Single-choice items (initialized with checked item)
//        .setSingleChoiceItems(singleItems, checkedItem) { dialog, which ->
//                // Respond to item chosen
//            }
//        .show()




//            try {
//                //
//                BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(getContext(), R.style.bottom_sheet_dialog_theme);
//                bottomSheetDialog.setCanceledOnTouchOutside(false);
//                bottomSheetDialog.setDismissWithAnimation(true);
//                bottomSheetDialog.setTitle("test");
//                View bottomSheetView = LayoutInflater.from(getContext().getApplicationContext()).inflate(R.layout.bottom_sheet_addtolist, (ConstraintLayout)view.findViewById(R.id.bottom_sheet_container));
//                bottomSheetView.findViewById(R.id.button_addToList).setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        bottomSheetDialog.dismiss();
//                    }
//                });
//                bottomSheetDialog.setContentView(bottomSheetView);
//                bottomSheetDialog.show();
//            } catch (Exception e) {
//                e.getMessage();
//                e.printStackTrace();
//            }

        }
        else if(buttonId == R.id.button_movieDetailsFragment_seeAllCast) {

        }

    }

    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu) {
        super.onPrepareOptionsMenu(menu);

        MenuItem menuItem = menu.getItem(1);
        menuItem.setVisible(false);
    }




    //------------------------------------------------------------- METHODS

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
        ImageView backdrop = view.findViewById(R.id.imageView_movieDetailsFragment_backdrop);
        TextView title = view.findViewById(R.id.textView_movieDetailsFragment_movieTitle);
        TextView releaseDate = view.findViewById(R.id.textView_movieDetailsFragment_releaseDate);
        TextView overview = view.findViewById(R.id.textView_movieDetailsFragment_overview);
        ImageView poster = view.findViewById(R.id.imageView_movieDetailsFragment_poster);
        TextView duration = view.findViewById(R.id.textView_movieDetailsFragment_duration);
        TextView genres = view.findViewById(R.id.textView_movieDetailsFragment_genres);
        TextView releaseStatus = view.findViewById(R.id.textView_movieDetailsFragment_releaseStatus);


        title.setText(movie.getTitle());
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
                    .fallback(R.drawable.broken_image)
                    .placeholder(R.drawable.placeholder_image)
                    .fitCenter() //4
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




}// end MovieDetailsFragment class