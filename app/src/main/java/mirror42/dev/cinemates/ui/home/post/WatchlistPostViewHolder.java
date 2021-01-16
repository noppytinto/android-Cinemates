package mirror42.dev.cinemates.ui.home.post;

import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import mirror42.dev.cinemates.R;

public class WatchlistPostViewHolder extends RecyclerView.ViewHolder {
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
        imageViewProfilePicture = itemView.findViewById(R.id.imageView_postOwnerFragment_profilePicture);
        textViewUsername = itemView.findViewById(R.id.textView_postOwnerFragment_username);
        textViewPublishDate = itemView.findViewById(R.id.textView_postOwnerFragment_publishDate);
        buttonMore = itemView.findViewById(R.id.button_postOwnerFragment_more);

        textViewPostDescription = itemView.findViewById(R.id.textView_contentPost_description);
        imageViewThumbnail_1 = (itemView.findViewById(R.id.include1_watchlistPost)).findViewById(R.id.imageview_movieCard_poster_s);
        imageViewThumbnail_2 = (itemView.findViewById(R.id.include2_watchlistPost)).findViewById(R.id.imageview_movieCard_poster_s);
        imageViewThumbnail_3 = (itemView.findViewById(R.id.include3_watchlistPost)).findViewById(R.id.imageview_movieCard_poster_s);

        buttonComment = itemView.findViewById(R.id.button_reactionsFragment_comment);
        buttonLike = itemView.findViewById(R.id.button_reactionsFragment_like);
    }




}// end WatchlistPostViewHolder classe
