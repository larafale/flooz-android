package flooz.android.com.flooz.UI.Fragment.Home;

/**
 * Created by Flooz on 9/2/14.
 */

import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.viewpagerindicator.CirclePageIndicator;

import flooz.android.com.flooz.Adapter.TimelinePagerAdapter;
import flooz.android.com.flooz.App.FloozApplication;
import flooz.android.com.flooz.Model.FLTransaction;
import flooz.android.com.flooz.Network.FloozRestClient;
import flooz.android.com.flooz.R;
import flooz.android.com.flooz.UI.Activity.HomeActivity;
import flooz.android.com.flooz.UI.Fragment.Camera.ImageViewerFragment;
import flooz.android.com.flooz.UI.Fragment.Home.TimelineFragment;
import flooz.android.com.flooz.UI.Fragment.Home.TransactionCardFragment;
import flooz.android.com.flooz.UI.View.HeaderPagerView;
import flooz.android.com.flooz.Utils.CustomFonts;
import flooz.android.com.flooz.Utils.CustomNotificationIntents;

public class FloozFragment extends Fragment implements TimelineFragment.TimelineFragmentDelegate
{
    public HomeActivity parentActivity;
    private HeaderPagerView headerPagerView;
    private CirclePageIndicator pageIndicator;

    private ImageView headerProfileButton;
    private TextView headerProfileButtonNotifs;

    private ImageView newTransactionButton;
    private ViewPager timelineViewPager = null;
    private TimelinePagerAdapter timelineViewPagerAdapter = null;

    private BroadcastReceiver reloadNotificationsReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (FloozRestClient.getInstance().notificationsManager.nbUnreadNotifications > 0) {
                headerProfileButtonNotifs.setVisibility(View.VISIBLE);
                headerProfileButtonNotifs.setText(String.valueOf(FloozRestClient.getInstance().notificationsManager.nbUnreadNotifications));
            }
            else
                headerProfileButtonNotifs.setVisibility(View.INVISIBLE);
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        LocalBroadcastManager.getInstance(FloozApplication.getAppContext()).registerReceiver(reloadNotificationsReceiver,
                CustomNotificationIntents.filterReloadNotifications());
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.flooz_fragment, null);

        this.headerPagerView = (HeaderPagerView) view.findViewById(R.id.timeline_scroll_header);

        this.pageIndicator = (CirclePageIndicator) view.findViewById(R.id.pagerindicator);

        view.findViewById(R.id.timeline_header).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (timelineViewPager.getCurrentItem() == 0)
                    timelineViewPagerAdapter.homeTimeline.timelineListView.getRefreshableView().smoothScrollToPosition(0);
                else
                    timelineViewPagerAdapter.publicTimeline.timelineListView.getRefreshableView().smoothScrollToPosition(0);
            }
        });

        if (this.timelineViewPagerAdapter == null)
            this.timelineViewPagerAdapter = new TimelinePagerAdapter(getChildFragmentManager());
        this.timelineViewPagerAdapter.homeTimeline.delegate = this;
        this.timelineViewPagerAdapter.publicTimeline.delegate = this;

        this.timelineViewPager = (ViewPager) view.findViewById(R.id.timeline_pager);
        this.timelineViewPager.setAdapter(this.timelineViewPagerAdapter);
        this.timelineViewPager.setCurrentItem(0);

        this.pageIndicator.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                int width = positionOffsetPixels + timelineViewPager.getWidth() * position;
                headerPagerView.setScrollX(width / 2);
            }

            @Override
            public void onPageSelected(int position) {
                if (position == 0)
                    timelineViewPagerAdapter.homeTimeline.refreshTransactions();
                else
                    timelineViewPagerAdapter.publicTimeline.refreshTransactions();
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        this.pageIndicator.setViewPager(this.timelineViewPager);
        this.pageIndicator.setFillColor(inflater.getContext().getResources().getColor(R.color.blue));
        this.pageIndicator.setStrokeColor(inflater.getContext().getResources().getColor(R.color.blue));

        this.headerProfileButton = (ImageView)view.findViewById(R.id.profile_header_button);

        this.headerProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FloozApplication.performLocalNotification(CustomNotificationIntents.showSlidingLeftMenu());
            }
        });

        this.headerProfileButtonNotifs = (TextView)view.findViewById(R.id.profile_header_button_notif);
        this.headerProfileButtonNotifs.setTypeface(CustomFonts.customTitleLight(inflater.getContext()));

        if (FloozRestClient.getInstance().notificationsManager.nbUnreadNotifications > 0) {
            this.headerProfileButtonNotifs.setVisibility(View.VISIBLE);
            this.headerProfileButtonNotifs.setText(String.valueOf(FloozRestClient.getInstance().notificationsManager.nbUnreadNotifications));
        }
        else
            this.headerProfileButtonNotifs.setVisibility(View.INVISIBLE);

        ImageView friendsButton = (ImageView)view.findViewById(R.id.friends_header_button);

        friendsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FloozApplication.performLocalNotification(CustomNotificationIntents.showSlidingRightMenu());
            }
        });

        this.newTransactionButton = (ImageView) view.findViewById(R.id.new_transac_button);
        this.newTransactionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (parentActivity != null)
                    parentActivity.pushMainFragment("create", R.animator.slide_up, android.R.animator.fade_out);
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);

    }

    @Override
    public void onItemSelected(FLTransaction transac) {
        ((TransactionCardFragment)this.parentActivity.contentFragments.get("card")).setTransaction(transac);
        ((TransactionCardFragment)this.parentActivity.contentFragments.get("card")).show(getChildFragmentManager(), "");

    }

    @Override
    public void onItemCommentSelected(FLTransaction transac) {
        ((TransactionCardFragment)this.parentActivity.contentFragments.get("card")).setTransaction(transac);
        ((TransactionCardFragment)this.parentActivity.contentFragments.get("card")).insertComment = true;
        ((TransactionCardFragment)this.parentActivity.contentFragments.get("card")).show(getChildFragmentManager(), "");
    }

    @Override
    public void onItemImageSelected(String imgUrl) {
        ((ImageViewerFragment)this.parentActivity.contentFragments.get("img")).setImage(imgUrl);
        this.parentActivity.pushMainFragment("img", R.animator.slide_up, android.R.animator.fade_out);
    }
}