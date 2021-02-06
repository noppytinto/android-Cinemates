package mirror42.dev.cinemates.ui.home;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import mirror42.dev.cinemates.MainActivity;
import mirror42.dev.cinemates.NavGraphDirections;
import mirror42.dev.cinemates.R;
import mirror42.dev.cinemates.adapter.RecyclerAdapterPost;
import mirror42.dev.cinemates.model.Like;
import mirror42.dev.cinemates.model.Post;
import mirror42.dev.cinemates.model.User;
import mirror42.dev.cinemates.ui.login.LoginViewModel;
import mirror42.dev.cinemates.ui.notification.NotificationsViewModel;
import mirror42.dev.cinemates.ui.post.PostFragmentDirections;

public class HomeFragment extends Fragment implements
        RecyclerAdapterPost.ReactionsClickAdapterListener{
    private final String TAG = this.getClass().getSimpleName();
    private HomeViewModel homeViewModel;
    private RecyclerAdapterPost recyclerAdapterPost;
    private View view;
    private LoginViewModel loginViewModel;
    private Button buttonUpdateFeed;
    private RecyclerView recyclerView;
    private ProgressDialog progressDialog;
    private NotificationsViewModel notificationsViewModel;
    private ProgressBar spinner;


    //------------------------------------------------------------------------------- ANDROID METHODS

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.view = view;
        buttonUpdateFeed = view.findViewById(R.id.button_homeFragment_updateFeed);
        spinner = view.findViewById(R.id.progressBar_homeFragment);

        initRecyclerView();

    }// end onViewCreated()

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        notificationsViewModel = new ViewModelProvider(requireActivity()).get(NotificationsViewModel.class);

        homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        homeViewModel.getFetchStatus().observe(getViewLifecycleOwner(), fetchStatus -> {
            hideProgressDialog();
            spinner.setVisibility(View.GONE);
            switch (fetchStatus) {
                case SUCCESS: {
                    ArrayList<Post> postsList = homeViewModel.getPostsList().getValue();
                    recyclerAdapterPost.loadNewData(postsList);
                }
                break;
                case NOT_EXISTS:
                    recyclerAdapterPost.loadNewData(null);
                    break;
                case FAILED:
                    break;
            }
        });

        loginViewModel = new ViewModelProvider(requireActivity()).get(LoginViewModel.class);
        loginViewModel.getLoginResult().observe(getViewLifecycleOwner(), loginResult -> {
            switch (loginResult) {
                case SUCCESS:
                case REMEMBER_ME_EXISTS: {
                    buttonUpdateFeed.setVisibility(View.VISIBLE);
                    User loggedUser = loginViewModel.getLiveLoggedUser().getValue();
                    spinner.setVisibility(View.VISIBLE);
                    homeViewModel.fetchPosts(loggedUser.getEmail(), loggedUser.getAccessToken(), loggedUser.getUsername());
//                    checkForNewNotifications(loggedUser);
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

        buttonUpdateFeed.setOnClickListener(v -> {
            // ignore v

            showProgressDialog();

            recyclerAdapterPost.clearList();

            User loggedUser = loginViewModel.getLiveLoggedUser().getValue();
            homeViewModel.fetchPosts(loggedUser.getEmail(), loggedUser.getAccessToken(), loggedUser.getUsername());
            checkForNewNotifications(loggedUser);
        });
    }// end onActivityCreated()

    @Override
    public void onResume() {
        super.onResume();

    }




    //------------------------------------------------------------------------------- METHODS

    private void showProgressDialog() {
        //notes: Declare progressDialog before so you can use .hide() later!
        progressDialog = new ProgressDialog(getContext());
        progressDialog.setMessage("Refresh in corso...");
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.show();
    }

    private void hideProgressDialog() {
        if(progressDialog!=null)
            progressDialog.hide();
    }

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
        String currentLoggedUserEmail = loginViewModel.getLiveLoggedUser().getValue().getEmail();
        TextView likesCounter = recyclerView.getLayoutManager().findViewByPosition(position).findViewById(R.id.button_reactionsLayout_showLikes);
        Button likebutton = recyclerView.getLayoutManager().findViewByPosition(position).findViewById(R.id.button_reactionsLayout_like);

        // updating likes counter
        int currentLikesCounter = Integer.parseInt(likesCounter.getText().toString());
        if(likebutton.isActivated()) { //remove like
            if(currentLikesCounter>0)
                currentLikesCounter = currentLikesCounter - 1;

            homeViewModel.removeLike(postId, currentLoggedUserEmail, loginViewModel.getLiveLoggedUser().getValue().getAccessToken());

            // removing like from current cached list
            ArrayList<Like> currentLikes = currentPost.getLikes();
            if(currentLikes!=null && currentLikes.size()>0) {
                Like placeholderLike = new Like();
                placeholderLike.setOwner(loginViewModel.getLiveLoggedUser().getValue());
                currentLikes.remove(placeholderLike);
            }
        }
        else { //add like
            currentLikesCounter = currentLikesCounter + 1;
            homeViewModel.addLike(postId, loginViewModel.getLiveLoggedUser().getValue().getEmail(), loginViewModel.getLiveLoggedUser().getValue().getAccessToken());

            // adding placehoder like to the current cached list
            Like placeholderLike = new Like();
            User currentUser = loginViewModel.getLiveLoggedUser().getValue();
            placeholderLike.setOwner(currentUser);
            ArrayList<Like> currentLikes = currentPost.getLikes();
            if(currentLikes==null)
                currentLikes = new ArrayList<>();

            currentLikes.add(placeholderLike);
        }

        likesCounter.setText(String.valueOf(currentLikesCounter));
    }

    @Override
    public void onShowLikesClicked(int position) {
        Post currentPost = recyclerAdapterPost.getPost(position);
        int likesCount = currentPost.getLikesCount();
        if(likesCount>0) {
            long postId = currentPost.getPostId();
            int tabToFocus = 1;
            NavGraphDirections.ActionGlobalPostFragment postFragmentDirection =
                    PostFragmentDirections.actionGlobalPostFragment(postId);
            postFragmentDirection.setTabToFocus(tabToFocus);
            NavHostFragment.findNavController(HomeFragment.this).navigate(postFragmentDirection);
        }
    }

    @Override
    public void onCommentButtonClicked(int position) {
        Post currentPost = recyclerAdapterPost.getPost(position);
        long postId = currentPost.getPostId();
        NavGraphDirections.ActionGlobalPostFragment postFragmentDirection =
                PostFragmentDirections.actionGlobalPostFragment(postId);
        NavHostFragment.findNavController(HomeFragment.this).navigate(postFragmentDirection);
    }

    @Override
    public void onShowCommentsClicked(int position) {
        Post currentPost = recyclerAdapterPost.getPost(position);
        int commentsCount = currentPost.getCommentsCount();

        if(commentsCount>0) {
            long postId = currentPost.getPostId();
            NavGraphDirections.ActionGlobalPostFragment postFragmentDirection =
                    PostFragmentDirections.actionGlobalPostFragment(postId);
            NavHostFragment.findNavController(HomeFragment.this).navigate(postFragmentDirection);
        }
    }

//    @Override
//    public void onAddCommentClicked(String commentText, long postId, int position, int commentsCount) {
//        homeViewModel.addComment(
//                postId,
//                commentText,
//                loginViewModel.getLiveLoggedUser().getValue().getEmail(),
//                loginViewModel.getLiveLoggedUser().getValue().getAccessToken());
//
//        // updating comments counter
//        TextView commentsCounter = recyclerView.getLayoutManager()
//                .findViewByPosition(position)
//                .findViewById(R.id.button_reactionsLayout_showComments);
//        commentsCount = commentsCount + 1;
//
//        if(commentsCount==1) {
//            commentsCounter.setText(commentsCount + " commento");
//        }
//        else {
//            commentsCounter.setText(commentsCount + " commenti");
//        }
//    }

//    @Override
//    public void onDeleteCommentClicked(long commentId, int commentPosition, int postPosition, int commentsCount) {
//        try {
//            TextView commentsCounter = recyclerView.getLayoutManager()
//                    .findViewByPosition(postPosition)
//                    .findViewById(R.id.button_reactionsLayout_showComments);
//
//            if(commentsCount>0)
//                commentsCount = commentsCount - 1;
//
//            if(commentsCount==1) {
//                commentsCounter.setText(commentsCount + " commento");
//            }
//            else {
//                commentsCounter.setText(commentsCount + " commenti");
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        homeViewModel.deleteComment(
//                commentId,
//                loginViewModel.getLiveLoggedUser().getValue().getEmail(),
//                loginViewModel.getLiveLoggedUser().getValue().getAccessToken());
//    }

    private void checkForNewNotifications(User loggedUser) {
        if(loggedUser!=null) {
            notificationsViewModel.getNotificationsStatus().observe(getViewLifecycleOwner(), status -> {
                switch (status) {
                    case NOTIFICATIONS_FETCHED: {
                        notificationsViewModel.checkForNewNotifications(getContext());
                    }
                    break;
                    case GOT_NEW_NOTIFICATIONS: {
                        ((MainActivity) getActivity()).activateNotificationsIcon();
                    }
                    break;
                    case NO_NOTIFICATIONS:
//                        deactivateNotificationsIcon();
                        break;
                    case ALL_NOTIFICATIONS_READ:
                        ((MainActivity) getActivity()).deactivateNotificationsIcon();
                        break;
                }
            });
            notificationsViewModel.fetchNotifications(loggedUser, getContext());
        }
    }

//    @Override
//    public void onAddCommentClicked(String commentText, long postId, int position) {
//
//    }
//
//    @Override
//    public void onCommentDeleted(CommentsViewModel.TaskStatus taskStatus) {
//
//    }


}// end HomeFragment class
