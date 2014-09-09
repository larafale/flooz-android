package flooz.android.com.flooz.UI.Activity;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;

import flooz.android.com.flooz.R;
import flooz.android.com.flooz.UI.View.HeaderPagerView;
import flooz.android.com.flooz.Adapter.HeaderPagerAdapter;
import flooz.android.com.flooz.Network.FloozRestClient;

public class HomeActivity extends Activity implements HeaderPagerView.Delegate
{
    private HeaderPagerView headerPagerView;
    private ViewPager viewPager;
    private HeaderPagerAdapter pagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        this.headerPagerView = (HeaderPagerView) findViewById(R.id.header);
        this.viewPager = (ViewPager) findViewById(R.id.pager);

        this.pagerAdapter = new HeaderPagerAdapter(getFragmentManager());
        this.viewPager.setAdapter(this.pagerAdapter);
        this.headerPagerView.setDelegate(this);

        this.viewPager.setCurrentItem(1);
        this.viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener()
        {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels)
            {
                int width = positionOffsetPixels + viewPager.getWidth() * position;
                headerPagerView.setScrollX(width / 2);
            }

            @Override
            public void onPageSelected(int position)
            {

            }

            @Override
            public void onPageScrollStateChanged(int state)
            {

            }
        });
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        this.headerPagerView.post(new Runnable() {
            @Override
            public void run() {
                headerPagerView.setInitialScroll();
                headerPagerView.animate().alpha(1).setDuration(500).withLayer();
            }
        });
    }

    @Override
    public void onHeaderButtonClick(int buttonId)
    {
        this.viewPager.setCurrentItem(buttonId, true);
    }

}
