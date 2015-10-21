package me.flooz.app.UI.Fragment.Home.TabFragments;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
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
import me.flooz.app.Model.FLUser;
import me.flooz.app.Network.FloozHttpResponseHandler;
import me.flooz.app.Network.FloozRestClient;
import me.flooz.app.R;
import me.flooz.app.UI.Activity.HomeActivity;
import me.flooz.app.UI.Fragment.Home.SearchFragment;
import me.flooz.app.UI.View.RadioButtonCenter;
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

    public PullRefreshLayout refreshContainer;
    public TimelineFragmentDelegate delegate;
    public TimelineListView timelineListView;
    private TimelineListAdapter timelineAdapter;
    private ImageView backgroundImage;
    private RadioButtonCenter settingsFilterAll;
    private RadioButtonCenter settingsFilterFriends;
    private RadioButtonCenter settingsFilterSelf;
    private Drawable scopeAll;
    private Drawable scopeFriend;
    private Drawable scopePrivate;
    private ImageView searchButton;

    public List<FLTransaction> transactions = null;
    public FLTransaction.TransactionScope currentFilter;

    private String nextPageUrl;

    private boolean starter = true;

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
        if (FloozRestClient.getInstance().appSettings.contains("defaultScope")) {
            this.currentFilter = FLTransaction.transactionParamsToScope(FloozRestClient.getInstance().appSettings.getString("defaultScope", ""));
        } else {
            this.currentFilter = FLTransaction.TransactionScope.TransactionScopeAll;
            FloozRestClient.getInstance().appSettings.edit().putString("defaultScope", FLTransaction.transactionScopeToParams(this.currentFilter)).apply();
        }
    }

    @SuppressLint("ValidFragment")
    public TimelineFragment(FLTransaction.TransactionScope scope) {
        this.currentFilter = scope;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.timeline_fragment, null);

        this.searchButton = (ImageView) view.findViewById(R.id.header_item_right);
        this.searchButton.setColorFilter(getResources().getColor(R.color.blue));

        this.settingsFilterAll = (RadioButtonCenter) view.findViewById(R.id.settings_segment_all);
        this.settingsFilterFriends = (RadioButtonCenter) view.findViewById(R.id.settings_segment_friends);
        this.settingsFilterSelf = (RadioButtonCenter) view.findViewById(R.id.settings_segment_private);
        prepareBitmaps();

        this.refreshContainer = (PullRefreshLayout) view.findViewById(R.id.timeline_refresh_container);
        this.timelineListView = (TimelineListView) view.findViewById(R.id.timeline_list);
        ((TextView)this.timelineListView.getScrollBarPanel().findViewById(R.id.timeline_scrollbar_panel_when)).setTypeface(CustomFonts.customContentRegular(inflater.getContext()));
        this.backgroundImage  = (ImageView) view.findViewById(R.id.timeline_background);

        switch (this.currentFilter) {
            case TransactionScopeFriend:
                resetFilterColor();
                this.scopeFriend.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN);
                this.settingsFilterFriends.setChecked(true);
                break;
            case TransactionScopePrivate:
                resetFilterColor();
                this.scopePrivate.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN);
                this.settingsFilterSelf.setChecked(true);
                break;
            default:
                resetFilterColor();
                this.scopeAll.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN);
                this.settingsFilterAll.setChecked(true);
                break;
        }

        this.refreshContainer.setOnRefreshListener(new PullRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshTransactions();
            }
        });

        this.settingsFilterAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filterChanged(FLTransaction.TransactionScope.TransactionScopeAll);
                resetFilterColor();
                scopeAll.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN);
            }
        });

        this.settingsFilterFriends.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filterChanged(FLTransaction.TransactionScope.TransactionScopeFriend);
                resetFilterColor();
                scopeFriend.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN);
            }
        });

        this.settingsFilterSelf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filterChanged(FLTransaction.TransactionScope.TransactionScopePrivate);
                resetFilterColor();
                scopePrivate.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN);
            }
        });

        this.searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tabBarActivity.pushFragmentInCurrentTab(new SearchFragment());
            }
        });

        this.timelineListView.setOnTimelineListViewListener(new TimelineListView.OnTimelineListViewListener() {
            @Override
            public void onPositionChanged(TimelineListView listView, int position, View scrollBarPanel) {
                ((ImageView) scrollBarPanel.findViewById(R.id.timeline_scrollbar_panel_scope)).setImageDrawable(FLTransaction.transactionScopeToImage(transactions.get(position).scope));
                ((ImageView) scrollBarPanel.findViewById(R.id.timeline_scrollbar_panel_scope)).setColorFilter(tabBarActivity.getResources().getColor(android.R.color.white));
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

        if (starter) {
            starter = !starter;
            this.refreshContainer.setRefreshing(true);
            this.refreshTransactions();
        }

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        LocalBroadcastManager.getInstance(FloozApplication.getAppContext()).registerReceiver(reloadTimelineReceiver,
                CustomNotificationIntents.filterReloadTimeline());

        LocalBroadcastManager.getInstance(FloozApplication.getAppContext()).registerReceiver(reloadUserReceiver,
                CustomNotificationIntents.filterReloadCurrentUser());

        updateBalance();

        if (transactions.size() != 0) {
            backgroundImage.setVisibility(View.GONE);
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        LocalBroadcastManager.getInstance(FloozApplication.getAppContext()).unregisterReceiver(reloadTimelineReceiver);
        LocalBroadcastManager.getInstance(FloozApplication.getAppContext()).unregisterReceiver(reloadUserReceiver);
    }

    @Override
    public void filterChanged(FLTransaction.TransactionScope filter) {
        if (filter != this.currentFilter) {
            this.currentFilter = filter;
            FloozRestClient.getInstance().appSettings.edit().putString("defaultScope", FLTransaction.transactionScopeToParams(this.currentFilter)).apply();
            this.transactions.clear();
            this.timelineAdapter.notifyDataSetChanged();
            this.refreshContainer.setRefreshing(true);
            refreshTransactions();
        }
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

    @Override
    public void ListItemUserClick(FLUser user) {
        FloozApplication.getInstance().showUserProfile(user);
    }

    private void resetFilterColor() {
        this.scopeAll.setColorFilter(getResources().getColor(R.color.blue), PorterDuff.Mode.SRC_IN);
        this.scopeFriend.setColorFilter(getResources().getColor(R.color.blue), PorterDuff.Mode.SRC_IN);
        this.scopePrivate.setColorFilter(getResources().getColor(R.color.blue), PorterDuff.Mode.SRC_IN);
    }

    private void prepareBitmaps() {
        int imgWidth = 55;
        int imgHeight = 55;

        Bitmap scopeAllBitmap = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.scope_public), imgWidth, imgHeight, true);
        Bitmap scopeFriendBitmap = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.scope_friend),imgWidth, imgHeight, true);
        Bitmap scopePrivateBitmap = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.scope_private), imgWidth, imgHeight, true);

        this.scopeAll = new BitmapDrawable(getResources(), scopeAllBitmap);
        this.scopeFriend = new BitmapDrawable(getResources(), scopeFriendBitmap);
        this.scopePrivate = new BitmapDrawable(getResources(), scopePrivateBitmap);
        resetFilterColor();

