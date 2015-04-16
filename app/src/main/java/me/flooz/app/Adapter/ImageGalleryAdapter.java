package me.flooz.app.Adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.provider.MediaStore;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import java.util.List;

import me.flooz.app.Utils.GalleryImage;
import me.flooz.app.Utils.ImageGalleryManager;

public class ImageGalleryAdapter extends BaseAdapter {

    private Context context;

    public List<GalleryImage> images;

    public ImageGalleryAdapter(Context c) {
        this.context = c;
        images = ImageGalleryManager.getPhoneImages();
    }

    public int getCount() {
        return this.images.size();
    }

    public GalleryImage getItem(int position) {
        return this.images.get(position);
    }

    public long getItemId(int position) {
        return 0;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView;

        if (convertView == null) {
            imageView = new ImageView(this.context);

            AbsListView.LayoutParams layoutParams = new AbsListView.LayoutParams(250, 250);

            imageView.setLayoutParams(layoutParams);
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        } else {
            imageView = (ImageView) convertView;
        }
        GalleryImage image = this.getItem(position);

        Bitmap bitmapImage = MediaStore.Images.Thumbnails.getThumbnail(this.context.getContentResolver(), Long.parseLong(image.imgID),
                MediaStore.Images.Thumbnails.MINI_KIND, null);
        int nh = (int) (bitmapImage.getHeight() * (256.0 / bitmapImage.getWidth()));
        bitmapImage = Bitmap.createScaledBitmap(bitmapImage, 256, nh, true);
        imageView.setImageBitmap(bitmapImage);

        return imageView;
    }
}
