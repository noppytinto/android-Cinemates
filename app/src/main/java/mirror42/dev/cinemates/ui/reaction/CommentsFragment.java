package mirror42.dev.cinemates.ui.reaction;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import mirror42.dev.cinemates.R;
import mirror42.dev.cinemates.adapter.RecyclerAdapterShowCommentsDialog;
import mirror42.dev.cinemates.model.Comment;


public class CommentsFragment extends Fragment implements
        RecyclerAdapterShowCommentsDialog.ClickAdapterListener {
    private ArrayList<Comment> commentsList;
    private RecyclerAdapterShowCommentsDialog recyclerAdapterShowCommentsDialog;
    AddCommentButtonListener listener;
    private RecyclerView recyclerView;

    public interface AddCommentButtonListener {
        void onAddCommentClicked(String commentText, long postId, int position, int commentsCount);
        void onDeleteCommentClicked(long commentId, int commentPosition, int position, int commentsCount);
    }




    //------------------------------------------------------------------------------- ANDROID METHODS

    public static CommentsFragment getInstance(Bundle arguments) {
        CommentsFragment frag = new CommentsFragment();
        frag.setArguments(arguments);
        return frag;
    }

    public void setListener(AddCommentButtonListener listner) {
        this.listener = listner;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_comments, container);
    }

//    @Override
//    public void onResume() {
//        super.onResume();
//        if(getDialog() != null)
//            getDialog().getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
//    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initRecyclerView(view);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        commentsList = (ArrayList<Comment>) getArguments().getSerializable("comments");

//        buttonComment.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Animation buttonAnim = AnimationUtils.loadAnimation(getContext(), R.anim.push_button_animation);
//                buttonComment.startAnimation(buttonAnim);
//                String commentText = editTextComment.getText().toString();
//                if( ! commentText.isEmpty()) {
//                    editTextComment.setText("");
//                    editTextComment.clearFocus();
//                    Comment newComment = new Comment();
//                    newComment.setText(commentText);
//                    newComment.setOwner(reactionOwner);
//                    newComment.setIsNewItem(true);
////                    newComment.setIsMine(true); //TODO: get new reaction id from db for delete to be allawed
//                    recyclerAdapterShowCommentsDialog.addPlaceholderitem(newComment);
//                    editTextComment.onEditorAction(EditorInfo.IME_ACTION_DONE); // hide keyboard on search button press
//
//                    listener.onAddCommentClicked(commentText, postId, postPosition, commentsCount);
//                    moveRecyclerToBottom();
//
//                    //TODO: get new reaction id from db for delete to be allowed
//
//                    final Toast toast = Toast.makeText(getContext(), "Commento pubblicato.", Toast.LENGTH_SHORT);
//                    toast.setGravity(Gravity.CENTER, 0, 0);
//                    toast.show();
////                    dismiss();
//                }
//            }
//        });


        recyclerAdapterShowCommentsDialog.loadNewData(commentsList);
        moveRecyclerToBottom();
    }



    //------------------------------------------------------------------------------- METHODS

    private void moveRecyclerToBottom() {
        if(commentsList != null && commentsList.size()>0) {
            recyclerView.smoothScrollToPosition(commentsList.size()-1);
        }
    }

    private void initRecyclerView(View view) {
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
//        new MaterialAlertDialogBuilder(getContext())
//                .setTitle("Vuoi eliminare il commento?")
//                .setNegativeButton("No", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        final Toast toast = Toast.makeText(getContext(), "Operazione annullata.", Toast.LENGTH_SHORT);
//                        toast.setGravity(Gravity.CENTER, 0, 0);
//                        toast.show();
//                    }
//                })
//                .setPositiveButton("Si", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        Comment currentComment = recyclerAdapterShowCommentsDialog.getComment(commentPosition);
//                        recyclerAdapterShowCommentsDialog.removeItem(currentComment);
//                        listener.onDeleteCommentClicked(currentComment.getId(), commentPosition, postPosition, commentsCount);
//                        dismiss();
//                        final Toast toast = Toast.makeText(getContext(), "Commento eliminato.", Toast.LENGTH_SHORT);
//                        toast.setGravity(Gravity.CENTER, 0, 0);
//                        toast.show();
//                    }
//                })
//        .show();
    }

}// end ShowCommentsDialogFragment class