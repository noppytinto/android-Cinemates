package mirror42.dev.cinemates.ui.list;

import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import mirror42.dev.cinemates.R;
import mirror42.dev.cinemates.ui.dialog.CustomListDialogFragment;

public class CustomListBrowserFragment extends Fragment implements CustomListDialogFragment.CustomListDialogListener {
    private CustomListBrowserViewModel mViewModel;
    FloatingActionButton buttonAdd;

    public static CustomListBrowserFragment newInstance() {
        return new CustomListBrowserFragment();
    }


    //------------------------------------------------------------- ANDROID MY METHODS

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.custom_list_browser_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        buttonAdd = view.findViewById(R.id.floatingActionButton_customListBrowserFragment_add);
        buttonAdd.setOnClickListener(v -> {
            // ignore v

            showCreateListDialog();

        });
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(CustomListBrowserViewModel.class);
        // TODO: Use the ViewModel
    }




    //------------------------------------------------------------- MY METHODS

    public void showCreateListDialog() {
        DialogFragment newFragment = new CustomListDialogFragment(this);
        newFragment.show(requireActivity().getSupportFragmentManager(), "CustomListDialogFragment");
    }

    public void showCenteredToast(String message) {
        final Toast toast = Toast.makeText(getContext(), message, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    @Override
    public void onPositiveButtonClicked(String listName, String listDescription) {
        // PRECONDITIONS:
        // listName and listDescription will alwaysbe  non-empty
        // checks are made up front

        showCenteredToast("nome: " + listName + "\ndescrizione: " + listDescription);
    }

}// end CustomListBrowserFragment class