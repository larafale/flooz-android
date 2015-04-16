package me.flooz.app.UI.Activity.Settings;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.Session;

import me.flooz.app.App.FloozApplication;
import me.flooz.app.Network.FloozRestClient;
import me.flooz.app.R;
import me.flooz.app.Utils.CustomFonts;
import me.flooz.app.Utils.CustomNotificationIntents;
import me.flooz.app.Utils.FLHelper;
import me.flooz.app.Utils.ViewServer;

/**
 * Created by Flooz on 3/10/15.
 */
public class PreferencesSettingsActivity extends Activity {

    private PreferencesSettingsActivity instance;
    private FloozApplication floozApp;
    private Boolean modal;

    private Boolean viewVisible = false;
    private ImageView headerBackButton;
    private CheckBox fbSwitch;

    private BroadcastReceiver reloadCurrentUserDataReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            reloadView();
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (FLHelper.isDebuggable())
            ViewServer.get(this).addWindow(this);

        this.instance = this;
        this.floozApp = (FloozApplication) this.getApplicationContext();
        this.modal = getIntent().getBooleanExtra("modal", false);

        this.setContentView(R.layout.settings_preferences_fragment);

        this.headerBackButton = (ImageView) this.findViewById(R.id.settings_preferences_header_back);
        TextView headerTitle = (TextView) this.findViewById(R.id.settings_preferences_header_title);
        TextView fbText = (TextView) this.findViewById(R.id.settings_preferences_fb_text);
        TextView notifText = (TextView) this.findViewById(R.id.settings_preferences_notif_text);
        this.fbSwitch = (CheckBox) this.findViewById(R.id.settings_preferences_fb_toggle);

        headerTitle.setTypeface(CustomFonts.customTitleExtraLight(this));
        notifText.setTypeface(CustomFonts.customTitleExtraLight(this));
        fbText.setTypeface(CustomFonts.customTitleExtraLight(this));

        this.findViewById(R.id.settings_preferences_notif).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(instance, NotificationsSettingsActivity.class);
                instance.startActivity(intent);
                instance.overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left);
            }
        });

        this.reloadView();

        this.fbSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    if (!FloozRestClient.getInstance().isConnectedToFacebook()) {
                        FloozRestClient.getInstance().connectFacebook();
                        fbSwitch.setChecked(false);
                    }
                } else {
                    FloozRestClient.getInstance().disconnectFacebook();
                }
            }
        });

        this.headerBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
                if (modal)
                    overridePendingTransition(android.R.anim.fade_in, R.anim.slide_down);
                else
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right);
            }
        });
    }


    @Override
    public void onResume() {
        super.onResume();
        floozApp.setCurrentActivity(this);

        if (FLHelper.isDebuggable())
            ViewServer.get(this).setFocusedWindow(this);

        this.viewVisible = true;
        this.reloadView();

        LocalBroadcastManager.getInstance(FloozApplication.getAppContext()).registerReceiver(reloadCurrentUserDataReceiver,
                CustomNotificationIntents.filterReloadCurrentUser());
    }

    @Override
    public void onPause() {
        clearReferences();
        super.onPause();
        LocalBroadcastManager.getInstance(FloozApplication.getAppContext()).unregisterReceiver(reloadCurrentUserDataReceiver);
    }

    @Override
    protected void onDestroy() {
        clearReferences();

        if (FLHelper.isDebuggable())
            ViewServer.get(this).removeWindow(this);

        super.onDestroy();
    }

    private void reloadView() {
        if (this.viewVisible) {
            if (FloozRestClient.getInstance().isConnectedToFacebook())
                this.fbSwitch.setChecked(true);
            else
                this.fbSwitch.setChecked(false);
        }
    }

    private void clearReferences(){
        Activity currActivity = floozApp.getCurrentActivity();
        if (currActivity != null && currActivity.equals(this))
            floozApp.setCurrentActivity(null);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Session.getActiveSession().onActivityResult(this, requestCode, resultCode, data);
    }

    @Override
    public void onBackPressed() {
        this.headerBackButton.performClick();
    }
}
