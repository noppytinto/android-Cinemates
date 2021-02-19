package mirror42.dev.cinemates.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

import mirror42.dev.cinemates.R;
import mirror42.dev.cinemates.model.User;

public class RecyclerAdapterShowLikesDialog extends RecyclerView.Adapter<RecyclerAdapterShowLikesDialog.UsersListItemViewHolder> {
    private ArrayList<User> usersList;
    private Context context;
    private ClickAdapterListener listener;

    public interface ClickAdapterListener {
        void onItemClicked(int position);
    }




    //----------------------------------------------------------------------- CONSTRUCTORS

    public RecyclerAdapterShowLikesDialog(ArrayList<User> usersList,
                                          Context context,
                                          ClickAdapterListener listener) {
        this.usersList = usersList;
        this.context = context;
        this.listener = listener;

    }



    //----------------------------------------------------------------------- METHODS

    @NonNull
    @Override
    public UsersListItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_users_list_item, parent, false);
        return new UsersListItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UsersListItemViewHolder holder, int position) {
        User user = usersList.get(position);

        String firstName = user.getFirstName();
        String lastName = user.getLastName();
        String username = user.getUsername();
        String profilePictureUrl = user.getProfilePicturePath();

        holder.textViewFullName.setText(firstName + " " + lastName);
        holder.textViewUsername.setText("@" + username);

        Glide.with(context)  //2
                .load(profilePictureUrl) //3
                .fallback(R.drawable.broken_image)
                .placeholder(R.drawable.placeholder_image)
                .circleCrop()
                .into(holder.imageViewProfilePicture); //8
    }


    @Override
    public int getItemCount() {
        return ( (usersList != null) && (usersList.size() != 0) ? usersList.size() : 0);
    }

    public User getUser(int position) {
        return ( (usersList != null) && (usersList.size() != 0) ? usersList.get(position) : null);
    }

    public void loadNewData(ArrayList<User> newList) {
        usersList = newList;
        notifyDataSetChanged();
    }

    public void addPlaceholderItem(User item) {
        usersList.add(item);
        notifyDataSetChanged();
    }



















    //---------------------------------------------------------------------- VIEWHOLDERS

    class UsersListItemViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {
        public ImageView imageViewProfilePicture;
        public TextView textViewFullName;
        public TextView textViewUsername;
        public ImageButton buttonAddFriend;



        //--------------------------------------------- CONSTRUCTORS

        UsersListItemViewHolder(@NonNull View itemView) {
            super(itemView);
            this.imageViewProfilePicture = itemView.findViewById(R.id.imageView_userListItem_profilePicture);
            this.textViewFullName = itemView.findViewById(R.id.textView_userListItem_fullName);
            this.textViewUsername = itemView.findViewById(R.id.textView_userListItem_username);

            this.imageViewProfilePicture.setOnClickListener(this);
        }


        //--------------------------------------------- METHODS

        @Override
        public void onClick(View v) {
            if(v.getId() == imageViewProfilePicture.getId()) {
                //TODO: show profile
                listener.onItemClicked(getAdapterPosition());

            }
        }

    }// end UsersListItemViewHolder class

}// end RecyclerAdapterShowLikesDialog class
