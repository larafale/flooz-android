package me.flooz.app.UI.Activity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.devmarvel.creditcardentry.library.CreditCard;
import com.devmarvel.creditcardentry.library.CreditCardForm;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import me.flooz.app.App.FloozApplication;
import me.flooz.app.Model.FLCreditCard;
import me.flooz.app.Model.FLError;
import me.flooz.app.Network.FloozHttpResponseHandler;
import me.flooz.app.Network.FloozRestClient;
import me.flooz.app.R;
import me.flooz.app.Utils.CustomFonts;
import me.flooz.app.Utils.CustomNotificationIntents;
import me.flooz.app.Utils.FLHelper;
import me.flooz.app.Utils.JSONHelper;
import me.flooz.app.Utils.ViewServer;

/**
 * Created by Flooz on 05/05/16.
 */
public class CashinCreditCardActivity extends BaseActivity {

    private FloozApplication floozApp;

    private ImageView headerBackButton;

    private TextView cardFormHint;
    private RelativeLayout cardForm;
    private EditText cardFormOwnerTextfield;
    private CreditCardForm cardFormInput;
    private LinearLayout cardFormSaveButton;
    private ImageView cardFormSaveImage;
    private TextView cardFormSaveText;

    private RelativeLayout cardLayout;
    private ImageView cardLayoutDeleteButton;
    private TextView cardLayoutOwner;
    private TextView cardLayoutNumber;
    private TextView cardLayoutExpire;

    private TextView amountFormHint;
    private EditText amountTextfield;
    private Button sendButton;

    private TextView infosText;

    private String random;
    private Boolean saveCard = false;

    private BroadcastReceiver reloadCurrentUserDataReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            reloadView();
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

        floozApp = (FloozApplication) this.getApplicationContext();
        this.setContentView(R.layout.cashin_credit_card_activity);

        this.random = FLHelper.generateRandomString();

        TextView title = (TextView) this.findViewById(R.id.header_title);
        this.headerBackButton = (ImageView) this.findViewById(R.id.header_item_left);

        this.cardFormHint = (TextView) this.findViewById(R.id.cashin_credit_card_form_hint);
        this.cardForm = (RelativeLayout) this.findViewById(R.id.cashin_credit_card_form_layout);
        this.cardFormOwnerTextfield = (EditText) this.findViewById(R.id.cashin_credit_card_form_owner);
        this.cardFormInput = (CreditCardForm) this.findViewById(R.id.cashin_credit_card_form);
        this.cardFormSaveButton = (LinearLayout) this.findViewById(R.id.cashin_credit_card_form_save);
        this.cardFormSaveImage = (ImageView) this.findViewById(R.id.cashin_credit_card_form_save_image);
        this.cardFormSaveText = (TextView) this.findViewById(R.id.cashin_credit_card_form_save_text);

        this.cardLayout = (RelativeLayout) this.findViewById(R.id.cashin_credit_card_card);
        this.cardLayoutDeleteButton = (ImageView) this.findViewById(R.id.cashin_credit_card_card_delete);
        this.cardLayoutOwner = (TextView) this.findViewById(R.id.cashin_credit_card_card_owner);
        this.cardLayoutNumber = (TextView) this.findViewById(R.id.cashin_credit_card_card_number);
        this.cardLayoutExpire = (TextView) this.findViewById(R.id.cashin_credit_card_card_expires);

        this.amountFormHint = (TextView) this.findViewById(R.id.cashin_credit_card_amount_hint);
        this.amountTextfield = (EditText) this.findViewById(R.id.cashin_credit_card_amount);
        this.sendButton = (Button) this.findViewById(R.id.cashin_credit_card_validate);

        this.infosText = (TextView) this.findViewById(R.id.cashin_credit_card_infos_text);

