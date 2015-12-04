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
import me.flooz.app.App.FloozApplication;
import me.flooz.app.R;
import me.flooz.app.Utils.FLHelper;
import me.flooz.app.Utils.ViewServer;
import se.emilsjolander.stickylistheaders.StickyListHeadersListView;

/**
 * Created by Flooz on 12/3/15.
 */
public class LocationActivity extends Activity implements LocationListAdapter.LocationListAdapterDelegate {

    private LocationActivity instance;
    private FloozApplication floozApp;

    private ImageView headerBackButton;
    private ImageView clearSearchButton;
    private EditText searchTextField;

    private LocationListAdapter listAdapter;
    private StickyListHeadersListView resultList;

    private JSONObject geo;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (FLHelper.isDebuggable())
            ViewServer.get(this).addWindow(this);

        this.instance = this;
        this.floozApp = (FloozApplication) this.getApplicationContext();

        if (getIntent() != null && getIntent().hasExtra("geo")) {
            try {
                geo = new JSONObject(getIntent().getStringExtra("geo"));
            } catch (JSONException e) {
                geo = null;
                e.printStackTrace();
            }
        } else {
            geo = null;
        }

        this.setContentView(R.layout.location_activity);

        this.headerBackButton = (ImageView) this.findViewById(R.id.header_item_left);
        this.clearSearchButton = (ImageView) this.findViewById(R.id.location_search_clear);
        this.searchTextField = (EditText) this.findViewById(R.id.location_search_textfield);
        this.resultList = (StickyListHeadersListView) this.findViewById(R.id.location_result_list);

        this.listAdapter = new LocationListAdapter(this);
        this.listAdapter.delegate = this;
        if (geo != null)
            this.listAdapter.selectedLocation = geo;

        resultList.setAdapter(this.listAdapter);

        this.headerBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(RESULT_OK, getIntent());
                finish();
                overridePendingTransition(android.R.anim.fade_in, R.anim.slide_down);
            }
        });

        this.clearSearchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchTextField.setText("");
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
                if (editable.length() > 0) {
                    clearSearchButton.setVisibility(View.VISIBLE);
                    listAdapter.searchPlace(searchTextField.getText().toString());
                } else {
                    clearSearchButton.setVisibility(View.GONE);
                    listAdapter.searchPlace(editable.toString());
                }
            }
        });

        this.resultList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                JSONObject object = listAdapter.getItem(position);
                if (object != null) {
                    geo = object;
                    listAdapter.selectedLocation = geo;
                    getIntent().removeExtra("geo");
                    getIntent().putExtra("geo", geo.toString());
                    headerBackButton.performClick();
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

    @Override
    public void clearCurrentSelection() {
        getIntent().removeExtra("geo");
        this.geo = null;
        this.listAdapter.selectedLocation = null;
        this.listAdapter.notifyDataSetChanged();
    }
}
