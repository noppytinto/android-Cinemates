package mirror42.dev.cinemates.ui.login;

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
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.DateValidatorPointBackward;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Locale;

import mirror42.dev.cinemates.R;
import mirror42.dev.cinemates.ui.resetPassword.ResetPasswordViewModel;
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

    private GoogleLoginViewModel.OperationTypeWithGoogle typeOperation; // va settato dopo i controlli da fare con fabrizio tipo email presente se si allora vedi se puoi fare login se no registrati!!;

    private String localImage = "";
    private String username = "test1111";
    private String email="test11@gmail.com";

    public GoogleLoginFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
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

        buttonContinue.setOnClickListener(this);
        buttonDatePicker.setOnClickListener(this);
        privacyPolicy.setOnClickListener(this);

        // qui devo gestire i dati carica l'immagine e l'utente preso da google!!!!
        // dopo aver caricato i dati prima di mostrarli fai i controlli per username e ed email  s.substring(0, s.indexOf("@"));

        showUploadProgressIndicator();
        googleLoginViewModel =  new ViewModelProvider(this).get(GoogleLoginViewModel.class);
        googleLoginViewModel.getUserCollisionCheckResult().observe(getViewLifecycleOwner(), userCollisionCheckResult -> {
            if(userCollisionCheckResult == GoogleLoginViewModel.ResultOperation.SUCCESS){
                typeOperation = GoogleLoginViewModel.OperationTypeWithGoogle.REGISTRATION;
                prepareRegistration();
            }else
                googleLoginViewModel.checkExternalUser(email);
        });

        googleLoginViewModel.getExternalUser().observe(getViewLifecycleOwner(), externalUser->{
            if(externalUser == GoogleLoginViewModel.ResultOperation.SUCCESS){
                typeOperation = GoogleLoginViewModel.OperationTypeWithGoogle.LOGIN;
                prepareLogin();
            }else{
                prepareLoginError();
            }
        });

        googleLoginViewModel.checkUserCollision( username,  email);

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


    private void prepareRegistration(){
        setImageProfile();
        buttonContinue.setVisibility(View.VISIBLE);
        profilePicture.setVisibility(View.VISIBLE);
        title.setText("Ciao " + username +  " "+ getString(R.string.googleLogin_page_title_registration));
        buttonDatePicker.setVisibility(View.VISIBLE);
        textInputLayoutBirthDate.setVisibility(View.VISIBLE);

        privacyPolicy.setVisibility(View.VISIBLE);

        checkBoxAnalytics.setVisibility(View.VISIBLE);
        checkBoxPromo.setVisibility(View.VISIBLE);
        checkBoxTermsAndConditions.setVisibility(View.VISIBLE);
        buttonContinue.setText(getString(R.string.googleLogin_page_button_Registration));
        hideUploadProgressIndicator();
    }

    private void prepareLogin(){

        setImageProfile();
        title.setText("Ciao " + username + "," + getString(R.string.googleLogin_page_title_access));

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
                .load(R.drawable.icon_user_dark_blue) //3
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

    private void doRegistrationOrLogin(){
        if(typeOperation == GoogleLoginViewModel.OperationTypeWithGoogle.LOGIN){
            //gestisco con viewMOdel il login
        }else if (typeOperation == GoogleLoginViewModel.OperationTypeWithGoogle.REGISTRATION ){
            checkTermsAndConditionsCheckBox();
            //gestisco con viewModel la registrazione
        }
    }

    private void checkTermsAndConditionsCheckBox(){

        if( ! checkBoxTermsAndConditions.isChecked()) {
            checkBoxTermsAndConditions.setTextColor(getResources().getColor(R.color.red));
            showCenteredToast("accettare condizioni e termini");
        }
        else {
            checkBoxTermsAndConditions.setTextColor(getResources().getColor(R.color.light_blue));
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