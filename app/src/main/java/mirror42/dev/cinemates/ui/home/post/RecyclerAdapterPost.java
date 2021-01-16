package mirror42.dev.cinemates.ui.home.post;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

import mirror42.dev.cinemates.R;
import mirror42.dev.cinemates.model.Post;
import mirror42.dev.cinemates.model.Post.PostType;
import mirror42.dev.cinemates.model.WatchlistPost;

public class RecyclerAdapterPost extends RecyclerView.Adapter<RecyclerView.ViewHolder>  {
    private ArrayList<Post> postList;
    private Context context;

    private static final int ADD_TO_LIST_WATCHLIST = 1;
    private static final int ADD_TO_LIST_WATCHED = 2;



    public RecyclerAdapterPost(ArrayList<Post> postList, Context context) {
        this.postList = postList;
        this.context = context;
    }

    @Override
    public int getItemViewType(int position) {
        PostType postType = postList.get(position).getPostType();

        switch (postType) {
            case ADD_TO_LIST_WATCHLIST:
                return ADD_TO_LIST_WATCHLIST;
            case ADD_TO_LIST_WATCHED:
                return ADD_TO_LIST_WATCHED;
            default:
                return 0;
        }
    }



    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view;

        if(viewType == ADD_TO_LIST_WATCHLIST) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.base_post_fragment, parent, false);
            return new WatchlistPostViewHolder(view);

        }
        else {
            return new WatchlistPostViewHolder(null);
        }

    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if(getItemViewType(position) == ADD_TO_LIST_WATCHLIST) {
            WatchlistPost watchlistPost = (WatchlistPost) postList.get(position);

            ((WatchlistPostViewHolder) holder).textViewUsername.setText(watchlistPost.getOwner().getUsername());
            ((WatchlistPostViewHolder) holder).textViewPublishDate.setText(String.valueOf(watchlistPost.getPublishDateMillis()));
            ((WatchlistPostViewHolder) holder).textViewPostDescription.setText(watchlistPost.getDescription());

            Glide.with(context)  //2
                    .load(watchlistPost.getThumbnail_1_url()) //3
                    .fallback(R.drawable.broken_image)
                    .placeholder(R.drawable.placeholder_image)
                    .fitCenter() //4
                    .into(((WatchlistPostViewHolder) holder).imageViewThumbnail_1); //8
            Glide.with(context)  //2
                    .load(watchlistPost.getThumbnail_2_url()) //3
                    .fallback(R.drawable.broken_image)
                    .placeholder(R.drawable.placeholder_image)
                    .fitCenter() //4
                    .into(((WatchlistPostViewHolder) holder).imageViewThumbnail_2); //8
            Glide.with(context)  //2
                    .load(watchlistPost.getThumbnail_3_url()) //3
                    .fallback(R.drawable.broken_image)
                    .placeholder(R.drawable.placeholder_image)
                    .fitCenter() //4
                    .into(((WatchlistPostViewHolder) holder).imageViewThumbnail_3); //8


            try {
                Glide.with(context)  //2
                        .load(watchlistPost.getOwner().getProfilePicturePath()) //3
                        .fallback(R.drawable.broken_image)
                        .placeholder(R.drawable.placeholder_image)
                        .circleCrop() //4
                        .into(((WatchlistPostViewHolder) holder).imageViewProfilePicture); //8
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }





    @Override
    public int getItemCount() {
        return ( (postList != null) && (postList.size() != 0) ? postList.size() : 0);
    }

    public Post getWatchlistPost(int position) {
        return ( (postList != null) && (postList.size() != 0) ? postList.get(position) : null);
    }

    public void loadNewData(ArrayList<Post> newList) {
        postList = newList;
        notifyDataSetChanged();
    }



}
