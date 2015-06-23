package me.flooz.app.UI.Activity.Settings;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Editable;
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

import me.flooz.app.App.FloozApplication;
import me.flooz.app.Model.FLCreditCard;
import me.flooz.app.Model.FLError;
import me.flooz.app.Network.FloozHttpResponseHandler;
import me.flooz.app.Network.FloozRestClient;
import me.flooz.app.R;
import me.flooz.app.Utils.CustomFonts;
import me.flooz.app.Utils.CustomNotificationIntents;
import me.flooz.app.Utils.FLHelper;
import me.flooz.app.Utils.ViewServer;
import scanpay.it.CreditCard;
import scanpay.it.ScanPay;
import scanpay.it.ScanPayActivity;

/**
 * Created by Flooz on 3/10/15.
 */
public class CreditCardSettingsActivity extends Activity {

    private static int RESULT_SCANPAY_ACTIVITY = 45;

    private CreditCardSettingsActivity instance;
    private FloozApplication floozApp;
    private Boolean modal;

    private ImageView headerBackButton;

    private TextView creditCardOwner;
    private TextView creditCardNumber;
    private LinearLayout removeCardContainer;
    private LinearLayout createCardContainer;

    private EditText cardOwner;
    private EditText cardNumber;
    private EditText cardExpires;
    private EditText cardCVV;

    private Button addCardButton;

    public Boolean next3DSecure = false;

    private FLCreditCard creditCard;


    private BroadcastReceiver reloadCurrentUserDataReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            reloadCreditCard();
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (FLHelper.isDebuggable())
            ViewServer.get(this).addWindow(this);

        this.instance = this;
        this.floozApp = (FloozApplication) this.getApplicationContext();
        this.modal = getIntent().getBooleanExtra("modal", false);

        this.setContentView(R.layout.settings_credit_card_fragment);

        this.headerBackButton = (ImageView) this.findViewById(R.id.settings_credit_card_header_back);
        TextView headerTitle = (TextView) this.findViewById(R.id.settings_credit_card_header_title);
        this.cardOwner = (EditText) this.findViewById(R.id.settings_credit_card_create_owner);
        this.cardNumber = (EditText) this.findViewById(R.id.settings_credit_card_create_number);
        this.cardExpires = (EditText) this.findViewById(R.id.settings_credit_card_create_expires);
        this.cardCVV = (EditText) this.findViewById(R.id.settings_credit_card_create_cvv);
        ImageView scanpayButton = (ImageView) this.findViewById(R.id.settings_credit_card_create_scanpay);
        this.addCardButton = (Button) this.findViewById(R.id.settings_credit_card_create_add);
        this.creditCardOwner = (TextView) this.findViewById(R.id.settings_credit_card_owner);
        this.creditCardNumber = (TextView) this.findViewById(R.id.settings_credit_card_number);
        LinearLayout removeCreditCardButton = (LinearLayout) this.findViewById(R.id.settings_credit_card_remove_button);
        TextView removeCreditCardText = (TextView) this.findViewById(R.id.settings_credit_card_remove_text);
        this.removeCardContainer = (LinearLayout) this.findViewById(R.id.settings_credit_card_remove_card_view);
        this.createCardContainer = (LinearLayout) this.findViewById(R.id.settings_credit_card_create_card_view);
        TextView cardInfos = (TextView) this.findViewById(R.id.settings_credit_card_infos);

        headerTitle.setTypeface(CustomFonts.customTitleExtraLight(this));
        this.creditCardOwner.setTypeface(CustomFonts.customContentRegular(this));
        this.creditCardNumber.setTypeface(CustomFonts.customTitleLight(this));
        removeCreditCardText.setTypeface(CustomFonts.customTitleExtraLight(this));
        this.cardOwner.setTypeface(CustomFonts.customContentLight(this));
        this.cardNumber.setTypeface(CustomFonts.customContentLight(this));
        this.cardExpires.setTypeface(CustomFonts.customContentLight(this));
        this.cardCVV.setTypeface(CustomFonts.customContentLight(this));
        this.addCardButton.setTypeface(CustomFonts.customContentLight(this));
        cardInfos.setTypeface(CustomFonts.customContentLight(this));

