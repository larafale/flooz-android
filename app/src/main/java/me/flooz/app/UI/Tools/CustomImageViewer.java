package me.flooz.app.UI.Tools;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.core.listener.ImageLoadingProgressListener;

import me.flooz.app.R;
import uk.co.senab.photoview.PhotoViewAttacher;

/**
 * Created by Wapazz on 21/09/15.
 */
public class CustomImageViewer extends Activity {

    private RelativeLayout imageViewer;
    private ImageView imageViewerClose;
    private ProgressBar imageViewerProgress;
    private ImageView imageViewerImage;
    private PhotoViewAttacher imageViewerAttacher;

    private static final String IMAGE_URL = "customViewerUrlImage";
    private static final String IMAGE_BITMAP = "customViewerBitmap";

    public static void start(Context context, String urlImage) {
        Intent intent = new Intent(context, CustomImageViewer.class);
        intent.putExtra(IMAGE_URL, urlImage);
        context.startActivity(intent);
    }

    public static void start(Context context, Bitmap image) {
        Intent intent = new Intent(context, CustomImageViewer.class);
        intent.putExtra(IMAGE_BITMAP, image);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.custom_imageviewer);

        this.imageViewer = (RelativeLayout) this.findViewById(R.id.main_image_container);
        this.imageViewerClose = (ImageView) this.findViewById(R.id.main_image_close);
        this.imageViewerProgress = (ProgressBar) this.findViewById(R.id.main_image_progress);
        this.imageViewerImage = (ImageView) this.findViewById(R.id.main_image_image);

        this.imageViewerAttacher = new PhotoViewAttacher(this.imageViewerImage);
        this.imageViewer.setClickable(true);

        String urlImage = this.getIntent().getStringExtra(IMAGE_URL);
        if (urlImage != null) {
            showImageViewer(urlImage);
        }
        else {
            Bitmap bitmap = this.getIntent().getParcelableExtra(IMAGE_BITMAP);
            showImageViewer(bitmap);
        }

        this.imageViewerClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void showImageViewer(final Bitmap image) {
        Animation anim = AnimationUtils.loadAnimation(getApplicationContext(), android.R.anim.fade_in);
        anim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                imageViewer.setVisibility(View.VISIBLE);
                imageViewerProgress.setMax(100);
                imageViewerProgress.setProgress(0);
                imageViewerImage.setVisibility(View.GONE);
                imageViewerProgress.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                imageViewerImage.setImageBitmap(image);
                imageViewerImage.setVisibility(View.VISIBLE);
                imageViewerProgress.setVisibility(View.GONE);
                imageViewerAttacher.update();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        this.imageViewer.startAnimation(anim);
    }

    private void showImageViewer(final String url) {
        if (!url.contentEquals("/img/fake")) {
            Animation anim = AnimationUtils.loadAnimation(getApplicationContext(), android.R.anim.fade_in);
            anim.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    imageViewer.setVisibility(View.VISIBLE);
                    imageViewerProgress.setMax(100);
                    imageViewerProgress.setProgress(0);
                    imageViewerImage.setVisibility(View.GONE);
                    imageViewerProgress.setVisibility(View.GONE);
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    imageViewerProgress.setVisibility(View.VISIBLE);
                    imageViewerImage.setVisibility(View.VISIBLE);

                    Glide.with(getApplicationContext()).load(url).diskCacheStrategy(DiskCacheStrategy.ALL).listener(new RequestListener<String, GlideDrawable>() {
                        @Override
                        public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                            imageViewerProgress.setVisibility(View.GONE);

                            return false;
                        }
                    }).into(imageViewerImage);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            this.imageViewer.startAnimation(anim);
        }
    }
}
