package mirror42.dev.cinemates.ui.resetPassword;

import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import mirror42.dev.cinemates.R;
import mirror42.dev.cinemates.utilities.FirebaseAnalytics;


public class ResetPasswordFragment extends Fragment implements View.OnClickListener{
    private final String TAG = getClass().getSimpleName();
    private TextInputLayout textInputLayoutEmail;
    private TextInputEditText editTextEmail;
    private Button resetPassword;
    private View view;
    private ResetPasswordViewModel resetPasswordViewModel;
    private FirebaseAnalytics firebaseAnalytics;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_reset_password, container, false);
    }

    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // firebase logging
        firebaseAnalytics = FirebaseAnalytics.getInstance();
        firebaseAnalytics.logScreenEvent(this, getString(R.string.resetPassword_page_firebase_resetPassword), getContext());

        initViews(view);

        resetPasswordViewModel =  new ViewModelProvider(this).get(ResetPasswordViewModel.class);
        resetPasswordViewModel.getResetStatus().observe(getViewLifecycleOwner(), resetResult -> {
            if(resetResult == ResetPasswordViewModel.ResetResult.SUCCESS)
                showCenteredToast( "Reset password completo ");
            else if(resetResult == ResetPasswordViewModel.ResetResult.FAILED)
                showCenteredToast( "Reset password fallito ");
        });
    }


    @Override
    public void onClick(View v) {
        if(v.getId() == resetPassword.getId()){
            String email = editTextEmail.getText().toString();
            if(checkAllFieldsAreFilled() && isVaildMail( email))
                resetPasswordViewModel.resetPassword(email);
        }
    }

    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu) {
        super.onPrepareOptionsMenu(menu);
        MenuItem userIcon = menu.getItem(1);
        userIcon.setVisible(false);
    }


    private void initViews(View view){
        this.view = view;
        resetPassword = view.findViewById(R.id.button_resetPassword_startResetPassword);
        textInputLayoutEmail = view.findViewById(R.id.textInputLayout_resetPassword_email);
        editTextEmail = view.findViewById(R.id.editText_resetPassword_email);

        resetPassword.setOnClickListener(this);
    }

    private boolean checkAllFieldsAreFilled() {
        boolean res = true;

        if(editTextEmail.getText().toString().isEmpty()) {
            textInputLayoutEmail.setError("*");
            showCenteredToast("campo e-mail vuoto");
            res = false;
        }
        else {
            textInputLayoutEmail.setError(null);
        }

        return res;
    }

    private boolean isVaildMail(String email) {
        boolean isValid = false;
        String regex = "^(.+)@(.+)$";

        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(email);
        if (matcher.matches())
            isValid = true;
        else{
            textInputLayoutEmail.setError("*");
            showCenteredToast( "Formato mail non valido ");
        }
        return isValid;
    }

    public void showCenteredToast(String message) {
        final Toast toast = Toast.makeText(getContext(), message, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }


}