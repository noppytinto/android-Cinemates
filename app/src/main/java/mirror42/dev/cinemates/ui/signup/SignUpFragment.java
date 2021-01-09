package mirror42.dev.cinemates.ui.signup;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.DateValidatorPointBackward;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Locale;

import mirror42.dev.cinemates.MainActivity;
import mirror42.dev.cinemates.R;
import mirror42.dev.cinemates.model.User;
import mirror42.dev.cinemates.utilities.FirebaseAnalytics;
import mirror42.dev.cinemates.utilities.MyUtilities;
import mirror42.dev.cinemates.utilities.RemoteConfigServer;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static android.app.Activity.RESULT_OK;

public class SignUpFragment extends Fragment implements
        View.OnClickListener,
        CompoundButton.OnCheckedChangeListener,
        Callback {
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
    private ImageButton buttonDatePicker;
    private MaterialDatePicker.Builder<Long> materialDatePickerBuilder;
    private ProgressBar spinner;
    private SignUpViewModel signUpViewModel;
    private View view;
    private RemoteConfigServer remoteConfigServer;
    //
    private static int SELECT_PICTURE = 30;
    private String imageUrl;
    private ImageView imageViewProfilePicture;
    private static final int PERMISSION_CODE = 10;
    private static final int PICK_IMAGE = 20;



    //--------------------------------------------------------------- ANDROID METHODS

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
//        editTextUsername.setOnEditorActionListener(this);
        //

        // firebase logging
        FirebaseAnalytics firebaseAnalytics = FirebaseAnalytics.getInstance();
        firebaseAnalytics.logScreenEvent(this, "Sign-Up page", getContext());

        //
        signUpViewModel = new ViewModelProvider(this).get(SignUpViewModel.class);
        signUpViewModel.getUser().observe(getViewLifecycleOwner(), new Observer<User>() {
            @Override
            public void onChanged(@Nullable User user) {
                if (user != null) {


                } else {

                }
            }
        });


        signUpViewModel.getFirebaseAuthState().observe(getViewLifecycleOwner(), (Observer<SignUpViewModel.FirebaseSignUpServerCodeState>) firebaseSignUpServerCodeState -> {
            NavController navController = Navigation.findNavController(view);

            switch (firebaseSignUpServerCodeState) {
                case SIGN_UP_SUCCESS:
                    MyUtilities.showCenteredToast("Firebase sign-up server:\ncreateUserWithEmail:success" , getContext());

                    break;
                case SIGN_UP_FAILURE:
                    MyUtilities.showCenteredToast("Firebase sign-up server:\ncreateUserWithEmail:failure", getContext());
                    break;
                case VERIFICATION_MAIL_SENT:
                    MyUtilities.showCenteredToastLong("Firebase sign-up server:\nRiceverai a breve un link di attivazione account nella tua posta", getContext());
                    navController.popBackStack();
                    navController.navigate(R.id.main_fragment);
                    break;
                case VERIFICATION_MAIL_NOT_SENT:
                    MyUtilities.showCenteredToast("Firebase sign-up server:\nVerification email NOT sent", getContext());
                    break;
                case PENDING_USER_COLLISION:
                    MyUtilities.showCenteredToastLong("Firebase sign-up server:\nemail ancora non approvata\ncontrolla la tua posta\"", getContext());
                    textInputLayoutEmail.setError("email ancora non approvata, controlla la tua posta");
                    navController.popBackStack();
                    navController.navigate(R.id.main_fragment);
                    break;
                case USERNAME_EMAIL_COLLISION:
                    MyUtilities.showCenteredToastLong("Firebase sign-up server:\nusername+email gia' presente", getContext());
                    textInputLayoutUsername.setError("username+email gia' presente");
                    textInputLayoutEmail.setError("username+email gia' presente");
                    break;
                case USERNAME_COLLISION:
                    MyUtilities.showCenteredToastLong("Firebase sign-up server:\nusername gia' presente", getContext());
                    textInputLayoutUsername.setError("username gia' presente");
                    break;
                case EMAIL_COLLISION:
                    MyUtilities.showCenteredToastLong("Firebase sign-up server:\nemail gia' presente", getContext());
                    textInputLayoutEmail.setError("email gia' presente");
                    break;
                case GENERIC_POSTGREST_ERROR:
                    MyUtilities.showCenteredToast("Firebase sign-up server:\nerrore postgrest", getContext());
                    textInputLayoutEmail.setError("email gia' presente");
                    break;
                case WEAK_PASSWORD:
                    MyUtilities.showCenteredToast("Firebase sign-up server:\nla password deve contenere un numero di caratteri superiori a 5", getContext());
                    textInputLayoutEmail.setError("la password deve contenere un numero di caratteri superiori a 5");
                    break;
            }
        });

        // Initialize Firebase Auth

//        FirebaseAuth.AuthStateListener mAuthListener = new FirebaseAuth.AuthStateListener() {
//            @Override
//            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
//                FirebaseUser user = firebaseAuth.getCurrentUser();
//                if (user != null) {
//                    sendEmailVerificationTo(user);
//
//                }
//            }
//        };
//
//        mAuth.addAuthStateListener(mAuthListener);


        // fast sign up
        fastSignUp();



    }// end onViewCreated class

    @Override
    public void onClick(View v) {
        if(v.getId() == buttonUpload.getId()) {
            fetchImageFromGallery(v);
        }
        else if(v.getId() == buttonsignUp.getId()) {
            // getting data
            String username = editTextUsername.getText().toString();
            String email = editTextEmail.getText().toString();
            String password = editTextPassword.getText().toString();
            String firstName = editTextFirstName.getText().toString();
            String lastName = editTextLastName.getText().toString();
            String birthDate = editTextBirthDate.getText().toString();
            boolean promo = checkBoxPromo.isChecked();
            boolean analytics = checkBoxAnalytics.isChecked();

            // check fields
            boolean allFieldsAreFilled = checkAllFieldsAreFilled();
            boolean repeatPasswordMatches = checkRepeatPasswordMatch();

            if(allFieldsAreFilled && repeatPasswordMatches) {
                String profilePicturePath = imageUrl;
                signUpViewModel.signUpAsPendingUser(username, email, password, firstName, lastName, birthDate, profilePicturePath, promo, analytics);
            }
            else {
                MyUtilities.showCenteredToastLong("Completare prima tutti i campi evidenziati in rosso.", getContext());
            }



            try {

                //

//                mStatusTextView.setText(getString(R.string.emailpassword_status_fmt,
//                        user.getEmail(), user.isEmailVerified()));

//
//                //
//                httpUrl = new HttpUrl.Builder()
//                        .scheme("https")
//                        .host(remoteConfigServer.getAzureHostName())
//                        .addPathSegments(remoteConfigServer.getPostgrestPath())
//                        .addPathSegment(dbFunction)
//                        .build();
//

//
//                Request request = HttpUtilities.buildPOSTrequest(httpUrl, requestBody, remoteConfigServer.getGuestToken());
//
//                //
//                Call call = httpClient.newCall(request);
//                call.enqueue(this);

            } catch (Exception e) {
                System.out.println("failed");
                e.printStackTrace();
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
        }
    }// end onClick()

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

    }





    //--------------------------------------------------------------- METHODS

    void fetchImageFromGallery(View view){
//        Intent intent = new Intent();
//        intent.setType("image/*");
//        intent.setAction(Intent.ACTION_GET_CONTENT);

        Intent getIntent = new Intent(Intent.ACTION_GET_CONTENT);
        getIntent.setType("image/*");

        Intent pickIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickIntent.setType("image/*");

        Intent chooserIntent = Intent.createChooser(getIntent, "Select Image");
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[] {pickIntent});



        if(checkPermissionForReadExtertalStorage()) {
            startActivityForResult(Intent.createChooser(chooserIntent, "Select Picture"), SELECT_PICTURE);
        }
        else {
            requestPermissionForReadExtertalStorage();
            startActivityForResult(Intent.createChooser(chooserIntent, "Select Picture"), SELECT_PICTURE);
        }


    }

    public void requestPermissionForReadExtertalStorage() {
        try {
            ActivityCompat.requestPermissions((Activity) getContext(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 11);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    public boolean checkPermissionForReadExtertalStorage() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int result = getContext().checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE);
            return result == PackageManager.PERMISSION_GRANTED;
        }
        return false;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == SELECT_PICTURE) {
                Uri selectedImageURI = data.getData();
                imageUrl = "-";
                try {
                    imageUrl = getRealPathFromURI(getContext(), selectedImageURI);
                    Glide.with(this)  //2
                            .load(imageUrl) //3
                            .fallback(R.drawable.broken_image)
                            .placeholder(R.drawable.placeholder_image)
                            .circleCrop() //4
                            .into(imageViewProfilePicture); //8
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public String getRealPathFromURI(Context context, Uri contentUri) {
        Cursor cursor = null;
        try {
            String[] proj = { MediaStore.Images.Media.DATA };
            cursor = context.getContentResolver().query(contentUri,  proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
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
        editTextUsername.setText("foo");
        editTextEmail.setText("noto42@outlook.com");
        editTextPassword.setText("aaaaaaa");
        editTextRepeatPassword.setText("aaaaaaa");
        editTextFirstName.setText("mrfoo");
        editTextLastName.setText("bar");
        editTextBirthDate.setText("1-1-1970");
    }

    @Override
    public void onFailure(@NotNull Call call, @NotNull IOException e) {
        System.out.println("failed");

    }

    @Override
    public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
        String responseData = response.body().string();
        if (response.isSuccessful()) {
            System.out.println(responseData);
        }
        else {
            System.out.println(responseData);
        }
    }


}// end signUpFragment class