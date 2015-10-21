package me.flooz.app.UI.Controllers;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
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

    private ImageView headerBackButton;

    private EditText oldPassword;
    private EditText newPassword;
    private EditText confirmPassword;
    private Button saveButton;

    public PasswordController(@NonNull View mainView, @NonNull final Activity parentActivity, @NonNull ControllerKind kind) {
        super(mainView, parentActivity, kind);

        this.headerBackButton = (ImageView) this.currentView.findViewById(R.id.header_item_left);
        TextView headerTitle = (TextView) this.currentView.findViewById(R.id.header_title);
        this.oldPassword = (EditText) this.currentView.findViewById(R.id.settings_password_old_pass);
        this.newPassword = (EditText) this.currentView.findViewById(R.id.settings_password_new_pass);
        this.confirmPassword = (EditText) this.currentView.findViewById(R.id.settings_password_new_pass_confirm);
        this.saveButton = (Button) this.currentView.findViewById(R.id.settings_password_save);

        headerTitle.setTypeface(CustomFonts.customTitleExtraLight(this.parentActivity));
        this.oldPassword.setTypeface(CustomFonts.customContentRegular(this.parentActivity));
        this.newPassword.setTypeface(CustomFonts.customContentRegular(this.parentActivity));
        this.confirmPassword.setTypeface(CustomFonts.customContentRegular(this.parentActivity));

        if (currentKind == ControllerKind.FRAGMENT_CONTROLLER)
            this.headerBackButton.setImageDrawable(this.parentActivity.getResources().getDrawable(R.drawable.nav_back));

        this.headerBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentKind == ControllerKind.ACTIVITY_CONTROLLER) {
                    parentActivity.finish();
                    parentActivity.overridePendingTransition(android.R.anim.fade_in, R.anim.slide_down);
                } else {
                    ((HomeActivity) parentActivity).popFragmentInCurrentTab();
                }
            }
        });

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
                        headerBackButton.performClick();
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

    @Override
    public void onBackPressed() {
        this.headerBackButton.performClick();
    }
}
