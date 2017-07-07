package me.flooz.app.UI.Fragment.Home.TabFragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.baoyz.widget.PullRefreshLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import me.flooz.app.Adapter.TimelineListAdapter;
import me.flooz.app.App.FloozApplication;
import me.flooz.app.Model.FLError;
import me.flooz.app.Model.FLScope;
import me.flooz.app.Model.FLTexts;
import me.flooz.app.Model.FLTransaction;
import me.flooz.app.Model.FLUser;
import me.flooz.app.Network.FloozHttpResponseHandler;
import me.flooz.app.Network.FloozRestClient;
import me.flooz.app.R;
import me.flooz.app.UI.Activity.SearchActivity;
import me.flooz.app.UI.View.TimelineListView;
import me.flooz.app.UI.View.ToolTipFilterViewDelegate;
import me.flooz.app.Utils.CustomFonts;
import me.flooz.app.Utils.CustomNotificationIntents;

/**
 * Created by Flooz on 9/23/14.
 */
public class TimelineFragment extends TabBarFragment implements TimelineListAdapter.TimelineListRowDelegate, ToolTipFilterViewDelegate {

    public interface TimelineFragmentDelegate {
        void onItemSelected(FLTransaction transac);
        void onItemCommentSelected(FLTransaction transac);
        void onItemImageSelected(String imgUrl);
        void onItemVideoSelected(String videoUrl);
    }

    public PullRefreshLayout refreshContainer;
    public TimelineFragmentDelegate delegate;
    public TimelineListView timelineListView;
    private TimelineListAdapter timelineAdapter;
    private ImageView backgroundImage;
    private ImageView searchButton;
    private ImageView scopeButton;
    private ImageView logoButton;
    private TextView scopeHint;

    public List<FLTransaction> transactions = null;
    public List<FLScope> availableScopes;
    public FLScope currentFilter;

    private String nextPageUrl;

    private boolean starter = true;

