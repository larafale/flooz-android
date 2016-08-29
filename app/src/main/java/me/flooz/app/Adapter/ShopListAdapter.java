package me.flooz.app.Adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.makeramen.roundedimageview.RoundedImageView;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import net.qiujuer.genius.blur.StackBlur;
import net.qiujuer.genius.kit.Kit;

import java.util.List;
import java.util.Map;

import me.flooz.app.Model.FLError;
import me.flooz.app.Model.FLShopItem;
import me.flooz.app.Network.FloozHttpResponseHandler;
import me.flooz.app.Network.FloozRestClient;
import me.flooz.app.R;
import me.flooz.app.UI.Activity.ShopListActivity;
import me.flooz.app.UI.Drawables.ShopLabelBackgroundDrawable;
import me.flooz.app.Utils.CustomFonts;

/**
 * Created by Flooz on 26/08/16.
 */
public class ShopListAdapter extends BaseAdapter {
    ShopListActivity activity;

    private Context context;
    private LayoutInflater inflater;

    public Boolean isSearchActive = false;
    public Boolean isSearchLoaded = false;

    private String loadURL;

    private String nextSearchURL;
    private String nextURL;

    private String searchString;

    private Handler searchHandler;
    private List<FLShopItem> items;
    public List<FLShopItem> defaultItems;

