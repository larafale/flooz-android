package flooz.android.com.flooz.Utils;

import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import flooz.android.com.flooz.App.FloozApplication;

public class ImageGalleryManager {

//    private static List<GalleryImage> insertThumbnails(List<GalleryImage> res) {
////        String[] projection = { MediaStore.Images.Thumbnails.DATA, MediaStore.Images.Thumbnails._ID, MediaStore.Images.Thumbnails.IMAGE_ID};
////        Cursor cursor = FloozApplication.getAppContext().getContentResolver().query(MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI,
////                projection, null, null, null);
////
////        if (cursor.moveToFirst()) {
////            int imgColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Thumbnails.IMAGE_ID);
////            int dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Thumbnails.DATA);
////            int idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Thumbnails._ID);
////            do {
////                String imgId = cursor.getString(imgColumn);
//
//        for (int i = 0; i < res.size(); i++) {
//            GalleryImage image = res.get(i);
//            image.thumbImg = MediaStore.Images.Thumbnails.getThumbnail(FloozApplication.getAppContext().getContentResolver(), Long.parseLong(image.imgID),
//                    MediaStore.Images.Thumbnails.MICRO_KIND, null);
//        }
////            } while (cursor.moveToNext());
////        }
////        cursor.close();
////
////        for (int i = 0; i < res.size(); i++) {
////            GalleryImage image = res.get(i);
////            if (image.thumbImg == null) {
////                res.remove(i);
////                --i;
////            }
////        }
////
//        return res;
//    }

    public static List<GalleryImage> getPhoneImages() {
        String[] projection = { MediaStore.Images.Media.DATA, MediaStore.Images.Media._ID};
        Cursor cursor = FloozApplication.getAppContext().getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection, null, null, null);

        List<GalleryImage> res = new ArrayList<>();

        if (cursor.moveToFirst()) {
            int dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            int idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID);
            do {
                GalleryImage image = new GalleryImage();

                String data = cursor.getString(dataColumn);
                File f = new File(data);
                image.imgURI = Uri.fromFile(f);
                image.imgID = cursor.getString(idColumn);
                image.thumbImg = MediaStore.Images.Thumbnails.getThumbnail(FloozApplication.getAppContext().getContentResolver(), Long.parseLong(image.imgID),
                        MediaStore.Images.Thumbnails.MINI_KIND, null);
                res.add(image);
            } while (cursor.moveToNext());
        }
        cursor.close();

        return res;
    }
}
