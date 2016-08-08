package me.flooz.app.Adapter;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.makeramen.roundedimageview.RoundedImageView;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.json.JSONArray;
import org.json.JSONObject;

import me.flooz.app.Model.FLError;
import me.flooz.app.Model.FLUser;
import me.flooz.app.Network.FloozHttpResponseHandler;
import me.flooz.app.Network.FloozRestClient;
import me.flooz.app.R;
import me.flooz.app.UI.View.LoadingImageView;
import me.flooz.app.Utils.CustomFonts;

/**
 * Created by Flooz on 05/08/16.
 */
public class ImagePickerAdapter extends BaseAdapter {
    private Context context;
    private LayoutInflater inflater;

    private Boolean firstLoading = false;
    public Boolean isSearchActive = false;

    private String type;
    private String searchString;
    private Handler searchHandler;
    private JSONArray items;
    public JSONArray defaultItems;


    private Runnable searchRunnable = new Runnable() {
        @Override
        public void run() {
            if (!searchString.isEmpty()) {
                isSearchActive = true;

                FloozRestClient.getInstance().imagesSearch(searchString, type, new FloozHttpResponseHandler() {
                    @Override
                    public void success(Object response) {
                        items = (JSONArray)response;
                        if (isSearchActive)
                            notifyDataSetChanged();
                    }

                    @Override
                    public void failure(int statusCode, FLError error) {

                    }
                });
            }
        }
    };

    public ImagePickerAdapter(Context ctx, String type) {
        this.inflater = LayoutInflater.from(ctx);
        this.context = ctx;

        this.type = type;
        this.searchString = "";
        this.searchHandler = new Handler(Looper.getMainLooper());

        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        if (searchString.isEmpty() && defaultItems != null) {
            return defaultItems.length();
        } else {
            if (items != null && items.length() > 0)
                return items.length();
            return 0;
        }
    }

    @Override
    public JSONObject getItem(int i) {
        if (searchString.isEmpty() && defaultItems != null) {
            return defaultItems.optJSONObject(i);
        } else {
            if (items != null && items.length() > 0)
                return items.optJSONObject(i);
            return null;
        }
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View convertView, ViewGroup parent) {
        final ViewHolder holder;

        JSONObject item = null;

        if (this.searchString != null && !this.searchString.isEmpty()) {
            if (items != null)
                item = items.optJSONObject(i);
        } else if (defaultItems != null && defaultItems.length() > 0) {
            item = defaultItems.optJSONObject(i);
        }

        if (convertView == null || convertView.getTag() == null) {
            holder = new ViewHolder();
            convertView = inflater.inflate(R.layout.image_picker_cell, parent, false);
            holder.imageView = (LoadingImageView) convertView.findViewById(R.id.image_picker_cell_image);

            convertView.setTag(holder);
        }
        else {
            holder = (ViewHolder) convertView.getTag();
        }

        if (item != null) {
           holder.imageView.setImageFromUrl(item.optString("thumbnail"));
        }

        return convertView;
    }

    public void search(String searchString) {
        this.searchHandler.removeCallbacks(searchRunnable);
        this.searchString = searchString;
        if (searchString.isEmpty()) {
            this.stopSearch();
        } else {
            this.searchHandler.postDelayed(searchRunnable, 500);
        }
    }

    public void stopSearch() {
        this.searchString = "";
        this.searchHandler.removeCallbacks(searchRunnable);
        this.isSearchActive = false;
        this.notifyDataSetChanged();
    }

    public class ViewHolder {
        LoadingImageView imageView;
    }
}