    private Runnable searchRunnable = new Runnable() {
        @Override
        public void run() {
            if (!searchString.isEmpty()) {
                isSearchActive = true;
                isSearchLoaded = false;

                FloozRestClient.getInstance().shopList(loadURL + "?q=" + searchString, new FloozHttpResponseHandler() {
                    @Override
                    public void success(Object response) {
                        Map<String, Object> ret = (Map<String, Object>) response;

                        items = (List<FLShopItem>) ret.get("shopItems");
                        nextSearchURL = (String) ret.get("nextUrl");
                        isSearchLoaded = true;

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

    public ShopListAdapter(ShopListActivity activity, String loadURL) {
        this.inflater = LayoutInflater.from(activity);
        this.context = activity;
        this.activity = activity;

        this.loadURL = loadURL;
        this.searchString = "";
        this.searchHandler = new Handler(Looper.getMainLooper());


        FloozRestClient.getInstance().shopList(loadURL, new FloozHttpResponseHandler() {
            @Override
            public void success(Object response) {

                Map<String, Object> ret = (Map<String, Object>) response;

                defaultItems = (List<FLShopItem>) ret.get("shopItems");
                nextURL = (String) ret.get("nextUrl");

                notifyDataSetChanged();
            }

            @Override
            public void failure(int statusCode, FLError error) {

            }
        });

        notifyDataSetChanged();
    }

    @Override
    public int getCount() {

        if (this.isSearchActive) {
            if (items != null && items.size() > 0) {
                if (this.nextSearchURL != null && !this.nextSearchURL.isEmpty())
                    return items.size() + 1;

                return items.size();
            }
            return 1;
        }

        if (defaultItems != null && defaultItems.size() > 0) {
            if (this.nextURL != null && !this.nextURL.isEmpty())
                return defaultItems.size() + 1;

            return defaultItems.size();
        }

        return 1;
    }

    @Override
    public FLShopItem getItem(int i) {
        if (isSearchActive) {
            if (items != null && items.size() > 0) {
                if (i == items.size())
                    return null;

                return items.get(i);

            } else if (this.isSearchLoaded)
                return null;

        } else if (defaultItems != null && defaultItems.size() > 0) {
            if (i == defaultItems.size())
                return null;

            return defaultItems.get(i);
        }

        return null;
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    private View getCategoryView(int i, View convertView, ViewGroup parent, FLShopItem currentItem) {
        final CategoryViewHolder viewHolder;

        if (convertView == null || convertView.getTag() == null || !(convertView.getTag() instanceof CategoryViewHolder)) {
            viewHolder = new CategoryViewHolder();
            convertView = inflater.inflate(R.layout.shop_list_category_cell, parent, false);

            viewHolder.pic = (RoundedImageView) convertView.findViewById(R.id.shop_category_cell_pic);
            viewHolder.name = (TextView) convertView.findViewById(R.id.shop_category_cell_title);

            viewHolder.name.setTypeface(CustomFonts.customContentBold(context));

            convertView.setTag(viewHolder);
        }
        else {
            viewHolder = (CategoryViewHolder) convertView.getTag();
        }

        if (currentItem != null) {
            viewHolder.pic.setImageBitmap(null);

            ImageLoader.getInstance().loadImage(currentItem.pic, new ImageLoadingListener() {
                @Override
                public void onLoadingStarted(String imageUri, View view) {

                }

                @Override
                public void onLoadingFailed(String imageUri, View view, FailReason failReason) {

                }

                @Override
                public void onLoadingComplete(String imageUri, View view, final Bitmap loadedImage) {

                    Thread thread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                final Bitmap image = StackBlur.blurNatively(loadedImage, 10, true);

                                ShopListAdapter.this.activity.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        viewHolder.pic.setImageBitmap(image);
                                    }
                                });

                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });

                    thread.setDaemon(true);
                    thread.start();
                }

                @Override
                public void onLoadingCancelled(String imageUri, View view) {

                }
            });

            viewHolder.name.setText(currentItem.name);
        }

        return convertView;
    }

    private View getCardView(int i, View convertView, ViewGroup parent, FLShopItem currentItem) {
        CardViewHolder viewHolder;

        if (convertView == null || convertView.getTag() == null || !(convertView.getTag() instanceof CardViewHolder)) {
            viewHolder = new CardViewHolder();
            convertView = inflater.inflate(R.layout.shop_list_card_cell, parent, false);

            viewHolder.pic = (RoundedImageView) convertView.findViewById(R.id.shop_card_cell_pic);
            viewHolder.labelBack = (LinearLayout) convertView.findViewById(R.id.shop_card_cell_label);
            viewHolder.name = (TextView) convertView.findViewById(R.id.shop_card_cell_name);
            viewHolder.amount = (TextView) convertView.findViewById(R.id.shop_card_cell_amount);

            viewHolder.labelBack.setBackground(new ShopLabelBackgroundDrawable());

            viewHolder.name.setTypeface(CustomFonts.customContentRegular(context));
            viewHolder.amount.setTypeface(CustomFonts.customContentBold(context));

            convertView.setTag(viewHolder);
        }
        else {
            viewHolder = (CardViewHolder) convertView.getTag();
        }

        if (currentItem != null) {
            ImageLoader.getInstance().displayImage(currentItem.pic, viewHolder.pic);

            viewHolder.name.setText(currentItem.name);

            if (currentItem.value != null && !currentItem.value.isEmpty()) {
                viewHolder.amount.setVisibility(View.VISIBLE);
                viewHolder.amount.setText(currentItem.value);
            } else
                viewHolder.amount.setVisibility(View.GONE);
        }

        return convertView;
    }

    private View getEmptyView(ViewGroup parent) {
        View empty = LayoutInflater.from(context).inflate(R.layout.empty_row, parent, false);

        TextView emptyText = (TextView) empty.findViewById(R.id.empty_row_text);

        emptyText.setTypeface(CustomFonts.customContentRegular(context));
        emptyText.setText(context.getResources().getString(R.string.EMPTY_LOCATION));

        return empty;
    }

    private View getLoadingView(ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.progress_row, parent, false);
    }

    @Override
    public View getView(int i, View convertView, ViewGroup parent) {

        FLShopItem currentItem = null;

        if (isSearchActive) {
            if (items != null && items.size() > 0) {
                if (i == items.size())
                    return this.getLoadingView(parent);

                currentItem = items.get(i);

            } else if (this.isSearchLoaded)
                return this.getEmptyView(parent);
            return this.getLoadingView(parent);
        } else if (defaultItems != null && defaultItems.size() > 0) {
            if (i == defaultItems.size())
                return this.getLoadingView(parent);

            currentItem = defaultItems.get(i);

        } else
            return this.getLoadingView(parent);


        if (currentItem != null) {
            switch (currentItem.type) {
                case ShopItemTypeCategory:
                    convertView = getCategoryView(i, convertView, parent, currentItem);
                    break;
                case ShopItemTypeCard:
                    convertView = getCardView(i, convertView, parent, currentItem);
                    break;
            }
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

    public class CategoryViewHolder {
        public RoundedImageView pic;
        public TextView name;
    }

    public class CardViewHolder {
        public RoundedImageView pic;
        public LinearLayout labelBack;
        public TextView name;
        public TextView amount;
    }

}
