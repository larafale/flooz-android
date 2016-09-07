package me.flooz.app.UI.Activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import me.flooz.app.App.FloozApplication;
import me.flooz.app.Model.FLTrigger;
import me.flooz.app.Model.FLUser;
import me.flooz.app.R;
import me.flooz.app.UI.View.FLShopFields.FLShopField;
import me.flooz.app.UI.View.FLShopFields.FLShopStepperField;
import me.flooz.app.UI.View.FLShopFields.FLShopSwitchField;
import me.flooz.app.UI.View.FLShopFields.FLShopTextField;
import me.flooz.app.Utils.CustomFonts;
import me.flooz.app.Utils.FLHelper;
import me.flooz.app.Utils.FLTriggerManager;
import me.flooz.app.Utils.ViewServer;

/**
 * Created by Flooz on 31/08/16.
 */
public class ShopParamActivity extends BaseActivity implements FLShopField.FLShopFieldDelegate {
    private FloozApplication floozApp;

    private List<String> buttonsString = new ArrayList<>();
    private List<JSONArray> buttonsAction = new ArrayList<>();

    private ImageView headerBackButton;
    private TextView titleTextview;

    private TextView headerTextview;
    private LinearLayout fieldsContainer;

    private LinearLayout btnContainer1;
    private LinearLayout btnContainer2;

    private Button btn1;
    private Button btn2;
    private Button btn3;
    private Button btn4;

    private View spacer1;
    private View spacer2;

    private JSONObject triggerData;

    private JSONObject paramsData;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (FLHelper.isDebuggable())
            ViewServer.get(this).addWindow(this);

        this.floozApp = (FloozApplication) this.getApplicationContext();

        this.paramsData = new JSONObject();

        if (getIntent() != null && getIntent().hasExtra("triggerData"))
            try {
                this.triggerData = new JSONObject(getIntent().getStringExtra("triggerData"));
            } catch (JSONException e) {
                e.printStackTrace();
            }

        if (this.triggerData.has("button")) {
            buttonsString.add(this.triggerData.optString("button"));

            if (this.triggerData.has("triggers")) {
                buttonsAction.add(this.triggerData.optJSONArray("triggers"));
            } else {
                buttonsAction.add(new JSONArray());
            }
        }

        if (this.triggerData.has("buttons")) {
            for (int i = 0; i < this.triggerData.optJSONArray("buttons").length(); i++) {
                JSONObject button = this.triggerData.optJSONArray("buttons").optJSONObject(i);
                if (button.has("title")) {
                    buttonsString.add(button.optString("title"));

                    if (button.has("triggers"))
                        buttonsAction.add(button.optJSONArray("triggers"));
                    else
                        buttonsAction.add(new JSONArray());
                }
            }
        }

        if (buttonsString.size() == 0) {
            buttonsString.add(this.getResources().getString(R.string.GLOBAL_OK));

            if (this.triggerData.has("triggers")) {
                buttonsAction.add(this.triggerData.optJSONArray("triggers"));
            } else {
                buttonsAction.add(new JSONArray());
            }
        }

        this.setContentView(R.layout.shop_param_activity);

        this.headerBackButton = (ImageView) this.findViewById(R.id.header_item_left);
        this.titleTextview = (TextView) this.findViewById(R.id.header_title);
        this.headerTextview = (TextView) this.findViewById(R.id.shop_param_header_text);
        this.fieldsContainer = (LinearLayout) this.findViewById(R.id.shop_param_fields);

        this.btnContainer1 = (LinearLayout) this.findViewById(R.id.shop_param_container1);
        this.btnContainer2 = (LinearLayout) this.findViewById(R.id.shop_param_container2);

        this.btn1 = (Button) this.findViewById(R.id.shop_param_btn1);
        this.btn2 = (Button) this.findViewById(R.id.shop_param_btn2);
        this.btn3 = (Button) this.findViewById(R.id.shop_param_btn3);
        this.btn4 = (Button) this.findViewById(R.id.shop_param_btn4);

        this.spacer1 = this.findViewById(R.id.shop_param_spacer1);
        this.spacer2 = this.findViewById(R.id.shop_param_spacer2);

        this.titleTextview.setTypeface(CustomFonts.customTitleLight(this));
        this.headerTextview.setTypeface(CustomFonts.customContentRegular(this));
        this.btn1.setTypeface(CustomFonts.customContentRegular(this));
        this.btn2.setTypeface(CustomFonts.customContentRegular(this));
        this.btn3.setTypeface(CustomFonts.customContentRegular(this));
        this.btn4.setTypeface(CustomFonts.customContentRegular(this));