    private BroadcastReceiver reloadTimelineReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            refreshTransactions();
        }
    };

    public TimelineFragment() {
        String filterData = FloozRestClient.getInstance().appSettings.getString(FloozRestClient.kFilterData, "");

        if (filterData != null && !filterData.isEmpty()) {
            this.currentFilter = FLScope.scopeFromObject(filterData);
        } else if (FloozRestClient.getInstance().currentTexts != null && FloozRestClient.getInstance().currentTexts.defaultScope != null) {
            this.currentFilter = FloozRestClient.getInstance().currentTexts.defaultScope;
            FloozRestClient.getInstance().appSettings.edit().putString(FloozRestClient.kFilterData, this.currentFilter.keyString).apply();
        } else if (FloozRestClient.getInstance().currentTexts != null &&
                FloozRestClient.getInstance().currentTexts.homeScopes != null &&
                FloozRestClient.getInstance().currentTexts.homeScopes.size() > 0) {
            this.currentFilter = FloozRestClient.getInstance().currentTexts.homeScopes.get(0);
        } else {
            this.currentFilter = FLScope.defaultScopeList().get(0);
            FloozRestClient.getInstance().appSettings.edit().putString(FloozRestClient.kFilterData, this.currentFilter.keyString).apply();
        }
    }

    @SuppressLint("ValidFragment")
    public TimelineFragment(FLScope scope) {
        this.currentFilter = scope;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.timeline_fragment, null);

        this.searchButton = (ImageView) view.findViewById(R.id.header_item_right);
        this.searchButton.setColorFilter(getResources().getColor(R.color.blue));

        this.scopeButton = (ImageView) view.findViewById(R.id.header_item_left);
        this.scopeButton.setColorFilter(getResources().getColor(R.color.blue));

        this.logoButton = (ImageView) view.findViewById(R.id.header_item_middle);
        this.scopeHint = (TextView) view.findViewById(R.id.timeline_scope_hint);

        this.refreshContainer = (PullRefreshLayout) view.findViewById(R.id.timeline_refresh_container);
        this.timelineListView = (TimelineListView) view.findViewById(R.id.timeline_list);
        this.backgroundImage  = (ImageView) view.findViewById(R.id.timeline_background);

        this.logoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final MediaPlayer mp = MediaPlayer.create(tabBarActivity, R.raw.coin);
                mp.start();

                final Dialog dialog = new Dialog(tabBarActivity);

                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView(R.layout.custom_dialog_social);

                TextView title = (TextView) dialog.findViewById(R.id.dialog_social_title);
                title.setTypeface(CustomFonts.customContentRegular(tabBarActivity), Typeface.BOLD);

                ImageView close = (ImageView) dialog.findViewById(R.id.dialog_social_close);

                RelativeLayout fbButton = (RelativeLayout) dialog.findViewById(R.id.dialog_social_fb);
                TextView fbButtonText = (TextView) dialog.findViewById(R.id.dialog_social_fb_text);
                ImageView fbButtonPic = (ImageView) dialog.findViewById(R.id.dialog_social_fb_pic);

                RelativeLayout twitterButton = (RelativeLayout) dialog.findViewById(R.id.dialog_social_twitter);
                TextView twitterButtonText = (TextView) dialog.findViewById(R.id.dialog_social_twitter_text);
                ImageView twitterButtonPic = (ImageView) dialog.findViewById(R.id.dialog_social_twitter_pic);

                RelativeLayout storeButton = (RelativeLayout) dialog.findViewById(R.id.dialog_social_store);
                TextView storeButtonText = (TextView) dialog.findViewById(R.id.dialog_social_store_text);
                ImageView storeButtonPic = (ImageView) dialog.findViewById(R.id.dialog_social_store_pic);

                close.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });

                fbButtonText.setTypeface(CustomFonts.customContentRegular(tabBarActivity));
                twitterButtonText.setTypeface(CustomFonts.customContentRegular(tabBarActivity));
                storeButtonText.setTypeface(CustomFonts.customContentRegular(tabBarActivity));

                fbButtonPic.setColorFilter(tabBarActivity.getResources().getColor(R.color.social_fb));
                twitterButtonPic.setColorFilter(tabBarActivity.getResources().getColor(R.color.social_twitter));
                storeButtonPic.setColorFilter(tabBarActivity.getResources().getColor(R.color.social_store));

                fbButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            tabBarActivity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("fb://page/1462571777306570")));
                        } catch (android.content.ActivityNotFoundException anfe) {
                            tabBarActivity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.facebook.com/floozme")));
                        }
                    }
                });

                twitterButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            tabBarActivity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("twitter:///user?screen_name=floozme")));
                        } catch (android.content.ActivityNotFoundException anfe) {
                            tabBarActivity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://twitter.com/floozme")));
                        }
                    }
                });

                storeButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final String appPackageName = tabBarActivity.getPackageName();
                        try {
                            tabBarActivity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                        } catch (android.content.ActivityNotFoundException anfe) {
                            tabBarActivity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
                        }
                    }
                });

                dialog.setCanceledOnTouchOutside(true);
                dialog.show();
            }
        });

        this.scopeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (int i = 0; i < availableScopes.size(); ++i) {
                    FLScope scope = availableScopes.get(i);

                    if (currentFilter.keyString.equals(scope.keyString)) {
                        int nextIndex = i + 1;

                        if (nextIndex == availableScopes.size())
                            nextIndex = 0;

                        currentFilter = availableScopes.get(nextIndex);
                        break;
                    }
                }

                filterChanged(currentFilter);
                checkScopeAvailability();
                currentFilter.displayImage(scopeButton);
                scopeHint.setText(currentFilter.desc);

                Animation fadeIn = new AlphaAnimation(0, 1);
                fadeIn.setInterpolator(new DecelerateInterpolator()); //add this
                fadeIn.setDuration(500);

                Animation fadeOut = new AlphaAnimation(1, 0);
                fadeOut.setInterpolator(new AccelerateInterpolator()); //and this
                fadeOut.setStartOffset(2000);
                fadeOut.setDuration(500);

                AnimationSet animation = new AnimationSet(false); //change to false
                animation.addAnimation(fadeIn);
                animation.addAnimation(fadeOut);
                scopeHint.setAnimation(animation);
            }
        });

        this.refreshContainer.setOnRefreshListener(new PullRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshTransactions();
            }
        });

        this.searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(tabBarActivity, SearchActivity.class);
                tabBarActivity.startActivity(intent);
                tabBarActivity.overridePendingTransition(R.anim.slide_up, android.R.anim.fade_out);
            }
        });

        this.timelineListView.setOnTimelineListViewListener(new TimelineListView.OnTimelineListViewListener() {
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

//        if (currentFilter == FLTransaction.TransactionScope.TransactionScopeFriend) {
//            backgroundImage.setImageResource(R.drawable.empty_tl_friend);
//            backgroundImage.setVisibility(View.VISIBLE);
//        } else if (currentFilter == FLTransaction.TransactionScope.TransactionScopePrivate) {
//            backgroundImage.setImageResource(R.drawable.empty_tl_private);
//            backgroundImage.setVisibility(View.VISIBLE);
//        }

        this.delegate = this.tabBarActivity;

        this.checkScopeAvailability();

        if (starter) {
            starter = !starter;
            this.refreshContainer.setRefreshing(true);
            this.refreshTransactions();
        }

        LocalBroadcastManager.getInstance(FloozApplication.getAppContext()).registerReceiver(reloadTimelineReceiver,
                CustomNotificationIntents.filterReloadTimeline());

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (transactions.size() != 0) {
            backgroundImage.setVisibility(View.GONE);
        }
    }

    @Override
    public void onPause() {
        super.onPause();

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        LocalBroadcastManager.getInstance(FloozApplication.getAppContext()).unregisterReceiver(reloadTimelineReceiver);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void filterChanged(FLScope filter) {
        this.currentFilter = filter;
        FloozRestClient.getInstance().appSettings.edit().putString(FloozRestClient.kFilterData, this.currentFilter.keyString).apply();
        this.transactions.clear();
        this.timelineAdapter.hasNextURL = false;
        this.timelineAdapter.notifyDataSetChanged();
        this.refreshContainer.setRefreshing(true);
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

    @Override
    public void ListItemVideoClick(String videoUrl) {
        if (delegate != null)
            delegate.onItemVideoSelected(videoUrl);
    }

    @Override
    public void ListItemUserClick(FLUser user) {
        FloozApplication.getInstance().showUserProfile(user);
    }

    @Override
    public void ListItemShareClick(FLTransaction transac) {
        Intent share = new Intent(Intent.ACTION_SEND);
        share.setType("text/plain");

        share.putExtra(Intent.EXTRA_TEXT, "https://www.flooz.me/flooz/" + transac.transactionId);

        tabBarActivity.startActivity(Intent.createChooser(share, tabBarActivity.getResources().getString(R.string.SHARE_FLOOZ)));
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

    public void checkScopeAvailability() {
        FLTexts currentTexts = FloozRestClient.getInstance().currentTexts;
        if (currentTexts != null && currentTexts.homeScopes != null && currentTexts.homeScopes.size() > 0) {
            this.availableScopes = currentTexts.homeScopes;
        } else {
            this.availableScopes = FLScope.defaultScopeList();
        }

        Boolean currentScopeAvailable = false;
        for (FLScope scope: this.availableScopes) {
            if (this.currentFilter != null && this.currentFilter.keyString.equals(scope.keyString)) {
                currentScopeAvailable = true;
                break;
            }
        }

        if (!currentScopeAvailable) {
            this.currentFilter = availableScopes.get(0);
            filterChanged(this.currentFilter);
        }

        if (availableScopes.size() < 2)
            this.scopeButton.setVisibility(View.INVISIBLE);
        else
            this.scopeButton.setVisibility(View.VISIBLE);

        this.currentFilter.displayImage(scopeButton);
        FloozRestClient.getInstance().appSettings.edit().putString(FloozRestClient.kFilterData, this.currentFilter.keyString).apply();
    }

    public void refreshTransactions() {
        FloozRestClient.getInstance().timeline(this.currentFilter, new FloozHttpResponseHandler() {
            @Override
            public void success(Object response) {
                @SuppressWarnings("unchecked")
                Map<String, Object> responseMap = (Map<String, Object>) response;

                if (responseMap.containsKey("transactions") && responseMap.get("transactions") != null
                        && responseMap.get("transactions") instanceof List
                        && responseMap.containsKey("scope")
                        && FLScope.scopeFromObject(responseMap.get("scope")).keyString.equals(currentFilter.keyString)) {
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
//                                if (transactions.size() == 0) {
////                                    if (currentFilter == FLTransaction.TransactionScope.TransactionScopeFriend) {
////                                        backgroundImage.setImageResource(R.drawable.empty_tl_friend);
////                                        backgroundImage.setVisibility(View.VISIBLE);
////                                    } else if (currentFilter == FLTransaction.TransactionScope.TransactionScopePrivate) {
////                                        backgroundImage.setImageResource(R.drawable.empty_tl_private);
////                                        backgroundImage.setVisibility(View.VISIBLE);
////                                    }
//                                } else {
//                                    backgroundImage.setVisibility(View.GONE);
//                                }
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
