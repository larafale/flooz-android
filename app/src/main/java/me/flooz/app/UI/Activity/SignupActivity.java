package me.flooz.app.UI.Activity;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.Session;

import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import me.flooz.app.App.FloozApplication;
import me.flooz.app.Model.FLUser;
import me.flooz.app.R;
import me.flooz.app.UI.Fragment.Signup.SignupBaseFragment;
import me.flooz.app.UI.Fragment.Signup.SignupCGUFragment;
import me.flooz.app.UI.Fragment.Signup.SignupImageFragment;
import me.flooz.app.UI.Fragment.Signup.SignupInfosFragment;
import me.flooz.app.UI.Fragment.Signup.SignupInvitationFragment;
import me.flooz.app.UI.Fragment.Signup.SignupPassFragment;
import me.flooz.app.UI.Fragment.Signup.SignupPhoneFragment;
import me.flooz.app.UI.Fragment.Signup.SignupSMSFragment;
import me.flooz.app.UI.Fragment.Signup.SignupUsernameFragment;
import me.flooz.app.Utils.CustomFonts;
import me.flooz.app.Utils.FLHelper;
import me.flooz.app.Utils.ImageHelper;
import me.flooz.app.Utils.ViewServer;
import scanpay.it.CreditCard;
import scanpay.it.ScanPay;

/**
 * Created by Flooz on 9/17/14.
 */
public class SignupActivity extends FragmentActivity {

    private SignupActivity instance;
    public FloozApplication floozApp;

    public FLUser userData;

    public static final int RESULT_SCANPAY_ACTIVITY = 4;
    public static final int SELECT_PICTURE = 1;
    public static final int TAKE_PICTURE = 2;

    private Uri tmpUriImage;

    public enum SignupPageIdentifier {
        SignupPhone,
        SignupSMS,
        SignupUsername,
        SignupImage,
        SignupInfos,
        SignupPass,
        SignupPassConfirm,
        SignupCGU,
        SignupInvitation
    }

    public RelativeLayout header;
    public TextView headerTitle;
    public ImageView headerBackButton;

    public SignupPageIdentifier currentPage;
    public SignupBaseFragment currentFragment;

    public List<SignupBaseFragment> fragmentHistory;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (FLHelper.isDebuggable())
            ViewServer.get(this).addWindow(this);

        instance = this;
        floozApp = (FloozApplication)this.getApplicationContext();

        setContentView(R.layout.signup_activity);

        this.fragmentHistory = new ArrayList<>();

        this.currentPage = SignupPageIdentifier.values()[getIntent().getIntExtra("page", SignupPageIdentifier.SignupPhone.ordinal())];

        this.userData = new FLUser();

        if (getIntent().getStringExtra("phone") != null && getIntent().getStringExtra("phone").length() > 0) {
            this.userData.phone = getIntent().getStringExtra("phone");
            if (this.userData.phone.startsWith("+33"))
                this.userData.phone = this.userData.phone.replace("+33", "0");
        }

        if (getIntent().getStringExtra("coupon") != null && getIntent().getStringExtra("coupon").length() > 0)
            this.userData.coupon = getIntent().getStringExtra("coupon");

