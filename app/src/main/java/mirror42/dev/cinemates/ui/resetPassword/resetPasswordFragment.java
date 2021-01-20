package mirror42.dev.cinemates.ui.resetPassword;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import mirror42.dev.cinemates.R;
import mirror42.dev.cinemates.ui.search.SearchViewModel;
import mirror42.dev.cinemates.utilities.MyUtilities;


public class resetPasswordFragment extends Fragment implements
        View.OnClickListener{

    private final String TAG = getClass().getSimpleName();
    private TextInputLayout textInputLayoutEmail;
    private TextInputEditText editTextEmail;
    private Button resetPassword;
    private View view;

    private ResetPasswordViewModel resetPasswordViewModel;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_reset_password, container, false);
    }

    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);

        resetPasswordViewModel =  new ViewModelProvider(this).get(ResetPasswordViewModel.class);
        resetPasswordViewModel.getResetStatus().observe(getViewLifecycleOwner(), new Observer<ResetPasswordViewModel.ResetResult>() {
            @Override
            public void onChanged(ResetPasswordViewModel.ResetResult resetResult) {

                if(resetResult == ResetPasswordViewModel.ResetResult.SUCCESS)
                    MyUtilities.showCenteredToast( "Reset password completo ", getContext());
                else if(resetResult == ResetPasswordViewModel.ResetResult.FAILED)
                    MyUtilities.showCenteredToast( "Reset password fallito ", getContext());
            }
        });
    }


    @Override
    public void onClick(View v) {

        if(v.getId() == resetPassword.getId()){
            String email = editTextEmail.getText().toString();
            resetPasswordViewModel.resetPassword(email);
        }
    }


    private void initViews(View view){

        this.view = view;
        resetPassword = view.findViewById(R.id.button_resetPassword_startResetPassword);
        textInputLayoutEmail = view.findViewById(R.id.textInputLayout_resetPassword_email);
        editTextEmail = view.findViewById(R.id.editText_resetPassword_email);

        resetPassword.setOnClickListener(this);
    }
}