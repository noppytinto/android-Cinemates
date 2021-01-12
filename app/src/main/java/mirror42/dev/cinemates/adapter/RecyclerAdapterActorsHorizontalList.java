package mirror42.dev.cinemates.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

import mirror42.dev.cinemates.R;
import mirror42.dev.cinemates.adapter.viewholder.ActorCardHolder;
import mirror42.dev.cinemates.tmdbAPI.model.Person;


public class RecyclerAdapterActorsHorizontalList extends RecyclerView.Adapter<ActorCardHolder>{
    private ArrayList<Person> peopleList;
    private Context context;




    //------------------------------------------------------------------------CONSTRUCTORS

    public RecyclerAdapterActorsHorizontalList(ArrayList<Person> peopleList, Context context) {
        this.peopleList = peopleList;
        this.context = context;
    }




    //------------------------------------------------------------------------ METHODS
    @Override
    public int getItemCount() {
        // determines how many items the RecyclerView has.
        // You want the size of the array to match the size of the RecyclerView,
        // so you return that

//        if( (moviesList != null) && (moviesList.size() != 0) )
//            return moviesList.size();
//        else
//            return 0;

        // Your Adapter now knows how many items to display on the screen

        return ( (peopleList != null) && (peopleList.size() != 0) ? peopleList.size() : 0);
    }

    @NonNull
    @Override
    public ActorCardHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Next, you need to create the Layout needed for the ViewHolder
        // to display each item in the RecyclerView
        //
        // it uses a LayoutInflater object to create a layout programmatically.
        // It uses the parent context of the Adapter to create itself
        // and attempts to inflate the Layout you want by passing
        // in the layout name and the parent ViewGroup so the View has a parent it can refer to.
        // The Boolean value is used to specify whether the View should be attached to the parent.
        // Always use false for RecyclerView layouts
        // as the RecyclerView attaches and detaches the Views for you.
        //
        // A ViewHolder object is created, passing in the view created from the layout.
        // Finally, the ViewHolder is returned from the method

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.actor_card, parent, false);
        return new ActorCardHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ActorCardHolder holder, int position) {
        // With the ViewHolder created,
        // you have to bind the list titles to it. To do this,
        // you need to know what Views to bind your data to.
        //
        // This is called repeatedly as you scroll through the RecyclerView

        Person person = peopleList.get(position);

        Glide.with(context)  //2
                .load(person.getProfileImageURL()) //3
                .fallback(R.drawable.broken_image)
                .placeholder(R.drawable.placeholder_image)
                .fitCenter() //4
                .into(holder.imageViewActorCard); //8

        holder.textViewActorCard.setText(person.getFullName() +"\n"+ person.getCharacter() + "\n(" + person.getDepartment() + ")");
    }



    public Person getPerson(int position) {
        return ( (peopleList != null) && (peopleList.size() != 0) ? peopleList.get(position) : null);
    }

    public void loadNewData(ArrayList<Person> newPeopleList) {
        peopleList = newPeopleList;
        notifyDataSetChanged();
    }




}// end RecycleAdapterActorsHorizontalList class
