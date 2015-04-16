package me.flooz.app.UI.Fragment.Signup;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import org.json.JSONObject;

import me.flooz.app.Model.FLError;
import me.flooz.app.Network.FloozHttpResponseHandler;
import me.flooz.app.Network.FloozRestClient;
import me.flooz.app.R;

/**
 * Created by Flooz on 4/8/15.
 */
public class SignupInvitationFragment  extends SignupBaseFragment {

    EditText couponTextfield;
    Button nextButton;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.signup_invitation_fragment, null);

        this.couponTextfield = (EditText) view.findViewById(R.id.signup_invitation_textfield);

        nextButton = (Button) view.findViewById(R.id.signup_invitation_next);

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                parentActivity.userData.coupon = couponTextfield.getText().toString();
                nextButton.setEnabled(false);
                FloozRestClient.getInstance().showLoadView();
                FloozRestClient.getInstance().signupPassStep("invitation", parentActivity.userData.getParamsForStep(false), new FloozHttpResponseHandler() {
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

        if (this.parentActivity.userData.coupon != null && !this.parentActivity.userData.coupon.isEmpty())
            this.couponTextfield.setText(this.parentActivity.userData.coupon);

        if (this.initData != null) {
            if (this.initData.has("title")) {
                this.parentActivity.headerTitle.setText(this.initData.optString("title"));
            }

            if (this.initData.has("placeholder")) {
                this.couponTextfield.setHint(this.initData.optString("placeholder"));
            }

            if (this.initData.has("button")) {
                nextButton.setText(this.initData.optString("button"));
            }
        }

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(couponTextfield, InputMethodManager.SHOW_IMPLICIT);

        nextButton.setEnabled(true);
    }

    @Override
    public void onPause() {
        super.onPause();
        InputMethodManager imm = (InputMethodManager) parentActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(parentActivity.getWindow().getDecorView().getRootView().getWindowToken(), 0);
    }
}