package flooz.android.com.flooz.UI.Fragment.Signup;

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
import flooz.android.com.flooz.Utils.CustomCameraHost;
import flooz.android.com.flooz.Utils.CustomFonts;

/**
 * Created by Flooz on 11/17/14.
 */
public class SignupImagePickerFragment extends SignupBaseFragment {

    private GridView gridView;

    private ImageGalleryAdapter gridAdapter;

    public CustomCameraHost.CustomCameraHostDelegate cameraHostDelegate;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.singup_image_picker_fragment, null);

        this.gridView = (GridView) view.findViewById(R.id.signup_image_picker_grid);

        this.gridAdapter = new ImageGalleryAdapter(inflater.getContext());

        this.gridView.setAdapter(this.gridAdapter);

        this.gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (cameraHostDelegate != null)
                    try {
                        cameraHostDelegate.photoTaken(MediaStore.Images.Media.getBitmap(inflater.getContext().getContentResolver(), gridAdapter.images.get(i).imgURI));
                        parentActivity.backToPreviousPage();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
            }
        });

        return view;
    }
}
