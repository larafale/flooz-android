package flooz.android.com.flooz.Adapter;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.makeramen.RoundedImageView;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.List;

import flooz.android.com.flooz.App.FloozApplication;
import flooz.android.com.flooz.Model.FLNotification;
import flooz.android.com.flooz.Network.FloozRestClient;
import flooz.android.com.flooz.R;
import flooz.android.com.flooz.Utils.CustomFonts;
import flooz.android.com.flooz.Utils.CustomNotificationIntents;

/**
 * Created by Flooz on 10/15/14.
 */
public class NotificationListAdapter extends BaseAdapter {

    private Context context;
    private List<FLNotification> notifications;

    private BroadcastReceiver reloadNotificationsReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            notifications = FloozRestClient.getInstance().notificationsManager.notifications;
            notifyDataSetChanged();
        }
    };

    public NotificationListAdapter(Context ctx) {
        this.context = ctx;
        this.notifications = FloozRestClient.getInstance().notificationsManager.notifications;

        LocalBroadcastManager.getInstance(FloozApplication.getAppContext()).registerReceiver(reloadNotificationsReceiver,
                CustomNotificationIntents.filterReloadNotifications());
    }

    @Override
    public int getCount() {
        return this.notifications.size();
    }

    @Override
    public Object getItem(int i) {
        return this.notifications.get(i);
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
            convertView = LayoutInflater.from(this.context).inflate(R.layout.notification_list_row, parent, false);
            holder.isRead = (ImageView) convertView.findViewById(R.id.notification_row_read_indicator);
            holder.content = (TextView) convertView.findViewById(R.id.notification_row_content);
            holder.img = (RoundedImageView) convertView.findViewById(R.id.notification_row_img);
            holder.date = (TextView) convertView.findViewById(R.id.notification_row_date);

            holder.content.setTypeface(CustomFonts.customContentLight(this.context));
            holder.date.setTypeface(CustomFonts.customContentLight(this.context));

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        FLNotification notif = this.notifications.get(i);

        if (notif.isRead)
            holder.isRead.setVisibility(View.INVISIBLE);
        else
            holder.isRead.setVisibility(View.VISIBLE);

        holder.content.setText(notif.content);

        holder.img.setImageDrawable(this.context.getResources().getDrawable(R.drawable.avatar_default));
        if (notif.user.avatarURL != null && !notif.user.avatarURL.isEmpty())
            ImageLoader.getInstance().displayImage(notif.user.avatarURL, holder.img);

        holder.date.setText(notif.dateText);

        return convertView;
    }

    class ViewHolder {
        ImageView isRead;
        TextView content;
        RoundedImageView img;
        TextView date;
    }
}
