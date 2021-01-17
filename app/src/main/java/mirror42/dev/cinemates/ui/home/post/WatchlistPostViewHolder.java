package mirror42.dev.cinemates.ui.home.post;

import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import mirror42.dev.cinemates.R;

public class WatchlistPostViewHolder extends RecyclerView.ViewHolder  {
    public ImageView imageViewProfilePicture;
    public TextView textViewUsername;
    public TextView textViewPublishDate;
    public ImageButton buttonMore;

    public TextView textViewPostDescription;
    public ImageView imageViewThumbnail_1;
    public ImageView imageViewThumbnail_2;
    public ImageView imageViewThumbnail_3;

    public Button buttonComment;
    public Button buttonLike;



    public WatchlistPostViewHolder(@NonNull View itemView) {
        super(itemView);
        imageViewProfilePicture = itemView.findViewById(R.id.imageView_postOwnerLayout_profilePicture);
        textViewUsername = itemView.findViewById(R.id.textView_postOwnerLayout_username);
        textViewPublishDate = itemView.findViewById(R.id.textView_postOwnerLayout_publishDate);
        buttonMore = itemView.findViewById(R.id.button_postOwnerLayout_more);

        textViewPostDescription = itemView.findViewById(R.id.textView_contentWatchlistPost_description);
        imageViewThumbnail_1 = (itemView.findViewById(R.id.include_conentWatchlistPost_thumbnail_1)).findViewById(R.id.imageview_movieThumbnail);
        imageViewThumbnail_2 = (itemView.findViewById(R.id.include_contentWatchlistPost_thumbnail_2)).findViewById(R.id.imageview_movieThumbnail);
        imageViewThumbnail_3 = (itemView.findViewById(R.id.include_contentWatchlistPost_thumbnail_3)).findViewById(R.id.imageview_movieThumbnail);

        buttonComment = itemView.findViewById(R.id.button_reactionsLayout_comment);
        buttonLike = itemView.findViewById(R.id.button_reactionsLayout_like);
    }








}// end WatchlistPostViewHolder classe
