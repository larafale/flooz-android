package me.flooz.app.Utils;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

import me.flooz.app.App.FloozApplication;

/**
 * Created by Flooz on 12/4/14.
 */
public class ImageHelper {

    public static String getPath(Context context, Uri uri) {
        if (uri == null) {
            return null;
        }

        String res = null;
        String[] proj = { MediaStore.Images.Media.DATA };
        Cursor cursor = context.getContentResolver().query(uri, proj, null, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                res = cursor.getString(column_index);
            }
            cursor.close();
            return res;
        }
        return uri.getPath();
    }

    public static int getRotation(Context context,Uri selectedImage) {
        int rotation = 0;
        String photoPath = ImageHelper.getPath(context, selectedImage);

        if (photoPath != null) {
            ExifInterface ei;
            try {
                ei = new ExifInterface(photoPath);
                int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

                switch (orientation) {
                    case ExifInterface.ORIENTATION_ROTATE_90:
                        rotation = 90;
                        break;
                    case ExifInterface.ORIENTATION_ROTATE_180:
                        rotation = 180;
                        break;
                    case ExifInterface.ORIENTATION_ROTATE_270:
                        rotation = 270;
                        break;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return rotation;
    }

    public static Bitmap rotateBitmap(Bitmap source, float angle)
    {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }

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

    public static void saveImageOnPhone(final Context context, final Bitmap finalBitmap) {
        class SaveImage extends AsyncTask<String, Void, String> {
            @Override
            protected String doInBackground(String... params) {
                ByteArrayOutputStream bytes = new ByteArrayOutputStream();

                finalBitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);

                File file = ImageHelper.getOutputMediaFile(MEDIA_TYPE_IMAGE);

                try {
                    boolean newFile = file.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                FileOutputStream fos;

                try {
                    fos = new FileOutputStream(file);
                    fos.write(bytes.toByteArray());
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                ContentValues values = new ContentValues(4);
                long current = System.currentTimeMillis();
                values.put(MediaStore.Images.Media.TITLE, file.getName());
                values.put(MediaStore.Images.Media.DATE_ADDED, (int) (current / 1000));
                values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpg");
                values.put(MediaStore.Images.Media.DATA, file.getAbsolutePath());
                ContentResolver contentResolver = context.getContentResolver();

                Uri base = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                contentResolver.insert(base, values);

                return "";
            }

            @Override
            protected void onPostExecute(String result) {

            }

            @Override
            protected void onPreExecute() {}

            @Override
            protected void onProgressUpdate(Void... values) {}
        }

        new SaveImage().execute(null, null, null);
    }

    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;

    /** Create a file Uri for saving an image or video */
    public static Uri getOutputMediaFileUri(int type){
        return Uri.fromFile(getOutputMediaFile(type));
    }

    /** Create a File for saving an image or video */
    public static File getOutputMediaFile(int type){
        File mediaStorageDir;
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_PICTURES), "FloozPictures");
        } else {
            mediaStorageDir = new File(FloozApplication.getAppContext().getFilesDir(), "FloozPictures");
        }

        if (!mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                return null;
            }
        }

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ENGLISH).format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE){
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "IMG_"+ timeStamp + ".jpg");
        } else if(type == MEDIA_TYPE_VIDEO) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "VID_"+ timeStamp + ".mp4");
        } else {
            return null;
        }

        return mediaFile;
    }
}
