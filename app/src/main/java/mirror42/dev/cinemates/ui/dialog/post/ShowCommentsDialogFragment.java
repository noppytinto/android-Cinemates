package mirror42.dev.cinemates.ui.dialog.post;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
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
    private int postPosition;
    private int commentsCount;
    private User reactionOwner;
    AddCommentButtonListener listener;
    private RecyclerView recyclerView;

    public interface AddCommentButtonListener {
        void onAddCommentClicked(String commentText, long postId, int position, int commentsCount);
        void onDeleteCommentClicked(long commentId, int commentPosition, int position, int commentsCount);
    }




    //------------------------------------------------------------------------------- ANDROID METHODS

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
    public void onResume() {
        super.onResume();
        if(getDialog() != null)
            getDialog().getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.view = view;
        buttonComment = view.findViewById(R.id.include_showCommentsDialog).findViewById(R.id.button_commentDialog);
        editTextComment = view.findViewById(R.id.include_showCommentsDialog).findViewById(R.id.editText_commentDialog);
        buttonComment = view.findViewById(R.id.include_showCommentsDialog).findViewById(R.id.button_commentDialog);
        initRecyclerView();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        commentsList = (ArrayList<Comment>) getArguments().getSerializable("commentsList");
        postId = getArguments().getLong("postId");
        postPosition = getArguments().getInt("position");
        commentsCount = getArguments().getInt("commentsCount");
        reactionOwner = (User) getArguments().getSerializable("reactionOwner");

        buttonComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Animation buttonAnim = AnimationUtils.loadAnimation(getContext(), R.anim.push_button_animation);
                buttonComment.startAnimation(buttonAnim);
                String commentText = editTextComment.getText().toString();
                if( ! commentText.isEmpty()) {
                    editTextComment.setText("");
                    editTextComment.clearFocus();
                    Comment newComment = new Comment();
                    newComment.setText(commentText);
                    newComment.setOwner(reactionOwner);
                    newComment.setIsNewItem(true);
//                    newComment.setIsMine(true); //TODO: get new reaction id from db for delete to be allawed
                    recyclerAdapterShowCommentsDialog.addPlaceholderitem(newComment);
                    editTextComment.onEditorAction(EditorInfo.IME_ACTION_DONE); // hide keyboard on search button press

                    listener.onAddCommentClicked(commentText, postId, postPosition, commentsCount);
                    moveRecyclerToBottom();

                    //TODO: get new reaction id from db for delete to be allowed

                    final Toast toast = Toast.makeText(getContext(), "Commento pubblicato.", Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
//                    dismiss();
                }
            }
        });


        recyclerAdapterShowCommentsDialog.loadNewData(commentsList);
        moveRecyclerToBottom();
    }



    @Override
    public void onDetach() {
        super.onDetach();
        reactionOwner = null;
    }



    //------------------------------------------------------------------------------- METHODS

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

    @Override
    public void onDeleteButtonPressed(int commentPosition) {
        new MaterialAlertDialogBuilder(getContext())
                .setTitle("Vuoi eliminare il commento?")
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        final Toast toast = Toast.makeText(getContext(), "Operazione annullata.", Toast.LENGTH_SHORT);
                        toast.setGravity(Gravity.CENTER, 0, 0);
                        toast.show();
                    }
                })
                .setPositiveButton("Si", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Comment currentComment = recyclerAdapterShowCommentsDialog.getComment(commentPosition);
                        recyclerAdapterShowCommentsDialog.removeItem(currentComment);
                        listener.onDeleteCommentClicked(currentComment.getId(), commentPosition, postPosition, commentsCount);
                        dismiss();
                        final Toast toast = Toast.makeText(getContext(), "Commento eliminato.", Toast.LENGTH_SHORT);
                        toast.setGravity(Gravity.CENTER, 0, 0);
                        toast.show();
                    }
                })
        .show();
    }

}// end ShowCommentsDialogFragment class