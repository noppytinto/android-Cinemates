package mirror42.dev.cinemates.ui.explore;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import mirror42.dev.cinemates.MyValues;
import mirror42.dev.cinemates.R;
import mirror42.dev.cinemates.adapters.RecycleAdapterExplorePage;
import mirror42.dev.cinemates.asyncTasks.DownloadLatestReleases;
import mirror42.dev.cinemates.listeners.RecyclerSearchListener;
import mirror42.dev.cinemates.tmdbAPI.Movie;

public class LatestReleasesFragment extends Fragment implements DownloadLatestReleases.DownloadListener, RecyclerSearchListener.OnClick_RecycleSearchListener {
    private final String TAG = this.getClass().getSimpleName();
    private RecycleAdapterExplorePage recycleAdapterExplorePage;
    private View view;
    private ArrayList<Movie> moviesList;
    private final String MOVIES_LIST_KEY = "MOVIE_LIST_KEY";



    //------------------------------------------------------------------------ CONSTRUCTORS

    public LatestReleasesFragment() {
        // Required empty public constructor
    }



    //------------------------------------------------------------------------ LIFECYLE METHODS

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "//------------------------------------ onCreateView() called");

        this.view = inflater.inflate(R.layout.fragment_latest_releases, container, false);
        // Inflate the layout for this fragment

        return view;
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(TAG, "//------------------------- onViewCreated() called");
        this.view = view;

        //1
        createRecycle();

        //2
        if(savedInstanceState != null) {
            moviesList = savedInstanceState.getParcelableArrayList(MOVIES_LIST_KEY);
            recycleAdapterExplorePage.loadNewData(moviesList);
        }
        else {
            downloadData();
        }

    }// end onViewCreated()

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "//--------------- onResume() called");
        Log.d(TAG, "onResume() ended");

    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.d(TAG, "onSaveInstanceState() called");

        outState.putParcelableArrayList(MOVIES_LIST_KEY, (ArrayList<Movie>) moviesList);

    }






    //------------------------------------------------------------------------ METHODS

    void createRecycle() {
        // defining HORIZONTAL layout manager for recycler
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);

        // defining Recycler view
        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.recyclerView_latestReleases_newsPage);
        recyclerView.setLayoutManager(linearLayoutManager);

        // adding recycle listener for touch detection
        recyclerView.addOnItemTouchListener(new RecyclerSearchListener(getContext(), recyclerView, this));

        // assigning adapter to recycle
        recycleAdapterExplorePage = new RecycleAdapterExplorePage(new ArrayList<Movie>(), getContext());
        recyclerView.setAdapter(recycleAdapterExplorePage);
    }

    public void downloadData() {
        Log.d(TAG, "downloadData() called");

        // starting async task here because of google suggestions
        DownloadLatestReleases downloadLatestReleases = new DownloadLatestReleases(this);
        downloadLatestReleases.execute(1); // 1 is the page 1 of the result
    }

    /**
     * when downloads(async) are completed
     * asynctask will cal back this method to deploy the result
     */
    @Override
    public void onDownloadComplete(ArrayList<Movie> moviesList, MyValues.DownloadStatus status) {
        Log.d(TAG, "onDownloadComplete() called");

        if (status == MyValues.DownloadStatus.OK) {
            this.moviesList = moviesList;
            recycleAdapterExplorePage.loadNewData(moviesList);
            Log.d(TAG, "onDownloadComplete() : download completed!");
        }
    }







    //------------------------------------------------------ RECYCLER LISTENER METHODS
    @Override
    public void onItemClick(View view, int position) {
        Log.d(TAG, "onItemClick() called");

//        try {
//            NavGraphDirections.ActionGlobalMovieDetailsFragment action = ExploreFragmentDirections.actionGlobalMovieDetailsFragment(recycleAdapterExplorePage.getMovie(position));
//            NavHostFragment.findNavController(mirror42.dev.cinemates20.fragments.explorePage.LatestReleasesFragment.this).navigate(action);
//        } catch (Exception e) {
//            e.getMessage();
//            e.printStackTrace();
//        }

        Log.d(TAG, "onItemClick() ended");

    }

    @Override
    public void onItemLongClick(View view, int position) {
//        Toast.makeText(getContext(), "item "+position+" long clicked", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "onItemLongClick() called");
        Log.d(TAG, "onItemLongClick() ended");
    }


}// end LatestReleasesFragment class