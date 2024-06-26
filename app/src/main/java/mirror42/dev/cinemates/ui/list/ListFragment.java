package mirror42.dev.cinemates.ui.list;

import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.view.ActionMode;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.util.ArrayList;

import mirror42.dev.cinemates.MainActivity;
import mirror42.dev.cinemates.NavGraphDirections;
import mirror42.dev.cinemates.R;
import mirror42.dev.cinemates.adapter.RecyclerAdapterMoviesList;
import mirror42.dev.cinemates.model.User;
import mirror42.dev.cinemates.model.list.CustomList;
import mirror42.dev.cinemates.model.list.MoviesList;
import mirror42.dev.cinemates.model.tmdb.Movie;
import mirror42.dev.cinemates.ui.dialog.RecommendListDialogFragment;
import mirror42.dev.cinemates.ui.explore.ExploreFragmentDirections;
import mirror42.dev.cinemates.ui.login.LoginViewModel;
import mirror42.dev.cinemates.utilities.FirebaseAnalytics;
import mirror42.dev.cinemates.utilities.ImageUtilities;

public class ListFragment extends Fragment implements
        RecyclerAdapterMoviesList.ClickAdapterListener,
        CompoundButton.OnCheckedChangeListener, RecommendListDialogFragment.RecommendListDialogListener, View.OnClickListener {
    private final String TAG = getClass().getSimpleName();
    private RecyclerAdapterMoviesList recyclerAdapterMoviesList;
    private ArrayList<Movie> selectedMovies;
    private ListViewModel listViewModel;
    private LoginViewModel loginViewModel;
    private ActionModeCallback actionModeCallback;
    private ActionMode actionMode;
    private MoviesList currentList;
    private FloatingActionButton recommendButton;
    private Button subscribeButton;
    private Button unsubscribeButton;
    private CircularProgressIndicator progressIndicator;
    private SwitchMaterial isPrivateSwitch;
    private TextView textViewListName;
    private TextView textViewListDescription;
    private boolean navigatedToMovieDetailsFragment;
    private User listOwner;
    private boolean deleteAllowed;
    private TextView bannerDeleteMovie;
    private boolean listIsMine;
    private View includeEmptyMessage;
    private ImageView imageViewEmptyMessage;
    private MoviesList.ListType listType;




    //--------------------------------------------------------------------------------------- ANDROID METHODS

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //
        init(view);

    }// end onViewCreated()

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        //
        if(getArguments() != null) {
            ListFragmentArgs args = ListFragmentArgs.fromBundle(getArguments());
            currentList = args.getList();

            if(currentList != null) {
                listOwner = currentList.getOwner();
                listType = currentList.getListType();

                //
                if(listOwner!=null) {
                    User loggedUser = loginViewModel.getLoggedUser();
                    listIsMine = loggedUser.getUsername().equals(listOwner.getUsername());
                }
                else {
                    listIsMine = true;
                }

                //
                setupListAppearance(currentList);

                //
                if(currentList.isEmpty()) {} // ignore
                else populateList(currentList);
            }
        }
    }// end onActivityCreated()

    @Override
    public void onResume() {
        super.onResume();
        hideMainToolbar();
        navigatedToMovieDetailsFragment = false;
    }

    @Override
    public void onPause() {
        super.onPause();
        if( ! navigatedToMovieDetailsFragment)
            showMainToolbar();
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if(isChecked) {
            setPrivate(true);
            hideRecommendButton();
        }
        else {
            setPrivate(false);
            showRecommendButton();
        }
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == recommendButton.getId()) {
            showRecommendListDialog();
        }
        else if(v.getId() == subscribeButton.getId()) {
            listViewModel.subscribeToList((CustomList) currentList, loginViewModel.getLoggedUser());
            listViewModel.getObservableTaskStatus().observe(getViewLifecycleOwner(), taskStatus -> {
                switch (taskStatus) {
                    case SUBSCRIBED:
                        showCenteredToast("iscritto con successo!");
                        hideSubscribeButton();
                        showUnubscribeButton();
                    break;
                    case FAILED_SUBSCRIPTION:
                        showCenteredToast("errore iscrizione!");
                        break;
                }
            });

        }
        else if(v.getId() == unsubscribeButton.getId()) {
            listViewModel.unsubscribeFromList((CustomList) currentList, loginViewModel.getLoggedUser());
            listViewModel.getObservableTaskStatus().observe(getViewLifecycleOwner(), taskStatus -> {
                switch (taskStatus) {
                    case UNSUBSCRIBED:
                        showCenteredToast("iscrizione cancellata!");
                        showSubscribeButton();
                        hideUnsubscribeButton();
                        break;
                    case FAILED_UNSUBSCRIPTION:
                        showCenteredToast("errore cancellazione iscrizione!");
                        break;
                }
            });

        }

    }






    //--------------------------------------------------------------------------------------- METHODS

    private void init(View view) {
        deleteAllowed = true;
        recommendButton = view.findViewById(R.id.floatingActionButton_listFragment_recommend);
        subscribeButton = view.findViewById(R.id.button_listFragment_subscribe);
        unsubscribeButton = view.findViewById(R.id.button_listFragment_unsubscribe);
        progressIndicator = view.findViewById(R.id.progressIndicator_listFragment);

        isPrivateSwitch = view.findViewById(R.id.switch_listFragment_isPrivate);
        bannerDeleteMovie = view.findViewById(R.id.textView_listFragment_deleteMessage);
        textViewListName = view.findViewById(R.id.textView_listFragment_listName);
        textViewListDescription = view.findViewById(R.id.textView_listFragment_description);
        loginViewModel = new ViewModelProvider(requireActivity()).get(LoginViewModel.class);
        listViewModel = new ViewModelProvider(this).get(ListViewModel.class);
        selectedMovies = new ArrayList<>();
        isPrivateSwitch.setOnCheckedChangeListener(this);
        recommendButton.setOnClickListener(this);
        subscribeButton.setOnClickListener(this);
        unsubscribeButton.setOnClickListener(this);
        imageViewEmptyMessage = view.findViewById(R.id.include_listFragment_emptyMessage).findViewById(R.id.imageView_emptyMessage);
        includeEmptyMessage = view.findViewById(R.id.include_listFragment_emptyMessage);
        //
        initRecyclerView(view);

        //
        FirebaseAnalytics firebaseAnalytics = FirebaseAnalytics.getInstance();
        firebaseAnalytics.logScreenEvent(this, "List page", getContext());
    }

    void initRecyclerView(View view) {
        // defining HORIZONTAL layout manager for recycler
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 3);
        gridLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);

        // defining Recycler view
        RecyclerView recyclerView = view.findViewById(R.id.recyclerView_listFragment);
        recyclerView.setLayoutManager(gridLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        actionModeCallback = new ActionModeCallback();

        // assigning adapter to recycle
        recyclerAdapterMoviesList = new RecyclerAdapterMoviesList(new ArrayList<>(), getContext(), this);
        recyclerView.setAdapter(recyclerAdapterMoviesList);
    }

    private void setupListAppearance(MoviesList list) {
        //
        setListNameAndDescription(list);

        //
        if(listIsMine) allowDeleteMovies();
        else disallowDeleteMovies();

        // setup buttons
        if(listType == MoviesList.ListType.CL) {
            // check list ownership
            if(listIsMine) {
                showIsPrivateSwitchButton();
                if(((CustomList)currentList).isPrivate())
                    isPrivateSwitch.setChecked(true);
                else showRecommendButton();
            }
            else {
                checkListSubscription();
            }
        }
    }

    private void checkListSubscription() {
        User loggerUser = loginViewModel.getLoggedUser();
        listViewModel.checkMySubscriptionToThisList((CustomList) currentList, loggerUser);
        listViewModel.getObservableTaskStatus().observe(getViewLifecycleOwner(), taskStatus -> {
            switch (taskStatus) {
                case ALREADY_SUBSCRIBED:
                    hideSubscribeButton();
                    showUnubscribeButton();
                    break;
                case NOT_SUBSCRIBED:
                    showSubscribeButton();
                    hideUnsubscribeButton();
                    break;
                case SUBSCRIPTION_CHECK_FAILED:
                    showCenteredToast("errore controllo iscrizione!");
                    break;
            }
        });
    }

    private void setListNameAndDescription(MoviesList list) {
        // setup banner and empty message
        if(listIsMine)
            if(list.isEmpty()) {
                bannerDeleteMovie.setVisibility(View.GONE);
                showEmptyMessageMyList();
            }
            else {
                bannerDeleteMovie.setVisibility(View.VISIBLE);
                hideEmptyMessage();
            }
        else {
            if(list.isEmpty()) showEmptyMessageUserList();
            else hideEmptyMessage();

            bannerDeleteMovie.setVisibility(View.GONE);
        }

        //
        switch (listType) {
            case WL:
                //
                textViewListName.setText("Watchlist");
                break;
            case FV:
                //
                textViewListName.setText("Preferiti");
                break;
            case WD:
                //
                textViewListName.setText("Visti");
                break;
            case CL:
                //
                textViewListName.setText(((CustomList)list).getName());
                textViewListDescription.setVisibility(View.VISIBLE);
                textViewListDescription.setText(((CustomList)list).getDescription());
                break;
        }
    }

    private void populateList(MoviesList list) {
        ArrayList<Movie> movies = list.getMovies();
        recyclerAdapterMoviesList.loadNewData(movies);
    }

    private void navigateToMovieDetailsFragment(Movie targetMovie) {
        navigatedToMovieDetailsFragment = true;
        NavGraphDirections.AnywhereToMovieDetailsFragment
                movieDetailsFragment = ExploreFragmentDirections.anywhereToMovieDetailsFragment(targetMovie);
        NavHostFragment.findNavController(this).navigate(movieDetailsFragment);
    }

    private void setPrivate(boolean value) {
        CustomList newList = new CustomList((CustomList) currentList);
        newList.setIsPrivate(value);
        // TODO: add setName/setDescription, if you want to change them

        listViewModel.updateCustomListDetails((CustomList) currentList, newList, loginViewModel.getLoggedUser());
    }

    public void showRecommendListDialog() {
        DialogFragment newFragment = new RecommendListDialogFragment(this, ((CustomList)currentList).getName());
        newFragment.show(requireActivity().getSupportFragmentManager(), "RecommendListDialogFragment");
    }

    @Override
    public void onRecommendButtonOnDialogClicked() {

    }

    @Override
    public void onItemClicked(int position) {
        //if actionmode is diabled, just navigate to the movie details page
        if(actionMode==null) {
            // enable movie details navigation
            try {
                //
                Movie movieSelected = recyclerAdapterMoviesList.getMovie(position);

                //
                FirebaseAnalytics firebaseAnalytics = FirebaseAnalytics.getInstance();
                firebaseAnalytics.logSelectedMovie(movieSelected, "selected movie in List page", this, getContext());

                //
                showMainToolbar();
                navigateToMovieDetailsFragment(movieSelected);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } //otw select item during actionmode enabled
        else {
            enableActionMode(position);
        }
    }

    @Override
    public void onItemLongClicked(int position) {
        if(deleteAllowed)
            enableActionMode(position);
    }



    //---------------------------------------------------- action mode

    public class ActionModeCallback implements ActionMode.Callback {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            mode.getMenuInflater().inflate(R.menu.menu_movies_list, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            hideMainToolbar();
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.menuItem_listMenu_delete:
                    // delete all the selected rows
                    deleteItems();
                    mode.finish();
                    return true;
                case R.id.menuItem_listMenu_selectAll:
                    selectAll();
                    return true;
                default:
                    return false;
            }
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            recyclerAdapterMoviesList.clearSelections();
            actionMode = null;
            bannerDeleteMovie.setText("Tieni premuto su un elemento per modificare la lista.");
        }
    }

    private void enableActionMode(int position) {
        if (actionMode == null) {
            actionMode = ((MainActivity)getActivity()).startSupportActionMode(actionModeCallback);
            bannerDeleteMovie.setText("Seleziona gli elementi da eliminare.");
        }
        toggleSelection(position);
    }

    private void toggleSelection(int position) {
        recyclerAdapterMoviesList.toggleSelection(position);

        int count = recyclerAdapterMoviesList.getSelectedItemCount();
        if (count == 0) {
            actionMode.finish();
            actionMode = null;
        } else {
            actionMode.setTitle("selezionati: " + count);
            actionMode.invalidate();
        }
    }

    private void selectAll() {
        // select all items in recycler
        recyclerAdapterMoviesList.selectAll();

        // specify actionmode behaviour on items selected
        int count = recyclerAdapterMoviesList.getSelectedItemCount();
        try {
            if (count == 0) {
                actionMode.finish();
            }
            else {
                actionMode.setTitle("selezionati: " + count);
                actionMode.invalidate();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        actionMode = null;
    }

    private void deleteItems() {
        // get selected item (get indexes)
        ArrayList<Integer> selectedItemPositions = recyclerAdapterMoviesList.getSelectedItems();
        selectedMovies = new ArrayList<>();

        // remove all items in the movies list
        for (int i = selectedItemPositions.size() - 1; i >= 0; i--) {
            recyclerAdapterMoviesList.removeData(selectedItemPositions.get(i));
        }
        //
        selectedMovies = recyclerAdapterMoviesList.getcurrentSelectedMovies();
        User loggedUser = loginViewModel.getObservableLoggedUser().getValue();
        listViewModel.removeMoviesFromList(selectedMovies, currentList, loggedUser);

        // and notify recycler
        recyclerAdapterMoviesList.notifyDataSetChanged();
        actionMode = null;

        // check empty list
        boolean listIsEmpty = recyclerAdapterMoviesList.listIsEmpty();

        // setup banner and empty message
        if(listIsMine)
            if(listIsEmpty) {
                bannerDeleteMovie.setVisibility(View.GONE);
                showEmptyMessageMyList();
            }
            else {
                bannerDeleteMovie.setVisibility(View.VISIBLE);
                hideEmptyMessage();
            }
        else {
            if(listIsEmpty) showEmptyMessageUserList();
            else hideEmptyMessage();
            bannerDeleteMovie.setVisibility(View.GONE);
        }
    } // end deleteItems()




    //----------------------------------------------------

    private void showRecommendButton() {recommendButton.setVisibility(View.VISIBLE);}

    private void hideRecommendButton() {recommendButton.setVisibility(View.GONE);}

    private void showIsPrivateSwitchButton() {isPrivateSwitch.setVisibility(View.VISIBLE);}

    private void hideIsPrivateSwitchButton() {isPrivateSwitch.setVisibility(View.GONE);}

    private void showSubscribeButton() {subscribeButton.setVisibility(View.VISIBLE);}

    private void hideSubscribeButton() {subscribeButton.setVisibility(View.GONE);}

    private void showUnubscribeButton() {unsubscribeButton.setVisibility(View.VISIBLE);}

    private void hideUnsubscribeButton() {unsubscribeButton.setVisibility(View.GONE);}

    public void hideMainToolbar() {
        try {
            ((MainActivity) requireActivity()).hideToolbar();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void showMainToolbar() {
        try {
            ((MainActivity) requireActivity()).showToolbar();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void showCenteredToast(String message) {
        final Toast toast = Toast.makeText(getContext(), message, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    private void hideEmptyMessage(){
        includeEmptyMessage.setVisibility(View.GONE);
    }

    private void showEmptyMessageMyList(){
        includeEmptyMessage.setVisibility(View.VISIBLE);
        final int EMPTY_MESSAGE_IMAGE = R.drawable.empty_default_list_message;
        ImageUtilities.loadRectangularImageInto(EMPTY_MESSAGE_IMAGE, imageViewEmptyMessage, requireContext());
    }

    private void showEmptyMessageUserList(){
        includeEmptyMessage.setVisibility(View.VISIBLE);
        final int EMPTY_MESSAGE_IMAGE = R.drawable.empty_custom_list_message;
        ImageUtilities.loadRectangularImageInto(EMPTY_MESSAGE_IMAGE, imageViewEmptyMessage, requireContext());
    }

    private void showProgressIndicator() {
        progressIndicator.setVisibility(View.VISIBLE);
    }

    private void hideProgressIndicator() {
        progressIndicator.setVisibility(View.GONE);
    }

    private void allowDeleteMovies() {
        deleteAllowed = true;
    }

    private void disallowDeleteMovies() {
        deleteAllowed = false;
    }

}// end ListFragment class