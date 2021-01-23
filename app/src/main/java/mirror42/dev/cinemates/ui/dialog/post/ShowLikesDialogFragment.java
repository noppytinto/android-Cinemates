package mirror42.dev.cinemates.ui.dialog.post;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

import mirror42.dev.cinemates.R;
import mirror42.dev.cinemates.model.User;

public class ShowLikesDialogFragment extends DialogFragment implements RecyclerAdapterShowLikesDialog.ClickAdapterListener {
    private ShowLikesDialogViewModel showLikesDialogViewModel;
    private ArrayList<User> usersList;
    private View view;
    private RecyclerAdapterShowLikesDialog recyclerAdapterShowLikesDialog;

    public ShowLikesDialogFragment(ArrayList<User> usersList) {
        this.usersList = usersList;
    }

    @NotNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = requireActivity().getLayoutInflater();

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        this.view = inflater.inflate(R.layout.dialog_show_likes_fragment, null);
        builder.setView(view);
                // Add action buttons
        return builder.create();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        showLikesDialogViewModel = new ViewModelProvider(this).get(ShowLikesDialogViewModel.class);
        // TODO: Use the ViewModel

        initRecyclerView();
        recyclerAdapterShowLikesDialog.loadNewData(usersList);

    }








    //------------------------------------------------------- METHODS

    private void initRecyclerView() {
        // defining Recycler view
        RecyclerView recyclerView = view.findViewById(R.id.recyclerView_showLikesDialogFragment);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // adding recycle listener for touch detection
        recyclerAdapterShowLikesDialog = new RecyclerAdapterShowLikesDialog(new ArrayList<>(), getContext(), this);
        recyclerView.setAdapter(recyclerAdapterShowLikesDialog);
    }


    @Override
    public void onItemClicked(int position) {

    }
}// end ShowLikesDialogFragment class