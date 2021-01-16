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

public class WatchlistPostAdapter extends RecyclerView.Adapter<WatchlistPostViewHolder>  {
    private ArrayList<WatchlistPost> postList;
    private Context context;



    public WatchlistPostAdapter(ArrayList<WatchlistPost> postList, Context context) {
        this.postList = postList;
        this.context = context;
    }



    @NonNull
    @Override
    public WatchlistPostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.base_post_fragment, parent, false);
        return new WatchlistPostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WatchlistPostViewHolder holder, int position) {
        WatchlistPost watchlistPost = postList.get(position);

        holder.textViewUsername.setText("Mario Rossi");
        holder.textViewPublishDate.setText("5 min fa");
        holder.textViewPostDescription.setText("ha aggiunt 5 film alla Watchlist.");

        Glide.with(context)  //2
                .load(watchlistPost.getThumbnail_1_url()) //3
                .fallback(R.drawable.broken_image)
                .placeholder(R.drawable.placeholder_image)
                .fitCenter() //4
                .into(holder.imageViewThumbnail_1); //8
        Glide.with(context)  //2
                .load(watchlistPost.getThumbnail_1_url()) //3
                .fallback(R.drawable.broken_image)
                .placeholder(R.drawable.placeholder_image)
                .fitCenter() //4
                .into(holder.imageViewThumbnail_2); //8
        Glide.with(context)  //2
                .load(watchlistPost.getThumbnail_1_url()) //3
                .fallback(R.drawable.broken_image)
                .placeholder(R.drawable.placeholder_image)
                .fitCenter() //4
                .into(holder.imageViewThumbnail_3); //8


        try {
            Glide.with(context)  //2
                    .load("https://www.themoviedb.org/t/p/w600_and_h900_bestv2/vSe6sIsdtcoqBhuWRXynahFg8Vf.jpg") //3
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
        return ( (postList != null) && (postList.size() != 0) ? postList.size() : 0);
    }

    public WatchlistPost getWatchlistPost(int position) {
        return ( (postList != null) && (postList.size() != 0) ? postList.get(position) : null);
    }

    public void loadNewData(ArrayList<WatchlistPost> newList) {
        postList = newList;
        notifyDataSetChanged();
    }



}
