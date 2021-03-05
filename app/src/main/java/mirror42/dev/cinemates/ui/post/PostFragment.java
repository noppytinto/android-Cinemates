package mirror42.dev.cinemates.ui.post;

import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import mirror42.dev.cinemates.R;
import mirror42.dev.cinemates.adapter.RecyclerAdapterShowLikesDialog;
import mirror42.dev.cinemates.adapter.ViewPagerAdapterPost;
import mirror42.dev.cinemates.model.CustomListCreatedPost;
import mirror42.dev.cinemates.model.CustomListPost;
import mirror42.dev.cinemates.model.FavoritesPost;
import mirror42.dev.cinemates.model.FollowPost;
import mirror42.dev.cinemates.model.Post;
import mirror42.dev.cinemates.model.Post.PostType;
import mirror42.dev.cinemates.model.User;
import mirror42.dev.cinemates.model.WatchedPost;
import mirror42.dev.cinemates.model.WatchlistPost;
import mirror42.dev.cinemates.ui.login.LoginViewModel;
import mirror42.dev.cinemates.ui.reaction.CommentsViewModel;
import mirror42.dev.cinemates.utilities.FirebaseAnalytics;

public class PostFragment extends Fragment implements
        RecyclerAdapterShowLikesDialog.ClickAdapterListener,
        View.OnClickListener{
    private RecyclerAdapterShowLikesDialog recyclerView;
    private ViewPager2 viewPager;
    private ViewPagerAdapterPost viewPagerAdapter;
    private TabLayout tabLayout;
    private LoginViewModel loginViewModel;
    private PostViewModel postViewModel;
    private View includeCommentBox;
    private int commentsCount;
    private int likesCount;
    private FloatingActionButton buttonPostComment;
    private TextInputEditText editTextCommentText;
    private TextInputLayout textLayout;
    private long postID;
    private CommentsViewModel commentsViewModel;
    private int tabToFocus;


    //---------------------------------------------------------------------- ANDROID METHODS

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_post, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        includeCommentBox = view.findViewById(R.id.include_postFragment_commentBox);
        editTextCommentText = view.findViewById(R.id.include_postFragment_commentBox).findViewById(R.id.editText_commentDialog);
        buttonPostComment = view.findViewById(R.id.include_postFragment_commentBox).findViewById(R.id.button_commentDialog);
        textLayout = view.findViewById(R.id.include_postFragment_commentBox).findViewById(R.id.editTextLayout_commentDialog);

        postViewModel = new ViewModelProvider(this).get(PostViewModel.class);
        loginViewModel = new ViewModelProvider(requireActivity()).get(LoginViewModel.class);
        commentsViewModel = new ViewModelProvider(this).get(CommentsViewModel.class);


        if(getArguments() != null) {
            PostFragmentArgs args = PostFragmentArgs.fromBundle(getArguments());
            postID = args.getPostId();
            tabToFocus = args.getTabToFocus();

            // if post exists
            // - show post details
            // - setup tabs
            // - show reactions
            // else
            // - show error message

            postViewModel.getObservableFetchStatus().observe(getViewLifecycleOwner(), fetchStatus -> {
                switch (fetchStatus) {
                    case SUCCESS: {
//                        Toast.makeText(getContext(), "post " + postID + " esiste", Toast.LENGTH_SHORT).show();
                        Post post = postViewModel.getObservablePostFetched().getValue();
                        PostType postType = post.getPostType();
                        Bundle arguments = buildRequiredArguments(post);
                        setupFragment(postType, arguments);

                        Bundle arg = new Bundle();
                        arg.putSerializable("comments", post.getComments());
                        arg.putSerializable("likes", post.getLikesOwnersList());

                        int commentsCount = post.getCommentsCount();
                        int likesCount = post.getLikesCount();
                        setupTabs(view, arg, commentsCount, likesCount);
                        setupPostCommentButtonListener();

                        commentsViewModel.getObservableTaskStatus().observe(getViewLifecycleOwner(), taskStatus -> {
                            switch (taskStatus) {
                                case COMMENT_DELETED: {
                                    decreaseCommentsCounter();
                                }
                                break;
                                case COMMENT_POSTED: {
                                    editTextCommentText.setText("");
                                    editTextCommentText.clearFocus();
                                    increaseCommentsCounter();
                                }
                                break;
                            }
                        });
                    }
                    break;
                    case FAILED:
                    case NOT_EXISTS: {
                        Toast.makeText(getContext(), "post " + postID + " NON esiste", Toast.LENGTH_SHORT).show();
                    }
                    break;
                    default: {
                    }
                }
            });
            User loggedUser = loginViewModel.getLoggedUser();
            postViewModel.fetchPost(postID, loggedUser);
        }

    }// end onViewCreated()


    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu) {
        super.onPrepareOptionsMenu(menu);
        MenuItem profileMenu = menu.getItem(0);
        profileMenu.setVisible(false);
        MenuItem notificationMenu = menu.getItem(1);
        notificationMenu.setVisible(false);
    }

    @Override
    public void onClick(View v) {

    }





    //---------------------------------------------------------------------- MY METHODS
    private void setupPostCommentButtonListener() {
        buttonPostComment.setOnClickListener(v -> {
            String commentText = editTextCommentText.getText().toString();
            Animation buttonAnim = AnimationUtils.loadAnimation(getContext(), R.anim.push_button_animation);
            buttonPostComment.startAnimation(buttonAnim);

            if( ! commentText.isEmpty()) {
                this.commentsViewModel.postComment(postID, commentText, loginViewModel.getLoggedUser());
                editTextCommentText.onEditorAction(EditorInfo.IME_ACTION_DONE); // hide keyboard on search button press
            }
            else {

            }
        });
    }

    @Override
    public void onItemClicked(int position) {

    }

    private Bundle buildRequiredArguments(Post post) {
        Bundle arguments = new Bundle();
        PostType postType = post.getPostType();

        switch (postType) {
            case WL: {
                // Create new fragment and transaction
                WatchlistPost watchlistPost = (WatchlistPost) post;
                arguments.putSerializable("watchlist_post_data", watchlistPost);
            }
            break;
            case FV: {
                // Create new fragment and transaction
                FavoritesPost favoritesPost = (FavoritesPost) post;
                arguments.putSerializable("favorites_post_data", favoritesPost);
            }
            break;
            case WD: {
                // Create new fragment and transaction
                WatchedPost watchedPost = (WatchedPost) post;
                arguments.putSerializable("watched_post_data", watchedPost);
            }
            break;
            case CL: {
                // Create new fragment and transaction
                CustomListPost customListPost = (CustomListPost) post;
                arguments.putSerializable("custom_list_post_data", customListPost);
            }
            break;
            case CC: {
                // Create new fragment and transaction
                CustomListCreatedPost customListCreatedPost = (CustomListCreatedPost) post;
                arguments.putSerializable("custom_list_created_post_data", customListCreatedPost);
            }
            break;
            case FW: {
                // Create new fragment and transaction
                FollowPost followPost = (FollowPost) post;
                arguments.putSerializable("follow_post_data", followPost);
            }
            break;
        }

        return arguments;
    }

    private void setupFragment(PostType postType, Bundle arguments) {
        switch (postType) {
            case WL: {
                // Create new fragment and transaction
                Fragment postFragment = WatchlistPostFragment.newInstance();
                postFragment.setArguments(arguments);
                display(postFragment);
            }
                break;
            case FV: {
                // Create new fragment and transaction
                Fragment postFragment = FavoritesPostFragment.newInstance();
                postFragment.setArguments(arguments);
                display(postFragment);
            }
            break;
            case WD: {
                // Create new fragment and transaction
                Fragment postFragment = WatchedPostFragment.newInstance();
                postFragment.setArguments(arguments);
                display(postFragment);
            }
            break;
            case CL: {
                // Create new fragment and transaction
                Fragment postFragment = CustomListPostFragment.newInstance();
                postFragment.setArguments(arguments);
                display(postFragment);
            }
            break;
            case CC: {
                // Create new fragment and transaction
                Fragment postFragment = CustomListCreatedPostFragment.newInstance();
                postFragment.setArguments(arguments);
                display(postFragment);
            }
            break;
            case FW: {
                // Create new fragment and transaction
                Fragment postFragment = FollowPostFragment.newInstance();
                postFragment.setArguments(arguments);
                display(postFragment);
            }
            break;
        }
    }

    private void display(Fragment targetFragment) {
        FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();

        // Replace whatever is in the fragment_container view with this fragment,
        // and add the transaction to the back stack if needed
        transaction.replace(R.id.container_postFragment, targetFragment);
        transaction.addToBackStack(null);

        // Commit the transaction
        transaction.commit();
    }

    private void setupTabs(View view, Bundle arguments, int commentsCount, int likesCount) {
        setupTabPages(view, arguments);
        setupTabAppearance(commentsCount, likesCount);
        setupTabListener();
    }

    private void setupTabPages(View view, Bundle arguments) {
        tabLayout = view.findViewById(R.id.tablayout_postFragment);
        viewPager = view.findViewById(R.id.viewPager_postFragment);
        FragmentManager fm = getChildFragmentManager();
        Lifecycle lifecycle = getViewLifecycleOwner().getLifecycle();
        viewPagerAdapter = new ViewPagerAdapterPost(fm, lifecycle, arguments);
        viewPager.setAdapter(viewPagerAdapter);

    }

    private void setupTabAppearance(int commentsCount, int likesCount) {
        this.commentsCount = commentsCount;
        this.likesCount = likesCount;
        TabLayoutMediator tabLayoutMediator = new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setIcon(R.drawable.icon_comment_light_blue);
                    tab.setText(String.valueOf(commentsCount));
                    break;
                case 1:
                    tab.setIcon(R.drawable.icon_like_light_blue);
                    tab.setText(String.valueOf(likesCount));
                    break;
            }
        });
        tabLayoutMediator.attach();
//        if(tabToFocus==1) viewPager.setCurrentItem(1); //TODO: fix items disappearance
    }

    private void setupTabListener() {
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                FirebaseAnalytics firebaseAnalytics = FirebaseAnalytics.getInstance();

                switch (tab.getPosition()) {
                    case 0:
                        includeCommentBox.setVisibility(View.VISIBLE);
                        break;
                    case 1:
                        includeCommentBox.setVisibility(View.GONE);
                        break;
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }

    private void decreaseCommentsCounter() {
        if(commentsCount>0) {
            commentsCount -= 1;
            setupTabAppearance(commentsCount, likesCount);
        }
    }

    private void increaseCommentsCounter() {
        commentsCount += 1;
        setupTabAppearance(commentsCount, likesCount);
    }

    private void showCenteredToast(String msg) {
        final Toast toast = Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

}// end WatchlistPostFragment clasd