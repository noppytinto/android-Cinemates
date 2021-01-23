package mirror42.dev.cinemates.ui.dialog.post;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

import mirror42.dev.cinemates.R;
import mirror42.dev.cinemates.model.Comment;
import mirror42.dev.cinemates.ui.home.HomeViewModel;
import mirror42.dev.cinemates.ui.login.LoginViewModel;


public class ShowCommentsDialogFragment extends DialogFragment implements RecyclerAdapterShowCommentsDialog.ClickAdapterListener {
    private ArrayList<Comment> commentsList;
    private View view;
    private RecyclerAdapterShowCommentsDialog recyclerAdapterShowCommentsDialog;
    private ImageButton buttonComment;
    private TextInputEditText editTextComment;
//    CommentButtonListener listener;
    private Long postId;
    private HomeViewModel homeViewModel;
    private LoginViewModel loginViewModel;


//    public interface CommentButtonListener {
//        public void onCommentButtonClicked(String commentText, long postId);
//    }

//    // Override the Fragment.onAttach() method to instantiate the CommentButtonListener
//    @Override
//    public void onAttach(Context context) {
//        super.onAttach(context);
//        // Verify that the host activity implements the callback interface
//        try {
//            // Instantiate the NoticeDialogListener so we can send events to the host
//            listener = (CommentButtonListener) context;
//        } catch (ClassCastException e) {
//            // The activity doesn't implement the interface, throw exception
//            throw new ClassCastException(getActivity().toString()
//                    + " must implement CommentButtonListener");
//        }
//    }





    public ShowCommentsDialogFragment(ArrayList<Comment> commentsList, long postId, HomeViewModel homeViewModel) {
        this.commentsList = commentsList;
        this.postId = postId;
        this.homeViewModel = homeViewModel;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        homeViewModel = null;
    }

    @NotNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = requireActivity().getLayoutInflater();

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        this.view = inflater.inflate(R.layout.dialog_show_comments, null);
        builder.setView(view);
        // Add action buttons
        return builder.create();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        buttonComment = view.findViewById(R.id.include_showCommentsDialog).findViewById(R.id.button_commentDialog);
        editTextComment = view.findViewById(R.id.include_showCommentsDialog).findViewById(R.id.editText_commentDialog);
        initRecyclerView();
        recyclerAdapterShowCommentsDialog.loadNewData(commentsList);

        loginViewModel = new ViewModelProvider(requireActivity()).get(LoginViewModel.class);


        buttonComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String commentText = editTextComment.getText().toString();
                if( ! commentText.isEmpty()) {
                    homeViewModel.addComment(postId, commentText, loginViewModel.getLoggedUser().getValue().getEmail(), loginViewModel.getLoggedUser().getValue().getAccessToken());
                    Toast.makeText(getContext(), "commento pubblicato, aggiorna.", Toast.LENGTH_SHORT).show();
                    dismiss();
                }
            }
        });

    }

    //------------------------------------------------------- METHODS

    private void initRecyclerView() {
        // defining Recycler view
        RecyclerView recyclerView = view.findViewById(R.id.recyclerView_showCommentsDialog);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // adding recycle listener for touch detection
        recyclerAdapterShowCommentsDialog = new RecyclerAdapterShowCommentsDialog(new ArrayList<>(), getContext(), this);
        recyclerView.setAdapter(recyclerAdapterShowCommentsDialog);
    }


    @Override
    public void onItemClicked(int position) {

    }

}// end ShowCommentsDialogFragment class