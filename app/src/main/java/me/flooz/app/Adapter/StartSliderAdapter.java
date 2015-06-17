package me.flooz.app.Adapter;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import me.flooz.app.Model.FLError;
import me.flooz.app.Model.FLSliderPage;
import me.flooz.app.Network.FloozHttpResponseHandler;
import me.flooz.app.Network.FloozRestClient;
import me.flooz.app.R;
import me.flooz.app.Utils.CustomFonts;

/**
 * Created by Flooz on 6/10/15.
 */
public class StartSliderAdapter extends PagerAdapter {

    List<FLSliderPage> views;
    LayoutInflater inflater;

    public StartSliderAdapter(Context ctx, List<FLSliderPage> slides){
        inflater=LayoutInflater.from(ctx);

        views = slides;
    }

    @Override
    public int getCount() {
        return views.size();
    }

    public Object instantiateItem(ViewGroup container, int position) {
        ViewGroup currentView;

        int rootLayout = R.layout.slide_fragment;
        currentView= (ViewGroup) inflater.inflate(rootLayout,container,false);

        ((TextView)currentView.findViewById(R.id.slide_text)).setText(views.get(position).text);
        ((TextView)currentView.findViewById(R.id.slide_text)).setTypeface(CustomFonts.customContentRegular(inflater.getContext()));

        container.addView(currentView);
        return currentView;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View)object);
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return (view == object);
    }
}