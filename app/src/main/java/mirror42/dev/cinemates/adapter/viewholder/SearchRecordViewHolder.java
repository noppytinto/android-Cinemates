package mirror42.dev.cinemates.adapter.viewholder;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import mirror42.dev.cinemates.R;


public class SearchRecordViewHolder extends RecyclerView.ViewHolder{
    public ImageView imageViewSearchRecordPoster = null;
    public TextView textViewSearchRecordMovieTitle = null;
    public TextView textViewSearchRecordOverview = null;


    public SearchRecordViewHolder(@NonNull View itemView) {
        super(itemView);

        this.imageViewSearchRecordPoster = (ImageView) itemView.findViewById(R.id.imageview_search_record_poster);
        this.textViewSearchRecordMovieTitle = (TextView) itemView.findViewById(R.id.textview_search_record_movie_title);
        this.textViewSearchRecordOverview = (TextView) itemView.findViewById(R.id.textview_search_record_overview);
    }
}
