package mirror42.dev.cinemates.ui.explore.latest;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

import mirror42.dev.cinemates.R;
import mirror42.dev.cinemates.tmdbAPI.model.Movie;

public class RecyclerAdapterExploreLatest extends RecyclerView.Adapter<RecyclerAdapterExploreLatest.MovieCardViewHolder>  {
    private ArrayList<Movie> moviesList;
    private Context context;

    class MovieCardViewHolder extends RecyclerView.ViewHolder  {
        public ImageView imageViewPoster;
        public View viewGradientSelected;
        public ImageView imageViewIconSelected;


        public MovieCardViewHolder(@NonNull View itemView) {
            super(itemView);
            this.imageViewPoster = itemView.findViewById(R.id.imageview_movieCard_poster);
            this.imageViewIconSelected = itemView.findViewById(R.id.imageView_movieCard_selected);
            this.viewGradientSelected = itemView.findViewById(R.id.gradient_movieCard_selected);
        }

    }// end MovieCardViewHolder class



    //------------------------------------------------------------------------CONSTRUCTORS

    public RecyclerAdapterExploreLatest(ArrayList<Movie> moviesList, Context context) {
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
                .into(holder.imageViewPoster);
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


}// end RecyclerAdapterExploreLatest class
