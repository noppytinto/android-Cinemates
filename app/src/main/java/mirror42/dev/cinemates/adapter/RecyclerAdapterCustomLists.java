package mirror42.dev.cinemates.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import mirror42.dev.cinemates.model.list.CustomList;

public class RecyclerAdapterCustomLists extends RecyclerView.Adapter<RecyclerAdapterCustomLists.CustomListViewHolder> {
    private ArrayList<CustomList> customLists;
    private CustomListCoverListener listener;
    private Context context;

    public interface CustomListCoverListener {
        void onCoverClicked(int position);
    }



    //------------------------------------------------------------------- CONSTRUCTORS

    public RecyclerAdapterCustomLists(ArrayList<CustomList> customLists,
                                       Context context,
                                      CustomListCoverListener listener) {
        this.customLists = customLists;
        this.context = context;
        this.listener = listener;
    }



    //------------------------------------------------------------------- ANDROID METHODS

    @NonNull
    @Override
    public CustomListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull CustomListViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 0;
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

        public CustomListViewHolder(@NonNull View itemView) {
            super(itemView);
//            thumbnail_1 = itemView.findViewById(R.id.imageView_custmListCover_1);
//            thumbnail_2 = itemView.findViewById(R.id.imageView_custmListCover_2);
//            thumbnail_3 = itemView.findViewById(R.id.imageView_custmListCover_3);
//            thumbnail_4 = itemView.findViewById(R.id.imageView_custmListCover_4);
//            thumbnail_5 = itemView.findViewById(R.id.imageView_custmListCover_5);
//            thumbnail_6 = itemView.findViewById(R.id.imageView_custmListCover_6);
//            thumbnail_7 = itemView.findViewById(R.id.imageView_custmListCover_7);
//            thumbnail_8 = itemView.findViewById(R.id.imageView_custmListCover_8);
//            listTitle = itemView.findViewById(R.id.textview_custmListCover_listName);
//            listDescription = itemView.findViewById(R.id.textview_custmListCover_listDescription);
//            containter = itemView.findViewById(R.id.cardView_custmListCover);

            containter.setOnClickListener(v -> listener.onCoverClicked(getAdapterPosition()));
        }
    }

}// end RecyclerAdapterCustomLists class
