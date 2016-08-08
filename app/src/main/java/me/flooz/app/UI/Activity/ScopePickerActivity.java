package me.flooz.app.UI.Activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;
import java.util.List;

import me.flooz.app.Adapter.ScopePickerAdapter;
import me.flooz.app.App.FloozApplication;
import me.flooz.app.Model.FLTransaction;
import me.flooz.app.Model.FLTrigger;
import me.flooz.app.R;
import me.flooz.app.Utils.FLHelper;
import me.flooz.app.Utils.FLTriggerManager;
import me.flooz.app.Utils.ViewServer;

/**
 * Created by Flooz on 08/08/16.
 */
public class ScopePickerActivity extends BaseActivity {
    private FloozApplication floozApp;

    private ImageView headerBackButton;
    private TextView headerTitle;

    private ScopePickerAdapter listAdapter;
    private ListView listView;

    private JSONObject triggerData;

    private List<FLTrigger> successTriggers;

    private FLTransaction.TransactionScope currentScope = null;

    private Boolean isPot = false;
    private JSONArray limitedScopes;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (FLHelper.isDebuggable())
            ViewServer.get(this).addWindow(this);

        this.floozApp = (FloozApplication) this.getApplicationContext();

        if (getIntent() != null && getIntent().hasExtra("triggerData"))
            try {
                this.triggerData = new JSONObject(getIntent().getStringExtra("triggerData"));

                if (this.triggerData.has("scope"))
                    this.currentScope = FLTransaction.transactionScopeIDToScope(this.triggerData.optInt("scope"));

                if (this.triggerData.has("isPot"))
                    this.isPot = this.triggerData.optBoolean("isPot");

                if (this.triggerData.has("scopes"))
                    this.limitedScopes = this.triggerData.optJSONArray("scopes");

            } catch (JSONException e) {
                e.printStackTrace();
            }
        if (getIntent()!= null) {
            if (getIntent().hasExtra("scope"))
                this.currentScope = FLTransaction.transactionParamsToScope(getIntent().getStringExtra("scope"));

            if (getIntent().hasExtra("isPot"))
                this.isPot = getIntent().getBooleanExtra("isPot", false);

            if (getIntent().hasExtra("scopes"))
                try {
                    this.limitedScopes = new JSONArray(getIntent().getStringExtra("scopes"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
        }

        this.setContentView(R.layout.scope_picker_activity);

        this.headerBackButton = (ImageView) this.findViewById(R.id.header_item_left);
        this.headerTitle = (TextView) this.findViewById(R.id.header_title);
        this.listView = (ListView) this.findViewById(R.id.scope_picker_list);

        this.listAdapter = new ScopePickerAdapter(this, isPot, currentScope, limitedScopes);

        listView.setAdapter(this.listAdapter);

        this.headerBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(RESULT_OK, getIntent());
                finish();
                overridePendingTransition(android.R.anim.fade_in, R.anim.slide_down);
            }
        });

        this.listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                currentScope = listAdapter.getItem(position);
                if (currentScope != null) {
                    listAdapter.currentScope = ScopePickerActivity.this.currentScope;
                    listAdapter.notifyDataSetChanged();

                    if (triggerData != null) {
                        successTriggers = FLTriggerManager.convertTriggersJSONArrayToList(triggerData.optJSONArray("success"));
                        FLTrigger successTrigger = successTriggers.get(0);

                        JSONObject data = new JSONObject();

                        try {
                            data.put("scope", FLTransaction.transactionScopeToParams(currentScope));

                            if (triggerData.has("in") && !triggerData.optString("in").isEmpty()) {
                                JSONObject base = successTrigger.data.optJSONObject(triggerData.optString("in"));

                                if (base != null) {
                                    Iterator it = base.keys();
                                    while (it.hasNext()) {
                                        String key = (String) it.next();
                                        data.put(key, base.opt(key));
                                    }
                                }

                                successTrigger.data.put(triggerData.optString("in"), data);
                            } else {
                                Iterator it = data.keys();
                                while (it.hasNext()) {
                                    String key = (String) it.next();
                                    successTrigger.data.put(key, data.opt(key));
                                }
                            }

                            headerBackButton.performClick();

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    } else {
                        Intent currentIntent = getIntent();

                        currentIntent.removeExtra("scope");

                        currentIntent.putExtra("scope", FLTransaction.transactionScopeToParams(currentScope));

                        headerBackButton.performClick();
                    }
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        floozApp.setCurrentActivity(this);

        if (FLHelper.isDebuggable())
            ViewServer.get(this).setFocusedWindow(this);
    }

    @Override
    public void onPause() {
        clearReferences();
        super.onPause();
    }

    @Override
    public void onStop() {
        if (successTriggers != null) {
            FLTriggerManager.getInstance().executeTriggerList(successTriggers);
        }

        super.onStop();
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

    @Override
    public void onBackPressed() {
        this.headerBackButton.performClick();
    }

}
