package me.flooz.app.UI.Tools;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Animatable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
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
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.controller.BaseControllerListener;
import com.facebook.drawee.controller.ControllerListener;
import com.facebook.drawee.drawable.ProgressBarDrawable;
import com.facebook.drawee.drawable.ScalingUtils;
import com.facebook.drawee.generic.GenericDraweeHierarchy;
import com.facebook.drawee.generic.GenericDraweeHierarchyBuilder;
import com.facebook.drawee.generic.RoundingParams;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.image.ImageInfo;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.github.silvestrpredko.dotprogressbar.DotProgressBar;
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
    private DotProgressBar dotProgressBar;
    private SimpleDraweeView imageView;

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
        this.imageView = (SimpleDraweeView) this.findViewById(R.id.main_image_image);
        this.dotProgressBar = (DotProgressBar) this.findViewById(R.id.main_image_progress);

        GenericDraweeHierarchyBuilder builder =
                new GenericDraweeHierarchyBuilder(getResources());

        RoundingParams roundingParams = RoundingParams.fromCornersRadius(5f);
        roundingParams.setOverlayColor(this.getResources().getColor(R.color.background_header));

        ProgressBarDrawable progressBar = new ProgressBarDrawable();
        progressBar.setHideWhenZero(true);
        progressBar.setColor(this.getResources().getColor(R.color.blue));

        GenericDraweeHierarchy hierarchy = builder
//                .setProgressBarImage(progressBar)
                .setActualImageScaleType(ScalingUtils.ScaleType.FIT_CENTER)
                .setRoundingParams(roundingParams)
                .build();

        this.imageView.setHierarchy(hierarchy);

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
        Animation anim = AnimationUtils.loadAnimation(this, android.R.anim.fade_in);
        anim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                imageViewer.setVisibility(View.VISIBLE);
                imageView.setVisibility(View.GONE);
                dotProgressBar.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                imageView.setImageBitmap(image);
                imageView.setVisibility(View.VISIBLE);
                dotProgressBar.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        this.imageViewer.startAnimation(anim);
    }

    private void showImageViewer(final String url) {
        if (!url.contentEquals("/img/fake")) {
            Animation anim = AnimationUtils.loadAnimation(this, android.R.anim.fade_in);
            anim.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    imageViewer.setVisibility(View.VISIBLE);
                    imageView.setVisibility(View.GONE);
                    dotProgressBar.setVisibility(View.GONE);
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    if (url.contentEquals("/img/fake.png")) {
                        imageView.setVisibility(View.VISIBLE);
                        imageView.setImageDrawable(CustomImageViewer.this.getResources().getDrawable(R.drawable.fake));
                    } else {
                        imageView.setVisibility(View.VISIBLE);
                        imageView.setImageDrawable(null);

                        dotProgressBar.setVisibility(View.VISIBLE);

                        ImageRequest request = ImageRequestBuilder.newBuilderWithSource(Uri.parse(url))
                                .setProgressiveRenderingEnabled(false)
                                .build();

                        ControllerListener controllerListener = new BaseControllerListener<ImageInfo>() {
                            @Override
                            public void onFinalImageSet(String id, @Nullable ImageInfo imageInfo, @Nullable Animatable anim) {
                                if (imageInfo == null) {
                                    return;
                                }
                                dotProgressBar.setVisibility(View.GONE);
                            }

                            @Override
                            public void onFailure(String id, Throwable throwable) {
                                dotProgressBar.setVisibility(View.GONE);
                            }
                        };

                        DraweeController controller = Fresco.newDraweeControllerBuilder()
                                .setControllerListener(controllerListener)
                                .setImageRequest(request)
                                .setTapToRetryEnabled(true)
                                .setAutoPlayAnimations(true)
                                .setOldController(imageView.getController())
                                .build();

                        imageView.setController(controller);
                    }
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            this.imageViewer.startAnimation(anim);
        }
    }
}
