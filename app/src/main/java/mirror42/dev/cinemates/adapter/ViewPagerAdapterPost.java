package mirror42.dev.cinemates.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import mirror42.dev.cinemates.ui.dialog.post.LikesFragment;
import mirror42.dev.cinemates.ui.home.post.PostFragment;

public class ViewPagerAdapterPost extends FragmentStateAdapter {

    public ViewPagerAdapterPost(@NonNull FragmentManager fm,
                                @NonNull Lifecycle lifecycle) {
        super(fm, lifecycle);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new PostFragment();
            default :
                return new LikesFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 2;
    }

}// end ViewPagerPost class
