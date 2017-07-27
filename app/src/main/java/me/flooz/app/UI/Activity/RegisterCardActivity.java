package me.flooz.app.UI.Activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import org.json.JSONException;
import org.json.JSONObject;

import me.flooz.app.App.FloozApplication;
import me.flooz.app.R;
import me.flooz.app.UI.Controllers.NotificationsController;
import me.flooz.app.UI.Controllers.RegisterCardController;
import me.flooz.app.UI.Controllers.WebController;
import me.flooz.app.Utils.FLHelper;
import me.flooz.app.Utils.ViewServer;

/**
 * Created by gawenberger on 10/07/2017.
 */

public class RegisterCardActivity extends BaseActivity {
    private FloozApplication floozApp;
    private RegisterCardController controller;

    public String title;
    public String url;
    public String backgroundColor;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (FLHelper.isDebuggable())
            ViewServer.get(this).addWindow(this);

        this.floozApp = (FloozApplication) this.getApplicationContext();

        this.title = getIntent().getStringExtra("title");
        this.url = getIntent().getStringExtra("url");
        this.backgroundColor = getIntent().getStringExtra("backgroundColor");
        this.title = this.getResources().getString(R.string.CARD);

        JSONObject triggerData = null;
        if (getIntent() != null && getIntent().hasExtra("triggerData"))
            try {
                triggerData = new JSONObject(getIntent().getStringExtra("triggerData"));

                if (triggerData.has("title") && !triggerData.optString("title").isEmpty())
                    this.title = triggerData.optString("title");

                if (triggerData.has("url") && !triggerData.optString("url").isEmpty())
                    this.url = triggerData.optString("url");

                if (triggerData.has("backgroundColor") && !triggerData.optString("backgroundColor").isEmpty())
                    this.backgroundColor = triggerData.optString("backgroundColor");

            } catch (JSONException e) {
                e.printStackTrace();
            }

        this.setContentView(R.layout.custom_register_card);
        this.controller = new RegisterCardController(this.findViewById(android.R.id.content), this, NotificationsController.ControllerKind.ACTIVITY_CONTROLLER, triggerData);
        this.controller.title = this.title;
        this.controller.url = this.url;
        this.controller.backgroundColor = this.backgroundColor;
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
    protected void onStart() {
        super.onStart();

        this.controller.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();

        this.controller.onStop();
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
