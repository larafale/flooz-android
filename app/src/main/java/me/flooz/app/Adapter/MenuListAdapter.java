package me.flooz.app.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.makeramen.roundedimageview.RoundedImageView;

import java.util.List;

import me.flooz.app.R;
import me.flooz.app.Utils.CustomFonts;
import me.flooz.app.Utils.MenuItem;

/**
 * Created by Flooz on 9/23/14.
 */
public class MenuListAdapter extends ArrayAdapter<MenuItem>
{
    private List<MenuItem> items;
    private Context context;

    public MenuListAdapter(Context context, List<MenuItem> values) {
        super(context, R.layout.timeline_row, values);
        this.context = context;
        this.items = values;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;

//        if (convertView == null) {
//            holder = new ViewHolder();
//            convertView = LayoutInflater.from(this.context).inflate(R.layout.account_menu_row, parent, false);
//            holder.img = (ImageView)convertView.findViewById(R.id.menu_row_img);
//            holder.text = (TextView)convertView.findViewById(R.id.menu_row_label);
//            holder.notifsLabel = (TextView)convertView.findViewById(R.id.menu_row_notification);
//
//            holder.text.setTypeface(CustomFonts.customTitleLight(this.context));
//            holder.notifsLabel.setTypeface(CustomFonts.customTitleLight(this.context));
//
//            convertView.setTag(holder);
//        } else {
//            holder = (ViewHolder) convertView.getTag();
//        }
//
//        MenuItem item = this.items.get(position);
//
//        holder.img.setImageDrawable(item.image);
//        holder.img.setColorFilter(this.context.getResources().getColor(android.R.color.white));
//        holder.text.setText(item.name.toUpperCase());
//        if (item.nbNotification > 0) {
//            holder.notifsLabel.setVisibility(View.VISIBLE);
//            holder.notifsLabel.setText(String.format("%d", item.nbNotification));
//        } else {
//            holder.notifsLabel.setVisibility(View.INVISIBLE);
//        }

        return convertView;
    }

    class ViewHolder {
        ImageView img;
        TextView text;
        TextView notifsLabel;
    }
}
