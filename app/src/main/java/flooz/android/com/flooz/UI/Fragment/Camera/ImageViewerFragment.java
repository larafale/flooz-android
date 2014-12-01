package flooz.android.com.flooz.UI.Fragment.Camera;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.ImageLoader;

import flooz.android.com.flooz.R;
import flooz.android.com.flooz.UI.Activity.HomeActivity;

/**
 * Created by Flooz on 9/30/14.
 */
public class ImageViewerFragment extends Fragment {

    public HomeActivity parentActivity;

    private Boolean viewCreated = false;

    private ImageView closeButton;
    private ImageView image;
    private String imageURL = null;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.image_viewer_fragment, null);

        this.closeButton = (ImageView) view.findViewById(R.id.image_viewer_close_button);
        this.image = (ImageView) view.findViewById(R.id.image_viewer_img);

        this.viewCreated = true;

        this.closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (parentActivity != null)
                    parentActivity.popMainFragment(R.animator.slide_down, android.R.animator.fade_in);
            }
        });


        if (this.imageURL != null && !this.imageURL.isEmpty())
            ImageLoader.getInstance().displayImage(this.imageURL, this.image);

        return view;
    }

    @Override
    public void onResume() {
    super.onResume();

    }

    public void setImage(String imgURL) {
        this.imageURL = imgURL;
        if (this.viewCreated)
            ImageLoader.getInstance().displayImage(this.imageURL, this.image);
    }

}
