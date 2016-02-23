package me.flooz.app.UI.Controllers;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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

import me.flooz.app.Model.FLError;
import me.flooz.app.Network.FloozHttpResponseHandler;
import me.flooz.app.Network.FloozRestClient;
import me.flooz.app.R;
import me.flooz.app.UI.Activity.HomeActivity;
import me.flooz.app.Utils.CustomFonts;
import me.flooz.app.Utils.FLTriggerManager;

/**
 * Created by Flooz on 9/1/15.
 */
public class BankController extends BaseController {

    private EditText ibanTextfield;
    private Button saveButton;

    private Boolean success = false;

    public BankController(@NonNull View mainView, @NonNull Activity parentActivity, @NonNull ControllerKind kind) {
        super(mainView, parentActivity, kind);

        this.init();
    }

    public BankController(@NonNull View mainView, @NonNull Activity parentActivity, @NonNull ControllerKind kind, @Nullable JSONObject data) {
        super(mainView, parentActivity, kind, data);

        this.init();
    }

    @Override
    protected void init() {
        super.init();

        this.ibanTextfield = (EditText) this.currentView.findViewById(R.id.settings_bank_iban);
        this.saveButton = (Button) this.currentView.findViewById(R.id.settings_bank_save);

        this.ibanTextfield.setTypeface(CustomFonts.customContentRegular(this.parentActivity));

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
                        success = true;
                        closeButton.performClick();
                    }

                    @Override
                    public void failure(int statusCode, FLError error) {

                    }
                });
            }
        });
    }

    @Override
    public void onStop() {
        if (success && triggersData != null && triggersData.has("success")) {
            FLTriggerManager.getInstance().executeTriggerList(FLTriggerManager.convertTriggersJSONArrayToList(triggersData.optJSONArray("success")));
            success = false;
        }
    }

    @Override
    public void onDestroy() {
        if (success && triggersData != null && triggersData.has("success")) {
            FLTriggerManager.getInstance().executeTriggerList(FLTriggerManager.convertTriggersJSONArrayToList(triggersData.optJSONArray("success")));
            success = false;
        }
    }

    private void reloadView() {
        if (FloozRestClient.getInstance().currentUser.sepa != null) {
            this.ibanTextfield.setText((String)FloozRestClient.getInstance().currentUser.sepa.get("iban"));
        }
    }
}
