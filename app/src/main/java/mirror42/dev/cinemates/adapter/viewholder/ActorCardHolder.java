package mirror42.dev.cinemates.adapter.viewholder;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import mirror42.dev.cinemates.R;


public class ActorCardHolder extends RecyclerView.ViewHolder {
    public ImageView imageViewActorCard;
    public TextView textViewActorCard;


    public ActorCardHolder(@NonNull View itemView) {
        super(itemView);

        this.imageViewActorCard = itemView.findViewById(R.id.imageview_actorCard);
        this.textViewActorCard = itemView.findViewById(R.id.textview_actorCard);
    }
}
