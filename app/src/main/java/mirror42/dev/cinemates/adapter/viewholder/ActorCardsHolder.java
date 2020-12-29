package mirror42.dev.cinemates.adapter.viewholder;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import mirror42.dev.cinemates.R;


public class ActorCardsHolder extends RecyclerView.ViewHolder {
    public ImageView imageViewActorCard;
    public TextView textViewActorCard;


    public ActorCardsHolder(@NonNull View itemView) {
        super(itemView);

        this.imageViewActorCard = (ImageView) itemView.findViewById(R.id.imageview_actor_card);
        this.textViewActorCard = (TextView) itemView.findViewById(R.id.textview_actor_card);
    }
}
