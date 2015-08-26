package me.flooz.app.UI.Fragment.Home.TabFragments;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.baoyz.widget.PullRefreshLayout;
import com.ryanharter.android.tooltips.ToolTip;
import com.ryanharter.android.tooltips.ToolTipLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import me.flooz.app.Adapter.TimelineListAdapter;
import me.flooz.app.App.FloozApplication;
import me.flooz.app.Model.FLError;
import me.flooz.app.Model.FLTransaction;
import me.flooz.app.Network.FloozHttpResponseHandler;
import me.flooz.app.Network.FloozRestClient;
import me.flooz.app.R;
import me.flooz.app.UI.View.TimelineListView;
import me.flooz.app.UI.View.ToolTipFilterView;
import me.flooz.app.UI.View.ToolTipFilterViewDelegate;
import me.flooz.app.Utils.CustomFonts;
import me.flooz.app.Utils.CustomNotificationIntents;
import me.flooz.app.Utils.FLHelper;

/**
 * Created by Flooz on 9/23/14.
 */
public class TimelineFragment extends TabBarFragment implements TimelineListAdapter.TimelineListRowDelegate, ToolTipFilterViewDelegate {

    public interface TimelineFragmentDelegate {
        void onItemSelected(FLTransaction transac);
        void onItemCommentSelected(FLTransaction transac);
        void onItemImageSelected(String imgUrl);
    }

    private ToolTip toolTipFilter;
    private ToolTipLayout tipContainer;
    private ToolTipFilterView tooltipFilterView;

    private TextView headerTitle;
    private TextView headerBalanceIndicator;
    private ImageView headerFilter;

    public PullRefreshLayout refreshContainer;
    public TimelineFragmentDelegate delegate;
    public TimelineListView timelineListView;
    private TimelineListAdapter timelineAdapter;
    private ImageView backgroundImage;

    public List<FLTransaction> transactions = null;
    public FLTransaction.TransactionScope currentFilter;

    private String nextPageUrl;

