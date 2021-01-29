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
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.util.ArrayList;
import java.util.Collections;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import mirror42.dev.cinemates.NavGraphDirections;
import mirror42.dev.cinemates.R;
import mirror42.dev.cinemates.model.User;
import mirror42.dev.cinemates.ui.login.LoginViewModel;
import mirror42.dev.cinemates.ui.notification.model.FollowRequestNotification;
import mirror42.dev.cinemates.ui.notification.model.Notification;

public class NotificationsFragment extends Fragment implements
        RecyclerAdapterNotifications.OnNotificationClickedListener {
    private final String TAG = this.getClass().getSimpleName();
    private NotificationsViewModel notificationsViewModel;
    private View view;
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerAdapterNotifications recyclerAdapterNotifications;
    private RecyclerView recyclerView;
    private LoginViewModel loginViewModel;
    private Disposable notificationsSubscriber;




    //-------------------------------------------------------------------------------------------------- ANDROID METHODS

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.notifications_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.view = view;
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout_notificationsFragment);
        initRecyclerView();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        notificationsViewModel = new ViewModelProvider(this).get(NotificationsViewModel.class);
        notificationsViewModel.getFetchStatus().observe(getViewLifecycleOwner(), fetchStatus -> {
            swipeRefreshLayout.setRefreshing(false);
            Toast.makeText(getContext(), "refresh completato", Toast.LENGTH_SHORT).show();

            switch (fetchStatus) {
                case SUCCESS: {
                    recyclerAdapterNotifications.loadNewData(notificationsViewModel.getNotificationsList().getValue());
                }
                    break;
                default:
            }
        });

        loginViewModel = new ViewModelProvider(requireActivity()).get(LoginViewModel.class);


        /*
         * Sets up a SwipeRefreshLayout.OnRefreshListener that is invoked when the user
         * performs a swipe-to-refresh gesture.
         */
        swipeRefreshLayout.setOnRefreshListener(
                () -> {
                    recyclerAdapterNotifications.clearList();

                    // This method performs the actual data-refresh operation.
                    // The method calls setRefreshing(false) when it's finished.
//                        myUpdateOperation();
                    if(loginViewModel!=null && ((loginViewModel.getLoginResult().getValue() == LoginViewModel.LoginResult.SUCCESS) ||
                                                 loginViewModel.getLoginResult().getValue() == LoginViewModel.LoginResult.REMEMBER_ME_EXISTS)) {

                        notificationsViewModel.fetchNotifications(
                                loginViewModel.getLoggedUser().getValue().getEmail(),
                                loginViewModel.getLoggedUser().getValue().getAccessToken());
                    }

                    else {
                        swipeRefreshLayout.setRefreshing(false);
                    }
                }
        );
    }

    @Override
    public void onResume() {
        super.onResume();

        // fetch notifications, only if the user is logged
        if(loginViewModel!=null && ((loginViewModel.getLoginResult().getValue() == LoginViewModel.LoginResult.SUCCESS) ||
                                     loginViewModel.getLoginResult().getValue() == LoginViewModel.LoginResult.REMEMBER_ME_EXISTS)) {

//            notificationsViewModel.fetchNotifications(
//                    loginViewModel.getLoggedUser().getValue().getEmail(),
//                    loginViewModel.getLoggedUser().getValue().getAccessToken());

            Observable<ArrayList<Notification>> followNotificationsObservable =
                    notificationsViewModel.createFollowNotificationsObservable(
                                                loginViewModel.getLoggedUser().getValue().getEmail(),
                                                loginViewModel.getLoggedUser().getValue().getAccessToken());

            Observable<ArrayList<Notification>> likeNotificationsObservable =
                    notificationsViewModel.createLikeNotificationsObservable(
                            loginViewModel.getLoggedUser().getValue().getEmail(),
                            loginViewModel.getLoggedUser().getValue().getAccessToken());

            Observable<ArrayList<Notification>> commentNotificationsObservable =
                    notificationsViewModel.createCommentNotificationsObservable(
                            loginViewModel.getLoggedUser().getValue().getEmail(),
                            loginViewModel.getLoggedUser().getValue().getAccessToken());

            Observable<ArrayList<Notification>> combinedNotificationsObservable =
                    Observable.combineLatest(
                                    followNotificationsObservable, likeNotificationsObservable, commentNotificationsObservable,
                                    (followNotifications, likeNotifications, commentsNotifications) -> {
                                        final ArrayList<Notification> combinedNotifications = new ArrayList<>();
                                        combinedNotifications.addAll(followNotifications);
                                        combinedNotifications.addAll(likeNotifications);
                                        combinedNotifications.addAll(commentsNotifications);
                                        Collections.sort(combinedNotifications, Collections.reverseOrder());

                                        return combinedNotifications;
                                    }
                            );


            notificationsSubscriber = combinedNotificationsObservable
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe( notificationsList -> recyclerAdapterNotifications.loadNewData(notificationsList),
                                this::handleExceptionsMessages);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (notificationsSubscriber != null && !notificationsSubscriber.isDisposed()) {
            notificationsSubscriber.dispose();
        }
    }


    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        MenuItem notification_item = menu.findItem(R.id.menu_item_notifications);
        MenuItem login_item = menu.findItem(R.id.menu_item_login);

        if(notification_item!=null)
            notification_item.setVisible(false);


        if(login_item!=null)
            login_item.setVisible(false);
    }

    private void handleExceptionsMessages(Throwable e) {
        Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
    }





    //-------------------------------------------------------------------------------------------------- METHODS

    private void initRecyclerView() {
        // defining Recycler view
        recyclerView = view.findViewById(R.id.recyclerView_notificationFragment);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // adding recycle listener for touch detection
        recyclerAdapterNotifications = new RecyclerAdapterNotifications(new ArrayList<>(), getContext(), this);
        recyclerView.setAdapter(recyclerAdapterNotifications);
    }

    @Override
    public void onFollowRequestNotificationClicked(int position) {
        FollowRequestNotification itemSelected = (FollowRequestNotification) recyclerAdapterNotifications.getNotification(position);

        String username = (itemSelected.getSender()).getUsername();
        String firstName = (itemSelected.getSender()).getFirstName();
        String lastName = (itemSelected.getSender()).getLastName();
        String profilePictureUrl = (itemSelected.getSender()).getProfilePicturePath();
        User user = new User();
        user.setUsername(username);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setProfilePicturePath(profilePictureUrl);

        NavGraphDirections.ActionGlobalUserProfileFragment action = NavGraphDirections.actionGlobalUserProfileFragment(user);
        NavController navController = NavHostFragment.findNavController(NotificationsFragment.this);
        navController.popBackStack();
        navController.navigate(action);

    }

    @Override
    public void onPostLikedNotificationClicked(int position) {

    }

    @Override
    public void onPostCommentedNotificationClicked(int position) {

    }




}// end NotificationsFragment class