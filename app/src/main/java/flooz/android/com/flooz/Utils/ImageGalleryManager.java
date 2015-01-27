package flooz.android.com.flooz.Utils;

import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import flooz.android.com.flooz.App.FloozApplication;

public class ImageGalleryManager {

    private static List<GalleryImage> insertThumbnails(List<GalleryImage> res) {
        String[] projection = { MediaStore.Images.Thumbnails.DATA, MediaStore.Images.Thumbnails._ID, MediaStore.Images.Thumbnails.IMAGE_ID};
        Cursor cursor = FloozApplication.getAppContext().getContentResolver().query(MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI,
                projection, null, null, null);

        if (cursor.moveToFirst()) {
            int imgColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Thumbnails.IMAGE_ID);
            int dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Thumbnails.DATA);
            int idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Thumbnails._ID);
            do {
                String imgId = cursor.getString(imgColumn);

                for (int i = 0; i < res.size(); i++) {
                    GalleryImage image = res.get(i);

                    if (image.imgID.equals(imgId)) {
                        String data = cursor.getString(dataColumn);
                        File f = new File(data);
                        image.thumbURI = Uri.fromFile(f);
                        image.thumbID = cursor.getString(idColumn);
                        image.thumbImg = BitmapFactory.decodeFile(f.getPath());
                    }
                }
            } while (cursor.moveToNext());
        }
        cursor.close();

        for (int i = 0; i < res.size(); i++) {
            GalleryImage image = res.get(i);
            if (image.thumbImg == null) {
                res.remove(i);
                --i;
            }
        }
        
        return res;
    }

    public static List<GalleryImage> getPhoneImages() {
        String[] projection = { MediaStore.Images.Media.DATA, MediaStore.Images.Media._ID};
        Cursor cursor = FloozApplication.getAppContext().getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection, null, null, null);

        List<GalleryImage> res = new ArrayList<GalleryImage>();

        if (cursor.moveToFirst()) {
            int dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            int idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID);
            do {
                GalleryImage image = new GalleryImage();

                String data = cursor.getString(dataColumn);
                File f = new File(data);
                image.imgURI = Uri.fromFile(f);
                image.imgID = cursor.getString(idColumn);
                res.add(image);
            } while (cursor.moveToNext());
        }
        cursor.close();

        res = insertThumbnails(res);

        return res;
    }
}
