package me.flooz.app.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.makeramen.roundedimageview.RoundedImageView;

import java.util.ArrayList;
import java.util.List;

import me.flooz.app.Model.FLCountry;
import me.flooz.app.Model.FLError;
import me.flooz.app.Model.FLTexts;
import me.flooz.app.Model.FLUser;
import me.flooz.app.Network.FloozHttpResponseHandler;
import me.flooz.app.Network.FloozRestClient;

/**
 * Created by Flooz on 11/24/15.
 */
public class CountryListAdapter extends BaseAdapter {

    private Context context;
    private LayoutInflater inflater;

    public FLCountry currentCountry;
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
        return null;
    }

    class ViewHolder {
        TextView name;
        TextView username;
        RoundedImageView pic;
        ImageView flag;
    }
}
