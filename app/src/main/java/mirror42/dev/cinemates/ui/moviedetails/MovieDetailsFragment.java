package mirror42.dev.cinemates.ui.moviedetails;

import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;
import mirror42.dev.cinemates.R;
import mirror42.dev.cinemates.adapter.RecyclerAdapterActorsHorizontalList;
import mirror42.dev.cinemates.tmdbAPI.model.Movie;
import mirror42.dev.cinemates.tmdbAPI.model.Person;
import mirror42.dev.cinemates.utilities.FirebaseEventsLogger;


public class MovieDetailsFragment extends Fragment implements View.OnClickListener {

    private MovieDetailsViewModel movieDetailsViewModel;
    private RecyclerAdapterActorsHorizontalList recyclerAdapterActorsHorizontalList;
    private View view;
    private FloatingActionButton addToListButton;




    //------------------------------------------------------------------------ LIFECYCLE METHODS

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
        addToListButton = view.findViewById(R.id.button_movie_details_fragment_add_to_list);
        addToListButton.setOnClickListener(this);

        //
        if(getArguments() != null) {
            MovieDetailsFragmentArgs args = MovieDetailsFragmentArgs.fromBundle(getArguments());
            Movie movie = args.getMovie();

            if(movie != null) {
                int movieId = movie.getTmdbID();

                // getting main activity
                // and hiding action bar
//                MainActivity main = (MainActivity) getActivity();
//                if(main!=null && main.getSupportActionBar()!=null) {
//                    main.getSupportActionBar().hide();

//                }

                //1
                initRecyclerView();

                //2
                movieDetailsViewModel = new ViewModelProvider(this).get(MovieDetailsViewModel.class);
                movieDetailsViewModel.getMovie().observe(getViewLifecycleOwner(), new Observer<Movie>() {
                    @Override
                    public void onChanged(@Nullable Movie movie) {
                        if(movie!=null) {
                            composeUI(movie);
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


            }// inner if
        }// outer if

        FirebaseEventsLogger firebaseEventsLogger = FirebaseEventsLogger.getInstance();
        firebaseEventsLogger.logScreenEvent(this, "Movie Details page", getContext());

    }// end onViewCreated()



    //------------------------------------------------------------- METHODS

    private void initRecyclerView() {
        // defining HORIZONTAL layout manager for recycler
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);

        // defining Recycler view
        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.recyclerview_movie_details_fragment_cast);
        recyclerView.setLayoutManager(linearLayoutManager);

        // assigning adapter to recycle
        recyclerAdapterActorsHorizontalList = new RecyclerAdapterActorsHorizontalList(new ArrayList<Person>(), getContext());
        recyclerView.setAdapter(recyclerAdapterActorsHorizontalList);
    }


    private void composeUI(Movie movie) {
        ImageView backdrop = view.findViewById(R.id.imageview_movie_details_fragment_backdrop);
        TextView title = view.findViewById(R.id.textView_movie_details_fragment_movie_title);
        TextView releaseDate = view.findViewById(R.id.textView_movie_details_fragment_release_date);
        TextView overview = view.findViewById(R.id.textView_movie_details_fragment_overview);
        ImageView poster = view.findViewById(R.id.imageView_movie_details_fragment_poster);
        TextView duration = view.findViewById(R.id.textView_movie_details_fragment_duration);
        TextView genres = view.findViewById(R.id.textView_movie_details_fragment_genres);
        TextView releaseStatus = view.findViewById(R.id.textView_movie_details_fragment_release_status);


        title.setText(movie.getTitle());
        releaseDate.setText(movie.getReleaseDate());
        overview.setText(movie.getOverview());
        duration.setText(String.valueOf(movie.getDuration()) + " min");
        genres.setText(fixGenresPrint(movie));
        releaseStatus.setText(movie.getReleaseStatus());


        Glide.with(this)  //2
                .load(movie.getPosterURL()) //3
                .fallback(R.drawable.broken_image)
                .placeholder(R.drawable.placeholder_image)
                .fitCenter() //4
                .into(poster); //8


        Glide.with(this)  //2
                .load(movie.getBackdropURL()) //3
                .fallback(R.drawable.broken_image)
                .placeholder(R.drawable.placeholder_image)
                .fitCenter() //4
                .into(backdrop); //8


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


    @Override
    public void onClick(View v) {
        Animation buttonAnim = AnimationUtils.loadAnimation(getContext(), R.anim.push_button_animation);
        addToListButton.startAnimation(buttonAnim);

        try {
            //
            BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(getContext(), R.style.bottom_sheet_dialog_theme);
            bottomSheetDialog.setCanceledOnTouchOutside(false);
            bottomSheetDialog.setDismissWithAnimation(true);
            bottomSheetDialog.setTitle("test");
            View bottomSheetView = LayoutInflater.from(getContext().getApplicationContext()).inflate(R.layout.bottom_sheet_layout, (ConstraintLayout)view.findViewById(R.id.bottom_sheet_container));
            bottomSheetView.findViewById(R.id.button_addToList_dialog).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    bottomSheetDialog.dismiss();
                }
            });
            bottomSheetDialog.setContentView(bottomSheetView);
            bottomSheetDialog.show();
        } catch (Exception e) {
            e.getMessage();
            e.printStackTrace();
        }
    }



}// end MovieDetailsFragment class