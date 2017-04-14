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

import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.List;

import me.flooz.app.App.FloozApplication;
import me.flooz.app.Model.FLButton;
import me.flooz.app.Network.FloozRestClient;
import me.flooz.app.R;
import me.flooz.app.Utils.CustomFonts;

/**
 * Created by Flooz on 22/05/16.
 */
public class PaymentAdapter extends BaseAdapter {
    private Context context;
    private List<FLButton> buttons;


    public PaymentAdapter(Context ctx, List<FLButton> items) {
        this.context = ctx;

        buttons = items;

        if (buttons == null || buttons.size() == 0) {
            buttons = FloozRestClient.getInstance().currentTexts.paymentSources;
        }
    }

    @Override
    public int getCount() {
        return this.buttons.size();
    }

    @Override
    public Object getItem(int i) {
        return this.buttons.get(i);
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
            convertView = LayoutInflater.from(this.context).inflate(R.layout.cashin_list_row, parent, false);
            holder.imageView = (ImageView) convertView.findViewById(R.id.cashin_row_img);
            holder.title = (TextView) convertView.findViewById(R.id.cashin_row_title);
            holder.subtitle = (TextView) convertView.findViewById(R.id.cashin_row_subtitle);
            holder.arrow = (ImageView) convertView.findViewById(R.id.cashin_row_arrow);

            holder.title.setTypeface(CustomFonts.customContentRegular(this.context));
            holder.subtitle.setTypeface(CustomFonts.customContentRegular(this.context));

            holder.imageView.setColorFilter(context.getResources().getColor(R.color.blue));
            holder.arrow.setColorFilter(context.getResources().getColor(R.color.blue));

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        FLButton button = this.buttons.get(i);

        int id = context.getResources().getIdentifier(button.defaultImg, "drawable", context.getPackageName());

        if (id != 0)
            holder.imageView.setImageDrawable(context.getResources().getDrawable(id));

        if (button.imgUrl != null && !button.imgUrl.isEmpty())
            ImageLoader.getInstance().displayImage(button.imgUrl, holder.imageView);

        holder.title.setText(button.title);
        holder.subtitle.setText(button.subtitle);

        return convertView;
    }

    class ViewHolder {
        ImageView imageView;
        TextView title;
        TextView subtitle;
        ImageView arrow;
    }
}
