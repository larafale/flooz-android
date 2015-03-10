package me.flooz.app.UI.Fragment.Home.Camera;

import android.graphics.Bitmap;
import android.hardware.Camera;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.commonsware.cwac.camera.CameraFragment;
import com.commonsware.cwac.camera.CameraView;

import me.flooz.app.R;
import me.flooz.app.Utils.CustomCameraHost;

/**
 * Created by Flooz on 10/6/14.
 */
public class CameraOverlayFragment extends CameraFragment implements CustomCameraHost.CustomCameraHostDelegate {

    private ImageView closeButton;
    private ImageView expandButton;
    private ImageView switchButton;
    private ImageView albumButton;
    private ImageView shootButton;
    private CameraView cameraView;

    public Boolean showFront = false;
    public Boolean isExpandable = false;
    public Boolean isClosable = false;
    public Boolean canAccessAlbum = true;

    public interface CameraOverlayFragmentCallbacks {
        public void switchCamera(Boolean showFront);
        public void expandCamera();
        public void closeCamera();
        public void showGallery();
    }

    public CustomCameraHost.CustomCameraHostDelegate delegate;
    public CameraOverlayFragmentCallbacks callbacks;

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View content = inflater.inflate(R.layout.camera_overlay, container, false);

        this.cameraView = (CameraView) content.findViewById(R.id.camera_overlay_cam);

        CustomCameraHost tmp = new CustomCameraHost(inflater.getContext());

        tmp.useFrontCam = this.showFront;

        this.cameraView.setHost(tmp);
        this.cameraView.setFlashMode(Camera.Parameters.FLASH_MODE_AUTO);

        setCameraView(cameraView);

        this.closeButton = (ImageView) content.findViewById(R.id.camera_overlay_close);
        this.expandButton = (ImageView) content.findViewById(R.id.camera_overlay_expand);
        this.switchButton = (ImageView) content.findViewById(R.id.camera_overlay_switch);
        this.albumButton = (ImageView) content.findViewById(R.id.camera_overlay_album);
        this.shootButton = (ImageView) content.findViewById(R.id.camera_overlay_shoot);

        final CameraOverlayFragment self = this;

        content.findViewById(R.id.camera_overlay_touch).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (cameraView.isAutoFocusAvailable())
                    cameraView.autoFocus();
            }
        });

        this.shootButton.setOnClickListener(new View.OnClickListener() {
                @Override
            public void onClick(View view) {
                    if (cameraView.isShown()) {
                        ((CustomCameraHost) cameraView.getHost()).delegate = self;
                        takePicture(true, false);
                    }
            }
        });

        this.switchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (callbacks != null)
                    callbacks.switchCamera(!showFront);
            }
        });

        this.expandButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (callbacks != null)
                    callbacks.expandCamera();
            }
        });

        this.closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (callbacks != null)
                    callbacks.closeCamera();
            }
        });

        this.albumButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (callbacks != null)
                    callbacks.showGallery();
            }
        });


        if (Camera.getNumberOfCameras() < 2)
            this.switchButton.setVisibility(View.GONE);

        if (!this.isClosable)
            this.closeButton.setVisibility(View.GONE);

        if (!this.isExpandable)
            this.expandButton.setVisibility(View.GONE);

        if (!this.canAccessAlbum)
            this.albumButton.setVisibility(View.GONE);

        return (content);
    }

    @Override
    public void photoTaken(Bitmap photo) {
        if (this.delegate != null)
            this.delegate.photoTaken(photo);
    }
}
