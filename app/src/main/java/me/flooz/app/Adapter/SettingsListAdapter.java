package me.flooz.app.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

import me.flooz.app.R;
import me.flooz.app.Utils.CustomFonts;

public class SettingsListAdapter extends BaseAdapter {

    private LayoutInflater inflater;
    private Context context;

    private List<SettingsListItem> items;

    public SettingsListAdapter(Context ctx, List<SettingsListItem> data, ListView list) {
        this.inflater = LayoutInflater.from(ctx);
        this.context = ctx;
        this.items = data;

        list.setAdapter(this);
        list.setOnItemClickListener((parent, view, position, id) -> getItem(position).getItemClickListener().onItemClick(parent, view, position, id));
    }

    @Override
    public int getCount() {
        return this.items.size();
    }

    @Override
    public SettingsListItem getItem(int i) {
        return this.items.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View convertView, ViewGroup parent) {
        final ViewHolder holder;

        if (convertView == null) {
            holder = new ViewHolder();
            convertView = inflater.inflate(R.layout.account_menu_row, parent, false);
            holder.text = (TextView) convertView.findViewById(R.id.account_menu_row_title);
            holder.notifsText = (TextView) convertView.findViewById(R.id.account_menu_row_notif);

            holder.text.setTypeface(CustomFonts.customContentRegular(this.context));
            holder.notifsText.setTypeface(CustomFonts.customContentRegular(this.context));

            ((ImageView) convertView.findViewById(R.id.account_menu_row_arrow)).setColorFilter(inflater.getContext().getResources().getColor(R.color.placeholder));

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        SettingsListItem item = this.items.get(i);

        holder.text.setText(item.getTitle());

        if (item.hasNotifs()) {
            holder.notifsText.setVisibility(View.VISIBLE);
            holder.notifsText.setText(String.format("%d", item.getNbNotifs()));
        } else {
            holder.notifsText.setVisibility(View.GONE);
        }

        return convertView;
    }

    class ViewHolder {
        TextView text;
        TextView notifsText;
    }
}
