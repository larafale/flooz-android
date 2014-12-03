package flooz.android.com.flooz.UI.Fragment.Signup;

import android.app.FragmentTransaction;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import flooz.android.com.flooz.R;
import flooz.android.com.flooz.UI.Fragment.Camera.CameraOverlayFragment;
import flooz.android.com.flooz.Utils.CustomCameraHost;

/**
 * Created by Flooz on 11/17/14.
 */
public class SignupImageCaptureFragment extends SignupBaseFragment implements CameraOverlayFragment.CameraOverlayFragmentCallbacks, CustomCameraHost.CustomCameraHostDelegate {

    public Boolean currentCameraIsFront = false;
    private CameraOverlayFragment camFragmentBack;
    private CameraOverlayFragment camFragmentFront;

    public CustomCameraHost.CustomCameraHostDelegate cameraHostDelegate;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.camera_fullscreen_fragment, null);

        this.camFragmentBack = new CameraOverlayFragment();
        this.camFragmentBack.delegate = this;
        this.camFragmentBack.callbacks = this;
        this.camFragmentBack.isClosable = true;
        this.camFragmentBack.canAccessAlbum = false;

        this.camFragmentFront = new CameraOverlayFragment();
        this.camFragmentFront.delegate = this;
        this.camFragmentFront.callbacks = this;
        this.camFragmentFront.showFront = true;
        this.camFragmentFront.isClosable = true;
        this.camFragmentFront.canAccessAlbum = false;

        FragmentTransaction ft = this.getChildFragmentManager().beginTransaction();

        if (Camera.getNumberOfCameras() < 2) {
            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(0, info);
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                this.currentCameraIsFront = true;
                ft.replace(R.id.camera_fullscreen_container, this.camFragmentFront);
            }
            else
                ft.replace(R.id.camera_fullscreen_container, this.camFragmentBack);
        } else if (this.currentCameraIsFront)
            ft.replace(R.id.camera_fullscreen_container, this.camFragmentFront);
        else
            ft.replace(R.id.camera_fullscreen_container, this.camFragmentBack);

        ft.addToBackStack(null).commit();

        return view;
    }

    @Override
    public void switchCamera(Boolean showFront) {
        CameraOverlayFragment in;

        if (showFront) {
            this.currentCameraIsFront = true;
            in = this.camFragmentFront;
        }
        else {
            this.currentCameraIsFront = false;
            in = this.camFragmentBack;
        }

        FragmentTransaction ft = this.getChildFragmentManager().beginTransaction();

        ft.setCustomAnimations(
                R.animator.card_flip_right_in, R.animator.card_flip_right_out,
                R.animator.card_flip_left_in, R.animator.card_flip_left_out);

        ft.replace(R.id.camera_fullscreen_container, in);
        ft.addToBackStack(null).commit();

    }

    @Override
    public void expandCamera() {

    }

    @Override
    public void closeCamera() {
        parentActivity.backToPreviousPage();
        parentActivity.showHeader();
    }

    @Override
    public void showGallery() {

    }

    @Override
    public void photoTaken(final Bitmap photo) {
        Handler mainHandler = new Handler(parentActivity.floozApp.getMainLooper());

        Runnable myRunnable = new Runnable() {
            @Override
            public void run() {
                cameraHostDelegate.photoTaken(photo);
                parentActivity.backToPreviousPage();
                parentActivity.showHeader();
            }
        };
        mainHandler.post(myRunnable);
    }
}
