package flooz.android.com.flooz.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

import flooz.android.com.flooz.R;
import flooz.android.com.flooz.Utils.CustomFonts;

/**
 * Created by Flooz on 10/15/14.
 */
public class SettingsListAdapter extends BaseAdapter {

    private LayoutInflater inflater;
    private Context context;

    private List<String> items;

    public SettingsListAdapter(Context ctx, List<String> data) {
        this.inflater = LayoutInflater.from(ctx);
        this.context = ctx;
        this.items = data;
    }

    @Override
    public int getCount() {
        return this.items.size();
    }

    @Override
    public Object getItem(int i) {
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

            holder.text.setTypeface(CustomFonts.customTitleExtraLight(this.context));

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.text.setText(this.items.get(i));

        return convertView;
    }

    class ViewHolder {
        TextView text;
    }
}
