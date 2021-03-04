package mirror42.dev.cinemates.adapter;

import android.content.Context;
import android.util.SparseBooleanArray;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

import mirror42.dev.cinemates.R;
import mirror42.dev.cinemates.model.tmdb.Movie;

public class RecyclerAdapterMoviesList extends RecyclerView.Adapter<RecyclerAdapterMoviesList.MovieCardViewHolder> {
    private ArrayList<Movie> moviesList;
    private Context context;
    private ArrayList<Movie> selectedMovies;
    private ClickAdapterListener listener;
    private SparseBooleanArray selectedItems;
    private static int currentSelectedIndex = -1;

    public interface ClickAdapterListener {
        void onItemClicked(int position);
        void onItemLongClicked(int position);
    }



    //---------------------------------------------------------------------------------------------------- CONSTRUCTORS

    public RecyclerAdapterMoviesList(ArrayList<Movie> moviesList,
                                     Context context,
                                     ClickAdapterListener listener) {
        this.moviesList = moviesList;
        this.context = context;
        this.listener = listener;
        this.selectedMovies = new ArrayList<>();
        this.selectedItems = new SparseBooleanArray();
    }




    //---------------------------------------------------------------------------------------------------- CONSTRUCTORS

    @NonNull
    @Override
    public MovieCardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.movie_card, parent, false);
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

        holder.setActivated(selectedItems.get(position, false));
        applyClickEvents(holder, position);

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
        Movie currentMovieSelected = getMovie(pos);
        if (selectedItems.get(pos, false)) {
            selectedMovies.remove(currentMovieSelected);
            selectedItems.delete(pos);
        } else {
            selectedMovies.add(currentMovieSelected);
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

    public ArrayList<Integer> getSelectedItems() {
        ArrayList<Integer> items = new ArrayList<>(selectedItems.size());
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
        for (int i = 0; i < getItemCount(); i++) {
            Movie currentMovieSelected = getMovie(i);
            selectedItems.put(i, true);
            selectedMovies.add(currentMovieSelected);
        }
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return ( (moviesList != null) && (moviesList.size() != 0) ? moviesList.size() : 0);
    }

    public Movie getMovie(int position) {
        return ( (moviesList != null) && (moviesList.size() != 0) ? moviesList.get(position) : null);
    }

    public ArrayList<Movie> getcurrentSelectedMovies() {
        return selectedMovies;
    }

    public void loadNewData(ArrayList<Movie> newList) {
        moviesList = newList;
        notifyDataSetChanged();
    }

    public boolean listIsEmpty() {
        int itemCount = getItemCount();

        if(itemCount==0)
            return true;
        else
            return false;
    }



    //---------------------------------------------------------------------- VIEWHOLDERS
    class MovieCardViewHolder extends RecyclerView.ViewHolder implements View.OnLongClickListener {
        public ImageView imageViewPoster;
        public View viewGradientSelected;
        public ImageView imageViewIconSelected;
        public CardView cardView;

        //--------------------------------------------- CONSTRUCTORS
        MovieCardViewHolder(@NonNull View itemView) {
            super(itemView);
            this.imageViewPoster = itemView.findViewById(R.id.imageview_movieCard_poster);
            this.imageViewIconSelected = itemView.findViewById(R.id.imageView_movieCard_selected);
            this.viewGradientSelected = itemView.findViewById(R.id.gradient_movieCard_selected);
            this.cardView = itemView.findViewById(R.id.cardview_movieCard);
            cardView.setOnLongClickListener(this);
        }

        //--------------------------------------------- METHODS
        @Override
        public boolean onLongClick(View v) {
            listener.onItemLongClicked(getAdapterPosition());
            v.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
            return true;
        }

        public void setActivated(boolean activate) {
            if(activate) {
                imageViewIconSelected.setVisibility(View.VISIBLE);
                viewGradientSelected.setVisibility(View.VISIBLE);
            }
            else {
                imageViewIconSelected.setVisibility(View.GONE);
                viewGradientSelected.setVisibility(View.GONE);
            }
        }

    }// end MovieCardViewHolder class
}// end RecyclerAdapterWatchlist class
