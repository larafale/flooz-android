package me.flooz.app.UI.Activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.makeramen.roundedimageview.RoundedImageView;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.json.JSONException;
import org.json.JSONObject;

import me.flooz.app.App.FloozApplication;
import me.flooz.app.Model.FLShopItem;
import me.flooz.app.R;
import me.flooz.app.Utils.CustomFonts;
import me.flooz.app.Utils.FLHelper;
import me.flooz.app.Utils.FLTriggerManager;
import me.flooz.app.Utils.ViewServer;

/**
 * Created by Flooz on 31/08/16.
 */
public class ShopParamActivity extends BaseActivity {
    private FloozApplication floozApp;

    private ImageView headerBackButton;
    private TextView titleTextview;

    private JSONObject triggerData;

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
        }

        this.setContentView(R.layout.shop_item_activity);

        this.headerBackButton = (ImageView) this.findViewById(R.id.header_item_left);
        this.titleTextview = (TextView) this.findViewById(R.id.header_title);

        this.titleTextview.setTypeface(CustomFonts.customTitleLight(this));

        if (this.triggerData != null) {
            if (triggerData.has("close") && !triggerData.optBoolean("close")) {
                this.headerBackButton.setVisibility(View.GONE);
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
