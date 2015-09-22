package me.flooz.app.UI.Activity;

import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.content.Context;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import me.flooz.app.Model.FLError;
import me.flooz.app.Model.FLShareText;
import me.flooz.app.Model.FLTrigger;
import me.flooz.app.Network.FloozHttpResponseHandler;
import me.flooz.app.R;
import me.flooz.app.Model.FLTransaction;
import me.flooz.app.App.FloozApplication;
import me.flooz.app.UI.Fragment.Home.TabFragments.CashoutFragment;
import me.flooz.app.UI.Fragment.Home.TabFragments.NotificationsFragment;
import me.flooz.app.UI.Fragment.Home.TabFragments.ProfileFragment;
import me.flooz.app.UI.Fragment.Home.TabFragments.ShareFragment;
import me.flooz.app.UI.Fragment.Home.TabFragments.TabBarFragment;
import me.flooz.app.UI.Fragment.Home.TabFragments.TimelineFragment;
import me.flooz.app.UI.Tools.ActionSheet;
import me.flooz.app.UI.Tools.ActionSheetItem;
import me.flooz.app.UI.Tools.CustomDialogImageViewer;
import me.flooz.app.UI.Tools.CustomImageViewer;
import me.flooz.app.UI.Tools.CustomToast;
import me.flooz.app.Network.FloozRestClient;
import me.flooz.app.Utils.CustomFonts;
import me.flooz.app.Utils.CustomNotificationIntents;
import me.flooz.app.UI.Fragment.Home.TransactionCardFragment;
import me.flooz.app.Utils.FLHelper;
import me.flooz.app.Utils.ImageHelper;
import me.flooz.app.Utils.SoftKeyboardHandledRelativeLayout;
import me.flooz.app.Utils.ViewServer;
import uk.co.senab.photoview.PhotoViewAttacher;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import org.json.JSONArray;

public class HomeActivity extends Activity implements TimelineFragment.TimelineFragmentDelegate
{
    public enum TabID {
        HOME_TAB,
        NOTIF_TAB,
        SHARE_TAB,
        ACCOUNT_TAB
    }

    private HomeActivity instance;

    private FragmentManager fragmentManager;

    private ArrayList<TabID> tabHistory;
    private ArrayList<TabBarFragment> homeTabHistory;
    private ArrayList<TabBarFragment> notifsTabHistory;
    private ArrayList<TabBarFragment> shareTabHistory;
    private ArrayList<TabBarFragment> accountTabHistory;

    private TabID currentTabID;
    private TabBarFragment currentFragment;
    private ArrayList<TabBarFragment> currentTabHistory;

    private SoftKeyboardHandledRelativeLayout mainView;

    private FrameLayout fragmentContainer;
    private int fragmentContainerTabBarMargin;

    private LinearLayout tabBar;

    private LinearLayout homeTab;
    private LinearLayout notifTab;
    private LinearLayout floozTab;
    private LinearLayout shareTab;
    private LinearLayout accountTab;

    private ImageView homeTabImage;
    private ImageView notifTabImage;
    private ImageView floozTabImage;
    private ImageView shareTabImage;
    private ImageView accountTabImage;

    private TextView homeTabText;
    private TextView notifTabText;
    private TextView shareTabText;
    private TextView accountTabText;

    private TextView homeTabBadge;
    private TextView notifTabBadge;
    private TextView shareTabBadge;
    private TextView accountTabBadge;

    private RelativeLayout imageViewer;
    private ImageView imageViewerClose;
    private ProgressBar imageViewerProgress;
    private ImageView imageViewerImage;
    private PhotoViewAttacher imageViewerAttacher;

