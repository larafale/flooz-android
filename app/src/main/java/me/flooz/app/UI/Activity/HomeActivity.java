package me.flooz.app.UI.Activity;

import android.graphics.Bitmap;
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
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import java.net.URL;
import java.util.Map;
import java.util.List;
import java.util.HashMap;
import java.util.ArrayList;

import me.flooz.app.Model.FLTrigger;
import me.flooz.app.R;
import me.flooz.app.Model.FLTransaction;
import me.flooz.app.App.FloozApplication;
import me.flooz.app.Utils.ContactsManager;
import me.flooz.app.Utils.CustomCameraHost;
import me.flooz.app.Network.FloozRestClient;
import me.flooz.app.UI.Fragment.Home.FloozFragment;
import me.flooz.app.Utils.CustomNotificationIntents;
import me.flooz.app.UI.Fragment.Home.ProfileFragment;
import me.flooz.app.UI.Fragment.Home.FriendsFragment;
import me.flooz.app.UI.Fragment.Home.HomeBaseFragment;
import me.flooz.app.UI.Fragment.Home.NewFloozFragment;
import me.flooz.app.UI.Fragment.Home.TransactionCardFragment;
import me.flooz.app.UI.Fragment.Home.TransactionSelectReceiverFragment;
import uk.co.senab.photoview.PhotoViewAttacher;

import com.commonsware.cwac.camera.CameraHost;
import com.commonsware.cwac.camera.CameraHostProvider;

import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.jeremyfeinstein.slidingmenu.lib.app.SlidingFragmentActivity;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.core.listener.ImageLoadingProgressListener;

import org.json.JSONArray;
import org.json.JSONException;

public class HomeActivity extends SlidingFragmentActivity implements CameraHostProvider
{
    public Map<String, HomeBaseFragment> contentFragments;

    public static final int RESULT_SCANPAY_ACTIVITY = 4;

    private HomeActivity instance;

    private FragmentManager fragmentManager;
    private List<HomeBaseFragment> fragmentHistory;
    private Boolean isLeftMenuWasOpen = false;
    private Boolean isRightMenuWasOpen = false;

    private ImageView tutoView;
    private RelativeLayout imageViewer;
    private ImageView imageViewerClose;
    private ProgressBar imageViewerProgress;
    private ImageView imageViewerImage;
    private PhotoViewAttacher imageViewerAttacher;

    private RelativeLayout transactionCardContainer;

    private TransactionCardFragment transactionCardFragment;

    private Fragment leftMenu;
    private Fragment rightMenu;

    public FloozApplication floozApp;

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

