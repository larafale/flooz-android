package me.flooz.app.UI.Fragment.Home;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.baoyz.widget.PullRefreshLayout;

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
import me.flooz.app.Utils.CustomFonts;
import me.flooz.app.Utils.CustomNotificationIntents;

/**
 * Created by Flooz on 9/23/14.
 */
public class TimelineFragment extends Fragment implements TimelineListAdapter.TimelineListRowDelegate {

    public interface TimelineFragmentDelegate {
        void onItemSelected(FLTransaction transac);
        void onItemCommentSelected(FLTransaction transac);
        void onItemImageSelected(String imgUrl);
    }

    public PullRefreshLayout refreshContainer;
    public TimelineFragmentDelegate delegate;
    public TimelineListView timelineListView;
    private TimelineListAdapter timelineAdapter;
    private ImageView backgroundImage;

    public List<FLTransaction> transactions = null;
    public FLTransaction.TransactionScope currentScope;

    private String nextPageUrl;

    private BroadcastReceiver reloadTimelineReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            refreshTransactions();
        }
    };

    public TimelineFragment() {
        this.currentScope = FLTransaction.TransactionScope.TransactionScopePublic;
    }

    @SuppressLint("ValidFragment")
    public TimelineFragment(FLTransaction.TransactionScope scope) {
        this.currentScope = scope;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.timeline_fragment, null);

        this.refreshContainer = (PullRefreshLayout) view.findViewById(R.id.timeline_refresh_container);
        this.timelineListView = (TimelineListView) view.findViewById(R.id.timeline_list);
        ((TextView)this.timelineListView.getScrollBarPanel().findViewById(R.id.timeline_scrollbar_panel_when)).setTypeface(CustomFonts.customContentRegular(inflater.getContext()));
        this.backgroundImage  = (ImageView) view.findViewById(R.id.timeline_background);

        this.refreshContainer.setOnRefreshListener(TimelineFragment.this::refreshTransactions);

        this.timelineListView.setOnTimelineListViewListener(new TimelineListView.OnTimelineListViewListener() {
            @Override
            public void onPositionChanged(TimelineListView listView, int position, View scrollBarPanel) {
                ((ImageView)scrollBarPanel.findViewById(R.id.timeline_scrollbar_panel_scope)).setImageDrawable(FLTransaction.transactionScopeToImage(transactions.get(position).scope));
                ((TextView)scrollBarPanel.findViewById(R.id.timeline_scrollbar_panel_when)).setText(transactions.get(position).when);
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

        if (currentScope == FLTransaction.TransactionScope.TransactionScopeFriend) {
            backgroundImage.setImageResource(R.drawable.empty_tl_friend);
            backgroundImage.setVisibility(View.VISIBLE);
        } else if (currentScope == FLTransaction.TransactionScope.TransactionScopePrivate) {
            backgroundImage.setImageResource(R.drawable.empty_tl_private);
            backgroundImage.setVisibility(View.VISIBLE);
        }

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        this.refreshTransactions();

        LocalBroadcastManager.getInstance(FloozApplication.getAppContext()).registerReceiver(reloadTimelineReceiver,
                CustomNotificationIntents.filterReloadTimeline());
    }

    @Override
    public void onPause() {
        super.onPause();

        LocalBroadcastManager.getInstance(FloozApplication.getAppContext()).unregisterReceiver(reloadTimelineReceiver);
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

    public void refreshTransactions() {
        FloozRestClient.getInstance().timeline(this.currentScope, new FloozHttpResponseHandler() {
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
                                if (currentScope == FLTransaction.TransactionScope.TransactionScopeFriend) {
                                    backgroundImage.setImageResource(R.drawable.empty_tl_friend);
                                    backgroundImage.setVisibility(View.VISIBLE);
                                } else if (currentScope == FLTransaction.TransactionScope.TransactionScopePrivate) {
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
