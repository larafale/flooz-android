package me.flooz.app.UI.Fragment.Home.Authentication;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import me.flooz.app.R;
import me.flooz.app.UI.Fragment.Home.HomeBaseFragment;
import me.flooz.app.Utils.CustomFonts;

/**
 * Created by Flooz on 12/12/14.
 */
public class AuthenticationFragment extends HomeBaseFragment {

    public enum AuthenticationPageIdentifier {
        AuthenticationSecureCode,
        AuthenticationPass,
        AuthenticationResetCode,
        AuthenticationResetCodeConfirm,
        AuthenticationChangePass
    };

    public interface AuthenticationDelegate {
        void authenticationValidated();
        void authenticationFailed();
    }

    public AuthenticationDelegate delegate;

    public RelativeLayout header;
    public TextView headerTitle;
    public ImageView headerBackButton;
    public ImageView headerTitleImg;

    public AuthenticationPageIdentifier currentPage;
    public AuthenticationBaseFragment currentFragment;

    public Boolean changeSecureCode = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.authentication_fragment, null);

        this.header = (RelativeLayout)view.findViewById(R.id.authentication_header);
        this.headerTitle = (TextView)view.findViewById(R.id.authentication_header_title);
        this.headerBackButton = (ImageView)view.findViewById(R.id.authentication_header_back);
        this.headerTitleImg = (ImageView)view.findViewById(R.id.authentication_header_title_img);

        this.headerTitle.setTypeface(CustomFonts.customTitleExtraLight(inflater.getContext()));
        this.headerBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                backToPreviousPage();
            }
        });

        this.currentPage = AuthenticationPageIdentifier.AuthenticationSecureCode;

        this.displayCurrentPage();

        return view;
    }

    public void displayCurrentPage() {
        InputMethodManager imm = (InputMethodManager) parentActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(parentActivity.getWindow().getDecorView().getRootView().getWindowToken(), 0);

        Fragment fragment = this.getFragmentForCurrentPage();

        FragmentTransaction ft = this.getChildFragmentManager().beginTransaction();

        ft.setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out);

        ft.replace(R.id.authentication_fragment_container, fragment);
        ft.addToBackStack(null).commit();

        this.changeTitleForCurrentPage();
    }


    public void changeCurrentPage(AuthenticationPageIdentifier page, int animIn, int animOut) {
        this.currentPage = page;
        InputMethodManager imm = (InputMethodManager) parentActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(parentActivity.getWindow().getDecorView().getRootView().getWindowToken(), 0);

        Fragment fragment = this.getFragmentForCurrentPage();

        FragmentTransaction ft = this.getChildFragmentManager().beginTransaction();

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

        fragment.parentActivity = null;

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

        InputMethodManager imm = (InputMethodManager) parentActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(parentActivity.getWindow().getDecorView().getRootView().getWindowToken(), 0);

        Fragment fragment = this.getFragmentForCurrentPage();

        FragmentTransaction ft = this.getChildFragmentManager().beginTransaction();

        ft.setCustomAnimations(R.animator.slide_in_left, R.animator.slide_out_right);

        ft.replace(R.id.authentication_fragment_container, fragment);
        ft.addToBackStack(null).commit();

        this.changeTitleForCurrentPage();
    }

    public void backToPreviousPage() {
        switch (this.currentPage) {

            case AuthenticationSecureCode:
                break;
            case AuthenticationPass:
                if (this.changeSecureCode) {
                    this.dismissView();
                    return;
                }
                else
                    this.currentPage = AuthenticationPageIdentifier.AuthenticationSecureCode;
                break;
            case AuthenticationResetCode:
                this.dismissView();
                return;
            case AuthenticationResetCodeConfirm:
                this.currentPage = AuthenticationPageIdentifier.AuthenticationResetCode;
                break;
            case AuthenticationChangePass:
                break;
        }

        InputMethodManager imm = (InputMethodManager) parentActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(parentActivity.getWindow().getDecorView().getRootView().getWindowToken(), 0);

        Fragment fragment = this.getFragmentForCurrentPage();

        FragmentTransaction ft = this.getChildFragmentManager().beginTransaction();

        ft.setCustomAnimations(R.animator.slide_in_right, R.animator.slide_out_left);

        ft.replace(R.id.authentication_fragment_container, fragment);
        ft.addToBackStack(null).commit();

        this.changeTitleForCurrentPage();
    }

    public void validateTransaction() {
        if (changeSecureCode && currentPage == AuthenticationPageIdentifier.AuthenticationSecureCode) {
            this.changeCurrentPage(AuthenticationPageIdentifier.AuthenticationResetCode, R.animator.slide_in_left, R.animator.slide_out_right);
        } else if (changeSecureCode && currentPage == AuthenticationPageIdentifier.AuthenticationResetCodeConfirm) {
            this.dismissView();
        } else if (!changeSecureCode) {
            if (delegate != null)
                delegate.authenticationValidated();

            parentActivity.popMainFragment(R.animator.slide_down, android.R.animator.fade_in);
        }
    }

    public void dismissView() {
        if (changeSecureCode) {
            parentActivity.popMainFragment(R.animator.slide_out_left, R.animator.slide_in_right);
        } else {
            if (delegate != null)
                delegate.authenticationFailed();

            parentActivity.popMainFragment(R.animator.slide_down, android.R.animator.fade_in);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        this.currentPage = AuthenticationPageIdentifier.AuthenticationSecureCode;
        this.displayCurrentPage();
    }

    @Override
    public void onResume() {
        super.onResume();
        this.currentPage = AuthenticationPageIdentifier.AuthenticationSecureCode;
        this.displayCurrentPage();
    }

    @Override
    public void onPause() {
        super.onPause();
        this.changeSecureCode = false;
    }

    @Override
    public void onStop() {
        super.onStop();
        this.changeSecureCode = false;
    }


    @Override
    public void onBackPressed() {
        this.currentFragment.onBackPressed();
    }
}
