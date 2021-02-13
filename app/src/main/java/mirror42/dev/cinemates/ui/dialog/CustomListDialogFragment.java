package mirror42.dev.cinemates.ui.dialog;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.jetbrains.annotations.NotNull;

import mirror42.dev.cinemates.R;

public class CustomListDialogFragment extends DialogFragment implements View.OnClickListener {
    private Button negativeButton;
    private Button positiveButton;
    private TextInputLayout nameTextInputLayout;
    private TextInputLayout descriptionTextInputLayout;
    private TextInputEditText nameTextInput;
    private TextInputEditText descriptionTextInput;
    private CustomListDialogListener listener;

    public interface CustomListDialogListener {
        void onPositiveButtonClicked(String listName, String listDescription);
    }



    //------------------------------------------------------------- CONSTRUCTORS

    public CustomListDialogFragment(CustomListDialogListener listener) {
        this.listener = listener;
    }




    //------------------------------------------------------------- ANDROID METHODS

    @NotNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.layout_custom_list_creation_dialog, null);
        positiveButton = view.findViewById(R.id.button_customListDialog_positive);
        negativeButton = view.findViewById(R.id.button_customListDialog_negative);
        nameTextInputLayout = view.findViewById(R.id.textInputLayout_customListDialog_name);
        descriptionTextInputLayout = view.findViewById(R.id.textInputLayout_customListDialog_description);
        nameTextInput = view.findViewById(R.id.editText_customListDialog_name);
        descriptionTextInput = view.findViewById(R.id.editText_customListDialog_description);
        positiveButton.setOnClickListener(this);
        negativeButton.setOnClickListener(this);

        builder.setView(view);
        // Create the AlertDialog object and return it
        return builder.create();
    }


    @Override
    public void onClick(View v) {
        if(v.getId() == positiveButton.getId()) {
            String name = nameTextInput.getText().toString();
            String description =  descriptionTextInput.getText().toString();
            if(fieldsAreValid(name, description)) {
                listener.onPositiveButtonClicked(name, description);
                dismiss();
            }
        }
        else if(v.getId() == negativeButton.getId()) {
            dismiss();
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }




    //------------------------------------------------------------- MY METHODS

    private boolean fieldsAreValid(String name, String description) {
        boolean result = true;

        if(name.isEmpty()) {
            nameTextInputLayout.setError("campo vuoto");
            result = false;
        }
        else {
            nameTextInputLayout.setError(null);
        }

        if(description.isEmpty()) {
            descriptionTextInputLayout.setError("campo vuoto");
            result = false;
        }
        else {
            descriptionTextInputLayout.setError(null);
        }

        return result;
    }

}// end CustomListDialogFragment class
