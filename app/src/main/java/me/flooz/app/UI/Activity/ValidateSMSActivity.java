package me.flooz.app.UI.Activity;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import me.flooz.app.App.FloozApplication;
import me.flooz.app.Model.FLError;
import me.flooz.app.Network.FloozHttpResponseHandler;
import me.flooz.app.Network.FloozRestClient;
import me.flooz.app.R;
import me.flooz.app.Utils.CustomFonts;
import me.flooz.app.Utils.FLHelper;
import me.flooz.app.Utils.ViewServer;

/**
 * Created by Flooz on 10/22/15.
 */
public class ValidateSMSActivity extends Activity {

    private FloozApplication floozApp;

    private ImageView headerBackButton;

    private Button nextButton;
    private EditText codeTextfield;
    private Handler timer;
    private int countdown;
    private int maxCountdown = 60;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (FLHelper.isDebuggable())
            ViewServer.get(this).addWindow(this);

        this.floozApp = (FloozApplication) this.getApplicationContext();

        JSONObject triggerData = null;
        if (getIntent() != null && getIntent().hasExtra("triggerData"))
            try {
                triggerData = new JSONObject(getIntent().getStringExtra("triggerData"));
            } catch (JSONException e) {
                e.printStackTrace();
            }

        this.setContentView(R.layout.validate_sms_activity);

        this.headerBackButton = (ImageView) this.findViewById(R.id.header_item_left);
        TextView title = (TextView) this.findViewById(R.id.header_title);
        codeTextfield = (EditText) this.findViewById(R.id.start_sms_textfield);
        nextButton = (Button) this.findViewById(R.id.start_sms_next);

        codeTextfield.requestFocus();
        title.setTypeface(CustomFonts.customTitleLight(this));
        codeTextfield.setTypeface(CustomFonts.customContentRegular(this));

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

        codeTextfield.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > 0) {
                    nextButton.setText(R.string.GLOBAL_VALIDATE);
                    if (!nextButton.isEnabled())
                        nextButton.setEnabled(true);
                } else {
                    String countDownValue;

                    if (countdown > 0)
                        countDownValue = String.format(getResources().getString(R.string.SIGNUP_SMS_RESEND), countdown);
                    else
                        countDownValue = getResources().getString(R.string.SIGNUP_SMS_RESEND_EMPTY);

                    nextButton.setText(countDownValue);
                    if (countdown > 0)
                        nextButton.setEnabled(false);
                    else
                        nextButton.setEnabled(true);
                }
            }
        });

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (codeTextfield.getText().length() > 0) {
                    FloozRestClient.getInstance().showLoadView();
                    FloozRestClient.getInstance().checkSMSForUser(codeTextfield.getText().toString(), new FloozHttpResponseHandler() {
                        @Override
                        public void success(Object response) {
                            timer = null;
                            headerBackButton.performClick();
                        }

                        @Override
                        public void failure(int statusCode, FLError error) {

                        }
                    });
                } else {
                    FloozRestClient.getInstance().sendSignupSMS(FloozRestClient.getInstance().currentUser.phone);
                    countdown = maxCountdown;
                    String countDownValue = String.format(getResources().getString(R.string.SIGNUP_SMS_RESEND), countdown);
                    nextButton.setText(countDownValue);
                    nextButton.setEnabled(false);
                }
            }
        });

        FloozRestClient.getInstance().sendSignupSMS(FloozRestClient.getInstance().currentUser.phone);
        countdown = maxCountdown;

        timer = new Handler();
        timer.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (countdown > 0)
                    --countdown;

                String countDownValue;

                if (countdown > 0)
                    countDownValue = String.format(getResources().getString(R.string.SIGNUP_SMS_RESEND), countdown);
                else
                    countDownValue = getResources().getString(R.string.SIGNUP_SMS_RESEND_EMPTY);

                if (codeTextfield.getText().length() > 0) {
                    nextButton.setText(R.string.GLOBAL_VALIDATE);
                    if (!nextButton.isEnabled())
                        nextButton.setEnabled(true);
                } else {
                    nextButton.setText(countDownValue);
                    if (countdown > 0)
                        nextButton.setEnabled(false);
                    else
                        nextButton.setEnabled(true);
                }

                if (timer != null)
                    timer.postDelayed(this, 1000);
            }
        }, 1000);
    }

    @Override
    public void onResume() {
        super.onResume();
        floozApp.setCurrentActivity(this);

        if (FLHelper.isDebuggable())
            ViewServer.get(this).setFocusedWindow(this);

        final InputMethodManager imm = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(codeTextfield, InputMethodManager.SHOW_IMPLICIT);
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
