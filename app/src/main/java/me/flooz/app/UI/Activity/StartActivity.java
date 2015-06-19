package me.flooz.app.UI.Activity;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.facebook.Session;

import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.flooz.app.App.FloozApplication;
import me.flooz.app.R;
import me.flooz.app.UI.Fragment.Start.StartBaseFragment;
import me.flooz.app.UI.Fragment.Start.StartHomeFragment;
import me.flooz.app.UI.Fragment.Start.StartSignupFragment;
import me.flooz.app.Utils.FLHelper;
import me.flooz.app.Utils.ViewServer;

/**
 * Created by Flooz on 9/17/14.
 */
public class StartActivity extends Activity {

    public FloozApplication floozApp;
    private StartActivity instance;

    private StartBaseFragment currentFragment;
    public List<StartBaseFragment> fragmentHistory;

    public Map<String, Object> signupData;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (FLHelper.isDebuggable())
            ViewServer.get(this).addWindow(this);

        setContentView(R.layout.start_activity);
        floozApp = (FloozApplication)this.getApplicationContext();
        instance = this;

        this.fragmentHistory = new ArrayList<>();

        getWindow().setBackgroundDrawableResource(R.drawable.launch_background) ;

        this.signupData = new HashMap<>();

        StartHomeFragment homeFragment = new StartHomeFragment();
        this.changeCurrentPage(homeFragment, false);
    }

    protected void onResume() {
        super.onResume();

        if (FLHelper.isDebuggable())
            ViewServer.get(this).setFocusedWindow(this);

        floozApp.setCurrentActivity(this);

    }

    protected void onPause() {
        clearReferences();
        super.onPause();
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

    public void changeCurrentPage(StartBaseFragment fragment, Boolean addToBackStack) {
        this.hideKeyboard();

        fragment.parentActivity = this;

        FragmentTransaction ft = this.getFragmentManager().beginTransaction();

        ft.replace(R.id.fragment_container, fragment);

        if (addToBackStack)
            ft.addToBackStack(null);

        ft.commit();

        this.fragmentHistory.add(fragment);
        this.currentFragment = fragment;
    }

    public void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(this.getWindow().getDecorView().getRootView().getWindowToken(), 0);
    }

    public void changeCurrentPage(StartBaseFragment fragment, int animEnter, int animExit, int animPopEnter, int animPopExit, Boolean addToBackStack) {
        this.hideKeyboard();

        fragment.parentActivity = this;

        FragmentTransaction ft = this.getFragmentManager().beginTransaction();

        ft.setCustomAnimations(animEnter, animExit, animPopEnter, animPopExit);

        ft.replace(R.id.fragment_container, fragment);

        if (addToBackStack)
            ft.addToBackStack(null);

        ft.commit();

        this.fragmentHistory.add(fragment);
        this.currentFragment = fragment;
    }

    public void changeCurrentPage(StartBaseFragment fragment, int animIn, int animOut, Boolean addToBackStack) {
        this.hideKeyboard();

        fragment.parentActivity = this;

        FragmentTransaction ft = this.getFragmentManager().beginTransaction();

        ft.setCustomAnimations(animIn, animOut, animIn, animOut);

        ft.replace(R.id.fragment_container, fragment);

        if (addToBackStack)
            ft.addToBackStack(null);

        ft.commit();

        this.fragmentHistory.add(fragment);
        this.currentFragment = fragment;
    }

    public void updateUserData(Map<String, Object> data) {

        this.signupData.putAll(data);

        if (fragmentHistory.get(fragmentHistory.size() - 1) instanceof StartSignupFragment) {
            fragmentHistory.get(fragmentHistory.size() - 1).refreshView();
        } else {
            FragmentManager fm = getFragmentManager();
            for(int i = 0; i < fm.getBackStackEntryCount(); ++i) {
                fm.popBackStack();
                fragmentHistory.remove(fragmentHistory.size() - 1);
            }

            this.changeCurrentPage(new StartSignupFragment(), android.R.animator.fade_in, android.R.animator.fade_out, true);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (Session.getActiveSession() != null)
            Session.getActiveSession().onActivityResult(this, requestCode, resultCode, data);
    }

    @Override
    public void onBackPressed() {
        if (this.fragmentHistory.size() > 1) {
            super.onBackPressed();
            this.fragmentHistory.remove(this.fragmentHistory.size() - 1);
        } else {
            this.finish();
        }
    }
}