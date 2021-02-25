package mirror42.dev.cinemates.ui.signup;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.DateValidatorPointBackward;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Locale;

import mirror42.dev.cinemates.R;
import mirror42.dev.cinemates.utilities.FirebaseAnalytics;
import mirror42.dev.cinemates.utilities.MyUtilities;
import mirror42.dev.cinemates.utilities.RemoteConfigServer;

import static android.app.Activity.RESULT_OK;

public class SignUpFragment extends Fragment implements
        View.OnClickListener,
        CompoundButton.OnCheckedChangeListener{
    private final int PERMISSION_CODE = 5;
    private final String TAG = this.getClass().getSimpleName();
    //
    private TextInputLayout textInputLayoutUsername;
    private TextInputLayout textInputLayoutEmail;
    private TextInputLayout textInputLayoutPassword;
    private TextInputLayout textInputLayoutRepeatPassword;
    private TextInputLayout textInputLayoutFirstName;
    private TextInputLayout textInputLayoutLastName;
    private TextInputLayout textInputLayoutBirthDate;
    //
    private TextInputEditText editTextUsername;
    private TextInputEditText editTextEmail;
    private TextInputEditText editTextPassword;
    private TextInputEditText editTextRepeatPassword;
    private TextInputEditText editTextFirstName;
    private TextInputEditText editTextLastName;
    private TextInputEditText editTextBirthDate;
    //
    private CheckBox checkBoxPromo;
    private CheckBox checkBoxAnalytics;
    private CheckBox checkBoxTermsAndConditions;

    //
    private Button buttonsignUp;
    private Button buttonUpload;
    private Button buttonDatePicker;
    private MaterialDatePicker.Builder<Long> materialDatePickerBuilder;
    private SignUpViewModel signUpViewModel;
    private View view;
    private RemoteConfigServer remoteConfigServer;
    //
    private static int PICK_IMAGE = 30;
    private String filePath;
    private ImageView imageViewProfilePicture;
    //
    private ProgressDialog progressDialog;
    private final int SELECT_PICTURE = 200;
    private Uri localImageUri;

    private TextView privacyPolicy;



    //--------------------------------------------------------------- ANDROID METHODS


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_sign_up, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.view = view;
        remoteConfigServer = RemoteConfigServer.getInstance();


//        spinner = view.findViewById(R.id.progresBar_signUpFragment);
        textInputLayoutUsername = view.findViewById(R.id.textInputLayout_signUpFragment_username);
        textInputLayoutEmail = view.findViewById(R.id.textInputLayout_signUpFragment_email);
        textInputLayoutPassword = view.findViewById(R.id.textInputLayout_signUpFragment_password);
        textInputLayoutRepeatPassword = view.findViewById(R.id.textInputLayout_signUpFragment_repeatPassword);
        textInputLayoutFirstName = view.findViewById(R.id.textInputLayout_signUpFragment_firstName);
        textInputLayoutLastName = view.findViewById(R.id.textInputLayout_signUpFragment_lastName);
        textInputLayoutBirthDate = view.findViewById(R.id.textInputLayout_signUpFragment_birthDate);
        //
        editTextUsername = view.findViewById(R.id.editText_signUpFragment_username);
        editTextEmail = view.findViewById(R.id.editText_signUpFragment_email);
        editTextPassword = view.findViewById(R.id.editText_signUpFragment_password);
        editTextRepeatPassword = view.findViewById(R.id.editText_signUpFragment_repeatPassword);
        editTextFirstName = view.findViewById(R.id.editText_signUpFragment_firstName);
        editTextLastName = view.findViewById(R.id.editText_signUpFragment_lastName);
        editTextBirthDate = view.findViewById(R.id.editText_signUpFragment_birthDate);
        //
        privacyPolicy = view.findViewById(R.id.textView_signUpFragment_privacyPolicy);
        //
        buttonsignUp = view.findViewById(R.id.button_signUpFragment_signUp);
        buttonDatePicker = view.findViewById(R.id.button_signUpPage_datePicker);
        checkBoxPromo = view.findViewById(R.id.checkBox_loginFragment_promo);
        checkBoxAnalytics = view.findViewById(R.id.checkBox_loginFragment_analytics);
        checkBoxTermsAndConditions = view.findViewById(R.id.checkBox_loginFragment_termsAndConditions);
        //
        buttonUpload = view.findViewById(R.id.button_signUpFragment_upload);
        imageViewProfilePicture = view.findViewById(R.id.imageView_signUpFragment_profilePicture);
        // setting listeners
        buttonUpload.setOnClickListener(this);
        buttonsignUp.setOnClickListener(this);
        buttonDatePicker.setOnClickListener(this);
        checkBoxPromo.setOnCheckedChangeListener(this);
        checkBoxAnalytics.setOnCheckedChangeListener(this);
        checkBoxTermsAndConditions.setOnCheckedChangeListener(this);
        privacyPolicy.setOnClickListener(this);

        // fast sign up
        fastSignUp();

    }// end onViewCreated class

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        FirebaseAnalytics firebaseAnalytics = FirebaseAnalytics.getInstance();
        firebaseAnalytics.logScreenEvent(this, "Sign-Up page", getContext());

        //
        signUpViewModel = new ViewModelProvider(this).get(SignUpViewModel.class);
        signUpViewModel.getFirebaseAuthState().observe(getViewLifecycleOwner(), firebaseSignUpServerCodeState -> {
            hideProgressDialog();

            switch (firebaseSignUpServerCodeState) {
                case SIGN_UP_SUCCESS:
                    showCenteredToast("Firebase sign-up server:\ncreateUserWithEmail:success");
                    break;
                case SIGN_UP_FAILURE:
                    showCenteredToast("Firebase sign-up server:\ncreateUserWithEmail:failure");
                    break;
                case VERIFICATION_MAIL_SENT: {
                    String msg = "Firebase sign-up server:\nRiceverai a breve un link di attivazione account nella tua posta";
                    final Toast toast = Toast.makeText(getContext(), msg, Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                    Navigation.findNavController(view).popBackStack();
                    Navigation.findNavController(view).navigate(R.id.main_fragment);
                }
                    break;
                case VERIFICATION_MAIL_NOT_SENT:
                    showCenteredToast("Firebase sign-up server:\nVerification email NOT sent");
                    break;
                case PENDING_USER_COLLISION:
                    showCenteredToast("Firebase sign-up server:\nemail ancora non approvata\ncontrolla la tua posta\"");
                    textInputLayoutEmail.setError("email ancora non approvata, controlla la tua posta");
                    Navigation.findNavController(view).popBackStack();
                    Navigation.findNavController(view).navigate(R.id.main_fragment);
                    break;
                case NO_PENDING_USER_COLLISION:
                    signUpViewModel.insertUserInFirebaseDB();
                    break;
                case USERNAME_EMAIL_COLLISION:
                    showCenteredToast("Firebase sign-up server:\nusername+email gia' presente");
                    textInputLayoutUsername.setError("username+email gia' presente");
                    textInputLayoutEmail.setError("username+email gia' presente");
                    break;
                case USERNAME_COLLISION:
                    showCenteredToast("Firebase sign-up server:\nusername gia' presente");
                    textInputLayoutUsername.setError("username gia' presente");
                    break;
                case EMAIL_COLLISION:
                    showCenteredToast("Firebase sign-up server:\nemail gia' presente");
                    textInputLayoutEmail.setError("email gia' presente");
                    break;
                case GENERIC_POSTGREST_ERROR:
                    showCenteredToast("Firebase sign-up server:\nerrore postgrest");
                    textInputLayoutEmail.setError("email gia' presente");
                    break;
                case WEAK_PASSWORD:
                    showCenteredToast("Firebase sign-up server:\nla password deve contenere almeno 6 caratteri");
                    textInputLayoutPassword.setError(getString(R.string.passwordHelper));
                    break;
                case NO_POSTGRES_USER_COLLISION:
                    signUpViewModel.checkPendingUserCollision();
                    break;
            }
        });



    }

    @Override
    public void onClick(View v) {
        if(v.getId() == buttonUpload.getId()) {
//            fetchImageFromGallery(v);
            imageChooser();
        }
        else if(v.getId() == buttonsignUp.getId()) {


            // check fields
            boolean allFieldsAreFilled = checkAllFieldsAreFilled();
            boolean repeatPasswordMatches = checkRepeatPasswordMatch();

            if(allFieldsAreFilled) {
                // getting data and trim text
                String username = editTextUsername.getText().toString().trim();
                String email = editTextEmail.getText().toString().trim();
                String password = editTextPassword.getText().toString().trim();
                String firstName = editTextFirstName.getText().toString().trim();
                String lastName = editTextLastName.getText().toString().trim();
                String birthDate = editTextBirthDate.getText().toString().trim();
                boolean promo = checkBoxPromo.isChecked();
                boolean analytics = checkBoxAnalytics.isChecked();
                boolean emailFromatIsValid = checkEmailFormat(email);
                if(repeatPasswordMatches && emailFromatIsValid) {
                    showProgressDialog();
                    signUpViewModel.signUpAsPendingUser(username, email, password, firstName, lastName, birthDate, localImageUri, promo, analytics);
                }
            }
            else {
                showCenteredToast("completare prima tutti i campi evidenziati in rosso");
            }
        }
        else if(v.getId() == buttonDatePicker.getId()) {
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
            picker.addOnPositiveButtonClickListener(confirmButton-> {
                long selectedDateInMillis = picker.getSelection();
                String selectedDate = MyUtilities.convertMillisInDate(selectedDateInMillis);
                editTextBirthDate.setText(selectedDate);
            });
        }else if(v.getId() == privacyPolicy.getId()){
            Navigation.findNavController(v).navigate(R.id.action_signUpFragment_to_privacyPolicyFragment);
        }
    }// end onClick()

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

    }

    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu) {
        super.onPrepareOptionsMenu(menu);
        MenuItem userIcon = menu.getItem(1);
        userIcon.setVisible(false);
    }




    //--------------------------------------------------------------- METHODS

    void fetchImageFromGallery(View view){
//        requestPermission();

    }

    void imageChooser() {
//        personalProfileViewModel.getResetStatus().observe(getViewLifecycleOwner(), changeImageResult -> {
//            switch (changeImageResult) {
//                case SUCCESS: {
//                    Glide.with(this)  //2
//                            .load(localImageUri) //3
//                            .fallback(R.drawable.broken_image)
//                            .placeholder(R.drawable.icon_user_dark_blue)
//                            .circleCrop() //4
//                            .into(profilePicture); //8
//                    loginViewModel.updateProfileImageUrl(personalProfileViewModel.getImageName());
//                    showCenteredToast( "cambio immagine profilo riuscito ");
//                }
//                break;
//                case FAILED:
//                    showCenteredToast( "cambio immagine profilo NON riuscito");
//                    break;
//            }
//        });

        // create an instance of the
        // intent of the type image
        Intent i = new Intent();
        i.setType("image/*");
        i.setAction(Intent.ACTION_GET_CONTENT);

        // pass the constant to compare it
        // with the returned requestCode
        startActivityForResult(Intent.createChooser(i, "Select Picture"), SELECT_PICTURE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data); //get the image’s file location
        if(requestCode==SELECT_PICTURE && resultCode==RESULT_OK){
            try {
                localImageUri = data.getData();
                Glide.with(this)  //2
                    .load(localImageUri) //3
                    .fallback(R.drawable.broken_image)
                    .placeholder(R.drawable.icon_user_dark_blue)
                    .circleCrop() //4
                    .into(imageViewProfilePicture); //8
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    private void requestPermission(){
        if(ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
            accessTheGallery();
        } else {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_CODE);
        }
    }

    public void accessTheGallery(){
        Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        i.setType("image/*");
     startActivityForResult(i, PICK_IMAGE);
    }


    private void showProgressDialog() {
        //notes: Declare progressDialog before so you can use .hide() later!
        try {
            progressDialog = new ProgressDialog(requireContext());
            progressDialog.setMessage("Operazione in corso...");
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void hideProgressDialog() {
        if(progressDialog!=null)
            progressDialog.hide();
    }



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode== PERMISSION_CODE){
            if(grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                accessTheGallery();
            }else {
                Toast.makeText(getActivity(), "permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

//    @Override
//    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
//        super.onActivityResult(requestCode, resultCode, data); //get the image’s file location
//
//        Bitmap thumbnail = null;
//        if(requestCode==PICK_IMAGE && resultCode==RESULT_OK){
//            try {
//                //set picked image to the mProfile
//                thumbnail = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), data.getData());
////                mProfile.setImageBitmap(bitmap);
//
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//
//            filePath = "-";
//            try {
//                filePath = getRealPathFromUri(data.getData(), getActivity());
//
//                // load thumbnail
//                Glide.with(this)  //2
//                        .load(filePath) //3
//                        .fallback(R.drawable.broken_image)
//                        .placeholder(R.drawable.placeholder_image)
//                        .circleCrop() //4
//                        .into(imageViewProfilePicture); //8
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
//    }

    private String getRealPathFromUri(Uri imageUri, Activity activity){
        Cursor cursor = activity.getContentResolver().query(imageUri, null,  null, null, null); if(cursor==null) {
            return imageUri.getPath();
        }else{
            cursor.moveToFirst();
            int idx =  cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            return cursor.getString(idx);
        }
    }

    private boolean checkAllFieldsAreFilled() {
        boolean res = true;
        if(editTextUsername.getText().toString().isEmpty()) {
            textInputLayoutUsername.setError("*");
            res = false;
        }
        else {
            textInputLayoutUsername.setError(null);
        }

        if(editTextEmail.getText().toString().isEmpty()) {
            textInputLayoutEmail.setError("*");
            res = false;
        }
        else {
            textInputLayoutEmail.setError(null);
        }

        if(editTextPassword.getText().toString().isEmpty()) {
            textInputLayoutPassword.setError("*");
            res = false;
        }
        else {
            textInputLayoutPassword.setError(null);
        }

        if(editTextRepeatPassword.getText().toString().isEmpty()) {
            textInputLayoutRepeatPassword.setError("*");
            res = false;
        }
        else {
            textInputLayoutRepeatPassword.setError(null);
        }

        if(editTextFirstName.getText().toString().isEmpty()) {
            textInputLayoutFirstName.setError("*");
            res = false;
        }
        else {
            textInputLayoutFirstName.setError(null);
        }

        if(editTextLastName.getText().toString().isEmpty()) {
            textInputLayoutLastName.setError("*");
            res = false;
        }
        else {
            textInputLayoutLastName.setError(null);
        }

        if(editTextBirthDate.getText().toString().isEmpty()) {
            textInputLayoutBirthDate.setError("*");
            res = false;
        }
        else {
            textInputLayoutBirthDate.setError(null);
        }

        if( ! checkBoxTermsAndConditions.isChecked()) {
            checkBoxTermsAndConditions.setTextColor(getResources().getColor(R.color.red));
            res = false;
        }
        else {
            checkBoxTermsAndConditions.setTextColor(getResources().getColor(R.color.light_blue));
        }

        return res;
    }

    private boolean checkRepeatPasswordMatch() {
        if( ! editTextRepeatPassword.getText().toString().equals(editTextPassword.getText().toString())) {
            textInputLayoutRepeatPassword.setError(getString(R.string.passwords_dont_match));
            return false;
        }
        else {
            textInputLayoutRepeatPassword.setError(null);
        }

        return true;
    }

    private void fastSignUp() {
//        editTextUsername.setText("foo");
//        editTextEmail.setText("noto42@outlook.com");
//        editTextPassword.setText("aaaaaaa");
//        editTextRepeatPassword.setText("aaaaaaa");
//        editTextFirstName.setText("mrfoo");
//        editTextLastName.setText("bar");
        editTextBirthDate.setText("1/1/1970");
    }

    public void showCenteredToast(String message) {
        final Toast toast = Toast.makeText(getContext(), message, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    private boolean checkEmailFormat(String email) {
        if(android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            textInputLayoutEmail.setError(null);
            return true;
        }

        textInputLayoutEmail.setError("Il formato mail non e' valido!");
        return false;
    }



}// end signUpFragment class