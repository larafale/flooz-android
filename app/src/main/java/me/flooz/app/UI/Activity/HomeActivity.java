package me.flooz.app.UI.Activity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.content.Context;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

import io.branch.referral.Branch;
import io.branch.referral.BranchError;
import me.flooz.app.Adapter.HeaderPagerAdapter;
import me.flooz.app.Adapter.TimelinePagerAdapter;
import me.flooz.app.Model.FLError;
import me.flooz.app.Model.FLTrigger;
import me.flooz.app.Model.FLUser;
import me.flooz.app.Network.FloozHttpResponseHandler;
import me.flooz.app.R;
import me.flooz.app.Model.FLTransaction;
import me.flooz.app.App.FloozApplication;
import me.flooz.app.UI.Fragment.Home.TimelineFragment;
import me.flooz.app.UI.Tools.ActionSheet;
import me.flooz.app.UI.Tools.ActionSheetItem;
import me.flooz.app.UI.Tools.CustomToast;
import me.flooz.app.Utils.ContactsManager;
import me.flooz.app.Network.FloozRestClient;
import me.flooz.app.Utils.CustomFonts;
import me.flooz.app.Utils.CustomNotificationIntents;
import me.flooz.app.UI.Fragment.Home.ProfileFragment;
import me.flooz.app.UI.Fragment.Home.FriendsFragment;
import me.flooz.app.UI.Fragment.Home.TransactionCardFragment;
import me.flooz.app.Utils.FLHelper;
import me.flooz.app.Utils.ImageHelper;
import me.flooz.app.Utils.ViewServer;
import uk.co.senab.photoview.PhotoViewAttacher;

