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
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import org.json.JSONException;
import org.json.JSONObject;

import mirror42.dev.cinemates.MainActivity;
import mirror42.dev.cinemates.R;
import mirror42.dev.cinemates.model.User;
import mirror42.dev.cinemates.ui.login.LoginViewModel;
import mirror42.dev.cinemates.utilities.FirebaseAnalytics;
import mirror42.dev.cinemates.utilities.ImageUtilities;
import mirror42.dev.cinemates.utilities.MyUtilities;
import mirror42.dev.cinemates.utilities.RemoteConfigServer;


public class UserProfileFragment extends Fragment implements View.OnClickListener {
    private View view;
    private ImageView profilePicture;
    private TextView textViewEmail;
    private Button buttonLogout;
    private RemoteConfigServer remoteConfigServer;
    private LoginViewModel loginViewModel;


    //----------------------------------------------------------------------- ANDROID METHODS


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // this is for Login Fragment:
        // if login is NOT successful, navigte back to main fragment
//        NavController navController = NavHostFragment.findNavController(this);
//        NavBackStackEntry navBackStackEntry = navController.getCurrentBackStackEntry();
//        SavedStateHandle savedStateHandle = navBackStackEntry.getSavedStateHandle();
//        savedStateHandle.getLiveData(LoginFragment.LOGIN_SUCCESSFUL).observe(navBackStackEntry, success -> {
//                    if( ! (Boolean) success) {
//                        int startDestination = navController.getGraph().getStartDestination();
//                        NavOptions navOptions = new NavOptions.Builder()
//                                .setPopUpTo(startDestination, true)
//                                .build();
//                        navController.navigate(startDestination, null, navOptions);
//                    }
//                });
    }

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

        //
        MainActivity mainActivity = (MainActivity) getActivity();
        mainActivity.hideToolbarElements();


        // setting listeners
        buttonLogout.setOnClickListener(this);

        // firebase logging
        FirebaseAnalytics firebaseAnalytics = FirebaseAnalytics.getInstance();
        firebaseAnalytics.logScreenEvent(this, "User profile page", getContext());

        //
        loginViewModel = new ViewModelProvider(requireActivity()).get(LoginViewModel.class);
        loginViewModel.getLoginResult().observe(getViewLifecycleOwner(), (Observer<LoginViewModel.LoginResult>) loginResult -> {
            if (loginResult == LoginViewModel.LoginResult.SUCCESS) {
                User user = loginViewModel.getUser().getValue();
                String profilePicturePath = user.getProfilePicturePath();

                ImageUtilities.loadCircularImageInto(remoteConfigServer.getCloudinaryDownloadBaseUrl() + profilePicturePath, profilePicture, getContext());
                textViewEmail.setText(user.getEmail());

            }
            else if(loginResult == LoginViewModel.LoginResult.FAILED) {

            }
            else if(loginResult == LoginViewModel.LoginResult.INVALID_REQUEST) {
            }
            else if(loginResult == LoginViewModel.LoginResult.REMEMBER_ME_EXISTS) {
                try {
                    // decrypt remember me data
                    JSONObject jsonObject = new JSONObject(MyUtilities.decryptFile(remoteConfigServer.getCinematesData(), getContext()));

                    // create remember me user
                    User remeberMeUser = User.parseUserFromJsonObject(jsonObject);

                    //
                    textViewEmail.setText(remeberMeUser.getEmail());

                    // set profile picture
                    String imagePath = remeberMeUser.getProfilePicturePath();
                    if(imagePath!=null || (! imagePath.isEmpty())) {
                        ImageUtilities.loadCircularImageInto(remoteConfigServer.getCloudinaryDownloadBaseUrl() + imagePath, profilePicture, getContext());
                    }

                    // store remember me user
                    loginViewModel.setUser(remeberMeUser);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });






    }// end onViewCreated()

    @Override
    public void onClick(View v) {
        if (v.getId() == buttonLogout.getId()) {
            loginViewModel.setUser(null);
            loginViewModel.setLoginResult(LoginViewModel.LoginResult.LOGOUT);

            // delete remember me data
            MyUtilities.deletFile(remoteConfigServer.getCinematesData(), getContext());

            //
            NavController navController = Navigation.findNavController(v);
            navController.popBackStack();
            navController.navigate(R.id.loginFragment);
        }
    }


    //----------------------------------------------------------------------- METHODS

    @Override
    public void onDetach() {
        super.onDetach();
        MainActivity mainActivity = (MainActivity) getActivity();
        mainActivity.showToolbarElements();

    }



}// end UserProfileFragment class