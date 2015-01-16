package flooz.android.com.flooz.UI.Fragment.Home.Camera;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.hardware.Camera;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import flooz.android.com.flooz.R;
import flooz.android.com.flooz.UI.Activity.HomeActivity;
import flooz.android.com.flooz.Utils.CustomCameraHost;

/**
 * Created by Flooz on 10/7/14.
 */
public class CameraFullscreenFragment extends Fragment implements CameraOverlayFragment.CameraOverlayFragmentCallbacks {

    public Boolean currentCameraIsFront = false;

    public HomeActivity parentActivity;

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
        this.camFragmentBack.delegate = this.cameraHostDelegate;
        this.camFragmentBack.callbacks = this;
        this.camFragmentBack.isClosable = true;

        this.camFragmentFront = new CameraOverlayFragment();
        this.camFragmentFront.delegate = this.cameraHostDelegate;
        this.camFragmentFront.callbacks = this;
        this.camFragmentFront.showFront = true;
        this.camFragmentFront.isClosable = true;

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
    public void expandCamera() { }

    @Override
    public void closeCamera() {
        this.parentActivity.popMainFragment(R.animator.slide_down, android.R.animator.fade_in);
    }

    @Override
    public void showGallery() {
        ((ImageGalleryFragment)this.parentActivity.contentFragments.get("photo_gallery")).cameraHostDelegate = this.cameraHostDelegate;
        parentActivity.popMainFragment();
        parentActivity.pushMainFragment("photo_gallery", R.animator.slide_up, android.R.animator.fade_out);
    }
}
