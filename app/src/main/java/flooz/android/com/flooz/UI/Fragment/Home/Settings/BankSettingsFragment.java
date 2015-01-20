package flooz.android.com.flooz.UI.Fragment.Home.Settings;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.Button;
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
public class BankSettingsFragment extends HomeBaseFragment {

    private ImageView headerBackButton;
    private TextView headerTitle;
    private EditText ibanTextfield;
    private Button saveButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.settings_bank_fragment, null);

        this.headerBackButton = (ImageView) view.findViewById(R.id.settings_bank_header_back);
        this.headerTitle = (TextView) view.findViewById(R.id.settings_bank_header_title);
        this.ibanTextfield = (EditText) view.findViewById(R.id.settings_bank_iban);
        this.saveButton = (Button) view.findViewById(R.id.settings_bank_save);

        this.headerTitle.setTypeface(CustomFonts.customTitleExtraLight(inflater.getContext()));
        this.ibanTextfield.setTypeface(CustomFonts.customContentRegular(inflater.getContext()));

        this.headerBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                parentActivity.popMainFragment(R.animator.slide_out_left, R.animator.slide_in_right);
            }
        });

        if (FloozRestClient.getInstance().currentUser.sepa != null) {
            this.ibanTextfield.setText((String)FloozRestClient.getInstance().currentUser.sepa.get("iban"));
        }

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

    @Override
    public void onBackPressed() {
        this.headerBackButton.performClick();
    }
}
