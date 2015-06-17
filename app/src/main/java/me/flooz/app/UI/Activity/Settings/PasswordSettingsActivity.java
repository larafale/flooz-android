package me.flooz.app.UI.Activity.Settings;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Map;

import me.flooz.app.App.FloozApplication;
import me.flooz.app.Model.FLError;
import me.flooz.app.Network.FloozHttpResponseHandler;
import me.flooz.app.Network.FloozRestClient;
import me.flooz.app.R;
import me.flooz.app.Utils.CustomFonts;
import me.flooz.app.Utils.FLHelper;
import me.flooz.app.Utils.ViewServer;

/**
 * Created by Flooz on 3/10/15.
 */
public class PasswordSettingsActivity extends Activity {

    private FloozApplication floozApp;
    private Boolean modal;

    private ImageView headerBackButton;

    private EditText oldPassword;
    private EditText newPassword;
    private EditText confirmPassword;
    private Button saveButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (FLHelper.isDebuggable())
            ViewServer.get(this).addWindow(this);

        this.floozApp = (FloozApplication) this.getApplicationContext();
        this.modal = getIntent().getBooleanExtra("modal", false);

        this.setContentView(R.layout.settings_password_fragment);

        this.headerBackButton = (ImageView) this.findViewById(R.id.settings_password_header_back);
        TextView headerTitle = (TextView) this.findViewById(R.id.settings_password_header_title);
        this.oldPassword = (EditText) this.findViewById(R.id.settings_password_old_pass);
        this.newPassword = (EditText) this.findViewById(R.id.settings_password_new_pass);
        this.confirmPassword = (EditText) this.findViewById(R.id.settings_password_new_pass_confirm);
        this.saveButton = (Button) this.findViewById(R.id.settings_password_save);

        headerTitle.setTypeface(CustomFonts.customTitleExtraLight(this));
        this.oldPassword.setTypeface(CustomFonts.customContentRegular(this));
        this.newPassword.setTypeface(CustomFonts.customContentRegular(this));
        this.confirmPassword.setTypeface(CustomFonts.customContentRegular(this));

        this.headerBackButton.setOnClickListener(view -> {
            finish();
            if (modal)
                overridePendingTransition(android.R.anim.fade_in, R.anim.slide_down);
            else
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right);
        });

        this.confirmPassword.setOnEditorActionListener((v, actionId, event) -> {
            if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE)) {
                saveButton.performClick();
            }
            return false;
        });

        this.saveButton.setOnClickListener(v -> {
            FloozRestClient.getInstance().showLoadView();

            Map<String, Object> params = new HashMap<>();

            params.put("password", oldPassword.getText().toString());
            params.put("newPassword", newPassword.getText().toString());
            params.put("confirm", confirmPassword.getText().toString());

            FloozRestClient.getInstance().updateUserPassword(params, new FloozHttpResponseHandler() {
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


    @Override
    public void onResume() {
        super.onResume();
        floozApp.setCurrentActivity(this);

        if (FLHelper.isDebuggable())
            ViewServer.get(this).setFocusedWindow(this);

        this.oldPassword.setText("");
        this.newPassword.setText("");
        this.confirmPassword.setText("");
        this.oldPassword.requestFocus();
        InputMethodManager imm = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(this.oldPassword, InputMethodManager.SHOW_IMPLICIT);
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
