package mirror42.dev.cinemates.ui.userprofile;

import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;

import mirror42.dev.cinemates.MainActivity;
import mirror42.dev.cinemates.NavGraphDirections;
import mirror42.dev.cinemates.R;
import mirror42.dev.cinemates.adapter.viewholder.RecyclerAdapterUsersList;
import mirror42.dev.cinemates.model.User;
import mirror42.dev.cinemates.ui.login.LoginViewModel;
import mirror42.dev.cinemates.utilities.ImageUtilities;

public class FollowingFragment extends Fragment implements View.OnClickListener, RecyclerAdapterUsersList.ClickAdapterListener {
    private final String TAG = this.getClass().getSimpleName();
    private FollowingViewModel followingViewModel;
    private LoginViewModel loginViewModel;
    private RecyclerView recyclerView;
    private RecyclerAdapterUsersList recyclerAdapterUsersList;
    private View includeEmptyMessage;
    private ImageView imageViewEmptyMessage;
    private boolean isMyList;

    public static FollowingFragment newInstance() {
        return new FollowingFragment();
    }

    @Override
    public void onResume() {
        super.onResume();
        ((MainActivity)requireActivity()).hideToolbar();

    }

    @Override
    public void onPause() {
        super.onPause();
        ((MainActivity)requireActivity()).showToolbar();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.following_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        init(view);

        if(getArguments() != null) {
            FollowingFragmentArgs args = FollowingFragmentArgs.fromBundle(getArguments());
            String username = args.getTargetUsername();

            if(username!=null || username.isEmpty()) {
                isMyList = username.equals(loginViewModel.getLoggedUser().getUsername());

                if(isMyList) initAsMyList(view);
                else initAsOthersList(view);

                fetchFollowings(username);
            }
        }
    }

    private void initAsMyList(View view) {
        initRecyclerView(view, true);
    }

    private void initAsOthersList(View view) {
        initRecyclerView(view, false);
    }

    private void init(View view) {
        followingViewModel = new ViewModelProvider(requireActivity()).get(FollowingViewModel.class);
        loginViewModel = new ViewModelProvider(requireActivity()).get(LoginViewModel.class);
        imageViewEmptyMessage = view.findViewById(R.id.include_followingFragment_emptyMessage).findViewById(R.id.imageView_emptyMessage);
        includeEmptyMessage = view.findViewById(R.id.include_followingFragment_emptyMessage);
    }


    private void initRecyclerView(View view, boolean showRemoveUserButton) {
        // defining Recycler view
        recyclerView = view.findViewById(R.id.recyclerView_followingFragment);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // adding recycle listener for touch detection
        recyclerAdapterUsersList = new RecyclerAdapterUsersList(new ArrayList<>(), getContext(), this, showRemoveUserButton);
        recyclerView.setAdapter(recyclerAdapterUsersList);
    }


    private void fetchFollowings(String username) {
        followingViewModel.getFetchStatus().observe(getViewLifecycleOwner(), fetchStatus -> {
            recyclerAdapterUsersList.clearList();
            switch (fetchStatus) {
                case FOLLOWING_FETCHED: {
                    ArrayList<User> following = followingViewModel.getObservableFollowing().getValue();
                    if(following!=null || following.size()>0) {
                        recyclerAdapterUsersList.loadNewData(following);
                        hideEmptyMessage();
                    }
                }
                break;
                case NO_FOLLOWING:
                    recyclerAdapterUsersList.clearList();
                    if(isMyList) showMessageForEmptyFollowingPage();
                    else showMessageForEmptyOthersFollowingPage();
                    break;
                case FOLLOWING_FETCH_FAILED:
                    showCenteredToast("impossibile caricare utenti!");
                break;
            }
        });
        followingViewModel.fetchFollowing(username, loginViewModel.getLoggedUser());
    }

    @Override
    public void onClick(View v) {

    }

    @Override
    public void onUserClicked(int position) {
        User itemSelected = recyclerAdapterUsersList.getItem(position);
        String username = itemSelected.getUsername();

        NavGraphDirections.ActionGlobalUserProfileFragment userProfileFragment =
                NavGraphDirections.actionGlobalUserProfileFragment(username);
        NavHostFragment.findNavController(FollowingFragment.this).navigate(userProfileFragment);
    }

    @Override
    public void onRemoveButtonClicked(int position) {
        User targetUser = recyclerAdapterUsersList.getItem(position);
        String targetUsername = targetUser.getUsername();

        followingViewModel.getObservableTaskStatus().observe(getViewLifecycleOwner(), taskStatus -> {
            switch (taskStatus) {
                case FOLLOWING_REMOVED: {
                    recyclerAdapterUsersList.removeItem(targetUser);
                    showCenteredToast("operazione effettuata!");
                }
                break;
                case FOLLOWING_REMOVED_FAIL: {
                    showCenteredToast("operazione annullata!");
                }
                break;
            }
        });

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Smettere di seguire?")
                .setNegativeButton("No", (dialog, which) -> {
                    showCenteredToast("operazione annullata");
                })
                .setPositiveButton("Si", (dialog, which) -> {
                    followingViewModel.removeFollowing(targetUsername, loginViewModel.getLoggedUser());
//                    dialog.dismiss();
                })
                .show();
    }

    public void showCenteredToast(String message) {
        final Toast toast = Toast.makeText(getContext(), message, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    private void showMessageForEmptyFollowingPage(){
        includeEmptyMessage.setVisibility(View.VISIBLE);
        final int EMPTY_MESSAGE_IMAGE = R.drawable.empty_following_list;
        ImageUtilities.loadRectangularImageInto(EMPTY_MESSAGE_IMAGE, imageViewEmptyMessage, requireContext());
    }

    private void showMessageForEmptyOthersFollowingPage(){
        includeEmptyMessage.setVisibility(View.VISIBLE);
        final int EMPTY_MESSAGE_IMAGE = R.drawable.empty_others_following_list;
        ImageUtilities.loadRectangularImageInto(EMPTY_MESSAGE_IMAGE, imageViewEmptyMessage, requireContext());
    }

    private void hideEmptyMessage(){
        includeEmptyMessage.setVisibility(View.GONE);
    }

}