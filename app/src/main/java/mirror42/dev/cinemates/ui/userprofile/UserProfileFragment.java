package mirror42.dev.cinemates.ui.userprofile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import mirror42.dev.cinemates.R;
import mirror42.dev.cinemates.ui.login.LoginActivity;
import mirror42.dev.cinemates.ui.login.LoginViewModel;
import mirror42.dev.cinemates.utilities.FirebaseEventsLogger;
import mirror42.dev.cinemates.utilities.MyUtilities;
import mirror42.dev.cinemates.utilities.RemoteConfigServer;


public class UserProfileFragment extends Fragment implements View.OnClickListener {
    private View view;
    private ImageView profilePicture;
    private TextView textViewEmail;
    private Button buttonLogout;
    private RemoteConfigServer remoteConfigServer;
    private UserProfileViewModel userProfileViewModel;
    private LoginViewModel loginViewModel;


    //----------------------------------------------------------------------- ANDROID METHODS

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_user_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.view = view;
        profilePicture = view.findViewById(R.id.imageView_loginFragment_profilePicture);
        textViewEmail = (TextView) view.findViewById(R.id.textView_loginFragment_email);
        buttonLogout = view.findViewById(R.id.button_loginFragment_logout);
        remoteConfigServer = RemoteConfigServer.getInstance();

        // setting listeners
        buttonLogout.setOnClickListener(this);

        // firebase logging
        FirebaseEventsLogger firebaseEventsLogger = FirebaseEventsLogger.getInstance();
        firebaseEventsLogger.logScreenEvent(this, "User profile page", getContext());

        //
        loginViewModel = new ViewModelProvider(requireActivity()).get(LoginViewModel.class);
        loginViewModel.getUser().observe(getViewLifecycleOwner(), user -> {
            if(user==null) {
                final NavController navController = Navigation.findNavController(view);
                navController.navigate(R.id.loginFragment);
            }
            else {

            }
        });







    }// end onViewCreated()

    @Override
    public void onClick(View v) {
        if (v.getId() == buttonLogout.getId()) {
            LoginActivity loginActivity = (LoginActivity) getActivity();

            // delete remember me data
            MyUtilities.deletFile(remoteConfigServer.getCinematesData(), getContext());
        }
    }


    //----------------------------------------------------------------------- METHODS







}// end UserProfileFragment class