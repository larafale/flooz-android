package flooz.android.com.flooz.UI.Fragment.Home.Camera;

import android.app.Fragment;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;

import flooz.android.com.flooz.Adapter.ImageGalleryAdapter;
import flooz.android.com.flooz.R;
import flooz.android.com.flooz.UI.Activity.HomeActivity;
import flooz.android.com.flooz.UI.Fragment.Home.HomeBaseFragment;
import flooz.android.com.flooz.Utils.CustomCameraHost;
import flooz.android.com.flooz.Utils.CustomFonts;

/**
 * Created by Flooz on 10/7/14.
 */
public class ImageGalleryFragment extends HomeBaseFragment {

    private ImageView headerCloseButton;
    private TextView headerTitle;
    private GridView gridView;

    private ImageGalleryAdapter gridAdapter;

    public CustomCameraHost.CustomCameraHostDelegate cameraHostDelegate;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.image_gallery_fragment, null);

        this.headerCloseButton = (ImageView) view.findViewById(R.id.photo_gallery_header_close);
        this.headerTitle = (TextView) view.findViewById(R.id.photo_gallery_header_title);
        this.gridView = (GridView) view.findViewById(R.id.photo_gallery_grid);

        this.headerTitle.setTypeface(CustomFonts.customTitleExtraLight(inflater.getContext()));

        this.gridAdapter = new ImageGalleryAdapter(inflater.getContext());

        this.gridView.setAdapter(this.gridAdapter);

        this.gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (cameraHostDelegate != null)
                    try {
                        cameraHostDelegate.photoTaken(MediaStore.Images.Media.getBitmap(inflater.getContext().getContentResolver(), gridAdapter.images.get(i).imgURI));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
            }
        });

        this.headerCloseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                parentActivity.popMainFragment(R.animator.slide_down, android.R.animator.fade_in);
            }
        });

        return view;
    }


    @Override
    public void onBackPressed() {
        this.headerCloseButton.performClick();
    }

}
