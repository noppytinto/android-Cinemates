package mirror42.dev.cinemates.ui.userprofile;

import android.app.PendingIntent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.NavDeepLinkBuilder;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;

import mirror42.dev.cinemates.MainActivity;
import mirror42.dev.cinemates.NavGraphDirections;
import mirror42.dev.cinemates.R;
import mirror42.dev.cinemates.model.User;
import mirror42.dev.cinemates.ui.list.FavouritesListCoverFragment;
import mirror42.dev.cinemates.ui.list.WatchedListCoverFragment;
import mirror42.dev.cinemates.ui.list.WatchistCoverFragment;
import mirror42.dev.cinemates.ui.login.LoginViewModel;
import mirror42.dev.cinemates.ui.notification.NotificationsViewModel;

import static mirror42.dev.cinemates.MainActivity.CHANNEL_ID;

public class UserProfileFragment extends Fragment implements View.OnClickListener {
    private UserProfileViewModel userProfileViewModel;
    private LoginViewModel loginViewModel;
    private ImageView imageViewProfilePicture;
    private TextView textViewfullName;
    private TextView textViewusername;
    private TextView followStatusMessage;
    private Button buttonSendFollow;
    private Button buttonAcceptFollow;
    private Button buttonDeclineFollow;
    private User profileOwner;
    private NotificationsViewModel notificationsViewModel;
    private View followRequestPrompt;
    private Button buttonCustomLists;
    private SwipeRefreshLayout swipeRefreshLayout;
    private FollowingViewModel followingViewModel;
    private FollowersViewModel followersViewModel;
    private Button followersButton;
    private Button followingButton;
    private FrameLayout watchlistCover;
    private FrameLayout favoritesListCover;
    private FrameLayout watchedListCover;
    private TextView listLabel;




    //-------------------------------------------------------------------------- ANDROID METHODS

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_user_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        init(view);

