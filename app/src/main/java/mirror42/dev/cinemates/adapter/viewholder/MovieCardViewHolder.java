package mirror42.dev.cinemates.adapter.viewholder;

import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import mirror42.dev.cinemates.R;
import mirror42.dev.cinemates.tmdbAPI.model.Movie;


public class MovieCardViewHolder extends RecyclerView.ViewHolder  {
    public ImageView imageViewPoster;
    public View viewGradientSelected;
    public ImageView imageViewIconSelected;
    public boolean isSelected;


    public MovieCardViewHolder(@NonNull View itemView) {
        super(itemView);
        this.imageViewPoster = itemView.findViewById(R.id.imageview_movieCard_poster);
        this.imageViewIconSelected = itemView.findViewById(R.id.imageView_movieCard_selected);
        this.viewGradientSelected = itemView.findViewById(R.id.gradient_movieCard_selected);
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





}// end MovieCardViewHolder class
