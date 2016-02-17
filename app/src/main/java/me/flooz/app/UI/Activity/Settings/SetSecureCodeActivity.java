package me.flooz.app.UI.Activity.Settings;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import me.flooz.app.App.FloozApplication;
import me.flooz.app.Model.FLError;
import me.flooz.app.Network.FloozHttpResponseHandler;
import me.flooz.app.Network.FloozRestClient;
import me.flooz.app.R;
import me.flooz.app.UI.Fragment.Start.StartBaseFragment;
import me.flooz.app.UI.View.NumericKeyboard;
import me.flooz.app.Utils.CustomFonts;
import me.flooz.app.Utils.FLHelper;
import me.flooz.app.Utils.ViewServer;

/**
 * Created by Flooz on 10/22/15.
 */
public class SetSecureCodeActivity extends Activity implements NumericKeyboard.NumericKeyboardDelegate {

    private FloozApplication floozApp;

    private ImageView headerBackButton;
    private LinearLayout codeContainer;
    private ImageView codeChar1;
    private ImageView codeChar2;
    private ImageView codeChar3;
    private ImageView codeChar4;
    private NumericKeyboard keyboard;

    private String currentCode = "";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (FLHelper.isDebuggable())
            ViewServer.get(this).addWindow(this);

        this.floozApp = (FloozApplication) this.getApplicationContext();

        this.setContentView(R.layout.set_secure_code_activity);

        JSONObject triggerData = null;
        if (getIntent() != null && getIntent().hasExtra("triggerData"))
            try {
                triggerData = new JSONObject(getIntent().getStringExtra("triggerData"));
            } catch (JSONException e) {
                e.printStackTrace();
            }

        this.headerBackButton = (ImageView) this.findViewById(R.id.header_item_left);
        TextView title = (TextView) this.findViewById(R.id.header_title);
        TextView hintText = (TextView) this.findViewById(R.id.start_secure_code_hint);
        this.codeContainer = (LinearLayout) this.findViewById(R.id.start_secure_code_container);
        this.codeChar1 = (ImageView) this.findViewById(R.id.start_secure_code_char_1);
        this.codeChar2 = (ImageView) this.findViewById(R.id.start_secure_code_char_2);
        this.codeChar3 = (ImageView) this.findViewById(R.id.start_secure_code_char_3);
        this.codeChar4 = (ImageView) this.findViewById(R.id.start_secure_code_char_4);
        this.keyboard = (NumericKeyboard) this.findViewById(R.id.start_secure_code_keypad);

        title.setTypeface(CustomFonts.customTitleLight(this));
        hintText.setTypeface(CustomFonts.customContentRegular(this));

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

        hintText.setText(R.string.SECORE_CODE_CHOOSE);

        this.keyboard.delegate = this;
        this.keyboard.maxLenght = 4;
    }

    private void refreshCodeContainer() {

        if (this.currentCode.length() >= 1)
            this.codeChar1.setSelected(true);
        else
            this.codeChar1.setSelected(false);

        if (this.currentCode.length() >= 2)
            this.codeChar2.setSelected(true);
        else
            this.codeChar2.setSelected(false);

        if (this.currentCode.length() >= 3)
            this.codeChar3.setSelected(true);
        else
            this.codeChar3.setSelected(false);

        if (this.currentCode.length() == 4) {
            this.codeChar4.setSelected(true);

            Map<String, Object> data = new HashMap<>(1);
            data.put("secureCode", this.currentCode);

            FloozRestClient.getInstance().showLoadView();
            FloozRestClient.getInstance().updateUser(data, new FloozHttpResponseHandler() {
                @Override
                public void success(Object response) {
                    FloozRestClient.getInstance().setSecureCode(currentCode);
                    headerBackButton.performClick();
                }

                @Override
                public void failure(int statusCode, FLError error) {
                }
            });
        }
        else
            this.codeChar4.setSelected(false);
    }

    @Override
    public void keyPressed() {
        this.currentCode = keyboard.value;
        this.refreshCodeContainer();
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
        headerBackButton.performClick();
    }
}
