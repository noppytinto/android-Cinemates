package mirror42.dev.cinemates.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

import mirror42.dev.cinemates.R;
import mirror42.dev.cinemates.model.search.SearchResult;
import mirror42.dev.cinemates.model.tmdb.Movie;

public class RecyclerAdapterViewAllMovieForType extends RecyclerView.Adapter<RecyclerAdapterViewAllMovieForType.MovieHolder> {


    private ArrayList<Movie> movies;
    private Context context;


    // listener
    private RecyclerViewClickListener mListener;


    // l'interfaccia serve per il listener
    static class MovieHolder extends RecyclerView.ViewHolder implements View.OnClickListener{         // holder non crea ogni volte la view quando scrolli !!!

        TextView title =  null;
        TextView overview =  null;
        ImageView poster = null;

        // per il listener
        private RecyclerViewClickListener mListener;

        public MovieHolder(@NonNull View itemView , RecyclerViewClickListener listener) {
            super(itemView);


            this.title =(TextView) itemView.findViewById(R.id.textview_movieRecord_movie_title);
            this.overview = (TextView)  itemView.findViewById(R.id.textview_movieRecord_overview);
            this.poster = (ImageView) itemView.findViewById(R.id.imageview_movieRecord_poster);

            mListener = listener;
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            mListener.onClick(view, getAdapterPosition());
        }

    } // class holder

    public RecyclerAdapterViewAllMovieForType(ArrayList<Movie> movies, Context context ,RecyclerViewClickListener listener) {
        this.movies = movies;
        this.context = context;
        this.mListener = listener;
    }



    @NonNull
    @Override
    public MovieHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {  // crea la view stessa

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_record_movie, parent  , false);

        return new MovieHolder(view , mListener);
    }

    @Override
    public void onBindViewHolder(@NonNull MovieHolder holder, int position) { // layoutmanger chiama questo quando i nuovi dati sono pronti mette datin nelle row

        Movie movie = movies.get(position);
        holder.title.setText(movie.getTitle());  // holder è la mia riga
        holder.overview.setText(movie.getOverview());
        Glide.with(context)  //2
                .load(movie.getPosterURL()) //3
                .fallback(R.drawable.broken_image)
                .placeholder(R.drawable.placeholder_image)
                .fitCenter() //4
                .into(holder.poster); //8
    }

    @Override
    public int getItemCount() {

        int size = 0;

        if((movies != null) && (movies.size() != 0) )
            size =  movies.size();

        return size;
    }
    public Movie getMovie(int position){

        Movie movie = null;

        if((movies != null) && (movies.size() != 0) )
            movie =   movies.get(position);

        return movie;
    }


    public void  loadNewData(ArrayList<Movie> movies){   // carico dati nel  recycle

        this.movies = movies;
        notifyDataSetChanged();
    }


}

