package flooz.android.com.flooz.Utils;

import android.graphics.Bitmap;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

import flooz.android.com.flooz.App.FloozApplication;

/**
 * Created by Flooz on 12/4/14.
 */
public class ImageHelper {

    public static File convertBitmapInFile(Bitmap image) {
        File filesDir = FloozApplication.getAppContext().getFilesDir();
        File uploadImage = new File(filesDir, "image.jpeg");

        OutputStream os;
        try {
            os = new FileOutputStream(uploadImage);
            image.compress(Bitmap.CompressFormat.JPEG, 100, os);
            os.flush();
            os.close();
        } catch (Exception e) {
            uploadImage = null;
        }

        return uploadImage;
    }
}
