package mirror42.dev.cinemates.ui.dialog;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
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

public class RecommendMovieDialogFragment extends DialogFragment implements View.OnClickListener, RecyclerAdapterFollowersList.FollowerListener {
    private Button negativeButton;
    private RecommendMovieDialogListener listener;
    private LoginViewModel loginViewModel;
    private RecommendMovieDialogViewModel recommendMovieDialogViewModel;
    private RecyclerView recyclerView;
    private RecyclerAdapterFollowersList recyclerAdapterFollowersList;
    private int movieId;


    public interface RecommendMovieDialogListener {
        void onRecommendButtonOnDialogClicked();
    }



    //------------------------------------------------------------- CONSTRUCTORS

    public RecommendMovieDialogFragment(RecommendMovieDialogListener listener, int movieId) {
        this.listener = listener;
        this.movieId = movieId;
    }




    //------------------------------------------------------------- ANDROID METHODS

    @NotNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.layout_recommend_movie_dialog, null);

        setupView(view);

        builder.setView(view);
        // Create the AlertDialog object and return it
        return builder.create();
    }


    public void setupView(@NonNull View view) {
        initRecycleView(view);
        negativeButton = view.findViewById(R.id.button_recommendMovieDialogLayout_negative);
        negativeButton.setOnClickListener(this);
        loginViewModel = new ViewModelProvider(requireActivity()).get(LoginViewModel.class);
        recommendMovieDialogViewModel = new ViewModelProvider(this).get(RecommendMovieDialogViewModel.class);
        recommendMovieDialogViewModel.getObservableFetchStatus().observe(this, fetchStatus -> {
            switch (fetchStatus) {
                case SUCCESS: {
                    ArrayList<User> followers = recommendMovieDialogViewModel.getObservableFollowers().getValue();
                    if(followers!=null)
                        recyclerAdapterFollowersList.loadNewData(followers);
                }
                break;
                case FAILED:
                    break;
            }
        });

        recommendMovieDialogViewModel.getObservableTaskStatus().observe(this, taskStatus -> {
            switch (taskStatus) {
                case SUCCESS:
                    Toast.makeText(requireContext(), "film raccomandato!", Toast.LENGTH_SHORT).show();
                    dismiss();
                    break;
                case FAILED:
                    Toast.makeText(requireContext(), "errore raccomandazione o raccomandazione gia' inoltrata", Toast.LENGTH_SHORT).show();
                    break;
            }
        });
        recommendMovieDialogViewModel.fetchFollowers(loginViewModel.getLoggedUser());

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

    private void initRecycleView(View view) {
        // defining Recycler view
        recyclerView = view.findViewById(R.id.recyclerView_recommendMovieDialogLayout);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // adding recycle listener for touch detection
        recyclerAdapterFollowersList = new RecyclerAdapterFollowersList(new ArrayList<>(), getContext(), this);
        recyclerView.setAdapter(recyclerAdapterFollowersList);
    }


    @Override
    public void onFollowerClicked(int position, View v) {
        User selectedUser = recyclerAdapterFollowersList.getItem(position);
        recommendMovieDialogViewModel.recommendMovie(movieId, selectedUser, loginViewModel.getLoggedUser());
    }

}// end RecommendMovieDialogFragment class