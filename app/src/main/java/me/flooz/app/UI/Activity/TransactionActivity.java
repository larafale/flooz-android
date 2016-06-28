package me.flooz.app.UI.Activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import org.json.JSONException;
import org.json.JSONObject;

import me.flooz.app.App.FloozApplication;
import me.flooz.app.Model.FLTransaction;
import me.flooz.app.R;
import me.flooz.app.UI.Controllers.NotificationsController;
import me.flooz.app.UI.Controllers.SearchController;
import me.flooz.app.UI.Controllers.TransactionCardController;
import me.flooz.app.UI.Controllers.TransactionController;
import me.flooz.app.Utils.FLHelper;
import me.flooz.app.Utils.ViewServer;

/**
 * Created by Flooz on 10/22/15.
 */
public class TransactionActivity  extends BaseActivity {

    private FloozApplication floozApp;
    public TransactionController controller;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (FLHelper.isDebuggable())
            ViewServer.get(this).addWindow(this);

        this.floozApp = (FloozApplication) this.getApplicationContext();

        this.setContentView(R.layout.transaction_card_fragment);

        Boolean insertComment = getIntent().getBooleanExtra("insertComment", false);
        String transaction = getIntent().getStringExtra("transaction");

        JSONObject triggerData = null;
        if (getIntent() != null && getIntent().hasExtra("triggerData"))
            try {
                triggerData = new JSONObject(getIntent().getStringExtra("triggerData"));
            } catch (JSONException e) {
                e.printStackTrace();
            }

        this.controller = new TransactionController(this.findViewById(android.R.id.content), this, NotificationsController.ControllerKind.ACTIVITY_CONTROLLER, triggerData);
        this.controller.insertComment = insertComment;

        try {
            FLTransaction transac = new FLTransaction(new JSONObject(transaction));
            this.controller.setTransaction(transac);
        } catch (JSONException e) {
            e.printStackTrace();
        }

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