        title.setTypeface(CustomFonts.customTitleLight(this));
        this.cardFormHint.setTypeface(CustomFonts.customContentBold(this));
        this.cardFormOwnerTextfield.setTypeface(CustomFonts.customContentRegular(this));
        this.cardFormSaveText.setTypeface(CustomFonts.customContentRegular(this));
        this.cardLayoutOwner.setTypeface(CustomFonts.customCreditCard(this));
        this.cardLayoutNumber.setTypeface(CustomFonts.customCreditCard(this));
        this.cardLayoutExpire.setTypeface(CustomFonts.customCreditCard(this));
        this.amountFormHint.setTypeface(CustomFonts.customContentBold(this));
        this.amountTextfield.setTypeface(CustomFonts.customContentRegular(this));
        this.sendButton.setTypeface(CustomFonts.customContentRegular(this));
        this.infosText.setTypeface(CustomFonts.customContentRegular(this));

        this.cardLayoutDeleteButton.setColorFilter(Color.WHITE);

        if (FloozRestClient.getInstance().currentTexts.cardHolder != null) {
            if (FloozRestClient.getInstance().currentTexts.cardHolder.contentEquals("true")) {
                this.cardFormOwnerTextfield.setText(FloozRestClient.getInstance().currentUser.fullname);
            } else {
                this.cardFormOwnerTextfield.setText("");
            }
        } else {
            this.cardFormOwnerTextfield.setVisibility(View.GONE);
        }

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

        this.cardLayoutDeleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FloozRestClient.getInstance().showLoadView();
                FloozRestClient.getInstance().removeCreditCard(null);
            }
        });

        this.cardFormSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveCard = !saveCard;
                reloadView();
            }
        });

        this.sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(getWindow().getDecorView().getRootView().getWindowToken(), 0);

                FloozRestClient.getInstance().showLoadView();

                Map<String, Object> params = new HashMap<>();

                if (FloozRestClient.getInstance().currentUser.creditCard != null) {
                    params.put("card", FloozRestClient.getInstance().currentUser.creditCard.cardId);
                } else {
                    Map<String, Object> cardParams = new HashMap<>();

                    if (FloozRestClient.getInstance().currentTexts.cardHolder != null)
                        cardParams.put("holder", cardFormOwnerTextfield.getText().toString());

                    CreditCard creditCard = cardFormInput.getCreditCard();

                    cardParams.put("number", creditCard.getCardNumber().replace(" ", ""));
                    cardParams.put("expires", String.format(Locale.US, "%02d", creditCard.getExpMonth()) + "-" + creditCard.getExpYear());
                    cardParams.put("cvv", creditCard.getSecurityCode());
                    cardParams.put("hidden", !saveCard);

                    params.put("card", cardParams);
                }

                params.put("amount", amountTextfield.getText().toString());
                params.put("random", random);

                FloozRestClient.getInstance().cashinCard(params, null);
            }
        });

        LocalBroadcastManager.getInstance(FloozApplication.getAppContext()).registerReceiver(reloadCurrentUserDataReceiver,
                CustomNotificationIntents.filterReloadCurrentUser());
    }

    private void reloadView() {
        if (FloozRestClient.getInstance().currentUser.creditCard != null) {
            FLCreditCard creditCard = FloozRestClient.getInstance().currentUser.creditCard;

            cardForm.setVisibility(View.GONE);
            cardLayout.setVisibility(View.VISIBLE);

            cardLayoutNumber.setText(creditCard.number.substring(0, 4) + " **** **** " + creditCard.number.substring(12, 16));
            cardLayoutOwner.setText(creditCard.owner);
            cardLayoutExpire.setText(creditCard.expires);
        } else {
            cardForm.setVisibility(View.VISIBLE);
            cardLayout.setVisibility(View.GONE);

            if (saveCard)
                cardFormSaveImage.setImageDrawable(getResources().getDrawable(R.drawable.check_on));
            else
                cardFormSaveImage.setImageDrawable(getResources().getDrawable(R.drawable.check_off));
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (FLHelper.isDebuggable())
            ViewServer.get(this).setFocusedWindow(this);
        floozApp.setCurrentActivity(this);

        this.reloadView();
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
}
