package flooz.android.com.flooz.UI.Fragment.Signup;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import flooz.android.com.flooz.App.FloozApplication;
import flooz.android.com.flooz.Model.FLError;
import flooz.android.com.flooz.Network.FloozHttpResponseHandler;
import flooz.android.com.flooz.Network.FloozRestClient;
import flooz.android.com.flooz.R;
import flooz.android.com.flooz.Utils.CustomFonts;

/**
 * Created by Flooz on 11/13/14.
 */
public class SignupUsernameFragment extends SignupBaseFragment {

    private TextView infoLabel;
    private Button nextButton;
    private EditText usernameTextfield;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.signup_username_fragment, null);

        usernameTextfield = (EditText) view.findViewById(R.id.signup_username_textfield);
        nextButton = (Button) view.findViewById(R.id.signup_username_next);
        infoLabel = (TextView) view.findViewById(R.id.signup_username_info_label);

        infoLabel.setTypeface(CustomFonts.customContentLight(inflater.getContext()));

        usernameTextfield.setTypeface(CustomFonts.customContentLight(inflater.getContext()));

        if (parentActivity.userData.username != null) {
            usernameTextfield.setText(parentActivity.userData.username);
            usernameTextfield.setSelection(parentActivity.userData.username.length());
        }

        this.usernameTextfield.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE)) {
                    nextButton.performClick();
                }
                return false;
            }
        });

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                parentActivity.userData.username = usernameTextfield.getText().toString();
                FloozRestClient.getInstance().signupPassStep("nick", parentActivity.userData.getParamsForStep(false), new FloozHttpResponseHandler() {
                    @Override
                    public void success(Object response) {
                        parentActivity.gotToNextPage();
                    }

                    @Override
                    public void failure(int statusCode, FLError error) {}
                });
            }
        });

        usernameTextfield.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                if (charSequence.length() >= 3)
                    nextButton.setEnabled(true);
                else
                    nextButton.setEnabled(false);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        usernameTextfield.requestFocus();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(FloozApplication.getAppContext().INPUT_METHOD_SERVICE);
        imm.showSoftInput(usernameTextfield, InputMethodManager.SHOW_IMPLICIT);
    }

    @Override
    public void onPause() {
        super.onPause();
        InputMethodManager imm = (InputMethodManager) parentActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(parentActivity.getWindow().getDecorView().getRootView().getWindowToken(), 0);
    }
}