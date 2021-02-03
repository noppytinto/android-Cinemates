package mirror42.dev.cinemates.ui.home.post;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import mirror42.dev.cinemates.MainActivity;
import mirror42.dev.cinemates.R;
import mirror42.dev.cinemates.adapter.RecyclerAdapterShowLikesDialog;
import mirror42.dev.cinemates.adapter.ViewPagerAdapterPost;
import mirror42.dev.cinemates.model.User;
import mirror42.dev.cinemates.ui.login.LoginViewModel;

public class PostFragment extends Fragment implements RecyclerAdapterShowLikesDialog.ClickAdapterListener {
    private RecyclerAdapterShowLikesDialog recyclerView;
    private ViewPager2 viewPager;
    private ViewPagerAdapterPost viewPagerAdapter;
    private TabLayout tabLayout;
    private LoginViewModel loginViewModel;
    private PostViewModel postViewModel;




    //---------------------------------------------------------------------- ANDROID METHODS


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        ((MainActivity) getActivity()).hideToolbar();
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
            postViewModel = new ViewModelProvider(requireActivity()).get(PostViewModel.class);
            postViewModel.getObservableFetchStatus().observe(getViewLifecycleOwner(), loginResult -> {
                switch (loginResult) {
                    case SUCCESS: {
                        Toast.makeText(getContext(), "post" + postId + " esiste", Toast.LENGTH_SHORT).show();
                        setupTabs(view);



                    }
                    break;
                    default: {
                        Toast.makeText(getContext(), "post" + postId + " NON esiste", Toast.LENGTH_SHORT).show();
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





    //---------------------------------------------------------------------- MY METHODS


    @Override
    public void onItemClicked(int position) {

    }


    private void setupTabs(View view) {
        tabLayout = view.findViewById(R.id.tablayout_postFragment);
        viewPager = view.findViewById(R.id.viewPager_postFragment);
        FragmentManager fm = getChildFragmentManager();
        Lifecycle lifecycle = getViewLifecycleOwner().getLifecycle();
        viewPagerAdapter = new ViewPagerAdapterPost(fm, lifecycle);
        viewPager.setUserInputEnabled(false); // disables horiz. swipe to scroll tabs gestures
        viewPager.setAdapter(viewPagerAdapter);


        TabLayoutMediator tabLayoutMediator = new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setIcon(R.drawable.icon_comment_light_blue);
                    tab.setText("10");
                    break;
                case 1:
                    tab.setIcon(R.drawable.icon_like_light_blue);
                    tab.setText("4");
                    break;
            }
        });
        tabLayoutMediator.attach();
    }




}// end WatchlistPostFragment clasd