package me.flooz.app.UI.Controllers;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import me.flooz.app.App.FloozApplication;
import me.flooz.app.Model.FLCreditCard;
import me.flooz.app.Model.FLError;
import me.flooz.app.Network.FloozHttpResponseHandler;
import me.flooz.app.Network.FloozRestClient;
import me.flooz.app.R;
import me.flooz.app.UI.Activity.HomeActivity;
import me.flooz.app.Utils.CustomFonts;
import me.flooz.app.Utils.CustomNotificationIntents;
import scanpay.it.CreditCard;
import scanpay.it.ScanPay;
import scanpay.it.ScanPayActivity;

/**
 * Created by Flooz on 9/1/15.
 */
public class CreditCardController extends BaseController {

    private static int RESULT_SCANPAY_ACTIVITY = 45;

    private Boolean modal;

    public String cardInfosText;

    private ImageView headerBackButton;

    private TextView creditCardOwner;
    private TextView creditCardNumber;
    private LinearLayout removeCardContainer;
    private LinearLayout createCardContainer;

    private EditText cardOwner;
    private EditText cardNumber;
    private EditText cardExpires;
    private EditText cardCVV;

    private TextView cardInfos;

    private Button addCardButton;

    public Boolean next3DSecure = false;

    private FLCreditCard creditCard;

    public JSONObject floozData;

