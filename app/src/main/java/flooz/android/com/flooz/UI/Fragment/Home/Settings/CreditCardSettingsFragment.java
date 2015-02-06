package flooz.android.com.flooz.UI.Fragment.Home.Settings;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
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
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import flooz.android.com.flooz.App.FloozApplication;
import flooz.android.com.flooz.Model.FLCreditCard;
import flooz.android.com.flooz.Model.FLError;
import flooz.android.com.flooz.Network.FloozHttpResponseHandler;
import flooz.android.com.flooz.Network.FloozRestClient;
import flooz.android.com.flooz.R;
import flooz.android.com.flooz.UI.Fragment.Home.HomeBaseFragment;
import flooz.android.com.flooz.Utils.CustomFonts;
import flooz.android.com.flooz.Utils.CustomNotificationIntents;
import scanpay.it.ScanPay;
import scanpay.it.ScanPayActivity;

/**
 * Created by Flooz on 12/15/14.
 */
public class CreditCardSettingsFragment extends HomeBaseFragment {
    private ImageView headerBackButton;
    private TextView headerTitle;

    private TextView creditCardOwner;
    private TextView creditCardNumber;
    private LinearLayout removeCreditCardButton;
    private TextView removeCreditCardText;
    private LinearLayout removeCardContainer;
    private LinearLayout createCardContainer;

    private EditText cardOwner;
    private EditText cardNumber;
    private EditText cardExpires;
    private EditText cardCVV;

    private ImageView scanpayButton;

    private Button addCardButton;

    public Boolean next3DSecure = false;
    public String secureData;

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

