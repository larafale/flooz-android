package me.flooz.app.UI.Activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import me.flooz.app.Adapter.CashinAdapter;
import me.flooz.app.Adapter.PaymentAdapter;
import me.flooz.app.App.FloozApplication;
import me.flooz.app.Model.FLButton;
import me.flooz.app.R;
import me.flooz.app.Utils.CustomFonts;
import me.flooz.app.Utils.FLHelper;
import me.flooz.app.Utils.FLTriggerManager;
import me.flooz.app.Utils.ViewServer;

/**
 * Created by Flooz on 22/05/16.
 */
public class PaymentSourceActivity extends BaseActivity {
    private FloozApplication floozApp;
    private ListView listView;
    private PaymentAdapter listAdapter;
    private View headerListView;
    private ImageView headerBackButton;

    private List<FLButton> items;

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

        items = new ArrayList<>();

        if (triggerData != null) {
            JSONArray itemsData = triggerData.optJSONArray("items");
            if (itemsData != null) {
                for (int i = 0; i < itemsData.length(); i++) {
                    FLButton button = new FLButton(itemsData.optJSONObject(i));
                    items.add(button);
                }
            }
        }

        floozApp = (FloozApplication) this.getApplicationContext();
        this.setContentView(R.layout.payment_source_activity);

        headerListView = this.getLayoutInflater().inflate(R.layout.payment_list_header, null);
        TextView title = (TextView) this.findViewById(R.id.header_title);
        this.headerBackButton = (ImageView) this.findViewById(R.id.header_item_left);

        TextView cashinInfos = (TextView) headerListView.findViewById(R.id.payment_header_infos);

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

        listView = (ListView) this.findViewById(R.id.payment_list);

        listAdapter = new PaymentAdapter(this, items);

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
    }

    @Override
    public void onResume() {
        super.onResume();
        if (FLHelper.isDebuggable())
            ViewServer.get(this).setFocusedWindow(this);
        floozApp.setCurrentActivity(this);
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

        super.onDestroy();
    }

    private void clearReferences() {
        Activity currActivity = floozApp.getCurrentActivity();
        if (currActivity != null && currActivity.equals(this))
            floozApp.setCurrentActivity(null);
    }
}
