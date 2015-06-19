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
import me.flooz.app.Utils.ViewServer;

/**
 * Created by Flooz on 3/9/15.
 */
public class CashoutActivity extends Activity {

    private CashoutActivity instance;
    private FloozApplication floozApp;

    private ImageView headerBackButton;
    private TextView balance;
    private EditText amountTextfield;
    private TextView cashoutButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (FLHelper.isDebuggable())
            ViewServer.get(this).addWindow(this);

        instance = this;
        floozApp = (FloozApplication)this.getApplicationContext();

        this.setContentView(R.layout.cashout_fragment);

        this.headerBackButton = (ImageView) this.findViewById(R.id.cashout_header_back);
        TextView headerTitle = (TextView) this.findViewById(R.id.cashout_header_title);
        TextView balanceInfos = (TextView) this.findViewById(R.id.cashout_balance_infos);
        TextView globalInfos = (TextView) this.findViewById(R.id.cashout_infos);
        this.balance = (TextView) this.findViewById(R.id.cashout_balance);
        TextView balanceCurrency = (TextView) this.findViewById(R.id.cashout_balance_currency);
        this.amountTextfield = (EditText) this.findViewById(R.id.cashout_amount_textfield);
        this.cashoutButton = (TextView) this.findViewById(R.id.cashout_button);

        headerTitle.setTypeface(CustomFonts.customTitleExtraLight(this));
        balanceInfos.setTypeface(CustomFonts.customContentRegular(this));
        globalInfos.setTypeface(CustomFonts.customContentRegular(this));
        this.balance.setTypeface(CustomFonts.customContentRegular(this));
        balanceCurrency.setTypeface(CustomFonts.customContentRegular(this));
        this.amountTextfield.setTypeface(CustomFonts.customContentRegular(this));
        this.cashoutButton.setTypeface(CustomFonts.customContentRegular(this));

        this.cashoutButton.setText(String.format(this.getResources().getString(R.string.CASHOUT_BUTTON), ""));

        String amountValue = FLHelper.trimTrailingZeros(FloozRestClient.getInstance().currentUser.amount.toString());

        this.balance.setText(amountValue);

        this.headerBackButton.setOnClickListener(view -> {
            finish();
            overridePendingTransition(android.R.anim.fade_in, R.anim.slide_down);
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
                if (s.length() > 7)
                    s.delete(7, s.length());

                if (s.length() > 0) {
                    cashoutButton.setText(String.format(instance.getResources().getString(R.string.CASHOUT_BUTTON), FLHelper.trimTrailingZeros(s.toString()) + " €"));
                }
                else {
                    cashoutButton.setText(String.format(instance.getResources().getString(R.string.CASHOUT_BUTTON), ""));
                }
            }
        });

        this.amountTextfield.setOnEditorActionListener((v, actionId, event) -> {
            if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE)) {
                if (cashoutButton.isEnabled())
                    cashoutButton.performClick();
            }
            return false;
        });

        this.cashoutButton.setOnClickListener(v -> {
            Intent intentNotifs = new Intent(instance, AuthenticationActivity.class);
            instance.startActivityForResult(intentNotifs, AuthenticationActivity.RESULT_AUTHENTICATION_ACTIVITY);
            instance.overridePendingTransition(R.anim.slide_up, android.R.anim.fade_out);
        });
    }

    public void authenticationValidated() {
        String amount = amountTextfield.getText().toString();

        if (amount.isEmpty())
            amount = "0";

        FloozRestClient.getInstance().cashout(Float.parseFloat(amount), new FloozHttpResponseHandler() {
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

        if (FLHelper.isDebuggable())
            ViewServer.get(this).setFocusedWindow(this);

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

        if (FLHelper.isDebuggable())
            ViewServer.get(this).removeWindow(this);

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
