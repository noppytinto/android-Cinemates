package mirror42.dev.cinemates.adapter.viewholder;

import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import mirror42.dev.cinemates.R;


public class ExplorePageViewHolder extends RecyclerView.ViewHolder  {
    public ImageView imageViewMovieCardPoster = null;

    public ExplorePageViewHolder(@NonNull View itemView) {
        super(itemView);
        this.imageViewMovieCardPoster = (ImageView) itemView.findViewById(R.id.imageview_movieCard_poster);
    }
}
