package me.flooz.app.UI.View;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Animatable;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.facebook.common.logging.FLog;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.controller.BaseControllerListener;
import com.facebook.drawee.controller.ControllerListener;
import com.facebook.drawee.drawable.ProgressBarDrawable;
import com.facebook.drawee.generic.GenericDraweeHierarchy;
import com.facebook.drawee.generic.GenericDraweeHierarchyBuilder;
import com.facebook.drawee.generic.RoundingParams;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.image.ImageInfo;
import com.facebook.imagepipeline.image.QualityInfo;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.github.silvestrpredko.dotprogressbar.DotProgressBar;
import com.makeramen.roundedimageview.RoundedImageView;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.core.listener.ImageLoadingProgressListener;

import me.flooz.app.R;
import me.flooz.app.Utils.CircleTransform;

/**
 * Created by Flooz on 12/8/14.
 */
public class LoadingImageView extends RelativeLayout {

    private DotProgressBar dotProgressBar;
    private SimpleDraweeView imageView;
    private Context context;

    public LoadingImageView(Context context)
    {
        super(context);
        init(context);
    }

    public LoadingImageView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        init(context);
    }

    public LoadingImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {
        View view = View.inflate(context, R.layout.loading_image_view, this);

        this.context = context;
        this.imageView = (SimpleDraweeView) view.findViewById(R.id.loading_image_view_img);
        this.dotProgressBar = (DotProgressBar) view.findViewById(R.id.loading_image_view_progress);

        GenericDraweeHierarchyBuilder builder =
                new GenericDraweeHierarchyBuilder(getResources());

        RoundingParams roundingParams = RoundingParams.fromCornersRadius(5f);
        roundingParams.setOverlayColor(context.getResources().getColor(R.color.background_header));

        ProgressBarDrawable progressBar = new ProgressBarDrawable();
        progressBar.setHideWhenZero(true);
        progressBar.setColor(context.getResources().getColor(R.color.blue));

        GenericDraweeHierarchy hierarchy = builder
                .setRoundingParams(roundingParams)
                .build();

        this.imageView.setHierarchy(hierarchy);
    }

    public void setImageFromUrl(String imgUrl) {
        if (imgUrl.contentEquals("/img/fake.png")) {
            imageView.setVisibility(VISIBLE);
            imageView.setImageDrawable(this.context.getResources().getDrawable(R.drawable.fake));
        } else {
            imageView.setVisibility(VISIBLE);
            imageView.setImageDrawable(null);

            dotProgressBar.setVisibility(VISIBLE);

            ImageRequest request = ImageRequestBuilder.newBuilderWithSource(Uri.parse(imgUrl))
                    .setProgressiveRenderingEnabled(false)
                    .build();

            ControllerListener<ImageInfo> controllerListener = new BaseControllerListener<ImageInfo>() {
                @Override
                public void onFinalImageSet(String id, @Nullable ImageInfo imageInfo, @Nullable Animatable anim) {
                    if (imageInfo == null) {
                        return;
                    }
                    dotProgressBar.setVisibility(GONE);
                }

                @Override
                public void onFailure(String id, Throwable throwable) {
                    dotProgressBar.setVisibility(GONE);
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
}
