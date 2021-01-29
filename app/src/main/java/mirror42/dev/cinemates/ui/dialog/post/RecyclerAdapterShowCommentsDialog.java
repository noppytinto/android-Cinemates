package mirror42.dev.cinemates.ui.dialog.post;

import android.content.Context;
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
import mirror42.dev.cinemates.model.Comment;
import mirror42.dev.cinemates.model.User;

public class RecyclerAdapterShowCommentsDialog
        extends RecyclerView.Adapter<RecyclerAdapterShowCommentsDialog.UserCommentsListItemViewHolder>{
    private ArrayList<Comment> comments;
    private Context context;
    private ClickAdapterListener listener;


    interface ClickAdapterListener {
        void onItemClicked(int position);
        void onDeleteButtonPressed(int position);
    }

    //----------------------------------------------------------------------- CONSTRUCTORS

    public RecyclerAdapterShowCommentsDialog(ArrayList<Comment> comments,
                                          Context context,
                                          ClickAdapterListener listener) {
        this.comments = comments;
        this.context = context;
        this.listener = listener;

    }



    //----------------------------------------------------------------------- METHODS

    @NonNull
    @Override
    public UserCommentsListItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_users_comment_list_item, parent, false);
        return new UserCommentsListItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserCommentsListItemViewHolder holder, int position) {
        Comment comment = comments.get(position);
        User user = comments.get(position).getOwner();

        String firstName = user.getFirstName();
        String lastName = user.getLastName();
        String commentText = comment.getText();
        String profilePictureUrl = user.getProfilePicturePath();

        holder.textViewFullName.setText(firstName + " " + lastName);
        holder.textViewCommentText.setText(commentText);

        if(comment.isMine())
            holder.buttonDelete.setVisibility(View.VISIBLE);
        else
            holder.buttonDelete.setVisibility(View.GONE);


        //
//        if(comment.isNewItem()) {
//            holder.cardView.setActivated(false);
//        }

        Glide.with(context)  //2
                .load(profilePictureUrl) //3
                .fallback(R.drawable.icon_user_dark_blue)
                .placeholder(R.drawable.icon_user_dark_blue)
                .circleCrop()
                .into(holder.imageViewProfilePicture); //8
    }


    @Override
    public int getItemCount() {
        return ( (comments != null) && (comments.size() != 0) ? comments.size() : 0);
    }

    public Comment getComment(int position) {
        return ( (comments != null) && (comments.size() != 0) ? comments.get(position) : null);
    }

    public void loadNewData(ArrayList<Comment> newList) {
        comments = newList;
        notifyDataSetChanged();
    }

    public void addPlaceholderitem(Comment item) {
        if(comments == null)
            comments = new ArrayList<>();

        comments.add(item);
        notifyDataSetChanged();
    }


    public void removeItem(Comment item) {
        comments.remove(item);
        notifyDataSetChanged();
    }





    //---------------------------------------------------------------------- VIEWHOLDERS

    class UserCommentsListItemViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {
        public ImageView imageViewProfilePicture;
        public TextView textViewFullName;
        public TextView textViewCommentText;
        public CardView cardView;
        private ImageButton buttonDelete;




        //--------------------------------------------- CONSTRUCTORS

        UserCommentsListItemViewHolder(@NonNull View itemView) {
            super(itemView);
            this.imageViewProfilePicture = itemView.findViewById(R.id.imageView_userCommentListItem_profilePicture);
            this.textViewFullName = itemView.findViewById(R.id.textView_userCommentListItem_fullName);
            this.textViewCommentText = itemView.findViewById(R.id.textView_userCommentListItem_commentText);
            this.cardView = itemView.findViewById(R.id.cardView_userCommentListItem);
            this.buttonDelete = itemView.findViewById(R.id.imageButton_userCommentListItem_delete);

            this.imageViewProfilePicture.setOnClickListener(this);
            this.buttonDelete.setOnClickListener(this);
        }


        //--------------------------------------------- METHODS

        @Override
        public void onClick(View v) {
            if(v.getId() == imageViewProfilePicture.getId()) {
                //TODO: show profile
                listener.onItemClicked(getAdapterPosition());

            }
            else if(v.getId() == buttonDelete.getId()) {
                //TODO: show profile
                listener.onDeleteButtonPressed(getAdapterPosition());

            }
        }

    }// end UsersListItemViewHolder class


}// end RecyclerAdapterShowCommentsDialog class
