package mirror42.dev.cinemates.ui.notification;

import android.os.Bundle;
import android.view.Gravity;
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

import com.google.android.material.progressindicator.CircularProgressIndicator;

import java.util.ArrayList;

import mirror42.dev.cinemates.MainActivity;
import mirror42.dev.cinemates.NavGraphDirections;
import mirror42.dev.cinemates.R;
import mirror42.dev.cinemates.adapter.RecyclerAdapterNotifications;
import mirror42.dev.cinemates.model.User;
import mirror42.dev.cinemates.model.list.CustomList;
import mirror42.dev.cinemates.model.notification.FollowRequestNotification;
import mirror42.dev.cinemates.model.notification.ListRecommendedNotification;
import mirror42.dev.cinemates.model.notification.MovieRecommendedNotification;
import mirror42.dev.cinemates.model.notification.Notification;
import mirror42.dev.cinemates.model.tmdb.Movie;
import mirror42.dev.cinemates.ui.login.LoginViewModel;
import mirror42.dev.cinemates.ui.search.SearchFragmentDirections;

public class NotificationsFragment extends Fragment implements
        RecyclerAdapterNotifications.OnNotificationClickedListener {
    private final String TAG = this.getClass().getSimpleName();
    private NotificationsViewModel notificationsViewModel;
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerAdapterNotifications recyclerAdapterNotifications;
    private RecyclerView recyclerView;
    private LoginViewModel loginViewModel;
    private CircularProgressIndicator progressIndicator;
    private View includeMessageForEmptyPage;




    //--------------------------------------------------------------------------------------- ANDROID METHODS


    @Override
    public void onResume() {
        super.onResume();
        ((MainActivity)requireActivity()).showToolbar();
    }

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
        init(view);
    }

    private void init(@NonNull View view) {
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout_notificationsFragment);
        progressIndicator = view.findViewById(R.id.progressIndicator_notificationsFragment);
        includeMessageForEmptyPage = view.findViewById(R.id.include_notifications_empty);
        initRecyclerView(view);
        loginViewModel = new ViewModelProvider(requireActivity()).get(LoginViewModel.class);
        notificationsViewModel = new ViewModelProvider(requireActivity()).get(NotificationsViewModel.class);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // load notifications, only if the user is logged
        fetchNotifications(loginViewModel.getLoggedUser());

        enableSwipeDownToRefresh();
    }

    private void showCenteredToast(String msg) {
        final Toast toast = Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0,0);
        toast.show();
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        hideMenuItem(menu, R.id.menu_item_notifications);
        hideMenuItem(menu, R.id.menu_item_login);
    }





    //--------------------------------------------------------------------------------------- MY METHODS

    private void showMessageForEmptyPage(){
        includeMessageForEmptyPage.setVisibility(View.VISIBLE);
    }

    private void hideMessageForEmptyPage(){
        includeMessageForEmptyPage.setVisibility(View.GONE);
    }

    @Override
    public void onFollowRequestNotificationClicked(int position) {
        User user = getUserFromNotification(recyclerAdapterNotifications.getItem(position));
        if(user==null) return;

        NavGraphDirections.ActionGlobalUserProfileFragment userProfileDirection =
                NavGraphDirections.actionGlobalUserProfileFragment(user.getUsername());
        navigateTo(userProfileDirection, true);
    }

    @Override
    public void onPostLikedNotificationClicked(int position) {
        //TODO
        Notification currentNotification = recyclerAdapterNotifications.getItem(position);
        long postID = currentNotification.getPostID();
        long notificationID = currentNotification.getId();

        // delete from remote DB
        notificationsViewModel.deleteNotificationFromRemoteDB(notificationID, loginViewModel.getLoggedUser());

        // then go to the relative post
        NavGraphDirections.ActionGlobalPostFragment postFragment =
                NavGraphDirections.actionGlobalPostFragment(postID);
        navigateTo(postFragment, false);
    }

    @Override
    public void onPostCommentedNotificationClicked(int position) {
        //TODO
        Notification currentNotification = recyclerAdapterNotifications.getItem(position);
        long postID = currentNotification.getPostID();
        long notificationID = currentNotification.getId();

        // delete from remote DB
        notificationsViewModel.deleteNotificationFromRemoteDB(notificationID, loginViewModel.getLoggedUser());

        // then go to the relative post
        NavGraphDirections.ActionGlobalPostFragment postFragment =
                NavGraphDirections.actionGlobalPostFragment(postID);
        navigateTo(postFragment, false);
    }

    @Override
    public void onListRecommendedNotificationClicked(int position) {
        ListRecommendedNotification currentNotification = (ListRecommendedNotification) recyclerAdapterNotifications.getItem(position);
        notificationsViewModel.getObservableFetchStatus().observe(getViewLifecycleOwner(), fetchStatus -> {
            switch (fetchStatus) {
                case SUCCESS: {
                    CustomList list = notificationsViewModel.getObservableCustomList().getValue();
                    if(list!=null) {
                        list.setOwner(currentNotification.getSender());

                        // delete from remote DB
                        long notificationID = currentNotification.getId();
                        notificationsViewModel.deleteNotificationFromRemoteDB(notificationID, loginViewModel.getLoggedUser());

                        NavGraphDirections.ActionGlobalListFragment listFragment =
                                NavGraphDirections.actionGlobalListFragment(list, "", "");
                        NavHostFragment.findNavController(NotificationsFragment.this).navigate(listFragment);
                    }
                    else showCenteredToast("impossibile aprire lista");
                }
                break;
                case FAILED: {
                    CustomList list = notificationsViewModel.getObservableCustomList().getValue();

                    if(list!=null) {
                        list.setOwner(currentNotification.getSender());

                        // delete from remote DB
                        long notificationID = currentNotification.getId();
                        notificationsViewModel.deleteNotificationFromRemoteDB(notificationID, loginViewModel.getLoggedUser());

                        NavGraphDirections.ActionGlobalListFragment listFragment =
                                NavGraphDirections.actionGlobalListFragment(list, "", "");
                        NavHostFragment.findNavController(NotificationsFragment.this).navigate(listFragment);
                    }
                    else showCenteredToast("impossibile aprire lista");
                }
                    break;
            }
        });

        notificationsViewModel.fetchCustomListMovies(
                currentNotification.getSender().getUsername(),
                currentNotification.getCustomList().getName(),
                currentNotification.getCustomList().getDescription(),
                loginViewModel.getLoggedUser());

    }

    @Override
    public void onSubscribedToListNotificationClicked(int position) {
        Notification currentNotification = recyclerAdapterNotifications.getItem(position);
        long notificationID = currentNotification.getId();

        // delete from remote DB
        notificationsViewModel.deleteNotificationFromRemoteDB(notificationID, loginViewModel.getLoggedUser());

        //
        NavGraphDirections.ActionGlobalUserProfileFragment userProfileDirection =
                NavGraphDirections.actionGlobalUserProfileFragment(currentNotification.getSender().getUsername());
        navigateTo(userProfileDirection, true);
    }

    @Override
    public void onMovieRecommendedNotificationClicked(int position) {
        MovieRecommendedNotification itemSelected = (MovieRecommendedNotification) recyclerAdapterNotifications.getItem(position);
        long notificationID = itemSelected.getId();

        // delete from remote DB
        notificationsViewModel.deleteNotificationFromRemoteDB(notificationID, loginViewModel.getLoggedUser());

        //
        Movie mv = new Movie();
        mv.setTmdbID(itemSelected.getMovieId());
        NavGraphDirections.AnywhereToMovieDetailsFragment action = SearchFragmentDirections.anywhereToMovieDetailsFragment(mv);
        NavHostFragment.findNavController(NotificationsFragment.this).navigate(action);
    }

    private void fetchNotifications(User loggedUser) {
        if(loggedUser==null) return;

        notificationsViewModel.getNotificationsStatus().observe(getViewLifecycleOwner(), notificationsStatust -> {
            ArrayList<Notification> notifications = notificationsViewModel.getNotifications().getValue();

            switch (notificationsStatust) {
                case NOTIFICATIONS_FETCHED: {
                    hideProgressIndicator();
                    if(notifications!=null) {
                        if(notifications.isEmpty()) showMessageForEmptyPage();
                        else hideMessageForEmptyPage();

                        updateUI(notifications);
                        notificationsViewModel.setNotificationsAsOld(notifications, getContext());
                    }
                }
                break;
                case GOT_NEW_NOTIFICATIONS: {
//                    hideProgressIndicator();
                }
                break;
                case NO_NOTIFICATIONS:
                    hideProgressIndicator();
                    showMessageForEmptyPage();
                    break;
                case NOTIFICATION_DELETED: {
                    showCenteredToast("notifica eliminata");
                    if(notifications==null||notifications.isEmpty()) {
                        showMessageForEmptyPage();
                    }
                    // delete from local DB
                    long notificationID = notificationsViewModel.getCurrentNotificationID();
                    notificationsViewModel.deleteNotificationFromLocalDB(notificationID, getContext());

                }
                break;
                case NOTIFICATION_NOT_DELETED: {
                    showCenteredToast("notifica NON eliminata :(");

                }
                break;
            }

        });


        try {
            showProgressIndicator();
            notificationsViewModel.fetchNotifications(loggedUser, requireContext());
        } catch (Exception e) {
            e.printStackTrace();
        }
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
        return loginViewModel != null && (( loginViewModel.getObservableLoginResult().getValue() == LoginViewModel.LoginResult.SUCCESS) ||
                                            loginViewModel.getObservableLoginResult().getValue() == LoginViewModel.LoginResult.REMEMBER_ME_EXISTS);
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
                showProgressIndicator();
                fetchNotifications(loginViewModel.getObservableLoggedUser().getValue());
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
        user.setProfilePictureURL(profilePictureUrl);

        return user;
    }

    private void navigateTo(NavDirections direction, boolean removeFromBackStack) {
        if(direction==null) return;

        NavController navController = NavHostFragment.findNavController(this);
        if (removeFromBackStack) navController.popBackStack();
        navController.navigate(direction);
    }

    private void showProgressIndicator() {
        progressIndicator.setVisibility(View.VISIBLE);
    }

    private void hideProgressIndicator() {
        progressIndicator.setVisibility(View.GONE);
    }


}// end NotificationsFragment class