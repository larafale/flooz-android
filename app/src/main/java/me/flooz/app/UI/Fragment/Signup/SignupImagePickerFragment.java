package me.flooz.app.UI.Fragment.Signup;

import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ProgressBar;

import java.io.IOException;
import java.net.URL;

import me.flooz.app.Adapter.ImageGalleryAdapter;
import me.flooz.app.R;
import me.flooz.app.Utils.CustomCameraHost;

/**
 * Created by Flooz on 11/17/14.
 */
public class SignupImagePickerFragment extends SignupBaseFragment {

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
        View view = inflater.inflate(R.layout.signup_image_picker_fragment, null);

        this.gridView = (GridView) view.findViewById(R.id.signup_image_picker_grid);
        this.progressBar = (ProgressBar) view.findViewById(R.id.signup_image_picker_progress);

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
                        parentActivity.backToPreviousPage();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
            }
        });

        return view;
    }
}
