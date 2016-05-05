package me.flooz.app.UI.Activity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;

import me.flooz.app.Adapter.CashinAdapter;
import me.flooz.app.App.FloozApplication;
import me.flooz.app.Model.FLButton;
import me.flooz.app.Network.FloozRestClient;
import me.flooz.app.R;
import me.flooz.app.Utils.CustomFonts;
import me.flooz.app.Utils.CustomNotificationIntents;
import me.flooz.app.Utils.FLHelper;
import me.flooz.app.Utils.FLTriggerManager;
import me.flooz.app.Utils.ViewServer;

/**
 * Created by Flooz on 04/05/16.
 */
public class CashinActivity extends BaseActivity {

    private FloozApplication floozApp;
    private ListView listView;
    private CashinAdapter listAdapter;
    private View headerListView;
    private ImageView headerBackButton;

    private TextView amountLabel;

    private BroadcastReceiver reloadCurrentUserDataReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            amountLabel.setText(FLHelper.trimTrailingZeros(String.format(Locale.US, "%.2f", FloozRestClient.getInstance().currentUser.amount.floatValue())));
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (FLHelper.isDebuggable())
            ViewServer.get(this).addWindow(this);

        JSONObject triggerData = null;
        if (getIntent() != null && getIntent().hasExtra("triggerData"))
            try {
                triggerData = new JSONObject(getIntent().getStringExtra("triggerData"));
            } catch (JSONException e) {
                e.printStackTrace();
            }

        floozApp = (FloozApplication) this.getApplicationContext();
        this.setContentView(R.layout.cashin_activity);

        headerListView = this.getLayoutInflater().inflate(R.layout.cashin_list_header, null);
        TextView title = (TextView) this.findViewById(R.id.header_title);
        this.headerBackButton = (ImageView) this.findViewById(R.id.header_item_left);

        amountLabel = (TextView) headerListView.findViewById(R.id.cashin_header_balance);
        TextView amountHint = (TextView) headerListView.findViewById(R.id.cashin_header_balance_hint);
        TextView amountCurrency = (TextView) headerListView.findViewById(R.id.cashin_header_balance_currency);
        TextView cashinInfos = (TextView) headerListView.findViewById(R.id.cashin_header_infos);

        amountHint.setTypeface(CustomFonts.customContentRegular(this));
        amountLabel.setTypeface(CustomFonts.customContentBold(this));
        amountCurrency.setTypeface(CustomFonts.customContentBold(this));
        cashinInfos.setTypeface(CustomFonts.customTitleLight(this));
        title.setTypeface(CustomFonts.customTitleLight(this));

        if (triggerData != null) {
            if (triggerData.has("title") && !triggerData.optString("title").isEmpty())
                title.setText(triggerData.optString("title"));

            if (triggerData.has("close") && !triggerData.optBoolean("close")) {
                this.headerBackButton.setVisibility(View.GONE);
            }
        }

        this.headerBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (headerBackButton.getVisibility() == View.VISIBLE) {
                    finish();
                    overridePendingTransition(android.R.anim.fade_in, R.anim.slide_down);
                }
            }
        });

        listView = (ListView) this.findViewById(R.id.cashin_list);

        listAdapter = new CashinAdapter(this);

        listView.addHeaderView(headerListView);
        listView.setAdapter(listAdapter);

        this.listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                FLButton button = (FLButton) listAdapter.getItem(position - 1);
                if (button.avalaible)
                    FLTriggerManager.getInstance().executeTriggerList(FLTriggerManager.convertTriggersJSONArrayToList(button.triggers));
            }
        });

        LocalBroadcastManager.getInstance(FloozApplication.getAppContext()).registerReceiver(reloadCurrentUserDataReceiver,
                CustomNotificationIntents.filterReloadCurrentUser());
    }

    @Override
    public void onResume() {
        super.onResume();
        if (FLHelper.isDebuggable())
            ViewServer.get(this).setFocusedWindow(this);
        floozApp.setCurrentActivity(this);

        amountLabel.setText(FLHelper.trimTrailingZeros(String.format(Locale.US, "%.2f", FloozRestClient.getInstance().currentUser.amount.floatValue())));
    }

    @Override
    public void onPause() {
        clearReferences();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        clearReferences();
        if (FLHelper.isDebuggable())
            ViewServer.get(this).removeWindow(this);

        LocalBroadcastManager.getInstance(this).unregisterReceiver(reloadCurrentUserDataReceiver);

        super.onDestroy();
    }

    private void clearReferences() {
        Activity currActivity = floozApp.getCurrentActivity();
        if (currActivity != null && currActivity.equals(this))
            floozApp.setCurrentActivity(null);
    }
}