        this.userData.distinctId = FloozApplication.mixpanelAPI.getDistinctId();

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

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

    }

    public void displayCurrentPage() {
        InputMethodManager imm = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(this.getWindow().getDecorView().getRootView().getWindowToken(), 0);

        SignupBaseFragment fragment = this.getFragmentForCurrentPage();

        FragmentTransaction ft = this.getFragmentManager().beginTransaction();

        ft.setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out);

        ft.add(R.id.signup_fragment_container, fragment);
        ft.addToBackStack(null).commit();

        if (this.currentPage == SignupPageIdentifier.SignupSMS) {
            this.fragmentHistory.clear();
            this.fragmentHistory.add(new SignupPhoneFragment());
        }

        this.fragmentHistory.add(fragment);
        this.currentFragment = fragment;

        this.changeTitleForCurrentPage();
    }

    public void changeCurrentPage(SignupPageIdentifier page, int animIn, int animOut, JSONObject data) {
        this.currentPage = page;
        InputMethodManager imm = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(this.getWindow().getDecorView().getRootView().getWindowToken(), 0);

        SignupBaseFragment fragment = this.getFragmentForCurrentPage();

        if (data != null)
            fragment.setInitData(data);

        FragmentTransaction ft = this.getFragmentManager().beginTransaction();

        ft.setCustomAnimations(animIn, animOut);

        ft.add(R.id.signup_fragment_container, fragment);
        ft.addToBackStack(null).commit();

        this.fragmentHistory.add(fragment);
        this.currentFragment = fragment;

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
            case SignupCGU:
                title = this.getApplicationContext().getResources().getString(R.string.INFORMATIONS_TERMS);
                break;
            case SignupInvitation:
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
            case SignupCGU:
                fragment = new SignupCGUFragment();
                break;
            case SignupInvitation:
                fragment = new SignupInvitationFragment();
                break;
            default:
                break;
        }

        fragment.parentActivity = this;
        fragment.pageId = this.currentPage;

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

    public void gotToNextPage(String step, JSONObject data) {

        if (step == null || step.isEmpty()) {
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
                case SignupCGU:
                    break;
                case SignupInvitation:
                    this.currentPage = SignupPageIdentifier.SignupPass;
                    break;
            }
        } else {
            switch (step) {
                case "step-nick":
                    this.currentPage = SignupPageIdentifier.SignupUsername;
                    break;
                case "step-image":
                    this.currentPage = SignupPageIdentifier.SignupImage;
                    break;
                case "step-infos":
                    this.currentPage = SignupPageIdentifier.SignupInfos;
                    break;
                case "step-invitation":
                    this.currentPage = SignupPageIdentifier.SignupInvitation;
                    break;
                case "step-securecode":
                    this.currentPage = SignupPageIdentifier.SignupPass;
                    break;
            }
        }

        this.changeCurrentPage(this.currentPage, R.animator.slide_in_left, R.animator.slide_out_right, data);
    }

    public void backToPreviousPage() {
        if (this.fragmentHistory.size() > 1) {
            InputMethodManager imm = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(this.getWindow().getDecorView().getRootView().getWindowToken(), 0);

            SignupBaseFragment fragment = this.fragmentHistory.get(this.fragmentHistory.size() - 1);

            FragmentTransaction ft = this.getFragmentManager().beginTransaction();

            ft.setCustomAnimations(R.animator.slide_in_right, R.animator.slide_out_left);

            ft.remove(fragment);
            ft.addToBackStack(null).commit();

            this.fragmentHistory.remove(this.fragmentHistory.size() - 1);
            this.currentPage = this.fragmentHistory.get(this.fragmentHistory.size() - 1).pageId;

            this.currentFragment = this.fragmentHistory.get(this.fragmentHistory.size() - 1);
            this.currentFragment.onResume();

            this.changeTitleForCurrentPage();
        } else {
            Intent intent = new Intent();
            intent.setClass(this.getApplicationContext(), StartActivity.class);
            this.startActivity(intent);
            this.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            this.finish();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SELECT_PICTURE || requestCode == TAKE_PICTURE) {
            if (resultCode == RESULT_OK) {
                Uri imageUri;
                if (data == null || data.getData() == null)
                    imageUri = this.tmpUriImage;
                else
                    imageUri = data.getData();

                this.tmpUriImage = null;

                final Uri selectedImageUri = imageUri;
                if (selectedImageUri != null) {
                    Handler handler = new Handler(Looper.getMainLooper());
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            String path = ImageHelper.getPath(instance, selectedImageUri);
                            if (path != null) {
                                File image = new File(path);
                                BitmapFactory.Options bmOptions = new BitmapFactory.Options();
                                Bitmap photo = BitmapFactory.decodeFile(image.getAbsolutePath(), bmOptions);

                                int rotation = ImageHelper.getRotation(instance, selectedImageUri);
                                if (rotation != 0) {
                                    photo = ImageHelper.rotateBitmap(photo, rotation);
                                }

                                userData.avatarData = photo;
                                userData.avatarURL = null;
                                currentFragment.refreshView();
                            }
                        }
                    });
                }
            }
        }
        else if (requestCode == RESULT_SCANPAY_ACTIVITY && resultCode == ScanPay.RESULT_SCAN_SUCCESS)
        {
            CreditCard creditCard = data.getParcelableExtra(ScanPay.EXTRA_CREDIT_CARD);
            Toast.makeText(this, creditCard.number + " " + creditCard.month + "/" + creditCard.year + " " + creditCard.cvv, Toast.LENGTH_LONG).show();
        }
        else if (requestCode == RESULT_SCANPAY_ACTIVITY && resultCode == ScanPay.RESULT_SCAN_CANCEL)
        {
            Toast.makeText(this, "Scan cancel", Toast.LENGTH_LONG).show();
        }
        else if (Session.getActiveSession() != null)
            Session.getActiveSession().onActivityResult(this, requestCode, resultCode, data);
    }

    public void takeImageFromCam() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        Uri fileUri = ImageHelper.getOutputMediaFileUri(ImageHelper.MEDIA_TYPE_IMAGE);
        tmpUriImage = fileUri;
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);

        startActivityForResult(intent, HomeActivity.TAKE_PICTURE);
    }

    public void takeImageFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, HomeActivity.SELECT_PICTURE);
    }

    @Override
    public void onBackPressed() {
        this.backToPreviousPage();
    }
}

