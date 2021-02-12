package mirror42.dev.cinemates.ui.list;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import mirror42.dev.cinemates.R;
import mirror42.dev.cinemates.ui.dialog.CustomListDialogFragment;

public class CustomListBrowserFragment extends Fragment {
    private CustomListBrowserViewModel mViewModel;
    FloatingActionButton buttonAdd;

    public static CustomListBrowserFragment newInstance() {
        return new CustomListBrowserFragment();
    }

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

            confirmFireMissiles();

        });
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(CustomListBrowserViewModel.class);
        // TODO: Use the ViewModel
    }

    public void confirmFireMissiles() {
        DialogFragment newFragment = new CustomListDialogFragment();
        newFragment.show(requireActivity().getSupportFragmentManager(), "custom list dialog");
    }


}