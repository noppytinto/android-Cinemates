package mirror42.dev.cinemates.ui.home;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.google.firebase.analytics.FirebaseAnalytics;

import mirror42.dev.cinemates.R;

public class HomeFragment extends Fragment {
    private final String TAG = this.getClass().getSimpleName();
    private FirebaseAnalytics mFirebaseAnalytics;

    private HomeViewModel homeViewModel;




    //------------------------------------------------------------------------ LIFECYCLE METHODS


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate() called");

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(getContext());

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView() called");

        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_home, container, false);

        homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        final TextView textView = root.findViewById(R.id.textview_home_fragment);
        homeViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });

        return root;
    }



    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(TAG, "onViewCreated() called");

        homeViewModel.applyRemoteConfig();

        logFirebaseScreenEvent();
    }




    private void logFirebaseScreenEvent() {
        // send to firebase analytics
        Bundle item = new Bundle();
        item.putString(FirebaseAnalytics.Param.SCREEN_CLASS, getClass().getSimpleName());
        item.putString(FirebaseAnalytics.Param.SCREEN_NAME, "Home tab");
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, item);
    }

}// end HomeFragment class