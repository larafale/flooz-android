package me.flooz.app.UI.Activity;

import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;

import org.json.JSONException;
import org.json.JSONObject;

import me.flooz.app.Adapter.LocationListAdapter;
import me.flooz.app.Adapter.ShopListAdapter;
import me.flooz.app.App.FloozApplication;
import me.flooz.app.R;
import me.flooz.app.UI.View.TimelineListView;
import me.flooz.app.Utils.FLHelper;
import me.flooz.app.Utils.ViewServer;

/**
 * Created by Flooz on 26/08/16.
 */
public class ShopListActivity extends BaseActivity {

    private FloozApplication floozApp;

    private ImageView headerBackButton;
    private ImageView searchButton;
    private EditText searchTextField;

    private ShopListAdapter listAdapter;
    private TimelineListView resultList;

    private JSONObject triggerData;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (FLHelper.isDebuggable())
            ViewServer.get(this).addWindow(this);

        this.floozApp = (FloozApplication) this.getApplicationContext();

        if (getIntent() != null && getIntent().hasExtra("triggerData")) {
            try {
                this.triggerData = new JSONObject(getIntent().getStringExtra("triggerData"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        this.setContentView(R.layout.shop_list_activity);

        this.headerBackButton = (ImageView) this.findViewById(R.id.header_item_left);
        this.searchTextField = (EditText) this.findViewById(R.id.shop_list_search);
        this.searchButton = (ImageView) this.findViewById(R.id.header_item_right);
        this.resultList = (TimelineListView) this.findViewById(R.id.shop_list);

        this.listAdapter = new ShopListAdapter(this, this.triggerData.optString("loadUrl"));

        resultList.setAdapter(this.listAdapter);

        this.searchButton.setColorFilter(this.getResources().getColor(R.color.blue));

        if (this.triggerData.has("search") && this.triggerData.optBoolean("search")) {
            this.searchButton.setVisibility(View.VISIBLE);
        } else
            this.searchButton.setVisibility(View.GONE);

        this.headerBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(RESULT_OK, getIntent());
                finish();
                overridePendingTransition(android.R.anim.fade_in, R.anim.slide_down);
            }
        });

        this.searchTextField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (searchTextField.getText().length() > 0) {
                    listAdapter.search(searchTextField.getText().toString());
                } else {
                    listAdapter.stopSearch();
                }
            }
        });

        this.resultList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

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
