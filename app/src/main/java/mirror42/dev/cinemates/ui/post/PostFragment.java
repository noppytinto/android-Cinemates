package mirror42.dev.cinemates.ui.post;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import mirror42.dev.cinemates.R;
import mirror42.dev.cinemates.adapter.RecyclerAdapterShowLikesDialog;
import mirror42.dev.cinemates.adapter.ViewPagerAdapterPost;
import mirror42.dev.cinemates.model.Post;
import mirror42.dev.cinemates.model.Post.PostType;
import mirror42.dev.cinemates.model.User;
import mirror42.dev.cinemates.model.WatchlistPost;
import mirror42.dev.cinemates.ui.login.LoginViewModel;
import mirror42.dev.cinemates.utilities.FirebaseAnalytics;

public class PostFragment extends Fragment implements RecyclerAdapterShowLikesDialog.ClickAdapterListener {
    private RecyclerAdapterShowLikesDialog recyclerView;
    private ViewPager2 viewPager;
    private ViewPagerAdapterPost viewPagerAdapter;
    private TabLayout tabLayout;
    private LoginViewModel loginViewModel;
    private PostViewModel postViewModel;
    private View includeCommentBox;




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


        if(getArguments() != null) {
            PostFragmentArgs args = PostFragmentArgs.fromBundle(getArguments());
            long postId = args.getPostId();

            // TODO: getting details from db




            // if post exists
            // - show post details
            // - setup tabs
            // - show reactions
            // else
            // - show error message


            loginViewModel = new ViewModelProvider(requireActivity()).get(LoginViewModel.class);
            postViewModel = new ViewModelProvider(this).get(PostViewModel.class);
            postViewModel.getObservableFetchStatus().observe(getViewLifecycleOwner(), loginResult -> {
                switch (loginResult) {
                    case SUCCESS: {
                        Toast.makeText(getContext(), "post" + postId + " esiste", Toast.LENGTH_SHORT).show();
                        User loggedUser = loginViewModel.getLoggedUser();
                        PostType postType = postViewModel.getFetchedPostType();
                        Post post = postViewModel.getObservablePostFetched().getValue();
                        Bundle arguments = buildRequiredArguments(post);
                        setupFragment(postType, arguments);

                        Bundle arg = new Bundle();
                        arg.putSerializable("comments", post.getComments());
                        arg.putSerializable("likes", post.getLikesOwnersList());

                        int commentsCount = post.getCommentsCount();
                        int likesCount = post.getLikesCount();
                        setupTabs(view, arg, commentsCount, likesCount);

                    }
                    break;
                    case FAILED:
                    case NOT_EXISTS: {
                        Toast.makeText(getContext(), "post " + postId + " NON esiste", Toast.LENGTH_SHORT).show();
                    }
                        break;
                    default: {
                    }
                }
            });
            User loggedUser = loginViewModel.getLoggedUser();
            postViewModel.fetchPost(postId, loggedUser);

        }
    }// end onViewCreated()

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        postViewModel = new ViewModelProvider(this).get(PostViewModel.class);
        // TODO: Use the ViewModel
    }



    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu) {
        super.onPrepareOptionsMenu(menu);
        MenuItem profileMenu = menu.getItem(0);
        profileMenu.setVisible(false);
        MenuItem notificationMenu = menu.getItem(1);
        notificationMenu.setVisible(false);
    }



    //---------------------------------------------------------------------- MY METHODS




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
            default:
        }

        return arguments;
    }

    private void setupFragment(PostType postType, Bundle arguments) {
        switch (postType) {
            case WL: {
                // Create new fragment and transaction
                Fragment watchlistPostFragment = WatchlistPostFragment.newInstance();
                watchlistPostFragment.setArguments(arguments);
                display(watchlistPostFragment);
            }
                break;
            default:
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
        tabLayout = view.findViewById(R.id.tablayout_postFragment);
        viewPager = view.findViewById(R.id.viewPager_postFragment);
        FragmentManager fm = getChildFragmentManager();
        Lifecycle lifecycle = getViewLifecycleOwner().getLifecycle();
        viewPagerAdapter = new ViewPagerAdapterPost(fm, lifecycle, arguments);
        viewPager.setUserInputEnabled(false); // disables horiz. swipe to scroll tabs gestures
        viewPager.setAdapter(viewPagerAdapter);


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




}// end WatchlistPostFragment clasd