package mirror42.dev.cinemates.ui.list;

import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

import mirror42.dev.cinemates.NavGraphDirections;
import mirror42.dev.cinemates.R;
import mirror42.dev.cinemates.adapter.RecyclerAdapterCustomLists;
import mirror42.dev.cinemates.model.list.CustomList;
import mirror42.dev.cinemates.ui.dialog.CustomListDialogFragment;
import mirror42.dev.cinemates.ui.login.LoginViewModel;

public class CustomListBrowserFragment extends Fragment
        implements CustomListDialogFragment.CustomListDialogListener,
        RecyclerAdapterCustomLists.CustomListCoverListener,
        View.OnClickListener {
    private CustomListBrowserViewModel customListBrowserViewModel;
    private LoginViewModel loginViewModel;
    private FloatingActionButton buttonAdd;
    private RecyclerView recyclerView;
    private RecyclerAdapterCustomLists recyclerAdapterCustomLists;
    private String newListName;
    private String newListDescription;
    private boolean isPrivate;





    //------------------------------------------------------------- ANDROID METHODS

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.custom_list_browser_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        loginViewModel = new ViewModelProvider(requireActivity()).get(LoginViewModel.class);
        buttonAdd = view.findViewById(R.id.floatingActionButton_customListBrowserFragment_add);

        if(getArguments()!=null) {
            initRecycleView(view);

            CustomListBrowserFragmentArgs args = CustomListBrowserFragmentArgs.fromBundle(getArguments());
            String fetchMode = args.getFetchMode();
            setUpFragment(fetchMode);

        }

    }

    private void setUpFragment(String fetchMode) {
        switch (fetchMode) {
            case "fetch_my_custom_lists": {
                initForFetchModeMyCustomLists();

            }
                break;
            case "fetch_subscribed_lists": {
                initForFetchModeMySubscribedLists();
            }
                break;
        }
    }


    private void initForFetchModeMyCustomLists() {
        buttonAdd.setOnClickListener(this);
        customListBrowserViewModel = new ViewModelProvider(this).get(CustomListBrowserViewModel.class);
        customListBrowserViewModel.getObservableFetchStatus().observe(getViewLifecycleOwner(), fetchStatus -> {
            switch (fetchStatus) {
                case SUCCESS: {
                    ArrayList<CustomList> lists = customListBrowserViewModel.getCustomList();
                    if(lists!=null) {
                        recyclerAdapterCustomLists.loadNewData(lists);
                    }
                }
                break;
                case FAILED:
                    break;
            }
        });

        customListBrowserViewModel.getObservableTaskStatus().observe(getViewLifecycleOwner(), taskStatus -> {
            switch (taskStatus) {
                case SUCCESS: {
                    createCustomListPlaceholder(newListName, newListDescription);
                    moveRecyclerToBottom();
                    showCenteredToast("lista creata");
                    break;
                }
                case FAILED: {
                    showCenteredToast("errore creazione lista");
                }
            }
        });

        customListBrowserViewModel.fetchMyCustomLists(loginViewModel.getLoggedUser());
    }

    private void initForFetchModeMySubscribedLists() {
        buttonAdd.setVisibility(View.GONE);
//        customListBrowserViewModel = new ViewModelProvider(this).get(CustomListBrowserViewModel.class);
//        customListBrowserViewModel.getObservableFetchStatus().observe(getViewLifecycleOwner(), fetchStatus -> {
//            switch (fetchStatus) {
//                case SUCCESS: {
//                    ArrayList<CustomList> lists = customListBrowserViewModel.getCustomList();
//                    if(lists!=null) {
//                        recyclerAdapterCustomLists.loadNewData(lists);
//                    }
//                }
//                break;
//                case FAILED:
//                    break;
//            }
//        });
//
//        customListBrowserViewModel.getObservableTaskStatus().observe(getViewLifecycleOwner(), taskStatus -> {
//            switch (taskStatus) {
//                case SUCCESS: {
//                    createCustomListPlaceholder(newListName, newListDescription);
//                    moveRecyclerToBottom();
//                    showCenteredToast("lista creata");
//                    break;
//                }
//                case FAILED: {
//                    showCenteredToast("errore creazione lista");
//                }
//            }
//        });

        // TODO: customListBrowserViewModel.fetchSubscribedLists(loginViewModel.getLoggedUser());
    }

    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // hide notification(0) and user(1) icon
        menu.getItem(0).setVisible(false);
        menu.getItem(1).setVisible(false);
    }



    //------------------------------------------------------------- MY METHODS

    private void initRecycleView(View view) {
        // defining Recycler view
        recyclerView = view.findViewById(R.id.recyclerView_customListBrowser);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerAdapterCustomLists = new RecyclerAdapterCustomLists(new ArrayList<>(), getContext(), this);
        recyclerView.setAdapter(recyclerAdapterCustomLists);
    }


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
    public void onPositiveButtonClicked(String listName, String listDescription, boolean isChecked) {
        // PRECONDITIONS:
        // listName and listDescription will alwaysbe  non-empty
        // checks are made up front

        isPrivate = isChecked;
        customListBrowserViewModel.createNewList(listName , listDescription, isChecked, loginViewModel.getLoggedUser());
        newListName = listName;
        newListDescription = listDescription;
    }

    private void createCustomListPlaceholder(String name, String description) {
        CustomList placeholder = new CustomList();
        placeholder.setName(name);
        placeholder.setDescription(description);
        placeholder.setIsPrivate(isPrivate);
        placeholder.setOwner(loginViewModel.getLoggedUser());
        //
        recyclerAdapterCustomLists.addPlaceholderItem(placeholder);
    }


    @Override
    public void onCoverClicked(int position) {
        CustomList clickedList = recyclerAdapterCustomLists.getList(position);

        if(clickedList!=null) {
            NavGraphDirections.ActionGlobalListFragment listFragment =
                    NavGraphDirections.actionGlobalListFragment(clickedList, "", "");
            NavHostFragment.findNavController(CustomListBrowserFragment.this).navigate(listFragment);
        }
        else showCenteredToast("impossibile aprire lista");
    }

    private void moveRecyclerToBottom() {
        if(recyclerAdapterCustomLists.getItemCount()>0) {
            recyclerView.smoothScrollToPosition(recyclerAdapterCustomLists.getItemCount()-1);
        }
    }


    @Override
    public void onClick(View v) {
        if(v.getId() == buttonAdd.getId()) {
            showCreateListDialog();
        }
    }
}// end CustomListBrowserFragment class