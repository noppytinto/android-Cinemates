package mirror42.dev.cinemates.adapter.viewholder;

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
import mirror42.dev.cinemates.model.User;

public class RecyclerAdapterFollowersList extends RecyclerView.Adapter<RecyclerAdapterFollowersList.FollowerViewHolder> {
    private ArrayList<User> followers;
    private final Context context;
    private FollowerListener listener;
    public interface FollowerListener {
        void onFollowerClicked(int position, View v);
    }


    //------------------------------------------------------------------------ CONSTRUCTORS

    public RecyclerAdapterFollowersList(ArrayList<User> followers, Context context, FollowerListener listener) {
        this.followers = followers;
        this.context = context;
        this.listener = listener;
    }



    //------------------------------------------------------------------------ METHODS

    @NonNull
    @Override
    public FollowerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_users_list_item, parent, false);
        return new FollowerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FollowerViewHolder holder, int position) {
        User follower = followers.get(position);

        try {
            Glide.with(context)  //2
                    .load(follower.getProfilePicturePath()) //3
                    .fallback(R.drawable.icon_user_light_blue)
                    .placeholder(R.drawable.icon_user_light_blue)
                    .circleCrop() //4
                    .into(holder.imageViewProfilePicture); //8
        } catch (Exception e) {
            e.printStackTrace();
        }

        holder.textViewFullName.setText(follower.getFullName());
        holder.textViewUsername.setText("@" + follower.getUsername());
    }

    @Override
    public int getItemCount() {
        return ( (followers != null) && (followers.size() != 0) ? followers.size() : 0);
    }

    public User getItem(int position) {
        return ( (followers != null) && (followers.size() != 0) ? followers.get(position) : null);
    }

    public void loadNewData(ArrayList<User> newList) {
        followers = newList;
        notifyDataSetChanged();
    }










    //---------------------------------------------------------------------- VIEWHOLDERS

    public class FollowerViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private CardView cardView;
        private ImageView imageViewProfilePicture;
        private TextView textViewFullName;
        private TextView textViewUsername;



        public FollowerViewHolder(@NonNull View itemView) {
            super(itemView);

            cardView = itemView.findViewById(R.id.cardView_userListItem);
            imageViewProfilePicture = itemView.findViewById(R.id.imageView_userListItem_profilePicture);
            textViewFullName = itemView.findViewById(R.id.textView_userListItem_fullName);
            textViewUsername = itemView.findViewById(R.id.textView_userListItem_username);
            cardView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            listener.onFollowerClicked(getAdapterPosition(), v);
        }

    }// end UserSearchResultViewHolder class




}// end RecyclerAdapterFollowersList class
