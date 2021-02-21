package mirror42.dev.cinemates.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.progressindicator.LinearProgressIndicator;

import java.util.ArrayList;

import mirror42.dev.cinemates.MainActivity;
import mirror42.dev.cinemates.NavGraphDirections;
import mirror42.dev.cinemates.R;
import mirror42.dev.cinemates.adapter.RecyclerAdapterPost;
import mirror42.dev.cinemates.model.CustomListPost;
import mirror42.dev.cinemates.model.FavoritesPost;
import mirror42.dev.cinemates.model.FollowPost;
import mirror42.dev.cinemates.model.Post;
import mirror42.dev.cinemates.model.User;
import mirror42.dev.cinemates.model.WatchedPost;
import mirror42.dev.cinemates.model.WatchlistPost;
import mirror42.dev.cinemates.model.tmdb.Movie;
import mirror42.dev.cinemates.ui.login.LoginViewModel;
import mirror42.dev.cinemates.ui.notification.NotificationsViewModel;
import mirror42.dev.cinemates.ui.post.PostFragmentDirections;
import mirror42.dev.cinemates.ui.search.SearchFragmentDirections;

public class HomeFragment extends Fragment implements
        RecyclerAdapterPost.ReactionsClickAdapterListener, View.OnClickListener {
    private final String TAG = this.getClass().getSimpleName();
    private HomeViewModel homeViewModel;
    private RecyclerAdapterPost recyclerAdapterPost;
    private View view;
    private LoginViewModel loginViewModel;
    private Button updateFeedButton;
    private RecyclerView recyclerView;
    private NotificationsViewModel notificationsViewModel;
    private LinearProgressIndicator progressIndicator;
    private View welcomeMessage;
    private Button signUpButton;



    //------------------------------------------------------------------------------- ANDROID METHODS



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.view = view;
        updateFeedButton = view.findViewById(R.id.button_homeFragment_updateFeed);
        progressIndicator = view.findViewById(R.id.progressIndicator_homeFragment);
        welcomeMessage = view.findViewById(R.id.include_homeFragment);
        signUpButton = view.findViewById(R.id.include_homeFragment).findViewById(R.id.button_homeMessagePost_signUp);
        signUpButton.setOnClickListener(this);
        updateFeedButton.setOnClickListener(this);

        initRecyclerView();

    }// end onViewCreated()

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        notificationsViewModel = new ViewModelProvider(requireActivity()).get(NotificationsViewModel.class);

        homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        homeViewModel.getFetchStatus().observe(getViewLifecycleOwner(), fetchStatus -> {
            switch (fetchStatus) {
                case SUCCESS: {
                    hideProgressIndicator();
                    ArrayList<Post> postsList = homeViewModel.getPostsList().getValue();
                    recyclerAdapterPost.loadNewData(postsList);
//                    homeViewModel.resetFetchStatus();

                }
                break;
                case EMPTY:
                    hideProgressIndicator();
                    recyclerAdapterPost.loadNewData(null);
                    break;
                case FAILED:
//                    hideProgressIndicator();
                    break;
            }
        });

        loginViewModel = new ViewModelProvider(requireActivity()).get(LoginViewModel.class);
        loginViewModel.getObservableLoginResult().observe(getViewLifecycleOwner(), loginResult -> {
            switch (loginResult) {
                case SUCCESS:
                case REMEMBER_ME_EXISTS: {
                    showProgressIndicator();
                    updateFeedButton.setVisibility(View.VISIBLE);
                    progressIndicator.setVisibility(View.VISIBLE);
                    fetchPosts();
//                    checkForNewNotifications(loginViewModel.getLoggedUser());
                    welcomeMessage.setVisibility(View.GONE);
                }
                break;
                case LOGGED_OUT:
                    updateFeedButton.setVisibility(View.GONE);
                    recyclerAdapterPost.clearList();
                    hideProgressIndicator();
                    welcomeMessage.setVisibility(View.VISIBLE);
                    break;
                case FAILED:
                    break;
                default:
                    updateFeedButton.setVisibility(View.GONE);
                    welcomeMessage.setVisibility(View.VISIBLE);
                    hideProgressIndicator();
            }
        });


    }// end onActivityCreated()




    //------------------------------------------------------------------------------- METHODS

    private void fetchPosts() {
            try {
                User loggedUser = loginViewModel.getLoggedUser();
                homeViewModel.fetchLimitedPosts(1, loggedUser);
            } catch (Exception e) {
                e.printStackTrace();
            }
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
        String currentLoggedUserEmail = loginViewModel.getLoggedUser().getEmail();
        TextView likesCounter = recyclerView.getLayoutManager().findViewByPosition(position).findViewById(R.id.button_reactionsLayout_showLikes);

        // updating likes counter
        int currentLikesCounter = currentPost.getLikesCount();
        if(currentPost.isLikedByMe()) {
            if(currentLikesCounter>0)
                currentLikesCounter = currentLikesCounter - 1;
            homeViewModel.removeLike(postId, currentLoggedUserEmail, loginViewModel.getLoggedUser().getAccessToken());
            currentPost.setIsLikedByMe(false);
        }
        else { //add like
            currentPost.setIsLikedByMe(true);
            currentLikesCounter = currentLikesCounter + 1;
            homeViewModel.addLike(postId, loginViewModel.getLoggedUser().getEmail(), loginViewModel.getLoggedUser().getAccessToken());
        }

        currentPost.setLikesCount(currentLikesCounter);
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

    @Override
    public void onPostContentClicked(int position) {
        Post currentPost = (Post) recyclerAdapterPost.getPost(position);

        switch (currentPost.getPostType()) {
            case WL: {
                WatchlistPost post = (WatchlistPost) currentPost;
                Movie movie = post.getMovie();
                NavGraphDirections.AnywhereToMovieDetailsFragment movieDetailsFragment = SearchFragmentDirections.anywhereToMovieDetailsFragment(movie);
                NavHostFragment.findNavController(HomeFragment.this).navigate(movieDetailsFragment);
            }
            break;
            case FV: {
                FavoritesPost post = (FavoritesPost) currentPost;
                Movie movie = post.getMovie();
                NavGraphDirections.AnywhereToMovieDetailsFragment movieDetailsFragment = SearchFragmentDirections.anywhereToMovieDetailsFragment(movie);
                NavHostFragment.findNavController(HomeFragment.this).navigate(movieDetailsFragment);
            }
            break;
            case WD: {
                WatchedPost post = (WatchedPost) currentPost;
                Movie movie = post.getMovie();
                NavGraphDirections.AnywhereToMovieDetailsFragment movieDetailsFragment = SearchFragmentDirections.anywhereToMovieDetailsFragment(movie);
                NavHostFragment.findNavController(HomeFragment.this).navigate(movieDetailsFragment);
            }
            break;
            case CL: {
                CustomListPost post = (CustomListPost) currentPost;
                Movie movie = post.getMovie();
                NavGraphDirections.AnywhereToMovieDetailsFragment movieDetailsFragment = SearchFragmentDirections.anywhereToMovieDetailsFragment(movie);
                NavHostFragment.findNavController(HomeFragment.this).navigate(movieDetailsFragment);
            }
            break;
        }

    }

    @Override
    public void onCustomListCreatedPostClicked(int position) {

    }

    @Override
    public void onFollowPostClicked(int position) {
        FollowPost itemSelected = (FollowPost) recyclerAdapterPost.getPost(position);

        NavGraphDirections.ActionGlobalUserProfileFragment userProfileFragment =
                NavGraphDirections.actionGlobalUserProfileFragment(itemSelected.getFollowed().getUsername());
        NavHostFragment.findNavController(HomeFragment.this).navigate(userProfileFragment);

    }

    @Override
    public void onPostOwnerAreaClicked(int position) {
        Post itemSelected = recyclerAdapterPost.getPost(position);
        String postOwnerUsername = itemSelected.getOwner().getUsername();
        String currentLoggedUsername = loginViewModel.getLoggedUser().getUsername();

        if( ! postOwnerUsername.equals(currentLoggedUsername)) {
            NavGraphDirections.ActionGlobalUserProfileFragment userProfileFragment =
                    NavGraphDirections.actionGlobalUserProfileFragment(postOwnerUsername);
            NavHostFragment.findNavController(HomeFragment.this).navigate(userProfileFragment);
        }
    }

    private void checkForNewNotifications(User loggedUser) {
        if(loggedUser!=null) {
            notificationsViewModel.getNotificationsStatus().observe(getViewLifecycleOwner(), status -> {
                switch (status) {
                    case NOTIFICATIONS_FETCHED: {
                        notificationsViewModel.checkForNewNotifications(getContext());
                    }
                    break;
                    case GOT_NEW_NOTIFICATIONS: {
                        ((MainActivity) requireActivity()).activateNotificationsIcon();
                    }
                    break;
                    case NO_NOTIFICATIONS:
//                        deactivateNotificationsIcon();
                        break;
                    case ALL_NOTIFICATIONS_READ:
                        ((MainActivity) requireActivity()).deactivateNotificationsIcon();
                        break;
                }
            });
            try {
                notificationsViewModel.fetchNotifications(loggedUser, getContext());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void showProgressIndicator() {
        progressIndicator.setVisibility(View.VISIBLE);
    }

    private void hideProgressIndicator() {
        progressIndicator.setVisibility(View.GONE);
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == signUpButton.getId()) {
            Navigation.findNavController(v).navigate(R.id.action_main_fragment_to_signUpFragment);
        }
        else if(v.getId() == updateFeedButton.getId()) {
            // ignore v
            showProgressIndicator();
            recyclerAdapterPost.clearList();

            User loggedUser = loginViewModel.getObservableLoggedUser().getValue();
            progressIndicator.setVisibility(View.VISIBLE);
            fetchPosts();
            checkForNewNotifications(loggedUser);
        }
    }

}// end HomeFragment class
