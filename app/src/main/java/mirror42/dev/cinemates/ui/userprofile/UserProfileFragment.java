package mirror42.dev.cinemates.ui.userprofile;

import android.app.PendingIntent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavDeepLinkBuilder;

import com.bumptech.glide.Glide;

import mirror42.dev.cinemates.MainActivity;
import mirror42.dev.cinemates.R;
import mirror42.dev.cinemates.model.User;
import mirror42.dev.cinemates.ui.login.LoginViewModel;
import mirror42.dev.cinemates.ui.notification.NotificationsViewModel;

import static mirror42.dev.cinemates.MainActivity.CHANNEL_ID;

public class UserProfileFragment extends Fragment implements View.OnClickListener {
    private UserProfileViewModel userProfileViewModel;
    private LoginViewModel loginViewModel;
    private ImageView imageViewProfilePicture;
    private TextView textViewfullName;
    private TextView textViewusername;
    private TextView textViewMessage;
    private Button buttonFollow;
    private Button buttonAcceptFollow;
    private User profileOwner;
    private NotificationsViewModel notificationsViewModel;



    //-------------------------------------------------------------------------- ANDROID METHODS

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_user_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        imageViewProfilePicture = view.findViewById(R.id.imageView_userProfileFragment_profilePicture);
        textViewfullName = view.findViewById(R.id.textView_userProfileFragment_fullName);
        textViewusername = view.findViewById(R.id.textView_userProfileFragment_username);
        textViewMessage = view.findViewById(R.id.textView_userProfileFragment_message);
        notificationsViewModel = new ViewModelProvider(requireActivity()).get(NotificationsViewModel.class);

        buttonFollow = view.findViewById(R.id.button_userProfileFragment_follow);
        buttonAcceptFollow = view.findViewById(R.id.button_userProfileFragment_acceptRequest);

        buttonFollow.setOnClickListener(this);
        buttonAcceptFollow.setOnClickListener(this);

