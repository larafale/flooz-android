package flooz.android.com.flooz.UI.Fragment.Home;

/**
 * Created by Flooz on 9/2/14.
 */

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.viewpagerindicator.CirclePageIndicator;

import flooz.android.com.flooz.R;
import flooz.android.com.flooz.Adapter.HeaderPagerAdapter;
import flooz.android.com.flooz.Adapter.TimelinePagerAdapter;
import flooz.android.com.flooz.App.FloozApplication;
import flooz.android.com.flooz.Model.FLTransaction;
import flooz.android.com.flooz.Network.FloozRestClient;
import flooz.android.com.flooz.Utils.CustomFonts;
import flooz.android.com.flooz.Utils.CustomNotificationIntents;

public class FloozFragment extends HomeBaseFragment implements TimelineFragment.TimelineFragmentDelegate
{
    private ViewPager headerPagerView;
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

        this.headerPagerView = (ViewPager) view.findViewById(R.id.timeline_header_pager);
        this.headerPagerView.setAdapter(new HeaderPagerAdapter(getChildFragmentManager()));
        this.headerPagerView.setCurrentItem(0);
        this.headerPagerView.setOffscreenPageLimit(2);

        this.pageIndicator = (CirclePageIndicator) view.findViewById(R.id.pagerindicator);

        view.findViewById(R.id.timeline_header).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (timelineViewPager.getCurrentItem() == 0)
                    timelineViewPagerAdapter.homeTimeline.timelineListView.smoothScrollToPosition(0);
                else if (timelineViewPager.getCurrentItem() == 1)
                    timelineViewPagerAdapter.publicTimeline.timelineListView.smoothScrollToPosition(0);
                else
                    timelineViewPagerAdapter.privateTimeline.timelineListView.smoothScrollToPosition(0);
            }
        });

        if (this.timelineViewPagerAdapter == null) {
            this.timelineViewPagerAdapter = new TimelinePagerAdapter(getChildFragmentManager());
            this.timelineViewPagerAdapter.homeTimeline.delegate = this;
            this.timelineViewPagerAdapter.publicTimeline.delegate = this;
        }

        this.timelineViewPager = (ViewPager) view.findViewById(R.id.timeline_pager);
        this.timelineViewPager.setAdapter(this.timelineViewPagerAdapter);
        this.timelineViewPager.setCurrentItem(0);
        this.timelineViewPager.setOffscreenPageLimit(2);

        this.pageIndicator.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                headerPagerView.setCurrentItem(position, true);

                if (position == 0)
                    timelineViewPagerAdapter.homeTimeline.refreshTransactions();
                else if (position == 1)
                    timelineViewPagerAdapter.publicTimeline.refreshTransactions();
                else
                    timelineViewPagerAdapter.privateTimeline.refreshTransactions();
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        this.pageIndicator.setViewPager(this.timelineViewPager);
        this.pageIndicator.setFillColor(inflater.getContext().getResources().getColor(R.color.blue));
        this.pageIndicator.setStrokeColor(inflater.getContext().getResources().getColor(R.color.blue));

        this.headerProfileButton = (ImageView) view.findViewById(R.id.profile_header_button);

        final int[] doubleClickChecker = {0};
        this.headerProfileButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                doubleClickChecker[0]++;
                Handler handler = new Handler();
                Runnable r = new Runnable() {

                    @Override
                    public void run() {
                        if (doubleClickChecker[0] == 1) {
                            FloozApplication.performLocalNotification(CustomNotificationIntents.showSlidingLeftMenu());
                        }
                        doubleClickChecker[0] = 0;
                    }
                };

                if (doubleClickChecker[0] == 1) {
                    handler.postDelayed(r, 250);
                } else if (doubleClickChecker[0] == 2) {
                    ((NotificationFragment)parentActivity.contentFragments.get("notifications")).showCross = true;
                    parentActivity.pushMainFragment("notifications", R.animator.slide_up, android.R.animator.fade_out);
                    doubleClickChecker[0] = 0;
                }
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
                ((TransactionSelectReceiverFragment)parentActivity.contentFragments.get("select_user")).firstNewFlooz = true;
                ((TransactionSelectReceiverFragment)parentActivity.contentFragments.get("select_user")).showCross = true;
                parentActivity.pushMainFragment("select_user", R.animator.slide_up, android.R.animator.fade_out);
            }
        });

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

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
        this.parentActivity.showTransactionCard(transac);
    }

    @Override
    public void onItemCommentSelected(FLTransaction transac) {
        this.parentActivity.showTransactionCard(transac, true);
    }

    @Override
    public void onItemImageSelected(String imgUrl) {
        this.parentActivity.showImageViewer(imgUrl);
    }

    @Override
    public void onBackPressed() {
        this.parentActivity.finish();
    }
}