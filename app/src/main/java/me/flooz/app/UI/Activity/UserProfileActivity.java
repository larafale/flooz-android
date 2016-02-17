package me.flooz.app.UI.Activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import org.json.JSONException;
import org.json.JSONObject;

import me.flooz.app.App.FloozApplication;
import me.flooz.app.Model.FLUser;
import me.flooz.app.R;
import me.flooz.app.UI.Controllers.BaseController;
import me.flooz.app.UI.Controllers.ProfileController;
import me.flooz.app.Utils.FLHelper;
import me.flooz.app.Utils.ViewServer;

/**
 * Created by Flooz on 10/19/15.
 */
public class UserProfileActivity extends Activity {

    private ProfileController controller;
    private FloozApplication floozApp;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (FLHelper.isDebuggable())
            ViewServer.get(this).addWindow(this);

        floozApp = (FloozApplication) this.getApplicationContext();

        this.setContentView(R.layout.profile_card_list_fragment);

        JSONObject triggerData = null;
        if (getIntent() != null && getIntent().hasExtra("triggerData"))
            try {
                triggerData = new JSONObject(getIntent().getStringExtra("triggerData"));
            } catch (JSONException e) {
                e.printStackTrace();
            }

        if (getIntent().hasExtra("user")) {
            try {
                FLUser user = new FLUser(new JSONObject(getIntent().getStringExtra("user")));
                this.controller = new ProfileController(user, this.findViewById(android.R.id.content), this, BaseController.ControllerKind.ACTIVITY_CONTROLLER, triggerData);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        this.controller.onStart();
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
