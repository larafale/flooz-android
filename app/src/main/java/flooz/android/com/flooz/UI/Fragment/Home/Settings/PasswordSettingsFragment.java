package flooz.android.com.flooz.UI.Fragment.Home.Settings;

import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Map;

import flooz.android.com.flooz.Model.FLError;
import flooz.android.com.flooz.Network.FloozHttpResponseHandler;
import flooz.android.com.flooz.Network.FloozRestClient;
import flooz.android.com.flooz.R;
import flooz.android.com.flooz.UI.Fragment.Home.HomeBaseFragment;
import flooz.android.com.flooz.Utils.CustomFonts;

/**
 * Created by Flooz on 12/15/14.
 */
public class PasswordSettingsFragment extends HomeBaseFragment {

    private ImageView headerBackButton;
    private TextView headerTitle;

    private EditText oldPassword;
    private EditText newPassword;
    private EditText confirmPassword;
    private Button saveButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.settings_password_fragment, null);

        this.headerBackButton = (ImageView) view.findViewById(R.id.settings_password_header_back);
        this.headerTitle = (TextView) view.findViewById(R.id.settings_password_header_title);
        this.oldPassword = (EditText) view.findViewById(R.id.settings_password_old_pass);
        this.newPassword = (EditText) view.findViewById(R.id.settings_password_new_pass);
        this.confirmPassword = (EditText) view.findViewById(R.id.settings_password_new_pass_confirm);
        this.saveButton = (Button) view.findViewById(R.id.settings_password_save);

        this.headerTitle.setTypeface(CustomFonts.customTitleExtraLight(inflater.getContext()));
        this.oldPassword.setTypeface(CustomFonts.customContentRegular(inflater.getContext()));
        this.newPassword.setTypeface(CustomFonts.customContentRegular(inflater.getContext()));
        this.confirmPassword.setTypeface(CustomFonts.customContentRegular(inflater.getContext()));

        this.headerBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                parentActivity.popMainFragment(R.animator.slide_out_left, R.animator.slide_in_right);
            }
        });

        this.oldPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                canValidate();
            }
        });

        this.newPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                canValidate();
            }
        });

        this.confirmPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                canValidate();
            }
        });

        this.confirmPassword.setOnEditorActionListener(new TextView.OnEditorActionListener() {
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

                params.put("phone", FloozRestClient.getInstance().currentUser.phone);
                params.put("password", oldPassword.getText().toString());
                params.put("newPassword", newPassword.getText().toString());
                params.put("confirm", confirmPassword.getText().toString());

                FloozRestClient.getInstance().updateUserPassword(params, new FloozHttpResponseHandler() {
                    @Override
                    public void success(Object response) {
                        parentActivity.popMainFragment(R.animator.slide_out_left, R.animator.slide_in_right);
                    }

                    @Override
                    public void failure(int statusCode, FLError error) {

                    }
                });
            }
        });

        return view;
    }

    public boolean canValidate() {
        boolean valid = false;

        if (this.oldPassword.getText().length() >= 1
                && this.newPassword.getText().length() >= 6
                && this.confirmPassword.getText().length() >= 6
                && this.newPassword.getText().toString().contentEquals(this.confirmPassword.getText().toString())) {
            valid = true;
        }

        this.saveButton.setEnabled(valid);

        return valid;
    }

    @Override
    public void onBackPressed() {
        this.headerBackButton.performClick();
    }
}
