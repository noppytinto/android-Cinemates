package mirror42.dev.cinemates.ui.userprofile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;

import mirror42.dev.cinemates.R;
import mirror42.dev.cinemates.model.User;

public class UserProfileFragment extends Fragment {
    private UserProfileViewModel mViewModel;
    private View view;
    private ImageView imageViewProfilePicture;
    private TextView textViewfullName;
    private TextView textViewusername;
    private Button buttonFollow;
    private Button buttonAcceptFollow;

    public static UserProfileFragment newInstance() {
        return new UserProfileFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_user_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.view = view;
        imageViewProfilePicture = view.findViewById(R.id.imageView_userProfileFragment_profilePicture);
        textViewfullName = view.findViewById(R.id.textView_userProfileFragment_fullName);
        textViewusername = view.findViewById(R.id.textView_userProfileFragment_username);
        buttonFollow = view.findViewById(R.id.button_userProfileFragment_follow);

        if(getArguments() != null) {
            UserProfileFragmentArgs args = UserProfileFragmentArgs.fromBundle(getArguments());
            User user = args.getUserArgument();

            if(user!=null) {
                String profilePictureUrl = user.getProfilePicturePath();
                Glide.with(getContext())  //2
                        .load(profilePictureUrl) //3
                        .fallback(R.drawable.broken_image)
                        .placeholder(R.drawable.placeholder_image)
                        .circleCrop() //4
                        .into(imageViewProfilePicture); //8

                textViewfullName.setText(user.getFullName());
                textViewusername.setText("@" + user.getUsername());
            }
        }


    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(UserProfileViewModel.class);
        // TODO: Use the ViewModel








    }

}// end UserProfileFragment class