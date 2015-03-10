package me.flooz.app.UI.Activity.Settings;

import android.app.Activity;
import android.os.Bundle;
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
import me.flooz.app.Network.FloozHttpResponseHandler;
import me.flooz.app.Network.FloozRestClient;
import me.flooz.app.R;
import me.flooz.app.Utils.CustomFonts;

/**
 * Created by Flooz on 3/10/15.
 */
public class BankSettingsActivity extends Activity {

    private BankSettingsActivity instance;
    private FloozApplication floozApp;
    private Boolean modal;

    private ImageView headerBackButton;
    private TextView headerTitle;
    private EditText ibanTextfield;
    private Button saveButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.instance = this;
        this.floozApp = (FloozApplication) this.getApplicationContext();
        this.modal = getIntent().getBooleanExtra("modal", false);

        this.setContentView(R.layout.settings_bank_fragment);

        this.headerBackButton = (ImageView) this.findViewById(R.id.settings_bank_header_back);
        this.headerTitle = (TextView) this.findViewById(R.id.settings_bank_header_title);
        this.ibanTextfield = (EditText) this.findViewById(R.id.settings_bank_iban);
        this.saveButton = (Button) this.findViewById(R.id.settings_bank_save);

        this.headerTitle.setTypeface(CustomFonts.customTitleExtraLight(this));
        this.ibanTextfield.setTypeface(CustomFonts.customContentRegular(this));

        this.headerBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
                if (modal)
                    overridePendingTransition(android.R.anim.fade_in, R.anim.slide_down);
                else
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right);
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
                if (s.length() > 0 && s.toString().indexOf("FR") != 0) {
                    s.insert(0, "FR");
                }

                if (s.length() == 2)
                    s.delete(0, s.length());

                if (s.length() > 27)
                    s.delete(27, s.length());

                if (s.length() == 27)
                    saveButton.setEnabled(true);
                else
                    saveButton.setEnabled(false);
            }
        });

        this.ibanTextfield.setOnEditorActionListener(new TextView.OnEditorActionListener() {
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
    public void onResume() {
        super.onResume();
        floozApp.setCurrentActivity(this);
    }

    @Override
    public void onPause() {
        clearReferences();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        clearReferences();
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
