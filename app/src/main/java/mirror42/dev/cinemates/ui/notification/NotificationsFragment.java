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

import mirror42.dev.cinemates.NavGraphDirections;
import mirror42.dev.cinemates.R;
import mirror42.dev.cinemates.model.User;
import mirror42.dev.cinemates.ui.login.LoginViewModel;
import mirror42.dev.cinemates.ui.notification.model.FollowRequestNotification;

public class NotificationsFragment extends Fragment implements
        RecyclerAdapterNotifications.OnNotificationClickedListener {
    private final String TAG = this.getClass().getSimpleName();
    private NotificationsViewModel notificationsViewModel;
    private View view;
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerAdapterNotifications recyclerAdapterNotifications;
    private RecyclerView recyclerView;
    private LoginViewModel loginViewModel;




    //-------------------------------------------------------------------------------------------------- CONSTRUCTORS





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
            switch (fetchStatus) {
                case SUCCESS: {
                    recyclerAdapterNotifications.loadNewData(notificationsViewModel.getNotificationsList().getValue());
                    swipeRefreshLayout.setRefreshing(false);
                }
                    break;
            }
        });


        loginViewModel = new ViewModelProvider(requireActivity()).get(LoginViewModel.class);
        loginViewModel.getLoginResult().observe(getViewLifecycleOwner(), loginResult -> {
            switch (loginResult) {

            }
        });



        /*
         * Sets up a SwipeRefreshLayout.OnRefreshListener that is invoked when the user
         * performs a swipe-to-refresh gesture.
         */
        swipeRefreshLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        Toast.makeText(getContext(), "onRefresh called from SwipeRefreshLayout", Toast.LENGTH_SHORT).show();

                        // This method performs the actual data-refresh operation.
                        // The method calls setRefreshing(false) when it's finished.
//                        myUpdateOperation();
                        notificationsViewModel.fetchData(loginViewModel.getLoggedUser().getValue().getEmail(), loginViewModel.getLoggedUser().getValue().getAccessToken());
                    }
                }
        );


        try {
            notificationsViewModel.fetchData(loginViewModel.getLoggedUser().getValue().getEmail(), loginViewModel.getLoggedUser().getValue().getAccessToken());
        } catch (Exception e) {
            e.printStackTrace();
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





    //-------------------------------------------------------------------------------------------------- METHODS

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

    private void initRecyclerView() {
        // defining Recycler view
        recyclerView = view.findViewById(R.id.recyclerView_notificationFragment);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // adding recycle listener for touch detection
        recyclerAdapterNotifications = new RecyclerAdapterNotifications(new ArrayList<>(), getContext(), this);
        recyclerView.setAdapter(recyclerAdapterNotifications);
    }



}// end NotificationsFragment class