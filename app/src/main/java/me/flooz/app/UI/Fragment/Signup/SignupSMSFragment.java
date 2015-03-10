package me.flooz.app.UI.Fragment.Signup;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import me.flooz.app.Model.FLError;
import me.flooz.app.Network.FloozHttpResponseHandler;
import me.flooz.app.Network.FloozRestClient;
import me.flooz.app.R;
import me.flooz.app.UI.View.NumericKeyboard;
import me.flooz.app.Utils.CustomFonts;

/**
 * Created by Flooz on 1/7/15.
 */
public class SignupSMSFragment extends SignupBaseFragment implements NumericKeyboard.NumericKeyboardDelegate {

    private Button nextButton;
    private EditText codeTextfield;
    private NumericKeyboard keyboard;
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
        View view = inflater.inflate(R.layout.signup_sms_fragment, null);

        codeTextfield = (EditText) view.findViewById(R.id.signup_sms_textfield);
        nextButton = (Button) view.findViewById(R.id.signup_sms_next);
        keyboard = (NumericKeyboard) view.findViewById(R.id.signup_sms_keypad);

        keyboard.delegate = this;

        keyboard.maxLenght = 4;

        codeTextfield.requestFocus();
        codeTextfield.setTypeface(CustomFonts.customContentLight(inflater.getContext()));

        codeTextfield.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                v.onTouchEvent(event);
                InputMethodManager imm = (InputMethodManager)v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
                return true;
            }
        });

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (codeTextfield.getText().length() == 4) {
                    parentActivity.userData.smsCode = codeTextfield.getText().toString();
                    FloozRestClient.getInstance().showLoadView();
                    FloozRestClient.getInstance().signupPassStep("sms", parentActivity.userData.getParamsForStep(false), new FloozHttpResponseHandler() {
                        @Override
                        public void success(Object response) {
                            timer = null;
                            parentActivity.gotToNextPage();
                        }

                        @Override
                        public void failure(int statusCode, FLError error) {

                        }
                    });
                } else {
                    FloozRestClient.getInstance().sendSignupSMS(parentActivity.userData.phone);
                    countdown = maxCountdown;
                    String countDownValue = String.format(inflater.getContext().getResources().getString(R.string.SIGNUP_SMS_RESEND), countdown);
                    nextButton.setText(countDownValue);
                    nextButton.setEnabled(false);
                }
            }
        });

        if (parentActivity.userData.smsCode != null && !parentActivity.userData.smsCode.isEmpty()) {
            codeTextfield.setText(parentActivity.userData.smsCode);
            keyboard.value = parentActivity.userData.smsCode;
            codeTextfield.setSelection(keyboard.value.length());

            if (keyboard.value.length() >= 4) {
                nextButton.setText(R.string.GLOBAL_VALIDATE);
                nextButton.setEnabled(true);
            }
        }


        FloozRestClient.getInstance().sendSignupSMS(parentActivity.userData.phone);
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

                    if (codeTextfield.getText().length() >= 4)
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
    public void keyPressed() {
        codeTextfield.setText(keyboard.value);
        if (keyboard.value.length() >= 4) {
            nextButton.setText(R.string.GLOBAL_VALIDATE);
            nextButton.setEnabled(true);
        } else if (keyboard.value.length() > 0) {
            nextButton.setText(R.string.GLOBAL_VALIDATE);
            nextButton.setEnabled(false);
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
        codeTextfield.setSelection(keyboard.value.length());
    }
}
