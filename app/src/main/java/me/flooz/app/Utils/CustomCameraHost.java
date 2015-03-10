package me.flooz.app.Utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.commonsware.cwac.camera.PictureTransaction;
import com.commonsware.cwac.camera.SimpleCameraHost;


/**
 * Created by Flooz on 10/6/14.
 */
public class CustomCameraHost extends SimpleCameraHost {

    public CustomCameraHostDelegate delegate;
    public boolean useFrontCam = false;

    public interface CustomCameraHostDelegate {
        void photoTaken(Bitmap photo);
    }

    public CustomCameraHost(Context _ctxt) {
        super(_ctxt);
    }

    @Override
    public void saveImage(PictureTransaction pictureTransaction, byte[] pictureData) {
        Bitmap img = BitmapFactory.decodeByteArray(pictureData, 0, pictureData.length);
        if (this.delegate != null)
            this.delegate.photoTaken(img);
    }

    @Override
    public void saveImage(PictureTransaction pictureTransaction, Bitmap img) {
        if (this.delegate != null)
            this.delegate.photoTaken(img);
    }

    @Override
    public boolean useSingleShotMode() {
        return true;
    }

    @Override
    public boolean useFrontFacingCamera() {
        return this.useFrontCam;
    }

    @Override
    public boolean mirrorFFC() {
        return this.useFrontCam;
    }

    @Override
    public boolean useFullBleedPreview() {
        return true;
    }
}