    private BroadcastReceiver reloadTimelineReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            refreshTransactions();
        }
    };

    private BroadcastReceiver reloadUserReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateBalance();
        }
    };

    public TimelineFragment() {
        this.currentFilter = FLTransaction.TransactionScope.TransactionScopeAll;
    }

    @SuppressLint("ValidFragment")
    public TimelineFragment(FLTransaction.TransactionScope scope) {
        this.currentFilter = scope;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.timeline_fragment, null);

        this.headerTitle = (TextView) view.findViewById(R.id.header_title);
        this.headerBalanceIndicator = (TextView) view.findViewById(R.id.header_item_right);
        this.headerFilter = (ImageView) view.findViewById(R.id.header_item_left);

        this.headerTitle.setTypeface(CustomFonts.customTitleExtraLight(this.tabBarActivity));
        this.headerBalanceIndicator.setTypeface(CustomFonts.customContentLight(this.tabBarActivity));
        this.headerFilter.setColorFilter(this.tabBarActivity.getResources().getColor(R.color.blue));

        this.refreshContainer = (PullRefreshLayout) view.findViewById(R.id.timeline_refresh_container);
        this.timelineListView = (TimelineListView) view.findViewById(R.id.timeline_list);
        ((TextView)this.timelineListView.getScrollBarPanel().findViewById(R.id.timeline_scrollbar_panel_when)).setTypeface(CustomFonts.customContentRegular(inflater.getContext()));
        this.backgroundImage  = (ImageView) view.findViewById(R.id.timeline_background);

        this.tipContainer = (ToolTipLayout) view.findViewById(R.id.timeline_tooltip_container);

        this.refreshContainer.setOnRefreshListener(TimelineFragment.this::refreshTransactions);

        this.timelineListView.setOnTimelineListViewListener(new TimelineListView.OnTimelineListViewListener() {
            @Override
            public void onPositionChanged(TimelineListView listView, int position, View scrollBarPanel) {
                ((ImageView) scrollBarPanel.findViewById(R.id.timeline_scrollbar_panel_scope)).setImageDrawable(FLTransaction.transactionScopeToImage(transactions.get(position).scope));
                ((TextView) scrollBarPanel.findViewById(R.id.timeline_scrollbar_panel_when)).setText(transactions.get(position).when);
            }

            @Override
            public void onShowLastItem() {
                loadNextPage();
            }
        });

        if (this.transactions == null)
            this.transactions = new ArrayList<>(0);

        if (this.timelineAdapter == null) {
            this.timelineAdapter = new TimelineListAdapter(FloozApplication.getAppContext(), this.transactions);
            this.timelineAdapter.delegate = this;
        }

        this.timelineListView.setAdapter(this.timelineAdapter);

        if (currentFilter == FLTransaction.TransactionScope.TransactionScopeFriend) {
            backgroundImage.setImageResource(R.drawable.empty_tl_friend);
            backgroundImage.setVisibility(View.VISIBLE);
        } else if (currentFilter == FLTransaction.TransactionScope.TransactionScopePrivate) {
            backgroundImage.setImageResource(R.drawable.empty_tl_private);
            backgroundImage.setVisibility(View.VISIBLE);
        }

        this.delegate = this.tabBarActivity;

        this.tooltipFilterView = new ToolTipFilterView(tabBarActivity);
        this.tooltipFilterView.changeFilter(this.currentFilter);
        this.tooltipFilterView.delegate = this;

        this.headerFilter.setOnClickListener(v -> {
            if (toolTipFilter != null) {
                filterChanged(currentFilter);
            } else {
                if (tooltipFilterView.view.getParent() != null)
                    ((ViewGroup)tooltipFilterView.view.getParent()).removeView(tooltipFilterView.view);

                toolTipFilter = new ToolTip.Builder(tabBarActivity)
                        .anchor(headerFilter)
                        .gravity(Gravity.BOTTOM)
                        .color(tabBarActivity.getResources().getColor(android.R.color.white))
                        .pointerSize(25)
                        .contentView(tooltipFilterView.view)
                        .dismissOnTouch(false)
                        .build();

                tipContainer.setVisibility(View.VISIBLE);
                tipContainer.addTooltip(toolTipFilter, true);
            }
        });

        this.tipContainer.setOnClickListener(v -> filterChanged(currentFilter));

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        this.refreshTransactions();

        LocalBroadcastManager.getInstance(FloozApplication.getAppContext()).registerReceiver(reloadTimelineReceiver,
                CustomNotificationIntents.filterReloadTimeline());

        LocalBroadcastManager.getInstance(FloozApplication.getAppContext()).registerReceiver(reloadUserReceiver,
                CustomNotificationIntents.filterReloadCurrentUser());

        updateBalance();
    }

    @Override
    public void onPause() {
        super.onPause();

        LocalBroadcastManager.getInstance(FloozApplication.getAppContext()).unregisterReceiver(reloadTimelineReceiver);
        LocalBroadcastManager.getInstance(FloozApplication.getAppContext()).unregisterReceiver(reloadUserReceiver);
    }

    @Override
    public void filterChanged(FLTransaction.TransactionScope filter) {
        this.tipContainer.dismiss(true);
        this.tipContainer.setVisibility(View.GONE);
        this.toolTipFilter = null;
        this.currentFilter = filter;

        refreshTransactions();
    }

    @Override
    public void ListItemClick(FLTransaction transac) {
        if (delegate != null)
            delegate.onItemSelected(transac);
    }

    @Override
    public void ListItemCommentClick(FLTransaction transac) {
        if (delegate != null)
            delegate.onItemCommentSelected(transac);
    }

    @Override
    public void ListItemImageClick(String imgUrl) {
        if (delegate != null)
            delegate.onItemImageSelected(imgUrl);
    }

    private void loadNextPage() {
        if (nextPageUrl == null || nextPageUrl.isEmpty())
            return;

        FloozRestClient.getInstance().timelineNextPage(this.nextPageUrl, new FloozHttpResponseHandler() {
            @Override
            public void success(Object response) {
                @SuppressWarnings("unchecked")
                Map<String, Object> responseMap = (Map<String, Object>) response;

                transactions.addAll((List<FLTransaction>) responseMap.get("transactions"));
                nextPageUrl = (String) responseMap.get("nextUrl");
                timelineAdapter.notifyDataSetChanged();
            }

            @Override
            public void failure(int statusCode, FLError error) {

            }
        });
    }

    public void updateBalance() {
        float amount = FloozRestClient.getInstance().currentUser.amount.floatValue();
        headerBalanceIndicator.setText(FLHelper.trimTrailingZeros(String.format("%.2f", amount)) + " €");
    }

    public void refreshTransactions() {
        FloozRestClient.getInstance().timeline(this.currentFilter, new FloozHttpResponseHandler() {
            @Override
            public void success(Object response) {
                @SuppressWarnings("unchecked")
                Map<String, Object> responseMap = (Map<String, Object>) response;

                if (responseMap.containsKey("transactions") && responseMap.get("transactions") != null && responseMap.get("transactions") instanceof List) {
                    if (transactions != null)
                        transactions.clear();
                    else
                        transactions = new ArrayList<>();

                    transactions.addAll((List<FLTransaction>) responseMap.get("transactions"));
                    nextPageUrl = (String) responseMap.get("nextUrl");

                    if (timelineAdapter == null) {
                        timelineAdapter = new TimelineListAdapter(FloozApplication.getAppContext(), transactions);
                        timelineAdapter.delegate = TimelineFragment.this;
                    }

                    timelineAdapter.notifyDataSetChanged();

                    refreshContainer.setRefreshing(false);

                    if (transactions.size() == 0) {
                        Handler _timer = new Handler();
                        _timer.postDelayed(() -> {
                            if (transactions.size() == 0) {
                                if (currentFilter == FLTransaction.TransactionScope.TransactionScopeFriend) {
                                    backgroundImage.setImageResource(R.drawable.empty_tl_friend);
                                    backgroundImage.setVisibility(View.VISIBLE);
                                } else if (currentFilter == FLTransaction.TransactionScope.TransactionScopePrivate) {
                                    backgroundImage.setImageResource(R.drawable.empty_tl_private);
                                    backgroundImage.setVisibility(View.VISIBLE);
                                }
                            } else {
                                backgroundImage.setVisibility(View.GONE);
                            }
                        }, 500);
                    } else {
                        backgroundImage.setVisibility(View.GONE);
                    }
                } else
                    refreshContainer.setRefreshing(false);
            }

            @Override
            public void failure(int statusCode, FLError error) {
                refreshContainer.setRefreshing(false);
            }
        });
    }
}
