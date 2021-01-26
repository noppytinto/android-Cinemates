package mirror42.dev.cinemates.ui.search;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

import mirror42.dev.cinemates.R;
import mirror42.dev.cinemates.ui.search.model.MovieSearchResult;
import mirror42.dev.cinemates.ui.search.model.SearchResult;
import mirror42.dev.cinemates.ui.search.model.UserSearchResult;

public class RecyclerAdapterSearchPage extends RecyclerView.Adapter<RecyclerView.ViewHolder>  {
    private ArrayList<SearchResult> searchResultList;
    private final Context context;
    private SearchResultListener listener;

    private static final int SEARCH_TYPE_MOVIE = 1;
    private static final int SEARCH_TYPE_USER = 2;

    interface SearchResultListener {
        void onUserSearchResultClicked(int position, View v);
        void onMovieSearchResultClicked(int position, View v);
    }



    //------------------------------------------------------------------------CONSTRUCTORS

    public RecyclerAdapterSearchPage(ArrayList<SearchResult> searchResultList, Context context, SearchResultListener listener) {
        this.searchResultList = searchResultList;
        this.context = context;
        this.listener = listener;
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
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_users_list_item, parent, false);
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
        Glide.with(context)  //2
                .load(searchResult.getProfilePictureUrl()) //3
                .fallback(R.drawable.broken_image)
                .placeholder(R.drawable.placeholder_image)
                .circleCrop() //4
                .into(holder.imageViewProfilePicture); //8

        holder.textViewFullName.setText(searchResult.getFirstName() + " " + searchResult.getLastName());
        holder.textViewUsername.setText("@" + searchResult.getUsername());
    }



    //------------------------------------------------------------------

    @Override
    public int getItemCount() {
        return ( (searchResultList != null) && (searchResultList.size() != 0) ? searchResultList.size() : 0);
    }

    public SearchResult getSearchResult(int position) {
        return ( (searchResultList != null) && (searchResultList.size() != 0) ? searchResultList.get(position) : null);
    }

    public void loadNewData(ArrayList<SearchResult> newSearchResultList) {
        if(newSearchResultList==null)
            searchResultList = new ArrayList<>();
        else
            searchResultList = newSearchResultList;
        notifyDataSetChanged();
    }



    //------------------------------------------------------------ VIEWHOLDERS

    public class UserSearchResultViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private CardView cardView;
        private ImageView imageViewProfilePicture;
        private TextView textViewFullName;
        private TextView textViewUsername;



        public UserSearchResultViewHolder(@NonNull View itemView) {
            super(itemView);

            cardView = itemView.findViewById(R.id.cardView_userListItem);
            imageViewProfilePicture = itemView.findViewById(R.id.imageView_userListItem_profilePicture);
            textViewFullName = itemView.findViewById(R.id.textView_userListItem_fullName);
            textViewUsername = itemView.findViewById(R.id.textView_userListItem_username);
            cardView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            listener.onUserSearchResultClicked(getAdapterPosition(), v);
        }

    }// end UserSearchResultViewHolder class

    public class MovieSearchResultViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        public ImageView imageViewSearchRecordPoster = null;
        public TextView textViewSearchRecordMovieTitle = null;
        public TextView textViewSearchRecordOverview = null;
        private CardView cardView;


        public MovieSearchResultViewHolder(@NonNull View itemView) {
            super(itemView);

            this.imageViewSearchRecordPoster = (ImageView) itemView.findViewById(R.id.imageview_searchRecord_poster);
            this.textViewSearchRecordMovieTitle = (TextView) itemView.findViewById(R.id.textview_searchRecord_movie_title);
            this.textViewSearchRecordOverview = (TextView) itemView.findViewById(R.id.textview_searchRecord_overview);
            this.cardView = itemView.findViewById(R.id.cardview_searchRecord);
            cardView.setOnClickListener(this);

        }

        @Override
        public void onClick(View v) {
            listener.onMovieSearchResultClicked(getAdapterPosition(), v);
        }
    }// end MovieSearchResultViewHolder class

}// end RecycleAdapterSearch class
