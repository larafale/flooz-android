package me.flooz.app.UI.Activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import me.flooz.app.App.FloozApplication;
import me.flooz.app.Model.FLError;
import me.flooz.app.Network.FloozHttpResponseHandler;
import me.flooz.app.Network.FloozRestClient;
import me.flooz.app.R;
import me.flooz.app.Utils.CustomFonts;
import me.flooz.app.Utils.FLHelper;

/**
 * Created by Flooz on 3/9/15.
 */
public class CashoutActivity extends Activity {

    private CashoutActivity instance;
    private FloozApplication floozApp;

    private ImageView headerBackButton;
    private TextView headerTitle;
    private TextView balanceInfos;
    private TextView globalInfos;
    private TextView balance;
    private TextView balanceCurrency;
    private EditText amountTextfield;
    private TextView cashoutButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        instance = this;
        floozApp = (FloozApplication)this.getApplicationContext();

        this.setContentView(R.layout.cashout_fragment);

        this.headerBackButton = (ImageView) this.findViewById(R.id.cashout_header_back);
        this.headerTitle = (TextView) this.findViewById(R.id.cashout_header_title);
        this.balanceInfos = (TextView) this.findViewById(R.id.cashout_balance_infos);
        this.globalInfos = (TextView) this.findViewById(R.id.cashout_infos);
        this.balance = (TextView) this.findViewById(R.id.cashout_balance);
        this.balanceCurrency = (TextView) this.findViewById(R.id.cashout_balance_currency);
        this.amountTextfield = (EditText) this.findViewById(R.id.cashout_amount_textfield);
        this.cashoutButton = (TextView) this.findViewById(R.id.cashout_button);

        this.headerTitle.setTypeface(CustomFonts.customTitleExtraLight(this));
        this.balanceInfos.setTypeface(CustomFonts.customContentRegular(this));
        this.globalInfos.setTypeface(CustomFonts.customContentRegular(this));
        this.balance.setTypeface(CustomFonts.customContentRegular(this));
        this.balanceCurrency.setTypeface(CustomFonts.customContentRegular(this));
        this.amountTextfield.setTypeface(CustomFonts.customContentRegular(this));
        this.cashoutButton.setTypeface(CustomFonts.customContentRegular(this));

        this.cashoutButton.setText(String.format(this.getResources().getString(R.string.CASHOUT_BUTTON), ""));

        String amountValue = FLHelper.trimTrailingZeros(FloozRestClient.getInstance().currentUser.amount.toString());

        this.balance.setText(amountValue);

        this.headerBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
                overridePendingTransition(android.R.anim.fade_in, R.anim.slide_down);
            }
        });

        this.amountTextfield.setText("");

        this.amountTextfield.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > 0) {
                    cashoutButton.setText(String.format(instance.getResources().getString(R.string.CASHOUT_BUTTON), FLHelper.trimTrailingZeros(s.toString()) + " €"));
                    cashoutButton.setEnabled(true);
                }
                else {
                    cashoutButton.setEnabled(false);
                    cashoutButton.setText(String.format(instance.getResources().getString(R.string.CASHOUT_BUTTON), ""));
                }
            }
        });

        this.amountTextfield.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE)) {
                    cashoutButton.performClick();
                }
                return false;
            }
        });

        this.cashoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentNotifs = new Intent(instance, AuthenticationActivity.class);
                instance.startActivityForResult(intentNotifs, AuthenticationActivity.RESULT_AUTHENTICATION_ACTIVITY);
                instance.overridePendingTransition(R.anim.slide_up, android.R.anim.fade_out);
            }
        });
    }

    public void authenticationValidated() {
        FloozRestClient.getInstance().cashout(Float.parseFloat(amountTextfield.getText().toString()), new FloozHttpResponseHandler() {
            @Override
            public void success(Object response) {
                String amountValue = FLHelper.trimTrailingZeros(FloozRestClient.getInstance().currentUser.amount.toString());

                balance.setText(amountValue);
                amountTextfield.setText("");
                amountTextfield.clearFocus();
            }

            @Override
            public void failure(int statusCode, FLError error) {

            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == AuthenticationActivity.RESULT_AUTHENTICATION_ACTIVITY) {
            if (resultCode == Activity.RESULT_OK)
                authenticationValidated();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        this.amountTextfield.setText("");
        this.amountTextfield.clearFocus();
    }

    @Override
    public void onResume() {
        super.onResume();
        floozApp.setCurrentActivity(this);
    }

    @Override
    public void onPause() {
        clearReferences();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        clearReferences();
        super.onDestroy();
    }


    private void clearReferences(){
        Activity currActivity = floozApp.getCurrentActivity();
        if (currActivity != null && currActivity.equals(this))
            floozApp.setCurrentActivity(null);
    }

    @Override
    public void onBackPressed() {
        this.headerBackButton.performClick();
    }

}
