package mirror42.dev.cinemates.ui.userprofile.list.watchlist;

import android.content.Context;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.ArrayList;
import java.util.List;

import mirror42.dev.cinemates.NavGraphDirections;
import mirror42.dev.cinemates.R;
import mirror42.dev.cinemates.tmdbAPI.model.Movie;
import mirror42.dev.cinemates.ui.explore.ExploreFragmentDirections;
import mirror42.dev.cinemates.utilities.FirebaseAnalytics;
import mirror42.dev.cinemates.utilities.MyUtilities;

public class RecyclerAdapterMoviesList extends RecyclerView.Adapter<RecyclerAdapterMoviesList.MovieCardViewHolder> {
    private ArrayList<Movie> moviesList;
    private Context context;
    private boolean actionModeisEnabled;
    private ArrayList<Movie> selectedMovies;
    private boolean allItemsAreSelected;
    private WatchlistFragment watchlistFragment;

    private ClickAdapterListener listener;
    private SparseBooleanArray selectedItems;
    private static int currentSelectedIndex = -1;

    public interface ClickAdapterListener {
        void onItemClicked(int position);
        void onItemLongClicked(int position);
    }

    public class MovieCardViewHolder extends RecyclerView.ViewHolder implements View.OnLongClickListener {
        public ImageView imageViewPoster;
        public View viewGradientSelected;
        public ImageView imageViewIconSelected;
        public CardView cardView;

        MovieCardViewHolder(@NonNull View itemView) {
            super(itemView);
            this.imageViewPoster = itemView.findViewById(R.id.imageview_movieCard_poster);
            this.imageViewIconSelected = itemView.findViewById(R.id.imageView_movieCard_selected);
            this.viewGradientSelected = itemView.findViewById(R.id.gradient_movieCard_selected);
            this.cardView = itemView.findViewById(R.id.cardview_movieThumbnail);
            cardView.setOnLongClickListener(this);
        }

        public void bindView(Movie movie) {
            if(movie.isSelected()) {
                imageViewIconSelected.setVisibility(View.VISIBLE);
                viewGradientSelected.setVisibility(View.VISIBLE);
            }
            else {
                imageViewIconSelected.setVisibility(View.GONE);
                viewGradientSelected.setVisibility(View.GONE);
            }
        }// end bindView()

        @Override
        public boolean onLongClick(View v) {
            listener.onItemLongClicked(getAdapterPosition());
            v.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
            return true;
        }

    }// end MovieCardViewHolder class






    //---------------------------------------------------------------------------------------------------- CONSTRUCTORS

    public RecyclerAdapterMoviesList(ArrayList<Movie> moviesList,
                                     Context context,
                                     WatchlistFragment watchlistFragment,
                                     ClickAdapterListener listener

    ) {
        this.moviesList = moviesList;
        this.context = context;
        selectedMovies = new ArrayList<>();
        this.watchlistFragment = watchlistFragment;
        this.listener = listener;
        selectedItems = new SparseBooleanArray();
    }




    //---------------------------------------------------------------------------------------------------- CONSTRUCTORS

    @NonNull
    @Override
    public MovieCardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.movie_card, parent, false);
        return new MovieCardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MovieCardViewHolder holder, int position) {
        Movie movie = moviesList.get(position);

//        holder.bindView(moviesList.get(position));


        if (movie.isSelected()) {
            holder.imageViewIconSelected.setVisibility(View.VISIBLE);
            holder.viewGradientSelected.setVisibility(View.VISIBLE);
        }

        holder.itemView.setActivated(selectedItems.get(position, false));

        applyClickEvents(holder, position);



        holder.imageViewPoster.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                MyUtilities.showCenteredToast("item " + position + " clicked", context);

                if( ! actionModeisEnabled) {
                    ActionMode.Callback callback = new ActionMode.Callback() {
                        @Override
                        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                            MenuInflater menuInflater = mode.getMenuInflater();
                            menuInflater.inflate(R.menu.list_menu, menu);
                            return true;
                        }

                        @Override
                        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                            actionModeisEnabled = true;
                            clickedItemBehaviour(holder);
                            watchlistFragment.hideMainToolbar();
                            return true;
                        }

                        @Override
                        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                            int id = item.getItemId();

                            switch (id) {
                                case R.id.menuItem_listMenu_delete:
                                    // for any selected movie
                                    // delete from recycler list
                                    for (Movie x : selectedMovies) {
                                        moviesList.remove(x);
                                    }


                                    if (moviesList.size() == 0) {
                                        //if list is empty
                                        // show message
                                    }

                                    watchlistFragment.removeMoviesFromList(selectedMovies);

                                    mode.finish();
                                    break;
                                case R.id.menuItem_listMenu_selectAll:
                                    // when all items selected
                                    if (selectedMovies.size() == moviesList.size()) {
                                        allItemsAreSelected = false;
                                        selectedMovies.clear();
                                        for (Movie m: moviesList) {
                                            m.setSelected(false);
                                        }
                                    } else {
                                        allItemsAreSelected = true;
                                        selectedMovies.clear();
                                        selectedMovies.addAll(moviesList);
                                        for (Movie m: moviesList) {
                                            m.setSelected(true);
                                        }
                                    }
                                    notifyDataSetChanged();
                                    break;
                            }// switch
                            return true;
                        }

                        @Override
                        public void onDestroyActionMode(ActionMode mode) {
                            actionModeisEnabled = false;
                            allItemsAreSelected = false;
                            selectedMovies.clear();
                            for (Movie m: moviesList) {
                                m.setSelected(false);
                            }
                            notifyDataSetChanged();
//                            clickedItemBehaviour(holder);
                            watchlistFragment.showMainToolbar();
                        }
                    };// new

                    // launch action mode
                    ((AppCompatActivity) v.getContext()).startActionMode(callback);
