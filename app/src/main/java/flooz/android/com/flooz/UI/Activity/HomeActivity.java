package flooz.android.com.flooz.UI.Activity;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;

import java.util.HashMap;
import java.util.Map;

import flooz.android.com.flooz.App.FloozApplication;
import flooz.android.com.flooz.R;
import flooz.android.com.flooz.UI.Fragment.FloozFragment;
import flooz.android.com.flooz.UI.Fragment.FriendsFragment;
import flooz.android.com.flooz.UI.Fragment.ProfileFragment;
import flooz.android.com.flooz.Utils.CustomNotificationIntents;

import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.jeremyfeinstein.slidingmenu.lib.app.SlidingFragmentActivity;

public class HomeActivity extends SlidingFragmentActivity
{
    private FragmentManager fragmentManager;
    private Map<String, Fragment> contentFragments;

    private Fragment leftMenu;
    private Fragment rightMenu;

    private BroadcastReceiver showLeftMenu = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            getSlidingMenu().showMenu(true);
        }
    };

    private BroadcastReceiver showRightMenu = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            getSlidingMenu().showSecondaryMenu(true);
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

        this.fragmentManager = this.getFragmentManager();

        this.contentFragments = new HashMap<String, Fragment>();

        this.contentFragments.put("main", new FloozFragment());

        this.leftMenu = new ProfileFragment();
        this.rightMenu = new FriendsFragment();

        setContentView(R.layout.activity_home);
        setBehindContentView(R.layout.left_sidemenu);

        SlidingMenu sm = getSlidingMenu();
        sm.setShadowWidthRes(R.dimen.slidingmenu_shadow_width);
        sm.setShadowDrawable(R.drawable.slidemenu_shadow);
        sm.setBehindOffsetRes(R.dimen.slidingmenu_offset);
        sm.setFadeDegree(0.35f);
        sm.setMode(SlidingMenu.LEFT_RIGHT);
        sm.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);

        sm.setMenu(R.layout.left_sidemenu);
        sm.setSecondaryMenu(R.layout.right_sidemenu);

        this.changeMainFragment("main", false);
        this.changeLeftMenuFragment(new ProfileFragment(), false);
        this.changeRightMenuFragment(new FriendsFragment(), false);

        LocalBroadcastManager.getInstance(FloozApplication.getAppContext()).registerReceiver(showLeftMenu,
                CustomNotificationIntents.filterShowSlidingLeftMenu());

        LocalBroadcastManager.getInstance(FloozApplication.getAppContext()).registerReceiver(showRightMenu,
                CustomNotificationIntents.filterShowSlidingRightMenu());

        LocalBroadcastManager.getInstance(FloozApplication.getAppContext()).registerReceiver(enableMenu,
                CustomNotificationIntents.filterEnableSlidingMenu());

        LocalBroadcastManager.getInstance(FloozApplication.getAppContext()).registerReceiver(disableMenu,
                CustomNotificationIntents.filterDisableSlidingMenu());
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

    }

    private void changeLeftMenuFragment(Fragment fragment, Boolean anim)
    {
        if (fragment == null || fragment.isAdded())
            return;

        FragmentTransaction ft = this.fragmentManager.beginTransaction();

        if (anim)
            ft.setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out);

        ft.replace(R.id.left_menu_frame, fragment);
        ft.commit();
        this.fragmentManager.executePendingTransactions();
    }

    private void changeRightMenuFragment(Fragment fragment, Boolean anim)
    {
        if (fragment == null || fragment.isAdded())
            return;

        FragmentTransaction ft = this.fragmentManager.beginTransaction();

        if (anim)
            ft.setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out);

        ft.replace(R.id.right_menu_frame, fragment);
        ft.commit();
        this.fragmentManager.executePendingTransactions();
    }

    private void changeMainFragment(String fragmentIdentifier, Boolean anim)
    {
        Fragment fragment = this.contentFragments.get(fragmentIdentifier);

        if (fragment == null || fragment.isAdded())
            return;

        FragmentTransaction ft = this.fragmentManager.beginTransaction();

        if (anim)
            ft.setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out);

        ft.replace(R.id.main_fragment_container, fragment);
        ft.commit();
        this.fragmentManager.executePendingTransactions();
    }

}
