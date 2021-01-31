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
import mirror42.dev.cinemates.ui.search.model.CastSearchResult;
import mirror42.dev.cinemates.ui.search.model.MovieSearchResult;
import mirror42.dev.cinemates.ui.search.model.SearchResult;
import mirror42.dev.cinemates.ui.search.model.UserSearchResult;

public class RecyclerAdapterSearchPage extends RecyclerView.Adapter<RecyclerView.ViewHolder>  {
    private ArrayList<SearchResult> searchResultList;
    private final Context context;
    private SearchResultListener listener;

    private static final int SEARCH_TYPE_MOVIE = 1;
    private static final int SEARCH_TYPE_USER = 2;
    private static final int SEARCH_TYPE_CAST = 3;

    interface SearchResultListener {
        void onUserSearchResultClicked(int position, View v);
        void onMovieSearchResultClicked(int position, View v);
        void onCastSearchResultClicked(int position, View v);
    }



    //------------------------------------------------------------------------ CONSTRUCTORS

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
            case CAST:
                return SEARCH_TYPE_CAST;
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
        else if(viewType == SEARCH_TYPE_CAST) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_search_record_cast, parent, false);
            return new CastSearchResultViewHolder(view);
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
            MovieSearchResult searchResult = (MovieSearchResult) searchResultList.get(position);
            //
            buildMovieSearchResult((MovieSearchResultViewHolder) holder, searchResult);
        }
        else if(getItemViewType(position) == SEARCH_TYPE_USER) {
            UserSearchResult searchResult = (UserSearchResult) searchResultList.get(position);
            //
            buildUserSearchResult((UserSearchResultViewHolder) holder, searchResult);
        }
        else if(getItemViewType(position) == SEARCH_TYPE_CAST) {
            CastSearchResult searchResult = (CastSearchResult) searchResultList.get(position);
            //
            buildActorSearchResult((CastSearchResultViewHolder) holder, searchResult);
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

    private void buildActorSearchResult(CastSearchResultViewHolder holder, CastSearchResult searchResult) {
        Glide.with(context)  //2
                .load(searchResult.getProfilePictureURL()) //3
                .fallback(R.drawable.icon_user_dark_blue)
                .placeholder(R.drawable.icon_user_dark_blue)
                .fitCenter() //4
                .into(holder.imageViewProfilePicture); //8

        holder.textViewFullName.setText(searchResult.getFullName());
        holder.textViewDepartment.setText(translate(searchResult.getDepartment()));
        holder.textViewKnownFor.setText(searchResult.getKnwonForAsString());
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


    public String translate(String original) {
        String translated = "";

        if(original!=null || !original.isEmpty()) {
            translated = original.toLowerCase();

            switch (translated) {
                case "acting":
                    translated = "(attore)";
                    break;
                case "directing":
                    translated = "(regista)";
                    break;
                case "writing":
                    translated = "(scrittore)";
                    break;
                case "sound":
                    translated = "(compositore)";
                    break;
                case "crew":
                    translated = "(membro crew)";
                    break;
                case "production":
                    translated = "(produttore)";
                    break;
                case "art":
                    translated = "(artista)";
                    break;
                case "costume & make-up":
                    translated = "(costumi & make-up)";
                    break;
                case "lighting":
                    translated = "(luci)";
                    break;
                case "visual effects":
                    translated = "(effetti speciali)";
                    break;
                default:
                    translated = "(" + translated + ")";
            }
        }

        return translated;
    }

    //------------------------------------------------------------------ VIEWHOLDERS

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
        public ImageView imageViewSearchRecordPoster;
        public TextView textViewSearchRecordMovieTitle;
        public TextView textViewSearchRecordOverview;
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

    public class CastSearchResultViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        public ImageView imageViewProfilePicture;
        public TextView textViewFullName;
        public TextView textViewDepartment;
        public TextView textViewKnownFor;
        private CardView cardView;


        public CastSearchResultViewHolder(@NonNull View itemView) {
            super(itemView);
            this.imageViewProfilePicture = itemView.findViewById(R.id.imageview_searchRecordActor_profilePicture);
            this.textViewFullName =  itemView.findViewById(R.id.textView_searchRecordActor_name);
            this.textViewDepartment =  itemView.findViewById(R.id.textView_searchRecordActor_department);
            this.textViewKnownFor =  itemView.findViewById(R.id.textView_searchRecordActor_knownFor);
            this.cardView = itemView.findViewById(R.id.cardview_searchRecordActor);
            cardView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            listener.onCastSearchResultClicked(getAdapterPosition(), v);
        }

    }// end CastSearchResultViewHolder class

}// end RecycleAdapterSearch class
