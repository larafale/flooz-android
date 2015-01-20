package flooz.android.com.flooz.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

import flooz.android.com.flooz.R;
import flooz.android.com.flooz.Utils.CustomFonts;

public class SettingsListAdapter extends BaseAdapter {

    private LayoutInflater inflater;
    private Context context;

    private List<SettingsListItem> items;
    private ListView listView;

    public SettingsListAdapter(Context ctx, List<SettingsListItem> data, ListView list) {
        this.inflater = LayoutInflater.from(ctx);
        this.context = ctx;
        this.items = data;
        this.listView = list;

        this.listView.setAdapter(this);
        this.listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                getItem(position).getItemClickListener().onItemClick(parent, view, position, id);
            }
        });
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
            convertView = inflater.inflate(R.layout.settings_row, parent, false);
            holder.text = (TextView) convertView.findViewById(R.id.settings_row_text);
            holder.notifsContainer = (LinearLayout) convertView.findViewById(R.id.settings_row_notifs_container);
            holder.notifsText = (TextView) convertView.findViewById(R.id.settings_row_notifs_text);

            holder.text.setTypeface(CustomFonts.customTitleExtraLight(this.context));
            holder.notifsText.setTypeface(CustomFonts.customContentBold(this.context));

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        SettingsListItem item = this.items.get(i);

        holder.text.setText(item.getTitle());

        if (item.hasNotifs()) {
            holder.notifsContainer.setVisibility(View.VISIBLE);
            holder.notifsText.setText(String.format("%d", item.getNbNotifs()));
        } else {
            holder.notifsContainer.setVisibility(View.GONE);
        }

        return convertView;
    }

    class ViewHolder {
        TextView text;
        LinearLayout notifsContainer;
        TextView notifsText;
    }
}
