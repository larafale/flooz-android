package me.flooz.app.UI.Fragment.Home.TabFragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import me.flooz.app.Model.FLTransaction;
import me.flooz.app.R;
import me.flooz.app.UI.Controllers.CollectController;
import me.flooz.app.UI.Controllers.CollectParticipationController;
import me.flooz.app.UI.Controllers.NotificationsController;

/**
 * Created by Flooz on 08/05/16.
 */
public class CollectParticipationFragment extends  TabBarFragment {
    public CollectParticipationController controller;
    public String collectId;
    public String userId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.collect_participation_activity, null);

        if (this.controller == null) {
            this.controller = new CollectParticipationController(view, tabBarActivity, NotificationsController.ControllerKind.FRAGMENT_CONTROLLER, this.triggerData);
            this.controller.userId = this.userId;
            this.controller.collectId = this.collectId;
        }

        return this.controller.currentView;
    }

    @Override
    public void onStart() {
        super.onStart();

        this.controller.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();

        this.controller.onStop();
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
