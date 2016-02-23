package me.flooz.app.UI.Activity;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import me.flooz.app.App.FloozApplication;
import me.flooz.app.R;
import me.flooz.app.UI.Fragment.Home.Authentication.AuthenticationBaseFragment;
import me.flooz.app.UI.Fragment.Home.Authentication.AuthenticationChangePassFragment;
import me.flooz.app.UI.Fragment.Home.Authentication.AuthenticationPassFragment;
import me.flooz.app.UI.Fragment.Home.Authentication.AuthenticationResetCodeFragment;
import me.flooz.app.UI.Fragment.Home.Authentication.AuthenticationSecureCodeFragment;
import me.flooz.app.Utils.CustomFonts;
import me.flooz.app.Utils.FLHelper;
import me.flooz.app.Utils.FLTriggerManager;
import me.flooz.app.Utils.ViewServer;

/**
 * Created by Flooz on 3/9/15.
 */
public class AuthenticationActivity extends BaseActivity {

    private FloozApplication floozApp;

    public static int RESULT_AUTHENTICATION_ACTIVITY = 12;

    public enum AuthenticationPageIdentifier {
        AuthenticationSecureCode,
        AuthenticationPass,
        AuthenticationResetCode,
        AuthenticationResetCodeConfirm,
        AuthenticationChangePass
    }

    private RelativeLayout header;
    private TextView headerTitle;
    private ImageView headerBackButton;
    private ImageView headerTitleImg;

    private AuthenticationPageIdentifier currentPage;
    private AuthenticationBaseFragment currentFragment;

    private JSONObject triggerData = null;

    public Boolean changeSecureCode = false;

    public int resultCode = -1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (FLHelper.isDebuggable())
            ViewServer.get(this).addWindow(this);

        if (getIntent() != null && getIntent().hasExtra("triggerData"))
            try {
                this.triggerData = new JSONObject(getIntent().getStringExtra("triggerData"));
            } catch (JSONException e) {
                e.printStackTrace();
            }

        floozApp = (FloozApplication)this.getApplicationContext();

        changeSecureCode = getIntent().getBooleanExtra("changeSecureCode", false);

        this.setContentView(R.layout.authentication_fragment);

        this.header = (RelativeLayout)this.findViewById(R.id.authentication_header);
        this.headerTitle = (TextView)this.findViewById(R.id.authentication_header_title);
        this.headerBackButton = (ImageView)this.findViewById(R.id.authentication_header_back);
        this.headerTitleImg = (ImageView)this.findViewById(R.id.authentication_header_title_img);

