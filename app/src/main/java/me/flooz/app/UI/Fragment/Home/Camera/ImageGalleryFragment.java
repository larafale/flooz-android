package me.flooz.app.UI.Fragment.Home.Camera;

import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.IOException;
import java.net.URL;

import me.flooz.app.Adapter.ImageGalleryAdapter;
import me.flooz.app.R;
import me.flooz.app.UI.Fragment.Home.HomeBaseFragment;
import me.flooz.app.Utils.CustomCameraHost;
import me.flooz.app.Utils.CustomFonts;

/**
 * Created by Flooz on 10/7/14.
 */
public class ImageGalleryFragment extends HomeBaseFragment {

    private ImageView headerCloseButton;
    private TextView headerTitle;
    private GridView gridView;
    private ProgressBar progressBar;

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
        this.progressBar = (ProgressBar) view.findViewById(R.id.photo_gallery_progress);

        this.headerTitle.setTypeface(CustomFonts.customTitleExtraLight(inflater.getContext()));

        class DownloadFilesTask extends AsyncTask<URL, Integer, Long> {
            protected void onPreExecute() {
                progressBar.setVisibility(View.VISIBLE);
            }

            protected Long doInBackground(URL... urls) {
                gridAdapter = new ImageGalleryAdapter(inflater.getContext());
                return null;
            }

            protected void onPostExecute(Long result) {
                gridView.setAdapter(gridAdapter);
                progressBar.setVisibility(View.GONE);            }
        }

        new DownloadFilesTask().execute();

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
