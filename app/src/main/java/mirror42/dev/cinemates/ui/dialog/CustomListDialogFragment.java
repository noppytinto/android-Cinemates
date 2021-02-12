package mirror42.dev.cinemates.ui.dialog;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import mirror42.dev.cinemates.R;

public class CustomListDialogFragment extends DialogFragment {
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();

        builder.setView(inflater.inflate(R.layout.layout_custom_list_creation_dialog, null))
                .setPositiveButton("Crea", (dialog, id) -> {
                    // FIRE ZE MISSILES!
                })
                .setNegativeButton("Annulla", (dialog, id) -> {
                    // User cancelled the dialog
                });
        // Create the AlertDialog object and return it
        return builder.create();
    }


}// end CustomListDialogFragment class