        if(getArguments() != null) {
            UserProfileFragmentArgs args = UserProfileFragmentArgs.fromBundle(getArguments());
            User user = args.getUserArgument();

            if(user!=null) {
                profileOwner = user;
                String profilePictureUrl = user.getProfilePicturePath();
                Glide.with(getContext())
                        .load(profilePictureUrl)
                        .fallback(R.drawable.icon_user_dark_blue)
                        .placeholder(R.drawable.icon_user_dark_blue)
                        .circleCrop()
                        .into(imageViewProfilePicture);

                textViewfullName.setText(user.getFullName());
                textViewusername.setText("@" + user.getUsername());
            }
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        loginViewModel = new ViewModelProvider(requireActivity()).get(LoginViewModel.class);

        if (currentUserIsLogged()) {
            userProfileViewModel = new ViewModelProvider(this).get(UserProfileViewModel.class);
            userProfileViewModel.getMyFollowStatus().observe(getViewLifecycleOwner(), followStatus -> {
                switch (followStatus) {
                    case I_FOLLOW_HIM: {
                        buttonFollow.setVisibility(View.GONE);
                        showCenteredToast("sei un suo follower");
                    }
                        break;
                    case I_DONT_FOLLOW_HIM: {
                        buttonFollow.setVisibility(View.VISIBLE);
                        showCenteredToast("NON sei un suo follower");


                        //
                        userProfileViewModel.checkMyFollowIsPending(
                                loginViewModel.getLoggedUser().getValue().getUsername(),
                                profileOwner.getUsername(),
                                loginViewModel.getLoggedUser().getValue().getAccessToken());

                    }
                        break;
                    case MY_FOLLOW_REQUEST_IS_PENDING: {
                        buttonFollow.setVisibility(View.VISIBLE);
                        buttonFollow.setEnabled(false);
                        buttonFollow.setText("Richiesta inviata");
                    }
                    break;
                    case MY_FOLLOW_REQUEST_IS_NOT_PENDING: {
                        buttonFollow.setVisibility(View.VISIBLE);
                    }
                    break;
                    case REQUEST_SENT_SUCCESSFULLY: {
                        buttonFollow.setVisibility(View.VISIBLE);
                        buttonFollow.setEnabled(false);
                        buttonFollow.setText("Richiesta inviata");
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

            userProfileViewModel.getHisFollowStatus().observe(getViewLifecycleOwner(), followStatus -> {
                switch (followStatus) {
                    case HE_FOLLOWS_ME: {
                        textViewMessage.setVisibility(View.VISIBLE);
                    }
                    break;
                    case HE_DOESNT_FOLLOW_ME: {
                        textViewMessage.setVisibility(View.GONE);

                        userProfileViewModel.checkHisFollowIsPending(
                                profileOwner.getUsername(),
                                loginViewModel.getLoggedUser().getValue().getUsername(),
                                loginViewModel.getLoggedUser().getValue().getAccessToken());
                    }
                    break;
                    case HIS_FOLLOW_REQUEST_IS_PENDING: {
                        buttonAcceptFollow.setVisibility(View.VISIBLE);
                        textViewMessage.setVisibility(View.GONE);
                    }
                    break;
                    case HIS_FOLLOW_REQUEST_IS_NOT_PENDING: {
                        buttonAcceptFollow.setVisibility(View.GONE);
                    }
                    break;
                    case FAILED: {
                        showCenteredToast("operazione annullata");

                    }
                    break;
                    default:

                }
            });

        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if(currentUserIsLogged()) {
            userProfileViewModel.checkIfollowHim(
                    loginViewModel.getLoggedUser().getValue().getUsername(),
                    profileOwner.getUsername(),
                    loginViewModel.getLoggedUser().getValue().getAccessToken());

            userProfileViewModel.checkHeFollowsMe(
                    profileOwner.getUsername(),
                    loginViewModel.getLoggedUser().getValue().getUsername(),
                    loginViewModel.getLoggedUser().getValue().getAccessToken());

        }
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == buttonFollow.getId()) {
            userProfileViewModel.getMySendFollowStatus().observe(getViewLifecycleOwner(), taskStatus -> {
                switch (taskStatus) {
                    case REQUEST_SENT_SUCCESSFULLY: {
                        buttonFollow.setVisibility(View.VISIBLE);
                        buttonFollow.setEnabled(false);
                        buttonFollow.setText("Richiesta inviata");
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
                    loginViewModel.getLoggedUser().getValue().getUsername(),
                    profileOwner.getUsername(),
                    loginViewModel.getLoggedUser().getValue().getAccessToken());
        }
        else if(v.getId() == buttonAcceptFollow.getId()) {
            userProfileViewModel.getHisSendFollowStatus().observe(getViewLifecycleOwner(), taskStatus -> {
                switch (taskStatus) {
                    case HIS_FOLLOW_REQUEST_HAS_BEEN_ACCEPTED: {
                        buttonAcceptFollow.setVisibility(View.GONE);
                        showCenteredToast("richiesta accettata");
                        textViewMessage.setVisibility(View.VISIBLE);
                    }
                    break;
                    case FAILED: {
                        showCenteredToast("operazione annullata");

                    }
                    break;
                    default:
                }
            });

            userProfileViewModel.acceptFollowRequest(
                    profileOwner.getUsername(),
                    loginViewModel.getLoggedUser().getValue().getUsername(),
                    loginViewModel.getLoggedUser().getValue().getAccessToken());
        }
    }

    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu) {
        super.onPrepareOptionsMenu(menu);
        checkForNewNotifications();
    }

    //-------------------------------------------------------------------------- MY METHODS

    private boolean currentUserIsLogged() {
        return loginViewModel != null && (( loginViewModel.getLoginResult().getValue() == LoginViewModel.LoginResult.SUCCESS) ||
                loginViewModel.getLoginResult().getValue() == LoginViewModel.LoginResult.REMEMBER_ME_EXISTS);
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

}// end UserProfileFragment class