    private BroadcastReceiver reloadCurrentUserDataReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            reloadCreditCard();
        }
    };

    public CreditCardController(@NonNull View mainView, @NonNull final Activity parentActivity, @NonNull ControllerKind kind) {
        super(mainView, parentActivity, kind);

        this.headerBackButton = (ImageView) this.currentView.findViewById(R.id.header_item_left);
        TextView headerTitle = (TextView) this.currentView.findViewById(R.id.header_title);
        this.cardOwner = (EditText) this.currentView.findViewById(R.id.settings_credit_card_create_owner);
        this.cardNumber = (EditText) this.currentView.findViewById(R.id.settings_credit_card_create_number);
        this.cardExpires = (EditText) this.currentView.findViewById(R.id.settings_credit_card_create_expires);
        this.cardCVV = (EditText) this.currentView.findViewById(R.id.settings_credit_card_create_cvv);
        ImageView scanpayButton = (ImageView) this.currentView.findViewById(R.id.settings_credit_card_create_scanpay);
        this.addCardButton = (Button) this.currentView.findViewById(R.id.settings_credit_card_create_add);
        this.creditCardOwner = (TextView) this.currentView.findViewById(R.id.settings_credit_card_owner);
        this.creditCardNumber = (TextView) this.currentView.findViewById(R.id.settings_credit_card_number);
        LinearLayout removeCreditCardButton = (LinearLayout) this.currentView.findViewById(R.id.settings_credit_card_remove_button);
        TextView removeCreditCardText = (TextView) this.currentView.findViewById(R.id.settings_credit_card_remove_text);
        this.removeCardContainer = (LinearLayout) this.currentView.findViewById(R.id.settings_credit_card_remove_card_view);
        this.createCardContainer = (LinearLayout) this.currentView.findViewById(R.id.settings_credit_card_create_card_view);
        this.cardInfos = (TextView) this.currentView.findViewById(R.id.settings_credit_card_infos);

        headerTitle.setTypeface(CustomFonts.customTitleExtraLight(this.parentActivity));
        this.creditCardOwner.setTypeface(CustomFonts.customCreditCard(this.parentActivity));
        this.creditCardNumber.setTypeface(CustomFonts.customCreditCard(this.parentActivity));
        removeCreditCardText.setTypeface(CustomFonts.customTitleExtraLight(this.parentActivity));
        this.cardOwner.setTypeface(CustomFonts.customContentLight(this.parentActivity));
        this.cardNumber.setTypeface(CustomFonts.customContentLight(this.parentActivity));
        this.cardExpires.setTypeface(CustomFonts.customContentLight(this.parentActivity));
        this.cardCVV.setTypeface(CustomFonts.customContentLight(this.parentActivity));
        this.addCardButton.setTypeface(CustomFonts.customContentLight(this.parentActivity));
        cardInfos.setTypeface(CustomFonts.customContentLight(this.parentActivity));

        if (currentKind == ControllerKind.FRAGMENT_CONTROLLER)
            this.headerBackButton.setImageDrawable(this.parentActivity.getResources().getDrawable(R.drawable.nav_back));

        this.headerBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentKind == ControllerKind.ACTIVITY_CONTROLLER) {
                    parentActivity.finish();
                    parentActivity.overridePendingTransition(android.R.anim.fade_in, R.anim.slide_down);
                } else {
                    ((HomeActivity)parentActivity).popFragmentInCurrentTab();
                }
            }
        });

        removeCreditCardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FloozRestClient.getInstance().showLoadView();
                FloozRestClient.getInstance().removeCreditCard(creditCard.cardId, new FloozHttpResponseHandler() {
                    @Override
                    public void success(Object response) {
                        FloozRestClient.getInstance().currentUser.creditCard = null;
                        reloadCreditCard();
                    }

                    @Override
                    public void failure(int statusCode, FLError error) {

                    }
                });
            }
        });

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

        this.cardCVV.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE)) {
                    addCardButton.performClick();
                }
                return false;
            }
        });

        scanpayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent scanActivity = new Intent(parentActivity, ScanPayActivity.class);
                scanActivity.putExtra(ScanPay.EXTRA_TOKEN, "be38035037ed6ca3cba7089b");

                scanActivity.putExtra(ScanPay.EXTRA_SHOULD_SHOW_CONFIRMATION_VIEW, false);
                scanActivity.putExtra(ScanPay.EXTRA_SHOULD_SHOW_MANUAL_ENTRY_BUTTON, false);

                parentActivity.startActivityForResult(scanActivity, RESULT_SCANPAY_ACTIVITY);
            }
        });

        this.addCardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FloozRestClient.getInstance().showLoadView();

                Map<String, Object> params = new HashMap<>(4);

                params.put("holder", cardOwner.getText().toString());
                params.put("number", cardNumber.getText().toString().replace(" ", ""));
                params.put("expires", cardExpires.getText().toString());
                params.put("cvv", cardCVV.getText().toString());

                if (floozData != null)
                    params.put("flooz", floozData);

                FloozRestClient.getInstance().createCreditCard(params, false, new FloozHttpResponseHandler() {
                    @Override
                    public void success(Object response) {
                        creditCard = new FLCreditCard();
                        creditCard.owner = cardOwner.getText().toString();
                        creditCard.number = cardNumber.getText().toString();
                        creditCard.expires = cardExpires.getText().toString();
                        creditCard.cvv = cardCVV.getText().toString();

                        FloozRestClient.getInstance().currentUser.creditCard = creditCard;
                        reloadCreditCard();
                        FloozRestClient.getInstance().updateCurrentUser(null);
                    }

                    @Override
                    public void failure(int statusCode, FLError error) {

                    }
                });
            }
        });

        if (FloozRestClient.getInstance().currentTexts != null && FloozRestClient.getInstance().currentTexts.cardInfos != null && !FloozRestClient.getInstance().currentTexts.cardInfos.isEmpty())
            cardInfosText = FloozRestClient.getInstance().currentTexts.cardInfos;

        if (this.currentKind == ControllerKind.ACTIVITY_CONTROLLER) {
            if (parentActivity.getIntent().getStringExtra("label") != null && !parentActivity.getIntent().getStringExtra("label").isEmpty()) {
                cardInfosText = parentActivity.getIntent().getStringExtra("label");
            }
        }

        this.reloadCreditCard();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RESULT_SCANPAY_ACTIVITY && resultCode == ScanPay.RESULT_SCAN_SUCCESS)
        {
            CreditCard creditCard = data.getParcelableExtra(ScanPay.EXTRA_CREDIT_CARD);
            creditCardNumber.setText(creditCard.number);
            cardExpires.setText(creditCard.month + "/" + creditCard.year);
            cardCVV.setText(creditCard.cvv);
        }
    }

    @Override
    public void onBackPressed() {
        this.headerBackButton.performClick();
    }

    @Override
    public void onStart() {
        if (!TextUtils.isEmpty(cardInfosText))
            cardInfos.setText(cardInfosText);
    }

    @Override
    public void onResume() {
        this.reloadCreditCard();

        FloozRestClient.getInstance().updateCurrentUser(new FloozHttpResponseHandler() {
            @Override
            public void success(Object response) {
                reloadCreditCard();
            }

            @Override
            public void failure(int statusCode, FLError error) {

            }
        });

        LocalBroadcastManager.getInstance(FloozApplication.getAppContext()).registerReceiver(reloadCurrentUserDataReceiver,
                CustomNotificationIntents.filterReloadCurrentUser());
    }

    @Override
    public void onPause() {
        LocalBroadcastManager.getInstance(FloozApplication.getAppContext()).unregisterReceiver(reloadCurrentUserDataReceiver);
    }

    public void reloadCreditCard() {
        this.creditCard = FloozRestClient.getInstance().currentUser.creditCard;

        if (this.creditCard != null && this.creditCard.cardId != null) {
            this.createCardContainer.setVisibility(View.GONE);
            this.removeCardContainer.setVisibility(View.VISIBLE);
            this.creditCardNumber.setText(this.creditCard.number.substring(0, 4) + " **** **** " + this.creditCard.number.substring(12, 16));
            this.creditCardOwner.setText(this.creditCard.owner);
        } else {
            this.removeCardContainer.setVisibility(View.GONE);
            this.createCardContainer.setVisibility(View.VISIBLE);

            if (this.creditCard != null) {
                this.cardOwner.setText(this.creditCard.owner);
                this.cardNumber.setText(this.creditCard.number);
                this.cardExpires.setText(this.creditCard.expires);
                this.cardCVV.setText(this.creditCard.cvv);
            } else {
                this.cardOwner.setText(FloozRestClient.getInstance().currentUser.fullname);
                this.cardNumber.setText("");
                this.cardCVV.setText("");
                this.cardExpires.setText("");
            }

            this.cardNumber.requestFocus();
        }
    }
}