package me.flooz.app.UI.Fragment.Start;

import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import me.flooz.app.Model.FLError;
import me.flooz.app.Network.FloozHttpResponseHandler;
import me.flooz.app.Network.FloozRestClient;
import me.flooz.app.R;
import me.flooz.app.Utils.CustomFonts;
import scanpay.it.ScanPay;
import scanpay.it.ScanPayActivity;

/**
 * Created by Flooz on 6/15/15.
 */
public class StartCardFragment extends StartBaseFragment {

    private static int RESULT_SCANPAY_ACTIVITY = 45;

    private EditText cardOwner;
    private EditText cardNumber;
    private EditText cardExpires;
    private EditText cardCVV;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.start_card_fragment, null);

        parentActivity.fragmentHistory.clear();
        parentActivity.fragmentHistory.add(this);

        TextView title = (TextView) view.findViewById(R.id.start_card_title);
        this.cardOwner = (EditText) view.findViewById(R.id.start_card_owner);
        this.cardNumber = (EditText) view.findViewById(R.id.start_card_number);
        this.cardExpires = (EditText) view.findViewById(R.id.start_card_expires);
        this.cardCVV = (EditText) view.findViewById(R.id.start_card_cvv);
        ImageView scanpayButton = (ImageView) view.findViewById(R.id.start_card_scanpay);
        Button addCardButton = (Button) view.findViewById(R.id.start_card_add);
        Button skipButton = (Button) view.findViewById(R.id.start_card_skip);
        TextView cardInfo = (TextView) view.findViewById(R.id.start_card_infos);

        title.setTypeface(CustomFonts.customTitleLight(inflater.getContext()));
        cardInfo.setTypeface(CustomFonts.customContentLight(inflater.getContext()));
        cardOwner.setTypeface(CustomFonts.customContentRegular(inflater.getContext()));
        cardNumber.setTypeface(CustomFonts.customContentRegular(inflater.getContext()));
        cardExpires.setTypeface(CustomFonts.customContentRegular(inflater.getContext()));
        cardCVV.setTypeface(CustomFonts.customContentRegular(inflater.getContext()));

        this.cardOwner.setText(FloozRestClient.getInstance().currentUser.fullname);

        this.cardNumber.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > 19)
                    s.delete(18, s.length() - 1);

                if (s.length() == 5 && s.charAt(4) == ' ')
                    s.delete(4, 5);
                else if (s.length() == 5 && s.charAt(4) != ' ')
                    s.insert(4, " ");

                if (s.length() == 10 && s.charAt(9) == ' ')
                    s.delete(9, 10);
                else if (s.length() == 10 && s.charAt(9) != ' ')
                    s.insert(9, " ");

                if (s.length() == 15 && s.charAt(14) == ' ')
                    s.delete(14, 15);
                else if (s.length() == 15 && s.charAt(14) != ' ')
                    s.insert(14, " ");

                if (s.length() == 19)
                    cardExpires.requestFocus();
            }
        });

        this.cardExpires.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > 5)
                    s.delete(5, s.length() - 1);


                if (s.length() == 3 && s.charAt(2) == ' ')
                    s.delete(2, 3);
                else if (s.length() == 3 && s.charAt(2) != '-')
                    s.insert(2, "-");

                if (s.length() == 1 && s.charAt(0) > '1')
                    s.insert(0, "0");

                if (s.length() == 5)
                    cardCVV.requestFocus();
            }
        });

        this.cardCVV.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > 3)
                    s.delete(3, s.length() - 1);

                if (s.length() == 3) {
                    cardOwner.clearFocus();
                    addCardButton.requestFocus();
                    InputMethodManager imm = (InputMethodManager) parentActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(parentActivity.getWindow().getDecorView().getRootView().getWindowToken(), 0);
                }
            }
        });

        this.cardCVV.setOnEditorActionListener((v, actionId, event) -> {
            if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE)) {
                addCardButton.performClick();
            }
            return false;
        });

        scanpayButton.setOnClickListener(v -> {
            Intent scanActivity = new Intent(parentActivity, ScanPayActivity.class);
            scanActivity.putExtra(ScanPay.EXTRA_TOKEN, "be38035037ed6ca3cba7089b");

            scanActivity.putExtra(ScanPay.EXTRA_SHOULD_SHOW_CONFIRMATION_VIEW, false);
            scanActivity.putExtra(ScanPay.EXTRA_SHOULD_SHOW_MANUAL_ENTRY_BUTTON, false);

            parentActivity.startActivityForResult(scanActivity, RESULT_SCANPAY_ACTIVITY);
        });

        addCardButton.setOnClickListener(v -> {

            Map<String, Object> params = new HashMap<>();
            params.put("holder", cardOwner.getText().toString());
            params.put("number", cardNumber.getText().toString());
            params.put("expires", cardExpires.getText().toString());
            params.put("cvv", cardCVV.getText().toString());

            FloozRestClient.getInstance().showLoadView();
            FloozRestClient.getInstance().signupPassStep("card", params, new FloozHttpResponseHandler() {
                @Override
                public void success(Object response) {
                    JSONObject responseObject = (JSONObject) response;
                    StartBaseFragment.handleStepResponse(responseObject, parentActivity);
                }

                @Override
                public void failure(int statusCode, FLError error) {

                }
            });
        });

        skipButton.setOnClickListener(v -> {
            FloozRestClient.getInstance().showLoadView();
            FloozRestClient.getInstance().signupPassStep("cardSkip", null, new FloozHttpResponseHandler() {
                @Override
                public void success(Object response) {
                    JSONObject responseObject = (JSONObject) response;

                    if (responseObject.has("step") && responseObject.optJSONObject("step").has("next")) {
                        if (responseObject.optJSONObject("step").optString("next").contentEquals("signup")) {
                            FloozRestClient.getInstance().showLoadView();
                            FloozRestClient.getInstance().signupPassStep("signup", parentActivity.signupData, new FloozHttpResponseHandler() {
                                @Override
                                public void success(Object response) {
                                    JSONObject responseObject = (JSONObject) response;

                                    FloozRestClient.getInstance().updateCurrentUserAfterSignup(responseObject);

                                    if (responseObject.has("step") && responseObject.optJSONObject("step").has("next")) {
                                        StartBaseFragment nextFragment = StartBaseFragment.getSignupFragmentFromId(responseObject.optJSONObject("step").optString("next"), responseObject.optJSONObject("step"));

                                        if (nextFragment != null) {
                                            parentActivity.changeCurrentPage(nextFragment, R.animator.slide_in_left, R.animator.slide_out_right, R.animator.slide_in_right, R.animator.slide_out_left, true);
                                        }
                                    }
                                }

                                @Override
                                public void failure(int statusCode, FLError error) {

                                }
                            });
                        } else {
                            StartBaseFragment nextFragment = StartBaseFragment.getSignupFragmentFromId(responseObject.optJSONObject("step").optString("next"), responseObject.optJSONObject("step"));

                            if (nextFragment != null) {
                                parentActivity.changeCurrentPage(nextFragment, R.animator.slide_in_left, R.animator.slide_out_right, R.animator.slide_in_right, R.animator.slide_out_left, true);
                            }
                        }
                    }
                }

                @Override
                public void failure(int statusCode, FLError error) {

                }
            });
        });


        if (FloozRestClient.getInstance().currentTexts != null && FloozRestClient.getInstance().currentTexts.cardInfos != null)
            cardInfo.setText(FloozRestClient.getInstance().currentTexts.cardInfos);

        if (this.initData != null) {
            if (this.initData.has("title")) {
                title.setText(this.initData.optString("title"));
            }

            if (this.initData.has("label")) {
                cardInfo.setText(this.initData.optString("label"));
            }
        }

        return view;
    }
}
