package mirror42.dev.cinemates.adapter.viewholder;

import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import mirror42.dev.cinemates.R;


public class MovieCardViewHolder extends RecyclerView.ViewHolder  {
    public ImageView imageViewPoster;

    public MovieCardViewHolder(@NonNull View itemView) {
        super(itemView);
        this.imageViewPoster = itemView.findViewById(R.id.imageview_movieCard_poster);
    }
}
