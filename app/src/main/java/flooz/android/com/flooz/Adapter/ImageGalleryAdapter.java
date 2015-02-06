package flooz.android.com.flooz.Adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.util.List;

import flooz.android.com.flooz.R;
import flooz.android.com.flooz.Utils.GalleryImage;
import flooz.android.com.flooz.Utils.ImageGalleryManager;

/**
 * Created by Flooz on 10/7/14.
 */
public class ImageGalleryAdapter extends BaseAdapter {

    private Context context;

    public List<GalleryImage> images;

    public ImageGalleryAdapter(Context c) {
        this.context = c;
        this.images = ImageGalleryManager.getPhoneImages();
    }

    public int getCount() {
        return this.images.size();
    }

    public Object getItem(int position) {
        return null;
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

        imageView.setImageBitmap(this.images.get(position).thumbImg);

        return imageView;
    }
}
