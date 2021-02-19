package mirror42.dev.cinemates.adapter;

import android.content.Context;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

import mirror42.dev.cinemates.R;
import mirror42.dev.cinemates.model.CustomListCreatedPost;
import mirror42.dev.cinemates.model.CustomListPost;
import mirror42.dev.cinemates.model.FavoritesPost;
import mirror42.dev.cinemates.model.FollowPost;
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
        void onCustomListCreatedPostClicked(int position);
        void onFollowPostClicked(int position);
        void onPostOwnerAreaClicked(int position);

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
            case CL:
                return CL;
            case CC:
                return CC;
            case FW:
                return FW;
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
        else if(viewType == CL) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_custom_list_post, parent, false);
            return new CustomListPostViewHolder(view);
        }
        else if(viewType == CC) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_custom_list_created_post, parent, false);
            return new CustomListCreatedPostViewHolder(view);
        }
        else if(viewType == FW) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_follow_post, parent, false);
            return new FollowPostViewHolder(view);
        }
        return new WatchlistPostViewHolder(null);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if(getItemViewType(position) == WL) {
            WatchlistPost post = (WatchlistPost) postList.get(position);

            //
            buildWatchlistPost((WatchlistPostViewHolder) holder, post);
        }
        else if(getItemViewType(position) == FV) {
            FavoritesPost post = (FavoritesPost) postList.get(position);

            //
            buildFavoritesPost((FavoritesPostViewHolder) holder, post);
        }
        else if(getItemViewType(position) == WD) {
            WatchedPost watchedPost = (WatchedPost) postList.get(position);

            //
            buildWatchedPost((WatchedPostViewHolder) holder, watchedPost);
        }
        else if(getItemViewType(position) == CL) {
            CustomListPost post = (CustomListPost) postList.get(position);

            //
            buildCustomListPost((CustomListPostViewHolder) holder, post);
        }
        else if(getItemViewType(position) == CC) {
            CustomListCreatedPost post = (CustomListCreatedPost) postList.get(position);

            //
            buildCustomListCreatedPost((CustomListCreatedPostViewHolder) holder, post);
        }
        else if(getItemViewType(position) == FW) {
            FollowPost post = (FollowPost) postList.get(position);

            //
            buildFollowListPost((FollowPostViewHolder) holder, post);
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
        clearList();
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

    private void buildWatchlistPost(WatchlistPostViewHolder holder, WatchlistPost post) {
        holder.textViewUsername.setText(post.getOwner().getUsername());
        holder.textViewPublishDate.setText(MyUtilities.convertMillisToReadableTimespan(post.getPublishDateMillis()));
        holder.textViewPostDescription.setText(post.getDescription());
        holder.buttonShowLikes.setText(String.valueOf(post.getLikesCount()));
        holder.textViewMovieTitle.setText(post.getMovieTitle());
        holder.textViewMovieOverview.setText(post.getMovieOverview());

        int commentsCount = post.getCommentsCount();
        if(commentsCount==1)
            holder.buttonShowComments.setText(commentsCount + " commento");
        else
            holder.buttonShowComments.setText(commentsCount + " commenti");



        if(post.isLikedByMe())
            holder.buttonLike.setActivated(true);
        else
            holder.buttonLike.setActivated(false);

        // TODO: set commented by me

        try {
            String posterUrl_1 = post.getMovie().getPosterURL();
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
                    .load(post.getOwner().getProfilePicturePath()) //3
                    .fallback(R.drawable.icon_user_dark_blue)
                    .placeholder(R.drawable.icon_user_dark_blue)
                    .circleCrop() //4
                    .into(holder.imageViewProfilePicture); //8
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void buildFavoritesPost(FavoritesPostViewHolder holder, FavoritesPost post) {
        holder.textViewUsername.setText(post.getOwner().getUsername());
        holder.textViewPublishDate.setText(MyUtilities.convertMillisToReadableTimespan(post.getPublishDateMillis()));
        holder.textViewPostDescription.setText(post.getDescription());
        holder.buttonShowLikes.setText(String.valueOf(post.getLikesCount()));
        holder.textViewMovieTitle.setText(post.getMovieTitle());
        holder.textViewMovieOverview.setText(post.getMovieOverview());

        int commentsCount = post.getCommentsCount();
        if(commentsCount==1)
            holder.buttonShowComments.setText(commentsCount + " commento");
        else
            holder.buttonShowComments.setText(commentsCount + " commenti");



        if(post.isLikedByMe())
            holder.buttonLike.setActivated(true);
        else
            holder.buttonLike.setActivated(false);

        // TODO: set commented by me

        try {
            String posterUrl_1 = post.getMovie().getPosterURL();
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
                    .load(post.getOwner().getProfilePicturePath()) //3
                    .fallback(R.drawable.icon_user_dark_blue)
                    .placeholder(R.drawable.icon_user_dark_blue)
                    .circleCrop() //4
                    .into(holder.imageViewProfilePicture); //8
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void buildWatchedPost(WatchedPostViewHolder holder, WatchedPost post) {
        holder.textViewUsername.setText(post.getOwner().getUsername());
        holder.textViewPublishDate.setText(MyUtilities.convertMillisToReadableTimespan(post.getPublishDateMillis()));
        holder.textViewPostDescription.setText(post.getDescription());
        holder.buttonShowLikes.setText(String.valueOf(post.getLikesCount()));
        holder.textViewMovieOverview.setText(post.getMovieOverview());

        int commentsCount = post.getCommentsCount();
        if(commentsCount==1)
            holder.buttonShowComments.setText(commentsCount + " commento");
        else
            holder.buttonShowComments.setText(commentsCount + " commenti");



        if(post.isLikedByMe())
            holder.buttonLike.setActivated(true);
        else
            holder.buttonLike.setActivated(false);

        // TODO: set commented by me

        try {
            String posterUrl_1 = post.getMovie().getPosterURL();
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
                    .load(post.getOwner().getProfilePicturePath()) //3
                    .fallback(R.drawable.icon_user_dark_blue)
                    .placeholder(R.drawable.icon_user_dark_blue)
                    .circleCrop() //4
                    .into(holder.imageViewProfilePicture); //8
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void buildCustomListPost(CustomListPostViewHolder holder, CustomListPost post) {
        holder.textViewUsername.setText(post.getOwner().getUsername());
        holder.textViewPublishDate.setText(MyUtilities.convertMillisToReadableTimespan(post.getPublishDateMillis()));
        holder.textViewPostDescription.setText(post.getDescription());
        holder.buttonShowLikes.setText(String.valueOf(post.getLikesCount()));
        holder.textViewMovieTitle.setText(post.getMovieTitle());
        holder.textViewMovieOverview.setText(post.getMovieOverview());

        int commentsCount = post.getCommentsCount();
        if(commentsCount==1)
            holder.buttonShowComments.setText(commentsCount + " commento");
        else
            holder.buttonShowComments.setText(commentsCount + " commenti");



        if(post.isLikedByMe())
            holder.buttonLike.setActivated(true);
        else
            holder.buttonLike.setActivated(false);

        // TODO: set commented by me

        try {
            String posterUrl_1 = post.getMovie().getPosterURL();
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
                    .load(post.getOwner().getProfilePicturePath()) //3
                    .fallback(R.drawable.icon_user_dark_blue)
                    .placeholder(R.drawable.icon_user_dark_blue)
                    .circleCrop() //4
                    .into(holder.imageViewProfilePicture); //8
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void buildCustomListCreatedPost(CustomListCreatedPostViewHolder holder, CustomListCreatedPost post) {
        holder.textViewUsername.setText(post.getOwner().getUsername());
        holder.textViewPublishDate.setText(MyUtilities.convertMillisToReadableTimespan(post.getPublishDateMillis()));
        holder.textViewPostDescription.setText(post.getDescription());
        holder.buttonShowLikes.setText(String.valueOf(post.getLikesCount()));

        int commentsCount = post.getCommentsCount();
        if(commentsCount==1)
            holder.buttonShowComments.setText(commentsCount + " commento");
        else
            holder.buttonShowComments.setText(commentsCount + " commenti");



        if(post.isLikedByMe())
            holder.buttonLike.setActivated(true);
        else
            holder.buttonLike.setActivated(false);

        // TODO: set commented by me

        try {
            Glide.with(context)  //2
                    .load(post.getOwner().getProfilePicturePath()) //3
                    .fallback(R.drawable.icon_user_dark_blue)
                    .placeholder(R.drawable.icon_user_dark_blue)
                    .circleCrop() //4
                    .into(holder.imageViewProfilePicture); //8
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //TODO
    private void buildFollowListPost(FollowPostViewHolder holder, FollowPost post) {
        holder.textViewUsername.setText(post.getOwner().getUsername());
        holder.textViewPublishDate.setText(MyUtilities.convertMillisToReadableTimespan(post.getPublishDateMillis()));
        holder.textViewPostDescription.setText(post.getDescription());
        holder.buttonShowLikes.setText(String.valueOf(post.getLikesCount()));

        int commentsCount = post.getCommentsCount();
        if(commentsCount==1)
            holder.buttonShowComments.setText(commentsCount + " commento");
        else
            holder.buttonShowComments.setText(commentsCount + " commenti");



        if(post.isLikedByMe())
            holder.buttonLike.setActivated(true);
        else
            holder.buttonLike.setActivated(false);

        // TODO: set commented by me

        try {
            Glide.with(context)  //2
                    .load(post.getOwner().getProfilePicturePath()) //3
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

        public TextView textViewPostDescription;
        public ImageView imageViewMoviePoster;
        public TextView textViewMovieTitle;
        public TextView textViewMovieOverview;
        public View container;

        public Button buttonComment;
        public Button buttonLike;
        public Button buttonShowComments;
        public Button buttonShowLikes;

        public View postOwnerArea;


        public WatchlistPostViewHolder(@NonNull View itemView) {
            super(itemView);
            imageViewProfilePicture = itemView.findViewById(R.id.imageView_postOwnerLayout_profilePicture);
            textViewUsername = itemView.findViewById(R.id.textView_postOwnerLayout_username);
            textViewPublishDate = itemView.findViewById(R.id.textView_postOwnerLayout_publishDate);
            postOwnerArea = itemView.findViewById(R.id.include_watchListPostLayout_postOwner);

            textViewPostDescription = itemView.findViewById(R.id.textView_contentListPost_description);
            textViewMovieTitle = itemView.findViewById(R.id.textView_contentListPost_movieTitle);
            imageViewMoviePoster = (itemView.findViewById(R.id.include_contentListPost_moviePoster)).findViewById(R.id.imageview_movieThumbnail);
            container = itemView.findViewById(R.id.include_watchListPostLayout_postContentLayout);
            textViewMovieOverview = itemView.findViewById(R.id.textView_contentListPost_movieOverview);

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
            postOwnerArea.setOnClickListener(this);
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
            else if(view.getId() == postOwnerArea.getId()) {
                listener.onPostOwnerAreaClicked(getAdapterPosition());
                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
            }
        }
    }// end WatchlistPostViewHolder class

    public class FavoritesPostViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public ImageView imageViewProfilePicture;
        public TextView textViewUsername;
        public TextView textViewPublishDate;

        public TextView textViewPostDescription;
        public ImageView imageViewMoviePoster;
        public TextView textViewMovieTitle;
        public TextView textViewMovieOverview;
        public View container;

        public Button buttonComment;
        public Button buttonLike;
        public Button buttonShowComments;
        public Button buttonShowLikes;

        public View postOwnerArea;


        public FavoritesPostViewHolder(@NonNull View itemView) {
            super(itemView);
            imageViewProfilePicture = itemView.findViewById(R.id.imageView_postOwnerLayout_profilePicture);
            textViewUsername = itemView.findViewById(R.id.textView_postOwnerLayout_username);
            textViewPublishDate = itemView.findViewById(R.id.textView_postOwnerLayout_publishDate);
            postOwnerArea = itemView.findViewById(R.id.include_favoritesPostLayout_postOwner);

            textViewPostDescription = itemView.findViewById(R.id.textView_contentListPost_description);
            textViewMovieTitle = itemView.findViewById(R.id.textView_contentListPost_movieTitle);
            imageViewMoviePoster = (itemView.findViewById(R.id.include_contentListPost_moviePoster)).findViewById(R.id.imageview_movieThumbnail);
            container = itemView.findViewById(R.id.include_favoritesPostLayout_postContent);
            textViewMovieOverview = itemView.findViewById(R.id.textView_contentListPost_movieOverview);

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
            postOwnerArea.setOnClickListener(this);

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
            else if(view.getId() == postOwnerArea.getId()) {
                listener.onPostOwnerAreaClicked(getAdapterPosition());
                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
            }
        }
    }// end FavoritesPostViewHolder class

    public class WatchedPostViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public ImageView imageViewProfilePicture;
        public TextView textViewUsername;
        public TextView textViewPublishDate;

        public TextView textViewPostDescription;
        public ImageView imageViewMoviePoster;
        public TextView textViewMovieTitle;
        public TextView textViewMovieOverview;
        public View container;

        public Button buttonComment;
        public Button buttonLike;
        public Button buttonShowComments;
        public Button buttonShowLikes;

        public View postOwnerArea;


        public WatchedPostViewHolder(@NonNull View itemView) {
            super(itemView);
            imageViewProfilePicture = itemView.findViewById(R.id.imageView_postOwnerLayout_profilePicture);
            textViewUsername = itemView.findViewById(R.id.textView_postOwnerLayout_username);
            textViewPublishDate = itemView.findViewById(R.id.textView_postOwnerLayout_publishDate);
            postOwnerArea = itemView.findViewById(R.id.include_watchedPostLayout_postOwner);

            textViewPostDescription = itemView.findViewById(R.id.textView_contentListPost_description);
            textViewMovieTitle = itemView.findViewById(R.id.textView_contentListPost_movieTitle);
            textViewMovieTitle.setVisibility(View.GONE);
            imageViewMoviePoster = (itemView.findViewById(R.id.include_contentListPost_moviePoster)).findViewById(R.id.imageview_movieThumbnail);
            container = itemView.findViewById(R.id.include_watchedPostLayout_postContent);
            textViewMovieOverview = itemView.findViewById(R.id.textView_contentListPost_movieOverview);
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
            postOwnerArea.setOnClickListener(this);
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
            else if(view.getId() == postOwnerArea.getId()) {
                listener.onPostOwnerAreaClicked(getAdapterPosition());
                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
            }
        }
    }// end WatchedPostViewHolder class

    public class CustomListCreatedPostViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public ImageView imageViewProfilePicture;
        public TextView textViewUsername;
        public TextView textViewPublishDate;

        public Button buttonComment;
        public Button buttonLike;
        public Button buttonShowComments;
        public Button buttonShowLikes;


        public TextView textViewPostDescription;
        public View container;

        public View postOwnerArea;


        public CustomListCreatedPostViewHolder(@NonNull View itemView) {
            super(itemView);
            imageViewProfilePicture = itemView.findViewById(R.id.imageView_postOwnerLayout_profilePicture);
            textViewUsername = itemView.findViewById(R.id.textView_postOwnerLayout_username);
            textViewPublishDate = itemView.findViewById(R.id.textView_postOwnerLayout_publishDate);
            postOwnerArea = itemView.findViewById(R.id.include_customListCreatedPostLayout_postOwner);

            textViewPostDescription = itemView.findViewById(R.id.textView_contentCustomListCreatedPost_description);
            container = itemView.findViewById(R.id.include_customListCreatedPostLayout_postContent);

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
            postOwnerArea.setOnClickListener(this);
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
                listener.onCustomListCreatedPostClicked(getAdapterPosition());
                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
            }
            else if(view.getId() == postOwnerArea.getId()) {
                listener.onPostOwnerAreaClicked(getAdapterPosition());
                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
            }
        }
    }// end CustomListCreatedPostViewHolder class

    public class CustomListPostViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public ImageView imageViewProfilePicture;
        public TextView textViewUsername;
        public TextView textViewPublishDate;

        public TextView textViewPostDescription;
        public ImageView imageViewMoviePoster;
        public TextView textViewMovieTitle;
        public TextView textViewMovieOverview;
        public View container;

        public Button buttonComment;
        public Button buttonLike;
        public Button buttonShowComments;
        public Button buttonShowLikes;

        public View postOwnerArea;


        public CustomListPostViewHolder(@NonNull View itemView) {
            super(itemView);
            imageViewProfilePicture = itemView.findViewById(R.id.imageView_postOwnerLayout_profilePicture);
            textViewUsername = itemView.findViewById(R.id.textView_postOwnerLayout_username);
            textViewPublishDate = itemView.findViewById(R.id.textView_postOwnerLayout_publishDate);
            postOwnerArea = itemView.findViewById(R.id.include_customListPostLayout_postOwner);

            textViewPostDescription = itemView.findViewById(R.id.textView_contentListPost_description);
            textViewMovieTitle = itemView.findViewById(R.id.textView_contentListPost_movieTitle);
            textViewMovieTitle.setVisibility(View.GONE);
            imageViewMoviePoster = (itemView.findViewById(R.id.include_contentListPost_moviePoster)).findViewById(R.id.imageview_movieThumbnail);
            container = itemView.findViewById(R.id.include_customListPostLayout_postContent);
            textViewMovieOverview = itemView.findViewById(R.id.textView_contentListPost_movieOverview);
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
            postOwnerArea.setOnClickListener(this);
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
            else if(view.getId() == postOwnerArea.getId()) {
                listener.onPostOwnerAreaClicked(getAdapterPosition());
                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
            }
        }
    }// end CustomListPostViewHolder class

    public class FollowPostViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public ImageView imageViewProfilePicture;
        public TextView textViewUsername;
        public TextView textViewPublishDate;

        public Button buttonComment;
        public Button buttonLike;
        public Button buttonShowComments;
        public Button buttonShowLikes;

        public TextView textViewPostDescription;
        public View container;

        public View postOwnerArea;


        public FollowPostViewHolder(@NonNull View itemView) {
            super(itemView);
            imageViewProfilePicture = itemView.findViewById(R.id.imageView_postOwnerLayout_profilePicture);
            textViewUsername = itemView.findViewById(R.id.textView_postOwnerLayout_username);
            textViewPublishDate = itemView.findViewById(R.id.textView_postOwnerLayout_publishDate);
            postOwnerArea = itemView.findViewById(R.id.include_followPostLayout_postOwner);

            textViewPostDescription = itemView.findViewById(R.id.textView_contentFollowPost_description);
            container = itemView.findViewById(R.id.include_followPostLayout_postContent);


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
            postOwnerArea.setOnClickListener(this);
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
                listener.onFollowPostClicked(getAdapterPosition());
                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
            }
            else if(view.getId() == postOwnerArea.getId()) {
                listener.onPostOwnerAreaClicked(getAdapterPosition());
                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
            }
        }
    }// end FollowPostViewHolder class





}// end RecyclerAdapterPost class
