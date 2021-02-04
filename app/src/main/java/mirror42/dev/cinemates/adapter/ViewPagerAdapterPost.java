package mirror42.dev.cinemates.adapter;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import mirror42.dev.cinemates.ui.post.PostFragment;
import mirror42.dev.cinemates.ui.post.ReactionListener;
import mirror42.dev.cinemates.ui.reaction.CommentsFragment;
import mirror42.dev.cinemates.ui.reaction.LikesFragment;

public class ViewPagerAdapterPost extends FragmentStateAdapter {
    private Bundle arguments;
    private ReactionListener listener;

    public ViewPagerAdapterPost(@NonNull FragmentManager fm,
                                @NonNull Lifecycle lifecycle, Bundle arguments, ReactionListener postFragment) {
        super(fm, lifecycle);
        this.arguments = arguments;
        this.listener = postFragment;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0: {
                CommentsFragment commentsFragment = CommentsFragment.getInstance(arguments, listener);
                ((PostFragment)listener).seOnPostCommentListener(commentsFragment);
            }
                return CommentsFragment.getInstance(arguments, listener);
            default :
                return LikesFragment.getInstance(arguments);
        }
    }

    @Override
    public int getItemCount() {
        return 2;
    }

}// end ViewPagerPost class