        this.headerTitle.setTypeface(CustomFonts.customTitleExtraLight(this));
        this.headerBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                backToPreviousPage();
            }
        });

        this.currentPage = AuthenticationPageIdentifier.AuthenticationSecureCode;

        this.displayCurrentPage();
    }

    public void displayCurrentPage() {
        InputMethodManager imm = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(this.getWindow().getDecorView().getRootView().getWindowToken(), 0);

        Fragment fragment = this.getFragmentForCurrentPage();

        FragmentTransaction ft = this.getFragmentManager().beginTransaction();

        ft.setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out);

        ft.replace(R.id.authentication_fragment_container, fragment);
        ft.addToBackStack(null).commit();

        this.changeTitleForCurrentPage();
    }


    public void changeCurrentPage(AuthenticationPageIdentifier page, int animIn, int animOut) {
        this.currentPage = page;
        InputMethodManager imm = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(this.getWindow().getDecorView().getRootView().getWindowToken(), 0);

        Fragment fragment = this.getFragmentForCurrentPage();

        FragmentTransaction ft = this.getFragmentManager().beginTransaction();

        ft.setCustomAnimations(animIn, animOut);

        ft.replace(R.id.authentication_fragment_container, fragment);
        ft.addToBackStack(null).commit();

        this.changeTitleForCurrentPage();
    }

    private void changeTitleForCurrentPage() {

        String title = "";

        switch (this.currentPage) {
            case AuthenticationSecureCode:
                break;
            case AuthenticationPass:
                break;
            case AuthenticationResetCode:
                break;
            case AuthenticationResetCodeConfirm:
                break;
            case AuthenticationChangePass:
                break;
        }

        if (title.isEmpty()) {
            this.headerTitle.setVisibility(View.GONE);
            this.headerTitleImg.setVisibility(View.VISIBLE);
        }
        else {
            this.headerTitle.setText(title);
            this.headerTitle.setVisibility(View.VISIBLE);
            this.headerTitleImg.setVisibility(View.GONE);
        }
    }

    private Fragment getFragmentForCurrentPage() {

        AuthenticationBaseFragment fragment = null;

        switch (this.currentPage) {
            case AuthenticationSecureCode:
                fragment = new AuthenticationSecureCodeFragment();
                this.hideHeader();
                break;
            case AuthenticationPass:
                fragment = new AuthenticationPassFragment();
                this.showHeader();
                break;
            case AuthenticationResetCode:
                fragment = new AuthenticationResetCodeFragment();
                this.showHeader();
                break;
            case AuthenticationResetCodeConfirm:
                String lastCode = ((AuthenticationResetCodeFragment)this.currentFragment).currentCode;
                fragment = new AuthenticationResetCodeFragment();
                ((AuthenticationResetCodeFragment)fragment).confirmMode = true;
                ((AuthenticationResetCodeFragment)fragment).lastCode = lastCode;
                break;
            case AuthenticationChangePass:
                fragment = new AuthenticationChangePassFragment();
                break;
            default:
                break;
        }

        fragment.parentActivity = this;

        this.currentFragment = fragment;

        return fragment;
    }

    public void hideHeader() {
        if (this.header.getVisibility() != View.GONE)
            this.header.setVisibility(View.GONE);
    }

    public void showHeader() {
        if (this.header.getVisibility() != View.VISIBLE)
            this.header.setVisibility(View.VISIBLE);
    }

    public void hideBackButton() {
        if (this.headerBackButton.getVisibility() != View.GONE)
            this.headerBackButton.setVisibility(View.GONE);
    }

    public void showBackButton() {
        if (this.headerBackButton.getVisibility() != View.VISIBLE)
            this.headerBackButton.setVisibility(View.VISIBLE);
    }

    public void gotToNextPage() {
        switch (this.currentPage) {
            case AuthenticationSecureCode:
                this.currentPage = AuthenticationPageIdentifier.AuthenticationPass;
                break;
            case AuthenticationPass:
                this.currentPage = AuthenticationPageIdentifier.AuthenticationResetCode;
                break;
            case AuthenticationResetCode:
                this.currentPage = AuthenticationPageIdentifier.AuthenticationResetCodeConfirm;
                break;
            case AuthenticationResetCodeConfirm:
                break;
            case AuthenticationChangePass:
                break;
        }

        InputMethodManager imm = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(this.getWindow().getDecorView().getRootView().getWindowToken(), 0);

        Fragment fragment = this.getFragmentForCurrentPage();

        FragmentTransaction ft = this.getFragmentManager().beginTransaction();

        ft.setCustomAnimations(R.animator.slide_in_left, R.animator.slide_out_right);

        ft.replace(R.id.authentication_fragment_container, fragment);
        ft.addToBackStack(null).commit();

        this.changeTitleForCurrentPage();
    }

    public void backToPreviousPage() {
        switch (this.currentPage) {
            case AuthenticationSecureCode:
                this.dismissView(false);
                return;
            case AuthenticationPass:
                if (this.changeSecureCode) {
                    this.dismissView(false);
                    return;
                }
                else
                    this.currentPage = AuthenticationPageIdentifier.AuthenticationSecureCode;
                break;
            case AuthenticationResetCode:
                this.dismissView(false);
                return;
            case AuthenticationResetCodeConfirm:
                this.currentPage = AuthenticationPageIdentifier.AuthenticationResetCode;
                break;
            case AuthenticationChangePass:
                break;
        }

        InputMethodManager imm = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(this.getWindow().getDecorView().getRootView().getWindowToken(), 0);

        Fragment fragment = this.getFragmentForCurrentPage();

        FragmentTransaction ft = this.getFragmentManager().beginTransaction();

        ft.setCustomAnimations(R.animator.slide_in_right, R.animator.slide_out_left);

        ft.replace(R.id.authentication_fragment_container, fragment);
        ft.addToBackStack(null).commit();

        this.changeTitleForCurrentPage();
    }

    public void validateTransaction() {
        if (changeSecureCode && currentPage == AuthenticationPageIdentifier.AuthenticationSecureCode) {
            this.changeCurrentPage(AuthenticationPageIdentifier.AuthenticationResetCode, R.animator.slide_in_left, R.animator.slide_out_right);
        } else if (changeSecureCode && currentPage == AuthenticationPageIdentifier.AuthenticationResetCodeConfirm) {
            this.dismissView(true);
        } else if (!changeSecureCode) {
            this.dismissView(true);
        }
    }

    public void dismissView(Boolean validate) {
        if (validate) {
            this.resultCode = Activity.RESULT_OK;
            setResult(Activity.RESULT_OK, null);
        } else {
            this.resultCode = Activity.RESULT_CANCELED;
            setResult(Activity.RESULT_CANCELED, null);
        }

        finish();
        overridePendingTransition(android.R.anim.fade_in, R.anim.slide_down);
    }

    @Override
    public void onResume() {
        super.onResume();

        if (FLHelper.isDebuggable())
            ViewServer.get(this).setFocusedWindow(this);

        floozApp.setCurrentActivity(this);
    }

    @Override
    public void onStop() {
        super.onStop();

        if (this.resultCode == RESULT_OK) {
            if (this.triggerData != null && this.triggerData.has("success")) {
                FLTriggerManager.getInstance().executeTriggerList(FLTriggerManager.convertTriggersJSONArrayToList(this.triggerData.optJSONArray("success")));
            }
        }
    }

    @Override
    public void onPause() {
        clearReferences();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        clearReferences();

        if (FLHelper.isDebuggable())
            ViewServer.get(this).removeWindow(this);

        super.onDestroy();
    }

    @Override
    public void onBackPressed(){
        backToPreviousPage();
    }

    private void clearReferences(){
        Activity currActivity = floozApp.getCurrentActivity();
        if (currActivity != null && currActivity.equals(this))
            floozApp.setCurrentActivity(null);
    }

}
