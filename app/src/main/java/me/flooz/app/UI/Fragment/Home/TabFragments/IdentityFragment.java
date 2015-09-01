package me.flooz.app.UI.Fragment.Home.TabFragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import me.flooz.app.R;
import me.flooz.app.UI.Controllers.IdentityController;
import me.flooz.app.UI.Controllers.NotificationsController;

/**
 * Created by Flooz on 9/1/15.
 */
public class IdentityFragment extends TabBarFragment {
    IdentityController controller;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.settings_identity_fragment, null);

        this.controller = new IdentityController(view, tabBarActivity, NotificationsController.ControllerKind.FRAGMENT_CONTROLLER);

        return view;
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
