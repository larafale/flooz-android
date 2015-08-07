package me.flooz.app.UI.Fragment.Start;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.makeramen.RoundedImageView;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.json.JSONObject;

import io.branch.referral.Branch;
import io.branch.referral.BranchApp;
import me.flooz.app.App.FloozApplication;
import me.flooz.app.Model.FLError;
import me.flooz.app.Network.FloozHttpResponseHandler;
import me.flooz.app.Network.FloozRestClient;
import me.flooz.app.R;
import me.flooz.app.UI.Activity.WebContentActivity;
import me.flooz.app.Utils.CustomFonts;

/**
 * Created by Flooz on 6/11/15.
 */
public class StartSignupFragment extends StartBaseFragment {

    private EditText firstnameTextfield;
    private EditText lastnameTextfield;
    private EditText emailTextfield;
    private EditText nicknameTextfield;
    private EditText phoneTextfield;
    private EditText passwordTextfield;
    private EditText sponsorTextfield;
    private RelativeLayout fbButtonView;
    private RelativeLayout fbPicView;
    private RoundedImageView fbPic;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.start_signup_fragment, null);

        TextView title = (TextView) view.findViewById(R.id.start_signup_title);
        LinearLayout fbButton = (LinearLayout) view.findViewById(R.id.start_signup_facebook);
        TextView orLabel = (TextView) view.findViewById(R.id.start_signup_or);
        firstnameTextfield = (EditText) view.findViewById(R.id.start_signup_firstname);
        lastnameTextfield = (EditText) view.findViewById(R.id.start_signup_lastname);
        emailTextfield = (EditText) view.findViewById(R.id.start_signup_email);
        nicknameTextfield = (EditText) view.findViewById(R.id.start_signup_nick);
        phoneTextfield = (EditText) view.findViewById(R.id.start_signup_phone);
        passwordTextfield = (EditText) view.findViewById(R.id.start_signup_password);
        sponsorTextfield = (EditText) view.findViewById(R.id.start_signup_sponsor);
        Button signupButton = (Button) view.findViewById(R.id.start_signup_next);
        Button cguButton = (Button) view.findViewById(R.id.start_signup_cgu);
        ImageView facebookPicto = (ImageView) view.findViewById(R.id.start_signup_facebook_picto);
        fbButtonView = (RelativeLayout) view.findViewById(R.id.start_signup_fb_button_view);
        fbPicView = (RelativeLayout) view.findViewById(R.id.start_signup_fb_pic_view);
        fbPic = (RoundedImageView) view.findViewById(R.id.start_signup_fb_pic);

        firstnameTextfield.setTypeface(CustomFonts.customContentRegular(inflater.getContext()));
        lastnameTextfield.setTypeface(CustomFonts.customContentRegular(inflater.getContext()));
        emailTextfield.setTypeface(CustomFonts.customContentRegular(inflater.getContext()));
        nicknameTextfield.setTypeface(CustomFonts.customContentRegular(inflater.getContext()));
        phoneTextfield.setTypeface(CustomFonts.customContentRegular(inflater.getContext()));
        passwordTextfield.setTypeface(CustomFonts.customContentRegular(inflater.getContext()));
        sponsorTextfield.setTypeface(CustomFonts.customContentRegular(inflater.getContext()));

        orLabel.setTypeface(CustomFonts.customContentLight(inflater.getContext()));
        title.setTypeface(CustomFonts.customTitleLight(inflater.getContext()));
        cguButton.setTypeface(CustomFonts.customContentLight(inflater.getContext()));

        facebookPicto.setColorFilter(inflater.getContext().getResources().getColor(android.R.color.white));

        lastnameTextfield.setOnEditorActionListener((v, actionId, event) -> {
            if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE)) {
                firstnameTextfield.requestFocus();
            }
            return false;
        });

        phoneTextfield.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > 10) {
                    s.delete(10, s.length());
                    emailTextfield.requestFocus();
                }
            }
        });

        fbButton.setOnClickListener(v -> {
            FloozRestClient.getInstance().showLoadView();
            FloozRestClient.getInstance().connectFacebook();
        });

        signupButton.setOnClickListener(v -> {
            parentActivity.hideKeyboard();


            if (FloozApplication.getInstance().branchParams != null && FloozApplication.getInstance().branchParams.has("referrer"))
                parentActivity.signupData.put("referrer",  FloozApplication.getInstance().branchParams.optString("referrer"));

            parentActivity.signupData.put("distinctId", FloozApplication.mixpanelAPI.getPeople().getDistinctId());
            parentActivity.signupData.put("firstName", firstnameTextfield.getText().toString());
            parentActivity.signupData.put("lastName", lastnameTextfield.getText().toString());
            parentActivity.signupData.put("nick", nicknameTextfield.getText().toString());
            parentActivity.signupData.put("email", emailTextfield.getText().toString());
            parentActivity.signupData.put("phone", phoneTextfield.getText().toString());
            parentActivity.signupData.put("password", passwordTextfield.getText().toString());
            parentActivity.signupData.put("sponsor", sponsorTextfield.getText().toString());

            FloozRestClient.getInstance().showLoadView();
            FloozRestClient.getInstance().signupPassStep("profile", parentActivity.signupData, new FloozHttpResponseHandler() {
                @Override
                public void success(Object response) {
                    JSONObject responseObject = (JSONObject) response;

                    StartBaseFragment.handleStepResponse(responseObject, parentActivity);
                }

                @Override
                public void failure(int statusCode, FLError error) {

                }
            });
        });

        cguButton.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.putExtra("title", inflater.getContext().getResources().getString(R.string.INFORMATIONS_TERMS));
            intent.putExtra("url", "https://www.flooz.me/cgu?layout=webview");
            intent.putExtra("modal", true);
            intent.setClass(parentActivity, WebContentActivity.class);
            parentActivity.startActivity(intent);
            parentActivity.overridePendingTransition(R.anim.slide_up, android.R.anim.fade_out);
        });

        this.refreshView();

        return view;
    }

    @Override
    public void refreshView() {
        super.refreshView();

        if (parentActivity.signupData.containsKey("firstName"))
            firstnameTextfield.setText((String)parentActivity.signupData.get("firstName"));

        if (parentActivity.signupData.containsKey("lastName"))
            lastnameTextfield.setText((String)parentActivity.signupData.get("lastName"));

        if (parentActivity.signupData.containsKey("email"))
            emailTextfield.setText((String)parentActivity.signupData.get("email"));

        if (parentActivity.signupData.containsKey("phone"))
            phoneTextfield.setText((String)parentActivity.signupData.get("phone"));

        if (parentActivity.signupData.containsKey("nick"))
            nicknameTextfield.setText((String)parentActivity.signupData.get("nick"));

        if (parentActivity.signupData.containsKey("avatarURL")) {
            ImageLoader.getInstance().displayImage((String)parentActivity.signupData.get("avatarURL"), this.fbPic);
            fbPicView.setVisibility(View.VISIBLE);
            fbButtonView.setVisibility(View.GONE);
        } else {
            fbPicView.setVisibility(View.GONE);
            fbButtonView.setVisibility(View.VISIBLE);
        }
    }
}
