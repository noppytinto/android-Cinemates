package mirror42.dev.cinemates.adapter;

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
import mirror42.dev.cinemates.model.notification.FollowRequestNotification;
import mirror42.dev.cinemates.model.notification.ListRecommendedNotification;
import mirror42.dev.cinemates.model.notification.Notification;
import mirror42.dev.cinemates.model.notification.Notification.NotificationType;
import mirror42.dev.cinemates.model.notification.PostCommentedNotification;
import mirror42.dev.cinemates.model.notification.PostLikedNotification;
import mirror42.dev.cinemates.model.notification.SubscribedToListNotification;
import mirror42.dev.cinemates.utilities.MyUtilities;

public class RecyclerAdapterNotifications extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
    private ArrayList<Notification> notificationsList;
    private OnNotificationClickedListener listener;
    private Context context;

    private static final int FR = 1;
    private static final int PC = 2;
    private static final int PL = 3;
    private static final int CR = 4;
    private static final int CS = 5;

    public interface OnNotificationClickedListener {
        void onFollowRequestNotificationClicked(int position);
        void onPostLikedNotificationClicked(int position);
        void onPostCommentedNotificationClicked(int position);
        void onListRecommendedNotificationClicked(int position);
        void onSubscribedToListNotificationClicked(int position);

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
            case FR:
                return FR;
            case PL:
                return PL;
            case PC:
                return PC;
            case CR:
                return CR;
            case CS:
                return CS;
            default:
                return 0;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = null;

        if(viewType == FR) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_follow_request_notification_item, parent, false);
            return new FollowRequestNotificationViewHolder(view);
        }
        else if(viewType == PL) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_post_liked_notification_item, parent, false);
            return new PostLikedNotificationViewHolder(view);
        }
        else if(viewType == PC) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_post_commented_notification_item, parent, false);
            return new PostCommentedNotificationViewHolder(view);
        }
        else if(viewType == CR) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_list_recommendation_notification, parent, false);
            return new ListRecommendationNotificationViewHolder(view);
        }
        else if(viewType == CS) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_subscribed_to_list_notification, parent, false);
            return new SubscribedToListNotificationViewHolder(view);
        }

        else {
            return new FollowRequestNotificationViewHolder(null);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if(getItemViewType(position) == FR) {
            FollowRequestNotification followRequestNotification = (FollowRequestNotification) notificationsList.get(position);

            //
            buildFollowNotificationItem((FollowRequestNotificationViewHolder) holder, followRequestNotification);
        }
        else if(getItemViewType(position) == PL) {
            PostLikedNotification postLikedNotification = (PostLikedNotification) notificationsList.get(position);

            //
            buildPostLikedNotificationItem((PostLikedNotificationViewHolder) holder, postLikedNotification);
        }
        else if(getItemViewType(position) == PC) {
            PostCommentedNotification postCommentedNotification = (PostCommentedNotification) notificationsList.get(position);

            //
            buildPostCommentedNotificationItem((PostCommentedNotificationViewHolder) holder, postCommentedNotification);
        }
        else if(getItemViewType(position) == CR) {
            ListRecommendedNotification listRecommendedNotification = (ListRecommendedNotification) notificationsList.get(position);

            //
            buildListRecommendedNotificationItem((ListRecommendationNotificationViewHolder) holder, listRecommendedNotification);
        }
        else if(getItemViewType(position) == CS) {
            SubscribedToListNotification subscribedToListNotification = (SubscribedToListNotification) notificationsList.get(position);

            //
            buildSubscribeToListNotificationItem((SubscribedToListNotificationViewHolder) holder, subscribedToListNotification);
        }
    }

    private void buildFollowNotificationItem(FollowRequestNotificationViewHolder holder, FollowRequestNotification notification) {
        holder.textViewFullName.setText(notification.getSender().getFullName());
        holder.textViewPublishDate.setText(MyUtilities.convertMillisToReadableTimespan(notification.getDateInMillis()));

        try {
            Glide.with(context)  //2
                    .load(notification.getSender().getProfilePicturePath()) //3
                    .fallback(R.drawable.icon_user_dark_blue)
                    .placeholder(R.drawable.icon_user_dark_blue)
                    .circleCrop() //4
                    .into(holder.imageViewProfilePicture); //8
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void buildPostLikedNotificationItem(PostLikedNotificationViewHolder holder, PostLikedNotification notification) {
        holder.textViewFullName.setText(notification.getSender().getFullName());
        holder.textViewPublishDate.setText(MyUtilities.convertMillisToReadableTimespan(notification.getDateInMillis()));

        try {
            Glide.with(context)  //2
                    .load(notification.getSender().getProfilePicturePath()) //3
                    .fallback(R.drawable.icon_user_dark_blue)
                    .placeholder(R.drawable.icon_user_dark_blue)
                    .circleCrop() //4
                    .into(holder.imageViewProfilePicture); //8
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void buildPostCommentedNotificationItem(PostCommentedNotificationViewHolder holder, PostCommentedNotification notification) {
        holder.textViewFullName.setText(notification.getSender().getFullName());
        holder.textViewPublishDate.setText(MyUtilities.convertMillisToReadableTimespan(notification.getDateInMillis()));

        try {
            Glide.with(context)  //2
                    .load(notification.getSender().getProfilePicturePath()) //3
                    .fallback(R.drawable.icon_user_dark_blue)
                    .placeholder(R.drawable.icon_user_dark_blue)
                    .circleCrop() //4
                    .into(holder.imageViewProfilePicture); //8
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void buildListRecommendedNotificationItem(ListRecommendationNotificationViewHolder holder, ListRecommendedNotification notification) {
        holder.textViewFullName.setText(notification.getSender().getFullName());
        holder.textViewPublishDate.setText(MyUtilities.convertMillisToReadableTimespan(notification.getDateInMillis()));

        try {
            Glide.with(context)  //2
                    .load(notification.getSender().getProfilePicturePath()) //3
                    .fallback(R.drawable.icon_user_dark_blue)
                    .placeholder(R.drawable.icon_user_dark_blue)
                    .circleCrop() //4
                    .into(holder.imageViewProfilePicture); //8
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void buildSubscribeToListNotificationItem(SubscribedToListNotificationViewHolder holder, SubscribedToListNotification notification) {
        holder.textViewFullName.setText(notification.getSender().getFullName());
        holder.textViewPublishDate.setText(MyUtilities.convertMillisToReadableTimespan(notification.getDateInMillis()));
        holder.notificationText.setText("si e' iscritto alla lista: " + notification.getListName());

        try {
            Glide.with(context)  //2
                    .load(notification.getSender().getProfilePicturePath()) //3
                    .fallback(R.drawable.icon_user_dark_blue)
                    .placeholder(R.drawable.icon_user_dark_blue)
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


    public void clearList() {
        notificationsList.clear();
        notifyDataSetChanged();
    }



    //------------------------------------------------------------------------------- VIEWHOLDERS

    class FollowRequestNotificationViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public ImageView imageViewProfilePicture;
        public TextView textViewFullName;
        public TextView textViewPublishDate;
        private CardView cardView;

        public FollowRequestNotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            imageViewProfilePicture = itemView.findViewById(R.id.imageView_followRequestNotificationItem_profilePicture);
            textViewFullName = itemView.findViewById(R.id.textView_followRequestNotificationItem_fullName);
            textViewPublishDate = itemView.findViewById(R.id.textView_followRequestNotificationItem_publishDate);
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
        public TextView textViewFullName;
        public TextView textViewPublishDate;
        private CardView cardView;

        public PostLikedNotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            imageViewProfilePicture = itemView.findViewById(R.id.imageView_postLikedNotificationItem_profilePicture);
            textViewFullName = itemView.findViewById(R.id.textView_postLikedNotificationItem_fullName);
            textViewPublishDate = itemView.findViewById(R.id.textView_postLikedNotificationItem_publishDate);
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
        public TextView textViewFullName;
        public TextView textViewPublishDate;
        private CardView cardView;

        public PostCommentedNotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            imageViewProfilePicture = itemView.findViewById(R.id.imageView_postCommentedNotificationItem_profilePicture);
            textViewFullName = itemView.findViewById(R.id.textView_postCommentedNotificationItem_fullName);
            textViewPublishDate = itemView.findViewById(R.id.textView_postCommentedNotificationItem_publishDate);
            cardView = itemView.findViewById(R.id.cardView_postCommentedNotificationItem);
            cardView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            listener.onPostCommentedNotificationClicked(getAdapterPosition());

        }
    }// end PostCommentedNotificationViewHolder class

    class ListRecommendationNotificationViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public ImageView imageViewProfilePicture;
        public TextView textViewFullName;
        public TextView textViewPublishDate;
        private CardView cardView;

        public ListRecommendationNotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            imageViewProfilePicture = itemView.findViewById(R.id.imageView_listRecommendationNotificatonLayout_profilePicture);
            textViewFullName = itemView.findViewById(R.id.textView_listRecommendationNotificatonLayout_fullName);
            textViewPublishDate = itemView.findViewById(R.id.textView_listRecommendationNotificatonLayout_publishDate);
            cardView = itemView.findViewById(R.id.cardView_listRecommendationNotificatonLayout);
            cardView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            listener.onListRecommendedNotificationClicked(getAdapterPosition());

        }
    }// end PostCommentedNotificationViewHolder class

    class SubscribedToListNotificationViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public ImageView imageViewProfilePicture;
        public TextView textViewFullName;
        public TextView textViewPublishDate;
        public TextView notificationText;

        private CardView cardView;

        public SubscribedToListNotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            imageViewProfilePicture = itemView.findViewById(R.id.imageView_subscribedToListNotificatonLayout_profilePicture);
            textViewFullName = itemView.findViewById(R.id.textView_subscribedToListNotificatonLayout_fullName);
            textViewPublishDate = itemView.findViewById(R.id.textView_subscribedToListNotificatonLayout_publishDate);
            notificationText = itemView.findViewById(R.id.textView_subscribedToListNotificatonLayout_text);

            cardView = itemView.findViewById(R.id.cardView_subscribedToListNotificatonLayout);
            cardView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            listener.onSubscribedToListNotificationClicked(getAdapterPosition());

        }
    }// end PostCommentedNotificationViewHolder class

}// end RecylerAdapterNotifications class
