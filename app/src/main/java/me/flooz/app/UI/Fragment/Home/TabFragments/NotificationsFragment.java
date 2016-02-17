package me.flooz.app.UI.Fragment.Home.TabFragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import me.flooz.app.R;
import me.flooz.app.UI.Controllers.NotificationsController;

/**
 * Created by Flooz on 8/25/15.
 */
public class NotificationsFragment extends TabBarFragment {

    NotificationsController controller;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.notification_fragment, null);

        if (this.controller == null)
            this.controller = new NotificationsController(view, tabBarActivity, NotificationsController.ControllerKind.FRAGMENT_CONTROLLER, this.triggerData);

        return this.controller.currentView;
    }

    public void onStart() {
        super.onStart();
        this.controller.onStart();
    }

    public void onResume() {
        super.onResume();
        this.controller.onResume();
    }

    public void onPause() {
        super.onPause();
        this.controller.onPause();
    }

    public void onDestroy() {
        super.onDestroy();
        this.controller.onDestroy();
    }

    public void onBackPressed() {
        this.controller.onBackPressed();
    }
}