//        this.settingsFilterAll.setCompoundDrawablesWithIntrinsicBounds(null, scopeAll, null, null);
//        this.settingsFilterFriends.setCompoundDrawablesWithIntrinsicBounds(null, scopeFriend, null, null);
//        this.settingsFilterSelf.setCompoundDrawablesWithIntrinsicBounds(null, scopePrivate, null, null);

        this.settingsFilterAll.setButtonDrawable(scopeAll);
        this.settingsFilterFriends.setButtonDrawable(scopeFriend);
        this.settingsFilterSelf.setButtonDrawable(scopePrivate);
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

                if (nextPageUrl == null || nextPageUrl.isEmpty())
                    timelineAdapter.hasNextURL = false;
                else
                    timelineAdapter.hasNextURL = true;

                timelineAdapter.notifyDataSetChanged();
            }

            @Override
            public void failure(int statusCode, FLError error) {

            }
        });
    }

    public void updateBalance() {
        float amount = FloozRestClient.getInstance().currentUser.amount.floatValue();
//        headerBalanceIndicator.setText(FLHelper.trimTrailingZeros(String.format("%.2f", amount)) + " €");
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

                    if (nextPageUrl == null || nextPageUrl.isEmpty())
                        timelineAdapter.hasNextURL = false;
                    else
                        timelineAdapter.hasNextURL = true;

                    timelineAdapter.notifyDataSetChanged();

                    refreshContainer.setRefreshing(false);

                    if (transactions.size() == 0) {
                            Handler _timer = new Handler();
                            _timer.postDelayed(new Runnable() {
                                @Override
                                public void run() {
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
                                }
                            }, 500);
                    } else {
                        backgroundImage.setVisibility(View.GONE);
                    }
                } else {
                    refreshContainer.setRefreshing(false);
                }
            }

            @Override
            public void failure(int statusCode, FLError error) {
                refreshContainer.setRefreshing(false);
            }
        });
    }
}
