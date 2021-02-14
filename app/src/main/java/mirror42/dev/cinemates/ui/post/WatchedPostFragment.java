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

import com.bumptech.glide.Glide;

import mirror42.dev.cinemates.R;
import mirror42.dev.cinemates.model.WatchedPost;
import mirror42.dev.cinemates.utilities.MyUtilities;

public class WatchedPostFragment extends Fragment {
    private ImageView imageViewProfile;
    private TextView textViewFullname;
    private TextView textViewPublishDate;
    private ImageView imageViewPoster;
    private TextView textPostDescription;
    private TextView textViewMovieTitle;
    private TextView textViewMovieOverview;

    public static WatchedPostFragment newInstance() {
        return new WatchedPostFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_watched_post, container, false);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        imageViewProfile = view.findViewById(R.id.include_watchedPostFragment_owner).findViewById(R.id.imageView_postOwnerLayout_profilePicture);
        textViewFullname = view.findViewById(R.id.include_watchedPostFragment_owner).findViewById(R.id.textView_postOwnerLayout_username);
        textViewPublishDate = view.findViewById(R.id.include_watchedPostFragment_owner).findViewById(R.id.textView_postOwnerLayout_publishDate);
        imageViewPoster = view.findViewById(R.id.include_watchedPostFragment_content).findViewById(R.id.imageview_movieThumbnail);
        textPostDescription = view.findViewById(R.id.include_watchedPostFragment_content).findViewById(R.id.textView_contentListPost_description);
        textViewMovieTitle = view.findViewById(R.id.include_watchedPostFragment_content).findViewById(R.id.textView_contentListPost_movieTitle);
        textViewMovieOverview = view.findViewById(R.id.include_watchedPostFragment_content).findViewById(R.id.textView_contentListPost_movieOverview);

        if(getArguments()!=null) {
            WatchedPost post = (WatchedPost) getArguments().getSerializable("watched_post_data");
            String posterURL = post.getMovie().getPosterURL();
            String profilePictureURL = post.getOwner().getProfilePicturePath();
            String fullname = post.getOwner().getFullName();
            String postDescription = post.getDescription();
            long publishDate = post.getPublishDateMillis();

            textViewFullname.setText(fullname);
            textViewPublishDate.setText(MyUtilities.convertMillisToReadableTimespan(publishDate));
            textPostDescription.setText(postDescription);
//            String movieTitle = post.getMovieTitle();
            String movieOverview = post.getMovieOverview();
            textViewMovieTitle.setVisibility(View.GONE);
            textViewMovieOverview.setText(movieOverview);
            textViewMovieOverview.setMaxLines(8);

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
    }
}
