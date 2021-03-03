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

import com.google.android.material.progressindicator.LinearProgressIndicator;

import java.util.ArrayList;

import mirror42.dev.cinemates.R;
import mirror42.dev.cinemates.model.User;
import mirror42.dev.cinemates.model.list.CustomList;
import mirror42.dev.cinemates.model.tmdb.Movie;
import mirror42.dev.cinemates.utilities.ImageUtilities;

public class RecyclerAdapterCustomLists extends RecyclerView.Adapter<RecyclerAdapterCustomLists.CustomListViewHolder> {
    private ArrayList<CustomList> customLists;
    private CustomListCoverListener listener;
    private Context context;
    private boolean areNotMyLists;

    public interface CustomListCoverListener {
        void onCoverClicked(int position);
        void imageClicked(int position);
    }



    //------------------------------------------------------------------- CONSTRUCTORS

    public RecyclerAdapterCustomLists(ArrayList<CustomList> customLists,
                                       Context context,
                                      CustomListCoverListener listener, boolean areNotMyLists) {
        this.customLists = customLists;
        this.context = context;
        this.listener = listener;
        this.areNotMyLists = areNotMyLists;
    }



    //------------------------------------------------------------------- ANDROID METHODS

    @NonNull
    @Override
    public CustomListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.layout_custom_list_cover, parent, false);
        return new CustomListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CustomListViewHolder holder, int position) {
        CustomList customList = customLists.get(position);
        ArrayList<ImageView> thumbnailsList = new ArrayList<>();
        thumbnailsList.add(holder.thumbnail_1);
        thumbnailsList.add(holder.thumbnail_2);
        thumbnailsList.add(holder.thumbnail_3);
        thumbnailsList.add(holder.thumbnail_4);
        thumbnailsList.add(holder.thumbnail_5);
        thumbnailsList.add(holder.thumbnail_6);
        thumbnailsList.add(holder.thumbnail_7);
        thumbnailsList.add(holder.thumbnail_8);

        ArrayList<Movie> movies = new ArrayList<>();
        movies = customList.getMovies();

        for(int i=0; movies!=null && i<8 && i<movies.size(); i++) {
            ImageView t = thumbnailsList.get(i);
            String posterUrl = movies.get(i).getPosterURL();
            ImageUtilities.loadRectangularImageInto(posterUrl, t, context);
        }


        holder.listTitle.setText(customList.getName());
        holder.listDescription.setText(customList.getDescription());

        holder.progressIndicator.setVisibility(View.GONE);

        if(areNotMyLists) {
            User owner = customList.getOwner();

            holder.ownerFullname.setVisibility(View.VISIBLE);
            holder.ownerUsername.setVisibility(View.VISIBLE);
            holder.profilePicture.setVisibility(View.VISIBLE);

            holder.ownerFullname.setText(owner.getFullName());
            holder.ownerUsername.setText(owner.getUsername());
            ImageUtilities.loadCircularImageInto(owner.getProfilePicturePath(), holder.profilePicture, context);
        }
    }

    @Override
    public int getItemCount() {
        return ( (customLists != null) && (customLists.size() != 0) ? customLists.size() : 0);
    }

    public CustomList getList(int position) {
        return ( (customLists != null) && (customLists.size() != 0) ? customLists.get(position) : null);
    }

    public void loadNewData(ArrayList<CustomList> newList) {
        customLists = newList;
        notifyDataSetChanged();
    }

    public void addPlaceholderItem(CustomList item) {
        if(customLists == null)
            customLists = new ArrayList<>();

        customLists.add(item);
        notifyDataSetChanged();
    }

    public void removeItem(CustomList item) {
        customLists.remove(item);
        notifyDataSetChanged();
    }






    //------------------------------------------------------------------- VIEWHOLDERS

    class CustomListViewHolder extends RecyclerView.ViewHolder {
        private ImageView thumbnail_1;
        private ImageView thumbnail_2;
        private ImageView thumbnail_3;
        private ImageView thumbnail_4;
        private ImageView thumbnail_5;
        private ImageView thumbnail_6;
        private ImageView thumbnail_7;
        private ImageView thumbnail_8;
        private TextView listTitle;
        private TextView listDescription;
        private CardView containter;
        private LinearProgressIndicator progressIndicator;
        private ImageView profilePicture;
        private TextView ownerFullname;
        private TextView ownerUsername;

        public CustomListViewHolder(@NonNull View itemView) {
            super(itemView);
            thumbnail_1 = itemView.findViewById(R.id.imageView_custmListCover_1);
            thumbnail_2 = itemView.findViewById(R.id.imageView_custmListCover_2);
            thumbnail_3 = itemView.findViewById(R.id.imageView_custmListCover_3);
            thumbnail_4 = itemView.findViewById(R.id.imageView_custmListCover_4);
            thumbnail_5 = itemView.findViewById(R.id.imageView_custmListCover_5);
            thumbnail_6 = itemView.findViewById(R.id.imageView_custmListCover_6);
            thumbnail_7 = itemView.findViewById(R.id.imageView_custmListCover_7);
            thumbnail_8 = itemView.findViewById(R.id.imageView_custmListCover_8);
            listTitle = itemView.findViewById(R.id.textView_custmListCover_title);
            listDescription = itemView.findViewById(R.id.textView_custmListCover_description);
            containter = itemView.findViewById(R.id.cardView_customListcover);
            progressIndicator = itemView.findViewById(R.id.progressIndicator_customListCover);
            profilePicture = itemView.findViewById(R.id.imageView_customListCover_profilePicture);
            ownerFullname = itemView.findViewById(R.id.textView_customListCover_fullName);
            ownerUsername = itemView.findViewById(R.id.textView_customListCover_username);

            containter.setOnClickListener(v -> listener.onCoverClicked(getAdapterPosition()));
            profilePicture.setOnClickListener(v->listener.imageClicked(getAdapterPosition()));
        }

    }

}// end RecyclerAdapterCustomLists class
