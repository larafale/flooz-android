package me.flooz.app.Utils;

import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import me.flooz.app.App.FloozApplication;

public class ImageGalleryManager {


    public static List<GalleryImage> getPhoneImages() {
        String[] projection = { MediaStore.Images.Media.DATA, MediaStore.Images.Media._ID};
        Cursor cursor = FloozApplication.getAppContext().getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection, null, null, null);

        List<GalleryImage> res = new ArrayList<>();

        if (cursor.moveToFirst()) {
            do {
                int dataColumn = cursor.getColumnIndex(MediaStore.Images.Media.DATA);
                int idColumn = cursor.getColumnIndex(MediaStore.Images.Media._ID);
                if (dataColumn >= 0 && idColumn >= 0) {
                    String data = cursor.getString(dataColumn);
                    if (data != null && !data.isEmpty()) {
                        File f = new File(data);
                        GalleryImage image = new GalleryImage();
                        image.imgURI = Uri.fromFile(f);
                        image.imgID = cursor.getString(idColumn);
                        image.thumbImg = MediaStore.Images.Thumbnails.getThumbnail(FloozApplication.getAppContext().getContentResolver(), Long.parseLong(image.imgID),
                                MediaStore.Images.Thumbnails.MINI_KIND, null);
                        if (image.thumbImg != null) {
                            int nh = (int) (image.thumbImg.getHeight() * (256.0 / image.thumbImg.getWidth()));
                            image.thumbImg = Bitmap.createScaledBitmap(image.thumbImg, 256, nh, true);
                            res.add(image);
                        }
                    }
                }
            } while (cursor.moveToNext());
        }
        cursor.close();

        return res;
    }
}
