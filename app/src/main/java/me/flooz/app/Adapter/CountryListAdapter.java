package me.flooz.app.Adapter;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.makeramen.roundedimageview.RoundedImageView;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
import java.util.List;

import me.flooz.app.Model.FLCountry;
import me.flooz.app.Model.FLError;
import me.flooz.app.Model.FLTexts;
import me.flooz.app.Model.FLUser;
import me.flooz.app.Network.FloozHttpResponseHandler;
import me.flooz.app.Network.FloozRestClient;
import me.flooz.app.R;
import me.flooz.app.Utils.CustomFonts;

/**
 * Created by Flooz on 11/24/15.
 */
public class CountryListAdapter extends BaseAdapter {

    private Context context;
    private LayoutInflater inflater;

    private List<FLCountry> countries;

    public CountryListAdapter(Context ctx) {
        this.inflater = LayoutInflater.from(ctx);
        this.context = ctx;

        FLTexts texts = FloozRestClient.getInstance().currentTexts;

        if (texts == null) {
            FloozRestClient.getInstance().textObjectFromApi(new FloozHttpResponseHandler() {
                @Override
                public void success(Object response) {
                    FLTexts texts = FloozRestClient.getInstance().currentTexts;
                    countries = texts.avalaibleCountries;
                    notifyDataSetChanged();
                }

                @Override
                public void failure(int statusCode, FLError error) {

                }
            });
        } else {
            countries = texts.avalaibleCountries;
            notifyDataSetChanged();
        }
    }

    @Override
    public int getCount() {
        return countries.size();
    }

    @Override
    public FLCountry getItem(int position) {
        return countries.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;

        if (convertView == null) {
            holder = new ViewHolder();
            convertView = inflater.inflate(R.layout.country_picker_row, parent, false);
            holder.text = (TextView) convertView.findViewById(R.id.country_picker_row_text);
            holder.flag = (ImageView) convertView.findViewById(R.id.country_picker_row_flag);

            holder.text.setTypeface(CustomFonts.customTitleExtraLight(this.context), Typeface.BOLD);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        final FLCountry country = this.getItem(position);

        holder.text.setText(country.name + " (" + country.indicatif + ")");
        holder.flag.setImageDrawable(context.getResources().getDrawable(country.imageID));

        return convertView;
    }

    class ViewHolder {
        TextView text;
        ImageView flag;
    }
}
