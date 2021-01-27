package mirror42.dev.cinemates.ui.notification;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

import mirror42.dev.cinemates.R;
import mirror42.dev.cinemates.ui.notification.model.FollowRequestNotification;
import mirror42.dev.cinemates.ui.notification.model.PostCommentedNotification;
import mirror42.dev.cinemates.ui.notification.model.PostLikedNotification;
import mirror42.dev.cinemates.ui.notification.model.Notification;
import mirror42.dev.cinemates.ui.notification.model.Notification.NotificationType;

public class RecyclerAdapterNotifications extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
    private ArrayList<Notification> notificationsList;
    private OnNotificationClickedListener listener;
    private Context context;

    private static final int FOLLOW_REQUEST = 1;
    private static final int POST_LIKED = 3;
    private static final int POST_COMMENTED = 2;

    public interface OnNotificationClickedListener {
        void onFollowRequestNotificationClicked(int position);
        void onPostLikedNotificationClicked(int position);
        void onPostCommentedNotificationClicked(int position);
    }



    //------------------------------------------------------------------------------- CONSTRUCTORS

    public RecyclerAdapterNotifications(ArrayList<Notification> notificationsList, Context context, OnNotificationClickedListener listener) {
        this.notificationsList = notificationsList;
        this.context = context;
        this.listener = listener;
    }



    //------------------------------------------------------------------------------- METHODS

    @Override
    public int getItemViewType(int position) {
        NotificationType notificationType = notificationsList.get(position).getNotificationType();

        switch (notificationType) {
            case FOLLOW_REQUEST:
                return FOLLOW_REQUEST;
            case POST_LIKED:
                return POST_LIKED;
            case POST_COMMENTED:
                return POST_COMMENTED;
            default:
                return 0;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = null;

        if(viewType == FOLLOW_REQUEST) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_follow_request_notification_item, parent, false);
            return new FollowRequestNotificationViewHolder(view);
        }
        else if(viewType == POST_LIKED) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_post_liked_notification_item, parent, false);
            return new PostLikedNotificationViewHolder(view);
        }
        else if(viewType == POST_COMMENTED) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_post_commented_notification_item, parent, false);
            return new PostCommentedNotificationViewHolder(view);
        }
        else {
            return new FollowRequestNotificationViewHolder(null);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if(getItemViewType(position) == FOLLOW_REQUEST) {
            FollowRequestNotification followRequestNotification = (FollowRequestNotification) notificationsList.get(position);

            //
            buildFollowNotificationItem((FollowRequestNotificationViewHolder) holder, followRequestNotification);
        }
        else if(getItemViewType(position) == POST_LIKED) {
            PostLikedNotification postLikedNotification = (PostLikedNotification) notificationsList.get(position);

            //
            buildPostLikedNotificationItem((PostLikedNotificationViewHolder) holder, postLikedNotification);
        }
        else if(getItemViewType(position) == POST_COMMENTED) {
            PostCommentedNotification postCommentedNotification = (PostCommentedNotification) notificationsList.get(position);

            //
            buildPostCommentedNotificationItem((PostCommentedNotificationViewHolder) holder, postCommentedNotification);
        }
    }

    private void buildFollowNotificationItem(FollowRequestNotificationViewHolder holder, FollowRequestNotification followRequestNotification) {
        holder.textViewFullName.setText(followRequestNotification.getSender().getFullName());
        holder.textViewUsername.setText("(@" + followRequestNotification.getSender().getUsername() + ")");

        try {
            Glide.with(context)  //2
                    .load(followRequestNotification.getSender().getProfilePicturePath()) //3
                    .fallback(R.drawable.broken_image)
                    .placeholder(R.drawable.placeholder_image)
                    .circleCrop() //4
                    .into(holder.imageViewProfilePicture); //8
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void buildPostLikedNotificationItem(PostLikedNotificationViewHolder holder, PostLikedNotification postLikedNotification) {
        holder.textViewFullName.setText(postLikedNotification.getSender().getFullName());
        holder.textViewUsername.setText("(@" + postLikedNotification.getSender().getUsername() + ")");

        try {
            Glide.with(context)  //2
                    .load(postLikedNotification.getSender().getProfilePicturePath()) //3
                    .fallback(R.drawable.broken_image)
                    .placeholder(R.drawable.placeholder_image)
                    .circleCrop() //4
                    .into(holder.imageViewProfilePicture); //8
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void buildPostCommentedNotificationItem(PostCommentedNotificationViewHolder holder, PostCommentedNotification postCommentedNotification) {
        holder.textViewFullName.setText(postCommentedNotification.getSender().getFullName());
        holder.textViewUsername.setText("(@" + postCommentedNotification.getSender().getUsername() + ")");

        try {
            Glide.with(context)  //2
                    .load(postCommentedNotification.getSender().getProfilePicturePath()) //3
                    .fallback(R.drawable.broken_image)
                    .placeholder(R.drawable.placeholder_image)
                    .circleCrop() //4
                    .into(holder.imageViewProfilePicture); //8
        } catch (Exception e) {
            e.printStackTrace();
        }
    }






    @Override
    public int getItemCount() {
        return ( (notificationsList != null) && (notificationsList.size() != 0) ? notificationsList.size() : 0);
    }

    public Notification getNotification(int position) {
        return ( (notificationsList != null) && (notificationsList.size() != 0) ? notificationsList.get(position) : null);
    }

    public void loadNewData(ArrayList<Notification> newList) {
        notificationsList = newList;
        notifyDataSetChanged();
    }






    //------------------------------------------------------------------------------- VIEWHOLDERS

    class FollowRequestNotificationViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public ImageView imageViewProfilePicture;
        public TextView textViewUsername;
        public TextView textViewFullName;
        private CardView cardView;

        public FollowRequestNotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            imageViewProfilePicture = itemView.findViewById(R.id.imageView_followRequestNotificationItem_profilePicture);
            textViewUsername = itemView.findViewById(R.id.textView_followRequestNotificationItem_username);
            textViewFullName = itemView.findViewById(R.id.textView_followRequestNotificationItem_fullName);
            cardView = itemView.findViewById(R.id.cardView_followRequestNotificationItem);
            cardView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            listener.onFollowRequestNotificationClicked(getAdapterPosition());

        }
    }// end FollowRequestNotificationViewHolder class

    class PostLikedNotificationViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public ImageView imageViewProfilePicture;
        public TextView textViewUsername;
        public TextView textViewFullName;
        private CardView cardView;

        public PostLikedNotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            imageViewProfilePicture = itemView.findViewById(R.id.imageView_postLikedNotificationItem_profilePicture);
            textViewUsername = itemView.findViewById(R.id.textView_postLikedNotificationItem_username);
            textViewFullName = itemView.findViewById(R.id.textView_postLikedNotificationItem_fullName);
            cardView = itemView.findViewById(R.id.cardView_postLikedNotificationItem);
            cardView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            listener.onPostLikedNotificationClicked(getAdapterPosition());

        }
    }// end PostLikedNotificationViewHolder class

    class PostCommentedNotificationViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public ImageView imageViewProfilePicture;
        public TextView textViewUsername;
        public TextView textViewFullName;
        private CardView cardView;

        public PostCommentedNotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            imageViewProfilePicture = itemView.findViewById(R.id.imageView_postCommentedNotificationItem_profilePicture);
            textViewUsername = itemView.findViewById(R.id.textView_postCommentedNotificationItem_username);
            textViewFullName = itemView.findViewById(R.id.textView_postCommentedNotificationItem_fullName);
            cardView = itemView.findViewById(R.id.cardView_postCommentedNotificationItem);
            cardView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            listener.onPostCommentedNotificationClicked(getAdapterPosition());

        }
    }// end PostCommentedNotificationViewHolder class


}// end RecylerAdapterNotifications class
