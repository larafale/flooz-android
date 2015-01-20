package flooz.android.com.flooz.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.gc.materialdesign.views.Switch;

import java.util.HashMap;
import java.util.Map;

import flooz.android.com.flooz.Network.FloozRestClient;
import flooz.android.com.flooz.R;
import flooz.android.com.flooz.Utils.CustomFonts;
import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;

/**
 * Created by Flooz on 12/16/14.
 */
public class NotificationSettingsListAdapter extends BaseAdapter implements StickyListHeadersAdapter {

    private Context context;
    private LayoutInflater inflater;
    private Map<String, Object> notifications;
    private Map<String, Object> notificationsText;

    public NotificationSettingsListAdapter(Context ctx) {
        this.inflater = LayoutInflater.from(ctx);
        this.context = ctx;

        this.notifications = FloozRestClient.getInstance().currentUser.notifications;
        this.notificationsText = FloozRestClient.getInstance().currentUser.notificationsText;
    }

    @Override
    public View getHeaderView(int i, View view, ViewGroup viewGroup) {
        HeaderViewHolder holder;

        if (view == null) {
            holder = new HeaderViewHolder();
            view = inflater.inflate(R.layout.settings_notification_header, viewGroup, false);
            holder.text = (TextView) view.findViewById(R.id.settings_notification_header_text);

            holder.text.setTypeface(CustomFonts.customTitleExtraLight(this.context));

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
        return ((HashMap)this.notifications.get("email")).size() + ((HashMap)this.notifications.get("push")).size();
    }

    @Override
    public Boolean getItem(int position) {
        if (position < ((HashMap)this.notifications.get("push")).size())
            return (Boolean)((HashMap)this.notifications.get("push")).values().toArray()[position];
        else
            return (Boolean)((HashMap)this.notifications.get("email")).values().toArray()[position - ((HashMap)this.notifications.get("push")).size()];
    }

    public String getItemTitle(int position) {

        String key;

        if (position < ((HashMap)this.notifications.get("push")).size())
            key = (String)((HashMap)this.notifications.get("push")).keySet().toArray()[position];
        else
            key = (String)((HashMap)this.notifications.get("email")).keySet().toArray()[position - ((HashMap)this.notifications.get("push")).size()];

        return (String)this.notificationsText.get(key);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;

        if (convertView == null) {
            holder = new ViewHolder();
            convertView = inflater.inflate(R.layout.settings_notification_row, parent, false);
            holder.settingName = (TextView) convertView.findViewById(R.id.settings_notification_row_text);
            holder.settingSwitch = (Switch) convertView.findViewById(R.id.settings_notification_row_toggle);

            holder.settingName.setTypeface(CustomFonts.customTitleExtraLight(this.context));

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.settingName.setText(getItemTitle(position));
        holder.settingSwitch.setChecked(getItem(position));

        holder.settingSwitch.setOncheckListener(new Switch.OnCheckListener() {
            @Override
            public void onCheck(boolean check) {
                String key;

                if (position < ((HashMap)notifications.get("push")).size()) {
                    key = (String) ((HashMap) notifications.get("push")).keySet().toArray()[position];
                    ((HashMap) notifications.get("push")).put(key, check);
                    FloozRestClient.getInstance().updateNotificationSettings("push", key, check, null);
                }
                else {
                    key = (String) ((HashMap) notifications.get("email")).keySet().toArray()[position - ((HashMap) notifications.get("push")).size()];
                    ((HashMap) notifications.get("email")).put(key, check);
                    FloozRestClient.getInstance().updateNotificationSettings("email", key, check, null);
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
        Switch settingSwitch;
    }
}
