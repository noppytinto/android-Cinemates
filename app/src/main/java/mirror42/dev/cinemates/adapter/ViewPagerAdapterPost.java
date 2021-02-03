package mirror42.dev.cinemates.adapter;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import mirror42.dev.cinemates.ui.dialog.post.CommentsFragment;
import mirror42.dev.cinemates.ui.dialog.post.LikesFragment;

public class ViewPagerAdapterPost extends FragmentStateAdapter {
    private Bundle arguments;

    public ViewPagerAdapterPost(@NonNull FragmentManager fm,
                                @NonNull Lifecycle lifecycle, Bundle arguments) {
        super(fm, lifecycle);
        this.arguments = arguments;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return CommentsFragment.getInstance(arguments);
            default :
                return LikesFragment.getInstance(arguments);
        }
    }

    @Override
    public int getItemCount() {
        return 2;
    }

}// end ViewPagerPost class
