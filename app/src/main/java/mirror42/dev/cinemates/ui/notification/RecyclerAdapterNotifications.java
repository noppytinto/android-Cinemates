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
import mirror42.dev.cinemates.ui.notification.model.Notification;
import mirror42.dev.cinemates.ui.notification.model.Notification.NotificationType;

public class RecyclerAdapterNotifications extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
    private ArrayList<Notification> notificationsList;
    private OnNotificationClickedListener listener;
    private Context context;

    private static final int FOLLOW_REQUEST = 1;
    private static final int POST_COMMENTED = 2;
    private static final int POST_LIKED = 3;

    public interface OnNotificationClickedListener {
        public void onFollowRequestNotificationClicked(int position);
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
            return new FollowRequestViewHolder(view);
        }
        else {
            return new FollowRequestViewHolder(null);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if(getItemViewType(position) == FOLLOW_REQUEST) {
            FollowRequestNotification followRequestNotification = (FollowRequestNotification) notificationsList.get(position);



            //
            buildWatchlistPost((FollowRequestViewHolder) holder, followRequestNotification);
        }
    }

    private void buildWatchlistPost(FollowRequestViewHolder holder, FollowRequestNotification followRequestNotification) {
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

    class FollowRequestViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public ImageView imageViewProfilePicture;
        public TextView textViewUsername;
        public TextView textViewFullName;
        private CardView cardView;

        public FollowRequestViewHolder(@NonNull View itemView) {
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
    }// end FollowRequestViewHolder class




}// end RecylerAdapterNotifications class