//                    clickedItemBehaviour(holder);

                }// if
                else {
                    clickedItemBehaviour(holder);
                }

                return true;
            }// end OnLongClick()
        });

        holder.imageViewPoster.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(actionModeisEnabled) {
                    clickedItemBehaviour(holder);
                }
                else {
                    // if the action mode is disabled
                    // enable movie details navigation
                    try {
                        //
                        Movie movieSelected = getMovie(position);

                        //
                        FirebaseAnalytics firebaseAnalytics = FirebaseAnalytics.getInstance();
                        firebaseAnalytics.logSelectedMovie(movieSelected, "selected movie in List page", this, watchlistFragment.getContext());

                        //
                        NavGraphDirections.AnywhereToMovieDetailsFragment
                                action = ExploreFragmentDirections.anywhereToMovieDetailsFragment(movieSelected);
                        NavHostFragment.findNavController(watchlistFragment).navigate(action);
                    } catch (Exception e) {
                        e.getMessage();
                        e.printStackTrace();
                    }
                }
            }
        });

//        if(allItemsAreSelected) {
////            holder.imageViewIconSelected.setVisibility(View.VISIBLE);
////            holder.viewGradientSelected.setVisibility(View.VISIBLE);
//        }
//        else{
////            holder.imageViewIconSelected.setVisibility(View.GONE);
////            holder.viewGradientSelected.setVisibility(View.GONE);
//        }


//        if (isSelectAll) {
//            holder.imageViewPoster.setVisibility(View.VISIBLE);
//            holder.imageViewPoster.setBackgroundColor(Color.GRAY);
//        }
//        else {
//            holder.imageViewPoster.setVisibility(View.GONE);
//            holder.imageViewPoster.setBackgroundColor(Color.TRANSPARENT);
//        }


        Glide.with(context)  //2
                .load(movie.getPosterURL()) //3
                .fallback(R.drawable.broken_image)
                .placeholder(R.drawable.placeholder_image)
                .fitCenter() //4
                .into(holder.imageViewPoster); //8


    }// end onBindViewHolder()

    private void applyClickEvents(MovieCardViewHolder holder, final int position) {
        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onItemClicked(position);
            }
        });

        holder.cardView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                listener.onItemLongClicked(position);
                view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
                return true;
            }
        });
    }

    public void toggleSelection(int pos) {
        currentSelectedIndex = pos;
        if (selectedItems.get(pos, false)) {
            selectedItems.delete(pos);
        } else {
            selectedItems.put(pos, true);
        }
        notifyItemChanged(pos);
    }

    public void clearSelections() {
        selectedItems.clear();
        notifyDataSetChanged();
    }

    public int getSelectedItemCount() {
        return selectedItems.size();
    }

    public List getSelectedItems() {
        List items =
                new ArrayList(selectedItems.size());
        for (int i = 0; i < selectedItems.size(); i++) {
            items.add(selectedItems.keyAt(i));
        }
        return items;
    }

    public void removeData(int position) {
        moviesList.remove(position);
        resetCurrentIndex();
    }

    public void updateData(int position) {
        moviesList.get(position).setSelected(true);
        resetCurrentIndex();
    }

    private void resetCurrentIndex() {
        currentSelectedIndex = -1;
    }


    public void selectAll() {
        for (int i = 0; i < getItemCount(); i++)
            selectedItems.put(i, true);
        notifyDataSetChanged();
    }








    public boolean getActionModeStatus() {
        return actionModeisEnabled;
    }

    private void clickedItemBehaviour(MovieCardViewHolder holder) {
        int position = holder.getAdapterPosition();

        if(position>=0) {
            Movie m = moviesList.get(position);
            // if the item was not clicked
            // then show selected and add to slected list
            if(holder.imageViewIconSelected.getVisibility() == View.GONE) {
                holder.imageViewIconSelected.setVisibility(View.VISIBLE);
                holder.viewGradientSelected.setVisibility(View.VISIBLE);
                moviesList.get(position).setSelected(true);
                selectedMovies.add(m);
            }
            else {
                // otw, hide selected and add remove from slected list
                holder.imageViewIconSelected.setVisibility(View.GONE);
                holder.viewGradientSelected.setVisibility(View.GONE);
                moviesList.get(position).setSelected(false);
                selectedMovies.remove(m);
            }
        }
    }// end clickedItemBehaviour()



    @Override
    public int getItemCount() {
        return ( (moviesList != null) && (moviesList.size() != 0) ? moviesList.size() : 0);
    }

    public Movie getMovie(int position) {
        return ( (moviesList != null) && (moviesList.size() != 0) ? moviesList.get(position) : null);
    }

    public void loadNewData(ArrayList<Movie> newList) {
        moviesList = newList;
        notifyDataSetChanged();
    }

    public void releaseFragment() {
        watchlistFragment = null;
    }


//    @Override
//    public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
//        super.onDetachedFromRecyclerView(recyclerView);
//        watchlistFragment = null;
//    }
}// end RecyclerAdapterWatchlist class
