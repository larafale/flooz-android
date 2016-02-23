package me.flooz.app.UI.Activity;

import android.app.Activity;
import android.content.Intent;
import android.net.MailTo;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import me.flooz.app.App.FloozApplication;
import me.flooz.app.R;
import me.flooz.app.UI.Controllers.CashoutController;
import me.flooz.app.UI.Controllers.NotificationsController;
import me.flooz.app.UI.Controllers.WebController;
import me.flooz.app.Utils.CustomFonts;
import me.flooz.app.Utils.FLHelper;
import me.flooz.app.Utils.ViewServer;

/**
 * Created by Flooz on 3/10/15.
 */
public class WebContentActivity extends BaseActivity {

    private FloozApplication floozApp;
    private WebController controller;

    public String title;
    public String url;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (FLHelper.isDebuggable())
            ViewServer.get(this).addWindow(this);

        this.floozApp = (FloozApplication) this.getApplicationContext();

        this.title = getIntent().getStringExtra("title");
        this.url = getIntent().getStringExtra("url");

        JSONObject triggerData = null;
        if (getIntent() != null && getIntent().hasExtra("triggerData"))
            try {
                triggerData = new JSONObject(getIntent().getStringExtra("triggerData"));

                if (triggerData.has("title") && !triggerData.optString("title").isEmpty())
                    this.title = triggerData.optString("title");

                if (triggerData.has("url") && !triggerData.optString("url").isEmpty())
                    this.url = triggerData.optString("url");

            } catch (JSONException e) {
                e.printStackTrace();
            }

        this.setContentView(R.layout.custom_webview_fragment);
        this.controller = new WebController(this.findViewById(android.R.id.content), this, NotificationsController.ControllerKind.ACTIVITY_CONTROLLER, triggerData);
        this.controller.title = this.title;
        this.controller.url = this.url;
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