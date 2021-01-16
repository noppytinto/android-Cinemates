package mirror42.dev.cinemates.ui.home;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import mirror42.dev.cinemates.R;
import mirror42.dev.cinemates.model.Post;
import mirror42.dev.cinemates.model.User;
import mirror42.dev.cinemates.model.WatchlistPost;
import mirror42.dev.cinemates.ui.home.post.RecyclerAdapterPost;
import mirror42.dev.cinemates.ui.login.LoginViewModel;

public class HomeFragment extends Fragment {
    private final String TAG = this.getClass().getSimpleName();
    private HomeViewModel homeViewModel;
    private TextView textView;
    private Button updateButton;
    private EditText editText;
    private String greetings;
    private RecyclerAdapterPost recyclerAdapterPost;
    private View view;
    private LoginViewModel loginViewModel;







    //------------------------------------------------------------------------------- ANDROID METHODS

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate() called");


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView() called");

        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_home, container, false);

//        homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);
//        final TextView textView = root.findViewById(R.id.textview_home_fragment);
//        homeViewModel.getText().observe(getViewLifecycleOwner(), s -> {
////            textView.setText(s);
//        });

        return root;
    }



    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(TAG, "onViewCreated() called");
        this.view = view;

//        homeViewModel.applyRemoteConfig();

        // rx java tests
//        textView = view.findViewById(R.id.textView_homeFragment_test);
//        updateButton = view.findViewById(R.id.button_homeFragment_update);
//        editText = view.findViewById(R.id.editText_homeFragment);


        initRecyclerView();



    }// end onViewCreated()

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        loginViewModel = new ViewModelProvider(requireActivity()).get(LoginViewModel.class);
        loginViewModel.getLoginResult().observe(getViewLifecycleOwner(), loginResult -> {
            switch (loginResult) {
                case SUCCESS: {
                    User user = new User();
                    user.setUsername("Mario Rossi");
                    user.setProfilePicturePath("https://www.themoviedb.org/t/p/w600_and_h900_bestv2/vSe6sIsdtcoqBhuWRXynahFg8Vf.jpg");

                    WatchlistPost watchlistPost = new WatchlistPost();
                    watchlistPost.setPostType(Post.PostType.ADD_TO_LIST_WATCHLIST);
                    watchlistPost.setOwner(user);
                    watchlistPost.setPublishDateMillis(1610703153494L);
                    watchlistPost.setDescription("ha aggiunt 5 film alla Watchlist.");
                    watchlistPost.setThumbnail_1_url("https://www.themoviedb.org/t/p/w600_and_h900_bestv2/mMWLGu9pFymqipN8yvISHsAaj72.jpg");
                    watchlistPost.setThumbnail_2_url("https://www.themoviedb.org/t/p/w600_and_h900_bestv2/k68nPLbIST6NP96JmTxmZijEvCA.jpg");
                    watchlistPost.setThumbnail_3_url("https://www.themoviedb.org/t/p/w600_and_h900_bestv2/8UlWHLMpgZm9bx6QYh0NFoq67TZ.jpg");

                    ArrayList<Post> arrayList = new ArrayList<>();
                    arrayList.add(watchlistPost);

                    recyclerAdapterPost.loadNewData(arrayList);
                }

                    break;
                case FAILED:
                    break;
            }
        });
    }// end onActivityCreated()




    //------------------------------------------------------------------------------- METHODS

    private void initRecyclerView() {
        // defining Recycler view
        RecyclerView recyclerView = view.findViewById(R.id.recyclerView_homeFragment);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // adding recycle listener for touch detection
        recyclerAdapterPost = new RecyclerAdapterPost(new ArrayList<>(), getContext());
        recyclerView.setAdapter(recyclerAdapterPost);
    }





}// end HomeFragment class
