package mirror42.dev.cinemates.ui.explore;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import mirror42.dev.cinemates.MyValues;
import mirror42.dev.cinemates.R;
import mirror42.dev.cinemates.adapters.RecycleAdapterExplorePage;
import mirror42.dev.cinemates.asyncTasks.DownloadUpcomings;
import mirror42.dev.cinemates.listeners.RecyclerSearchListener;
import mirror42.dev.cinemates.tmdbAPI.Movie;


public class UpcomginsFragment extends Fragment implements DownloadUpcomings.DownloadListener, RecyclerSearchListener.OnClick_RecycleSearchListener {
    private RecycleAdapterExplorePage recyclerAdapterExplorePage;
    private View view;
    private ArrayList<Movie> moviesList;
    private final String MOVIES_LIST_KEY = "MOVIE_LIST_KEY";



    //------------------------------------------------------------------------ CONSTRUCTORS

    public UpcomginsFragment() {
        // Required empty public constructor
    }





    //------------------------------------------------------------------------ LIFECYCLE METHODS

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_upcomgins, container, false);
    }


    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.view = view;

        createRecycle();

        //2
        if(savedInstanceState != null) {
            moviesList = savedInstanceState.getParcelableArrayList(MOVIES_LIST_KEY);
            recyclerAdapterExplorePage.loadNewData(moviesList);
        }
        else {
            downloadData();
        }
    }// end onViewCreated()

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(MOVIES_LIST_KEY, (ArrayList<Movie>) moviesList);
    }







    //------------------------------------------------------------------------ METHODS

    @Override
    public void onDownloadComplete(ArrayList<Movie> moviesList, MyValues.DownloadStatus status) {
        if (status == MyValues.DownloadStatus.OK) {
            this.moviesList = moviesList;
            recyclerAdapterExplorePage.loadNewData(moviesList);
        }
    }

    public void downloadData() {
        // starting async task here because of google suggestions
        DownloadUpcomings downloadUpcomings = new DownloadUpcomings(this);
        downloadUpcomings.execute(1);
    }

    public void createRecycle() {
        // defining HORIZONTAL layout manager for recycler
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);

        // defining Recycler view
        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.recyclerView_upcoming_newsPage);
        recyclerView.setLayoutManager(linearLayoutManager);

        // adding recycle listener for touch detection
        recyclerView.addOnItemTouchListener(new RecyclerSearchListener(getContext(), recyclerView, this));

        // assigning adapter to recycle
        recyclerAdapterExplorePage = new RecycleAdapterExplorePage(new ArrayList<Movie>(), getContext());
        recyclerView.setAdapter(recyclerAdapterExplorePage);
    }






    //------------------------------------------------------ RECYCLER VIEW LISTENER METHODS
    @Override
    public void onItemClick(View view, int position) {
//        try {
//            NavGraphDirections.ActionGlobalMovieDetailsFragment action = ExploreFragmentDirections.actionGlobalMovieDetailsFragment(recyclerAdapterExplorePage.getMovie(position));
//            NavHostFragment.findNavController(mirror42.dev.cinemates20.fragments.explorePage.UpcomginsFragment.this).navigate(action);
//        } catch (Exception e) {
//            e.getMessage();
//            e.printStackTrace();
//        }
    }

    @Override
    public void onItemLongClick(View view, int position) {
        Toast.makeText(getContext(), "item "+position+" long clicked", Toast.LENGTH_SHORT).show();

    }

}// end UpcomginsFragment class