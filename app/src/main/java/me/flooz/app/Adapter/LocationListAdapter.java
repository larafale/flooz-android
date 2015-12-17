package me.flooz.app.Adapter;

import android.app.Activity;
import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;

import org.json.JSONArray;
import org.json.JSONObject;

import fr.quentinklein.slt.LocationTracker;
import fr.quentinklein.slt.TrackerSettings;
import me.flooz.app.Model.FLError;
import me.flooz.app.Network.FloozHttpResponseHandler;
import me.flooz.app.Network.FloozRestClient;
import me.flooz.app.R;
import me.flooz.app.Utils.CustomFonts;
import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;

/**
 * Created by Flooz on 12/3/15.
 */
public class LocationListAdapter  extends BaseAdapter implements StickyListHeadersAdapter {

    public interface LocationListAdapterDelegate {
        void clearCurrentSelection();
        void locationTimeout();
    }

    private Boolean firstLoading = false;
    public Boolean isSearchActive = false;

    private Context context;
    private LayoutInflater inflater;

    public LocationListAdapterDelegate delegate;

    public JSONObject selectedLocation = null;
    public JSONArray aroundPlaces = new JSONArray();
    public JSONArray searchPlaces = new JSONArray();
    private Handler searchHandler;

    private String searchData;

    private String ll;
    private LocationTracker locationTracker;

