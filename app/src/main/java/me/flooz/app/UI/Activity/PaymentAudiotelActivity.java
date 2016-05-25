package me.flooz.app.UI.Activity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import me.flooz.app.App.FloozApplication;
import me.flooz.app.Network.FloozRestClient;
import me.flooz.app.R;
import me.flooz.app.Utils.CustomFonts;
import me.flooz.app.Utils.CustomNotificationIntents;
import me.flooz.app.Utils.FLHelper;
import me.flooz.app.Utils.ViewServer;

/**
 * Created by Flooz on 22/05/16.
 */
public class PaymentAudiotelActivity extends BaseActivity {
    private FloozApplication floozApp;

    private ImageView headerBackButton;

    private TextView infosTextview;
    private ImageView numberImage;
    private TextView avalaibleTextview;
    private TextView hintBalance;
    private TextView balance;
    private TextView hintTextview;
    private EditText codeTextfield;
    private Button sendButton;

    public JSONObject floozData;

    private BroadcastReceiver reloadData = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.hasExtra("code"))
                codeTextfield.setText(intent.getStringExtra("code"));
        }
    };

    private BroadcastReceiver reloadBalance = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            balance.setText(FLHelper.trimTrailingZeros( String.format(Locale.US, "%.2f", FloozRestClient.getInstance().currentUser.amount.floatValue())) + " €");
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

        if (triggerData != null && triggerData.has("flooz")) {
            if (triggerData.opt("flooz") instanceof JSONObject)
                this.floozData = triggerData.optJSONObject("flooz");
            else if (triggerData.opt("flooz") instanceof String) {
                try {
                    this.floozData = new JSONObject(triggerData.optString("flooz"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

        floozApp = (FloozApplication) this.getApplicationContext();
        this.setContentView(R.layout.payment_audiotel_activity);

        TextView title = (TextView) this.findViewById(R.id.header_title);
        this.headerBackButton = (ImageView) this.findViewById(R.id.header_item_left);

        this.infosTextview = (TextView) this.findViewById(R.id.payment_audiotel_infos);
        this.numberImage = (ImageView) this.findViewById(R.id.payment_audiotel_img);
        this.avalaibleTextview = (TextView) this.findViewById(R.id.payment_audiotel_avalaible);
        this.hintBalance = (TextView) this.findViewById(R.id.payment_audiotel_balance_hint);
        this.balance = (TextView) this.findViewById(R.id.payment_audiotel_balance);
        this.hintTextview = (TextView) this.findViewById(R.id.payment_audiotel_hint);
        this.codeTextfield = (EditText) this.findViewById(R.id.payment_audiotel_code);
        this.sendButton = (Button) this.findViewById(R.id.payment_audiotel_button);

        title.setTypeface(CustomFonts.customTitleLight(this));
        this.infosTextview.setTypeface(CustomFonts.customContentRegular(this));
        this.avalaibleTextview.setTypeface(CustomFonts.customContentRegular(this));
        this.hintBalance.setTypeface(CustomFonts.customContentRegular(this));
        this.balance.setTypeface(CustomFonts.customContentRegular(this));
        this.hintTextview.setTypeface(CustomFonts.customContentRegular(this));
        this.codeTextfield.setTypeface(CustomFonts.customContentRegular(this));
        this.sendButton.setTypeface(CustomFonts.customContentRegular(this));

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

        if (FloozRestClient.getInstance().currentTexts.audiotelImage != null)
            ImageLoader.getInstance().displayImage(FloozRestClient.getInstance().currentTexts.audiotelImage, this.numberImage);

        if (FloozRestClient.getInstance().currentTexts.audiotelInfos != null)
            this.infosTextview.setText(FloozRestClient.getInstance().currentTexts.audiotelInfos);

        this.numberImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (FloozRestClient.getInstance().currentTexts.audiotelNumber != null)
                    startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel://" + FloozRestClient.getInstance().currentTexts.audiotelNumber)));
            }
        });

        this.sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(getWindow().getDecorView().getRootView().getWindowToken(), 0);

                Map<String, Object> param = new HashMap<>();

                param.put("code", codeTextfield.getText().toString());

                if (floozData != null)
                    param.put("flooz", floozData);

                FloozRestClient.getInstance().showLoadView();
                FloozRestClient.getInstance().cashinAudiotel(param, null);
            }
        });

        LocalBroadcastManager.getInstance(this).registerReceiver(reloadData, new IntentFilter("cashin:audiotel:sync"));
        LocalBroadcastManager.getInstance(this).registerReceiver(reloadBalance, CustomNotificationIntents.filterReloadCurrentUser());
    }

    @Override
    public void onResume() {
        super.onResume();
        if (FLHelper.isDebuggable())
            ViewServer.get(this).setFocusedWindow(this);
        floozApp.setCurrentActivity(this);

        balance.setText(FLHelper.trimTrailingZeros( String.format(Locale.US, "%.2f", FloozRestClient.getInstance().currentUser.amount.floatValue())) + " €");
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

        LocalBroadcastManager.getInstance(this).unregisterReceiver(reloadData);

        super.onDestroy();
    }

    private void clearReferences() {
        Activity currActivity = floozApp.getCurrentActivity();
        if (currActivity != null && currActivity.equals(this))
            floozApp.setCurrentActivity(null);
    }
}
