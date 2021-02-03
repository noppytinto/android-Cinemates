package mirror42.dev.cinemates.ui.home.post;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import mirror42.dev.cinemates.R;
import mirror42.dev.cinemates.adapter.RecyclerAdapterShowLikesDialog;
import mirror42.dev.cinemates.model.User;

public class WatchlistPostFragment extends Fragment implements RecyclerAdapterShowLikesDialog.ClickAdapterListener {
    private WatchlistPostViewModel mViewModel;
    private RecyclerAdapterShowLikesDialog recyclerView;




    //---------------------------------------------------------------------- ANDROID METHODS

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.watchlist_post_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initRecyclerView(view);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(WatchlistPostViewModel.class);
        // TODO: Use the ViewModel




        ArrayList<User> likes = new ArrayList<>();
        User like = new User();
        like.setFirstName("Mario");
        like.setLastName("Rossi");
        like.setUsername("mario");
        like.setProfilePictureURL("-");

        likes.add(like);
        likes.add(like);
        likes.add(like);
        likes.add(like);
        likes.add(like);
        likes.add(like);
        likes.add(like);
        likes.add(like);
        likes.add(like);
        likes.add(like);
        likes.add(like);
        likes.add(like);
        likes.add(like);
        likes.add(like);
        likes.add(like);
        likes.add(like);
        likes.add(like);
        likes.add(like);
        likes.add(like);
        likes.add(like);
        likes.add(like);
        likes.add(like);
        likes.add(like);
        likes.add(like);
        likes.add(like);
        likes.add(like);

        recyclerView.loadNewData(likes);
    }





    //---------------------------------------------------------------------- MY METHODS

    private void initRecyclerView(View view) {
        // defining Recycler view
        RecyclerView recyclerView = view.findViewById(R.id.recyclerView_showLikesDialogFragment);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
//        recyclerView.setNestedScrollingEnabled(false);

        // adding recycle listener for touch detection
        this.recyclerView = new RecyclerAdapterShowLikesDialog(new ArrayList<>(), getContext(), this);
        recyclerView.setAdapter(this.recyclerView);
    }

    @Override
    public void onItemClicked(int position) {

    }





}// end WatchlistPostFragment clasd