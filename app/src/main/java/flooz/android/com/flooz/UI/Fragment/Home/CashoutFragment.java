package flooz.android.com.flooz.UI.Fragment.Home;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONObject;

import flooz.android.com.flooz.App.FloozApplication;
import flooz.android.com.flooz.Model.FLError;
import flooz.android.com.flooz.Network.FloozHttpResponseHandler;
import flooz.android.com.flooz.Network.FloozRestClient;
import flooz.android.com.flooz.R;
import flooz.android.com.flooz.UI.Fragment.Home.Authentication.AuthenticationFragment;
import flooz.android.com.flooz.Utils.CustomFonts;
import flooz.android.com.flooz.Utils.CustomNotificationIntents;
import flooz.android.com.flooz.Utils.FLHelper;
import flooz.android.com.flooz.Utils.ImageHelper;

/**
 * Created by Flooz on 12/9/14.
 */
public class CashoutFragment extends HomeBaseFragment implements AuthenticationFragment.AuthenticationDelegate {

    private CashoutFragment instance;

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
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.cashout_fragment, null);

        this.instance = this;

        this.headerBackButton = (ImageView) view.findViewById(R.id.cashout_header_back);
        this.headerTitle = (TextView) view.findViewById(R.id.cashout_header_title);
        this.balanceInfos = (TextView) view.findViewById(R.id.cashout_balance_infos);
        this.globalInfos = (TextView) view.findViewById(R.id.cashout_infos);
        this.balance = (TextView) view.findViewById(R.id.cashout_balance);
        this.balanceCurrency = (TextView) view.findViewById(R.id.cashout_balance_currency);
        this.amountTextfield = (EditText) view.findViewById(R.id.cashout_amount_textfield);
        this.cashoutButton = (TextView) view.findViewById(R.id.cashout_button);

        this.headerTitle.setTypeface(CustomFonts.customTitleExtraLight(inflater.getContext()));
        this.balanceInfos.setTypeface(CustomFonts.customContentRegular(inflater.getContext()));
        this.globalInfos.setTypeface(CustomFonts.customContentRegular(inflater.getContext()));
        this.balance.setTypeface(CustomFonts.customContentRegular(inflater.getContext()));
        this.balanceCurrency.setTypeface(CustomFonts.customContentRegular(inflater.getContext()));
        this.amountTextfield.setTypeface(CustomFonts.customContentRegular(inflater.getContext()));
        this.cashoutButton.setTypeface(CustomFonts.customContentRegular(inflater.getContext()));

        this.cashoutButton.setText(String.format(inflater.getContext().getResources().getString(R.string.CASHOUT_BUTTON), ""));

        String amountValue = FloozRestClient.getInstance().currentUser.amount.toString();

        this.balance.setText(amountValue);

        this.headerBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (parentActivity != null)
                    parentActivity.popMainFragment(R.animator.slide_down, android.R.animator.fade_in);
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
                    cashoutButton.setText(String.format(inflater.getContext().getResources().getString(R.string.CASHOUT_BUTTON), FLHelper.trimTrailingZeros(s.toString()) + " €"));
                    cashoutButton.setEnabled(true);
                }
                else {
                    cashoutButton.setEnabled(false);
                    cashoutButton.setText(String.format(inflater.getContext().getResources().getString(R.string.CASHOUT_BUTTON), ""));
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
                ((AuthenticationFragment)parentActivity.contentFragments.get("authentication")).delegate = instance;
                parentActivity.pushMainFragment("authentication", R.animator.slide_up, android.R.animator.fade_out);
            }
        });

        return view;
    }

    @Override
    public void authenticationValidated() {
        FloozRestClient.getInstance().showLoadView();
        FloozRestClient.getInstance().cashout(Float.parseFloat(amountTextfield.getText().toString()), new FloozHttpResponseHandler() {
            @Override
            public void success(Object response) {
                if (parentActivity != null)
                    parentActivity.popMainFragment(R.animator.slide_down, android.R.animator.fade_in);
            }

            @Override
            public void failure(int statusCode, FLError error) {

            }
        });
    }

    @Override
    public void authenticationFailed() {

    }

    @Override
    public void onStart() {
        super.onResume();
        this.amountTextfield.setText("");
        this.amountTextfield.clearFocus();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onBackPressed() {
        this.headerBackButton.performClick();
    }
}