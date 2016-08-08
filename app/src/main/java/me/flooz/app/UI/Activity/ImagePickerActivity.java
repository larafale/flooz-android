package me.flooz.app.UI.Activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;
import java.util.List;

import me.flooz.app.Adapter.ImagePickerAdapter;
import me.flooz.app.Adapter.ImagePickerSuggestAdapter;
import me.flooz.app.App.FloozApplication;
import me.flooz.app.Model.FLError;
import me.flooz.app.Model.FLTrigger;
import me.flooz.app.Model.FLUser;
import me.flooz.app.Network.FloozHttpResponseHandler;
import me.flooz.app.Network.FloozRestClient;
import me.flooz.app.R;
import me.flooz.app.Utils.CustomFonts;
import me.flooz.app.Utils.FLHelper;
import me.flooz.app.Utils.FLTriggerManager;
import me.flooz.app.Utils.ViewServer;

/**
 * Created by Flooz on 04/08/16.
 */
public class ImagePickerActivity extends BaseActivity {

    private FloozApplication floozApp;

    private ImageView headerBackButton;
    private EditText searchTextField;
    private GridView gridView;
    private ListView suggestList;
    private TextView suggestTitle;
    private TextView title;
    private ImageView trademarkImage;
    private RelativeLayout gridBackground;

    private ImagePickerAdapter gridAdapter;
    private ImagePickerSuggestAdapter suggestAdapter;

    private JSONObject triggerData;

    private List<FLTrigger> successTriggers;

    private JSONArray defaultItems;

    private String pickerType;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (FLHelper.isDebuggable())
            ViewServer.get(this).addWindow(this);

        this.floozApp = (FloozApplication) this.getApplicationContext();

        if (getIntent() != null) {
            if (getIntent().hasExtra("triggerData")) {
                try {
                    this.triggerData = new JSONObject(getIntent().getStringExtra("triggerData"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            this.pickerType = getIntent().getStringExtra("type");
        }

        this.setContentView(R.layout.image_picker_activity);

        this.headerBackButton = (ImageView) this.findViewById(R.id.header_item_left);
        this.searchTextField = (EditText) this.findViewById(R.id.image_picker_search);
        this.gridView = (GridView) this.findViewById(R.id.image_picker_list);
        this.suggestList = (ListView) this.findViewById(R.id.image_picker_background_list);
        this.suggestTitle = (TextView) this.findViewById(R.id.image_picker_background_title);
        this.title = (TextView) this.findViewById(R.id.header_title);
        this.trademarkImage = (ImageView) this.findViewById(R.id.image_picker_background_tm);
        this.gridBackground = (RelativeLayout) this.findViewById(R.id.image_picker_background);

        this.gridAdapter = new ImagePickerAdapter(this, this.pickerType);

        this.gridView.setAdapter(this.gridAdapter);

        JSONArray suggests = new JSONArray();

        if (this.pickerType.contentEquals("gif"))
            suggests = FloozRestClient.getInstance().currentTexts.suggestGifs;
        else if (this.pickerType.contentEquals("web"))
            suggests = FloozRestClient.getInstance().currentTexts.suggestWeb;

        this.suggestAdapter = new ImagePickerSuggestAdapter(this, suggests);

        this.suggestList.setAdapter(this.suggestAdapter);

        this.suggestTitle.setTypeface(CustomFonts.customContentLight(this));
        this.title.setTypeface(CustomFonts.customTitleLight(this));

        this.trademarkImage.setColorFilter(this.getResources().getColor(R.color.placeholder));

        FloozRestClient.getInstance().imagesSearch("", this.pickerType, new FloozHttpResponseHandler() {
            @Override
            public void success(Object response) {
                defaultItems = (JSONArray)response;
                gridAdapter.defaultItems = defaultItems;

                if (searchTextField.getText().length() == 0 && (defaultItems == null || defaultItems.length() == 0)) {
                    gridView.setVisibility(View.GONE);
                    gridBackground.setVisibility(View.VISIBLE);
                } else {
                    gridView.setVisibility(View.VISIBLE);
                    gridBackground.setVisibility(View.GONE);
                    gridAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void failure(int statusCode, FLError error) {

            }
        });

        this.headerBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(RESULT_OK, getIntent());
                finish();
                overridePendingTransition(android.R.anim.fade_in, R.anim.slide_down);
            }
        });

        if (this.pickerType.contentEquals("gif")) {
            this.title.setText("Choisir un GIF");
            this.searchTextField.setHint("Rechercher un GIF");
            this.trademarkImage.setVisibility(View.VISIBLE);
        } else if (this.pickerType.contentEquals("web")) {
            this.title.setText("Choisir une image");
            this.searchTextField.setHint("Rechercher une image");
            this.trademarkImage.setVisibility(View.GONE);
        }

        this.searchTextField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (searchTextField.getText().length() == 0 && (defaultItems == null || defaultItems.length() == 0)) {
                    gridView.setVisibility(View.GONE);
                    gridBackground.setVisibility(View.VISIBLE);
                } else {
                    gridView.setVisibility(View.VISIBLE);
                    gridBackground.setVisibility(View.GONE);
                    gridAdapter.search(editable.toString());
                }
            }
        });

        this.suggestList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String tmp = suggestAdapter.getItem(position);

                searchTextField.setText(tmp);

                if (searchTextField.getText().length() == 0 && (defaultItems == null || defaultItems.length() == 0)) {
                    gridView.setVisibility(View.GONE);
                    gridBackground.setVisibility(View.VISIBLE);
                } else {
                    gridView.setVisibility(View.VISIBLE);
                    gridBackground.setVisibility(View.GONE);
                    gridAdapter.search(searchTextField.getText().toString());
                }
            }
        });

        this.gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                JSONObject object = gridAdapter.getItem(position);
                if (object != null) {
                    if (triggerData != null) {
                        successTriggers = FLTriggerManager.convertTriggersJSONArrayToList(triggerData.optJSONArray("success"));
                        FLTrigger successTrigger = successTriggers.get(0);

                        JSONObject data = new JSONObject();

                        try {
                            data.put("imageUrl", object.optString("url"));

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

                        currentIntent.removeExtra("imageUrl");

                        currentIntent.putExtra("imageUrl", object.optString("url"));

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