        if (this.triggerData != null) {
            if (triggerData.has("close") && !triggerData.optBoolean("close")) {
                this.headerBackButton.setVisibility(View.GONE);
            }

            if (triggerData.has("title") && !triggerData.optString("title").isEmpty()) {
                this.titleTextview.setText(triggerData.optString("title"));
            }

            if (triggerData.has("header") && !triggerData.optString("header").isEmpty()) {
                this.headerTextview.setText(triggerData.optString("header"));
                this.headerTextview.setVisibility(View.VISIBLE);
            } else {
                this.headerTextview.setVisibility(View.GONE);
            }

            if (triggerData.has("fields") && triggerData.optJSONArray("fields").length() > 0) {
                for (int i = 0; i < triggerData.optJSONArray("fields").length(); i++) {
                    JSONObject field = triggerData.optJSONArray("fields").optJSONObject(i);

                    String fieldType = field.optString("type");

                    FLShopField shopField = null;

                    if (fieldType.contentEquals("stepper")) {
                        shopField = new FLShopStepperField(this, field, this);
                    }
                    else if (fieldType.contentEquals("switch")) {
                        shopField = new FLShopSwitchField(this, field, this);
                    }
                    else if (fieldType.indexOf("textfield") == 0) {
                        shopField = new FLShopTextField(this, field, this);
                    }

                    if (shopField != null)
                        this.fieldsContainer.addView(shopField);
                }
            }
        }

        this.headerBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (triggerData != null && triggerData.has("close") && !triggerData.optBoolean("close"))
                    return;

                setResult(RESULT_OK, getIntent());
                ShopParamActivity.this.finish();
                ShopParamActivity.this.overridePendingTransition(android.R.anim.fade_in, R.anim.slide_down);
            }
        });

        View.OnClickListener clickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int index = 0;

                if (v == btn1)
                    index = 0;
                else if (v == btn2)
                    index = 1;
                else if (v == btn3)
                    index = 2;
                else if (v == btn4)
                    index = 3;

                List<FLTrigger> successTriggers = FLTriggerManager.convertTriggersJSONArrayToList(buttonsAction.get(index));
                FLTrigger successTrigger = successTriggers.get(0);

                try {
                    if (triggerData.has("in") && !triggerData.optString("in").isEmpty()) {
                        JSONObject base = successTrigger.data.optJSONObject(triggerData.optString("in"));

                        if (base != null) {
                            Iterator it = base.keys();
                            while (it.hasNext()) {
                                String key = (String) it.next();
                                paramsData.put(key, base.opt(key));
                            }
                        }

                        successTrigger.data.put(triggerData.optString("in"), paramsData);
                    } else {
                        Iterator it = paramsData.keys();
                        while (it.hasNext()) {
                            String key = (String) it.next();
                            successTrigger.data.put(key, paramsData.opt(key));
                        }
                    }

                    headerBackButton.performClick();

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                FLTriggerManager.getInstance().executeTriggerList(successTriggers);
            }
        };

        btn1.setOnClickListener(clickListener);
        btn2.setOnClickListener(clickListener);
        btn3.setOnClickListener(clickListener);
        btn4.setOnClickListener(clickListener);

        btn1.setText(buttonsString.get(0));

        if (buttonsString.size() < 2) {
            btn2.setVisibility(View.GONE);
            spacer1.setVisibility(View.GONE);
        } else {
            btn2.setVisibility(View.VISIBLE);
            spacer1.setVisibility(View.VISIBLE);
            btn2.setText(buttonsString.get(1));
        }

        if (buttonsString.size() < 3) {
            btnContainer2.setVisibility(View.GONE);
        } else {
            btnContainer2.setVisibility(View.VISIBLE);

            btn3.setText(buttonsString.get(2));

            if (buttonsString.size() < 4) {
                btn4.setVisibility(View.GONE);
                spacer2.setVisibility(View.GONE);
            } else {
                btn4.setVisibility(View.VISIBLE);
                spacer2.setVisibility(View.VISIBLE);
                btn4.setText(buttonsString.get(3));
            }
        }
    }


    @Override
    public void valueChanged(String key, Object value) {
        try {
            paramsData.put(key, value);
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
