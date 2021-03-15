package mirror42.dev.cinemates.adapter.viewholder;

import android.content.Context;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

import mirror42.dev.cinemates.R;
import mirror42.dev.cinemates.model.User;

public class RecyclerAdapterUsersList extends RecyclerView.Adapter<RecyclerAdapterUsersList.UserItemViewHolder> {
    private final String TAG = this.getClass().getSimpleName();
    private ClickAdapterListener listener;
    private Context context;
    private ArrayList<User> recyclerList;
    private boolean showRemoveUserButton;

    public interface ClickAdapterListener {
        void onUserClicked(int position);
        void onRemoveButtonClicked(int position);

    }




    //-------------------------------------------------------------------------------------- CONSTRUCTORS

    public RecyclerAdapterUsersList(ArrayList<User> users, Context context, ClickAdapterListener listener, boolean showRemoveUserButton) {
        this.recyclerList = users;
        this.context = context;
        this.listener = listener;
        this.showRemoveUserButton = showRemoveUserButton;
    }




    //-------------------------------------------------------------------------------------- METHODS
    @NonNull
    @Override
    public UserItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = null;
        view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_users_list_item, parent, false);
        return new UserItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserItemViewHolder holder, int position) {
        User item = recyclerList.get(position);
        if(showRemoveUserButton)
            holder.removeUserButton.setVisibility(View.VISIBLE);

        Glide.with(context)  //2
                .load(item.getProfilePicturePath()) //3
                .fallback(R.drawable.icon_user_dark_blue)
                .placeholder(R.drawable.icon_user_dark_blue)
                .circleCrop() //4
                .into(holder.imageViewProfilePicture); //8

        holder.textViewFullName.setText(item.getFullName());
        holder.textViewUsername.setText("@" + item.getUsername());
    }

    @Override
    public int getItemCount() {
        return ( (recyclerList != null) && (recyclerList.size() != 0) ? recyclerList.size() : 0);
    }

    public User getItem(int position) {
        return ( (recyclerList != null) && (recyclerList.size() != 0) ? recyclerList.get(position) : null);
    }

    public void loadNewData(ArrayList<User> newList) {
        if(newList==null)
            recyclerList = new ArrayList<>();
        else
            recyclerList = newList;
        notifyDataSetChanged();
    }


    public void removeItem(User item) {
        recyclerList.remove(item);
        notifyDataSetChanged();
    }

    public void clearList() {
        if(recyclerList!=null)
            recyclerList.clear();
        notifyDataSetChanged();
    }



    //-------------------------------------------------------------------------------------- VIEWHOLDERS

    class UserItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private CardView cardView;
        private ImageView imageViewProfilePicture;
        private TextView textViewFullName;
        private TextView textViewUsername;
        private ImageButton removeUserButton;



        public UserItemViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardView_userListItem);
            imageViewProfilePicture = itemView.findViewById(R.id.imageView_userListItem_profilePicture);
            textViewFullName = itemView.findViewById(R.id.textView_userListItem_fullName);
            textViewUsername = itemView.findViewById(R.id.textView_userListItem_username);
            removeUserButton = itemView.findViewById(R.id.imageButton_userListItem_remove);

            cardView.setOnClickListener(this);
            removeUserButton.setOnClickListener(this);
        }

        //--------------------------------------------- METHODS

        @Override
        public void onClick(View v) {
            if(v.getId() == removeUserButton.getId()) {
                listener.onRemoveButtonClicked(getAdapterPosition());
                v.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
            }
            else if(v.getId() == cardView.getId()) {
                listener.onUserClicked(getAdapterPosition());
                v.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
            }

        }

    }// end UserItemViewHolder class


}// end RecyclerAdapterUsersList class
