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
import mirror42.dev.cinemates.model.User;

public class ShowLikesDialogFragment extends DialogFragment implements RecyclerAdapterShowLikesDialog.ClickAdapterListener {
    private ArrayList<User> usersList;
    private View view;
    private RecyclerAdapterShowLikesDialog recyclerAdapterShowLikesDialog;


    public ShowLikesDialogFragment() {
        // Empty constructor is required for DialogFragment
        // Make sure not to add arguments to the constructor
        // Use `newInstance` instead as shown below
    }




    public static ShowLikesDialogFragment getInstance(ArrayList<User> usersList) {
        ShowLikesDialogFragment frag = new ShowLikesDialogFragment();
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
        this.view = view;
        initRecyclerView();
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        usersList = (ArrayList<User>) getArguments().getSerializable("usersList");
        recyclerAdapterShowLikesDialog.loadNewData(usersList);
    }








    //------------------------------------------------------- METHODS

    private void initRecyclerView() {
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