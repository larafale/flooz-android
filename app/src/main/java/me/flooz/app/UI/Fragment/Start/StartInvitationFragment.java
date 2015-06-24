package me.flooz.app.UI.Fragment.Start;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONObject;

import me.flooz.app.Model.FLError;
import me.flooz.app.Network.FloozHttpResponseHandler;
import me.flooz.app.Network.FloozRestClient;
import me.flooz.app.R;
import me.flooz.app.Utils.CustomFonts;

/**
 * Created by Flooz on 6/15/15.
 */
public class StartInvitationFragment extends StartBaseFragment {

    EditText couponTextfield;
    Button nextButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.start_invitation_fragment, null);

        TextView title = (TextView) view.findViewById(R.id.start_invitation_title);
        this.couponTextfield = (EditText) view.findViewById(R.id.start_invitation_textfield);

        title.setTypeface(CustomFonts.customTitleLight(inflater.getContext()));
        couponTextfield.setTypeface(CustomFonts.customContentRegular(inflater.getContext()));

        nextButton = (Button) view.findViewById(R.id.start_invitation_next);

        nextButton.setOnClickListener(v -> {
            parentActivity.signupData.put("coupon", couponTextfield.getText().toString());
            FloozRestClient.getInstance().showLoadView();
            FloozRestClient.getInstance().signupPassStep("invitation", parentActivity.signupData, new FloozHttpResponseHandler() {
                @Override
                public void success(Object response) {
                    JSONObject responseObject = (JSONObject) response;

                    if (responseObject.has("step") && responseObject.optJSONObject("step").has("next")) {
                        if (responseObject.optJSONObject("step").optString("next").contentEquals("signup")) {
                            FloozRestClient.getInstance().showLoadView();
                            FloozRestClient.getInstance().signupPassStep("signup", parentActivity.signupData, new FloozHttpResponseHandler() {
                                @Override
                                public void success(Object response) {
                                    JSONObject responseObject = (JSONObject) response;

                                    FloozRestClient.getInstance().updateCurrentUserAfterSignup(responseObject);

                                    if (responseObject.has("step") && responseObject.optJSONObject("step").has("next")) {
                                        StartBaseFragment nextFragment = StartBaseFragment.getSignupFragmentFromId(responseObject.optJSONObject("step").optString("next"), responseObject.optJSONObject("step"));

                                        if (nextFragment != null) {
                                            parentActivity.changeCurrentPage(nextFragment, R.animator.slide_in_left, R.animator.slide_out_right, R.animator.slide_in_right, R.animator.slide_out_left, true);
                                        }
                                    }
                                }

                                @Override
                                public void failure(int statusCode, FLError error) {

                                }
                            });
                        } else {
                            StartBaseFragment nextFragment = StartBaseFragment.getSignupFragmentFromId(responseObject.optJSONObject("step").optString("next"), responseObject.optJSONObject("step"));

                            if (nextFragment != null) {
                                parentActivity.changeCurrentPage(nextFragment, R.animator.slide_in_left, R.animator.slide_out_right, R.animator.slide_in_right, R.animator.slide_out_left, true);
                            }
                        }
                    }
                }

                @Override
                public void failure(int statusCode, FLError error) {

                }
            });

        });

        if (this.parentActivity.signupData.containsKey("coupon") && !this.parentActivity.signupData.get("coupon").toString().isEmpty())
            this.couponTextfield.setText(this.parentActivity.signupData.get("coupon").toString());

        if (this.initData != null) {
            if (this.initData.has("title")) {
                title.setText(this.initData.optString("title"));
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
    }

    @Override
    public void onPause() {
        super.onPause();
        InputMethodManager imm = (InputMethodManager) parentActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(parentActivity.getWindow().getDecorView().getRootView().getWindowToken(), 0);
    }

}