        LocalBroadcastManager.getInstance(FloozApplication.getAppContext()).registerReceiver(reloadCurrentUserDataReceiver,
                CustomNotificationIntents.filterReloadCurrentUser());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.settings_credit_card_fragment, null);

        this.headerBackButton = (ImageView) view.findViewById(R.id.settings_credit_card_header_back);
        this.headerTitle = (TextView) view.findViewById(R.id.settings_credit_card_header_title);
        this.cardOwner = (EditText) view.findViewById(R.id.settings_credit_card_create_owner);
        this.cardNumber = (EditText) view.findViewById(R.id.settings_credit_card_create_number);
        this.cardExpires = (EditText) view.findViewById(R.id.settings_credit_card_create_expires);
        this.cardCVV = (EditText) view.findViewById(R.id.settings_credit_card_create_cvv);
        this.scanpayButton = (ImageView) view.findViewById(R.id.settings_credit_card_create_scanpay);
        this.addCardButton = (Button) view.findViewById(R.id.settings_credit_card_create_add);
        this.creditCardOwner = (TextView) view.findViewById(R.id.settings_credit_card_owner);
        this.creditCardNumber = (TextView) view.findViewById(R.id.settings_credit_card_number);
        this.removeCreditCardButton = (LinearLayout) view.findViewById(R.id.settings_credit_card_remove_button);
        this.removeCreditCardText = (TextView) view.findViewById(R.id.settings_credit_card_remove_text);
        this.removeCardContainer = (LinearLayout) view.findViewById(R.id.settings_credit_card_remove_card_view);
        this.createCardContainer = (LinearLayout) view.findViewById(R.id.settings_credit_card_create_card_view);

        this.headerTitle.setTypeface(CustomFonts.customTitleExtraLight(inflater.getContext()));
        this.creditCardOwner.setTypeface(CustomFonts.customContentRegular(inflater.getContext()));
        this.creditCardNumber.setTypeface(CustomFonts.customTitleLight(inflater.getContext()));
        this.removeCreditCardText.setTypeface(CustomFonts.customTitleExtraLight(inflater.getContext()));
        this.cardOwner.setTypeface(CustomFonts.customContentLight(inflater.getContext()));
        this.cardNumber.setTypeface(CustomFonts.customContentLight(inflater.getContext()));
        this.cardExpires.setTypeface(CustomFonts.customContentLight(inflater.getContext()));
        this.cardCVV.setTypeface(CustomFonts.customContentLight(inflater.getContext()));
        this.addCardButton.setTypeface(CustomFonts.customContentLight(inflater.getContext()));

        this.headerBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                parentActivity.popMainFragment(R.animator.slide_out_left, R.animator.slide_in_right);
            }
        });

        this.removeCreditCardButton.setOnClickListener(new View.OnClickListener() {
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

        this.cardOwner.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                valid();
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

                valid();
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

                valid();
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
                if (s.length() > 4)
                    s.delete(3, s.length() - 1);

                if (s.length() == 3) {
                    cardOwner.clearFocus();
                    addCardButton.requestFocus();
                    InputMethodManager imm = (InputMethodManager) parentActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(parentActivity.getWindow().getDecorView().getRootView().getWindowToken(), 0);
                }

                valid();
            }
        });

        this.cardCVV.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE)) {
                    addCardButton.performClick();
                }
                return false;
            }
        });

        this.scanpayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent scanActivity = new Intent(parentActivity, ScanPayActivity.class);
                scanActivity.putExtra(ScanPay.EXTRA_TOKEN, "be38035037ed6ca3cba7089b");

                scanActivity.putExtra(ScanPay.EXTRA_SHOULD_SHOW_CONFIRMATION_VIEW, false);
                scanActivity.putExtra(ScanPay.EXTRA_SHOULD_SHOW_MANUAL_ENTRY_BUTTON, false);

                parentActivity.startActivityForResult(scanActivity, parentActivity.RESULT_SCANPAY_ACTIVITY);
            }
        });


        this.addCardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                creditCard = new FLCreditCard();
                creditCard.owner = cardOwner.getText().toString();
                creditCard.number = cardNumber.getText().toString();
                creditCard.expires = cardExpires.getText().toString();
                creditCard.cvv = cardCVV.getText().toString();

                FloozRestClient.getInstance().currentUser.creditCard = creditCard;

                FloozRestClient.getInstance().showLoadView();
                FloozRestClient.getInstance().createCreditCard(cardOwner.getText().toString(), cardNumber.getText().toString(), cardExpires.getText().toString(), cardCVV.getText().toString(), false, new FloozHttpResponseHandler() {
                    @Override
                    public void success(Object response) {
                        if (next3DSecure) {
                            ((Secure3DFragment)parentActivity.contentFragments.get("settings_3ds")).data = secureData;
                            parentActivity.pushMainFragment("settings_3ds", R.animator.slide_up, android.R.animator.fade_out);
                        }
                        else {
                            FloozRestClient.getInstance().updateCurrentUser(null);
                            parentActivity.popMainFragment(R.animator.slide_out_left, R.animator.slide_in_right);
                        }
                    }

                    @Override
                    public void failure(int statusCode, FLError error) {
                        FloozRestClient.getInstance().currentUser.creditCard = null;
                    }
                });
            }
        });

        this.reloadCreditCard();

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        this.reloadCreditCard();
    }

    @Override
    public void onResume() {
        super.onResume();
        this.reloadCreditCard();
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

                this.valid();
            } else {
                this.cardOwner.setText(FloozRestClient.getInstance().currentUser.fullname);
                this.cardNumber.setText("");
                this.cardCVV.setText("");
                this.cardExpires.setText("");
            }
        }
    }

    private void valid() {
        boolean validate = true;

        if (this.cardOwner.getText().toString().isEmpty())
            validate = false;

        if (this.cardNumber.getText().toString().length() != 19)
            validate = false;

        if (this.cardExpires.getText().toString().length() != 5)
            validate = false;

        if (this.cardCVV.getText().toString().length() != 3)
            validate = false;

        this.addCardButton.setEnabled(validate);
    }

    @Override
    public void onBackPressed() {
        this.headerBackButton.performClick();
    }
}
