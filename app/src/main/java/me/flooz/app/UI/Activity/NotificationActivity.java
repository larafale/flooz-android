package me.flooz.app.UI.Activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.baoyz.widget.PullRefreshLayout;

import org.json.JSONException;
import org.json.JSONObject;

import me.flooz.app.Adapter.NotificationListAdapter;
import me.flooz.app.App.FloozApplication;
import me.flooz.app.Model.FLError;
import me.flooz.app.Model.FLNotification;
import me.flooz.app.Network.FloozHttpResponseHandler;
import me.flooz.app.Network.FloozRestClient;
import me.flooz.app.R;
import me.flooz.app.UI.Controllers.NotificationsController;
import me.flooz.app.Utils.CustomFonts;
import me.flooz.app.Utils.FLHelper;
import me.flooz.app.Utils.ViewServer;

/**
 * Created by Flooz on 3/9/15.
 */
public class NotificationActivity extends BaseActivity {

    private FloozApplication floozApp;

    NotificationsController controller;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (FLHelper.isDebuggable())
            ViewServer.get(this).addWindow(this);

        floozApp = (FloozApplication)this.getApplicationContext();

        this.setContentView(R.layout.notification_fragment);

        JSONObject triggerData = null;
        if (getIntent() != null && getIntent().hasExtra("triggerData"))
            try {
                triggerData = new JSONObject(getIntent().getStringExtra("triggerData"));
            } catch (JSONException e) {
                e.printStackTrace();
            }

        this.controller = new NotificationsController(this.findViewById(android.R.id.content), this, NotificationsController.ControllerKind.ACTIVITY_CONTROLLER, triggerData);
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

    private void clearReferences(){
        Activity currActivity = floozApp.getCurrentActivity();
        if (currActivity != null && currActivity.equals(this))
            floozApp.setCurrentActivity(null);
    }

    @Override
    public void onBackPressed() {
        this.controller.onBackPressed();
    }
}
