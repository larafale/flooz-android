package me.flooz.app.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import org.json.JSONArray;

import me.flooz.app.R;
import me.flooz.app.Utils.CustomFonts;

/**
 * Created by Flooz on 05/08/16.
 */
public class ImagePickerSuggestAdapter extends BaseAdapter {
    private Context context;
    private LayoutInflater inflater;

    private JSONArray items;

    public ImagePickerSuggestAdapter(Context ctx, JSONArray items) {
        this.inflater = LayoutInflater.from(ctx);
        this.context = ctx;

        this.items = items;

        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        if (items != null && items.length() > 0)
            return items.length();
        return 0;
    }

    @Override
    public String getItem(int i) {
        if (items != null && items.length() > 0)
            return items.optString(i);
        return null;
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View convertView, ViewGroup parent) {
        final ViewHolder holder;

        if (convertView == null || convertView.getTag() == null) {
            holder = new ViewHolder();
            convertView = inflater.inflate(R.layout.image_picker_suggest_cell, parent, false);
            holder.textView = (TextView) convertView.findViewById(R.id.image_picker_suggest_cell_text);

            holder.textView.setTypeface(CustomFonts.customContentRegular(this.context));

            convertView.setTag(holder);
        }
        else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.textView.setText(this.getItem(i));

        return convertView;
    }

    public class ViewHolder {
        TextView textView;
    }

}
