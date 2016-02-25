package me.flooz.app.UI.Controllers;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import me.flooz.app.App.FloozApplication;
import me.flooz.app.Model.FLCountry;
import me.flooz.app.Model.FLError;
import me.flooz.app.Model.FLUser;
import me.flooz.app.Network.FloozHttpResponseHandler;
import me.flooz.app.Network.FloozRestClient;
import me.flooz.app.R;
import me.flooz.app.UI.Activity.HomeActivity;
import me.flooz.app.UI.View.FLPhoneField;
import me.flooz.app.Utils.CustomFonts;
import me.flooz.app.Utils.CustomNotificationIntents;
import me.flooz.app.Utils.FLHelper;

/**
 * Created by Flooz on 9/1/15.
 */
public class IdentityController extends BaseController implements FLPhoneField.FLPhoneFieldDelegate {

    private EditText firstnameTextfield;
    private EditText lastnameTextfield;
    private FLPhoneField phoneTextfield;
    private EditText emailTextfield;
    private EditText addressTextfield;
    private EditText zipCodeTextfield;
    private EditText cityTextfield;

    private TextView sendVerifyPhone;
    private TextView sendVerifyMail;

    private String currentPhone;

    private Button saveButton;

    private BroadcastReceiver reloadCurrentUserDataReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            reloadView();
        }
    };

    public IdentityController(@NonNull View mainView, @NonNull Activity parentActivity, @NonNull ControllerKind kind) {
        super(mainView, parentActivity, kind);

        this.init();
    }

    public IdentityController(@NonNull View mainView, @NonNull Activity parentActivity, @NonNull ControllerKind kind, @Nullable JSONObject data) {
        super(mainView, parentActivity, kind, data);

        this.init();
    }

    @Override
    protected void init() {
        super.init();

        this.firstnameTextfield = (EditText) this.currentView.findViewById(R.id.settings_identity_firstname);
        this.lastnameTextfield = (EditText) this.currentView.findViewById(R.id.settings_identity_lastname);
        this.phoneTextfield = (FLPhoneField) this.currentView.findViewById(R.id.settings_coord_phone);
        this.emailTextfield = (EditText) this.currentView.findViewById(R.id.settings_coord_email);
        this.addressTextfield = (EditText) this.currentView.findViewById(R.id.settings_coord_address);
        this.zipCodeTextfield = (EditText) this.currentView.findViewById(R.id.settings_coord_zip);
        this.cityTextfield = (EditText) this.currentView.findViewById(R.id.settings_coord_city);
        this.sendVerifyPhone = (TextView) this.currentView.findViewById(R.id.settings_coord_verify_phone);
        this.sendVerifyMail = (TextView) this.currentView.findViewById(R.id.settings_coord_verify_email);
        this.saveButton = (Button) this.currentView.findViewById(R.id.settings_coord_save);

        this.firstnameTextfield.setTypeface(CustomFonts.customContentRegular(this.parentActivity));
        this.lastnameTextfield.setTypeface(CustomFonts.customContentRegular(this.parentActivity));
        this.emailTextfield.setTypeface(CustomFonts.customContentRegular(this.parentActivity));
        this.addressTextfield.setTypeface(CustomFonts.customContentRegular(this.parentActivity));
        this.zipCodeTextfield.setTypeface(CustomFonts.customContentRegular(this.parentActivity));
        this.cityTextfield.setTypeface(CustomFonts.customContentRegular(this.parentActivity));
        this.sendVerifyPhone.setTypeface(CustomFonts.customContentRegular(this.parentActivity));
        this.sendVerifyMail.setTypeface(CustomFonts.customContentRegular(this.parentActivity));

        this.phoneTextfield.delegate = this;

        this.reloadView();

        this.sendVerifyPhone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FloozRestClient.getInstance().sendSMSValidation();
                v.setVisibility(View.GONE);
            }
        });

        this.sendVerifyMail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FloozRestClient.getInstance().sendEmailValidation();
                v.setVisibility(View.GONE);
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

        this.cityTextfield.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE)) {
                    saveButton.performClick();
                }
                return false;
            }
        });

        this.saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FLUser currentUser = FloozRestClient.getInstance().currentUser;

                Map<String, Object> param = new HashMap<>();
                Map<String, Object> settings = new HashMap<>();
                Map<String, Object> address = new HashMap<>();

                if (!firstnameTextfield.getText().toString().contentEquals(currentUser.firstname))
                    param.put("firstName", firstnameTextfield.getText().toString());

                if (!lastnameTextfield.getText().toString().contentEquals(currentUser.lastname))
                    param.put("lastName", lastnameTextfield.getText().toString());

                if (currentPhone != null && !currentPhone.isEmpty())
                    param.put("phone", currentPhone);

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
                        IdentityController.this.onBackPressed();
                    }

                    @Override
                    public void failure(int statusCode, FLError error) {

                    }
                });
            }
        });
    }

    private void reloadView() {
        FLUser currentUser = FloozRestClient.getInstance().currentUser;

        this.firstnameTextfield.setText(currentUser.firstname);
        this.lastnameTextfield.setText(currentUser.lastname);
        this.emailTextfield.setText(currentUser.email);
        this.addressTextfield.setText(currentUser.address.get("address"));
        this.zipCodeTextfield.setText(currentUser.address.get("zipCode"));
        this.cityTextfield.setText(currentUser.address.get("city"));
        this.phoneTextfield.setCountry(currentUser.country);

        String phone = currentUser.phone.replace(currentUser.country.indicatif, "0");

        this.phoneTextfield.setPhoneNumber(phone);

        if (currentUser.checkDocuments.get("phone").equals(3)) {
            this.sendVerifyPhone.setVisibility(View.VISIBLE);
            this.phoneTextfield.setEnable(false);
        }
        else {
            this.sendVerifyPhone.setVisibility(View.GONE);
            this.phoneTextfield.setEnable(true);
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
    public void phoneChanged(String phone, FLCountry country) {
        if (phone != null && !phone.isEmpty()) {
            currentPhone = FLHelper.fullPhone(phone, country.code);
        }
    }

    @Override
    public void phoneNext() {
        if (this.emailTextfield.isClickable())
            this.emailTextfield.requestFocus();
        else
            this.addressTextfield.requestFocus();
    }
}
