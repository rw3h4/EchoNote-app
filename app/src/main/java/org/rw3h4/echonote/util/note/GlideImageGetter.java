package org.rw3h4.echonote.util.note;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.Html;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;

// This class uses Glide to asynchronously load images for an Html.ImageGetter
public class GlideImageGetter implements Html.ImageGetter {
    private final Context context;
    private final TextView textView;

    public GlideImageGetter(Context context, TextView textView) {
        this.context = context;
        this.textView =  textView;
    }

    @Override
    public Drawable getDrawable(String source) {
        BitmapDrawablePlaceholder drawable = new BitmapDrawablePlaceholder();

        // Load the image using Glide
        Glide.with(context).asBitmap().load(source).into(new CustomTarget<Bitmap>() {
            @Override
            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                // Image loaded successfully
                BitmapDrawable bitmapDrawable = new BitmapDrawable(context.getResources(), resource);

                // Scale the image to fit the width of the textview
                int screenWidth = context.getResources().getDisplayMetrics().widthPixels;
                float scale = (float) screenWidth / (float) resource.getWidth();
                int newHeight = (int) (resource.getHeight() * scale);

                // Set the bounds of the drawable to the scaled size
                bitmapDrawable.setBounds(0, 0, screenWidth, newHeight);

                // Invalidate the TextView to make it redraw with the new image
                textView.setText(textView.getText());
            }

            @Override
            public void onLoadCleared(@Nullable Drawable placeholder) {}
        });

        return drawable;
    }

    // A simpple drawable wrapper to be updated synchronously
    private static class BitmapDrawablePlaceholder extends BitmapDrawable {
        protected Drawable drawable;

        @Override
        public void draw(android.graphics.Canvas canvas) {
            if (drawable != null) {
                drawable.draw(canvas);
            }
        }

        public void setDrawable(Drawable drawable) {
            this.drawable = drawable;
        }
    }
}
