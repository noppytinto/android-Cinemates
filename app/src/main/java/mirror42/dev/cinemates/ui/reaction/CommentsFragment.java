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
import mirror42.dev.cinemates.adapter.RecyclerAdapterCommentsList;
import mirror42.dev.cinemates.model.Comment;
import mirror42.dev.cinemates.ui.login.LoginViewModel;


public class CommentsFragment extends Fragment implements
        RecyclerAdapterCommentsList.ClickAdapterListener{
    private final String TAG = this.getClass().getSimpleName();
    private CommentsViewModel commentsViewModel;
    private ArrayList<Comment> commentsList;
    private RecyclerAdapterCommentsList recyclerAdapterCommentsList;
    private RecyclerView recyclerView;
    private LoginViewModel loginViewModel;
    private String commentText;







    //------------------------------------------------------------------------------- ANDROID METHODS

    public static CommentsFragment getInstance(Bundle arguments) {
        CommentsFragment frag = new CommentsFragment();
        frag.setArguments(arguments);
        return frag;
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
        commentsViewModel = new ViewModelProvider(requireParentFragment()).get(CommentsViewModel.class);
        commentsViewModel.getObservableTaskStatus().observe(getViewLifecycleOwner(), taskStatus -> {
            switch (taskStatus) {
                case COMMENT_DELETED: {
                    showCenteredToast("commento eliminato");
                }
                break;
                case COMMENT_NOT_DELETED: {
                    showCenteredToast("impossibile eliminare commento");
                }
                break;
                case COMMENT_POSTED: {
                    showCenteredToast("commento pubblicato");
                    Comment newComment = new Comment();
                    newComment.setText(commentsViewModel.getCommentText());
                    newComment.setOwner(loginViewModel.getLoggedUser());
                    newComment.setIsNewItem(true);
                    long currentTimeMillis = System.currentTimeMillis();
                    newComment.setPublishDateMillis(currentTimeMillis);
//                    newComment.setIsMine(true); //TODO: get new reaction id from db for delete to be allawed
                    recyclerAdapterCommentsList.addPlaceholderComment(newComment);
                    moveRecyclerToBottom();
                }
                break;
                case COMMNET_NOT_POSTED: {
                    showCenteredToast("impossibile pubblicare commento");
                }
                break;
            }
        });



        recyclerAdapterCommentsList.loadNewData(commentsList);
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
        recyclerAdapterCommentsList = new RecyclerAdapterCommentsList(new ArrayList<>(), getContext(), this);
        recyclerView.setAdapter(recyclerAdapterCommentsList);
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
                    Comment currentComment = recyclerAdapterCommentsList.getComment(commentPosition);
                    recyclerAdapterCommentsList.removeItem(currentComment);
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