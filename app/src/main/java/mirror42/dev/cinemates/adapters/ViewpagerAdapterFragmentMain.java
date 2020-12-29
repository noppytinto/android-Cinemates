package mirror42.dev.cinemates.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import mirror42.dev.cinemates.ui.explore.ExploreFragment;
import mirror42.dev.cinemates.ui.home.HomeFragment;
import mirror42.dev.cinemates.ui.search.SearchFragment;


public class ViewpagerAdapterFragmentMain extends FragmentStateAdapter {
    public ViewpagerAdapterFragmentMain(Fragment fm) {
        super(fm);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new HomeFragment();
            case 1:
                return new ExploreFragment();
            default :
                return new SearchFragment();
        }
    }

    @Override
    public int getItemCount() {

        return 3;
    }

}// end ViewPagerAdapter class

