package me.flooz.app.UI.Fragment.Signup;

import android.content.Context;
import android.graphics.Typeface;
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

import me.flooz.app.App.FloozApplication;
import me.flooz.app.Model.FLError;
import me.flooz.app.Network.FloozHttpResponseHandler;
import me.flooz.app.Network.FloozRestClient;
import me.flooz.app.R;
import me.flooz.app.Utils.CustomFonts;

/**
 * Created by Flooz on 11/13/14.
 */
public class SignupUsernameFragment extends SignupBaseFragment {

    private Button nextButton;
    private EditText usernameTextfield;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.signup_username_fragment, null);

        usernameTextfield = (EditText) view.findViewById(R.id.signup_username_textfield);
        nextButton = (Button) view.findViewById(R.id.signup_username_next);
        TextView infoLabel = (TextView) view.findViewById(R.id.signup_username_info_label);

        infoLabel.setTypeface(CustomFonts.customContentLight(inflater.getContext()));

        usernameTextfield.setTypeface(CustomFonts.customContentLight(inflater.getContext()), Typeface.BOLD);

        if (parentActivity.userData.username != null) {
            usernameTextfield.setText(parentActivity.userData.username);
            usernameTextfield.setSelection(parentActivity.userData.username.length());
            if (usernameTextfield.getText().length() >= 3)
                nextButton.setEnabled(true);
            else
                nextButton.setEnabled(false);
        }

        this.usernameTextfield.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_NEXT) {
                    if (nextButton.isEnabled())
                        nextButton.performClick();
                }
                return false;
            }
        });

        this.nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                nextButton.setEnabled(false);
                parentActivity.userData.username = usernameTextfield.getText().toString();
                FloozRestClient.getInstance().showLoadView();
                FloozRestClient.getInstance().signupPassStep("nick", parentActivity.userData.getParamsForStep(false), new FloozHttpResponseHandler() {
                    @Override
                    public void success(Object response) {
                        JSONObject responseJSON = (JSONObject) response;

                        parentActivity.gotToNextPage(responseJSON.optString("nextStep"), responseJSON.optJSONObject("nextStepData"));
                    }

                    @Override
                    public void failure(int statusCode, FLError error) {
                        nextButton.setEnabled(true);
                    }
                });
            }
        });

        this.usernameTextfield.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                if (charSequence.length() >= 3)
                    nextButton.setEnabled(true);
                else
                    nextButton.setEnabled(false);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        usernameTextfield.requestFocus();

        if (this.initData != null) {
            if (this.initData.has("info"))
                infoLabel.setText(this.initData.optString("info"));
        }

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(usernameTextfield, InputMethodManager.SHOW_IMPLICIT);

        if (usernameTextfield.getText().length() >= 3)
            nextButton.setEnabled(true);
        else
            nextButton.setEnabled(false);
    }

    @Override
    public void onPause() {
        super.onPause();
        InputMethodManager imm = (InputMethodManager) parentActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(parentActivity.getWindow().getDecorView().getRootView().getWindowToken(), 0);
    }
}