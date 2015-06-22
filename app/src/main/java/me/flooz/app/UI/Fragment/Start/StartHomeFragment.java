package me.flooz.app.UI.Fragment.Start;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.viewpagerindicator.CirclePageIndicator;

import java.util.Timer;
import java.util.TimerTask;

import me.flooz.app.Adapter.StartSliderAdapter;
import me.flooz.app.Model.FLError;
import me.flooz.app.Network.FloozHttpResponseHandler;
import me.flooz.app.Network.FloozRestClient;
import me.flooz.app.R;
import me.flooz.app.UI.View.LoopPager.LoopViewPager;
import me.flooz.app.Utils.CustomFonts;

/**
 * Created by Flooz on 6/10/15.
 */
public class StartHomeFragment extends StartBaseFragment {

    private LoopViewPager pagerView;
    private CirclePageIndicator pageIndicator;
    private StartSliderAdapter pagerAdapter;

    private Handler handler = new Handler();

    private Runnable runnable = () -> {
        pagerView.setCurrentItem(pagerView.getCurrentItem() + 1, true);
        handler.postDelayed(this.runnable, 4000);
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.start_home_fragment, null);

        TextView title = (TextView) view.findViewById(R.id.start_home_title);
        this.pagerView = (LoopViewPager) view.findViewById(R.id.start_home_pager);
        this.pageIndicator = (CirclePageIndicator) view.findViewById(R.id.start_home_page_indicator);
        Button loginButton = (Button) view.findViewById(R.id.start_home_login);
        Button signupButton = (Button) view.findViewById(R.id.start_home_signup);

        title.setTypeface(CustomFonts.customTitleLight(inflater.getContext()));

        this.handler = new Handler(Looper.getMainLooper());

        this.pageIndicator.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                handler.removeCallbacks(runnable);
                handler.postDelayed(runnable, 4000);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        FloozRestClient.getInstance().textObjectFromApi(new FloozHttpResponseHandler() {
            @Override
            public void success(Object response) {
                pagerAdapter = new StartSliderAdapter(inflater.getContext(), FloozRestClient.getInstance().currentTexts.slider.slides);
                pagerView.setAdapter(pagerAdapter);
                pagerView.setCurrentItem(0);

                pageIndicator.setViewPager(pagerView);
                pageIndicator.setFillColor(inflater.getContext().getResources().getColor(android.R.color.white));
                pageIndicator.setStrokeColor(inflater.getContext().getResources().getColor(android.R.color.white));

                handler.removeCallbacks(runnable);
                handler.postDelayed(runnable, 4000);
            }

            @Override
            public void failure(int statusCode, FLError error) {

            }
        });

        loginButton.setOnClickListener(v -> parentActivity.changeCurrentPage(new StartLoginFragment(), android.R.animator.fade_in, android.R.animator.fade_out, true));
        signupButton.setOnClickListener(v -> parentActivity.changeCurrentPage(new StartSignupFragment(), android.R.animator.fade_in, android.R.animator.fade_out, true));

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        if (this.pagerView.getAdapter() != null)
            this.handler.postDelayed(runnable, 4000);
    }

    @Override
    public void onPause() {
        super.onPause();

        handler.removeCallbacks(runnable);
    }
}
