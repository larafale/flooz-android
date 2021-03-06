package me.flooz.app.UI.Fragment.Start;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONObject;

import me.flooz.app.Model.FLError;
import me.flooz.app.Network.FloozHttpResponseHandler;
import me.flooz.app.Network.FloozRestClient;
import me.flooz.app.R;
import me.flooz.app.UI.View.NumericKeyboard;
import me.flooz.app.Utils.CustomFonts;
import me.flooz.app.Utils.FLHelper;

/**
 * Created by Flooz on 6/15/15.
 */
public class StartSMSFragment extends StartBaseFragment {

    private Button nextButton;
    private EditText codeTextfield;
    private Handler timer;
    private int countdown;
    private int maxCountdown = 60;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.start_sms_fragment, null);

        TextView title = (TextView) view.findViewById(R.id.start_sms_title);
        codeTextfield = (EditText) view.findViewById(R.id.start_sms_textfield);
        nextButton = (Button) view.findViewById(R.id.start_sms_next);

        codeTextfield.requestFocus();
        title.setTypeface(CustomFonts.customTitleLight(inflater.getContext()));
        codeTextfield.setTypeface(CustomFonts.customContentRegular(inflater.getContext()));

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
                        countDownValue = String.format(parentActivity.getResources().getString(R.string.SIGNUP_SMS_RESEND), countdown);
                    else
                        countDownValue = parentActivity.getResources().getString(R.string.SIGNUP_SMS_RESEND_EMPTY);

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
                    parentActivity.signupData.put("smscode", codeTextfield.getText().toString());
                    FloozRestClient.getInstance().showLoadView();
                    FloozRestClient.getInstance().signupPassStep("sms", parentActivity.signupData, new FloozHttpResponseHandler() {
                        @Override
                        public void success(Object response) {
                            timer = null;
                            JSONObject responseObject = (JSONObject) response;
                            StartBaseFragment.handleStepResponse(responseObject, parentActivity);
                        }

                        @Override
                        public void failure(int statusCode, FLError error) {

                        }
                    });
                } else {
                    FloozRestClient.getInstance().sendSignupSMS(FLHelper.fullPhone((String)parentActivity.signupData.get("phone"), (String)parentActivity.signupData.get("country")));
                    countdown = maxCountdown;
                    String countDownValue = String.format(inflater.getContext().getResources().getString(R.string.SIGNUP_SMS_RESEND), countdown);
                    nextButton.setText(countDownValue);
                    nextButton.setEnabled(false);
                }
            }
        });

        if (parentActivity.signupData.get("smscode") != null && !((String)parentActivity.signupData.get("smscode")).isEmpty()) {
            codeTextfield.setText((String)parentActivity.signupData.get("smscode"));
            codeTextfield.setSelection(codeTextfield.getText().length());

            if (codeTextfield.getText().length() > 0) {
                nextButton.setText(R.string.GLOBAL_VALIDATE);
                nextButton.setEnabled(true);
            }
        }

        FloozRestClient.getInstance().sendSignupSMS(FLHelper.fullPhone((String)parentActivity.signupData.get("phone"), (String)parentActivity.signupData.get("country")));
        countdown = maxCountdown;

        timer = new Handler();
        timer.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (countdown > 0)
                    --countdown;

                String countDownValue;

                if (countdown > 0)
                    countDownValue = String.format(inflater.getContext().getResources().getString(R.string.SIGNUP_SMS_RESEND), countdown);
                else
                    countDownValue = inflater.getContext().getResources().getString(R.string.SIGNUP_SMS_RESEND_EMPTY);

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

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        final InputMethodManager imm = (InputMethodManager) this.parentActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(codeTextfield, InputMethodManager.SHOW_IMPLICIT);
    }
}
