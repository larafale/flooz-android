package me.flooz.app.UI.Fragment.Home.TabFragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import me.flooz.app.R;
import me.flooz.app.UI.Controllers.NotificationsController;
import me.flooz.app.UI.Controllers.NotifsSettingsController;
import me.flooz.app.UI.Controllers.PrivacyController;

/**
 * Created by Flooz on 9/2/15.
 */
public class NotifsSettingsFragment extends TabBarFragment {

    NotifsSettingsController controller;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.settings_notifications_fragment, null);

        if (this.controller == null)
            this.controller = new NotifsSettingsController(view, tabBarActivity, NotificationsController.ControllerKind.FRAGMENT_CONTROLLER);

        return this.controller.currentView;
    }

    @Override
    public void onStart() {
        super.onStart();
        this.controller.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        this.controller.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        this.controller.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        this.controller.onDestroy();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        this.controller.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onBackPressed() {
        this.controller.onBackPressed();
    }
}
