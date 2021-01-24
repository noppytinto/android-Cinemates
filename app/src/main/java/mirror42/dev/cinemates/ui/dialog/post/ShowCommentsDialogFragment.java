package mirror42.dev.cinemates.ui.dialog.post;

import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;

import mirror42.dev.cinemates.R;
import mirror42.dev.cinemates.model.Comment;
import mirror42.dev.cinemates.model.User;


public class ShowCommentsDialogFragment extends DialogFragment implements RecyclerAdapterShowCommentsDialog.ClickAdapterListener {
    private ArrayList<Comment> commentsList;
    private View view;
    private RecyclerAdapterShowCommentsDialog recyclerAdapterShowCommentsDialog;
    private ImageButton buttonComment;
    private TextInputEditText editTextComment;
    private Long postId;
    private int position;
    private int commentsCount;
    private User reactionOwner;
    AddCommentButtonListener listener;
    private RecyclerView recyclerView;

    public interface AddCommentButtonListener {
        public void onAddCommentClicked(String commentText, long postId, int position, int commentsCount);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        reactionOwner = null;
    }

    public ShowCommentsDialogFragment() {
        // Empty constructor is required for DialogFragment
        // Make sure not to add arguments to the constructor
        // Use `newInstance` instead as shown below
    }

    public static ShowCommentsDialogFragment getInstance(User reactionOwner, ArrayList<Comment> commentsList, long postId, int position, int commentsCount) {
        ShowCommentsDialogFragment frag = new ShowCommentsDialogFragment();
        Bundle args = new Bundle();
        args.putSerializable("commentsList", commentsList);
        args.putSerializable("reactionOwner", reactionOwner);
        args.putLong("postId", postId);
        args.putInt("position", position);
        args.putInt("commentsCount", commentsCount);
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
        position = getArguments().getInt("position");
        commentsCount = getArguments().getInt("commentsCount");
        reactionOwner = (User) getArguments().getSerializable("reactionOwner");

        buttonComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String commentText = editTextComment.getText().toString();
                if( ! commentText.isEmpty()) {
                    editTextComment.setText("");
                    editTextComment.clearFocus();
                    Comment newComment = new Comment();
                    newComment.setText(commentText);
                    newComment.setOwner(reactionOwner);
                    newComment.setIsNewItem(true);
                    recyclerAdapterShowCommentsDialog.addItem(newComment);
                    editTextComment.onEditorAction(EditorInfo.IME_ACTION_DONE); // hide keyboard on search button press


                    listener.onAddCommentClicked(commentText, postId, position, commentsCount);
                    moveRecyclerToBottom();

                    final Toast toast = Toast.makeText(getContext(), "commento pubblicato.", Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
//                    dismiss();
                }
            }
        });


        recyclerAdapterShowCommentsDialog.loadNewData(commentsList);
        moveRecyclerToBottom();
    }

    //------------------------------------------------------- METHODS

    private void moveRecyclerToBottom() {
        if(commentsList != null && commentsList.size()>0) {
            recyclerView.smoothScrollToPosition(commentsList.size()-1);
        }
    }

    private void initRecyclerView() {
        // defining Recycler view
        recyclerView = view.findViewById(R.id.recyclerView_showCommentsDialog);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
//        linearLayoutManager.setReverseLayout(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        // adding recycle listener for touch detection
        recyclerAdapterShowCommentsDialog = new RecyclerAdapterShowCommentsDialog(new ArrayList<>(), getContext(), this);
        recyclerView.setAdapter(recyclerAdapterShowCommentsDialog);

    }


    @Override
    public void onItemClicked(int position) {

    }

}// end ShowCommentsDialogFragment class