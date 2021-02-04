package mirror42.dev.cinemates.ui.reaction;

import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;

import mirror42.dev.cinemates.R;
import mirror42.dev.cinemates.adapter.RecyclerAdapterShowCommentsDialog;
import mirror42.dev.cinemates.model.Comment;
import mirror42.dev.cinemates.ui.login.LoginViewModel;


public class CommentsFragment extends Fragment implements
        RecyclerAdapterShowCommentsDialog.ClickAdapterListener {
    private CommentsViewModel commentsViewModel;
    private ArrayList<Comment> commentsList;
    private RecyclerAdapterShowCommentsDialog recyclerAdapterShowCommentsDialog;
    CommentListener listener;
    private RecyclerView recyclerView;
    private LoginViewModel loginViewModel;

    public interface CommentListener {
        void onAddCommentClicked(String commentText, long postId, int position);
        void onCommentDeleted();
    }




    //------------------------------------------------------------------------------- ANDROID METHODS

    public static CommentsFragment getInstance(Bundle arguments, CommentListener listener) {
        CommentsFragment frag = new CommentsFragment();
        frag.setListener(listener);
        frag.setArguments(arguments);
        return frag;
    }

    public void setListener(CommentListener listner) {
        this.listener = listner;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_comments, container);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initRecyclerView(view);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        commentsList = (ArrayList<Comment>) getArguments().getSerializable("comments");

        loginViewModel = new ViewModelProvider(requireActivity()).get(LoginViewModel.class);
        commentsViewModel = new ViewModelProvider(this).get(CommentsViewModel.class);
        commentsViewModel.getObservableTaskStatus().observe(getViewLifecycleOwner(), taskStatus -> {
            switch (taskStatus) {
                case SUCCESS: {
                    showCenteredToast("commento eliminato");
                    listener.onCommentDeleted();
                }
                    break;
                case FAILED: {
                    showCenteredToast("impossibile eliminare commento");
                }
                    break;
            }
        });

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

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
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
        new MaterialAlertDialogBuilder(getContext())
                .setTitle("Vuoi eliminare il commento?")
                .setNegativeButton("No", (dialog, which) -> {
                    showCenteredToast("operazione annullata");
                })
                .setPositiveButton("Si", (dialog, which) -> {
                    Comment currentComment = recyclerAdapterShowCommentsDialog.getComment(commentPosition);
                    recyclerAdapterShowCommentsDialog.removeItem(currentComment);
                    commentsViewModel.deleteComment(currentComment.getId(), loginViewModel.getLoggedUser());
//                    dialog.dismiss();
                })
        .show();
    }

    private void showCenteredToast(String msg) {
        final Toast toast = Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

}// end ShowCommentsDialogFragment class