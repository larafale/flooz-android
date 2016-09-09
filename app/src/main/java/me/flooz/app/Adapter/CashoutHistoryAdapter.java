package me.flooz.app.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Map;

import me.flooz.app.Model.FLError;
import me.flooz.app.Network.FloozHttpResponseHandler;
import me.flooz.app.Network.FloozRestClient;
import me.flooz.app.R;
import me.flooz.app.Utils.CustomFonts;
import me.flooz.app.Utils.FLHelper;
import me.flooz.app.Utils.JSONHelper;
import me.flooz.app.Utils.MomentDate;

/**
 * Created by Flooz on 09/09/16.
 */
public class CashoutHistoryAdapter extends BaseAdapter {

    private Context context;
    private LayoutInflater inflater;

    public Boolean isLoaded = false;
    public Boolean isLoadingNext = false;

    private String nextURL;

    private JSONArray items;

    public CashoutHistoryAdapter(Context context) {
        this.inflater = LayoutInflater.from(context);
        this.context = context;

        FloozRestClient.getInstance().cashoutHistory(new FloozHttpResponseHandler() {
            @Override
            public void success(Object response) {

                Map<String, Object> ret = (Map<String, Object>) response;

                items = (JSONArray) ret.get("items");
                nextURL = (String) ret.get("nextUrl");
                isLoaded = true;

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
        if (isLoaded) {
            if (items != null && items.length() > 0) {
                if (this.nextURL != null && !this.nextURL.isEmpty())
                    return items.length() + 1;
                return items.length();
            }

        }

        return 1;
    }

    @Override
    public JSONObject getItem(int i) {
        if (items != null && items.length() > 0) {
            if (i == items.length())
                return null;

            return items.optJSONObject(i);
        }

        return null;
    }

    @Override
    public long getItemId(int i) {
        return i;
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

        JSONObject currentItem;

        if (items != null && items.length() > 0) {
            if (i == items.length())
                return this.getLoadingView(parent);

            currentItem = items.optJSONObject(i);
        } else if (isLoaded)
            return this.getEmptyView(parent);
        else
            return this.getLoadingView(parent);

        if (currentItem != null) {
            ViewHolder viewHolder;

            if (convertView == null || convertView.getTag() == null || !(convertView.getTag() instanceof ViewHolder)) {
                viewHolder = new ViewHolder();
                convertView = inflater.inflate(R.layout.cashout_history_cell, parent, false);

                viewHolder.date = (TextView) convertView.findViewById(R.id.cashout_history_cell_date);
                viewHolder.amount = (TextView) convertView.findViewById(R.id.cashout_history_cell_amount);

                viewHolder.date.setTypeface(CustomFonts.customContentRegular(context));
                viewHolder.amount.setTypeface(CustomFonts.customContentBold(context));

                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            SimpleDateFormat sdf = new SimpleDateFormat("dd MMM à HH:mm", Locale.FRANCE);

            try {
                MomentDate mDate = new MomentDate(context, currentItem.optString("cAt"));

                viewHolder.date.setText(sdf.format(mDate.getDate()));
            } catch (ParseException e) {
                e.printStackTrace();
            }

            viewHolder.amount.setText(FLHelper.trimTrailingZeros(String.format(Locale.US, "%.2f", currentItem.optDouble("amount"))) + " €");
        }

        return convertView;
    }

    public void loadNextPage() {
        if (!isLoadingNext && nextURL != null && !nextURL.isEmpty()) {
            isLoadingNext = true;
            FloozRestClient.getInstance().cashoutHistoryNextPage(nextURL, new FloozHttpResponseHandler() {
                @Override
                public void success(Object response) {
                    Map<String, Object> ret = (Map<String, Object>) response;

                    try {
                        items = JSONHelper.concatArray(items, (JSONArray) ret.get("items"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    nextURL = (String) ret.get("nextUrl");
                    isLoadingNext = false;

                    notifyDataSetChanged();
                }

                @Override
                public void failure(int statusCode, FLError error) {

                }
            });
        }
    }

    public class ViewHolder {
        public TextView date;
        public TextView amount;
    }

}
