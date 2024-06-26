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
import mirror42.dev.cinemates.model.FollowPost;
import mirror42.dev.cinemates.utilities.MyUtilities;

public class FollowPostFragment extends Fragment {
    private final String TAG = this.getClass().getSimpleName();
    private ImageView imageViewProfile;
    private TextView textViewFullname;
    private TextView textViewPublishDate;
    private TextView textPostDescription;


    public static FollowPostFragment newInstance() {
        return new FollowPostFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_follow_post, container, false);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        imageViewProfile = view.findViewById(R.id.imageView_postOwnerLayout_profilePicture);
        textViewFullname = view.findViewById(R.id.textView_postOwnerLayout_username);
        textViewPublishDate = view.findViewById(R.id.textView_postOwnerLayout_publishDate);
        textPostDescription = view.findViewById(R.id.textView_contentFollowPost_description);

        if(getArguments()!=null) {
            FollowPost post = (FollowPost) getArguments().getSerializable("follow_post_data");
            String profilePictureURL = post.getOwner().getProfilePicturePath();
            String fullname = post.getOwner().getFullName();
            String postDescription = post.getDescription();
            long publishDate = post.getPublishDateMillis();

            textViewFullname.setText(fullname);
            textViewPublishDate.setText(MyUtilities.convertMillisToReadableTimespan(publishDate));
            textPostDescription.setText(postDescription);

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




}
