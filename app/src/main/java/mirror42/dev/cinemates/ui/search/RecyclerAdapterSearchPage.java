package mirror42.dev.cinemates.ui.search;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

import mirror42.dev.cinemates.R;
import mirror42.dev.cinemates.ui.search.model.MovieSearchResult;
import mirror42.dev.cinemates.ui.search.model.SearchResult;
import mirror42.dev.cinemates.ui.search.model.UserSearchResult;
import mirror42.dev.cinemates.ui.search.viewholders.MovieSearchResultViewHolder;
import mirror42.dev.cinemates.ui.search.viewholders.UserSearchResultViewHolder;

public class RecyclerAdapterSearchPage extends RecyclerView.Adapter<RecyclerView.ViewHolder>  {
    private ArrayList<SearchResult> searchResultList;
    private Context context;

    private static final int SEARCH_TYPE_MOVIE = 1;
    private static final int SEARCH_TYPE_USER = 2;



    //------------------------------------------------------------------------CONSTRUCTORS

    public RecyclerAdapterSearchPage(ArrayList<SearchResult> searchResultList, Context context) {
        this.searchResultList = searchResultList;
        this.context = context;
    }







    //------------------------------------------------------------------------ METHODS

    @Override
    public int getItemViewType(int position) {
        SearchResult.SearchType searchType = searchResultList.get(position).getResultType();

        switch (searchType) {
            case MOVIE:
                return SEARCH_TYPE_MOVIE;
            case USER:
                return SEARCH_TYPE_USER;
            default:
                return 0;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = null;

        if(viewType == SEARCH_TYPE_MOVIE) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_search_record_movie, parent, false);
            return new MovieSearchResultViewHolder(view);
        }
        else if(viewType == SEARCH_TYPE_USER) {
            //TODO: inflate layout
            return new UserSearchResultViewHolder(view);
        }
        else {
            return new MovieSearchResultViewHolder(null);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if(getItemViewType(position) == SEARCH_TYPE_MOVIE) {
            MovieSearchResult movieSearchResult = (MovieSearchResult) searchResultList.get(position);
            //
            buildMovieSearchResult((MovieSearchResultViewHolder) holder, movieSearchResult);
        }
        else if(getItemViewType(position) == SEARCH_TYPE_USER) {
            UserSearchResult userSearchResult = (UserSearchResult) searchResultList.get(position);
            //
            buildUserSearchResult((UserSearchResultViewHolder) holder, userSearchResult);
        }
    }


    //----------------------------------------------------- search result builders

    private void buildMovieSearchResult(MovieSearchResultViewHolder holder, MovieSearchResult searchResult) {

        Glide.with(context)  //2
                .load(searchResult.getPosterURL()) //3
                .fallback(R.drawable.broken_image)
                .placeholder(R.drawable.placeholder_image)
                .fitCenter() //4
                .into(holder.imageViewSearchRecordPoster); //8

        holder.textViewSearchRecordMovieTitle.setText(searchResult.getTitle());
        holder.textViewSearchRecordOverview.setText(searchResult.getOverview());
    }

    private void buildUserSearchResult(UserSearchResultViewHolder holder, UserSearchResult searchResult) {
        //TODO:
    }








    //------------------------------------------------------------------

    @Override
    public int getItemCount() {
        return ( (searchResultList != null) && (searchResultList.size() != 0) ? searchResultList.size() : 0);
    }

    public SearchResult getSearchResultList(int position) {
        return ( (searchResultList != null) && (searchResultList.size() != 0) ? searchResultList.get(position) : null);
    }

    public void loadNewData(ArrayList<SearchResult> newSearchResultList) {
        searchResultList = newSearchResultList;
        notifyDataSetChanged();
    }


}// end RecycleAdapterSearch class
