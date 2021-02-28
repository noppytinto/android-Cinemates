package mirror42.dev.cinemates.ui.login;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.DateValidatorPointBackward;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Locale;
import java.util.Random;

import mirror42.dev.cinemates.R;
import mirror42.dev.cinemates.model.User;
import mirror42.dev.cinemates.utilities.FirebaseAnalytics;
import mirror42.dev.cinemates.utilities.MyUtilities;

public class GoogleLoginFragment extends Fragment implements
        View.OnClickListener,
        CompoundButton.OnCheckedChangeListener{

    private View view;
    private TextView title;
    private TextView privacyPolicy;
    private CheckBox checkBoxPromo;
    private CheckBox checkBoxAnalytics;
    private CheckBox checkBoxTermsAndConditions;
    private Button buttonContinue;
    private MaterialDatePicker.Builder<Long> materialDatePickerBuilder;
    private TextInputLayout textInputLayoutBirthDate;
    private TextInputEditText editTextBirthDate;
    private Button buttonDatePicker;
    private ImageView profilePicture;
    private LinearProgressIndicator uploadProgressIndicator;

    private GoogleLoginViewModel googleLoginViewModel;
    private LoginViewModel loginViewModel;

    private GoogleLoginViewModel.OperationTypeWithGoogle typeOperation;

    private User googleUser;
    private GoogleSignInClient mGoogleSignInClient;

    public GoogleLoginFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);

        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(getContext(), gso);
    }

    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu) {
        super.onPrepareOptionsMenu(menu);
        MenuItem userIcon = menu.getItem(1);
        userIcon.setVisible(false);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_google_login, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        FirebaseAnalytics firebaseAnalytics = FirebaseAnalytics.getInstance();
        firebaseAnalytics.logScreenEvent(this, "Google login page", getContext());

        this.view = view;

        initElements();
        initListener();

        setUserWithGoogleInfo();

        loginViewModel = new ViewModelProvider(requireActivity()).get(LoginViewModel.class);

        showUploadProgressIndicator();
        googleLoginViewModel =  new ViewModelProvider(this).get(GoogleLoginViewModel.class);
        googleLoginViewModel.getUserCollisionCheckResult().observe(getViewLifecycleOwner(), userCollisionCheckResult -> {
            if(userCollisionCheckResult == GoogleLoginViewModel.ResultOperation.SUCCESS){
                typeOperation = GoogleLoginViewModel.OperationTypeWithGoogle.REGISTRATION;
                prepareRegistration();
            }else
                googleLoginViewModel.checkExternalUser(googleUser.getEmail());
        });

        googleLoginViewModel.getExternalUser().observe(getViewLifecycleOwner(), externalUser->{
            if(externalUser == GoogleLoginViewModel.ResultOperation.SUCCESS){
                typeOperation = GoogleLoginViewModel.OperationTypeWithGoogle.LOGIN;
                prepareLogin();
            }else{
                prepareLoginError();
            }
        });

        googleLoginViewModel.checkUserCollision(googleUser.getUsername(), googleUser.getEmail());
    }


    @Override
    public void onClick(View v) {
        if(v.getId() == privacyPolicy.getId()){
            Navigation.findNavController(v).navigate(R.id.privacyPolicyFragment);
        }else if(v.getId() == buttonDatePicker.getId()) {
            Locale.setDefault(Locale.ITALY); //TODO: should be handled differently
            materialDatePickerBuilder = MaterialDatePicker.Builder.datePicker();
            materialDatePickerBuilder.setTheme(R.style.Cinemates_MaterialDatePicker);
            materialDatePickerBuilder.setTitleText("Seleziona data nascita");
            CalendarConstraints.Builder constraintsBuilder = new CalendarConstraints.Builder();
            long today = MaterialDatePicker.todayInUtcMilliseconds();
            constraintsBuilder.setEnd(today);
            constraintsBuilder.setValidator(DateValidatorPointBackward.now());
            materialDatePickerBuilder.setCalendarConstraints(constraintsBuilder.build());
            MaterialDatePicker<Long> picker = materialDatePickerBuilder.build();
            picker.show(getActivity().getSupportFragmentManager(), picker.toString());
            picker.addOnPositiveButtonClickListener(confirmButton -> {
                long selectedDateInMillis = picker.getSelection();
                String selectedDate = MyUtilities.convertMillisInDate(selectedDateInMillis);
                editTextBirthDate.setText(selectedDate);
            });
        }else if(v.getId() == buttonContinue.getId()){
            doRegistrationOrLogin();
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

    }




    //-----------------------------------------------------------------MY METHODS

    private void setUserWithGoogleInfo() {
        GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(getContext());
        if (acct != null) {
            googleUser = new User();


            String personGivenName = acct.getGivenName();
            String personFamilyName = acct.getFamilyName();
            String personEmail = acct.getEmail();

            Uri personPhoto = acct.getPhotoUrl();
            String profileImage = String.valueOf(personPhoto);

            if(profileImage != null)
                googleUser.setProfilePictureURL(String.valueOf(personPhoto));
            else
                googleUser.setProfilePictureURL("no_image");

            googleUser.setFirstName(personGivenName);
            googleUser.setLastName(personFamilyName);

            googleUser.setEmail(personEmail);
            googleUser.setExternalUser(true);
            googleUser.setPassword("no_pass");

            revokeAccess();

        }
    }

    private void revokeAccess() {
        mGoogleSignInClient.revokeAccess()
                .addOnCompleteListener((Activity) getContext(), new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                    }
                });
    }

    private void prepareRegistration(){
        setImageProfile();

        title.setText("Ciao " + googleUser.getFirstName()+" "+googleUser.getLastName() +  " "+ getString(R.string.googleLogin_page_title_registration));

        profilePicture.setVisibility(View.VISIBLE);
        buttonDatePicker.setVisibility(View.VISIBLE);
        textInputLayoutBirthDate.setVisibility(View.VISIBLE);

        privacyPolicy.setVisibility(View.VISIBLE);

        checkBoxAnalytics.setVisibility(View.VISIBLE);
        checkBoxPromo.setVisibility(View.VISIBLE);
        checkBoxTermsAndConditions.setVisibility(View.VISIBLE);

        buttonContinue.setVisibility(View.VISIBLE);
        buttonContinue.setText(getString(R.string.googleLogin_page_button_Registration));

        hideUploadProgressIndicator();
    }

    private void prepareLogin(){

        setImageProfile();
        title.setText("Ciao " + googleUser.getFirstName() + " " + googleUser.getLastName()  + "," + getString(R.string.googleLogin_page_title_access));

        buttonContinue.setVisibility(View.VISIBLE);
        profilePicture.setVisibility(View.VISIBLE);
        buttonContinue.setText(getString(R.string.googleLogin_page_button_login));
        buttonDatePicker.setVisibility(View.GONE);
        textInputLayoutBirthDate.setVisibility(View.GONE);

        privacyPolicy.setVisibility(View.GONE);

        checkBoxAnalytics.setVisibility(View.GONE);
        checkBoxPromo.setVisibility(View.GONE);
        checkBoxTermsAndConditions.setVisibility(View.GONE);

        hideUploadProgressIndicator();
    }

    private void prepareLoginError(){
        hideUploadProgressIndicator();

        title.setText(getString(R.string.googleLogin_page_title_back));
        buttonContinue.setVisibility(View.GONE);
        buttonDatePicker.setVisibility(View.GONE);
        textInputLayoutBirthDate.setVisibility(View.GONE);

        privacyPolicy.setVisibility(View.GONE);
        profilePicture.setVisibility(View.GONE);
        checkBoxAnalytics.setVisibility(View.GONE);
        checkBoxPromo.setVisibility(View.GONE);
        checkBoxTermsAndConditions.setVisibility(View.GONE);
    }

    private void  setImageProfile(){
        Glide.with(this)  //2
                .load(googleUser.getProfilePicturePath()) //3
                .fallback(R.drawable.broken_image)
                .placeholder(R.drawable.icon_user_dark_blue)
                .circleCrop() //4
                .into(profilePicture); //8
    }

    private void  initElements(){
        title = view.findViewById(R.id.textView_googleLogin_title);
        privacyPolicy = view.findViewById(R.id.textView_googleLogin_privacyPolicy);
        checkBoxAnalytics = view.findViewById(R.id.checkBox_googleLogin_analytics);
        checkBoxTermsAndConditions = view.findViewById(R.id.checkBox_googleLogin_termsAndConditions);
        checkBoxPromo = view.findViewById(R.id.checkBox_googleLogin_promo);


        buttonContinue = view.findViewById(R.id.button_googleLogin_startLoginRegistration);
        buttonDatePicker = view.findViewById(R.id.button_googleLogin_datePicker);

        textInputLayoutBirthDate = view.findViewById(R.id.textInputLayout_googleLogin_birthDate);
        editTextBirthDate = view.findViewById(R.id.editText_googleLogin_birthDate);
        editTextBirthDate.setText("1/1/1970");

        uploadProgressIndicator = view.findViewById(R.id.progressIndicator_googleLogin);
        profilePicture =  view.findViewById(R.id.imageView_googleLogin_profilePicture);
    }

    private void initListener(){
        buttonContinue.setOnClickListener(this);
        buttonDatePicker.setOnClickListener(this);
        privacyPolicy.setOnClickListener(this);
    }

    private void doRegistrationOrLogin(){

        if(typeOperation == GoogleLoginViewModel.OperationTypeWithGoogle.LOGIN){

            googleLoginViewModel.getLoginUserResult().observe(getViewLifecycleOwner(), loginUserResult->{
                if(loginUserResult == GoogleLoginViewModel.ResultOperation.SUCCESS){

                    User userToPass = new User(
                            googleUser.getUsername(), googleUser.getPassword() , googleUser.getEmail(),
                            googleUser.getFirstName(), googleUser.getLastName(), googleUser.getBirthDate(),
                            googleUser.getProfilePicturePath(), googleUser.getAccessToken(),googleUser.getAnalytics());
                    userToPass.setFollowingCount(googleUser.getFollowingCount());
                    userToPass.setFollowersCount(googleUser.getFollowersCount());
                    userToPass.setExternalUser(true);

                    loginViewModel.setUser(userToPass);
                    loginViewModel.setLoginResult(LoginViewModel.LoginResult.SUCCESS);

                    NavController navController = Navigation.findNavController(view);
                    navController.popBackStack();
                    navController.navigate(R.id.personalProfileFragment);
                }else{
                    showCenteredToast("Ci dispiace non è stato possibile effettuare il login ");
                }
            });

            googleLoginViewModel.selectUserInfo(googleUser,typeOperation );
        }else if (typeOperation == GoogleLoginViewModel.OperationTypeWithGoogle.REGISTRATION ){

            if(checkTermsAndConditionsCheckBox()){

                googleLoginViewModel.getRegisterUserResult().observe(getViewLifecycleOwner(), registerUserResult->{
                if(registerUserResult == GoogleLoginViewModel.ResultOperation.SUCCESS){
                    User userToPass = new User(
                            googleUser.getUsername(), googleUser.getPassword() , googleUser.getEmail(),
                            googleUser.getFirstName(), googleUser.getLastName(), googleUser.getBirthDate(),
                            googleUser.getProfilePicturePath(), googleUser.getAccessToken(),googleUser.getAnalytics());
                    userToPass.setFollowingCount(googleUser.getFollowingCount());
                    userToPass.setFollowersCount(googleUser.getFollowersCount());
                    userToPass.setExternalUser(true);

                    loginViewModel.setUser(userToPass);
                    loginViewModel.setLoginResult(LoginViewModel.LoginResult.SUCCESS);

                    NavController navController = Navigation.findNavController(view);
                    navController.popBackStack();
                    navController.navigate(R.id.personalProfileFragment);
                }else{
                    showCenteredToast("Ci dispiace non è stato possibile effettuare la registrazione ");
                }
            });

                setUserInfoForRegistration();
                googleLoginViewModel.insertUserWithGoogleCredential(googleUser);
            }
        }
    }

    private void setUserInfoForRegistration(){

        googleUser.setAnalytics(checkBoxAnalytics.isChecked());
        googleUser.setPromo(checkBoxPromo.isChecked());

        googleUser.setBirthDate(editTextBirthDate.getText().toString());

        Random rand = new Random();
        int randomSerialUsername = rand.nextInt(10000) + rand.nextInt(100);

        String serialUsername = Integer.toString(randomSerialUsername);
        String username = googleUser.getFirstName() + serialUsername;
        googleUser.setUsername(username);
    }

    private boolean checkTermsAndConditionsCheckBox(){

        if( ! checkBoxTermsAndConditions.isChecked()) {
            checkBoxTermsAndConditions.setTextColor(getResources().getColor(R.color.red));
            showCenteredToast("accettare condizioni e termini");
            return false;
        }
        else {
            checkBoxTermsAndConditions.setTextColor(getResources().getColor(R.color.light_blue));
            return true;
        }
    }

    public void showCenteredToast(String message) {
        final Toast toast = Toast.makeText(getContext(), message, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    private void showUploadProgressIndicator(){
        uploadProgressIndicator.setVisibility(View.VISIBLE);
    }

    private void hideUploadProgressIndicator(){

        uploadProgressIndicator.setVisibility(View.GONE);
    }
    
}