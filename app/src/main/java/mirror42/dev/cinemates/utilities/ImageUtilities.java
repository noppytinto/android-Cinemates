package mirror42.dev.cinemates.utilities;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.view.MenuItem;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;

import mirror42.dev.cinemates.R;

public class ImageUtilities {

    public static void loadCircularImageInto(String imageUrl, ImageView imageView, Context context) {
        try {
            Glide.with(context)  //2
                    .load(imageUrl) //3
                    .fallback(R.drawable.broken_image)
                    .placeholder(R.drawable.placeholder_image)
                    .circleCrop() //4
                    .into(imageView); //8
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void loadRectangularImageInto(String imageUrl, ImageView imageView, Context context) {
        try {
            Glide.with(context)  //2
                    .load(imageUrl) //3
                    .fallback(R.drawable.broken_image)
                    .placeholder(R.drawable.placeholder_image)
                    .centerCrop() //4
                    .into(imageView); //8
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void loadCircularImageInto(String imageUrl, MenuItem menuItem, Context context) {
        try {
            Glide.with(context)  //2
                    .asDrawable()
                    .load(imageUrl) //3
                    .fallback(R.drawable.broken_image)
                    .placeholder(R.drawable.placeholder_image)
                    .circleCrop() //4
                    .into(new CustomTarget<Drawable>() {
                        @Override
                        public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                            try {
                                Drawable drawable = resource;
                                menuItem.setIcon(drawable);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onLoadCleared(@Nullable Drawable placeholder) {

                        }
                    }); //8
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void loadDefaultProfilePictureInto(MenuItem menuItem, Context context) {
        Drawable drawable = null;

        try {
            drawable = context.getResources().getDrawable(R.drawable.user_icon_light_blue);
            menuItem.setIcon(drawable);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    public static Drawable getDefaultProfilePictureIcon(Context context) {
        Drawable defaultProfilePicture = null;

        try {
            defaultProfilePicture = context.getResources().getDrawable(R.drawable.user_icon_light_blue);
        } catch (Resources.NotFoundException e) {
            e.printStackTrace();
        }

        return defaultProfilePicture;
    }



}// end ImageUtilities class