        if(getArguments() != null) {
            UserProfileFragmentArgs args = UserProfileFragmentArgs.fromBundle(getArguments());
            String username = args.getUsername();
            String currentLoggedUsername = loginViewModel.getLoggedUser().getUsername();

            if(username.equals(currentLoggedUsername)) {
                navigateToPersonalProfileFragment();
            }
            else {
                if(username!=null || username.isEmpty()) {
                    fetchUserProfileData(username);
                }
            }
        }
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == buttonSendFollow.getId()) {
            userProfileViewModel.getMySendFollowStatus().observe(getViewLifecycleOwner(), taskStatus -> {
                switch (taskStatus) {
                    case REQUEST_SENT_SUCCESSFULLY: {
                        buttonSendFollow.setVisibility(View.VISIBLE);
                        buttonSendFollow.setEnabled(false);
                        buttonSendFollow.setText("Richiesta inviata");
                        showCenteredToast("richiesta inviata");


//                            sendFollowNotification("test");
                    }
                    break;
                    case FAILED: {
                        showCenteredToast("operazione annullata");

                    }
                    break;
                    default:
                }
            });

            userProfileViewModel.sendFollowRequest(
                    loginViewModel.getObservableLoggedUser().getValue().getUsername(),
                    profileOwner.getUsername(),
                    loginViewModel.getObservableLoggedUser().getValue().getAccessToken());
        }
        else if(v.getId() == buttonAcceptFollow.getId()) {
            userProfileViewModel.getHisSendFollowStatus().observe(getViewLifecycleOwner(), taskStatus -> {
                switch (taskStatus) {
                    case HIS_FOLLOW_REQUEST_HAS_BEEN_ACCEPTED: {
                        showCenteredToast("richiesta accettata");
                        followStatusMessage.setVisibility(View.VISIBLE);

                        // update counter
                        int followersCount = profileOwner.getFollowingCount() + 1;
                        followingButton.setText("Seguiti\n" + followersCount);
                        userProfileViewModel.setHisFollowStatus(UserProfileViewModel.FollowStatus.HIS_FOLLOW_REQUEST_IS_NOT_PENDING);
                    }
                    break;
                    case FAILED: {
                        hideFollowRequestPrompt();
                        showCenteredToast("operazione annullata");

                    }
                    break;
                    default:
                }
            });

            userProfileViewModel.acceptFollowRequest(
                    profileOwner.getUsername(),
                    loginViewModel.getObservableLoggedUser().getValue().getUsername(),
                    loginViewModel.getObservableLoggedUser().getValue().getAccessToken());
        }
        else if(v.getId() == buttonDeclineFollow.getId()) {
            userProfileViewModel.getHisSendFollowStatus().observe(getViewLifecycleOwner(), taskStatus -> {
                switch (taskStatus) {
                    case HIS_FOLLOW_REQUEST_HAS_BEEN_DECLINED: {
                        showCenteredToast("richiesta rifiutata");
                        hideFollowRequestPrompt();
                    }
                    break;
                    case FAILED: {
                        showCenteredToast("operazione annullata");
                    }
                    break;
                    default:
                }
            });

            userProfileViewModel.declineFollowRequest(
                    profileOwner.getUsername(),
                    loginViewModel.getObservableLoggedUser().getValue().getUsername(),
                    loginViewModel.getObservableLoggedUser().getValue().getAccessToken());
        }
        else if(v.getId() == buttonCustomLists.getId()) {
            String fetchMode = "fetch_public_lists";
            String ownerUsername = profileOwner.getUsername();
//
            NavDirections customListBrowserFragment =
                    UserProfileFragmentDirections.actionUserProfileFragmentToCustomListBrowserFragment(fetchMode, ownerUsername);
            Navigation.findNavController(v).navigate(customListBrowserFragment);
        }
        else if(v.getId() == followersButton.getId()){
            NavDirections followersFragment =
                    UserProfileFragmentDirections.actionUserProfileFragmentToFollowersFragment(profileOwner.getUsername());
            Navigation.findNavController(v).navigate(followersFragment);
        }
        else if(v.getId() == followingButton.getId()){
            NavDirections followersFragment =
                    UserProfileFragmentDirections.actionUserProfileFragmentToFollowingFragment(profileOwner.getUsername());
            Navigation.findNavController(v).navigate(followersFragment);
        }
    }// end onClick()

    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu) {
        super.onPrepareOptionsMenu(menu);
        checkForNewNotifications();
    }





    //-------------------------------------------------------------------------- MY METHODS

    private void setupListCovers() {
        Bundle arguments = new Bundle();
        boolean isMyList = false;
        String profileOwnerUsername = profileOwner.getUsername();
        arguments.putSerializable("list_ownership", isMyList);
        arguments.putSerializable("list_owner_username", profileOwnerUsername);

        Fragment watchistCoverFragment = WatchistCoverFragment.newInstance();
        watchistCoverFragment.setArguments(arguments);
        displayFragment(watchistCoverFragment, R.id.container_userProfile_watchListCover);

        Fragment favoritesCoverFragment = FavouritesListCoverFragment.newInstance();
        favoritesCoverFragment.setArguments(arguments);
        displayFragment(favoritesCoverFragment, R.id.container_userProfile_favoritesListCover);

        Fragment watchedCoverFragment = WatchedListCoverFragment.newInstance();
        watchistCoverFragment.setArguments(arguments);
        displayFragment(watchedCoverFragment, R.id.container_userProfile_watchedListCover);
    }

    private void displayFragment(Fragment targetFragment, int containerId) {
        FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();

        // Replace whatever is in the fragment_container view with this fragment,
        // and add the transaction to the back stack if needed
        transaction.replace(containerId, targetFragment);
        transaction.addToBackStack(null);

        // Commit the transaction
        transaction.commit();
    }


    private void init(View view) {
        imageViewProfilePicture = view.findViewById(R.id.imageView_userProfileFragment_profilePicture);
        textViewfullName = view.findViewById(R.id.textView_userProfileFragment_fullName);
        textViewusername = view.findViewById(R.id.textView_userProfileFragment_username);
        followStatusMessage = view.findViewById(R.id.textView_userProfileFragment_message);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout_userProfileFragment);
        buttonSendFollow = view.findViewById(R.id.button_userProfileFragment_follow);
        buttonAcceptFollow = view.findViewById(R.id.include_userProfileFragment_requestPrompt).findViewById(R.id.button_requestPromptLayout_accept);
        buttonDeclineFollow = view.findViewById(R.id.include_userProfileFragment_requestPrompt).findViewById(R.id.button_requestPromptLayout_decline);
        followRequestPrompt = view.findViewById(R.id.include_userProfileFragment_requestPrompt);
        buttonCustomLists = view.findViewById(R.id.button_userProfileFragment_customLists);
        followersButton = view.findViewById(R.id.button_userProfileFragment_followers);
        followingButton = view.findViewById(R.id.button_userProfileFragment_following);
        watchlistCover = view.findViewById(R.id.container_userProfile_watchListCover);
        favoritesListCover = view.findViewById(R.id.container_userProfile_favoritesListCover);
        watchedListCover = view.findViewById(R.id.container_userProfile_watchedListCover);
        listLabel = view.findViewById(R.id.textView_userProfileFragment_listLabel);

        buttonSendFollow.setOnClickListener(this);
        buttonAcceptFollow.setOnClickListener(this);
        buttonDeclineFollow.setOnClickListener(this);
        buttonCustomLists.setOnClickListener(this);
        followersButton.setOnClickListener(this);
        followingButton.setOnClickListener(this);
        notificationsViewModel = new ViewModelProvider(requireActivity()).get(NotificationsViewModel.class);
        loginViewModel = new ViewModelProvider(requireActivity()).get(LoginViewModel.class);
        followingViewModel = new ViewModelProvider(this).get(FollowingViewModel.class);
        followersViewModel = new ViewModelProvider(this).get(FollowersViewModel.class);

    }

    private void navigateToPersonalProfileFragment() {
        NavDirections personalProfileFragment =
                NavGraphDirections.actionGlobalPersonalProfileFragment();
        navigateTo(personalProfileFragment, true);
    }

    private void navigateTo(NavDirections direction, boolean removeFromBackStack) {
        if(direction==null) return;

        NavController navController = NavHostFragment.findNavController(this);
        if (removeFromBackStack) navController.popBackStack();
        navController.navigate(direction);
    }

    private void loadSocialStatistics() {
        int followersCount = profileOwner.getFollowersCount();
        int followingCount = profileOwner.getFollowingCount();
        followingButton.setText("Seguiti\n" + followingCount);
        followersButton.setText("Follower\n" + followersCount);
    }

    private void enableSwipeDownToRefresh() {
        /*
         * Sets up a SwipeRefreshLayout.OnRefreshListener that is invoked when the user
         * performs a swipe-to-refresh gesture.
         */
        swipeRefreshLayout.setOnRefreshListener( () -> {
            // This method performs the actual data-refresh operation.
            // The method calls setRefreshing(false) when it's finished.
            if(currentUserIsLogged()) {
                checkFollowStatus();
            }

            swipeRefreshLayout.setRefreshing(false);
            Toast.makeText(getContext(), "Refresh completato.", Toast.LENGTH_SHORT).show();
        });
    }

    private void fetchUserProfileData(String username) {
        userProfileViewModel = new ViewModelProvider(this).get(UserProfileViewModel.class);
        userProfileViewModel.getFetchStatus().observe(getViewLifecycleOwner(), fetchStatus -> {
            switch (fetchStatus) {
                case SUCCESS: {
                    profileOwner = userProfileViewModel.getObservableFetchedUser().getValue();
                    String profilePictureUrl = profileOwner.getProfilePicturePath();
                    try {
                        Glide.with(requireContext())
                                .load(profilePictureUrl)
                                .fallback(R.drawable.icon_user_dark_blue)
                                .placeholder(R.drawable.icon_user_dark_blue)
                                .circleCrop()
                                .into(imageViewProfilePicture);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    textViewfullName.setText(profileOwner.getFullName());
                    textViewusername.setText("@" + profileOwner.getUsername());

                    startCheckOperations();
                    loadSocialStatistics();
                }
                break;
                case FAILED:
                    break;
            }
        });
        userProfileViewModel.fetchUserProfileData(username, loginViewModel.getLoggedUser());
    }// fetchUserProfileData()

    private void startCheckOperations() {
        if (currentUserIsLogged()) {
            enableSwipeDownToRefresh();

            userProfileViewModel.getMyFollowStatus().observe(getViewLifecycleOwner(), followStatus -> {
                switch (followStatus) {
                    case I_FOLLOW_HIM: {
                        buttonSendFollow.setVisibility(View.GONE);
                        showLists();
                        setupListCovers();
                    }
                    break;
                    case I_DONT_FOLLOW_HIM: {
                        buttonSendFollow.setVisibility(View.VISIBLE);
//                        showCenteredToast("NON sei un suo follower");

                        //
                        userProfileViewModel.checkMyFollowIsPending(
                                loginViewModel.getObservableLoggedUser().getValue().getUsername(),
                                profileOwner.getUsername(),
                                loginViewModel.getObservableLoggedUser().getValue().getAccessToken());
                        hideLists();
                    }
                    break;
                    case MY_FOLLOW_REQUEST_IS_PENDING: {
                        buttonSendFollow.setVisibility(View.VISIBLE);
                        buttonSendFollow.setEnabled(false);
                        buttonSendFollow.setText("Richiesta inviata");
                    }
                    break;
                    case MY_FOLLOW_REQUEST_IS_NOT_PENDING: {
                        buttonSendFollow.setVisibility(View.VISIBLE);
                    }
                    break;
                    case REQUEST_SENT_SUCCESSFULLY: {
                        buttonSendFollow.setVisibility(View.VISIBLE);
                        buttonSendFollow.setEnabled(false);
                        buttonSendFollow.setText("Richiesta inviata");
                        showCenteredToast("richiesta inviata");
                    }
                    break;
                    case FAILED: {
                        showCenteredToast("operazione annullata");
                    }
                    break;
                    default:
                }
            });

            userProfileViewModel.getHisFollowStatus().observe(getViewLifecycleOwner(), followStatus -> {
                switch (followStatus) {
                    case HE_FOLLOWS_ME: {
                        followStatusMessage.setVisibility(View.VISIBLE);
                    }
                    break;
                    case HE_DOESNT_FOLLOW_ME: {
                        followStatusMessage.setVisibility(View.GONE);

                        userProfileViewModel.checkHisFollowIsPending(
                                profileOwner.getUsername(),
                                loginViewModel.getObservableLoggedUser().getValue().getUsername(),
                                loginViewModel.getObservableLoggedUser().getValue().getAccessToken());
                    }
                    break;
                    case HIS_FOLLOW_REQUEST_IS_PENDING: {
                        showFollowRequestPrompt();
                        followStatusMessage.setVisibility(View.GONE);
                    }
                    break;
                    case HIS_FOLLOW_REQUEST_IS_NOT_PENDING: {
                        hideFollowRequestPrompt();
                    }
                    break;
                    case FAILED: {
                        showCenteredToast("operazione annullata");

                    }
                    break;
                    default:

                }
            });
            checkFollowStatus();
        }
    }// end startCheckOperations()

    private void checkIFollowHim() {
        userProfileViewModel.checkIfollowHim(
                profileOwner.getUsername(),
                loginViewModel.getObservableLoggedUser().getValue().getUsername(),
                loginViewModel.getObservableLoggedUser().getValue().getAccessToken());
    }

    private void checkHeFollowsMe() {
        userProfileViewModel.checkHeFollowsMe(
                loginViewModel.getObservableLoggedUser().getValue().getUsername(),
                profileOwner.getUsername(),
                loginViewModel.getObservableLoggedUser().getValue().getAccessToken());
    }

    private void checkFollowStatus() {
        if(currentUserIsLogged()) {
            checkIFollowHim();
            checkHeFollowsMe();
        }
    }


    private void showFollowRequestPrompt() {
        followRequestPrompt.setVisibility(View.VISIBLE);
    }

    private void hideFollowRequestPrompt() {
        followRequestPrompt.setVisibility(View.GONE);
    }


    private boolean currentUserIsLogged() {
        return loginViewModel != null && (( loginViewModel.getObservableLoginResult().getValue() == LoginViewModel.LoginResult.SUCCESS) ||
                loginViewModel.getObservableLoginResult().getValue() == LoginViewModel.LoginResult.REMEMBER_ME_EXISTS);
    }

    public void showCenteredToast(String message) {
        final Toast toast = Toast.makeText(getContext(), message, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    private void checkForNewNotifications() {
        if(notificationsViewModel!=null) {
            if(notificationsViewModel.getNotificationsStatus().getValue() == NotificationsViewModel.NotificationsStatus.GOT_NEW_NOTIFICATIONS) {
                ((MainActivity) getActivity()).activateNotificationsIcon();
            }
            else {
                ((MainActivity) getActivity()).deactivateNotificationsIcon();
            }
        }
    }

    //TODO: on testing
    public void sendFollowNotification(String senderUsername) {
        // Create an explicit intent for an Activity in your app
        Bundle bundle = new Bundle();
        bundle.putSerializable("user_argument", profileOwner);
//        Intent intent = new Intent(getContext(), MainActivity.class);
//        intent.putExtra("user_argument", bundle);
//        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//        PendingIntent pendingIntent = PendingIntent.getActivity(getContext(), 0, intent, 0);

        PendingIntent pendingIntent = new NavDeepLinkBuilder(getContext())
                .setGraph(R.navigation.nav_graph)
                .setDestination(R.id.userProfileFragment)
                .setArguments(bundle)
                .createPendingIntent();



        NotificationCompat.Builder builder = new NotificationCompat.Builder(getContext(), CHANNEL_ID)
                .setSmallIcon(R.drawable.icon_user_dark_blue)
                .setContentTitle("Nuova richiesta di seguirti")
                .setContentText(senderUsername + " chiede di seguirti.")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                // Set the intent that will fire when the user taps the notification
                /*.setContentIntent(pendingIntent)*/
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getContext());
        // notificationId is a unique int for each notification that you must define
        notificationManager.notify(0, builder.build());


    }

    private void showLists() {
        listLabel.setVisibility(View.VISIBLE);
        buttonCustomLists.setVisibility(View.VISIBLE);
        watchlistCover.setVisibility(View.VISIBLE);
        favoritesListCover.setVisibility(View.VISIBLE);
        watchedListCover.setVisibility(View.VISIBLE);
    }

    private void hideLists() {
        listLabel.setVisibility(View.GONE);
        buttonCustomLists.setVisibility(View.GONE);
        watchlistCover.setVisibility(View.GONE);
        favoritesListCover.setVisibility(View.GONE);
        watchedListCover.setVisibility(View.GONE);
    }

}// end UserProfileFragment class