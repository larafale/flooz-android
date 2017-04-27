package me.flooz.app.UI.View;

import android.content.Context;
import android.graphics.drawable.Animatable;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

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

import me.flooz.app.R;

/**
 * Created by Flooz on 12/8/14.
 */
public class LoadingImageView extends RelativeLayout {

    private DotProgressBar dotProgressBar;
    private SimpleDraweeView imageView;
    private ImageView playButton;
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
        this.playButton = (ImageView) view.findViewById(R.id.loading_image_view_play_button);

        GenericDraweeHierarchyBuilder builder =
                new GenericDraweeHierarchyBuilder(getResources());

        RoundingParams roundingParams = RoundingParams.fromCornersRadius(5f);
        roundingParams.setOverlayColor(context.getResources().getColor(R.color.background_header));

        ProgressBarDrawable progressBar = new ProgressBarDrawable();
        progressBar.setHideWhenZero(true);
        progressBar.setColor(context.getResources().getColor(R.color.blue));

        GenericDraweeHierarchy hierarchy = builder
                .setRoundingParams(roundingParams)
                .setFailureImage(context.getResources().getDrawable(R.drawable.fake), ScalingUtils.ScaleType.CENTER_CROP)
                .build();

        this.imageView.setHierarchy(hierarchy);
    }

    public void setImageFromUrl(String imgUrl) {
        this.setImageFromUrl(imgUrl, false);
    }

    public void setImageFromUrl(String imgUrl, final boolean isVideoThumb) {
        if (imgUrl.contentEquals("/img/fake.png")) {
            imageView.setVisibility(VISIBLE);
            imageView.setImageDrawable(this.context.getResources().getDrawable(R.drawable.fake));
        } else {
            imageView.setVisibility(VISIBLE);
            imageView.setImageDrawable(null);

            dotProgressBar.setVisibility(VISIBLE);
            playButton.setVisibility(GONE);

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
                    if (isVideoThumb) {
                        playButton.setVisibility(VISIBLE);
                    }
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
