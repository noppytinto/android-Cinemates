package mirror42.dev.cinemates.utilities;

import android.content.Context;
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
        Glide.with(context)  //2
                .load(imageUrl) //3
                .fallback(R.drawable.broken_image)
                .placeholder(R.drawable.placeholder_image)
                .circleCrop() //4
                .into(imageView); //8
    }

    public static void loadCircularImageInto(String imageUrl, MenuItem menuItem, Context context) {
        Glide.with(context)  //2
                .asDrawable()
                .load(imageUrl) //3
                .fallback(R.drawable.broken_image)
                .placeholder(R.drawable.placeholder_image)
                .circleCrop() //4
                .into(new CustomTarget<Drawable>() {
                    @Override
                    public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                        menuItem.setIcon(resource);
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {

                    }
                }); //8
    }



}
