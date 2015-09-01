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
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import me.flooz.app.Adapter.SettingsListAdapter;
import me.flooz.app.Adapter.SettingsListItem;
import me.flooz.app.App.FloozApplication;
import me.flooz.app.Network.FloozRestClient;
import me.flooz.app.R;
import me.flooz.app.UI.Controllers.CashoutController;
import me.flooz.app.UI.Controllers.NotificationsController;
import me.flooz.app.UI.Controllers.PreferencesController;
import me.flooz.app.Utils.CustomFonts;
import me.flooz.app.Utils.CustomNotificationIntents;
import me.flooz.app.Utils.FLHelper;
import me.flooz.app.Utils.ViewServer;

/**
 * Created by Flooz on 3/10/15.
 */
public class PreferencesSettingsActivity extends Activity {

    private PreferencesController controller;
    private FloozApplication floozApp;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (FLHelper.isDebuggable())
            ViewServer.get(this).addWindow(this);

        floozApp = (FloozApplication) this.getApplicationContext();

        this.setContentView(R.layout.settings_preferences_fragment);

        this.controller = new PreferencesController(this.findViewById(android.R.id.content), this, NotificationsController.ControllerKind.ACTIVITY_CONTROLLER);
    }

    @Override
    public void onResume() {
        super.onResume();
        floozApp.setCurrentActivity(this);

        if (FLHelper.isDebuggable())
            ViewServer.get(this).setFocusedWindow(this);

        this.controller.onResume();
    }

    @Override
    public void onPause() {
        clearReferences();
        super.onPause();

        this.controller.onPause();
    }

    @Override
    protected void onDestroy() {
        clearReferences();

        if (FLHelper.isDebuggable())
            ViewServer.get(this).removeWindow(this);

        super.onDestroy();

        this.controller.onDestroy();
    }

    private void clearReferences() {
        Activity currActivity = floozApp.getCurrentActivity();
        if (currActivity != null && currActivity.equals(this))
            floozApp.setCurrentActivity(null);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        this.controller.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onBackPressed() {
        this.controller.onBackPressed();
    }
}
