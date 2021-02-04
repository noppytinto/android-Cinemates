package mirror42.dev.cinemates.adapter;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import mirror42.dev.cinemates.ui.post.PostFragment;
import mirror42.dev.cinemates.ui.reaction.CommentsFragment;
import mirror42.dev.cinemates.ui.reaction.LikesFragment;

public class ViewPagerAdapterPost extends FragmentStateAdapter {
    private Bundle arguments;
    private PostFragment listener;

    public ViewPagerAdapterPost(@NonNull FragmentManager fm,
                                @NonNull Lifecycle lifecycle, Bundle arguments, PostFragment listener) {
        super(fm, lifecycle);
        this.arguments = arguments;
        this.listener = listener;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return CommentsFragment.getInstance(arguments, (CommentsFragment.CommentListener) listener);
            default :
                return LikesFragment.getInstance(arguments);
        }
    }

    @Override
    public int getItemCount() {
        return 2;
    }

}// end ViewPagerPost class
