package me.flooz.app.UI.Controllers;

import android.Manifest;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
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
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.devmarvel.creditcardentry.library.CreditCardForm;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import io.card.payment.CardIOActivity;
import io.card.payment.CreditCard;
import me.flooz.app.App.FloozApplication;
import me.flooz.app.Model.FLCreditCard;
import me.flooz.app.Model.FLError;
import me.flooz.app.Network.FloozHttpResponseHandler;
import me.flooz.app.Network.FloozRestClient;
import me.flooz.app.R;
import me.flooz.app.UI.Activity.LocationActivity;
import me.flooz.app.Utils.CustomFonts;
import me.flooz.app.Utils.CustomNotificationIntents;
import me.flooz.app.Utils.ImageHelper;
import me.flooz.app.Utils.JSONHelper;

/**
 * Created by Flooz on 9/1/15.
 */
public class CreditCardController extends BaseController {

    private static int RESULT_CARDIO_ACTIVITY = 45;
    private static final int PERMISSION_CAMERA = 3;

    public String cardInfosText;

    private TextView creditCardOwner;
    private TextView creditCardNumber;
    private TextView creditCardExpires;
    private LinearLayout removeCardContainer;
    private RelativeLayout createCardContainer;

    private TextView cardFormHint;
    private RelativeLayout cardForm;
    private EditText cardFormOwnerTextfield;
    private CreditCardForm cardFormInput;
    private LinearLayout cardFormSaveButton;
    private ImageView cardFormSaveImage;
    private TextView cardFormSaveText;
    private ImageView scanButton;

    private TextView infosText;

    private Button addCardButton;

    public Boolean next3DSecure = false;

    private FLCreditCard creditCard;

    public JSONObject floozData;
    private Boolean saveCard = true;
    private Boolean hideSaveCard = true;


