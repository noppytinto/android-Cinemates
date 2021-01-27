package mirror42.dev.cinemates.ui.userprofile;

import android.app.PendingIntent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
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

import mirror42.dev.cinemates.R;
import mirror42.dev.cinemates.model.User;
import mirror42.dev.cinemates.ui.login.LoginViewModel;

import static mirror42.dev.cinemates.MainActivity.CHANNEL_ID;

public class UserProfileFragment extends Fragment implements View.OnClickListener {
    private UserProfileViewModel mViewModel;
    private LoginViewModel loginViewModel;
    private View view;
    private ImageView imageViewProfilePicture;
    private TextView textViewfullName;
    private TextView textViewusername;
    private TextView textViewMessage;
    private Button buttonFollow;
    private Button buttonAcceptFollow;
    private User profileOwner;


    //-------------------------------------------------------------------------- ANDROID METHODS

    @Override
    public void onResume() {
        super.onResume();
        try {
            mViewModel.checkYouFollowHim(
                    loginViewModel.getLoggedUser().getValue().getUsername(),
                    profileOwner.getUsername(),
                    loginViewModel.getLoggedUser().getValue().getAccessToken());

            mViewModel.checkHeFollowsYou(
                    profileOwner.getUsername(),
                    loginViewModel.getLoggedUser().getValue().getUsername(),
                    loginViewModel.getLoggedUser().getValue().getAccessToken());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_user_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.view = view;
        imageViewProfilePicture = view.findViewById(R.id.imageView_userProfileFragment_profilePicture);
        textViewfullName = view.findViewById(R.id.textView_userProfileFragment_fullName);
        textViewusername = view.findViewById(R.id.textView_userProfileFragment_username);
        textViewMessage = view.findViewById(R.id.textView_userProfileFragment_message);

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
                Glide.with(getContext())  //2
                        .load(profilePictureUrl) //3
                        .fallback(R.drawable.broken_image)
                        .placeholder(R.drawable.placeholder_image)
                        .circleCrop() //4
                        .into(imageViewProfilePicture); //8

                textViewfullName.setText(user.getFullName());
                textViewusername.setText("@" + user.getUsername());





                loginViewModel = new ViewModelProvider(requireActivity()).get(LoginViewModel.class);
                loginViewModel.getLoginResult().observe(getViewLifecycleOwner(), loginResult -> {
                });

                mViewModel = new ViewModelProvider(this).get(UserProfileViewModel.class);
                mViewModel.getTaskStatus().observe(getViewLifecycleOwner(), taskStatus -> {
//            buttonAcceptFollow.setVisibility(View.GONE);

                    switch (taskStatus) {
                        case REQUEST_SENT_SUCCESSFULLY: {
                            buttonFollow.setVisibility(View.VISIBLE);
                            buttonFollow.setEnabled(false);
                            buttonFollow.setText("Richiesta inviata");
                            final Toast toast = Toast.makeText(getContext(), "richiesta inviata", Toast.LENGTH_SHORT);
//                    toast.setGravity(Gravity.CENTER, 0, 0);
                            toast.show();

//                            sendFollowNotification("test");
                        }
                        break;
                        case FRIEND_CHECK_COMPLETE:
                            if(mViewModel.isFriend()) {
                                buttonFollow.setVisibility(View.GONE);
                                final Toast toast = Toast.makeText(getContext(), "siete amici :D", Toast.LENGTH_SHORT);
                                toast.setGravity(Gravity.CENTER, 0, 0);
                                toast.show();
                            }
                            else {
                                buttonFollow.setVisibility(View.VISIBLE);
                                final Toast toast = Toast.makeText(getContext(), "non siete amici :(", Toast.LENGTH_SHORT);
//                        toast.setGravity(Gravity.CENTER, 0, 0);
                                toast.show();

                                mViewModel.checkMyFollowIsPending(
                                        loginViewModel.getLoggedUser().getValue().getUsername(),
                                        profileOwner.getUsername(),
                                        loginViewModel.getLoggedUser().getValue().getAccessToken());

                            }
                            break;
                        case MY_FOLLOW_REQUEST_PENDING: {
                            buttonFollow.setVisibility(View.VISIBLE);
                            buttonFollow.setEnabled(false);
                            buttonFollow.setText("Richiesta inviata");
                        }
                        break;
                        case MY_FOLLOW_REQUEST_NOT_PENDING: {
                            buttonFollow.setVisibility(View.VISIBLE);
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
                        case HIS_FOLLOW_REQUEST_ACCEPTED: {
                            buttonAcceptFollow.setVisibility(View.GONE);
                            final Toast toast = Toast.makeText(getContext(), "richiesta accettata", Toast.LENGTH_SHORT);
//                    toast.setGravity(Gravity.CENTER, 0, 0);
                            toast.show();
                            textViewMessage.setVisibility(View.VISIBLE);
                        }
                        break;
                        case HE_FOLLOWS_YOU: {
                            textViewMessage.setVisibility(View.VISIBLE);

                        }
                        break;
                        case HE_DOESNT_FOLLOw_YOU: {
                            textViewMessage.setVisibility(View.GONE);

                            mViewModel.checkHisFollowIsPendingTask(
                                    profileOwner.getUsername(),
                                    loginViewModel.getLoggedUser().getValue().getUsername(),
                                    loginViewModel.getLoggedUser().getValue().getAccessToken());
                        }
                        break;
                        case FAILED: {
                            final Toast toast = Toast.makeText(getContext(), "operazione annullata!", Toast.LENGTH_SHORT);
//                    toast.setGravity(Gravity.CENTER, 0, 0);
                            toast.show();
                        }
                        break;
                        default:

                    }
                });










            }








        }
        else {

        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == buttonFollow.getId()) {
            mViewModel.sendFollowRequest(
                    loginViewModel.getLoggedUser().getValue().getUsername(),
                    profileOwner.getUsername(),
                    loginViewModel.getLoggedUser().getValue().getAccessToken());
        }
        else if(v.getId() == buttonAcceptFollow.getId()) {
            mViewModel.acceptFollowRequest(
                    profileOwner.getUsername(),
                    loginViewModel.getLoggedUser().getValue().getUsername(),
                    loginViewModel.getLoggedUser().getValue().getAccessToken());
        }
    }


    //-------------------------------------------------------------------------- MY METHODS
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
                .setSmallIcon(R.drawable.user_icon_dark_blue)
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