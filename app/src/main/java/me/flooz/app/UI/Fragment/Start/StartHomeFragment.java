package me.flooz.app.UI.Fragment.Start;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.viewpagerindicator.CirclePageIndicator;

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

        FloozRestClient.getInstance().textObjectFromApi(new FloozHttpResponseHandler() {
            @Override
            public void success(Object response) {
                pagerAdapter = new StartSliderAdapter(inflater.getContext(), FloozRestClient.getInstance().currentTexts.slider.slides);
                pagerView.setAdapter(pagerAdapter);
                pagerView.setCurrentItem(0);

                pageIndicator.setViewPager(pagerView);
                pageIndicator.setFillColor(inflater.getContext().getResources().getColor(android.R.color.white));
                pageIndicator.setStrokeColor(inflater.getContext().getResources().getColor(android.R.color.white));
            }

            @Override
            public void failure(int statusCode, FLError error) {

            }
        });

        loginButton.setOnClickListener(v -> parentActivity.changeCurrentPage(new StartLoginFragment(), android.R.animator.fade_in, android.R.animator.fade_out, true));
        signupButton.setOnClickListener(v -> parentActivity.changeCurrentPage(new StartSignupFragment(), android.R.animator.fade_in, android.R.animator.fade_out, true));

        return view;
    }
}
