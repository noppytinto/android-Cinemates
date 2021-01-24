package mirror42.dev.cinemates.ui.home;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import mirror42.dev.cinemates.R;
import mirror42.dev.cinemates.model.Comment;
import mirror42.dev.cinemates.model.Post;
import mirror42.dev.cinemates.model.User;
import mirror42.dev.cinemates.ui.dialog.post.ShowCommentsDialogFragment;
import mirror42.dev.cinemates.ui.dialog.post.ShowLikesDialogFragment;
import mirror42.dev.cinemates.ui.home.post.RecyclerAdapterPost;
import mirror42.dev.cinemates.ui.login.LoginViewModel;

public class HomeFragment extends Fragment implements
        RecyclerAdapterPost.ReactionsClickAdapterListener,
        ShowCommentsDialogFragment.AddCommentButtonListener {
    private final String TAG = this.getClass().getSimpleName();
    private HomeViewModel homeViewModel;
    private RecyclerAdapterPost recyclerAdapterPost;
    private View view;
    private LoginViewModel loginViewModel;
    private Button buttonUpdateFeed;
    private RecyclerView recyclerView;



    //------------------------------------------------------------------------------- ANDROID METHODS

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate() called");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView() called");
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(TAG, "onViewCreated() called");
        this.view = view;
        buttonUpdateFeed = view.findViewById(R.id.button_homeFragment_updateFeed);

        initRecyclerView();

    }// end onViewCreated()

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        homeViewModel.getFetchStatus().observe(getViewLifecycleOwner(), fetchStatus -> {
            switch (fetchStatus) {
                case SUCCESS: {
                    ArrayList<Post> postsList = homeViewModel.getPostsList().getValue();
                    recyclerAdapterPost.loadNewData(postsList);
                }
                    break;
                case EMPTY:
                    recyclerAdapterPost.loadNewData(null);
                    break;
                case FAILED:
                    break;
            }
        });


        loginViewModel = new ViewModelProvider(requireActivity()).get(LoginViewModel.class);
        loginViewModel.getLoginResult().observe(getViewLifecycleOwner(), loginResult -> {
            switch (loginResult) {
                case SUCCESS: case REMEMBER_ME_EXISTS: {
                    buttonUpdateFeed.setVisibility(View.VISIBLE);
                    User loggedUser = loginViewModel.getLoggedUser().getValue();
                    homeViewModel.fetchData(loggedUser.getEmail(), loggedUser.getAccessToken());
                }
                    break;
                case LOGGED_OUT:
                    buttonUpdateFeed.setVisibility(View.GONE);
                    recyclerAdapterPost.loadNewData(null);
                    break;
                case FAILED:
                    break;
                default:
                    buttonUpdateFeed.setVisibility(View.GONE);
            }
        });


        buttonUpdateFeed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                User loggedUser = loginViewModel.getLoggedUser().getValue();
                homeViewModel.fetchData(loggedUser.getEmail(), loggedUser.getAccessToken());
            }
        });
    }// end onActivityCreated()




    //------------------------------------------------------------------------------- METHODS

    private void initRecyclerView() {
        // defining Recycler view
        recyclerView = view.findViewById(R.id.recyclerView_homeFragment);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // adding recycle listener for touch detection
        recyclerAdapterPost = new RecyclerAdapterPost(new ArrayList<>(), getContext(), this);
        recyclerView.setAdapter(recyclerAdapterPost);
    }


    @Override
    public void onLikeButtonClicked(int position) {
//        recyclerAdapterPost.updateLikeCounter(position);
        Post currentPost = recyclerAdapterPost.getPost(position);
        long postId = currentPost.getPostId();
        String currentLoggedUserEmail = loginViewModel.getLoggedUser().getValue().getEmail();
        TextView likesCounter = recyclerView.getLayoutManager().findViewByPosition(position).findViewById(R.id.button_reactionsLayout_showLikes);
        Button likebutton = recyclerView.getLayoutManager().findViewByPosition(position).findViewById(R.id.button_reactionsLayout_like);

        // updating likes counter
        int currentLikesCounter = Integer.parseInt(likesCounter.getText().toString());
        if(likebutton.isActivated()) {
            if(currentLikesCounter>0) {
                currentLikesCounter = currentLikesCounter - 1;
            }
            homeViewModel.removeLike(postId, currentLoggedUserEmail, loginViewModel.getLoggedUser().getValue().getAccessToken());
        }
        else {
            currentLikesCounter = currentLikesCounter + 1;
            homeViewModel.addLike(postId, loginViewModel.getLoggedUser().getValue().getEmail(), loginViewModel.getLoggedUser().getValue().getAccessToken());
        }

        likesCounter.setText(String.valueOf(currentLikesCounter));
//        Toast.makeText(getContext(), String.valueOf(currentPost.getPostId()), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onShowLikesClicked(int position) {
        Post currentPost = recyclerAdapterPost.getPost(position);
        ArrayList<User> users = currentPost.getLikesOwnersList();

        DialogFragment newFragment = new ShowLikesDialogFragment(users);
        newFragment.show(getActivity().getSupportFragmentManager(), "ShowLikesDialogFragment");
    }

    @Override
    public void onCommentButtonClicked(int position) {
        Post currentPost = recyclerAdapterPost.getPost(position);
        long postId = currentPost.getPostId();
        ArrayList<Comment> comments = currentPost.getComments();
        int currentCommentsCount = currentPost.getCommentsCount();

        FragmentManager fm = getActivity().getSupportFragmentManager();
        ShowCommentsDialogFragment showCommentsDialogFragment = ShowCommentsDialogFragment.getInstance(comments, postId, position, currentCommentsCount);
        showCommentsDialogFragment.setListener(this);
        showCommentsDialogFragment.show(fm, "showCommentsDialogFragment");
    }

    @Override
    public void onShowCommentsClicked(int position) {
        Post currentPost = recyclerAdapterPost.getPost(position);
        long postId = currentPost.getPostId();
        ArrayList<Comment> comments = currentPost.getComments();
        int currentCommentsCount = currentPost.getCommentsCount();

        FragmentManager fm = getActivity().getSupportFragmentManager();
        ShowCommentsDialogFragment showCommentsDialogFragment = ShowCommentsDialogFragment.getInstance(comments, postId, position, currentCommentsCount);
        showCommentsDialogFragment.setListener(this);
        showCommentsDialogFragment.show(fm, "showCommentsDialogFragment");
    }

    @Override
    public void onAddCommentClicked(String commentText, long postId, int position, int commentsCount) {
        homeViewModel.addComment(
                postId,
                commentText,
                loginViewModel.getLoggedUser().getValue().getEmail(),
                loginViewModel.getLoggedUser().getValue().getAccessToken());

        // updating comments counter
        TextView commentsCounter = recyclerView.getLayoutManager()
                .findViewByPosition(position)
                .findViewById(R.id.button_reactionsLayout_showComments);
        commentsCount = commentsCount + 1;

        if(commentsCount==1) {
            commentsCounter.setText(commentsCount + " commento");
        }
        else {
            commentsCounter.setText(commentsCount + " commenti");
        }
    }
}// end HomeFragment class
