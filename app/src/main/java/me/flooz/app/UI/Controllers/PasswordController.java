package me.flooz.app.UI.Controllers;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import me.flooz.app.Model.FLError;
import me.flooz.app.Network.FloozHttpResponseHandler;
import me.flooz.app.Network.FloozRestClient;
import me.flooz.app.R;
import me.flooz.app.UI.Activity.HomeActivity;
import me.flooz.app.Utils.CustomFonts;

/**
 * Created by Flooz on 9/2/15.
 */
public class PasswordController extends BaseController {

    private EditText oldPassword;
    private EditText newPassword;
    private EditText confirmPassword;
    private Button saveButton;

    public PasswordController(@NonNull View mainView, @NonNull Activity parentActivity, @NonNull ControllerKind kind) {
        super(mainView, parentActivity, kind);

        this.init();
    }

    public PasswordController(@NonNull View mainView, @NonNull Activity parentActivity, @NonNull ControllerKind kind, @Nullable JSONObject data) {
        super(mainView, parentActivity, kind, data);

        this.init();
    }

    @Override
    protected void init() {
        super.init();

        this.oldPassword = (EditText) this.currentView.findViewById(R.id.settings_password_old_pass);
        this.newPassword = (EditText) this.currentView.findViewById(R.id.settings_password_new_pass);
        this.confirmPassword = (EditText) this.currentView.findViewById(R.id.settings_password_new_pass_confirm);
        this.saveButton = (Button) this.currentView.findViewById(R.id.settings_password_save);

        this.oldPassword.setTypeface(CustomFonts.customContentRegular(this.parentActivity));
        this.newPassword.setTypeface(CustomFonts.customContentRegular(this.parentActivity));
        this.confirmPassword.setTypeface(CustomFonts.customContentRegular(this.parentActivity));

        this.confirmPassword.setOnEditorActionListener(new TextView.OnEditorActionListener() {
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
                FloozRestClient.getInstance().showLoadView();

                Map<String, Object> params = new HashMap<>();

                params.put("password", oldPassword.getText().toString());
                params.put("newPassword", newPassword.getText().toString());
                params.put("confirm", confirmPassword.getText().toString());

                FloozRestClient.getInstance().updateUserPassword(params, new FloozHttpResponseHandler() {
                    @Override
                    public void success(Object response) {
                        PasswordController.this.onBackPressed();
                    }

                    @Override
                    public void failure(int statusCode, FLError error) {

                    }
                });
            }
        });
    }

    @Override
    public void onResume() {
        this.oldPassword.setText("");
        this.newPassword.setText("");
        this.confirmPassword.setText("");
        this.oldPassword.requestFocus();
        InputMethodManager imm = (InputMethodManager) this.parentActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(this.oldPassword, InputMethodManager.SHOW_IMPLICIT);
    }
}