        final Intent intent = getIntent();
        JSONArray triggersArray = null;
        if (intent != null && intent.getAction() != null && intent.getAction().contentEquals(Intent.ACTION_VIEW)) {
            if (!FloozRestClient.getInstance().autologin()) {
                FloozApplication.getInstance().displayStartView();
                this.finish();
            }
        }
        if (intent != null && intent.getExtras() != null) {
            if (!FloozRestClient.getInstance().autologin()) {
                FloozApplication.getInstance().displayStartView();
                this.finish();
            }

            String message = intent.getExtras().getString("triggers");
            try {
                triggersArray = new JSONArray(message);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            if (triggersArray != null) {
                Handler mainHandler = new Handler(this.getMainLooper());
                final JSONArray finalJsonObject = triggersArray;
                mainHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (floozApp.getCurrentActivity() instanceof HomeActivity) {
                            for (int i = 0; i < finalJsonObject.length(); i++) {
                                FloozRestClient.getInstance().handleTrigger(new FLTrigger(finalJsonObject.optJSONObject(i)));
                            }
                        }
                    }
                }, 500);
            }
        }

        floozApp = (FloozApplication)this.getApplicationContext();

        this.fragmentManager = this.getFragmentManager();

        this.contentFragments = new HashMap<>();

        this.leftMenu = new ProfileFragment();
        this.rightMenu = new FriendsFragment();

        ((FriendsFragment)this.rightMenu).parentActivity = this;
        ((ProfileFragment)this.leftMenu).parentActivity = this;

        this.setContentView(R.layout.activity_home);

        this.tutoView = (ImageView) this.findViewById(R.id.main_tuto);
        this.imageViewer = (RelativeLayout) this.findViewById(R.id.main_image_container);
        this.imageViewerClose = (ImageView) this.findViewById(R.id.main_image_close);
        this.imageViewerProgress = (ProgressBar) this.findViewById(R.id.main_image_progress);
        this.imageViewerImage = (ImageView) this.findViewById(R.id.main_image_image);

        this.transactionCardContainer = (RelativeLayout) this.findViewById(R.id.main_transac_container);

        this.imageViewerAttacher = new PhotoViewAttacher(this.imageViewerImage);

        this.imageViewer.setClickable(true);

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

        this.fragmentHistory = new ArrayList<>();

        this.contentFragments.put("main", new FloozFragment());
        this.contentFragments.put("create", new NewFloozFragment());
        this.contentFragments.put("select_user", new TransactionSelectReceiverFragment());

        for (Map.Entry<String, HomeBaseFragment> entry : this.contentFragments.entrySet()) {
            entry.getValue().parentActivity = this;
        }

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
        sm.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);

        sm.setMenu(R.layout.left_sidemenu);
        sm.setSecondaryMenu(R.layout.right_sidemenu);
        sm.setSlidingEnabled(false);

        sm.setOnCloseListener(new SlidingMenu.OnCloseListener() {
            @Override
            public void onClose() {
                getSlidingMenu().setSlidingEnabled(false);
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(getWindow().getDecorView().getRootView().getWindowToken(), 0);
            }
        });

        this.pushMainFragment("main");
        this.changeLeftMenuFragment(this.leftMenu, false);
        this.changeRightMenuFragment(this.rightMenu, false);
    }

    protected void onResume() {
        super.onResume();
        floozApp.setCurrentActivity(this);
        Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                FloozApplication.mixpanelAPI.getPeople().showSurveyIfAvailable(instance);
            }
        }, 1500);

        LocalBroadcastManager.getInstance(FloozApplication.getAppContext()).registerReceiver(showLeftMenu,
                CustomNotificationIntents.filterShowSlidingLeftMenu());

        LocalBroadcastManager.getInstance(FloozApplication.getAppContext()).registerReceiver(showRightMenu,
                CustomNotificationIntents.filterShowSlidingRightMenu());

        LocalBroadcastManager.getInstance(FloozApplication.getAppContext()).registerReceiver(enableMenu,
                CustomNotificationIntents.filterEnableSlidingMenu());

        LocalBroadcastManager.getInstance(FloozApplication.getAppContext()).registerReceiver(disableMenu,
                CustomNotificationIntents.filterDisableSlidingMenu());

        class DownloadContactsTask extends AsyncTask<URL, Integer, Long> {
            protected void onPreExecute() {
                ContactsManager.allContacts = null;
            }

            protected Long doInBackground(URL... urls) {
                ContactsManager.getContactsList();
                return null;
            }
        }

        new DownloadContactsTask().execute();
    }

    protected void onPause() {
        clearReferences();
        super.onPause();

        LocalBroadcastManager.getInstance(FloozApplication.getAppContext()).unregisterReceiver(showLeftMenu);
        LocalBroadcastManager.getInstance(FloozApplication.getAppContext()).unregisterReceiver(showRightMenu);
        LocalBroadcastManager.getInstance(FloozApplication.getAppContext()).unregisterReceiver(enableMenu);
        LocalBroadcastManager.getInstance(FloozApplication.getAppContext()).unregisterReceiver(disableMenu);
    }

    protected void onDestroy() {
        clearReferences();
        super.onDestroy();
    }

    private void clearReferences(){
        Activity currActivity = floozApp.getCurrentActivity();
        if (currActivity != null && currActivity.equals(this))
            floozApp.setCurrentActivity(null);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

    }

    public void pushMainFragment(String fragmentId) {
        if (this.contentFragments.containsKey(fragmentId)) {
            HomeBaseFragment fragment = this.contentFragments.get(fragmentId);

            if (this.fragmentHistory.contains(fragment)) {
                if (this.fragmentHistory.indexOf(fragment) == this.fragmentHistory.size() - 1)
                    this.changeMainFragment(fragmentId);
                return;
            }

            if (this.imageViewer.getVisibility() == View.VISIBLE) {
                this.imageViewerClose.performClick();
            }
            if (this.transactionCardContainer.getVisibility() == View.VISIBLE) {
                this.hideTransactionCard();
            }

            InputMethodManager imm = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(this.getWindow().getDecorView().getRootView().getWindowToken(), 0);

            if (this.fragmentHistory.size() == 1) {
                this.isRightMenuWasOpen = this.getSlidingMenu().isSecondaryMenuShowing();
                if (!this.isRightMenuWasOpen)
                    this.isLeftMenuWasOpen = this.getSlidingMenu().isMenuShowing();
            }
            this.getSlidingMenu().showContent(true);
            this.getSlidingMenu().setSlidingEnabled(false);

            this.fragmentHistory.add(fragment);

            FragmentTransaction ft = this.fragmentManager.beginTransaction();

            ft.add(R.id.main_fragment_container, fragment);
            ft.addToBackStack(null);
            ft.commit();
        }
    }

    public void pushMainFragment(String fragmentId, int animIn, int animOut) {
        if (this.contentFragments.containsKey(fragmentId)) {
            HomeBaseFragment fragment = this.contentFragments.get(fragmentId);

            if (this.fragmentHistory.contains(fragment)) {
                if (this.fragmentHistory.indexOf(fragment) == this.fragmentHistory.size() - 1)
                    this.changeMainFragment(fragmentId, animIn, animOut);
                return;
            }

            if (this.imageViewer.getVisibility() == View.VISIBLE) {
                this.imageViewerClose.performClick();
            }
            if (this.transactionCardContainer.getVisibility() == View.VISIBLE) {
                this.hideTransactionCard();
            }

            InputMethodManager imm = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(this.getWindow().getDecorView().getRootView().getWindowToken(), 0);

            if (this.fragmentHistory.size() == 1) {
                this.isRightMenuWasOpen = this.getSlidingMenu().isSecondaryMenuShowing();
                if (!this.isRightMenuWasOpen)
                    this.isLeftMenuWasOpen = this.getSlidingMenu().isMenuShowing();
            }

            this.getSlidingMenu().showContent(true);
            this.getSlidingMenu().setSlidingEnabled(false);

            this.fragmentHistory.add(fragment);

            FragmentTransaction ft = this.fragmentManager.beginTransaction();

            ft.setCustomAnimations(animIn, animOut);

            ft.add(R.id.main_fragment_container, fragment);
            ft.addToBackStack(null);
            ft.commit();
        }
    }

    public void popMainFragment() {
        if (this.fragmentHistory.size() > 1) {
            InputMethodManager imm = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(this.getWindow().getDecorView().getRootView().getWindowToken(), 0);

            HomeBaseFragment fragment = this.fragmentHistory.get(this.fragmentHistory.size() - 1);

            FragmentTransaction ft = this.fragmentManager.beginTransaction();

            ft.remove(fragment);
            ft.addToBackStack(null);
            ft.commit();

            this.fragmentHistory.remove(this.fragmentHistory.size() - 1);

            if (this.fragmentHistory.size() == 1) {
                if (this.isLeftMenuWasOpen) {
                    this.getSlidingMenu().showMenu(true);
                    this.getSlidingMenu().setSlidingEnabled(true);
                } else if (this.isRightMenuWasOpen) {
//                    this.getSlidingMenu().showSecondaryMenu(true);
//                    this.getSlidingMenu().setSlidingEnabled(true);
                }

                this.isRightMenuWasOpen = false;
                this.isLeftMenuWasOpen = false;
            }
        }
    }

    public void popMainFragment(int animOut, int animIn) {
        if (this.fragmentHistory.size() > 1) {
            InputMethodManager imm = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(this.getWindow().getDecorView().getRootView().getWindowToken(), 0);

            HomeBaseFragment fragment = this.fragmentHistory.get(this.fragmentHistory.size() - 1);

            FragmentTransaction ft = this.fragmentManager.beginTransaction();

            ft.setCustomAnimations(animIn, animOut);
            ft.remove(fragment);
            ft.addToBackStack(null);
            ft.commit();

            this.fragmentHistory.remove(this.fragmentHistory.size() - 1);

            if (this.fragmentHistory.size() == 1) {
                if (this.isLeftMenuWasOpen) {
                    this.getSlidingMenu().showMenu(true);
                    this.getSlidingMenu().setSlidingEnabled(true);
                } else if (this.isRightMenuWasOpen) {
//                    this.getSlidingMenu().showSecondaryMenu(true);
//                    this.getSlidingMenu().setSlidingEnabled(true);
                }

                this.isRightMenuWasOpen = false;
                this.isLeftMenuWasOpen = false;
            }
        }
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

    public void changeMainFragment(String fragmentId)
    {
        if (this.contentFragments.containsKey(fragmentId)) {
            this.popMainFragment();
            this.pushMainFragment(fragmentId);
        }
    }

    public void changeMainFragment(String fragmentId, int animIn, int animOut)
    {
        if (this.contentFragments.containsKey(fragmentId)) {
            this.popMainFragment();
            this.pushMainFragment(fragmentId, animIn, animOut);
        }
    }

    public void showTransactionCard(FLTransaction transaction) {
        this.showTransactionCard(transaction, false);
    }

    public void showTransactionCard(FLTransaction transaction, Boolean insertComment) {
        this.transactionCardFragment.insertComment = insertComment;
        this.transactionCardFragment.setTransaction(transaction);

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

    public void showImageViewer(final String url) {
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
                }, new ImageLoadingProgressListener() {
                    @Override
                    public void onProgressUpdate(String imageUri, View view, int current, int total) {
                        float tmp = current;
                        tmp /= total;
                        tmp *= 100;

                        imageViewerProgress.setProgress((int)tmp);
                    }
                });
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        this.imageViewer.startAnimation(anim);
    }

    public void hideImageViewer() {
        this.imageViewerClose.performClick();
    }

    @Override
    public CameraHost getCameraHost() {
        return(new CustomCameraHost(this));
    }

    @Override
    public void onBackPressed() {
        if (this.imageViewer.getVisibility() == View.VISIBLE) {
            this.imageViewerClose.performClick();
        } else if (this.transactionCardContainer.getVisibility() == View.VISIBLE) {
            this.hideTransactionCard();
        } else if (this.tutoView.getVisibility() == View.VISIBLE) {

        } else if (this.getSlidingMenu().isMenuShowing()) {
            this.getSlidingMenu().showContent(true);
            this.getSlidingMenu().setSlidingEnabled(false);
        } else if (this.getSlidingMenu().isSecondaryMenuShowing()) {
            this.getSlidingMenu().showContent(true);
            this.getSlidingMenu().setSlidingEnabled(false);
        } else if (this.fragmentHistory.size() > 0) {
            this.fragmentHistory.get(this.fragmentHistory.size() - 1).onBackPressed();
        } else {
            this.finish();
        }
    }
}
