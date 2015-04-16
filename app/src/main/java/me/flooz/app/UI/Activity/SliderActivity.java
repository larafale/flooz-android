package me.flooz.app.UI.Activity;

import android.app.ActionBar;
import android.app.Activity;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.TextView;

import com.viewpagerindicator.CirclePageIndicator;

import me.flooz.app.Adapter.SliderPagerAdapter;
import me.flooz.app.App.FloozApplication;
import me.flooz.app.Network.FloozRestClient;
import me.flooz.app.R;
import me.flooz.app.Utils.CustomFonts;
import me.flooz.app.Utils.FLHelper;
import me.flooz.app.Utils.ViewServer;

/**
 * Created by Flooz on 4/7/15.
 */
public class SliderActivity extends Activity {

    public FloozApplication floozApp;

    private ViewPager pagerView;
    private CirclePageIndicator pageIndicator;
    private SliderPagerAdapter pagerAdapter;
    private TextView skipButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        View decorView = getWindow().getDecorView();

        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);

        if (FLHelper.isDebuggable())
            ViewServer.get(this).addWindow(this);

        setContentView(R.layout.slider_activity);
        floozApp = (FloozApplication)this.getApplicationContext();

        this.pagerAdapter = new SliderPagerAdapter(this.getFragmentManager(), FloozRestClient.getInstance().currentTexts.slider.generateFragments());

        this.pagerView = (ViewPager) this.findViewById(R.id.slider_pager);
        this.pageIndicator = (CirclePageIndicator) this.findViewById(R.id.slider_pager_indicator);
        this.skipButton = (TextView) this.findViewById(R.id.slider_skip);

        this.skipButton.setTypeface(CustomFonts.customContentLight(this));

        this.pagerView.setAdapter(this.pagerAdapter);
        this.pagerView.setCurrentItem(0);
        this.pagerView.setOffscreenPageLimit(this.pagerAdapter.getCount());

        this.pageIndicator.setViewPager(this.pagerView);
        this.pageIndicator.setFillColor(this.getResources().getColor(android.R.color.white));
        this.pageIndicator.setStrokeColor(this.getResources().getColor(android.R.color.white));

        this.pageIndicator.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (position == pagerAdapter.getCount() - 1) {
                    pageIndicator.setVisibility(View.GONE);
                    skipButton.setVisibility(View.GONE);
                } else {
                    pageIndicator.setVisibility(View.VISIBLE);
                    skipButton.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        this.skipButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }

    public void dismiss() {
        FloozRestClient.getInstance().appSettings.edit().putBoolean("IntroSliders", true).apply();
        finish();
        overridePendingTransition(android.R.anim.fade_in, R.anim.slide_down);
    }

    protected void onResume() {
        super.onResume();

        if (FLHelper.isDebuggable())
            ViewServer.get(this).setFocusedWindow(this);

        floozApp.setCurrentActivity(this);
    }

    protected void onPause() {
        clearReferences();
        super.onPause();
    }

    protected void onDestroy() {
        clearReferences();

        if (FLHelper.isDebuggable())
            ViewServer.get(this).removeWindow(this);

        super.onDestroy();
    }

    private void clearReferences(){
        Activity currActivity = floozApp.getCurrentActivity();
        if (currActivity != null && currActivity.equals(this))
            floozApp.setCurrentActivity(null);
    }

    @Override
    public void onBackPressed() {
        dismiss();
    }
}
