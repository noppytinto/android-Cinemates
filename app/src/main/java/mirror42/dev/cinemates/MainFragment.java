package mirror42.dev.cinemates;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import mirror42.dev.cinemates.adapter.ViewpagerAdapterFragmentMain;
import mirror42.dev.cinemates.ui.explore.ExploreFragment;
import mirror42.dev.cinemates.ui.home.HomeFragment;
import mirror42.dev.cinemates.ui.search.SearchFragment;
import mirror42.dev.cinemates.utilities.FirebaseAnalytics;


public class MainFragment extends Fragment {
    private ViewPager2 viewPager;
    private TabLayout tabLayout;
    private ViewpagerAdapterFragmentMain viewpagerAdapterFragmentMain;



    public MainFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_main, container, false);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        tabLayout = view.findViewById(R.id.tablayout_mainFragment);
        viewPager = view.findViewById(R.id.viewpager_mainFragment);
        FragmentManager fm = getChildFragmentManager();
        Lifecycle lifecycle = getViewLifecycleOwner().getLifecycle();
        viewpagerAdapterFragmentMain = new ViewpagerAdapterFragmentMain(fm, lifecycle);
        viewPager.setUserInputEnabled(false); // disables horiz. swipe to scroll tabs gestures
        viewPager.setAdapter(viewpagerAdapterFragmentMain);

        TabLayoutMediator tabLayoutMediator = new TabLayoutMediator(tabLayout, viewPager, new TabLayoutMediator.TabConfigurationStrategy() {
            @Override
            public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                switch (position) {
                    case 0:
                        tab.setIcon(R.drawable.home_icon_light_blue);
                        break;
                    case 1:
                        tab.setIcon(R.drawable.movie_icon_light_blue);
                        break;
                    case 2:
                        tab.setIcon(R.drawable.search_icon_light_blue);
                        break;
                    default:
                        tab.setText("");
                }
            }
        });
        tabLayoutMediator.attach();

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                FirebaseAnalytics firebaseAnalytics = FirebaseAnalytics.getInstance();

                switch (tab.getPosition()) {
                    case 0:
                        Log.d("tab analyzer", "clicked on home tab");
                        firebaseAnalytics.logScreenEvent(HomeFragment.class, "Home tab", getContext());
                        tab.setIcon(R.drawable.home_icon_light_blue);
                        break;
                    case 1:
                        Log.d("tab analyzer", "clicked on explore tab");
                        firebaseAnalytics.logScreenEvent(ExploreFragment.class, "Explore tab", getContext());
                        tab.setIcon(R.drawable.movie_icon_light_blue);
                        break;
                    case 2:
                        Log.d("tab analyzer", "clicked on search tab");
                        firebaseAnalytics.logScreenEvent(SearchFragment.class, "Search tab", getContext());
                        tab.setIcon(R.drawable.search_icon_light_blue);
                        break;
                    default:
                        tab.setText("");
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });


    }// end onViewCreated()
}// end MainFragment class