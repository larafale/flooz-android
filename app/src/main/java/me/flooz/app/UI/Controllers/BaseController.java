package me.flooz.app.UI.Controllers;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONObject;

import me.flooz.app.R;
import me.flooz.app.UI.Activity.HomeActivity;
import me.flooz.app.Utils.CustomFonts;

/**
 * Created by Flooz on 8/25/15.
 */
public class BaseController {

    public enum ControllerKind {
        ACTIVITY_CONTROLLER,
        FRAGMENT_CONTROLLER
    }

    public ControllerKind currentKind;
    public View currentView;
    public Activity parentActivity;
    public JSONObject triggersData;
    protected TextView titleLabel;
    protected ImageView closeButton;

    @Deprecated
    public BaseController()
    {
        // don't use this constructor! i don't want it to ever be called!
        throw new RuntimeException("Illegal constructor called");
    }

    public BaseController(@NonNull View mainView, @NonNull Activity parentActivity, @NonNull ControllerKind kind) {
        this.parentActivity = parentActivity;
        this.currentView = mainView;
        this.currentKind = kind;
    }

    public BaseController(@NonNull View mainView, @NonNull Activity parentActivity, @NonNull ControllerKind kind, @Nullable JSONObject data) {
        this.parentActivity = parentActivity;
        this.currentView = mainView;
        this.currentKind = kind;
        this.triggersData = data;
    }

    protected void init() {
        this.closeButton = (ImageView) this.currentView.findViewById(R.id.header_item_left);
        this.titleLabel = (TextView) this.currentView.findViewById(R.id.header_title);

        if (this.titleLabel != null) {
            this.titleLabel.setTypeface(CustomFonts.customTitleExtraLight(this.parentActivity));

            if (this.triggersData != null && this.triggersData.has("title") && !this.triggersData.optString("title").isEmpty())
                this.titleLabel.setText(this.triggersData.optString("title"));
        }

        if (this.closeButton != null) {
            if (this.triggersData != null && this.triggersData.has("close") && !this.triggersData.optBoolean("close")) {
                this.closeButton.setVisibility(View.INVISIBLE);
            } else {
                if (this.currentKind == ControllerKind.FRAGMENT_CONTROLLER)
                    this.closeButton.setImageDrawable(this.parentActivity.getResources().getDrawable(R.drawable.nav_back));
                else
                    this.closeButton.setImageDrawable(this.parentActivity.getResources().getDrawable(R.drawable.nav_cross));

                this.closeButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (currentKind == ControllerKind.ACTIVITY_CONTROLLER) {
                            BaseController.this.parentActivity.finish();
                            BaseController.this.parentActivity.overridePendingTransition(android.R.anim.fade_in, R.anim.slide_down);
                        } else {
                            ((HomeActivity) BaseController.this.parentActivity).popFragmentInCurrentTab();
                        }
                    }
                });
            }
        }
    }

    public void onStart() {

    }

    public void onResume() {

    }

    public void onPause() {

    }

    public void onStop() {

    }

    public void onDestroy() {

    }

    public void onBackPressed() {
        if (!(this.triggersData != null && this.triggersData.has("close") && !this.triggersData.optBoolean("close")) && this.closeButton != null && this.closeButton.getVisibility() == View.VISIBLE) {
            this.closeButton.performClick();
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {

    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

    }
}