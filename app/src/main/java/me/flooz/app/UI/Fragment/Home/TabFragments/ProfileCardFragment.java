package me.flooz.app.UI.Fragment.Home.TabFragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import me.flooz.app.Model.FLUser;
import me.flooz.app.R;
import me.flooz.app.UI.Controllers.NotificationsController;
import me.flooz.app.UI.Controllers.ProfileController;

/**
 * Created by Wapazz on 22/09/15.
 */
public class ProfileCardFragment extends TabBarFragment {

    public ProfileController controller;
    public FLUser user;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        if (this.controller == null) {
            this.controller = new ProfileController(user, inflater.inflate(R.layout.profile_card_list_fragment, null), tabBarActivity, NotificationsController.ControllerKind.FRAGMENT_CONTROLLER, this.triggerData);
        }

        return this.controller.currentView;
    }

    @Override
    public void onStart() {
        super.onStart();

        if (this.controller != null)
            this.controller.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();

        if (this.controller != null)
            this.controller.onStop();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (this.controller != null)
            this.controller.onResume();
    }

    @Override
    public void onPause() {
        if (this.controller != null)
            this.controller.onPause();
        super.onPause();
    }

    @Override
    public void onDestroy() {
        if (this.controller != null)
            this.controller.onDestroy();

        super.onDestroy();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (this.controller != null)
            this.controller.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onBackPressed() {
        if (this.controller != null)
            this.controller.onBackPressed();
    }
}
