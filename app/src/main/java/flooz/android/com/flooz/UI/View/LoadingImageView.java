package flooz.android.com.flooz.UI.View;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.makeramen.RoundedImageView;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.core.listener.ImageLoadingProgressListener;

import flooz.android.com.flooz.R;

/**
 * Created by Flooz on 12/8/14.
 */
public class LoadingImageView extends RelativeLayout {

    private RoundedImageView imageView;
    private ProgressBar progressBar;
    private RelativeLayout progressBackground;

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

        this.imageView = (RoundedImageView) view.findViewById(R.id.loading_image_view_img);
        this.progressBar = (ProgressBar) view.findViewById(R.id.loading_image_view_progress);
        this.progressBar.setMax(100);
        this.progressBar.setProgress(0);
        this.progressBackground = (RelativeLayout) view.findViewById(R.id.loading_image_view_container);
    }

    public void setImageFromUrl(String imgUrl) {
        imageView.setVisibility(GONE);
        progressBackground.setVisibility(VISIBLE);
        progressBar.setProgress(0);

        ImageLoader.getInstance().displayImage(imgUrl, this.imageView, null, new ImageLoadingListener() {
            @Override
            public void onLoadingStarted(String imageUri, View view) {
                imageView.setVisibility(GONE);
                progressBackground.setVisibility(VISIBLE);
                progressBar.setProgress(0);
            }

            @Override
            public void onLoadingFailed(String imageUri, View view, FailReason failReason) {

            }

            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                imageView.setVisibility(VISIBLE);
                progressBackground.setVisibility(GONE);
            }

            @Override
            public void onLoadingCancelled(String imageUri, View view) {

            }
        }, new ImageLoadingProgressListener() {
            @Override
            public void onProgressUpdate(String imageUri, View view, int current, int total) {
                float tmp = current;
                tmp /= total;
                tmp *= 100;

                progressBar.setProgress((int)tmp);
            }
        });
    }
}
