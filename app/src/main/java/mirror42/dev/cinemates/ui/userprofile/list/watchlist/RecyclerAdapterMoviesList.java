package mirror42.dev.cinemates.ui.userprofile.list.watchlist;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

import mirror42.dev.cinemates.R;
import mirror42.dev.cinemates.adapter.viewholder.MovieCardViewHolder;
import mirror42.dev.cinemates.tmdbAPI.model.Movie;

public class RecyclerAdapterMoviesList extends RecyclerView.Adapter<MovieCardViewHolder> {
    private ArrayList<Movie> moviesList;
    private Context context;


    public RecyclerAdapterMoviesList(ArrayList<Movie> moviesList, Context context) {
        this.moviesList = moviesList;
        this.context = context;
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

        Glide.with(context)  //2
                .load(movie.getPosterURL()) //3
                .fallback(R.drawable.broken_image)
                .placeholder(R.drawable.placeholder_image)
                .fitCenter() //4
                .into(holder.imageViewPoster); //8
    }

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
