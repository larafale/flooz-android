package flooz.android.com.flooz.UI.Activity;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.commonsware.cwac.camera.CameraHost;
import com.commonsware.cwac.camera.CameraHostProvider;
import com.facebook.Session;

import flooz.android.com.flooz.App.FloozApplication;
import flooz.android.com.flooz.Model.FLUser;
import flooz.android.com.flooz.R;
import flooz.android.com.flooz.UI.Fragment.Signup.SignupBaseFragment;
import flooz.android.com.flooz.UI.Fragment.Signup.SignupCGUFragment;
import flooz.android.com.flooz.UI.Fragment.Signup.SignupImageCaptureFragment;
import flooz.android.com.flooz.UI.Fragment.Signup.SignupImageFragment;
import flooz.android.com.flooz.UI.Fragment.Signup.SignupImagePickerFragment;
import flooz.android.com.flooz.UI.Fragment.Signup.SignupInfosFragment;
import flooz.android.com.flooz.UI.Fragment.Signup.SignupPassFragment;
import flooz.android.com.flooz.UI.Fragment.Signup.SignupPhoneFragment;
import flooz.android.com.flooz.UI.Fragment.Signup.SignupSMSFragment;
import flooz.android.com.flooz.UI.Fragment.Signup.SignupUsernameFragment;
import flooz.android.com.flooz.Utils.CustomCameraHost;
import flooz.android.com.flooz.Utils.CustomFonts;
import scanpay.it.CreditCard;
import scanpay.it.ScanPay;

/**
 * Created by Flooz on 9/17/14.
 */
public class SignupActivity extends FragmentActivity implements CustomCameraHost.CustomCameraHostDelegate, CameraHostProvider {

    public FloozApplication floozApp;

    public FLUser userData = new FLUser();

    public static final int RESULT_SCANPAY_ACTIVITY = 4;

    public enum SignupPageIdentifier {
        SignupPhone,
        SignupSMS,
        SignupUsername,
        SignupImage,
        SignupInfos,
        SignupPass,
        SignupPassConfirm,
        SignupImageCapture,
        SignupImagePicker,
        SignupCGU,
    };

    public RelativeLayout header;
    public TextView headerTitle;
    public ImageView headerBackButton;

    public SignupPageIdentifier currentPage;
    public SignupBaseFragment currentFragment;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        floozApp = (FloozApplication)this.getApplicationContext();

        setContentView(R.layout.signup_activity);

        this.currentPage = SignupPageIdentifier.values()[getIntent().getIntExtra("page", SignupPageIdentifier.SignupPhone.ordinal())];
        if (getIntent().getStringExtra("phone") != null && getIntent().getStringExtra("phone").length() > 0) {
            this.userData.phone = getIntent().getStringExtra("phone");
            if (this.userData.phone.startsWith("+33"))
                this.userData.phone = this.userData.phone.replace("+33", "0");
        }

        if (getIntent().getStringExtra("coupon") != null && getIntent().getStringExtra("coupon").length() > 0)
            this.userData.coupon = getIntent().getStringExtra("coupon");

        this.userData.distinctId = floozApp.mixpanelAPI.getDistinctId();

        this.header = (RelativeLayout)this.findViewById(R.id.signup_header);
        this.headerTitle = (TextView)this.findViewById(R.id.signup_header_title);
        this.headerBackButton = (ImageView)this.findViewById(R.id.signup_header_back);

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

        ft.replace(R.id.signup_fragment_container, fragment);
        ft.addToBackStack(null).commit();

