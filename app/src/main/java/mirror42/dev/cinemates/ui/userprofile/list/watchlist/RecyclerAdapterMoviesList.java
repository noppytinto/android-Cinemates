package mirror42.dev.cinemates.ui.userprofile.list.watchlist;

import android.content.Context;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toolbar;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.ArrayList;
import mirror42.dev.cinemates.NavGraphDirections;
import mirror42.dev.cinemates.R;
import mirror42.dev.cinemates.adapter.viewholder.MovieCardViewHolder;
import mirror42.dev.cinemates.tmdbAPI.model.Movie;
import mirror42.dev.cinemates.ui.explore.ExploreFragmentDirections;
import mirror42.dev.cinemates.utilities.FirebaseAnalytics;
import mirror42.dev.cinemates.utilities.MyUtilities;

public class RecyclerAdapterMoviesList extends RecyclerView.Adapter<MovieCardViewHolder> {
    private ArrayList<Movie> moviesList;
    private Context context;
    private boolean actionModeisEnabled;
    private ArrayList<Movie> selectedMovies;
    private boolean allItemsAreSelected;
    private WatchlistFragment watchlistFragment;


    public RecyclerAdapterMoviesList(ArrayList<Movie> moviesList, Context context, WatchlistFragment watchlistFragment) {
        this.moviesList = moviesList;
        this.context = context;
        selectedMovies = new ArrayList<>();
        this.watchlistFragment = watchlistFragment;
    }



    @NonNull
    @Override
    public MovieCardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.movie_card, parent, false);
        return new MovieCardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MovieCardViewHolder holder, int position) {
        Movie movie = moviesList.get(position);

        holder.bindView(moviesList.get(position));


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
                        Movie movieSelected = getMoviesList(position);

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

    public Movie getMoviesList(int position) {
        return ( (moviesList != null) && (moviesList.size() != 0) ? moviesList.get(position) : null);
    }

    public void loadNewData(ArrayList<Movie> newList) {
        moviesList = newList;
        notifyDataSetChanged();
    }





}// end RecyclerAdapterWatchlist class
