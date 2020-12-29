package mirror42.dev.cinemates;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import mirror42.dev.cinemates.adapter.ViewpagerAdapterFragmentMain;


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


        tabLayout = view.findViewById(R.id.tablayout_fragment_main);
        viewPager = view.findViewById(R.id.viewpager_fragment_main);
        viewpagerAdapterFragmentMain = new ViewpagerAdapterFragmentMain(this);
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


    }
}