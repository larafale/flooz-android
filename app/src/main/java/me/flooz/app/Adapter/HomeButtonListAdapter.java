package me.flooz.app.Adapter;

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

import java.util.List;

import me.flooz.app.App.FloozApplication;
import me.flooz.app.Model.FLButton;
import me.flooz.app.Network.FloozRestClient;
import me.flooz.app.R;
import me.flooz.app.UI.View.LoadingImageView;
import me.flooz.app.Utils.CustomFonts;
import me.flooz.app.Utils.CustomNotificationIntents;

/**
 * Created by Flooz on 03/05/16.
 */
public class HomeButtonListAdapter extends BaseAdapter {
    private Context context;
    private List<FLButton> buttons;

    private BroadcastReceiver reloadReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            buttons = FloozRestClient.getInstance().currentTexts.homeButtons;
            notifyDataSetChanged();
        }
    };

    public HomeButtonListAdapter(Context ctx) {
        this.context = ctx;
        buttons = FloozRestClient.getInstance().currentTexts.homeButtons;
    }

    public void loadBroadcastReceivers() {
        LocalBroadcastManager.getInstance(FloozApplication.getAppContext()).registerReceiver(reloadReceiver,
                CustomNotificationIntents.filterReloadText());
    }

    public void unloadBroadcastReceivers() {
        LocalBroadcastManager.getInstance(FloozApplication.getAppContext()).unregisterReceiver(reloadReceiver);
    }

    @Override
    public int getCount() {
        return FloozRestClient.getInstance().currentTexts.homeButtons.size();
    }

    @Override
    public Object getItem(int i) {
        return FloozRestClient.getInstance().currentTexts.homeButtons.get(i);
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
            convertView = LayoutInflater.from(this.context).inflate(R.layout.home_button, parent, false);
            holder.imageView = (ImageView) convertView.findViewById(R.id.home_button_img);
            holder.title = (TextView) convertView.findViewById(R.id.home_button_title);
            holder.subtitle = (TextView) convertView.findViewById(R.id.home_button_subtitle);
            holder.arrow = (ImageView) convertView.findViewById(R.id.home_button_arrow);
            holder.avalaible = (TextView) convertView.findViewById(R.id.home_button_available);

            holder.title.setTypeface(CustomFonts.customContentRegular(this.context));
            holder.subtitle.setTypeface(CustomFonts.customContentRegular(this.context));
            holder.avalaible.setTypeface(CustomFonts.customContentBold(this.context));

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        FLButton button = FloozRestClient.getInstance().currentTexts.homeButtons.get(i);

        int id = context.getResources().getIdentifier(button.defaultImg, "drawable", context.getPackageName());

        if (id != 0)
            holder.imageView.setImageDrawable(context.getResources().getDrawable(id));

        if (button.imgUrl != null && !button.imgUrl.isEmpty())
            FloozApplication.getInstance().imageFetcher.attachImage(button.imgUrl, holder.imageView);

        holder.title.setText(button.title);
        holder.subtitle.setText(button.subtitle);

        if (button.avalaible) {
            holder.imageView.setColorFilter(context.getResources().getColor(R.color.blue));
            holder.arrow.setColorFilter(context.getResources().getColor(R.color.blue));
            holder.avalaible.setVisibility(View.GONE);
        } else {
            holder.imageView.setColorFilter(context.getResources().getColor(R.color.placeholder));
            holder.arrow.setColorFilter(context.getResources().getColor(R.color.placeholder));
            holder.avalaible.setVisibility(View.VISIBLE);
        }

        return convertView;
    }

    class ViewHolder {
        ImageView imageView;
        TextView title;
        TextView subtitle;
        ImageView arrow;
        TextView avalaible;
    }

}
