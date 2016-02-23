package me.flooz.app.UI.Controllers;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONObject;

import me.flooz.app.Model.FLError;
import me.flooz.app.Network.FloozHttpResponseHandler;
import me.flooz.app.Network.FloozRestClient;
import me.flooz.app.R;
import me.flooz.app.UI.Activity.AuthenticationActivity;
import me.flooz.app.UI.Activity.HomeActivity;
import me.flooz.app.Utils.CustomFonts;
import me.flooz.app.Utils.FLHelper;
import me.flooz.app.Utils.ViewServer;

/**
 * Created by Flooz on 8/26/15.
 */
public class CashoutController extends BaseController {

    private TextView balance;
    private EditText amountTextfield;
    private TextView cashoutButton;

    public CashoutController(@NonNull View mainView, @NonNull Activity parentActivity, @NonNull ControllerKind kind) {
        super(mainView, parentActivity, kind);

        this.init();
    }

    public CashoutController(@NonNull View mainView, @NonNull Activity parentActivity, @NonNull ControllerKind kind, @Nullable JSONObject data) {
        super(mainView, parentActivity, kind, data);

        this.init();
    }

    @Override
    protected void init() {
        super.init();

        TextView headerTitle = (TextView) this.currentView.findViewById(R.id.header_title);
        TextView balanceInfos = (TextView) this.currentView.findViewById(R.id.cashout_balance_infos);
        TextView globalInfos = (TextView) this.currentView.findViewById(R.id.cashout_infos);
        this.balance = (TextView) this.currentView.findViewById(R.id.cashout_balance);
        TextView balanceCurrency = (TextView) this.currentView.findViewById(R.id.cashout_balance_currency);
        this.amountTextfield = (EditText) this.currentView.findViewById(R.id.cashout_amount_textfield);
        this.cashoutButton = (TextView) this.currentView.findViewById(R.id.cashout_button);

        headerTitle.setTypeface(CustomFonts.customTitleExtraLight(this.parentActivity));
        balanceInfos.setTypeface(CustomFonts.customContentRegular(this.parentActivity));
        globalInfos.setTypeface(CustomFonts.customContentRegular(this.parentActivity));
        this.balance.setTypeface(CustomFonts.customContentRegular(this.parentActivity));
        balanceCurrency.setTypeface(CustomFonts.customContentRegular(this.parentActivity));
        this.amountTextfield.setTypeface(CustomFonts.customContentRegular(this.parentActivity));
        this.cashoutButton.setTypeface(CustomFonts.customContentRegular(this.parentActivity));

        this.cashoutButton.setText(String.format(this.parentActivity.getResources().getString(R.string.CASHOUT_BUTTON), ""));

        String amountValue = FLHelper.trimTrailingZeros(FloozRestClient.getInstance().currentUser.amount.toString());

        this.balance.setText(amountValue);

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
                    cashoutButton.setText(String.format(parentActivity.getResources().getString(R.string.CASHOUT_BUTTON), FLHelper.trimTrailingZeros(s.toString()) + " €"));
                } else {
                    cashoutButton.setText(String.format(parentActivity.getResources().getString(R.string.CASHOUT_BUTTON), ""));
                }
            }
        });

        this.amountTextfield.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE)) {
                    if (cashoutButton.isEnabled())
                        cashoutButton.performClick();
                }
                return false;
            }
        });

        this.cashoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String amount = amountTextfield.getText().toString();

                if (amount.isEmpty())
                    amount = "0";

                FloozRestClient.getInstance().showLoadView();
                FloozRestClient.getInstance().cashoutValidate(Float.parseFloat(amount), null);
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        this.amountTextfield.setText("");
        this.amountTextfield.clearFocus();
    }
}
