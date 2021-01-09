package mirror42.dev.cinemates.ui.userprofile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
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
        setHasOptionsMenu(true);
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



        // setting listeners
        buttonLogout.setOnClickListener(this);

        // firebase logging
        FirebaseAnalytics firebaseAnalytics = FirebaseAnalytics.getInstance();
        firebaseAnalytics.logScreenEvent(this, "User profile page", getContext());

        //
        loginViewModel = new ViewModelProvider(requireActivity()).get(LoginViewModel.class);
        loginViewModel.getLoginResult().observe(getViewLifecycleOwner(), loginResult -> {

            switch (loginResult) {
                case SUCCESS:
                    break;
                case LOGGED_OUT:
                    break;
                case REMEMBER_ME_EXISTS:
                    break;

            }


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
            loginViewModel.setLoginResult(LoginViewModel.LoginResult.LOGGED_OUT);

            // delete remember me data
            MyUtilities.deletFile(remoteConfigServer.getCinematesData(), getContext());

            //
            MainActivity mainActivity = (MainActivity) getActivity();
            mainActivity.hideLogo();

            //
            NavController navController = Navigation.findNavController(v);
            navController.popBackStack();
            navController.navigate(R.id.loginFragment);
        }
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        MenuItem item = menu.findItem(R.id.menu_item_login);
        if(item!=null)
            item.setVisible(false);

        MainActivity mainActivity = (MainActivity) getActivity();
        mainActivity.hideLogo();
    }


    //----------------------------------------------------------------------- METHODS


    @Override
    public void onDetach() {
        super.onDetach();

        if(loginViewModel.getLoginResult().getValue() != LoginViewModel.LoginResult.LOGGED_OUT) {
            MainActivity mainActivity = (MainActivity) getActivity();
            mainActivity.showLogo();
        }
    }
}// end UserProfileFragment class