package me.flooz.app.UI.Controllers;

import android.app.Activity;
import android.support.annotation.NonNull;
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

import me.flooz.app.Model.FLError;
import me.flooz.app.Network.FloozHttpResponseHandler;
import me.flooz.app.Network.FloozRestClient;
import me.flooz.app.R;
import me.flooz.app.UI.Activity.HomeActivity;
import me.flooz.app.Utils.CustomFonts;

/**
 * Created by Flooz on 9/1/15.
 */
public class BankController extends BaseController {

    private ImageView headerBackButton;
    private EditText ibanTextfield;
    private Button saveButton;

    public BankController(@NonNull View mainView, @NonNull final Activity parentActivity, @NonNull ControllerKind kind) {
        super(mainView, parentActivity, kind);

        this.headerBackButton = (ImageView) this.currentView.findViewById(R.id.header_item_left);
        TextView headerTitle = (TextView) this.currentView.findViewById(R.id.header_title);
        this.ibanTextfield = (EditText) this.currentView.findViewById(R.id.settings_bank_iban);
        this.saveButton = (Button) this.currentView.findViewById(R.id.settings_bank_save);

        headerTitle.setTypeface(CustomFonts.customTitleExtraLight(this.parentActivity));
        this.ibanTextfield.setTypeface(CustomFonts.customContentRegular(this.parentActivity));

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

        this.reloadView();

        this.ibanTextfield.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > 27)
                    s.delete(27, s.length());
            }
        });

        this.ibanTextfield.setOnEditorActionListener(new TextView.OnEditorActionListener() {
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
                Map<String, Object> sepa = new HashMap<>();
                sepa.put("iban", ibanTextfield.getText());

                Map<String, Object> settings = new HashMap<>();
                settings.put("sepa", sepa);

                Map<String, Object> user = new HashMap<>();
                user.put("settings", settings);

                FloozRestClient.getInstance().updateUser(user, new FloozHttpResponseHandler() {
                    @Override
                    public void success(Object response) {

                    }

                    @Override
                    public void failure(int statusCode, FLError error) {

                    }
                });
            }
        });
    }

    private void reloadView() {
        if (FloozRestClient.getInstance().currentUser.sepa != null) {
            this.ibanTextfield.setText((String)FloozRestClient.getInstance().currentUser.sepa.get("iban"));
        }
    }

    @Override
    public void onBackPressed() {
        this.headerBackButton.performClick();
    }
}