        this.changeTitleForCurrentPage();
    }


    public void changeCurrentPage(SignupPageIdentifier page, int animIn, int animOut) {
        this.currentPage = page;
        InputMethodManager imm = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(this.getWindow().getDecorView().getRootView().getWindowToken(), 0);

        Fragment fragment = this.getFragmentForCurrentPage();

        FragmentTransaction ft = this.getFragmentManager().beginTransaction();

        ft.setCustomAnimations(animIn, animOut);

        ft.replace(R.id.signup_fragment_container, fragment);
        ft.addToBackStack(null).commit();

        this.changeTitleForCurrentPage();
    }

    public void changeCurrentPage(SignupBaseFragment fragment, int animIn, int animOut) {
        InputMethodManager imm = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(this.getWindow().getDecorView().getRootView().getWindowToken(), 0);

        this.currentFragment = fragment;
        this.currentFragment.parentActivity = this;

        FragmentTransaction ft = this.getFragmentManager().beginTransaction();

        ft.setCustomAnimations(animIn, animOut);

        ft.replace(R.id.signup_fragment_container, this.currentFragment);
        ft.addToBackStack(null).commit();

        this.changeTitleForCurrentPage();
    }

    private void changeTitleForCurrentPage() {

        String title = "";

        switch (this.currentPage) {
            case SignupPhone:
                title = this.getApplicationContext().getResources().getString(R.string.SIGNUP_PAGE_TITLE_NumMobile);
                break;
            case SignupSMS:
                title = this.getApplicationContext().getResources().getString(R.string.SIGNUP_PAGE_TITLE_Sms);
                break;
            case SignupUsername:
                title = this.getApplicationContext().getResources().getString(R.string.SIGNUP_PAGE_TITLE_Pseudo);
                break;
            case SignupImage:
                title = this.getApplicationContext().getResources().getString(R.string.SIGNUP_PAGE_TITLE_Image);
                break;
            case SignupInfos:
                title = this.getApplicationContext().getResources().getString(R.string.SIGNUP_PAGE_TITLE_Info);
                break;
            case SignupPass:
                break;
            case SignupPassConfirm:
                break;
            case SignupImageCapture:
                break;
            case SignupImagePicker:
                title = this.getApplicationContext().getResources().getString(R.string.NAV_PHOTO_GALLERY);
                break;
            case SignupCGU:
                title = this.getApplicationContext().getResources().getString(R.string.INFORMATIONS_TERMS);
                break;
        }

        this.headerTitle.setText(title);
    }

    private SignupBaseFragment getFragmentForCurrentPage() {

        SignupBaseFragment fragment = null;

        switch (this.currentPage) {
            case SignupPhone:
                fragment = new SignupPhoneFragment();
                break;
            case SignupSMS:
                fragment = new SignupSMSFragment();
                break;
            case SignupUsername:
                fragment = new SignupUsernameFragment();
                break;
            case SignupImage:
                fragment = new SignupImageFragment();
                break;
            case SignupInfos:
                fragment = new SignupInfosFragment();
                break;
            case SignupPass:
                fragment = new SignupPassFragment();
                break;
            case SignupPassConfirm:
                fragment = new SignupPassFragment();
                ((SignupPassFragment)fragment).confirmMode = true;
                break;
            case SignupImageCapture:
                fragment = new SignupImageCaptureFragment();
                ((SignupImageCaptureFragment)fragment).cameraHostDelegate = this;
                break;
            case SignupImagePicker:
                fragment = new SignupImagePickerFragment();
                ((SignupImagePickerFragment)fragment).cameraHostDelegate = this;
                break;
            case SignupCGU:
                fragment = new SignupCGUFragment();
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
            case SignupPhone:
                this.currentPage = SignupPageIdentifier.SignupSMS;
                break;
            case SignupSMS:
                this.currentPage = SignupPageIdentifier.SignupUsername;
                break;
            case SignupUsername:
                this.currentPage = SignupPageIdentifier.SignupImage;
                break;
            case SignupImage:
                this.currentPage = SignupPageIdentifier.SignupInfos;
                break;
            case SignupInfos:
                this.currentPage = SignupPageIdentifier.SignupPass;
                break;
            case SignupPass:
                this.currentPage = SignupPageIdentifier.SignupPassConfirm;
                break;
            case SignupPassConfirm:
                break;
            case SignupImageCapture:
                break;
            case SignupImagePicker:
                break;
            case SignupCGU:
                break;
        }

        InputMethodManager imm = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(this.getWindow().getDecorView().getRootView().getWindowToken(), 0);

        Fragment fragment = this.getFragmentForCurrentPage();

        FragmentTransaction ft = this.getFragmentManager().beginTransaction();

        ft.setCustomAnimations(R.animator.slide_in_left, R.animator.slide_out_right);

        ft.replace(R.id.signup_fragment_container, fragment);
        ft.addToBackStack(null).commit();

        this.changeTitleForCurrentPage();
    }

    public void backToPreviousPage() {
        switch (this.currentPage) {
            case SignupPhone:
                Intent intent2 = new Intent();
                intent2.setClass(this.getApplicationContext(), StartActivity.class);
                this.startActivity(intent2);
                this.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                this.finish();
                break;
            case SignupSMS:
                this.currentPage = SignupPageIdentifier.SignupPhone;
                break;
            case SignupUsername:
                this.currentPage = SignupPageIdentifier.SignupSMS;
                break;
            case SignupImage:
                this.currentPage = SignupPageIdentifier.SignupUsername;
                break;
            case SignupInfos:
                this.currentPage = SignupPageIdentifier.SignupImage;
                break;
            case SignupPass:
                this.currentPage = SignupPageIdentifier.SignupInfos;
                break;
            case SignupPassConfirm:
                this.currentPage = SignupPageIdentifier.SignupPass;
                break;
            case SignupImageCapture:
                this.currentPage = SignupPageIdentifier.SignupImage;
                break;
            case SignupImagePicker:
                this.currentPage = SignupPageIdentifier.SignupImage;
                break;
            case SignupCGU:
                this.currentPage = SignupPageIdentifier.SignupInfos;
                break;
        }

        InputMethodManager imm = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(this.getWindow().getDecorView().getRootView().getWindowToken(), 0);

        Fragment fragment = this.getFragmentForCurrentPage();

        FragmentTransaction ft = this.getFragmentManager().beginTransaction();

        ft.setCustomAnimations(R.animator.slide_in_right, R.animator.slide_out_left);

        ft.replace(R.id.signup_fragment_container, fragment);
        ft.addToBackStack(null).commit();

        this.changeTitleForCurrentPage();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RESULT_SCANPAY_ACTIVITY && resultCode == ScanPay.RESULT_SCAN_SUCCESS)
        {
            CreditCard creditCard = data.getParcelableExtra(ScanPay.EXTRA_CREDIT_CARD);
            Toast.makeText(this, creditCard.number + " " + creditCard.month + "/" + creditCard.year + " " + creditCard.cvv, Toast.LENGTH_LONG).show();
        }
        else if (requestCode == RESULT_SCANPAY_ACTIVITY && resultCode == ScanPay.RESULT_SCAN_CANCEL)
        {
            Toast.makeText(this, "Scan cancel", Toast.LENGTH_LONG).show();
        }
        else
            Session.getActiveSession().onActivityResult(this, requestCode, resultCode, data);
    }

    @Override
    public void photoTaken(Bitmap photo) {
        userData.avatarData = photo;
        userData.avatarURL = null;
    }

    @Override
    public void onBackPressed() {
        this.backToPreviousPage();
    }

    @Override
    public CameraHost getCameraHost() {
        return(new CustomCameraHost(this));
    }
}

