package mirror42.dev.cinemates.ui.search.viewholders;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import mirror42.dev.cinemates.R;


public class MovieSearchResultViewHolder extends RecyclerView.ViewHolder{
    public ImageView imageViewSearchRecordPoster = null;
    public TextView textViewSearchRecordMovieTitle = null;
    public TextView textViewSearchRecordOverview = null;


    public MovieSearchResultViewHolder(@NonNull View itemView) {
        super(itemView);

        this.imageViewSearchRecordPoster = (ImageView) itemView.findViewById(R.id.imageview_searchRecord_poster);
        this.textViewSearchRecordMovieTitle = (TextView) itemView.findViewById(R.id.textview_searchRecord_movie_title);
        this.textViewSearchRecordOverview = (TextView) itemView.findViewById(R.id.textview_searchRecord_overview);
    }
}
