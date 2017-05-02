package me.flooz.app.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

import me.flooz.app.Model.FLUser;
import me.flooz.app.Network.FloozRestClient;
import me.flooz.app.R;
import me.flooz.app.Utils.CustomFonts;

/**
 * Created by Flooz on 26/05/16.
 */
public class SettingsDocumentAdapter extends BaseAdapter {

    private LayoutInflater inflater;
    private Context context;
    private JSONArray items;

    public SettingsDocumentAdapter(Context ctx, JSONArray items) {
        this.inflater = LayoutInflater.from(ctx);
        this.context = ctx;
        this.items = items;
    }

    @Override
    public int getCount() {
        return this.items.length();
    }

    @Override
    public JSONObject getItem(int i) {
        return this.items.optJSONObject(i);
    }

    public String getItemKey(int i) { return this.items.optJSONObject(i).optString("key"); }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View convertView, ViewGroup parent) {
        final ViewHolder holder;

        if (convertView == null) {
            holder = new ViewHolder();
            convertView = inflater.inflate(R.layout.settings_document_row, parent, false);
            holder.text = (TextView) convertView.findViewById(R.id.settings_document_row_text);
            holder.pic = (ImageView) convertView.findViewById(R.id.settings_document_row_img);

            holder.text.setTypeface(CustomFonts.customContentRegular(this.context));

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        JSONObject item = this.getItem(i);

        holder.text.setText(item.optString("title"));

        FLUser currentUser = FloozRestClient.getInstance().currentUser;

        String key = item.optString("key");

        if (currentUser.checkDocuments.get(key).equals(0))
            holder.pic.setImageResource(R.drawable.document_refused);
        else if (currentUser.checkDocuments.get(key).equals(3) || currentUser.checkDocuments.get(key).equals(4))
            holder.pic.setImageResource(R.drawable.friends_add);
        else
            holder.pic.setImageResource(R.drawable.friends_accepted);

        return convertView;
    }

    class ViewHolder {
        TextView text;
        ImageView pic;
    }

}
