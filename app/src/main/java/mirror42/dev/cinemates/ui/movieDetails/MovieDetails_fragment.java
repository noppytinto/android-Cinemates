package mirror42.dev.cinemates.ui.movieDetails;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import mirror42.dev.cinemates.MyValues;
import mirror42.dev.cinemates.R;
import mirror42.dev.cinemates.adapters.RecycleAdapterActorsHorizontalList;
import mirror42.dev.cinemates.asyncTasks.DownloadMovieDetails;
import mirror42.dev.cinemates.tmdbAPI.Movie;


public class MovieDetails_fragment extends Fragment implements DownloadMovieDetails.DownloadListener, View.OnClickListener {
    private RecycleAdapterActorsHorizontalList recycleAdapterActorsHorizontalList;
    private View view;
    private FloatingActionButton addToListButton;

    public MovieDetails_fragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

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





        if(getArguments() != null) {
//            MovieDetails_fragmentArgs args = MovieDetails_fragmentArgs.fromBundle(getArguments());
//            Movie movie = args.getMovie();
//
//            if(movie != null) {
//
//                // getting main activity
//                // and hiding action bar
////                MainActivity main = (MainActivity) getActivity();
////                if(main!=null && main.getSupportActionBar()!=null) {
////                    main.getSupportActionBar().hide();
//
////                }
//
//
//                int movieID = movie.getTmdbID();
//                // starting async task here because of google suggestions
//                DownloadMovieDetails downloadMovieDetails = new DownloadMovieDetails(this);
//                downloadMovieDetails.execute(movieID);
//
//                // defining HORIZONTAL layout manager for recycler
//                LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
//                linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
//
//                // defining Recycler view
//                RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.recyclerview_movie_details_fragment_cast);
//                recyclerView.setLayoutManager(linearLayoutManager);
//
//                // assigning adapter to recycle
//                recycleAdapterActorsHorizontalList = new RecycleAdapterActorsHorizontalList(new ArrayList<Person>(), getContext());
//                recyclerView.setAdapter(recycleAdapterActorsHorizontalList);
//            }// inner if
        }// outer if
    }// end onViewCreated()



        //------------------------------------------------------------- METHODS

    @Override
    public void onDownloadComplete(Movie movie, MyValues.DownloadStatus status) {
        if (status == MyValues.DownloadStatus.OK) {
            Toast.makeText(getContext(), "Movie details retrieved! :D", Toast.LENGTH_LONG).show();
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
            recycleAdapterActorsHorizontalList.loadNewData(movie.getCastAndCrew());

        } else {
            Toast.makeText(getContext(), "Movie details NOT retrieved! D:", Toast.LENGTH_SHORT).show();
        }
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
            BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(getContext(), R.style.BottomSheetDialogTheme);
            bottomSheetDialog.setCanceledOnTouchOutside(false);
            bottomSheetDialog.setDismissWithAnimation(true);
            bottomSheetDialog.setTitle("test");
            View bottomSheetView = LayoutInflater.from(getContext().getApplicationContext()).inflate(R.layout.bottom_sheet_layout, (ConstraintLayout)view.findViewById(R.id.bottomSheetContainer));
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


    @Override
    public void onDestroyView() {
        super.onDestroyView();

        // getting main activity
        // and hding action bar
//        MainActivity main = (MainActivity) getActivity();
//        if(main!=null && main.getSupportActionBar()!=null) {
//            main.getSupportActionBar().show();
//
//        }
    }
}// end MovieDetails_fragment class