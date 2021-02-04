package mirror42.dev.cinemates.ui.post;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;

import mirror42.dev.cinemates.R;
import mirror42.dev.cinemates.model.WatchlistPost;
import mirror42.dev.cinemates.utilities.MyUtilities;

public class WatchlistPostFragment extends Fragment {
    private WatchlistPostViewModel watchlistPostViewModel;
    private ImageView imageViewProfile;
    private TextView textViewFullname;
    private TextView textViewPublishDate;
    private ImageView imageViewPoster;

    public static WatchlistPostFragment newInstance() {
        return new WatchlistPostFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_watchlist_post, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        imageViewProfile = view.findViewById(R.id.include_watchlistPost_owner).findViewById(R.id.imageView_postOwnerLayout_profilePicture);
        textViewFullname = view.findViewById(R.id.include_watchlistPost_owner).findViewById(R.id.textView_postOwnerLayout_username);
        textViewPublishDate = view.findViewById(R.id.include_watchlistPost_owner).findViewById(R.id.textView_postOwnerLayout_publishDate);
        imageViewPoster = view.findViewById(R.id.include_watchlistPost_content).findViewById(R.id.imageview_movieThumbnail);

        if(getArguments()!=null) {
            WatchlistPost post = (WatchlistPost) getArguments().getSerializable("watchlist_post_data");
            String posterURL = post.getMovie().getPosterURL();
            String profilePictureURL = post.getOwner().getProfilePicturePath();
            String fullname = post.getOwner().getFullName();
            long publishDate = post.getPublishDateMillis();

            textViewFullname.setText(fullname);
            textViewPublishDate.setText(MyUtilities.convertMillisToReadableTimespan(publishDate));

            try {
                Glide.with(getContext())  //2
                        .load(posterURL) //3
                        .fallback(R.drawable.broken_image)
                        .placeholder(R.drawable.placeholder_image)
                        .fitCenter() //4
                        .into(imageViewPoster); //8
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                Glide.with(getContext())  //2
                        .load(profilePictureURL) //3
                        .fallback(R.drawable.icon_user_dark_blue)
                        .placeholder(R.drawable.icon_user_dark_blue)
                        .circleCrop() //4
                        .into(imageViewProfile); //8
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        watchlistPostViewModel = new ViewModelProvider(this).get(WatchlistPostViewModel.class);
        // TODO: Use the ViewModel
    }

}// end WatchlistPostFragment class