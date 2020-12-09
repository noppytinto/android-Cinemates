package mirror42.dev.cinemates.ui.search;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import mirror42.dev.cinemates.MyValues;
import mirror42.dev.cinemates.R;
import mirror42.dev.cinemates.adapters.RecycleAdapterSearchPage;
import mirror42.dev.cinemates.asyncTasks.DownloadMoviesList;
import mirror42.dev.cinemates.listeners.RecyclerSearchListener;
import mirror42.dev.cinemates.tmdbAPI.Movie;

//import mirror42.dev.cinemates.NavGraphDirections;

public class SearchFragment extends Fragment implements DownloadMoviesList.DownloadListener, View.OnClickListener, RecyclerSearchListener.OnClick_RecycleSearchListener {
    private final String TAG = this.getClass().getSimpleName();
    private ArrayList<Movie> moviesList;
    private Button button_search;
    private EditText editText_search;
    private String query;
    private RecycleAdapterSearchPage recycleAdapterSearchPage;
    private View view;
    private final String MOVIES_LIST_KEY = "MOVIE_LIST_KEY";
    private final String QUERY_KEY = "QUERY_KEY";
    private ArrayList<Movie> oldMoviesList;
    private String oldQuery;
    private Bundle savedState;


    //------------------------------------------------------------------------ COSNTRUCTOR

    public SearchFragment() {
        // Required empty public constructor
    }




    //------------------------------------------------------------------------ LIFECYCLE METHODS

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_search, container, false);
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(TAG, "onViewCreated() called");
        this.view = view;
        //
        String query = null;
        editText_search = view.findViewById(R.id.edittext_search_fragment);

        //
        button_search = view.findViewById(R.id.button_search_fragment);
        button_search.setOnClickListener(this);


        //1
        createRecycle();

        //restoring state if configuration changes
        restoreStateIfRotated(savedInstanceState);
        restoreStateIfReplaced();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.d(TAG, "onSaveInstanceState() called");

        outState.putString(QUERY_KEY, query);
        outState.putParcelableArrayList(MOVIES_LIST_KEY, moviesList);
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause() called");
    }

    @Override
    public void onStop() {
        super.onStop();
        savedState = new Bundle();
        savedState.putString(QUERY_KEY, query);
        savedState.putParcelableArrayList(MOVIES_LIST_KEY, moviesList);
        Log.d(TAG, "onStop() called");

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d(TAG, "onDestroyView() called");

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy() called");

    }

    @Override
    public void onDetach() {
        super.onDetach();
        Log.d(TAG, "onDetach() called");

    }

    private void restoreStateIfRotated(Bundle savedInstanceState) {
        if(savedInstanceState != null) {
            query = savedInstanceState.getString(QUERY_KEY);
            moviesList = savedInstanceState.getParcelableArrayList(MOVIES_LIST_KEY);

            //
            editText_search.setText(query);
            recycleAdapterSearchPage.loadNewData(moviesList);
        }
    }

    private void restoreStateIfReplaced() {
        if(savedState!=null) {
            try {
                query = savedState.getString(QUERY_KEY);
                moviesList = savedState.getParcelableArrayList(MOVIES_LIST_KEY);

                //
                editText_search.setText(query);
                recycleAdapterSearchPage.loadNewData(moviesList);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    //------------------------------------------------------------------------ METHODS

    @Override
    public void onDownloadComplete(ArrayList<Movie> moviesList, MyValues.DownloadStatus status) {
        if (status == MyValues.DownloadStatus.OK) {
            Toast.makeText(getContext(), "Search completed! :D", Toast.LENGTH_LONG).show();
            this.moviesList = moviesList;
            recycleAdapterSearchPage.loadNewData(moviesList);
        } else {
            Toast.makeText(getContext(), "No matches! D:", Toast.LENGTH_SHORT).show();
        }
    }

    private void downloadData() {
        // starting async task here because of google suggestions
        DownloadMoviesList downloadMoviesList = new DownloadMoviesList(this);
        downloadMoviesList.execute(query);
    }

    /*
                NavHostFragment.findNavController(FirstFragment.this)
                    .navigate(R.id.action_FirstFragment_to_SecondFragment2);
 */
    @Override
    public void onClick(View v) {
        Animation buttonAnim = AnimationUtils.loadAnimation(getContext(), R.anim.push_button_animation);
        button_search.startAnimation(buttonAnim);
        query = editText_search.getText().toString();
        downloadData();
    }




    //--------------------------------- RECYCLER METHODS

    private void createRecycle() {
        // defining Recycler view
        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.recyclerview_search_fragment);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // adding recycle listener for touch detection
        recyclerView.addOnItemTouchListener(new RecyclerSearchListener(getContext(), recyclerView, this));

        // assigning adapter to recycle
        recycleAdapterSearchPage = new RecycleAdapterSearchPage(new ArrayList<Movie>(), getContext());
        recyclerView.setAdapter(recycleAdapterSearchPage);
    }


    @Override
    public void onItemClick(View view, int position) {
        try {
//            NavGraphDirections.ActionGlobalMovieDetailsFragment action = SearchFragmentDirections.actionGlobalMovieDetailsFragment(recycleAdapterSearchPage.getMoviesList(position));
//            NavHostFragment.findNavController(mirror42.dev.cinemates20.fragments.SearchFragment.this).navigate(action);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onItemLongClick(View view, int position) {
        Toast.makeText(getContext(), "item "+position+" long clicked", Toast.LENGTH_SHORT).show();

    }


}// end SearchFragment class