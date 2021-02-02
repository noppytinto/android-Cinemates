package mirror42.dev.cinemates.ui.notification;

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
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.NavDirections;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.util.ArrayList;

import mirror42.dev.cinemates.NavGraphDirections;
import mirror42.dev.cinemates.R;
import mirror42.dev.cinemates.adapter.RecyclerAdapterNotifications;
import mirror42.dev.cinemates.model.User;
import mirror42.dev.cinemates.model.notification.FollowRequestNotification;
import mirror42.dev.cinemates.model.notification.Notification;
import mirror42.dev.cinemates.ui.login.LoginViewModel;

public class NotificationsFragment extends Fragment implements
        RecyclerAdapterNotifications.OnNotificationClickedListener {
    private final String TAG = this.getClass().getSimpleName();
    private NotificationsViewModel notificationsViewModel;
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerAdapterNotifications recyclerAdapterNotifications;
    private RecyclerView recyclerView;
    private LoginViewModel loginViewModel;




    //--------------------------------------------------------------------------------------- ANDROID METHODS

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.notifications_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout_notificationsFragment);
        initRecyclerView(view);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        loginViewModel = new ViewModelProvider(requireActivity()).get(LoginViewModel.class);
        notificationsViewModel = new ViewModelProvider(requireActivity()).get(NotificationsViewModel.class);
        notificationsViewModel.getNotificationsStatus().observe(getViewLifecycleOwner(), notificationsStatust -> {
            ArrayList<Notification> notifications = notificationsViewModel.getNotifications().getValue();

            switch (notificationsStatust) {
                case NOTIFICATIONS_FETCHED: {
                    if(notifications!=null) {
                        updateUI(notifications);
                        notificationsViewModel.setNotificationsAsOld(notifications, getContext());
                    }
                }
                    break;
                case GOT_NEW_NOTIFICATIONS: {

                }
                    break;
                case NO_NOTIFICATIONS:
                    break;
                case ALL_NOTIFICATIONS_READ:
                    break;
            }

        });


        // load notifications, only if the user is logged
        loadNotifications(loginViewModel.getLiveLoggedUser().getValue());

        enableSwipeDownToRefresh();

    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        hideMenuItem(menu, R.id.menu_item_notifications);
        hideMenuItem(menu, R.id.menu_item_login);
    }





    //--------------------------------------------------------------------------------------- MY METHODS

    @Override
    public void onFollowRequestNotificationClicked(int position) {
        User user = getUserFromNotification(recyclerAdapterNotifications.getNotification(position));
        if(user==null) return;

        NavGraphDirections.ActionGlobalUserProfileFragment userProfileDirection =
                NavGraphDirections.actionGlobalUserProfileFragment(user);
        navigateTo(userProfileDirection, true);
    }

    @Override
    public void onPostLikedNotificationClicked(int position) {
        //TODO
    }

    @Override
    public void onPostCommentedNotificationClicked(int position) {
        //TODO
    }

    private void loadNotifications(User loggedUser) {
        if(loggedUser==null) return;
        notificationsViewModel.fetchNotifications(loggedUser, getContext());
    }

    private void updateUI(ArrayList<Notification> notifications) {
        if(notifications==null || notifications.size()==0) {
            Toast.makeText(getContext(), "Nessuna notifica.", Toast.LENGTH_SHORT).show();
        }
        else {
            recyclerAdapterNotifications.loadNewData(notifications);
        }
    }





    //--------------------------------------------------------------------------------------- SUPPORT METHODS

    private void initRecyclerView(@NonNull View view) {
        // defining Recycler view
        recyclerView = view.findViewById(R.id.recyclerView_notificationFragment);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // adding recycle listener for touch detection
        recyclerAdapterNotifications = new RecyclerAdapterNotifications(new ArrayList<>(), getContext(), this);
        recyclerView.setAdapter(recyclerAdapterNotifications);
    }

    private boolean currentUserIsLogged() {
        return loginViewModel != null && (( loginViewModel.getLoginResult().getValue() == LoginViewModel.LoginResult.SUCCESS) ||
                                            loginViewModel.getLoginResult().getValue() == LoginViewModel.LoginResult.REMEMBER_ME_EXISTS);
    }

    private void enableSwipeDownToRefresh() {
        /*
         * Sets up a SwipeRefreshLayout.OnRefreshListener that is invoked when the user
         * performs a swipe-to-refresh gesture.
         */
        swipeRefreshLayout.setOnRefreshListener( () -> {
            // This method performs the actual data-refresh operation.
            // The method calls setRefreshing(false) when it's finished.
            recyclerAdapterNotifications.clearList();
            if(currentUserIsLogged()) {
                loadNotifications(loginViewModel.getLiveLoggedUser().getValue());
            }

            swipeRefreshLayout.setRefreshing(false);
            Toast.makeText(getContext(), "Refresh completato.", Toast.LENGTH_SHORT).show();
        });
    }

    private void hideMenuItem(Menu menu, int IDResource) {
        MenuItem menuItem = menu.findItem(IDResource);
        if(menuItem!=null)
            menuItem.setVisible(false);
    }

    private User getUserFromNotification(Notification notification) {
        if(notification==null) return null;

        FollowRequestNotification followNotification = (FollowRequestNotification) notification;
        String username = (followNotification.getSender()).getUsername();
        String firstName = (followNotification.getSender()).getFirstName();
        String lastName = (followNotification.getSender()).getLastName();
        String profilePictureUrl = (followNotification.getSender()).getProfilePicturePath();
        User user = new User();
        user.setUsername(username);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setProfilePicturePath(profilePictureUrl);

        return user;
    }

    private void navigateTo(NavDirections direction, boolean removeFromBackStack) {
        if(direction==null) return;

        NavController navController = NavHostFragment.findNavController(this);
        if (removeFromBackStack) navController.popBackStack();
        navController.navigate(direction);
    }

}// end NotificationsFragment class