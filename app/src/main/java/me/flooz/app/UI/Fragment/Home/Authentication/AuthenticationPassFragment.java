package me.flooz.app.UI.Fragment.Home.Authentication;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
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
import android.widget.TextView;

import org.json.JSONObject;

import me.flooz.app.Model.FLError;
import me.flooz.app.Network.FloozHttpResponseHandler;
import me.flooz.app.Network.FloozRestClient;
import me.flooz.app.R;
import me.flooz.app.UI.Activity.AuthenticationActivity;
import me.flooz.app.Utils.CustomFonts;

/**
 * Created by Flooz on 12/12/14.
 */
public class AuthenticationPassFragment extends AuthenticationBaseFragment {

    private Button nextButton;
    private EditText phoneTextfield;
    private EditText passwordTextfield;

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.authentication_pass_fragment, null);

        this.parentActivity.showHeader();

        this.phoneTextfield = (EditText) view.findViewById(R.id.authentication_pass_phone);
        this.passwordTextfield = (EditText) view.findViewById(R.id.authentication_pass_password);
        this.nextButton = (Button) view.findViewById(R.id.authentication_pass_next);
        TextView forgetButton = (TextView) view.findViewById(R.id.authentication_pass_pass_forget);

        this.phoneTextfield.setTypeface(CustomFonts.customContentLight(inflater.getContext()));
        this.passwordTextfield.setTypeface(CustomFonts.customContentLight(inflater.getContext()));
        forgetButton.setTypeface(CustomFonts.customContentLight(inflater.getContext()));

        this.phoneTextfield.setText(FloozRestClient.getInstance().currentUser.phone.replace("+33", "0"));

        this.phoneTextfield.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (passwordTextfield.getText().length() >= 6 && s.length() >= 8) {
                    nextButton.setEnabled(true);
                }
                else {
                    nextButton.setEnabled(false);
                }
            }
        });

        this.passwordTextfield.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() >= 6 && phoneTextfield.getText().length() >= 8) {
                    nextButton.setEnabled(true);
                }
                else {
                    nextButton.setEnabled(false);
                }
            }
        });

        this.passwordTextfield.setOnEditorActionListener((v, actionId, event) -> {
            if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE)) {
                if (nextButton.isEnabled())
                    nextButton.performClick();
            }
            return false;
        });

        this.nextButton.setOnClickListener(v -> {
            FloozRestClient.getInstance().showLoadView();
            FloozRestClient.getInstance().loginForSecureCode(phoneTextfield.getText().toString(), passwordTextfield.getText().toString(), new FloozHttpResponseHandler() {
                @Override
                public void success(Object response) {
                    parentActivity.gotToNextPage();
                    FloozRestClient.getInstance().clearSecureCode();
                }

                @Override
                public void failure(int statusCode, FLError error) {

                }
            });
        });

        forgetButton.setOnClickListener(v -> {
            FloozRestClient.getInstance().showLoadView();
            FloozRestClient.getInstance().passwordForget(FloozRestClient.getInstance().currentUser.email, new FloozHttpResponseHandler() {
                @Override
                public void success(Object response) {

                }

                @Override
                public void failure(int statusCode, FLError error) {

                }
            });
        });

        return view;
    }

    @Override
    public void onBackPressed() {
        this.parentActivity.backToPreviousPage();
    }

    @Override
    public void onStart() {
        super.onStart();
        this.passwordTextfield.requestFocus();
        InputMethodManager imm = (InputMethodManager) parentActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(this.passwordTextfield, InputMethodManager.SHOW_IMPLICIT);
    }
}
