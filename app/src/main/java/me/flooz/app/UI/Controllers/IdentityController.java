package me.flooz.app.UI.Controllers;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Map;

import me.flooz.app.App.FloozApplication;
import me.flooz.app.Model.FLError;
import me.flooz.app.Model.FLUser;
import me.flooz.app.Network.FloozHttpResponseHandler;
import me.flooz.app.Network.FloozRestClient;
import me.flooz.app.R;
import me.flooz.app.UI.Activity.HomeActivity;
import me.flooz.app.Utils.CustomFonts;
import me.flooz.app.Utils.CustomNotificationIntents;

/**
 * Created by Flooz on 9/1/15.
 */
public class IdentityController extends BaseController {

    private ImageView headerBackButton;

    private EditText firstnameTextfield;
    private EditText lastnameTextfield;
    private EditText phoneTextfield;
    private EditText emailTextfield;
    private EditText addressTextfield;
    private EditText zipCodeTextfield;
    private EditText cityTextfield;

    private TextView sendVerifyPhone;
    private TextView sendVerifyMail;

    private Button saveButton;

    private BroadcastReceiver reloadCurrentUserDataReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            reloadView();
        }
    };

    public IdentityController(@NonNull View mainView, @NonNull Activity parentActivity, @NonNull ControllerKind kind) {
        super(mainView, parentActivity, kind);

        this.headerBackButton = (ImageView) this.currentView.findViewById(R.id.header_item_left);
        TextView headerTitle = (TextView) this.currentView.findViewById(R.id.header_title);
        this.firstnameTextfield = (EditText) this.currentView.findViewById(R.id.settings_identity_firstname);
        this.lastnameTextfield = (EditText) this.currentView.findViewById(R.id.settings_identity_lastname);
        this.phoneTextfield = (EditText) this.currentView.findViewById(R.id.settings_coord_phone);
        this.emailTextfield = (EditText) this.currentView.findViewById(R.id.settings_coord_email);
        this.addressTextfield = (EditText) this.currentView.findViewById(R.id.settings_coord_address);
        this.zipCodeTextfield = (EditText) this.currentView.findViewById(R.id.settings_coord_zip);
        this.cityTextfield = (EditText) this.currentView.findViewById(R.id.settings_coord_city);
        this.sendVerifyPhone = (TextView) this.currentView.findViewById(R.id.settings_coord_verify_phone);
        this.sendVerifyMail = (TextView) this.currentView.findViewById(R.id.settings_coord_verify_email);
        this.saveButton = (Button) this.currentView.findViewById(R.id.settings_coord_save);

        headerTitle.setTypeface(CustomFonts.customTitleExtraLight(this.parentActivity));
        this.firstnameTextfield.setTypeface(CustomFonts.customContentRegular(this.parentActivity));
        this.lastnameTextfield.setTypeface(CustomFonts.customContentRegular(this.parentActivity));
        this.phoneTextfield.setTypeface(CustomFonts.customContentRegular(this.parentActivity));
        this.emailTextfield.setTypeface(CustomFonts.customContentRegular(this.parentActivity));
        this.addressTextfield.setTypeface(CustomFonts.customContentRegular(this.parentActivity));
        this.zipCodeTextfield.setTypeface(CustomFonts.customContentRegular(this.parentActivity));
        this.cityTextfield.setTypeface(CustomFonts.customContentRegular(this.parentActivity));
        this.sendVerifyPhone.setTypeface(CustomFonts.customContentRegular(this.parentActivity));
        this.sendVerifyMail.setTypeface(CustomFonts.customContentRegular(this.parentActivity));

        this.reloadView();

        if (currentKind == ControllerKind.FRAGMENT_CONTROLLER)
            this.headerBackButton.setImageDrawable(this.parentActivity.getResources().getDrawable(R.drawable.nav_back));

        this.headerBackButton.setOnClickListener(view -> {
            if (currentKind == ControllerKind.ACTIVITY_CONTROLLER) {
                parentActivity.finish();
                parentActivity.overridePendingTransition(android.R.anim.fade_in, R.anim.slide_down);
            } else {
                ((HomeActivity)this.parentActivity).popFragmentInCurrentTab();
            }
        });

        this.sendVerifyPhone.setOnClickListener(v -> {
            FloozRestClient.getInstance().sendSMSValidation();
            v.setVisibility(View.GONE);
        });

        this.sendVerifyMail.setOnClickListener(v -> {
            FloozRestClient.getInstance().sendEmailValidation();
            v.setVisibility(View.GONE);
        });


        this.phoneTextfield.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > 10)
                    s.delete(10, s.length());
            }
        });

        this.zipCodeTextfield.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > 5)
                    s.delete(5, s.length());
            }
        });

        this.cityTextfield.setOnEditorActionListener((v, actionId, event) -> {
            if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE)) {
                saveButton.performClick();
            }
            return false;
        });

        this.saveButton.setOnClickListener(v -> {
            FLUser currentUser = FloozRestClient.getInstance().currentUser;

            Map<String, Object> param = new HashMap<>();
            Map<String, Object> settings = new HashMap<>();
            Map<String, Object> address = new HashMap<>();

            if (!firstnameTextfield.getText().toString().contentEquals(currentUser.firstname))
                param.put("firstName", firstnameTextfield.getText().toString());

            if (!lastnameTextfield.getText().toString().contentEquals(currentUser.lastname))
                param.put("lastName", lastnameTextfield.getText().toString());

            if (!phoneTextfield.getText().toString().contentEquals(currentUser.phone.replace("+33", "0")))
                param.put("phone", phoneTextfield.getText().toString());

            if (!emailTextfield.getText().toString().contentEquals(currentUser.email))
                param.put("email", emailTextfield.getText().toString());

            if (((currentUser.address.get("address") == null || (currentUser.address.get("address") != null && currentUser.address.get("address").isEmpty())) && addressTextfield.getText().length() > 0)
                    || (currentUser.address.get("address") != null && !currentUser.address.get("address").isEmpty() && !addressTextfield.getText().toString().contentEquals(currentUser.address.get("address"))))
                address.put("address", addressTextfield.getText().toString());

            if (((currentUser.address.get("zipCode") == null || (currentUser.address.get("zipCode") != null && currentUser.address.get("zipCode").isEmpty())) && zipCodeTextfield.getText().length() > 0)
                    || (currentUser.address.get("zipCode") != null && !currentUser.address.get("zipCode").isEmpty() && !zipCodeTextfield.getText().toString().contentEquals(currentUser.address.get("zipCode")))) {
                address.put("zipCode", zipCodeTextfield.getText().toString());
            }

            if (((currentUser.address.get("city") == null || (currentUser.address.get("city") != null && currentUser.address.get("city").isEmpty())) && cityTextfield.getText().length() > 0)
                    || (currentUser.address.get("city") != null && !currentUser.address.get("city").isEmpty() && !cityTextfield.getText().toString().contentEquals(currentUser.address.get("city")))) {
                address.put("city", cityTextfield.getText().toString());
            }

            if (address.size() > 0) {
                settings.put("address", address);
                param.put("settings", settings);
            }

            FloozRestClient.getInstance().showLoadView();
            FloozRestClient.getInstance().updateUser(param, new FloozHttpResponseHandler() {
                @Override
                public void success(Object response) {
                    headerBackButton.performClick();
                }

                @Override
                public void failure(int statusCode, FLError error) {

                }
            });
        });
    }

    private void reloadView() {
        FLUser currentUser = FloozRestClient.getInstance().currentUser;

        this.firstnameTextfield.setText(currentUser.firstname);
        this.lastnameTextfield.setText(currentUser.lastname);
        this.phoneTextfield.setText(currentUser.phone.replace("+33", "0"));
        this.emailTextfield.setText(currentUser.email);
        this.addressTextfield.setText(currentUser.address.get("address"));
        this.zipCodeTextfield.setText(currentUser.address.get("zipCode"));
        this.cityTextfield.setText(currentUser.address.get("city"));

        if (currentUser.checkDocuments.get("phone").equals(3)) {
            this.sendVerifyPhone.setVisibility(View.VISIBLE);
            this.phoneTextfield.setFocusable(false);
            this.phoneTextfield.setFocusableInTouchMode(false);
            this.phoneTextfield.setCursorVisible(false);
            this.phoneTextfield.setClickable(false);
        }
        else {
            this.sendVerifyPhone.setVisibility(View.GONE);
            this.phoneTextfield.setFocusable(true);
            this.phoneTextfield.setFocusableInTouchMode(true);
            this.phoneTextfield.setCursorVisible(true);
            this.phoneTextfield.setClickable(true);
        }

        if (currentUser.checkDocuments.get("email").equals(3)) {
            this.sendVerifyMail.setVisibility(View.VISIBLE);
            this.emailTextfield.setFocusable(false);
            this.emailTextfield.setFocusableInTouchMode(false);
            this.emailTextfield.setCursorVisible(false);
            this.emailTextfield.setClickable(false);
        }
        else {
            this.sendVerifyMail.setVisibility(View.GONE);
            this.emailTextfield.setFocusable(true);
            this.emailTextfield.setFocusableInTouchMode(true);
            this.emailTextfield.setCursorVisible(true);
            this.emailTextfield.setClickable(true);
        }
    }

    @Override
    public void onResume() {
        this.reloadView();

        LocalBroadcastManager.getInstance(FloozApplication.getAppContext()).registerReceiver(reloadCurrentUserDataReceiver,
                CustomNotificationIntents.filterReloadCurrentUser());
    }

    @Override
    public void onPause() {
        LocalBroadcastManager.getInstance(FloozApplication.getAppContext()).unregisterReceiver(reloadCurrentUserDataReceiver);
    }

    @Override
    public void onBackPressed() {
        this.headerBackButton.performClick();
    }
}
