package mirror42.dev.cinemates.ui.dialog.post;

import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;

import mirror42.dev.cinemates.R;
import mirror42.dev.cinemates.model.Comment;


public class ShowCommentsDialogFragment extends DialogFragment implements RecyclerAdapterShowCommentsDialog.ClickAdapterListener {
    private ArrayList<Comment> commentsList;
    private View view;
    private RecyclerAdapterShowCommentsDialog recyclerAdapterShowCommentsDialog;
    private ImageButton buttonComment;
    private TextInputEditText editTextComment;
    private Long postId;
    AddCommentButtonListener listener;

    public interface AddCommentButtonListener {
        public void onAddCommentClicked(String commentText, long postId);
    }

    public ShowCommentsDialogFragment() {
        // Empty constructor is required for DialogFragment
        // Make sure not to add arguments to the constructor
        // Use `newInstance` instead as shown below
    }

    public static ShowCommentsDialogFragment getInstance(ArrayList<Comment> commentsList, long postId) {
        ShowCommentsDialogFragment frag = new ShowCommentsDialogFragment();
        Bundle args = new Bundle();
        args.putSerializable("commentsList", commentsList);
        args.putLong("postId", postId);
        frag.setArguments(args);
        return frag;
    }

    public void setListener(AddCommentButtonListener listner) {
        this.listener = listner;
    }


    @Override

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_show_comments, container);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.view = view;
        buttonComment = view.findViewById(R.id.include_showCommentsDialog).findViewById(R.id.button_commentDialog);
        editTextComment = view.findViewById(R.id.include_showCommentsDialog).findViewById(R.id.editText_commentDialog);
        initRecyclerView();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        commentsList = (ArrayList<Comment>) getArguments().getSerializable("commentsList");
        postId = getArguments().getLong("postId");

        buttonComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String commentText = editTextComment.getText().toString();
                if( ! commentText.isEmpty()) {
                    listener.onAddCommentClicked(commentText, postId);

                    final Toast toast = Toast.makeText(getContext(), "commento pubblicato, aggiorna.", Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                    dismiss();
                }
            }
        });

        recyclerAdapterShowCommentsDialog.loadNewData(commentsList);

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