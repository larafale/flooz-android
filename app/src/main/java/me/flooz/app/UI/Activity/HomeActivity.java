package me.flooz.app.UI.Activity;

import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.input.InputManager;
import android.media.Image;
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
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.support.v4.content.LocalBroadcastManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import jp.wasabeef.blurry.Blurry;
import me.flooz.app.Adapter.HomeButtonListAdapter;
import me.flooz.app.Model.FLButton;
import me.flooz.app.Model.FLError;
import me.flooz.app.Model.FLShareText;
import me.flooz.app.Model.FLTrigger;
import me.flooz.app.Network.FloozHttpResponseHandler;
import me.flooz.app.R;
import me.flooz.app.Model.FLTransaction;
import me.flooz.app.App.FloozApplication;
import me.flooz.app.UI.Activity.Settings.CreditCardSettingsActivity;
import me.flooz.app.UI.Fragment.Home.TabFragments.CollectFragment;
import me.flooz.app.UI.Fragment.Home.TabFragments.NotificationsFragment;
import me.flooz.app.UI.Fragment.Home.TabFragments.ProfileCardFragment;
import me.flooz.app.UI.Fragment.Home.TabFragments.ProfileFragment;
import me.flooz.app.UI.Fragment.Home.TabFragments.ShareFragment;
import me.flooz.app.UI.Fragment.Home.TabFragments.TabBarFragment;
import me.flooz.app.UI.Fragment.Home.TabFragments.TimelineFragment;
import me.flooz.app.UI.Tools.ActionSheet;
import me.flooz.app.UI.Tools.ActionSheetItem;
import me.flooz.app.UI.Tools.CustomImageViewer;
import me.flooz.app.UI.Tools.CustomToast;
import me.flooz.app.Network.FloozRestClient;
import me.flooz.app.UI.View.CustomFrameLayout;
import me.flooz.app.Utils.CustomFonts;
import me.flooz.app.Utils.CustomNotificationIntents;
import me.flooz.app.UI.Fragment.Home.TabFragments.TransactionCardFragment;
import me.flooz.app.Utils.FLHelper;
import me.flooz.app.Utils.FLTriggerManager;
import me.flooz.app.Utils.ImageHelper;
import me.flooz.app.Utils.SoftKeyboardHandledRelativeLayout;
import me.flooz.app.Utils.ViewServer;
import uk.co.senab.photoview.PhotoViewAttacher;

import org.json.JSONArray;
import org.json.JSONObject;

public class HomeActivity extends BaseActivity implements TimelineFragment.TimelineFragmentDelegate
{
    public enum TabID {
        HOME_TAB,
        NOTIF_TAB,
        SHARE_TAB,
        ACCOUNT_TAB,
        NONE
    }

    private FragmentManager fragmentManager;

    private ArrayList<TabID> tabHistory;
    private ArrayList<TabBarFragment> homeTabHistory;
    private ArrayList<TabBarFragment> notifsTabHistory;
    private ArrayList<TabBarFragment> shareTabHistory;
    private ArrayList<TabBarFragment> accountTabHistory;
    public ArrayList<ArrayList<TabBarFragment>> fullTabHistory;

    public TabID currentTabID;
    public TabBarFragment currentFragment;
    public ArrayList<TabBarFragment> currentTabHistory;

    private SoftKeyboardHandledRelativeLayout mainView;

    private CustomFrameLayout fragmentContainer;
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

    private RelativeLayout homeButtonView;
    private ImageView homeButtonBackground;
    private ImageView homeButtonClose;
    private TextView homeButtonTitle;
    private ListView homeButtonList;
    private HomeButtonListAdapter homeButtonListAdapter;

    private InputMethodManager imm;

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

    private BroadcastReceiver reloadInvitationReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            FLShareText texts = FloozRestClient.getInstance().currentShareText;

