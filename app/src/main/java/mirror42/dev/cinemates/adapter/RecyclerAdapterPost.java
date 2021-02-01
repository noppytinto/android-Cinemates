package mirror42.dev.cinemates.adapter;

import android.content.Context;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

import mirror42.dev.cinemates.R;
import mirror42.dev.cinemates.model.Post;
import mirror42.dev.cinemates.model.Post.PostType;
import mirror42.dev.cinemates.model.WatchlistPost;
import mirror42.dev.cinemates.utilities.MyUtilities;

public class RecyclerAdapterPost extends RecyclerView.Adapter<RecyclerView.ViewHolder>  {
    private ArrayList<Post> postList;
    private Context context;
    private ReactionsClickAdapterListener listener;
    private int currentSelectedElementIndex = -1;

    private static final int ADD_TO_WATCHLIST = 1;
    private static final int ADD_TO_WATCHED_LIST = 2;

    public interface ReactionsClickAdapterListener {
        void onLikeButtonClicked(int position);
        void onShowLikesClicked(int position);
        void onCommentButtonClicked(int position);
        void onShowCommentsClicked(int position);
    }




    //-------------------------------------------------------------------------------------------------- CONSTRUCTORS

    public RecyclerAdapterPost(ArrayList<Post> postList, Context context, ReactionsClickAdapterListener listener) {
        this.postList = postList;
        this.context = context;
        this.listener = listener;
    }



    //-------------------------------------------------------------------------------------------------- METHODS

    @Override
    public int getItemViewType(int position) {
        PostType postType = postList.get(position).getPostType();

        switch (postType) {
            case ADD_TO_WATCHLIST:
                return ADD_TO_WATCHLIST;
            case ADD_TO_WATCHED_LIST:
                return ADD_TO_WATCHED_LIST;
            default:
                return 0;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = null;

        if(viewType == ADD_TO_WATCHLIST) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_watchlist_post, parent, false);
            return new WatchlistPostViewHolder(view);
        }
        else {
            return new WatchlistPostViewHolder(null);
        }

    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if(getItemViewType(position) == ADD_TO_WATCHLIST) {
            WatchlistPost watchlistPost = (WatchlistPost) postList.get(position);



            //
            buildWatchlistPost((WatchlistPostViewHolder) holder, watchlistPost);
        }
    }

    @Override
    public int getItemCount() {
        return ( (postList != null) && (postList.size() != 0) ? postList.size() : 0);
    }

    public Post getPost(int position) {
        return ( (postList != null) && (postList.size() != 0) ? postList.get(position) : null);
    }

    public void loadNewData(ArrayList<Post> newList) {
        postList = newList;
        notifyDataSetChanged();
    }


    public void clearList() {
        postList.clear();
        notifyDataSetChanged();
    }




    //--------------------------------------------------------------------------------------------------

    private void buildWatchlistPost(WatchlistPostViewHolder holder, WatchlistPost watchlistPost) {
        holder.textViewUsername.setText(watchlistPost.getOwner().getUsername());
        holder.textViewPublishDate.setText(MyUtilities.convertMillisToReadableTimespan(watchlistPost.getPublishDateMillis()));
        holder.textViewPostDescription.setText(watchlistPost.getDescription());
        holder.buttonShowLikes.setText(String.valueOf(watchlistPost.getLikesCount()));

        int commentsCount = watchlistPost.getCommentsCount();
        if(commentsCount==1)
            holder.buttonShowComments.setText(commentsCount + " commento");
        else
            holder.buttonShowComments.setText(commentsCount + " commenti");



        if(watchlistPost.isLikedByMe())
            holder.buttonLike.setActivated(true);
        else
            holder.buttonLike.setActivated(false);

        // TODO: set commented by me

        try {
            String posterUrl_1 = watchlistPost.getMovie().getPosterURL();
            Glide.with(context)  //2
                    .load(posterUrl_1) //3
                    .fallback(R.drawable.broken_image)
                    .placeholder(R.drawable.placeholder_image)
                    .fitCenter() //4
                    .into(holder.imageViewThumbnail_1); //8
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            Glide.with(context)  //2
                    .load(watchlistPost.getOwner().getProfilePicturePath()) //3
                    .fallback(R.drawable.icon_user_dark_blue)
                    .placeholder(R.drawable.icon_user_dark_blue)
                    .circleCrop() //4
                    .into(holder.imageViewProfilePicture); //8
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    //-------------------------------------------------------------------------------------------------- viewholders

    public class WatchlistPostViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
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
        public Button buttonShowComments;
        public Button buttonShowLikes;



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
            buttonShowComments = itemView.findViewById(R.id.button_reactionsLayout_showComments);
            buttonShowLikes = itemView.findViewById(R.id.button_reactionsLayout_showLikes);

            //listeners
            buttonLike.setOnClickListener(this);
            buttonLike.setActivated(false);
            buttonComment.setOnClickListener(this);
            buttonShowLikes.setOnClickListener(this);
            buttonShowComments.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if(view.getId() == buttonLike.getId()) {
                listener.onLikeButtonClicked(getAdapterPosition());
                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
                if(buttonLike.isActivated())
                    buttonLike.setActivated(false);
                else
                    buttonLike.setActivated(true);
            }
            else if(view.getId() == buttonComment.getId()) {
                listener.onCommentButtonClicked(getAdapterPosition());
                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
            }
            else if(view.getId() == buttonShowLikes.getId()) {
                listener.onShowLikesClicked(getAdapterPosition());
                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
            }
            else if(view.getId() == buttonShowComments.getId()) {
                listener.onShowCommentsClicked(getAdapterPosition());
                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
            }
        }
    }// end WatchlistPostViewHolder classe



}// end RecyclerAdapterPost class
