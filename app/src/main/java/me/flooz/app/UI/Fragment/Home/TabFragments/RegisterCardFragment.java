package me.flooz.app.UI.Fragment.Home.TabFragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import me.flooz.app.R;
import me.flooz.app.UI.Controllers.NotificationsController;
import me.flooz.app.UI.Controllers.RegisterCardController;
import me.flooz.app.UI.Controllers.WebController;

/**
 * Created by gawenberger on 10/07/2017.
 */

public class RegisterCardFragment extends TabBarFragment {

    RegisterCardController controller;

    public String title;
    public String url;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.custom_register_card, null);

        if (this.controller == null) {
            this.controller = new RegisterCardController(view, tabBarActivity, NotificationsController.ControllerKind.FRAGMENT_CONTROLLER, this.triggerData);

            if (triggerData.has("title") && !triggerData.optString("title").isEmpty())
                this.title = triggerData.optString("title");

            if (triggerData.has("url") && !triggerData.optString("url").isEmpty())
                this.url = triggerData.optString("url");


            if (this.title != null && !this.title.isEmpty())
                this.controller.title = this.title;

            if (this.url != null && !this.url.isEmpty())
                this.controller.url = this.url;
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