import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.jeremyfeinstein.slidingmenu.lib.app.SlidingFragmentActivity;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.core.listener.ImageLoadingProgressListener;
import com.viewpagerindicator.CirclePageIndicator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class HomeActivity extends SlidingFragmentActivity implements TimelineFragment.TimelineFragmentDelegate
{
    public static final int SELECT_PICTURE = 1;
    public static final int TAKE_PICTURE = 2;

    private HomeActivity instance;

    private FragmentManager fragmentManager;

    private RelativeLayout imageViewer;
    private ImageView imageViewerClose;
    private ProgressBar imageViewerProgress;
    private ImageView imageViewerImage;
    private PhotoViewAttacher imageViewerAttacher;

    private RelativeLayout transactionCardContainer;

    private TransactionCardFragment transactionCardFragment;

    public ProfileFragment leftMenu;
    public FriendsFragment rightMenu;

    public FloozApplication floozApp;
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

    private BroadcastReceiver showLeftMenu = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (getSlidingMenu().isMenuShowing()) {
                getSlidingMenu().showContent(true);
                getSlidingMenu().setSlidingEnabled(false);
            }
            else {
                FloozRestClient.getInstance().updateCurrentUser(null);
                getSlidingMenu().showMenu(true);
                getSlidingMenu().setSlidingEnabled(true);
            }
        }
    };

    private BroadcastReceiver showRightMenu = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (getSlidingMenu().isSecondaryMenuShowing()) {
                getSlidingMenu().showContent(true);
                getSlidingMenu().setSlidingEnabled(false);
            }
            else {
                FloozRestClient.getInstance().updateCurrentUser(null);
                getSlidingMenu().showSecondaryMenu(true);
                getSlidingMenu().setSlidingEnabled(true);
            }
        }
    };

    private BroadcastReceiver disableMenu = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            getSlidingMenu().setSlidingEnabled(false);
        }
    };

    private BroadcastReceiver enableMenu = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            getSlidingMenu().setSlidingEnabled(true);
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        this.instance = this;

        if (FLHelper.isDebuggable())
            ViewServer.get(this).addWindow(this);

        final Intent intent = getIntent();
        if (intent != null && intent.getAction() != null && intent.getAction().contentEquals(Intent.ACTION_VIEW)) {
            if (!FloozRestClient.getInstance().autologin()) {
                FloozApplication.getInstance().displayStartView();
            }
        }
        if (intent != null && intent.getExtras() != null) {
            if (!intent.getBooleanExtra("inApp", false)) {
                if (!FloozRestClient.getInstance().autologin()) {
                    FloozApplication.getInstance().displayStartView();
                }
            }
        }

        Branch branch = Branch.getInstance(getApplicationContext());
        branch.initSession((referringParams, error) -> {
            if (error == null) {
                if (FloozRestClient.getInstance().accessToken != null) {
                    String data = referringParams.optString("data");

                    if (data != null && !data.isEmpty()) {
                        try {
                            JSONObject dataObject = new JSONObject(data);

                            JSONArray t = dataObject.optJSONArray("triggers");

                            for (int i = 0; i < t.length(); i++) {
                                final FLTrigger trigger = new FLTrigger(t.optJSONObject(i));
                                if (trigger.delay.doubleValue() > 0) {
                                    Handler thandler = new Handler(Looper.getMainLooper());
                                    thandler.postDelayed(() -> FloozRestClient.getInstance().handleTrigger(trigger), (int) (trigger.delay.doubleValue() * 1000));
                                } else {
                                    Handler thandler = new Handler(Looper.getMainLooper());
                                    thandler.post(() -> FloozRestClient.getInstance().handleTrigger(trigger));
                                }
                            }

                            if (dataObject.has("popup")) {
                                FLError errorContent = new FLError(dataObject.optJSONObject("popup"));
                                CustomToast.show(FloozApplication.getAppContext(), errorContent);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    FloozApplication.getInstance().displayStartView();
                }
            }
        }, this.getIntent().getData(), this);

        floozApp = (FloozApplication)this.getApplicationContext();

        this.fragmentManager = this.getFragmentManager();

        this.leftMenu = new ProfileFragment();
        this.rightMenu = new FriendsFragment();

        this.rightMenu.parentActivity = this;
        this.leftMenu.parentActivity = this;

        this.setContentView(R.layout.activity_home);
        this.headerPagerView = (ViewPager) this.findViewById(R.id.timeline_header_pager);
        this.headerPagerView.setAdapter(new HeaderPagerAdapter(getFragmentManager()));
        this.headerPagerView.setCurrentItem(0);
        this.headerPagerView.setOffscreenPageLimit(2);

        this.pageIndicator = (CirclePageIndicator) this.findViewById(R.id.pagerindicator);

        this.findViewById(R.id.timeline_header).setOnClickListener(v -> {
            if (timelineViewPager.getCurrentItem() == 0)
                timelineViewPagerAdapter.homeTimeline.timelineListView.smoothScrollToPosition(0);
            else if (timelineViewPager.getCurrentItem() == 1)
                timelineViewPagerAdapter.publicTimeline.timelineListView.smoothScrollToPosition(0);
            else
                timelineViewPagerAdapter.privateTimeline.timelineListView.smoothScrollToPosition(0);
        });

        if (this.timelineViewPagerAdapter == null) {
            this.timelineViewPagerAdapter = new TimelinePagerAdapter(getFragmentManager());
            this.timelineViewPagerAdapter.homeTimeline.delegate = this;
            this.timelineViewPagerAdapter.publicTimeline.delegate = this;
            this.timelineViewPagerAdapter.privateTimeline.delegate = this;
        }

        this.timelineViewPager = (ViewPager) this.findViewById(R.id.timeline_pager);
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
        this.pageIndicator.setFillColor(this.getResources().getColor(R.color.blue));
        this.pageIndicator.setStrokeColor(this.getResources().getColor(R.color.blue));

        this.headerProfileButton = (ImageView) this.findViewById(R.id.profile_header_button);

        final int[] doubleClickChecker = {0};
        this.headerProfileButton.setOnClickListener(v -> {
            doubleClickChecker[0]++;
            Handler handler = new Handler();
            Runnable r = () -> {
                if (doubleClickChecker[0] == 1) {
                    showLeftMenu();
                }
                doubleClickChecker[0] = 0;
            };

            if (doubleClickChecker[0] == 1) {
                handler.postDelayed(r, 250);
            } else if (doubleClickChecker[0] == 2) {
                Intent intent1 = new Intent(instance, NotificationActivity.class);
                instance.startActivity(intent1);
                instance.overridePendingTransition(R.anim.slide_up, android.R.anim.fade_out);
                doubleClickChecker[0] = 0;
            }
        });

        this.headerProfileButtonNotifs = (TextView)this.findViewById(R.id.profile_header_button_notif);
        this.headerProfileButtonNotifs.setTypeface(CustomFonts.customTitleLight(this));

        if (FloozRestClient.getInstance().notificationsManager.nbUnreadNotifications > 0) {
            this.headerProfileButtonNotifs.setVisibility(View.VISIBLE);
            this.headerProfileButtonNotifs.setText(String.valueOf(FloozRestClient.getInstance().notificationsManager.nbUnreadNotifications));
        }
        else
            this.headerProfileButtonNotifs.setVisibility(View.INVISIBLE);

        ImageView friendsButton = (ImageView)this.findViewById(R.id.friends_header_button);

        friendsButton.setOnClickListener(v -> showRightMenu());

        this.newTransactionButton = (ImageView) this.findViewById(R.id.new_transac_button);
        this.newTransactionButton.setOnClickListener(v -> {
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
                Intent intent1 = new Intent(instance, NewTransactionActivity.class);
                instance.startActivity(intent1);
                instance.overridePendingTransition(R.anim.slide_up, android.R.anim.fade_out);
            }
        });

        this.imageViewer = (RelativeLayout) this.findViewById(R.id.main_image_container);
        this.imageViewerClose = (ImageView) this.findViewById(R.id.main_image_close);
        this.imageViewerProgress = (ProgressBar) this.findViewById(R.id.main_image_progress);
        this.imageViewerImage = (ImageView) this.findViewById(R.id.main_image_image);

        this.transactionCardContainer = (RelativeLayout) this.findViewById(R.id.main_transac_container);

        this.imageViewerAttacher = new PhotoViewAttacher(this.imageViewerImage);

        this.imageViewer.setClickable(true);

        this.imageViewerAttacher.setOnLongClickListener(v -> {
            List<ActionSheetItem> items = new ArrayList<>();
            items.add(new ActionSheetItem(instance, R.string.MENU_SAVE_PICTURE, () -> {
                ImageHelper.saveImageOnPhone(instance, ((BitmapDrawable)imageViewerAttacher.getImageView().getDrawable()).getBitmap());
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

        this.transactionCardFragment = new TransactionCardFragment();
        this.transactionCardFragment.parentActivity = this;

        FragmentTransaction ft = this.fragmentManager.beginTransaction();
        ft.replace(R.id.main_transac_view, this.transactionCardFragment);
        ft.addToBackStack(null).commit();

        this.setBehindContentView(R.layout.left_sidemenu);

        SlidingMenu sm = getSlidingMenu();
        sm.setShadowWidthRes(R.dimen.slidingmenu_shadow_width);
        sm.setShadowDrawable(R.drawable.slidemenu_left_shadow);
        sm.setSecondaryShadowDrawable(R.drawable.slidemenu_right_shadow);
        sm.setBehindOffsetRes(R.dimen.slidingmenu_offset);
        sm.setFadeDegree(0.40f);
        sm.setMode(SlidingMenu.LEFT_RIGHT);

        sm.setMenu(R.layout.left_sidemenu);
        sm.setSecondaryMenu(R.layout.right_sidemenu);
        sm.setSlidingEnabled(false);

        sm.setOnCloseListener(() -> {
            getSlidingMenu().setSlidingEnabled(false);
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getWindow().getDecorView().getRootView().getWindowToken(), 0);
        });

        this.changeLeftMenuFragment(this.leftMenu, false);
        this.changeRightMenuFragment(this.rightMenu, false);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == AuthenticationActivity.RESULT_AUTHENTICATION_ACTIVITY) {
                transactionCardFragment.authenticationValidated();
            }

            if (requestCode == SELECT_PICTURE || requestCode == TAKE_PICTURE) {
                Uri imageUri;
                if (data == null || data.getData() == null)
                    imageUri = this.leftMenu.tmpUriImage;
                else
                    imageUri = data.getData();

                this.leftMenu.tmpUriImage = null;

                final Uri selectedImageUri = imageUri;
                if (selectedImageUri != null) {
                    Handler handler = new Handler(Looper.getMainLooper());
                    handler.post(() -> {
                        String path = ImageHelper.getPath(instance, selectedImageUri);
                        if (path != null) {
                            File image = new File(path);
                            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
                            Bitmap photo = BitmapFactory.decodeFile(image.getAbsolutePath(), bmOptions);

                            if (photo != null) {
                                int rotation = ImageHelper.getRotation(instance, selectedImageUri);
                                if (rotation != 0) {
                                    photo = ImageHelper.rotateBitmap(photo, rotation);
                                }

                                int nh = (int) (photo.getHeight() * (512.0 / photo.getWidth()));
                                Bitmap scaled = Bitmap.createScaledBitmap(photo, 512, nh, true);
                                leftMenu.userView.setImageBitmap(scaled);
                                FloozRestClient.getInstance().uploadDocument("picId", image, new FloozHttpResponseHandler() {
                                    @Override
                                    public void success(Object response) {
                                        FloozRestClient.getInstance().updateCurrentUser(null);
                                    }

                                    @Override
                                    public void failure(int statusCode, FLError error) {
                                        FLUser user = FloozRestClient.getInstance().currentUser;

                                        if (user.avatarURL != null)
                                            ImageLoader.getInstance().displayImage(user.avatarURL, leftMenu.userView);
                                        else
                                            leftMenu.userView.setImageDrawable(getResources().getDrawable(R.drawable.avatar_default));
                                    }
                                });
                            }
                        }
                    });
                }
            }
        }
    }

    public void onStart() {
        super.onStart();

    }

    protected void onResume() {
        super.onResume();

        if (FLHelper.isDebuggable())
            ViewServer.get(this).setFocusedWindow(this);

        floozApp.setCurrentActivity(this);

        LocalBroadcastManager.getInstance(FloozApplication.getAppContext()).registerReceiver(showLeftMenu,
                CustomNotificationIntents.filterShowSlidingLeftMenu());

        LocalBroadcastManager.getInstance(FloozApplication.getAppContext()).registerReceiver(showRightMenu,
                CustomNotificationIntents.filterShowSlidingRightMenu());

        LocalBroadcastManager.getInstance(FloozApplication.getAppContext()).registerReceiver(enableMenu,
                CustomNotificationIntents.filterEnableSlidingMenu());

        LocalBroadcastManager.getInstance(FloozApplication.getAppContext()).registerReceiver(disableMenu,
                CustomNotificationIntents.filterDisableSlidingMenu());

        LocalBroadcastManager.getInstance(FloozApplication.getAppContext()).registerReceiver(reloadNotificationsReceiver,
                CustomNotificationIntents.filterReloadNotifications());

        Handler handlerIntent = new Handler(Looper.getMainLooper());
        final boolean b = handlerIntent.postDelayed(() -> {
            final Intent intent = getIntent();
            JSONArray triggersArray = null;
            if (intent != null && intent.getExtras() != null) {
                String message = intent.getStringExtra("triggers");
                if (message != null && !message.isEmpty()) {
                    try {
                        triggersArray = new JSONArray(message);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    if (triggersArray != null) {
                        final JSONArray finalJsonObject = triggersArray;
                        if (floozApp.getCurrentActivity() instanceof HomeActivity) {
                            for (int i = 0; i < finalJsonObject.length(); i++) {
                                final FLTrigger trigger = new FLTrigger(finalJsonObject.optJSONObject(i));
                                if (trigger.delay.doubleValue() > 0) {
                                    Handler handlerTrigger = new Handler(Looper.getMainLooper());
                                    handlerTrigger.postDelayed(() -> FloozRestClient.getInstance().handleTrigger(trigger), (int) (trigger.delay.doubleValue() * 1000));
                                } else {
                                    FloozRestClient.getInstance().handleTrigger(trigger);
                                }
                            }
                        }
                    }
                } else if (intent.getData() != null) {
                    String path = null;
                    try {
                        path = URLDecoder.decode(intent.getData().toString(), "UTF-8");
                        path = path.replace("flooz://", "");
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }

                    if (path != null) {
                        try {
                            JSONObject obj = new JSONObject(path);

                            JSONObject data = obj.optJSONObject("data");

                            JSONArray t = data.optJSONArray("triggers");

                            for (int i = 0; i < t.length(); i++) {
                                final FLTrigger trigger = new FLTrigger(t.optJSONObject(i));
                                if (trigger.delay.doubleValue() > 0) {
                                    Handler thandler = new Handler(Looper.getMainLooper());
                                    thandler.postDelayed(() -> FloozRestClient.getInstance().handleTrigger(trigger), (int) (trigger.delay.doubleValue() * 1000));
                                } else {
                                    Handler thandler = new Handler(Looper.getMainLooper());
                                    thandler.post(() -> FloozRestClient.getInstance().handleTrigger(trigger));
                                }
                            }

                            if (data.has("popup")) {
                                FLError errorContent = new FLError(data.optJSONObject("popup"));
                                CustomToast.show(FloozApplication.getAppContext(), errorContent);
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
                setIntent(new Intent());
            }
        }, 1000);
    }

    protected void onPause() {
        clearReferences();
        super.onPause();

        LocalBroadcastManager.getInstance(FloozApplication.getAppContext()).unregisterReceiver(showLeftMenu);
        LocalBroadcastManager.getInstance(FloozApplication.getAppContext()).unregisterReceiver(showRightMenu);
        LocalBroadcastManager.getInstance(FloozApplication.getAppContext()).unregisterReceiver(enableMenu);
        LocalBroadcastManager.getInstance(FloozApplication.getAppContext()).unregisterReceiver(disableMenu);
        LocalBroadcastManager.getInstance(FloozApplication.getAppContext()).unregisterReceiver(reloadNotificationsReceiver);
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

    public void changeLeftMenuFragment(Fragment fragment, Boolean anim)
    {
        if (fragment == null || fragment.isAdded())
            return;

        FragmentTransaction ft = this.fragmentManager.beginTransaction();

        if (anim)
            ft.setCustomAnimations(android.R.anim.fade_in, android.R.animator.fade_out);

        ft.replace(R.id.left_menu_frame, fragment);
        ft.addToBackStack(null).commit();
    }

    public void changeRightMenuFragment(Fragment fragment, Boolean anim)
    {
        if (fragment == null || fragment.isAdded())
            return;

        FragmentTransaction ft = this.fragmentManager.beginTransaction();

        if (anim)
            ft.setCustomAnimations(R.animator.slide_up,
                    R.animator.slide_down,
                    R.animator.slide_up,
                    R.animator.slide_down);

        ft.replace(R.id.right_menu_frame, fragment).addToBackStack(null).commit();
    }

    public void showLeftMenu() {
        if (getSlidingMenu().isMenuShowing()) {
            getSlidingMenu().showContent(true);
            getSlidingMenu().setSlidingEnabled(false);
        }
        else {
            FloozRestClient.getInstance().updateCurrentUser(null);
            getSlidingMenu().showMenu(true);
            getSlidingMenu().setSlidingEnabled(true);
        }
    }

    public void showRightMenu() {
        if (getSlidingMenu().isSecondaryMenuShowing()) {
            getSlidingMenu().showContent(true);
            getSlidingMenu().setSlidingEnabled(false);
        }
        else {
            FloozRestClient.getInstance().updateCurrentUser(new FloozHttpResponseHandler() {
                @Override
                public void success(Object response) {
                    FloozRestClient.getInstance().readFriendNotifications(null);
                }

                @Override
                public void failure(int statusCode, FLError error) {

                }
            });
            getSlidingMenu().showSecondaryMenu(true);
            getSlidingMenu().setSlidingEnabled(true);
        }
    }

    public void disableMenu() {
        getSlidingMenu().setSlidingEnabled(false);
    }

    public void enableMenu() {
        getSlidingMenu().setSlidingEnabled(true);
    }


    public void showTransactionCard(FLTransaction transaction) {
        this.showTransactionCard(transaction, false);
    }

    public void showTransactionCard(FLTransaction transaction, Boolean insertComment) {
        FloozRestClient.getInstance().readNotification(transaction.transactionId, null);

        this.transactionCardFragment.insertComment = insertComment;
        this.transactionCardFragment.setTransaction(transaction);
        this.transactionCardFragment.cardScroll.scrollTo(0, 0);

        this.getSlidingMenu().showContent(false);
        this.getSlidingMenu().setSlidingEnabled(false);

        if (this.transactionCardContainer.getVisibility() == View.GONE) {
            Animation anim = AnimationUtils.loadAnimation(getApplicationContext(), android.R.anim.fade_in);
            anim.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    transactionCardContainer.setVisibility(View.VISIBLE);
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    transactionCardFragment.updateComment();
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            this.transactionCardContainer.startAnimation(anim);
        }
    }

    public void hideTransactionCard() {
        if (this.transactionCardContainer.getVisibility() == View.VISIBLE) {
            Animation anim = AnimationUtils.loadAnimation(getApplicationContext(), android.R.anim.fade_out);
            anim.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    transactionCardContainer.setVisibility(View.GONE);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            this.transactionCardContainer.startAnimation(anim);
        }
    }

    public void showImageViewer(final Bitmap image) {
        this.getSlidingMenu().showContent(false);
        this.getSlidingMenu().setSlidingEnabled(false);

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
            this.getSlidingMenu().showContent(false);
            this.getSlidingMenu().setSlidingEnabled(false);

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
        this.showImageViewer(imgUrl);
    }

    @Override
    public void onBackPressed() {
        if (this.imageViewer.getVisibility() == View.VISIBLE) {
            this.imageViewerClose.performClick();
        } else if (this.transactionCardContainer.getVisibility() == View.VISIBLE) {
            this.hideTransactionCard();
        } else if (this.getSlidingMenu().isMenuShowing()) {
            this.getSlidingMenu().showContent(true);
            this.getSlidingMenu().setSlidingEnabled(false);
        } else if (this.getSlidingMenu().isSecondaryMenuShowing()) {
            this.getSlidingMenu().showContent(true);
            this.getSlidingMenu().setSlidingEnabled(false);
        } else {
            this.finish();
        }
    }

    @Override
    public void onNewIntent(Intent intent) {
        this.setIntent(intent);
    }
}
