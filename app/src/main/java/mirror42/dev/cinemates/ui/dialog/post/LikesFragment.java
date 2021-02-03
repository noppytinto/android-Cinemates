package mirror42.dev.cinemates.ui.dialog.post;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import mirror42.dev.cinemates.R;
import mirror42.dev.cinemates.adapter.RecyclerAdapterShowLikesDialog;
import mirror42.dev.cinemates.model.User;

public class LikesFragment extends DialogFragment implements RecyclerAdapterShowLikesDialog.ClickAdapterListener {
    private ArrayList<User> usersList;
    private RecyclerAdapterShowLikesDialog recyclerAdapterShowLikesDialog;


    public LikesFragment() {
        // Empty constructor is required for DialogFragment
        // Make sure not to add arguments to the constructor
        // Use `newInstance` instead as shown below
    }




    public static LikesFragment getInstance(ArrayList<User> usersList) {
        LikesFragment frag = new LikesFragment();
        Bundle args = new Bundle();
        args.putSerializable("usersList", usersList);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_show_likes, container);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initRecyclerView(view);
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        usersList = (ArrayList<User>) getArguments().getSerializable("usersList");
        recyclerAdapterShowLikesDialog.loadNewData(usersList);
    }

    @Override
    public void onResume() {
        super.onResume();
        if(getDialog() != null)
            getDialog().getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }






    //------------------------------------------------------- MY METHODS

    private void initRecyclerView(View view) {
        // defining Recycler view
        RecyclerView recyclerView = view.findViewById(R.id.recyclerView_showLikesDialogFragment);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // adding recycle listener for touch detection
        recyclerAdapterShowLikesDialog = new RecyclerAdapterShowLikesDialog(new ArrayList<>(), getContext(), this);
        recyclerView.setAdapter(recyclerAdapterShowLikesDialog);
    }

    @Override
    public void onItemClicked(int position) {

    }



}// end ShowLikesDialogFragment class