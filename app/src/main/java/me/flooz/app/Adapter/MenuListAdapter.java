package me.flooz.app.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

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
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View rowView = inflater.inflate(R.layout.account_menu_row, parent, false);

        ImageView img = (ImageView)rowView.findViewById(R.id.menu_row_img);
        TextView text = (TextView)rowView.findViewById(R.id.menu_row_label);
        TextView notifsLabel = (TextView)rowView.findViewById(R.id.menu_row_notification);

        text.setTypeface(CustomFonts.customTitleExtraLight(inflater.getContext()));
        notifsLabel.setTypeface(CustomFonts.customTitleLight(inflater.getContext()));

        MenuItem item = this.items.get(position);

        img.setImageDrawable(item.image);
        text.setText(item.name);
        if (item.nbNotification > 0)
            notifsLabel.setText("" + item.nbNotification);
        else {
            notifsLabel.setVisibility(View.INVISIBLE);
        }

        return rowView;
    }

}
