package mirror42.dev.cinemates.ui.changePassword;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;

import mirror42.dev.cinemates.R;
import mirror42.dev.cinemates.ui.login.LoginViewModel;


public class changePasswordFragment extends Fragment implements
        View.OnClickListener{

    private final String TAG = getClass().getSimpleName();
    private Button changePassword;
    private LoginViewModel loginViewModel;
    private View view;

    public changePasswordFragment() {

    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_change_password, container, false);
    }

    @Override
    public void onClick(View v) {

    }
}