package me.flooz.app.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import me.flooz.app.Model.FLError;
import me.flooz.app.Network.FloozHttpResponseHandler;
import me.flooz.app.Network.FloozRestClient;
import me.flooz.app.R;
import me.flooz.app.Utils.CustomFonts;
import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;

/**
 * Created by Flooz on 12/16/14.
 */
public class NotificationSettingsListAdapter extends BaseAdapter implements StickyListHeadersAdapter {

    private Context context;
    private LayoutInflater inflater;
    private Map<String, Object> notifications;
    private JSONObject notificationsText;

    public NotificationSettingsListAdapter(Context ctx) {
        this.inflater = LayoutInflater.from(ctx);
        this.context = ctx;

        this.notifications = FloozRestClient.getInstance().currentUser.notifications;

        if (FloozRestClient.getInstance().currentTexts != null)
            this.notificationsText = FloozRestClient.getInstance().currentTexts.notificationsText;
        else {
            FloozRestClient.getInstance().loadTextData();
            if (FloozRestClient.getInstance().currentTexts != null) {
                this.notificationsText = FloozRestClient.getInstance().currentTexts.notificationsText;
            } else {
                FloozRestClient.getInstance().textObjectFromApi(new FloozHttpResponseHandler() {
                    @Override
                    public void success(Object response) {
                        notificationsText = FloozRestClient.getInstance().currentTexts.notificationsText;
                        notifyDataSetChanged();
                    }

                    @Override
                    public void failure(int statusCode, FLError error) {

                    }
                });
            }
        }
    }

    @Override
    public View getHeaderView(int i, View view, ViewGroup viewGroup) {
        HeaderViewHolder holder;

        if (view == null) {
            holder = new HeaderViewHolder();
            view = inflater.inflate(R.layout.account_menu_header, viewGroup, false);
            holder.text = (TextView) view.findViewById(R.id.account_menu_header_title);

            holder.text.setTypeface(CustomFonts.customContentRegular(this.context));

            view.setTag(holder);
        } else {
            holder = (HeaderViewHolder) view.getTag();
        }

        if (i < ((HashMap)this.notifications.get("push")).size())
            holder.text.setText(R.string.NOTIFICATIONS_SECTION_PUSH);
        else
            holder.text.setText(R.string.NOTIFICATIONS_SECTION_EMAIL);

        return view;
    }

    @Override
    public long getHeaderId(int i) {
        if (i < ((HashMap)this.notifications.get("push")).size())
            return 0;
        else
            return 1;
    }

    @Override
    public int getCount() {
        if (this.notifications != null && this.notifications.get("email") != null && this.notifications.get("push") != null)
            return ((HashMap)this.notifications.get("email")).size() + ((HashMap)this.notifications.get("push")).size();
        return 0;
    }

    @Override
    public Boolean getItem(int position) {
        if (position < ((HashMap)this.notifications.get("push")).size()) {
            HashMap<String, Boolean> map = (HashMap<String, Boolean>)this.notifications.get("push");
            Object[] values = map.values().toArray();
            return (Boolean)values[position];
        }
        else {
            HashMap<String, Boolean> map = (HashMap<String, Boolean>)this.notifications.get("email");
            Object[] values = map.values().toArray();
            return (Boolean)values[position - ((HashMap) this.notifications.get("push")).size()];
        }
    }

    public String getItemTitle(int position) {
        String key;

        if (position < ((HashMap)this.notifications.get("push")).size())
            key = (String)((HashMap)this.notifications.get("push")).keySet().toArray()[position];
        else
            key = (String)((HashMap)this.notifications.get("email")).keySet().toArray()[position - ((HashMap)this.notifications.get("push")).size()];

        if (this.notificationsText != null)
            return this.notificationsText.optString(key);
        else
            return "";
    }

    public String getItemKey(int position) {
        String key;

        if (position < ((HashMap)this.notifications.get("push")).size())
            key = (String)((HashMap)this.notifications.get("push")).keySet().toArray()[position];
        else
            key = (String)((HashMap)this.notifications.get("email")).keySet().toArray()[position - ((HashMap)this.notifications.get("push")).size()];

        return key;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;

        holder = new ViewHolder();
        convertView = inflater.inflate(R.layout.settings_notification_row, parent, false);
        holder.settingName = (TextView) convertView.findViewById(R.id.settings_notification_row_text);
        holder.settingSwitch = (CheckBox) convertView.findViewById(R.id.settings_notification_row_toggle);

        holder.settingName.setTypeface(CustomFonts.customContentRegular(this.context));

        convertView.setTag(holder);

        final String itemKey = getItemKey(position);
        String itemTitle = getItemTitle(position);
        Boolean itemValue = getItem(position);

        holder.settingName.setText(itemTitle);
        holder.settingSwitch.setChecked(itemValue);

        holder.settingSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (position < ((HashMap) notifications.get("push")).size()) {
                    ((HashMap) notifications.get("push")).put(itemKey, isChecked);
                    FloozRestClient.getInstance().updateNotificationSettings("push", itemKey, isChecked, null);
                } else {
                    ((HashMap) notifications.get("email")).put(itemKey, isChecked);
                    FloozRestClient.getInstance().updateNotificationSettings("email", itemKey, isChecked, null);
                }
                notifyDataSetChanged();
            }
        });

        return convertView;
    }

    class HeaderViewHolder {
        TextView text;
    }

    class ViewHolder {
        TextView settingName;
        CheckBox settingSwitch;
    }
}
