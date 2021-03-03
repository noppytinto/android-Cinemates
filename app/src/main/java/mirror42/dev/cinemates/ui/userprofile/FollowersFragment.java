package mirror42.dev.cinemates.ui.userprofile;

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

public class FollowersFragment extends Fragment implements View.OnClickListener, RecyclerAdapterUsersList.ClickAdapterListener {
    private final String TAG = this.getClass().getSimpleName();
    private FollowersViewModel followersViewModel;
    private LoginViewModel loginViewModel;
    private RecyclerView recyclerView;
    private RecyclerAdapterUsersList recyclerAdapterUsersList;
    private int personalFollowerCounter;
    private boolean modifyPersonalFollowerCounterAllowed;

    public static FollowersFragment newInstance() {
        return new FollowersFragment();
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
        return inflater.inflate(R.layout.followers_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        followersViewModel = new ViewModelProvider(this).get(FollowersViewModel.class);
        loginViewModel = new ViewModelProvider(requireActivity()).get(LoginViewModel.class);


        if(getArguments() != null) {
            FollowersFragmentArgs args = FollowersFragmentArgs.fromBundle(getArguments());
            String username = args.getTargetUsername();

            if(username!=null || username.isEmpty()) {
                if(username.equals(loginViewModel.getLoggedUser().getUsername()))
                    initRecyclerView(view, true);
                else
                    initRecyclerView(view, false);

                fetchFollowers(username);
            }
        }
    }

    public int getPersonalFollowerCounter() {
        return personalFollowerCounter;
    }

    public void setPersonalFollowerCounter(int personalFollowerCounter) {
        this.personalFollowerCounter = personalFollowerCounter;
    }

    public boolean isModifyPersonalFollowerCounterAllowed() {
        return modifyPersonalFollowerCounterAllowed;
    }

    public void setModifyPersonalFollowerCounterAllowed(boolean modifyPersonalFollowerCounterAllowed) {
        this.modifyPersonalFollowerCounterAllowed = modifyPersonalFollowerCounterAllowed;
    }

    private void initRecyclerView(View view, boolean showRemoveUserButton) {
        // defining Recycler view
        recyclerView = view.findViewById(R.id.recyclerView_followersFragment);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // adding recycle listener for touch detection
        recyclerAdapterUsersList = new RecyclerAdapterUsersList(new ArrayList<>(), getContext(), this, showRemoveUserButton);
        recyclerView.setAdapter(recyclerAdapterUsersList);
    }


    private void fetchFollowers(String username) {
        followersViewModel.getFetchStatus().observe(getViewLifecycleOwner(), fetchStatus -> {
            switch (fetchStatus) {
                case FOLLOWERS_FETCHED: {
                    ArrayList<User> followers = followersViewModel.getObservableFollowers().getValue();
                    if(followers!=null || followers.size()>0) {
                        recyclerAdapterUsersList.loadNewData(followers);
                    }
                }
                break;
                case NO_FOLLOWERS:
                    showCenteredToast("lista vuota");
                break;
                case FOLLOWERS_FETCH_FAILED:
                    showCenteredToast("impossibile caricare utenti!");
                break;
            }
        });
        followersViewModel.fetchFollowers(username, loginViewModel.getLoggedUser());
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
        NavHostFragment.findNavController(FollowersFragment.this).navigate(userProfileFragment);
    }

    @Override
    public void onRemoveButtonClicked(int position) {
        User targetUser = recyclerAdapterUsersList.getItem(position);
        String targetUsername = targetUser.getUsername();
        followersViewModel.getObservableTaskStatus().observe(getViewLifecycleOwner(), taskStatus -> {
            switch (taskStatus) {
                case FOLLOWER_REMOVED: {
                    recyclerAdapterUsersList.removeItem(targetUser);
                    showCenteredToast("operazione effettuata!");
                }
                break;
                case FOLLOWER_REMOVED_FAIL: {
                    showCenteredToast("operazione annullata!");
                }
                break;
            }
        });

        new MaterialAlertDialogBuilder(getContext())
                .setTitle("Vuoi rimuovere follower?")
                .setNegativeButton("No", (dialog, which) -> {
                    showCenteredToast("operazione annullata");
                })
                .setPositiveButton("Si", (dialog, which) -> {
                    followersViewModel.removeFollower(targetUsername, loginViewModel.getLoggedUser());
//                    dialog.dismiss();
                })
                .show();


    }

    public void showCenteredToast(String message) {
        final Toast toast = Toast.makeText(getContext(), message, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    public void fetchFollowerCount() {

    }



}// end FollowersFragment class