package flooz.android.com.flooz.UI.Activity;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.facebook.Session;

import flooz.android.com.flooz.R;
import flooz.android.com.flooz.Model.FLUser;
import flooz.android.com.flooz.App.FloozApplication;
import flooz.android.com.flooz.UI.Fragment.Signin.SigninBaseFragment;
import flooz.android.com.flooz.UI.Fragment.Signin.SigninCodeFragment;
import flooz.android.com.flooz.UI.Fragment.Signin.SigninHomeFragment;
import flooz.android.com.flooz.UI.Fragment.Signin.SigninPassFragment;
import flooz.android.com.flooz.Utils.CustomFonts;

/**
 * Created by Flooz on 11/25/14.
 */
public class SigninActivity extends FragmentActivity {

    public FloozApplication floozApp;

    public FLUser userData = new FLUser();

    public enum SigninPageIdentifier {
        SigninHome,
        SigninCode,
        SigninConfirmCode,
        SigninResetPass
    };

    public RelativeLayout header;
    public TextView headerTitle;
    public ImageView headerBackButton;
    public ImageView headerTitleImg;

    public SigninPageIdentifier currentPage;
    public SigninBaseFragment currentFragment;

    public Boolean userHasSecureCode;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        floozApp = (FloozApplication)this.getApplicationContext();

        setContentView(R.layout.signin_activity);

        this.currentPage = SigninPageIdentifier.values()[getIntent().getIntExtra("page", SigninPageIdentifier.SigninHome.ordinal())];
        if (getIntent().getStringExtra("phone") != null && getIntent().getStringExtra("phone").length() > 0)
            this.userData.phone = getIntent().getStringExtra("phone");
        else
            this.userData.phone = "";

        if (this.userData.phone.startsWith("+33"))
            this.userData.phone = this.userData.phone.replace("+33", "0");

        this.userHasSecureCode = getIntent().getBooleanExtra("secureCode", true);

        this.header = (RelativeLayout)this.findViewById(R.id.signin_header);
        this.headerTitle = (TextView)this.findViewById(R.id.signin_header_title);
        this.headerBackButton = (ImageView)this.findViewById(R.id.signin_header_back);
        this.headerTitleImg = (ImageView)this.findViewById(R.id.signin_header_title_img);

        this.headerTitle.setTypeface(CustomFonts.customTitleExtraLight(this.getApplicationContext()));
        this.headerBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                backToPreviousPage();
            }
        });

        this.displayCurrentPage();
    }

    protected void onResume() {
        super.onResume();
        floozApp.setCurrentActivity(this);
    }
    protected void onPause() {
        clearReferences();
        super.onPause();
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

    public void displayCurrentPage() {
        InputMethodManager imm = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(this.getWindow().getDecorView().getRootView().getWindowToken(), 0);

        Fragment fragment = this.getFragmentForCurrentPage();

        FragmentTransaction ft = this.getFragmentManager().beginTransaction();

        ft.setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out);

        ft.replace(R.id.signin_fragment_container, fragment);
        ft.addToBackStack(null).commit();

        this.changeTitleForCurrentPage();
    }


    public void changeCurrentPage(SigninPageIdentifier page, int animIn, int animOut) {
        this.currentPage = page;
        InputMethodManager imm = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(this.getWindow().getDecorView().getRootView().getWindowToken(), 0);

        Fragment fragment = this.getFragmentForCurrentPage();

        FragmentTransaction ft = this.getFragmentManager().beginTransaction();

        ft.setCustomAnimations(animIn, animOut);

        ft.replace(R.id.signin_fragment_container, fragment);
        ft.addToBackStack(null).commit();

        this.changeTitleForCurrentPage();
    }

    private void changeTitleForCurrentPage() {

        String title = "";

        switch (this.currentPage) {
            case SigninHome:;
                break;
            case SigninCode:
                break;
            case SigninConfirmCode:
                break;
            case SigninResetPass:
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

        SigninBaseFragment fragment = null;

        switch (this.currentPage) {
            case SigninHome:
                fragment = new SigninHomeFragment();
                break;
            case SigninCode:
                fragment = new SigninCodeFragment();
                ((SigninCodeFragment)fragment).confirmMode = false;
                break;
            case SigninConfirmCode:
                fragment = new SigninCodeFragment();
                ((SigninCodeFragment)fragment).confirmMode = true;
                break;
            case SigninResetPass:
                fragment = new SigninPassFragment();
                break;
            default:
                break;
        }

        fragment.parentActivity = this;

        this.currentFragment = fragment;

        return fragment;
    }

    public void hideHeader() {
        this.header.setVisibility(View.GONE);
    }

    public void showHeader() {
        this.header.setVisibility(View.VISIBLE);
    }

    public void hideBackButton() {
        this.headerBackButton.setVisibility(View.GONE);
    }

    public void showBackButton() {
        this.headerBackButton.setVisibility(View.VISIBLE);
    }

    public void gotToNextPage() {
        switch (this.currentPage) {
            case SigninHome:
                break;
            case SigninCode:
                this.currentPage = SigninPageIdentifier.SigninConfirmCode;
                break;
            case SigninConfirmCode:
                break;
            case SigninResetPass:
                break;
        }

        InputMethodManager imm = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(this.getWindow().getDecorView().getRootView().getWindowToken(), 0);

        Fragment fragment = this.getFragmentForCurrentPage();

        FragmentTransaction ft = this.getFragmentManager().beginTransaction();

        ft.setCustomAnimations(R.animator.slide_in_left, R.animator.slide_out_right);

        ft.replace(R.id.signin_fragment_container, fragment);
        ft.addToBackStack(null).commit();

        this.changeTitleForCurrentPage();
    }

    public void backToPreviousPage() {
        switch (this.currentPage) {
            case SigninHome:
                Intent intent = new Intent();
                intent.setClass(this.getApplicationContext(), SignupActivity.class);
                intent.putExtra("page", SignupActivity.SignupPageIdentifier.SignupPhone.ordinal());
                intent.putExtra("signup", true);
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                return;
            case SigninResetPass:
                Intent intent2 = new Intent();
                intent2.setClass(this.getApplicationContext(), SignupActivity.class);
                intent2.putExtra("page", SignupActivity.SignupPageIdentifier.SignupPhone.ordinal());
                intent2.putExtra("signup", true);
                startActivity(intent2);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            case SigninCode:
                break;
            case SigninConfirmCode:
                this.currentPage = SigninPageIdentifier.SigninCode;
                break;
        }

        InputMethodManager imm = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(this.getWindow().getDecorView().getRootView().getWindowToken(), 0);

        Fragment fragment = this.getFragmentForCurrentPage();

        FragmentTransaction ft = this.getFragmentManager().beginTransaction();

        ft.setCustomAnimations(R.animator.slide_in_right, R.animator.slide_out_left);

        ft.replace(R.id.signin_fragment_container, fragment);
        ft.addToBackStack(null).commit();

        this.changeTitleForCurrentPage();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Session.getActiveSession().onActivityResult(this, requestCode, resultCode, data);
    }

    @Override
    public void onBackPressed() {
        this.backToPreviousPage();
    }
}