    private Runnable searchRunnable = new Runnable() {
        @Override
        public void run() {
            if (!searchData.isEmpty()) {
                isSearchActive = true;

                FloozRestClient.getInstance().placesSearch(searchData, ll, new FloozHttpResponseHandler() {
                    @Override
                    public void success(Object response) {
                        searchPlaces = (JSONArray)response;
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

    public LocationListAdapter(Context ctx) {
        this.inflater = LayoutInflater.from(ctx);
        this.context = ctx;

        this.searchHandler = new Handler(Looper.getMainLooper());

        this.ll = FloozRestClient.getInstance().loadLlData();

        if (ll != null) {
            FloozRestClient.getInstance().placesFrom(ll, new FloozHttpResponseHandler() {
                @Override
                public void success(Object response) {
                    aroundPlaces = (JSONArray) response;
                    firstLoading = true;
                    notifyDataSetChanged();
                }

                @Override
                public void failure(int statusCode, FLError error) {

                }
            });
        }

        this.locationTracker = new LocationTracker(this.context, new TrackerSettings().setUsePassive(false)) {
            @Override
            public void onLocationFound(@NonNull Location location) {
                ll = location.getLatitude() + "," + location.getLongitude();
                FloozRestClient.getInstance().saveLlData(ll);
                FloozRestClient.getInstance().placesFrom(ll, new FloozHttpResponseHandler() {
                    @Override
                    public void success(Object response) {
                        aroundPlaces = (JSONArray) response;
                        firstLoading = true;
                        notifyDataSetChanged();
                    }

                    @Override
                    public void failure(int statusCode, FLError error) {

                    }
                });

                LocationListAdapter.this.stopTracker();
            }

            @Override
            public void onTimeout() {
                firstLoading = true;
                notifyDataSetChanged();

                if (delegate != null)
                    delegate.locationTimeout();
            }
        };

        notifyDataSetChanged();

        this.startTracker();
    }

    public void startTracker() {
        if (!this.locationTracker.isListening() && this.ll == null) {
            try {
                this.locationTracker.startListen();
            } catch (SecurityException e) {
                e.printStackTrace();
            }
        }
    }

    public void stopTracker() {
        if (this.locationTracker.isListening()) {
            this.locationTracker.stopListen();
        }
    }

    @Override
    public View getHeaderView(int position, View convertView, ViewGroup parent) {
        HeaderViewHolder holder;

        if (convertView == null || convertView.getTag() == null) {
            holder = new HeaderViewHolder();
            convertView = inflater.inflate(R.layout.account_menu_header, parent, false);
            holder.text = (TextView) convertView.findViewById(R.id.account_menu_header_title);

            holder.text.setTypeface(CustomFonts.customContentBold(this.context));

            convertView.setTag(holder);
        } else {
            holder = (HeaderViewHolder) convertView.getTag();
        }

        if (selectedLocation != null) {
            if (position == 0)
                holder.text.setText(this.context.getResources().getString(R.string.SELECTED_LOCATION));
            else if (!this.isSearchActive)
                holder.text.setText(this.context.getResources().getString(R.string.AROUND_YOU));
            else
                holder.text.setText(this.context.getResources().getString(R.string.LOCATION_RESULTS));
        } else {
            if (!this.isSearchActive)
                holder.text.setText(this.context.getResources().getString(R.string.AROUND_YOU));
            else
                holder.text.setText(this.context.getResources().getString(R.string.LOCATION_RESULTS));
        }

        return convertView;
    }

    @Override
    public long getHeaderId(int i) {
        if (i == 0 && selectedLocation != null)
            return 0;
        if (!isSearchActive)
            return 1;
        return 2;
    }

    @Override
    public int getCount() {
        int count = 0;

        if (selectedLocation != null)
            count += 1;

        if (firstLoading) {
            if (!isSearchActive) {
                if (aroundPlaces.length() > 0)
                    count += aroundPlaces.length();
                else
                    count += 1;
            } else {
                if (searchPlaces.length() > 0)
                    count += searchPlaces.length();
                else
                    count += 1;
            }
        } else
            count += 1;

        return count;
    }

    @Override
    public JSONObject getItem(int position) {
        if (selectedLocation != null) {
            if (position == 0)
                return selectedLocation;
            else if (!firstLoading)
                return null;
            else if (!this.isSearchActive) {
                if (aroundPlaces.length() > 0)
                    return this.aroundPlaces.optJSONObject(position - 1);
                else
                    return null;
            }
            else {
                if (searchPlaces.length() > 0)
                    return this.searchPlaces.optJSONObject(position - 1);
                else
                    return null;
            }
        } else {
            if (!firstLoading)
                return null;
            else if (!this.isSearchActive) {
                if (aroundPlaces.length() > 0)
                    return this.aroundPlaces.optJSONObject(position);
                else
                    return null;
            }
            else {
                if (searchPlaces.length() > 0)
                    return this.searchPlaces.optJSONObject(position);
                else
                    return null;
            }
        }
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;

        if (position == 0 && selectedLocation == null && !firstLoading)
            return LayoutInflater.from(context).inflate(R.layout.progress_row, parent, false);
        else if (position == 1 && selectedLocation != null && !firstLoading)
            return LayoutInflater.from(context).inflate(R.layout.progress_row, parent, false);

        if (convertView == null || convertView.getTag() == null) {
            holder = new ViewHolder();
            convertView = inflater.inflate(R.layout.location_row, parent, false);

            holder.icon = (ImageView) convertView.findViewById(R.id.location_row_img);
            holder.title = (TextView) convertView.findViewById(R.id.location_row_title);
            holder.subtitle = (TextView) convertView.findViewById(R.id.location_row_subtitle);
            holder.remove = (ImageView) convertView.findViewById(R.id.location_row_remove);

            holder.title.setTypeface(CustomFonts.customContentRegular(this.context));
            holder.subtitle.setTypeface(CustomFonts.customContentRegular(this.context));

            holder.remove.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (delegate != null)
                        delegate.clearCurrentSelection();
                }
            });

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        final JSONObject location = this.getItem(position);

        if (((isSearchActive && searchPlaces.length() == 0) || aroundPlaces.length() == 0 || (firstLoading && ll == null)) && (position != 0 || selectedLocation == null)) {
            View empty = LayoutInflater.from(context).inflate(R.layout.empty_row, parent, false);

            TextView emptyText = (TextView) empty.findViewById(R.id.empty_row_text);

            emptyText.setTypeface(CustomFonts.customContentRegular(context));

            emptyText.setText(context.getResources().getString(R.string.EMPTY_LOCATION));

            return empty;
        }

        if (position == 0 && selectedLocation != null)
            holder.remove.setVisibility(View.VISIBLE);
        else
            holder.remove.setVisibility(View.GONE);

        holder.title.setText(location.optString("name"));

        if (location.has("location") && location.optJSONObject("location").has("distance")) {
            double distance = location.optJSONObject("location").optDouble("distance");

            String subtitle = "";

            if (distance >= 1000) {
                subtitle = String.format("%.1f km", distance / 1000);
            } else {
                subtitle = String.format("%.0f m", distance);
            }

            if (location.optJSONObject("location").has("city"))
                subtitle += " - " + location.optJSONObject("location").optString("city");

            holder.subtitle.setText(subtitle);
            holder.subtitle.setVisibility(View.VISIBLE);
        } else
            holder.subtitle.setVisibility(View.GONE);

        if (location.has("categories") && location.optJSONArray("categories").length() > 0) {
            Boolean foundPrimary = false;
            JSONArray categories = location.optJSONArray("categories");

            for (int i = 0; i < categories.length(); i++) {
                JSONObject category = categories.optJSONObject(i);
                if (category.has("primary") && category.optBoolean("primary")) {
                    holder.icon.setVisibility(View.VISIBLE);
                    String imgURL = category.optJSONObject("icon").optString("prefix") + "64" + category.optJSONObject("icon").optString("suffix");
                    ImageLoader.getInstance().displayImage(imgURL, holder.icon);
                    foundPrimary = true;
                    break;
                }
            }

            if (!foundPrimary) {
                JSONObject category = categories.optJSONObject(0);
                holder.icon.setVisibility(View.VISIBLE);
                String imgURL = category.optJSONObject("icon").optString("prefix") + "64" + category.optJSONObject("icon").optString("suffix");
                ImageLoader.getInstance().displayImage(imgURL, holder.icon);
            }
        } else {
            holder.icon.setVisibility(View.INVISIBLE);
        }

        return convertView;
    }

    public void searchPlace(String searchString) {
        this.searchHandler.removeCallbacks(searchRunnable);
        this.searchData = searchString;
        if (searchString.isEmpty()) {
            this.stopSearch();
        } else {
            this.searchHandler.postDelayed(searchRunnable, 500);
        }
    }

    public void stopSearch() {
        this.searchData = "";
        this.searchHandler.removeCallbacks(searchRunnable);
        this.isSearchActive = false;
        this.notifyDataSetChanged();
    }

    class HeaderViewHolder {
        TextView text;
    }

    class ViewHolder {
        ImageView icon;
        TextView title;
        TextView subtitle;
        ImageView remove;
    }
}
