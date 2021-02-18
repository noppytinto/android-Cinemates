package mirror42.dev.cinemates.ui.changePassword;

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
import android.widget.CompoundButton;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import mirror42.dev.cinemates.R;
import mirror42.dev.cinemates.model.User;
import mirror42.dev.cinemates.ui.login.LoginViewModel;
import mirror42.dev.cinemates.ui.resetPassword.ResetPasswordViewModel;
import mirror42.dev.cinemates.utilities.FirebaseAnalytics;
import mirror42.dev.cinemates.utilities.MyUtilities;


public class changePasswordFragment extends Fragment implements
        View.OnClickListener{

    private final String TAG = getClass().getSimpleName();


    private TextInputLayout textInputLayoutActualPassword;
    private TextInputEditText editTextActualPassword;

    private TextInputLayout textInputLayoutNewPassword;
    private TextInputEditText editTextNewPassword;

    private TextInputLayout textInputLayoutRepPassword;
    private TextInputEditText editTextRepPassword;

    private Button changePassword;

    private View view;

    private ChangePasswordViewModel changePasswordViewModel;
    private LoginViewModel loginViewModel;

    private FirebaseAnalytics firebaseAnalytics;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu) {
        super.onPrepareOptionsMenu(menu);
        MenuItem userIcon = menu.getItem(1);
        MenuItem notifyIcon = menu.getItem(0);
        userIcon.setVisible(false);
        notifyIcon.setVisible(false);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_change_password, container, false);
    }

    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // firebase logging
        firebaseAnalytics = FirebaseAnalytics.getInstance();
        firebaseAnalytics.logScreenEvent(this, getString(R.string.changePassword_page_firebase_changePassword), getContext());

        initViews(view);

        loginViewModel = new ViewModelProvider(requireActivity()).get(LoginViewModel.class);

        changePasswordViewModel =  new ViewModelProvider(this).get(ChangePasswordViewModel.class);
        changePasswordViewModel.getResetStatus().observe(getViewLifecycleOwner(), resetResult -> {
            if(resetResult == ChangePasswordViewModel.ResetResult.SUCCESS){
                showCenteredToast( "Cambio password completo ");
                NavController navController = Navigation.findNavController(view);
                navController.popBackStack();
                navController.navigate(R.id.personalProfileFragment);

            }

            else if(resetResult == ChangePasswordViewModel.ResetResult.FAILED)
                showCenteredToast( "Cambio password fallito ");
        });

    }

    @Override
    public void onClick(View v) {
        if(v.getId() == changePassword.getId()){

            if(checkAllFieldsAreFilled() && checkActualPasswordMatch() && checkNewPasswordLength() && checkRepeatPasswordMatch()){
                String newPass =   editTextNewPassword.getText().toString();
                changePasswordViewModel.changePassword(loginViewModel.getLoggedUser(), newPass);
            }else
                showCenteredToast("Completare prima tutti i campi evidenziati in rosso.");
        }
    }




    private void initViews(View view){
        this.view = view;

        textInputLayoutActualPassword = view.findViewById(R.id.textInputLayout_changePasswordFragment_actualPassword);
        editTextActualPassword = view.findViewById(R.id.editText_changePasswordFragment_actualPassword);

        textInputLayoutNewPassword = view.findViewById(R.id.textInputLayout_changePasswordFragment_newPassword);
        editTextNewPassword = view.findViewById(R.id.editText_changePassword_newPassword);

        textInputLayoutRepPassword = view.findViewById(R.id.textInputLayout_changePasswordFragment_newPasswordRep);
        editTextRepPassword = view.findViewById(R.id.editText_changePassword_newPasswordRep);

        changePassword = view.findViewById(R.id.button_changePasswordFragment_startChangePassword);
        changePassword.setOnClickListener(this);

    }


    private boolean checkAllFieldsAreFilled() {
        boolean res = true;

        if(editTextActualPassword.getText().toString().isEmpty()) {
            textInputLayoutActualPassword.setError("*");
            res = false;
        }
        else {
            textInputLayoutActualPassword.setError(null);
        }

        if(editTextNewPassword.getText().toString().isEmpty()) {
            textInputLayoutNewPassword.setError("*");
            res = false;
        }
        else {
            textInputLayoutNewPassword.setError(null);
        }

        if(editTextRepPassword.getText().toString().isEmpty()) {
            textInputLayoutRepPassword.setError("*");
            res = false;
        }
        else {
            textInputLayoutRepPassword.setError(null);
        }

        return res;
    }

    private boolean checkRepeatPasswordMatch() {

        if( ! editTextRepPassword.getText().toString().equals(editTextNewPassword.getText().toString())) {
            textInputLayoutRepPassword.setError(getString(R.string.passwords_dont_match));
            return false;
        }
        else {
            textInputLayoutRepPassword.setError(null);
        }

        return true;
    }

    private boolean  checkNewPasswordLength() {

        if( editTextNewPassword.getText().toString().length()<6) {
            textInputLayoutNewPassword.setError(getString(R.string.passwordHelper));
            return false;
        }
        else {
            textInputLayoutNewPassword.setError(null);
        }

        return true;
    }

    private boolean checkActualPasswordMatch() {

        final String passwordHide = MyUtilities.SHA256encrypt(editTextActualPassword.getText().toString());

        if( ! passwordHide.equals(loginViewModel.getLoggedUser().getPassword())) {
            textInputLayoutActualPassword.setError(getString(R.string.passwords_dont_match));
            return false;
        }
        else {
            textInputLayoutActualPassword.setError(null);
        }

        return true;
    }



    public void showCenteredToast(String message) {
        final Toast toast = Toast.makeText(getContext(), message, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }



}