        this.headerBackButton.setOnClickListener(view -> {
            finish();
            if (modal)
                overridePendingTransition(android.R.anim.fade_in, R.anim.slide_down);
            else
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right);
        });

        removeCreditCardButton.setOnClickListener(v -> {
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
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(getWindow().getDecorView().getRootView().getWindowToken(), 0);
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
            Intent scanActivity = new Intent(instance, ScanPayActivity.class);
            scanActivity.putExtra(ScanPay.EXTRA_TOKEN, "be38035037ed6ca3cba7089b");

            scanActivity.putExtra(ScanPay.EXTRA_SHOULD_SHOW_CONFIRMATION_VIEW, false);
            scanActivity.putExtra(ScanPay.EXTRA_SHOULD_SHOW_MANUAL_ENTRY_BUTTON, false);

            instance.startActivityForResult(scanActivity, RESULT_SCANPAY_ACTIVITY);
        });

        this.addCardButton.setOnClickListener(v -> {
            FloozRestClient.getInstance().showLoadView();
            FloozRestClient.getInstance().createCreditCard(cardOwner.getText().toString(), cardNumber.getText().toString(), cardExpires.getText().toString(), cardCVV.getText().toString(), false, new FloozHttpResponseHandler() {
                @Override
                public void success(Object response) {
                    creditCard = new FLCreditCard();
                    creditCard.owner = cardOwner.getText().toString();
                    creditCard.number = cardNumber.getText().toString();
                    creditCard.expires = cardExpires.getText().toString();
                    creditCard.cvv = cardCVV.getText().toString();

                    FloozRestClient.getInstance().currentUser.creditCard = creditCard;

                    if (next3DSecure) {
//                            ((Secure3DFragment)parentActivity.contentFragments.get("settings_3ds")).data = secureData;
//                            parentActivity.pushMainFragment("settings_3ds", R.animator.slide_up, android.R.animator.fade_out);
                    }
                    else {
                        FloozRestClient.getInstance().updateCurrentUser(null);
                    }
                }

                @Override
                public void failure(int statusCode, FLError error) {
                    FloozRestClient.getInstance().currentUser.creditCard = null;
                }
            });
        });

        if (FloozRestClient.getInstance().currentTexts != null && FloozRestClient.getInstance().currentTexts.cardInfos != null)
            cardInfos.setText(FloozRestClient.getInstance().currentTexts.cardInfos);

        if (getIntent().getStringExtra("label") != null) {
            cardInfos.setText(getIntent().getStringExtra("label"));
        }

        this.reloadCreditCard();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
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
    public void onResume() {
        super.onResume();
        floozApp.setCurrentActivity(this);

        if (FLHelper.isDebuggable())
            ViewServer.get(this).setFocusedWindow(this);

        this.reloadCreditCard();

        LocalBroadcastManager.getInstance(FloozApplication.getAppContext()).registerReceiver(reloadCurrentUserDataReceiver,
                CustomNotificationIntents.filterReloadCurrentUser());
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

        LocalBroadcastManager.getInstance(FloozApplication.getAppContext()).unregisterReceiver(reloadCurrentUserDataReceiver);
    }

    private void clearReferences(){
        Activity currActivity = floozApp.getCurrentActivity();
        if (currActivity != null && currActivity.equals(this))
            floozApp.setCurrentActivity(null);
    }

    public void reloadCreditCard() {
        this.creditCard = FloozRestClient.getInstance().currentUser.creditCard;

        if (this.creditCard != null && this.creditCard.cardId != null) {
            this.createCardContainer.setVisibility(View.GONE);
            this.removeCardContainer.setVisibility(View.VISIBLE);
            this.creditCardNumber.setText(this.creditCard.number.replaceAll(".(?=.)", "$0 "));
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
        }
    }

    @Override
    public void onBackPressed() {
        this.headerBackButton.performClick();
    }
}
