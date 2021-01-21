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

    private static final int ADD_TO_WATCHLIST = 1;
    private static final int ADD_TO_WATCHED_LIST = 2;




    //------------------------------------------------------------------------------- CONSTRUCTORS

    public RecyclerAdapterPost(ArrayList<Post> postList, Context context) {
        this.postList = postList;
        this.context = context;
    }



    //------------------------------------------------------------------------------- CONSTRUCTORS

    @Override
    public int getItemViewType(int position) {
        PostType postType = postList.get(position).getPostType();

        switch (postType) {
            case ADD_TO_WATCHLIST:
                return ADD_TO_WATCHLIST;
            case ADD_TO_WATCHED_LIST:
                return ADD_TO_WATCHED_LIST;
            default:
                return 0;
        }
    }



    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view;

        if(viewType == ADD_TO_WATCHLIST) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.watchlist_post_layout, parent, false);
            return new WatchlistPostViewHolder(view);
        }
        else {
            return new WatchlistPostViewHolder(null);
        }

    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if(getItemViewType(position) == ADD_TO_WATCHLIST) {
            WatchlistPost watchlistPost = (WatchlistPost) postList.get(position);
            //
            buildWatchlistPost((WatchlistPostViewHolder) holder, watchlistPost);
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



    //--------------------------------------------------------------------------------

    private void buildWatchlistPost(WatchlistPostViewHolder holder, WatchlistPost watchlistPost) {
        holder.textViewUsername.setText(watchlistPost.getOwner().getUsername());
        holder.textViewPublishDate.setText(String.valueOf(watchlistPost.getPublishDateMillis()));
        holder.textViewPostDescription.setText(watchlistPost.getDescription());



        try {
            String posterUrl_1 = watchlistPost.getMovie().getPosterURL();
            Glide.with(context)  //2
                    .load(posterUrl_1) //3
                    .fallback(R.drawable.broken_image)
                    .placeholder(R.drawable.placeholder_image)
                    .fitCenter() //4
                    .into(holder.imageViewThumbnail_1); //8
        } catch (Exception e) {
            e.printStackTrace();
        }

//        try {
//            String posterUrl_2 = watchlistPost.getAddedMovies().get(1).getPosterURL();
//            Glide.with(context)  //2
//                    .load(posterUrl_2) //3
//                    .fallback(R.drawable.broken_image)
//                    .placeholder(R.drawable.placeholder_image)
//                    .fitCenter() //4
//                    .into(holder.imageViewThumbnail_2); //8
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        try {
//            String posterUrl_3 = watchlistPost.getAddedMovies().get(2).getPosterURL();
//            Glide.with(context)  //2
//                    .load(posterUrl_3) //3
//                    .fallback(R.drawable.broken_image)
//                    .placeholder(R.drawable.placeholder_image)
//                    .fitCenter() //4
//                    .into(holder.imageViewThumbnail_3); //8
//        } catch (Exception e) {
//            e.printStackTrace();
//        }


        try {
            Glide.with(context)  //2
                    .load(watchlistPost.getOwner().getProfilePicturePath()) //3
                    .fallback(R.drawable.broken_image)
                    .placeholder(R.drawable.placeholder_image)
                    .circleCrop() //4
                    .into(holder.imageViewProfilePicture); //8
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



}// end RecyclerAdapterPost class