    private Handler tabBarVisibilityHandler = new Handler(Looper.getMainLooper());
    private Runnable tabBarShowRunnable = new Runnable() {
        @Override
        public void run() {
            ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) fragmentContainer.getLayoutParams();
            layoutParams.setMargins(layoutParams.leftMargin, layoutParams.topMargin, layoutParams.rightMargin, fragmentContainerTabBarMargin);
            fragmentContainer.setLayoutParams(layoutParams);

            tabBar.setVisibility(View.VISIBLE);
        }
    };

    public FloozApplication floozApp;

    private BroadcastReceiver reloadNotificationsReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            changeTabBadgeValue(TabID.NOTIF_TAB, FloozRestClient.getInstance().notificationsManager.nbUnreadNotifications);
        }
    };

    private BroadcastReceiver reloadUserReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            changeTabBadgeValue(TabID.ACCOUNT_TAB, FloozRestClient.getInstance().currentUser.json.optJSONObject("metrics").optInt("accountMissing"));
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        this.instance = this;

        if (FLHelper.isDebuggable())
            ViewServer.get(this).addWindow(this);

        floozApp = (FloozApplication)this.getApplicationContext();

        this.fragmentManager = this.getFragmentManager();

        this.setContentView(R.layout.activity_home);

        this.mainView = (SoftKeyboardHandledRelativeLayout) this.findViewById(R.id.main_view);
        this.mainView.setOnSoftKeyboardVisibilityChangeListener(new SoftKeyboardHandledRelativeLayout.SoftKeyboardVisibilityChangeListener() {
            @Override
            public void onSoftKeyboardShow() {
                if (FloozApplication.getInstance().getCurrentActivity() instanceof HomeActivity)
                    hideTabBar();
            }

            @Override
            public void onSoftKeyboardHide() {
                if (FloozApplication.getInstance().getCurrentActivity() instanceof HomeActivity)
                    showTabBar();
            }
        });

        this.fragmentContainer = (FrameLayout) this.findViewById(R.id.fragment_container);
        ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) fragmentContainer.getLayoutParams();
        this.fragmentContainerTabBarMargin = layoutParams.bottomMargin;

        this.tabBar = (LinearLayout) this.findViewById(R.id.tab_bar);

        this.homeTab = (LinearLayout) this.findViewById(R.id.home_tab);
        this.notifTab = (LinearLayout) this.findViewById(R.id.notif_tab);
        this.floozTab = (LinearLayout) this.findViewById(R.id.flooz_tab);
        this.shareTab = (LinearLayout) this.findViewById(R.id.share_tab);
        this.accountTab = (LinearLayout) this.findViewById(R.id.account_tab);

        this.homeTabImage = (ImageView) this.findViewById(R.id.home_tab_image);
        this.notifTabImage = (ImageView) this.findViewById(R.id.notif_tab_image);
        this.floozTabImage = (ImageView) this.findViewById(R.id.flooz_tab_image);
        this.shareTabImage = (ImageView) this.findViewById(R.id.share_tab_image);
        this.accountTabImage = (ImageView) this.findViewById(R.id.account_tab_image);

        this.homeTabText = (TextView) this.findViewById(R.id.home_tab_text);
        this.notifTabText = (TextView) this.findViewById(R.id.notif_tab_text);
        this.shareTabText = (TextView) this.findViewById(R.id.share_tab_text);
        this.accountTabText = (TextView) this.findViewById(R.id.account_tab_text);

        this.homeTabBadge = (TextView) this.findViewById(R.id.home_tab_badge);
        this.notifTabBadge = (TextView) this.findViewById(R.id.notif_tab_badge);
        this.shareTabBadge = (TextView) this.findViewById(R.id.share_tab_badge);
        this.accountTabBadge = (TextView) this.findViewById(R.id.account_tab_badge);

        int clearColor = this.getResources().getColor(R.color.placeholder);
        Typeface titleTypeface = CustomFonts.customContentLight(this);
        Typeface badgeTypeface = CustomFonts.customContentRegular(this);

        this.homeTabText.setTypeface(titleTypeface);
        this.notifTabText.setTypeface(titleTypeface);
        this.shareTabText.setTypeface(titleTypeface);
        this.accountTabText.setTypeface(titleTypeface);

        this.homeTabBadge.setTypeface(badgeTypeface);
        this.notifTabBadge.setTypeface(badgeTypeface);
        this.shareTabBadge.setTypeface(badgeTypeface);
        this.accountTabBadge.setTypeface(badgeTypeface);

        this.homeTabText.setTextColor(clearColor);
        this.notifTabText.setTextColor(clearColor);
        this.shareTabText.setTextColor(clearColor);
        this.accountTabText.setTextColor(clearColor);

        this.homeTabImage.setColorFilter(clearColor);
        this.notifTabImage.setColorFilter(clearColor);
        this.shareTabImage.setColorFilter(clearColor);
        this.accountTabImage.setColorFilter(clearColor);

        this.imageViewer = (RelativeLayout) this.findViewById(R.id.main_image_container);
        this.imageViewerClose = (ImageView) this.findViewById(R.id.main_image_close);
        this.imageViewerProgress = (ProgressBar) this.findViewById(R.id.main_image_progress);
        this.imageViewerImage = (ImageView) this.findViewById(R.id.main_image_image);

        this.imageViewerAttacher = new PhotoViewAttacher(this.imageViewerImage);

        this.imageViewer.setClickable(true);

        this.homeTab.setOnClickListener(v -> changeCurrentTab(TabID.HOME_TAB));
        this.notifTab.setOnClickListener(v -> changeCurrentTab(TabID.NOTIF_TAB));
        this.shareTab.setOnClickListener(v -> changeCurrentTab(TabID.SHARE_TAB));
        this.accountTab.setOnClickListener(v -> changeCurrentTab(TabID.ACCOUNT_TAB));

        this.tabHistory = new ArrayList<>();

        TabBarFragment timelineFragment = new TimelineFragment();
        TabBarFragment accountFragment = new ProfileFragment();
        TabBarFragment notifFragment = new NotificationsFragment();
        TabBarFragment shareFragment = new ShareFragment();

        timelineFragment.tabBarActivity = this;
        accountFragment.tabBarActivity = this;
        notifFragment.tabBarActivity = this;
        shareFragment.tabBarActivity = this;

        this.homeTabHistory = new ArrayList<>();
        this.notifsTabHistory = new ArrayList<>();
        this.shareTabHistory = new ArrayList<>();
        this.accountTabHistory = new ArrayList<>();

        this.homeTabHistory.add(timelineFragment);
        this.notifsTabHistory.add(notifFragment);
        this.shareTabHistory.add(shareFragment);
        this.accountTabHistory.add(accountFragment);

        this.floozTabImage.setOnClickListener(v -> {
            if (FloozRestClient.getInstance().currentUser.ux != null
                    && FloozRestClient.getInstance().currentUser.ux.has("homeButton")
                    && FloozRestClient.getInstance().currentUser.ux.optJSONArray("homeButton").length() > 0) {

                JSONArray t = FloozRestClient.getInstance().currentUser.ux.optJSONArray("homeButton");

                for (int i = 0; i < t.length(); i++) {
                    final FLTrigger trigger = new FLTrigger(t.optJSONObject(i));
                    if (trigger.delay.doubleValue() > 0) {
                        Handler handler = new Handler(Looper.getMainLooper());
                        handler.postDelayed(() -> FloozRestClient.getInstance().handleTrigger(trigger), (int) (trigger.delay.doubleValue() * 1000));
                    } else {
                        FloozRestClient.getInstance().handleTrigger(trigger);
                    }
                }
            } else {
                Intent intent = new Intent(instance, NewTransactionActivity.class);
                instance.startActivity(intent);
                instance.overridePendingTransition(R.anim.slide_up, android.R.anim.fade_out);
            }
        });

        this.imageViewerAttacher.setOnLongClickListener(v -> {
            List<ActionSheetItem> items = new ArrayList<>();
            items.add(new ActionSheetItem(instance, R.string.MENU_SAVE_PICTURE, () -> {
                ImageHelper.saveImageOnPhone(instance, ((BitmapDrawable) imageViewerAttacher.getImageView().getDrawable()).getBitmap());
            }));
            ActionSheet.showWithItems(instance, items);

            return false;
        });

        this.imageViewerClose.setOnClickListener(v -> {
            Animation anim = AnimationUtils.loadAnimation(getApplicationContext(), android.R.anim.fade_out);
            anim.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    imageViewer.setVisibility(View.GONE);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            imageViewer.startAnimation(anim);
        });

        this.changeCurrentTab(TabID.HOME_TAB);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        currentFragment.onActivityResult(requestCode, resultCode, data);
    }

    public void onStart() {
        super.onStart();

        if (FloozRestClient.getInstance().currentShareText == null) {
            FloozRestClient.getInstance().getInvitationText(new FloozHttpResponseHandler() {
                @Override
                public void success(Object response) {
                    FLShareText texts = FloozRestClient.getInstance().currentShareText;

                    shareTabText.setText(texts.shareTitle);
                }

                @Override
                public void failure(int statusCode, FLError error) {

                }
            });
        } else {
            FLShareText texts = FloozRestClient.getInstance().currentShareText;

            shareTabText.setText(texts.shareTitle);
        }
    }

    protected void onResume() {
        super.onResume();

        if (FLHelper.isDebuggable())
            ViewServer.get(this).setFocusedWindow(this);

        floozApp.setCurrentActivity(this);

        LocalBroadcastManager.getInstance(FloozApplication.getAppContext()).registerReceiver(reloadNotificationsReceiver,
                CustomNotificationIntents.filterReloadNotifications());

        LocalBroadcastManager.getInstance(FloozApplication.getAppContext()).registerReceiver(reloadUserReceiver,
                CustomNotificationIntents.filterReloadCurrentUser());

        if (FloozApplication.getInstance().pendingTriggers != null) {
            Handler handlerIntent = new Handler(Looper.getMainLooper());
            final boolean b = handlerIntent.postDelayed(() -> {
                FloozRestClient.getInstance().handleTriggerArray(FloozApplication.getInstance().pendingTriggers);
                FloozApplication.getInstance().pendingTriggers = null;
            }, 100);
        }

        if (FloozApplication.getInstance().pendingPopup != null) {
            Handler handlerIntent = new Handler(Looper.getMainLooper());
            final boolean b = handlerIntent.postDelayed(() -> {
                CustomToast.show(this, new FLError(FloozApplication.getInstance().pendingPopup));
                FloozApplication.getInstance().pendingPopup = null;
            }, 100);
        }
    }

    protected void onPause() {
        clearReferences();
        super.onPause();

        LocalBroadcastManager.getInstance(FloozApplication.getAppContext()).unregisterReceiver(reloadNotificationsReceiver);
        LocalBroadcastManager.getInstance(FloozApplication.getAppContext()).unregisterReceiver(reloadUserReceiver);
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

    private void changeTabBadgeValue(@NonNull TabID tabID, int value) {
        switch (tabID) {
            case HOME_TAB:
                if (value > 0) {
                    this.homeTabBadge.setText("" + value);
                    this.homeTabBadge.setVisibility(View.VISIBLE);
                } else
                    this.homeTabBadge.setVisibility(View.GONE);
                break;
            case NOTIF_TAB:
                if (value > 0) {
                    this.notifTabBadge.setText("" + value);
                    this.notifTabBadge.setVisibility(View.VISIBLE);
                } else
                    this.notifTabBadge.setVisibility(View.GONE);
                break;
            case SHARE_TAB:
                if (value > 0) {
                    this.shareTabBadge.setText("" + value);
                    this.shareTabBadge.setVisibility(View.VISIBLE);
                } else
                    this.shareTabBadge.setVisibility(View.GONE);
                break;
            case ACCOUNT_TAB:
                if (value > 0) {
                    this.accountTabBadge.setText("" + value);
                    this.accountTabBadge.setVisibility(View.VISIBLE);
                } else
                    this.accountTabBadge.setVisibility(View.GONE);
                break;
        }
    }

    public void changeCurrentTab(@NonNull TabID tabID) {
        if (tabID != this.currentTabID) {
            int clearColor = this.getResources().getColor(R.color.placeholder);
            int selectedColor = this.getResources().getColor(R.color.blue);

            TextView oldItemText = null;
            ImageView oldItemIcon = null;
            TextView newItemText = null;
            ImageView newItemIcon = null;

            if (tabID != this.currentTabID) {
                if (this.currentTabID != null) {
                    switch (this.currentTabID) {
                        case HOME_TAB:
                            oldItemText = this.homeTabText;
                            oldItemIcon = this.homeTabImage;
                            this.homeTabText.setTextColor(clearColor);
                            this.homeTabImage.setColorFilter(clearColor);
                            break;
                        case NOTIF_TAB:
                            oldItemText = this.notifTabText;
                            oldItemIcon = this.notifTabImage;
                            this.notifTabText.setTextColor(clearColor);
                            this.notifTabImage.setColorFilter(clearColor);
                            break;
                        case SHARE_TAB:
                            oldItemText = this.shareTabText;
                            oldItemIcon = this.shareTabImage;
                            this.shareTabText.setTextColor(clearColor);
                            this.shareTabImage.setColorFilter(clearColor);
                            break;
                        case ACCOUNT_TAB:
                            oldItemText = this.accountTabText;
                            oldItemIcon = this.accountTabImage;
                            this.accountTabText.setTextColor(clearColor);
                            this.accountTabImage.setColorFilter(clearColor);
                            break;
                    }
                }

                this.currentTabID = tabID;

                if (this.tabHistory.contains(this.currentTabID))
                    this.tabHistory.remove(this.currentTabID);

                this.tabHistory.add(this.currentTabID);

                TabBarFragment fragment = null;

                switch (this.currentTabID) {
                    case HOME_TAB:
                        newItemText = this.homeTabText;
                        newItemIcon = this.homeTabImage;
                        fragment = this.homeTabHistory.get(this.homeTabHistory.size() - 1);
                        this.currentTabHistory = this.homeTabHistory;
                        break;
                    case NOTIF_TAB:
                        newItemText = this.notifTabText;
                        newItemIcon = this.notifTabImage;
                        fragment = this.notifsTabHistory.get(this.notifsTabHistory.size() - 1);
                        this.currentTabHistory = this.notifsTabHistory;
                        break;
                    case SHARE_TAB:
                        newItemText = this.shareTabText;
                        newItemIcon = this.shareTabImage;
                        fragment = this.shareTabHistory.get(this.shareTabHistory.size() - 1);
                        this.currentTabHistory = this.shareTabHistory;
                        break;
                    case ACCOUNT_TAB:
                        newItemText = this.accountTabText;
                        newItemIcon = this.accountTabImage;
                        fragment = this.accountTabHistory.get(this.accountTabHistory.size() - 1);
                        this.currentTabHistory = this.accountTabHistory;
                        break;
                }

                if (fragment != null) {
                    this.currentFragment = fragment;
                    FragmentTransaction ft = this.fragmentManager.beginTransaction();
                    ft.replace(R.id.fragment_container, fragment);
                    ft.addToBackStack(null).commit();
                    this.fragmentManager.executePendingTransactions();
                }

                if (oldItemText != null) {
                    oldItemText.setTextColor(clearColor);
                    oldItemIcon.setColorFilter(clearColor);
                }

                newItemText.setTextColor(selectedColor);
                newItemIcon.setColorFilter(selectedColor);

            }
        } else if (this.currentTabHistory.size() > 1) {
            while (this.currentTabHistory.size() > 2)
                this.currentTabHistory.remove(this.currentTabHistory.size() - 1);

            this.popFragmentInCurrentTab();
        }
    }

    public void pushFragmentInCurrentTab(TabBarFragment fragment) {
        this.pushFragmentInCurrentTab(fragment, R.animator.slide_in_left, R.animator.slide_out_right);
    }

    public void pushFragmentInCurrentTab(TabBarFragment fragment, int animIn, int animOut) {
        fragment.tabBarActivity = this;

        FragmentTransaction ft = this.getFragmentManager().beginTransaction();

        ft.setCustomAnimations(animIn, animOut, animIn, animOut);

        ft.replace(R.id.fragment_container, fragment);

        ft.commit();

        this.currentTabHistory.add(fragment);
        this.currentFragment = fragment;
    }

    public void popFragmentInCurrentTab() {
        this.popFragmentInCurrentTab(R.animator.slide_in_right, R.animator.slide_out_left);
    }

    public void popFragmentInCurrentTab(int animIn, int animOut) {
        if (this.currentTabHistory.size() > 1) {

            this.currentTabHistory.remove(this.currentTabHistory.size() - 1);

            TabBarFragment fragment = this.currentTabHistory.get(this.currentTabHistory.size() - 1);

            FragmentTransaction ft = this.getFragmentManager().beginTransaction();

            ft.setCustomAnimations(animIn, animOut, animIn, animOut);

            ft.replace(R.id.fragment_container, fragment);

            ft.commit();

            this.currentFragment = fragment;
        }
    }

    public void showTabBar() {
        this.tabBarVisibilityHandler.postDelayed(this.tabBarShowRunnable, 100);
    }

    public void hideTabBar() {
        this.tabBarVisibilityHandler.removeCallbacks(this.tabBarShowRunnable);
        this.tabBar.setVisibility(View.GONE);

        ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams)this.fragmentContainer.getLayoutParams();
        layoutParams.setMargins(layoutParams.leftMargin, layoutParams.topMargin, layoutParams.rightMargin, 0);
        this.fragmentContainer.setLayoutParams(layoutParams);
    }

    public void showTransactionCard(FLTransaction transaction) {
        this.showTransactionCard(transaction, false);
    }

    public void showTransactionCard(FLTransaction transaction, Boolean insertComment) {
        FloozRestClient.getInstance().readNotification(transaction.transactionId, null);
        TransactionCardFragment transactionCardFragment = new TransactionCardFragment();

        transactionCardFragment.insertComment = insertComment;
        transactionCardFragment.transaction = transaction;

        this.pushFragmentInCurrentTab(transactionCardFragment);
    }

    public void showImageViewer(final Bitmap image) {
        Animation anim = AnimationUtils.loadAnimation(getApplicationContext(), android.R.anim.fade_in);
        anim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                imageViewer.setVisibility(View.VISIBLE);
                imageViewerProgress.setMax(100);
                imageViewerProgress.setProgress(0);
                imageViewerImage.setVisibility(View.GONE);
                imageViewerProgress.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                imageViewerImage.setImageBitmap(image);
                imageViewerImage.setVisibility(View.VISIBLE);
                imageViewerProgress.setVisibility(View.GONE);
                imageViewerAttacher.update();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        this.imageViewer.startAnimation(anim);
    }

    public void showImageViewer(final String url) {
        if (!url.contentEquals("/img/fake")) {
            Animation anim = AnimationUtils.loadAnimation(getApplicationContext(), android.R.anim.fade_in);
            anim.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    imageViewer.setVisibility(View.VISIBLE);
                    imageViewerProgress.setMax(100);
                    imageViewerProgress.setProgress(0);
                    imageViewerImage.setVisibility(View.GONE);
                    imageViewerProgress.setVisibility(View.GONE);
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    imageViewerProgress.setVisibility(View.VISIBLE);

                    ImageLoader.getInstance().displayImage(url, imageViewerImage, null, new ImageLoadingListener() {
                        @Override
                        public void onLoadingStarted(String imageUri, View view) {
                            imageViewerImage.setVisibility(View.GONE);
                            imageViewerProgress.setVisibility(View.VISIBLE);
                            imageViewerProgress.setProgress(0);
                        }

                        @Override
                        public void onLoadingFailed(String imageUri, View view, FailReason failReason) {

                        }

                        @Override
                        public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                            imageViewerImage.setVisibility(View.VISIBLE);
                            imageViewerProgress.setVisibility(View.GONE);
                            imageViewerAttacher.update();
                        }

                        @Override
                        public void onLoadingCancelled(String imageUri, View view) {

                        }
                    }, (imageUri, view, current, total) -> {
                        float tmp = current;
                        tmp /= total;
                        tmp *= 100;

                        imageViewerProgress.setProgress((int) tmp);
                    });
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            this.imageViewer.startAnimation(anim);
        }
    }

    public void hideImageViewer() {
        this.imageViewerClose.performClick();
    }

    @Override
    public void onItemSelected(FLTransaction transac) {
        this.showTransactionCard(transac);
    }

    @Override
    public void onItemCommentSelected(FLTransaction transac) {
        this.showTransactionCard(transac, true);
    }

    @Override
    public void onItemImageSelected(String imgUrl) {
        CustomImageViewer.start(this, imgUrl);
    }

    @Override
    public void onBackPressed() {
        if (this.imageViewer.getVisibility() == View.VISIBLE) {
            this.hideImageViewer();
        } else if (this.currentTabHistory.size() > 1) {
            this.currentFragment.onBackPressed();
        } else if (this.tabHistory.size() > 1) {
            this.tabHistory.remove(this.tabHistory.size() - 1);
            this.changeCurrentTab(this.tabHistory.get(this.tabHistory.size() - 1));
        } else {
            this.finish();
        }
    }

    @Override
    public void onNewIntent(Intent intent) {
        this.setIntent(intent);
    }
}
