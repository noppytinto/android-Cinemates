package mirror42.dev.cinemates.ui.dialog;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

import mirror42.dev.cinemates.R;
import mirror42.dev.cinemates.adapter.viewholder.RecyclerAdapterFollowersList;
import mirror42.dev.cinemates.model.User;
import mirror42.dev.cinemates.ui.login.LoginViewModel;

public class RecommendListDialogFragment extends DialogFragment implements View.OnClickListener, RecyclerAdapterFollowersList.FollowerListener {
    private Button negativeButton;
    private RecommendListDialogListener listener;
    private LoginViewModel loginViewModel;
    private RecommendListDialogViewModel recommendListDialogViewModel;
    private RecyclerView recyclerView;
    private RecyclerAdapterFollowersList recyclerAdapterFollowersList;
    private String listName;
    private View includeMessageForEmptyFollowersDialog;
    private TextView dialogTitle;


    public interface RecommendListDialogListener {
        void onRecommendButtonOnDialogClicked();
    }



    //------------------------------------------------------------- CONSTRUCTORS

    public RecommendListDialogFragment(RecommendListDialogListener listener, String listName) {
        this.listener = listener;
        this.listName = listName;
    }




    //------------------------------------------------------------- ANDROID METHODS

    @NotNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.layout_recommend_list_dialog, null);

        setupView(view);

        builder.setView(view);
        // Create the AlertDialog object and return it
        return builder.create();
    }


    public void setupView(@NonNull View view) {
        initRecycleView(view);
        negativeButton = view.findViewById(R.id.button_recommendDialogLayout_negative);
        negativeButton.setOnClickListener(this);
        includeMessageForEmptyFollowersDialog = view.findViewById(R.id.include_empty_followers_dialog_fragment);
        hideMessageForEmptyFollowersDialog();
        dialogTitle = view.findViewById(R.id.textView_recommendDialogLayout_title);
        loginViewModel = new ViewModelProvider(requireActivity()).get(LoginViewModel.class);
        recommendListDialogViewModel = new ViewModelProvider(this).get(RecommendListDialogViewModel.class);
        recommendListDialogViewModel.getObservableFetchStatus().observe(this, fetchStatus -> {
            switch (fetchStatus) {
                case SUCCESS: {
                    dialogTitle.setVisibility(View.VISIBLE);
                    ArrayList<User> followers = recommendListDialogViewModel.getObservableFollowers().getValue();
                    if(followers!=null)
                        recyclerAdapterFollowersList.loadNewData(followers);
                }
                break;
                case FAILED:
                    dialogTitle.setVisibility(View.GONE);
                    showMessageForEmptyFollowersDialog();
                    break;
            }
        });

        recommendListDialogViewModel.getObservableTaskStatus().observe(this, taskStatus -> {
            switch (taskStatus) {
                case SUCCESS:
                    Toast.makeText(requireContext(), "lista raccomandata", Toast.LENGTH_SHORT).show();
                    dismiss();
                break;
                case FAILED:
                    Toast.makeText(requireContext(), "errore raccomandazione o raccomandazione gia' inoltrata", Toast.LENGTH_SHORT).show();
                    break;
            }
        });
        recommendListDialogViewModel.fetchFollowers(loginViewModel.getLoggedUser());

    }


    @Override
    public void onClick(View v) {
        if(v.getId() == negativeButton.getId()) {
            dismiss();
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }






    //------------------------------------------------------------- MY METHODS

    private void showMessageForEmptyFollowersDialog(){
        includeMessageForEmptyFollowersDialog.setVisibility(View.VISIBLE);
    }

    private void hideMessageForEmptyFollowersDialog(){
        includeMessageForEmptyFollowersDialog.setVisibility(View.GONE);
    }

    private void initRecycleView(View view) {
        // defining Recycler view
        recyclerView = view.findViewById(R.id.recyclerView_recommendDialogLayout);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // adding recycle listener for touch detection
        recyclerAdapterFollowersList = new RecyclerAdapterFollowersList(new ArrayList<>(), getContext(), this);
        recyclerView.setAdapter(recyclerAdapterFollowersList);
    }


    @Override
    public void onFollowerClicked(int position, View v) {
        User selectedUser = recyclerAdapterFollowersList.getItem(position);
        recommendListDialogViewModel.recommendList(listName, selectedUser, loginViewModel.getLoggedUser());
    }



}// end RecommendListDialogFragment class