    private BroadcastReceiver reloadCurrentUserDataReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            reloadCreditCard();
        }
    };

    public CreditCardController(@NonNull View mainView, @NonNull Activity parentActivity, @NonNull ControllerKind kind) {
        super(mainView, parentActivity, kind);

        hideSaveCard = true;

        this.init();
    }

    public CreditCardController(@NonNull View mainView, @NonNull Activity parentActivity, @NonNull ControllerKind kind, @Nullable JSONObject data) {
        super(mainView, parentActivity, kind, data);

        if (data != null)
            hideSaveCard = false;

        if (this.triggersData != null) {
            if (this.triggersData.has("hideSave")) {
                hideSaveCard = this.triggersData.optBoolean("hideSave");
            }

            if (this.triggersData.has("save")) {
                saveCard = this.triggersData.optBoolean("save");
            }
        }

        this.init();
    }

    @Override
    protected void init() {
        super.init();

        this.cardFormHint = (TextView) this.currentView.findViewById(R.id.settings_credit_card_create_form_hint);
        this.cardForm = (RelativeLayout) this.currentView.findViewById(R.id.settings_credit_card_create_form_layout);
        this.cardFormOwnerTextfield = (EditText) this.currentView.findViewById(R.id.settings_credit_card_create_form_owner);
        this.cardFormInput = (CreditCardForm) this.currentView.findViewById(R.id.settings_credit_card_create_form);
        this.cardFormSaveButton = (LinearLayout) this.currentView.findViewById(R.id.settings_credit_card_create_form_save);
        this.cardFormSaveImage = (ImageView) this.currentView.findViewById(R.id.settings_credit_card_create_form_save_image);
        this.cardFormSaveText = (TextView) this.currentView.findViewById(R.id.settings_credit_card_create_form_save_text);
        this.infosText = (TextView) this.currentView.findViewById(R.id.settings_credit_card_create_infos_text);
        this.addCardButton = (Button) this.currentView.findViewById(R.id.settings_credit_card_create_button);
        this.scanButton = (ImageView) this.currentView.findViewById(R.id.settings_credit_card_create_form_scan);

        this.creditCardOwner = (TextView) this.currentView.findViewById(R.id.settings_credit_card_owner);
        this.creditCardNumber = (TextView) this.currentView.findViewById(R.id.settings_credit_card_number);
        this.creditCardExpires = (TextView) this.currentView.findViewById(R.id.settings_credit_card_expires);
        LinearLayout removeCreditCardButton = (LinearLayout) this.currentView.findViewById(R.id.settings_credit_card_remove_button);
        TextView removeCreditCardText = (TextView) this.currentView.findViewById(R.id.settings_credit_card_remove_text);
        this.removeCardContainer = (LinearLayout) this.currentView.findViewById(R.id.settings_credit_card_remove_card_view);
        this.createCardContainer = (RelativeLayout) this.currentView.findViewById(R.id.settings_credit_card_create_card_view);

        this.creditCardOwner.setTypeface(CustomFonts.customCreditCard(this.parentActivity));
        this.creditCardNumber.setTypeface(CustomFonts.customCreditCard(this.parentActivity));
        this.creditCardExpires.setTypeface(CustomFonts.customCreditCard(this.parentActivity));
        removeCreditCardText.setTypeface(CustomFonts.customTitleExtraLight(this.parentActivity));
        this.cardFormHint.setTypeface(CustomFonts.customContentBold(this.parentActivity));
        this.cardFormOwnerTextfield.setTypeface(CustomFonts.customContentRegular(this.parentActivity));
        this.cardFormSaveText.setTypeface(CustomFonts.customContentRegular(this.parentActivity));
        this.addCardButton.setTypeface(CustomFonts.customContentLight(this.parentActivity));
        this.infosText.setTypeface(CustomFonts.customContentRegular(this.parentActivity));

        if (FloozRestClient.getInstance().currentTexts.cardHolder != null) {
            if (FloozRestClient.getInstance().currentTexts.cardHolder.contentEquals("true")) {
                this.cardFormOwnerTextfield.setText(FloozRestClient.getInstance().currentUser.fullname);
            } else {
                this.cardFormOwnerTextfield.setText("");
            }
        } else {
            this.cardFormOwnerTextfield.setVisibility(View.GONE);
        }

        if (hideSaveCard)
            cardFormSaveButton.setVisibility(View.GONE);

        this.infosText.setText(FloozRestClient.getInstance().currentTexts.cardInfos);

        this.scanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ActivityCompat.checkSelfPermission(parentActivity, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
                        || ActivityCompat.checkSelfPermission(parentActivity, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
//                if (ActivityCompat.shouldShowRequestPermissionRationale(NewTransactionActivity.this,  Manifest.permission.CAMERA)) {
//                    // Display UI and wait for user interaction
//                } else {
                    ActivityCompat.requestPermissions(parentActivity, new String[]{Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_CAMERA);
//                }
                } else {
                    showCardIO();
                }
            }
        });

        this.cardFormSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveCard = !saveCard;
                reloadCreditCard();
            }
        });

        removeCreditCardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FloozRestClient.getInstance().showLoadView();
                FloozRestClient.getInstance().removeCreditCard(creditCard.cardId, new FloozHttpResponseHandler() {
                    @Override
                    public void success(Object response) {

                    }

                    @Override
                    public void failure(int statusCode, FLError error) {

                    }
                });
            }
        });

        this.addCardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FloozRestClient.getInstance().showLoadView();

                Map<String, Object> params = new HashMap<>(4);

                if (FloozRestClient.getInstance().currentTexts.cardHolder != null)
                    params.put("holder", cardFormOwnerTextfield.getText().toString());

                params.put("number", cardFormInput.getCreditCard().getCardNumber());
                params.put("expires", cardFormInput.getCreditCard().getExpDate());
                params.put("cvv", cardFormInput.getCreditCard().getSecurityCode());

                if (!hideSaveCard)
                    params.put("hidden", !saveCard);

                if (floozData != null)
                    try {
                        params.put("flooz", JSONHelper.toMap(floozData));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                FloozRestClient.getInstance().createCreditCard(params, false, new FloozHttpResponseHandler() {
                    @Override
                    public void success(Object response) {

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

    public void showCardIO() {
        Intent scanIntent = new Intent(parentActivity, CardIOActivity.class);

        scanIntent.putExtra(CardIOActivity.EXTRA_HIDE_CARDIO_LOGO, true);
        scanIntent.putExtra(CardIOActivity.EXTRA_SUPPRESS_MANUAL_ENTRY, true);
        scanIntent.putExtra(CardIOActivity.EXTRA_SUPPRESS_CONFIRMATION, true);
        scanIntent.putExtra(CardIOActivity.EXTRA_GUIDE_COLOR, parentActivity.getResources().getColor(R.color.blue));

        parentActivity.startActivityForResult(scanIntent, RESULT_CARDIO_ACTIVITY);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RESULT_CARDIO_ACTIVITY) {
            if (data != null && data.hasExtra(CardIOActivity.EXTRA_SCAN_RESULT)) {
                CreditCard scanResult = data.getParcelableExtra(CardIOActivity.EXTRA_SCAN_RESULT);

                cardFormInput.setCardNumber(scanResult.cardNumber, true);

                if (scanResult.isExpiryValid())
                    cardFormInput.setExpDate(scanResult.expiryMonth + "/" + scanResult.expiryYear, true);

            }
        }
    }

    @Override
    public void onStart() {
        if (!TextUtils.isEmpty(cardInfosText))
            infosText.setText(cardInfosText);
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
            this.creditCardExpires.setText(this.creditCard.expires);
        } else {
            this.removeCardContainer.setVisibility(View.GONE);
            this.createCardContainer.setVisibility(View.VISIBLE);

            if (hideSaveCard) {
                cardFormSaveButton.setVisibility(View.GONE);
            } else {
                cardFormSaveButton.setVisibility(View.VISIBLE);

                if (saveCard)
                    cardFormSaveImage.setImageDrawable(parentActivity.getResources().getDrawable(R.drawable.check_on));
                else
                    cardFormSaveImage.setImageDrawable(parentActivity.getResources().getDrawable(R.drawable.check_off));
            }
        }
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == PERMISSION_CAMERA) {
            if (grantResults.length == 2 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED ) {
                showCardIO();
            }
        }
    }
}