            shareTabText.setText(texts.shareTitle);
        }
    };

    private static HomeActivity instance;

    public static HomeActivity getInstance() {
        return instance;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        instance = this;

        if (FLHelper.isDebuggable())
            ViewServer.get(this).addWindow(this);

        floozApp = (FloozApplication)this.getApplicationContext();

        this.fragmentManager = this.getFragmentManager();
        this.imm = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);

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
                if (FloozApplication.getInstance().getCurrentActivity() instanceof HomeActivity) {
                    if ((currentFragment instanceof TransactionCardFragment || currentFragment instanceof CollectFragment)) {
                        if (tabBar.getVisibility() == View.VISIBLE) {
                            hideTabBar();
                        }
                    } else
                        showTabBar();
                }
            }
        });

        this.fragmentContainer = (CustomFrameLayout) this.findViewById(R.id.fragment_container);
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

        this.homeButtonView = (RelativeLayout) this.findViewById(R.id.home_menu_container);
        this.homeButtonBackground = (ImageView) this.findViewById(R.id.home_menu_background);
        this.homeButtonClose = (ImageView) this.findViewById(R.id.home_menu_close);
        this.homeButtonTitle = (TextView) this.findViewById(R.id.home_menu_title);
        this.homeButtonList = (ListView) this.findViewById(R.id.home_menu_list);


        this.homeButtonListAdapter = new HomeButtonListAdapter(this);
        this.homeButtonList.setAdapter(this.homeButtonListAdapter);

        this.homeButtonList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                FLButton button = (FLButton) homeButtonListAdapter.getItem(position);
                if (button.avalaible)
                    FLTriggerManager.getInstance().executeTriggerList(FLTriggerManager.convertTriggersJSONArrayToList(button.triggers));

                homeButtonView.setVisibility(View.GONE);
            }
        });

        int clearColor = this.getResources().getColor(R.color.placeholder);
        Typeface titleTypeface = CustomFonts.customContentLight(this);
        Typeface badgeTypeface = CustomFonts.customContentRegular(this);

        this.homeTabText.setTypeface(titleTypeface);
        this.notifTabText.setTypeface(titleTypeface);
        this.shareTabText.setTypeface(titleTypeface);
        this.accountTabText.setTypeface(titleTypeface);
        this.homeButtonTitle.setTypeface(titleTypeface);

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

        this.homeTab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeCurrentTab(TabID.HOME_TAB);
            }
        });
        this.notifTab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeCurrentTab(TabID.NOTIF_TAB);
            }
        });

        this.shareTab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeCurrentTab(TabID.SHARE_TAB);
            }
        });

        this.accountTab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeCurrentTab(TabID.ACCOUNT_TAB);
            }
        });

        this.tabHistory = new ArrayList<>();

        TabBarFragment timelineFragment = new TimelineFragment();

        ProfileCardFragment controller = new ProfileCardFragment();
        controller.user = FloozRestClient.getInstance().currentUser;
        TabBarFragment accountFragment = controller;

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

        this.fullTabHistory = new ArrayList<>();
        this.fullTabHistory.add(this.homeTabHistory);
        this.fullTabHistory.add(this.notifsTabHistory);
        this.fullTabHistory.add(this.shareTabHistory);
        this.fullTabHistory.add(this.accountTabHistory);

        this.floozTabImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (FloozRestClient.getInstance().currentTexts != null) {
                    if (FloozRestClient.getInstance().currentTexts.homeTriggers != null && FloozRestClient.getInstance().currentTexts.homeTriggers.size() > 0) {
                        FLTriggerManager.getInstance().executeTriggerList(FloozRestClient.getInstance().currentTexts.homeTriggers);
                    } else if (FloozRestClient.getInstance().currentTexts.homeButtons != null && FloozRestClient.getInstance().currentTexts.homeButtons.size() > 0) {
                        if (FloozRestClient.getInstance().currentTexts.homeButtons.size() == 1) {
                            FLTriggerManager.getInstance().executeTriggerList(FLTriggerManager.convertTriggersJSONArrayToList(FloozRestClient.getInstance().currentTexts.homeButtons.get(0).triggers));
                        } else {
                            homeButtonView.setVisibility(View.VISIBLE);
                        }
                    }
                }
            }
        });

        this.homeButtonClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                homeButtonView.setVisibility(View.GONE);
            }
        });

        this.imageViewerAttacher.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                List<ActionSheetItem> items = new ArrayList<>();
                items.add(new ActionSheetItem(instance, R.string.MENU_SAVE_PICTURE, new ActionSheetItem.ActionSheetItemClickListener() {
                    @Override
                    public void onClick() {
                        ImageHelper.saveImageOnPhone(instance, ((BitmapDrawable) imageViewerAttacher.getImageView().getDrawable()).getBitmap());
                    }
                }));

                ActionSheet.showWithItems(instance, items);

                return false;
            }
        });

        this.imageViewerClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
            }
        });

        this.changeCurrentTab(TabID.HOME_TAB);

        this.changeTabBadgeValue(TabID.ACCOUNT_TAB, FloozRestClient.getInstance().currentUser.json.optJSONObject("metrics").optInt("accountMissing"));
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

        LocalBroadcastManager.getInstance(FloozApplication.getAppContext()).registerReceiver(reloadInvitationReceiver,
                CustomNotificationIntents.filterReloadInvitation());

        if (FloozApplication.getInstance().pendingTriggers != null) {
            Handler handlerIntent = new Handler(Looper.getMainLooper());
            final boolean b = handlerIntent.postDelayed(new Runnable() {
                @Override
                public void run() {
                    FloozRestClient.getInstance().handleTriggerArray(FloozApplication.getInstance().pendingTriggers);
                    FloozApplication.getInstance().pendingTriggers = null;
                }
            }, 100);
        }

        if (FloozApplication.getInstance().pendingPopup != null) {
            Handler handlerIntent = new Handler(Looper.getMainLooper());
            final boolean b = handlerIntent.postDelayed(new Runnable() {
                @Override
                public void run() {
                    CustomToast.show(HomeActivity.this, new FLError(FloozApplication.getInstance().pendingPopup));
                    FloozApplication.getInstance().pendingPopup = null;
                }
            }, 100);
        }
    }

    protected void onPause() {
        clearReferences();
        super.onPause();

        LocalBroadcastManager.getInstance(FloozApplication.getAppContext()).unregisterReceiver(reloadNotificationsReceiver);
        LocalBroadcastManager.getInstance(FloozApplication.getAppContext()).unregisterReceiver(reloadUserReceiver);
        LocalBroadcastManager.getInstance(FloozApplication.getAppContext()).unregisterReceiver(reloadInvitationReceiver);
    }

    protected void onDestroy() {
        clearReferences();

        if (FLHelper.isDebuggable())
            ViewServer.get(this).removeWindow(this);

        instance = null;

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
        this.changeCurrentTab(tabID, false, null);
    }

    public void changeCurrentTab(@NonNull TabID tabID, Boolean clearHistory) {
        this.changeCurrentTab(tabID, clearHistory, null);
    }

    public void changeCurrentTab(@NonNull TabID tabID, Runnable completion) {
        this.changeCurrentTab(tabID, false, completion);
    }

    public void changeCurrentTab(@NonNull TabID tabID, Boolean clearHistory, Runnable completion) {
        if (tabID != this.currentTabID) {

            imm.hideSoftInputFromWindow(this.getWindow().getDecorView().getRootView().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

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

                if (clearHistory) {
                    int loop = this.currentTabHistory.size() - 1;
                    while (loop > 0) {
                        TabBarFragment clearFragment = this.currentTabHistory.get(this.currentTabHistory.size() - 1);
                        this.currentTabHistory.remove(clearFragment);
                        --loop;
                    }
                    fragment = this.currentTabHistory.get(this.currentTabHistory.size() - 1);
                }

                if (fragment != null) {
                    this.currentFragment = fragment;
                    FragmentTransaction ft = this.fragmentManager.beginTransaction();
                    ft.replace(R.id.fragment_container, fragment);
                    ft.addToBackStack(null).commit();
                    this.fragmentManager.executePendingTransactions();

                    if (completion != null) {
                        Handler handler = new Handler(Looper.getMainLooper());
                        handler.post(completion);
                    }
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

            this.popFragmentInCurrentTab(completion);
        } else if (completion != null) {
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(completion);
        }

    }

    public static TabID tabIDFromIndex(Integer index) {
        switch (index) {
            case 0:
                return TabID.HOME_TAB;
            case 1:
                return TabID.NOTIF_TAB;
            case 2:
                return TabID.SHARE_TAB;
            case 3:
                return TabID.ACCOUNT_TAB;
            default:
                break;
        }
        return TabID.NONE;
    }

    public void pushFragmentInCurrentTab(TabBarFragment fragment) {
        this.pushFragmentInCurrentTab(fragment, R.animator.slide_in_left, R.animator.slide_out_right, null);
    }

    public void pushFragmentInCurrentTab(TabBarFragment fragment, Runnable completion) {
        this.pushFragmentInCurrentTab(fragment, R.animator.slide_in_left, R.animator.slide_out_right, completion);
    }

    public void pushFragmentInCurrentTab(TabBarFragment fragment, int animIn, int animOut, Runnable completion) {
        imm.hideSoftInputFromWindow(this.getWindow().getDecorView().getRootView().getWindowToken(), InputMethodManager.HIDE_IMPLICIT_ONLY);
        imm.hideSoftInputFromWindow(this.getWindow().getDecorView().getRootView().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

        fragment.tabBarActivity = this;

        FragmentTransaction ft = this.getFragmentManager().beginTransaction();

        ft.setCustomAnimations(animIn, animOut, animIn, animOut);

        ft.replace(R.id.fragment_container, fragment);

        ft.commit();

        this.currentTabHistory.add(fragment);
        this.currentFragment = fragment;

        this.getFragmentManager().executePendingTransactions();

        if ((this.currentFragment instanceof TransactionCardFragment || this.currentFragment instanceof CollectFragment)) {
            if (this.tabBar.getVisibility() == View.VISIBLE) {
                this.hideTabBar();
            }
        } else {
            this.showTabBar();
        }

        if (completion != null) {
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(completion);
        }
    }

    public void popFragmentInCurrentTab() {
        this.popFragmentInCurrentTab(R.animator.slide_in_right, R.animator.slide_out_left, null);
    }

    public void popFragmentInCurrentTab(Runnable completion) {
        this.popFragmentInCurrentTab(R.animator.slide_in_right, R.animator.slide_out_left, completion);
    }

    public void popFragmentInCurrentTab(int animIn, int animOut) {
        this.popFragmentInCurrentTab(animIn, animOut, null);
    }

    public void popFragmentInCurrentTab(int animIn, int animOut, Runnable completion) {
        if (this.currentTabHistory.size() > 1) {

            imm.hideSoftInputFromWindow(this.getWindow().getDecorView().getRootView().getWindowToken(), InputMethodManager.HIDE_IMPLICIT_ONLY);
            imm.hideSoftInputFromWindow(this.getWindow().getDecorView().getRootView().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

            this.currentTabHistory.remove(this.currentTabHistory.size() - 1);

            TabBarFragment fragment = this.currentTabHistory.get(this.currentTabHistory.size() - 1);

            FragmentTransaction ft = this.getFragmentManager().beginTransaction();

            ft.setCustomAnimations(animIn, animOut, animIn, animOut);

            ft.replace(R.id.fragment_container, fragment);

            ft.commit();

            this.currentFragment = fragment;

            this.getFragmentManager().executePendingTransactions();

            if ((this.currentFragment instanceof TransactionCardFragment || this.currentFragment instanceof CollectFragment)) {
                if (this.tabBar.getVisibility() == View.VISIBLE) {
                    this.hideTabBar();
                }
            } else {
                this.showTabBar();
            }

            if (completion != null) {
                Handler handler = new Handler(Looper.getMainLooper());
                handler.post(completion);
            }
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

    @Override
    public void onItemSelected(FLTransaction transac) {
        if (transac.isCollect)
            FloozApplication.getInstance().showCollect(transac);
        else
            FloozApplication.getInstance().showTransactionCard(transac);
    }

    @Override
    public void onItemCommentSelected(FLTransaction transac) {
        if (transac.isCollect)
            FloozApplication.getInstance().showCollect(transac, true);
        else
            FloozApplication.getInstance().showTransactionCard(transac, true);
    }

    @Override
    public void onItemImageSelected(String imgUrl) {
        CustomImageViewer.start(this, imgUrl);
    }

    @Override
    public void onBackPressed() {
        if (this.currentTabHistory.size() > 1) {
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

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        currentFragment.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
