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
import mirror42.dev.cinemates.model.FavoritesPost;
import mirror42.dev.cinemates.model.Post;
import mirror42.dev.cinemates.model.Post.PostType;
import mirror42.dev.cinemates.model.WatchedPost;
import mirror42.dev.cinemates.model.WatchlistPost;
import mirror42.dev.cinemates.utilities.MyUtilities;

public class RecyclerAdapterPost extends RecyclerView.Adapter<RecyclerView.ViewHolder>  {
    private ArrayList<Post> postList;
    private Context context;
    private ReactionsClickAdapterListener listener;
    private int currentSelectedElementIndex = -1;

    private static final int WL = 1;
    private static final int WD = 2;
    private static final int FV = 3;
    private static final int CL = 4;
    private static final int CC = 5;
    private static final int FW = 6;

    public interface ReactionsClickAdapterListener {
        void onLikeButtonClicked(int position);
        void onShowLikesClicked(int position);
        void onCommentButtonClicked(int position);
        void onShowCommentsClicked(int position);
        void onPostContentClicked(int position);
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
            case WL:
                return WL;
            case FV:
                return FV;
            case WD:
                return WD;
            default:
                return 0;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = null;

        if(viewType == WL) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_watchlist_post, parent, false);
            return new WatchlistPostViewHolder(view);
        }
        else if(viewType == FV) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_favorites_post, parent, false);
            return new FavoritesPostViewHolder(view);
        }
        else if(viewType == WD) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_watched_post, parent, false);
            return new WatchedPostViewHolder(view);
        }

        return new WatchlistPostViewHolder(null);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if(getItemViewType(position) == WL) {
            WatchlistPost watchlistPost = (WatchlistPost) postList.get(position);

            //
            buildWatchlistPost((WatchlistPostViewHolder) holder, watchlistPost);
        }
        else if(getItemViewType(position) == FV) {
            FavoritesPost favoritesPost = (FavoritesPost) postList.get(position);

            //
            buildFavoritesPost((FavoritesPostViewHolder) holder, favoritesPost);
        }
        else if(getItemViewType(position) == WD) {
            WatchedPost watchedPost = (WatchedPost) postList.get(position);

            //
            buildWatchedPost((WatchedPostViewHolder) holder, watchedPost);
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
        if(postList!= null && postList.size()>0) {
            postList.clear();
        }
        notifyDataSetChanged();
    }




    //--------------------------------------------------------------------------------------------------

    private void buildWatchlistPost(WatchlistPostViewHolder holder, WatchlistPost watchlistPost) {
        holder.textViewUsername.setText(watchlistPost.getOwner().getUsername());
        holder.textViewPublishDate.setText(MyUtilities.convertMillisToReadableTimespan(watchlistPost.getPublishDateMillis()));
        holder.textViewPostDescription.setText(watchlistPost.getDescription());
        holder.buttonShowLikes.setText(String.valueOf(watchlistPost.getLikesCount()));
        holder.textViewMovieTitle.setText(watchlistPost.getMovieTitle());
        holder.textViewMovieOverview.setText(watchlistPost.getMovieOverview());

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
                    .into(holder.imageViewMoviePoster); //8
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

    private void buildFavoritesPost(FavoritesPostViewHolder holder, FavoritesPost favoritesPost) {
        holder.textViewUsername.setText(favoritesPost.getOwner().getUsername());
        holder.textViewPublishDate.setText(MyUtilities.convertMillisToReadableTimespan(favoritesPost.getPublishDateMillis()));
        holder.textViewPostDescription.setText(favoritesPost.getDescription());
        holder.buttonShowLikes.setText(String.valueOf(favoritesPost.getLikesCount()));
        holder.textViewMovieTitle.setText(favoritesPost.getMovieTitle());
        holder.textViewMovieOverview.setText(favoritesPost.getMovieOverview());

        int commentsCount = favoritesPost.getCommentsCount();
        if(commentsCount==1)
            holder.buttonShowComments.setText(commentsCount + " commento");
        else
            holder.buttonShowComments.setText(commentsCount + " commenti");



        if(favoritesPost.isLikedByMe())
            holder.buttonLike.setActivated(true);
        else
            holder.buttonLike.setActivated(false);

        // TODO: set commented by me

        try {
            String posterUrl_1 = favoritesPost.getMovie().getPosterURL();
            Glide.with(context)  //2
                    .load(posterUrl_1) //3
                    .fallback(R.drawable.broken_image)
                    .placeholder(R.drawable.placeholder_image)
                    .fitCenter() //4
                    .into(holder.imageViewMoviePoster); //8
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            Glide.with(context)  //2
                    .load(favoritesPost.getOwner().getProfilePicturePath()) //3
                    .fallback(R.drawable.icon_user_dark_blue)
                    .placeholder(R.drawable.icon_user_dark_blue)
                    .circleCrop() //4
                    .into(holder.imageViewProfilePicture); //8
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void buildWatchedPost(WatchedPostViewHolder holder, WatchedPost watchedPost) {
        holder.textViewUsername.setText(watchedPost.getOwner().getUsername());
        holder.textViewPublishDate.setText(MyUtilities.convertMillisToReadableTimespan(watchedPost.getPublishDateMillis()));
        holder.textViewPostDescription.setText(watchedPost.getDescription());
        holder.buttonShowLikes.setText(String.valueOf(watchedPost.getLikesCount()));
        holder.textViewMovieOverview.setText(watchedPost.getMovieOverview());

        int commentsCount = watchedPost.getCommentsCount();
        if(commentsCount==1)
            holder.buttonShowComments.setText(commentsCount + " commento");
        else
            holder.buttonShowComments.setText(commentsCount + " commenti");



        if(watchedPost.isLikedByMe())
            holder.buttonLike.setActivated(true);
        else
            holder.buttonLike.setActivated(false);

        // TODO: set commented by me

        try {
            String posterUrl_1 = watchedPost.getMovie().getPosterURL();
            Glide.with(context)  //2
                    .load(posterUrl_1) //3
                    .fallback(R.drawable.broken_image)
                    .placeholder(R.drawable.placeholder_image)
                    .fitCenter() //4
                    .into(holder.imageViewMoviePoster); //8
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            Glide.with(context)  //2
                    .load(watchedPost.getOwner().getProfilePicturePath()) //3
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
        public ImageView imageViewMoviePoster;
        public TextView textViewMovieTitle;
        public TextView textViewMovieOverview;
        public View container;

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

            textViewPostDescription = itemView.findViewById(R.id.textView_contentEssentialListPost_description);
            textViewMovieTitle = itemView.findViewById(R.id.textView_contentEssentialListPost_movieTitle);
            imageViewMoviePoster = (itemView.findViewById(R.id.include_conentEssentialListPost_moviePoster)).findViewById(R.id.imageview_movieThumbnail);
            container = itemView.findViewById(R.id.include_watchListPost_postContentLayout);
            textViewMovieOverview = itemView.findViewById(R.id.textView_contentEssentialListPost_movieOverview);

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
            container.setOnClickListener(this);
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
            else if(view.getId() == container.getId()) {
                listener.onPostContentClicked(getAdapterPosition());
                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
            }
        }
    }// end WatchlistPostViewHolder class

    public class FavoritesPostViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public ImageView imageViewProfilePicture;
        public TextView textViewUsername;
        public TextView textViewPublishDate;
        public ImageButton buttonMore;

        public TextView textViewPostDescription;
        public ImageView imageViewMoviePoster;
        public TextView textViewMovieTitle;
        public TextView textViewMovieOverview;
        public View container;

        public Button buttonComment;
        public Button buttonLike;
        public Button buttonShowComments;
        public Button buttonShowLikes;



        public FavoritesPostViewHolder(@NonNull View itemView) {
            super(itemView);
            imageViewProfilePicture = itemView.findViewById(R.id.imageView_postOwnerLayout_profilePicture);
            textViewUsername = itemView.findViewById(R.id.textView_postOwnerLayout_username);
            textViewPublishDate = itemView.findViewById(R.id.textView_postOwnerLayout_publishDate);
            buttonMore = itemView.findViewById(R.id.button_postOwnerLayout_more);

            textViewPostDescription = itemView.findViewById(R.id.textView_contentEssentialListPost_description);
            textViewMovieTitle = itemView.findViewById(R.id.textView_contentEssentialListPost_movieTitle);
            imageViewMoviePoster = (itemView.findViewById(R.id.include_conentEssentialListPost_moviePoster)).findViewById(R.id.imageview_movieThumbnail);
            container = itemView.findViewById(R.id.include_favoritesPostLayout_postContent);
            textViewMovieOverview = itemView.findViewById(R.id.textView_contentEssentialListPost_movieOverview);

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
            container.setOnClickListener(this);
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
            else if(view.getId() == container.getId()) {
                listener.onPostContentClicked(getAdapterPosition());
                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
            }
        }
    }// end FavoritesPostViewHolder class

    public class WatchedPostViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public ImageView imageViewProfilePicture;
        public TextView textViewUsername;
        public TextView textViewPublishDate;
        public ImageButton buttonMore;

        public TextView textViewPostDescription;
        public ImageView imageViewMoviePoster;
        public TextView textViewMovieTitle;
        public TextView textViewMovieOverview;
        public View container;

        public Button buttonComment;
        public Button buttonLike;
        public Button buttonShowComments;
        public Button buttonShowLikes;



        public WatchedPostViewHolder(@NonNull View itemView) {
            super(itemView);
            imageViewProfilePicture = itemView.findViewById(R.id.imageView_postOwnerLayout_profilePicture);
            textViewUsername = itemView.findViewById(R.id.textView_postOwnerLayout_username);
            textViewPublishDate = itemView.findViewById(R.id.textView_postOwnerLayout_publishDate);
            buttonMore = itemView.findViewById(R.id.button_postOwnerLayout_more);

            textViewPostDescription = itemView.findViewById(R.id.textView_contentEssentialListPost_description);
            textViewMovieTitle = itemView.findViewById(R.id.textView_contentEssentialListPost_movieTitle);
            textViewMovieTitle.setVisibility(View.GONE);
            imageViewMoviePoster = (itemView.findViewById(R.id.include_conentEssentialListPost_moviePoster)).findViewById(R.id.imageview_movieThumbnail);
            container = itemView.findViewById(R.id.include_watchedPostLayout_postContent);
            textViewMovieOverview = itemView.findViewById(R.id.textView_contentEssentialListPost_movieOverview);
            textViewMovieOverview.setMaxLines(8);

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
            container.setOnClickListener(this);
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
            else if(view.getId() == container.getId()) {
                listener.onPostContentClicked(getAdapterPosition());
                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
            }
        }
    }// end WatchedPostViewHolder class



}// end RecyclerAdapterPost class
