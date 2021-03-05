package mirror42.dev.cinemates.ui.list;

import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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
import com.google.android.material.progressindicator.CircularProgressIndicator;

import java.util.ArrayList;

import mirror42.dev.cinemates.MainActivity;
import mirror42.dev.cinemates.NavGraphDirections;
import mirror42.dev.cinemates.R;
import mirror42.dev.cinemates.adapter.RecyclerAdapterCustomLists;
import mirror42.dev.cinemates.model.User;
import mirror42.dev.cinemates.model.list.CustomList;
import mirror42.dev.cinemates.ui.dialog.CustomListDialogFragment;
import mirror42.dev.cinemates.ui.login.LoginViewModel;
import mirror42.dev.cinemates.utilities.ImageUtilities;
import mirror42.dev.cinemates.utilities.MyValues;

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
    private boolean areNotMyLists;
    private CircularProgressIndicator progressIndicator;
    private ImageView imageViewEmptyMessage;
    private View includeEmptyMessage;


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
        init(view);

        if(getArguments()!=null) {
            CustomListBrowserFragmentArgs args = CustomListBrowserFragmentArgs.fromBundle(getArguments());
            String fetchMode = args.getFetchMode();
            String profileOwnerUsername = args.getListOwner();

            setupView(fetchMode, profileOwnerUsername, view);

        }
    }

    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // hide notification(0) and user(1) icon
        menu.getItem(0).setVisible(false);
        menu.getItem(1).setVisible(false);
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == buttonAdd.getId()) {
            showCreateListDialog();
        }
    }



    //------------------------------------------------------------- MY METHODS

    private void init(View view) {
        buttonAdd = view.findViewById(R.id.floatingActionButton_customListBrowserFragment_add);
        progressIndicator = view.findViewById(R.id.progressIndicator_customListBrowser);
        imageViewEmptyMessage = view.findViewById(R.id.include_customListBrowser_emptyMessage).findViewById(R.id.imageView_emptyMessage);
        includeEmptyMessage = view.findViewById(R.id.include_customListBrowser_emptyMessage);
        loginViewModel = new ViewModelProvider(requireActivity()).get(LoginViewModel.class);
        customListBrowserViewModel = new ViewModelProvider(this).get(CustomListBrowserViewModel.class);
    }

    private void setupView(String fetchMode, String profileOwnerUsername, View view) {
        showProgressIndicator();
        switch (fetchMode) {
            case "fetch_my_custom_lists": {
                areNotMyLists = false;
                initRecycleView(view, areNotMyLists);
                fetchMyCustomLists();
            }
            break;
            case "fetch_subscribed_lists": {
                areNotMyLists = true;
                ((MainActivity)requireActivity()).setToolbarTitle("Liste che seguo");
                initRecycleView(view, areNotMyLists);
                fetchSubscribedLists();
            }
            break;
            case "fetch_friend_lists": {
                //note: "friend" are actually following users
                areNotMyLists = true;
                ((MainActivity)requireActivity()).setToolbarTitle("Liste di: @" + profileOwnerUsername);
                initRecycleView(view, areNotMyLists);
                fetchFriendCustomLists(profileOwnerUsername);
            }
            break;
        }
    }


    private void fetchMyCustomLists() {
        buttonAdd.setOnClickListener(this);
        customListBrowserViewModel.getObservableMyCustomListsFetchStatus().observe(getViewLifecycleOwner(), fetchStatus -> {
            switch (fetchStatus) {
                case SUCCESS: {
                    hideProgressIndicator();
                    hideEmptyMessage();
                    ArrayList<CustomList> lists = customListBrowserViewModel.getCustomList();
                    if(lists!=null) {
                        recyclerAdapterCustomLists.loadNewData(lists);
                    }
                }
                break;
                case NOT_EXISTS:
                    hideProgressIndicator();
                    showEmptyMessageCustomList();
                    break;
                case FAILED:
                    hideProgressIndicator();
                    showCenteredToast("caricamento liste fallito");
                    break;
            }
        });
        customListBrowserViewModel.fetchMyCustomLists(loginViewModel.getLoggedUser());
    }

    private void fetchSubscribedLists() {
        buttonAdd.setVisibility(View.GONE);
        customListBrowserViewModel.getObservableSubscribedFetchStatus().observe(getViewLifecycleOwner(), fetchStatus -> {
            switch (fetchStatus) {
                case SUCCESS: {
                    hideProgressIndicator();
                    hideEmptyMessage();
                    ArrayList<CustomList> lists = customListBrowserViewModel.getCustomList();
                    if(lists!=null) {
                        recyclerAdapterCustomLists.loadNewData(lists);
                    }

                    // reset fetch status
                    customListBrowserViewModel.setSubscribedFetchStatus(MyValues.FetchStatus.IDLE);
                }
                break;
                case NOT_EXISTS:
                    showEmptyMessageSubList();
                    hideProgressIndicator();
                    break;
                case FAILED:
                    hideProgressIndicator();
                    showCenteredToast("caricamento liste fallito");
                    break;
            }
        });

        customListBrowserViewModel.fetchSubscribedLists(loginViewModel.getLoggedUser());
    }

    private void fetchFriendCustomLists(String profileOwnerUsername) {
        buttonAdd.setVisibility(View.GONE);
        customListBrowserViewModel.getObservablePublicFetchStatus().observe(getViewLifecycleOwner(), fetchStatus -> {
            switch (fetchStatus) {
                case SUCCESS: {
                    hideProgressIndicator();
                    hideEmptyMessage();
                    ArrayList<CustomList> lists = customListBrowserViewModel.getCustomList();
                    if(lists!=null) {
                        recyclerAdapterCustomLists.loadNewData(lists);
                    }
                }
                break;
                case NOT_EXISTS:
                    hideProgressIndicator();
                    showEmptyMessagePublicList();
                    break;
                case FAILED:
                    hideProgressIndicator();
                    showCenteredToast("caricamento liste fallito");
                    break;
            }
        });
        customListBrowserViewModel.fetchFriendCustomLists(profileOwnerUsername, loginViewModel.getLoggedUser());
    }

    private void initRecycleView(View view, boolean areNotMyLists) {
        // defining Recycler view
        recyclerView = view.findViewById(R.id.recyclerView_customListBrowser);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerAdapterCustomLists = new RecyclerAdapterCustomLists(new ArrayList<>(), requireContext(), this, areNotMyLists);
        recyclerView.setAdapter(recyclerAdapterCustomLists);
//        recyclerAdapterCustomLists.clearList();
    }

    public void showCreateListDialog() {
        DialogFragment newFragment = new CustomListDialogFragment(this);
        newFragment.show(requireActivity().getSupportFragmentManager(), "CustomListDialogFragment");

        customListBrowserViewModel.getObservableTaskStatus().observe(getViewLifecycleOwner(), taskStatus -> {
            switch (taskStatus) {
                case SUCCESS: {
                    createCustomListPlaceholder(newListName, newListDescription);
                    moveRecyclerToBottom();
                    hideEmptyMessage();
                    showCenteredToast("lista creata");
                    break;
                }
                case FAILED: {
                    showCenteredToast("errore creazione lista");
                }
            }
        });
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
        CustomList clickedList = recyclerAdapterCustomLists.getItem(position);

        if(clickedList!=null) {
            NavGraphDirections.ActionGlobalListFragment listFragment =
                    NavGraphDirections.actionGlobalListFragment(clickedList, "", "");
            NavHostFragment.findNavController(CustomListBrowserFragment.this).navigate(listFragment);
        }
        else showCenteredToast("impossibile aprire lista");
    }

    @Override
    public void imageClicked(int position) {

        User userOwner = recyclerAdapterCustomLists.getItem(position).getOwner();
        NavGraphDirections.ActionGlobalUserProfileFragment userProfileFragment =
                NavGraphDirections.actionGlobalUserProfileFragment(userOwner.getUsername());
        NavHostFragment.findNavController(CustomListBrowserFragment.this).navigate(userProfileFragment);


    }

    private void moveRecyclerToBottom() {
        if(recyclerAdapterCustomLists.getItemCount()>0) {
            recyclerView.smoothScrollToPosition(recyclerAdapterCustomLists.getItemCount()-1);
        }
    }

    private void showProgressIndicator() {
        progressIndicator.setVisibility(View.VISIBLE);
    }

    private void hideProgressIndicator() {
        progressIndicator.setVisibility(View.GONE);
    }

    public void showCenteredToast(String message) {
        final Toast toast = Toast.makeText(getContext(), message, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    private void showEmptyMessageCustomList(){
        includeEmptyMessage.setVisibility(View.VISIBLE);
        final int EMPTY_MESSAGE_IMAGE = R.drawable.empty_custom_lists_message;
        ImageUtilities.loadRectangularImageInto(EMPTY_MESSAGE_IMAGE, imageViewEmptyMessage, requireContext());
    }

    private void hideEmptyMessage(){
        includeEmptyMessage.setVisibility(View.GONE);
    }

    private void showEmptyMessageSubList(){
        includeEmptyMessage.setVisibility(View.VISIBLE);
        final int EMPTY_MESSAGE_IMAGE = R.drawable.empty_sub_lists_message;
        ImageUtilities.loadRectangularImageInto(EMPTY_MESSAGE_IMAGE, imageViewEmptyMessage, requireContext());
    }

    private void showEmptyMessagePublicList(){
        includeEmptyMessage.setVisibility(View.VISIBLE);
        final int EMPTY_MESSAGE_IMAGE = R.drawable.empty_users_custom_lists_message;
        ImageUtilities.loadRectangularImageInto(EMPTY_MESSAGE_IMAGE, imageViewEmptyMessage, requireContext());
    }



}// end CustomListBrowserFragment class