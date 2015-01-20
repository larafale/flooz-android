package flooz.android.com.flooz.UI.Fragment.Signin;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Map;

import flooz.android.com.flooz.Model.FLError;
import flooz.android.com.flooz.Network.FloozHttpResponseHandler;
import flooz.android.com.flooz.Network.FloozRestClient;
import flooz.android.com.flooz.R;
import flooz.android.com.flooz.UI.Activity.SigninActivity;
import flooz.android.com.flooz.Utils.CustomFonts;

/**
 * Created by Flooz on 11/27/14.
 */
public class SigninPassFragment extends SigninBaseFragment {

    private Button nextButton;
    private EditText passwordTextfield;
    private EditText confirmTextfield;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.signin_pass_fragment, null);

        this.confirmTextfield = (EditText) view.findViewById(R.id.signin_pass_confirm);
        this.passwordTextfield = (EditText) view.findViewById(R.id.signin_pass_pass);
        this.nextButton = (Button) view.findViewById(R.id.signin_pass_next);

        this.confirmTextfield.setTypeface(CustomFonts.customContentLight(inflater.getContext()));
        this.passwordTextfield.setTypeface(CustomFonts.customContentLight(inflater.getContext()));

        this.confirmTextfield.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (passwordTextfield.getText().length() >= 6 && confirmTextfield.getText().length() >= 6 && passwordTextfield.getText().toString().contentEquals(confirmTextfield.getText().toString())) {
                    nextButton.setEnabled(true);
                }
                else {
                    nextButton.setEnabled(false);
                }
            }
        });

        this.passwordTextfield.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (passwordTextfield.getText().length() >= 6 && confirmTextfield.getText().length() >= 6 && passwordTextfield.getText().toString().contentEquals(confirmTextfield.getText().toString())) {
                    nextButton.setEnabled(true);
                }
                else {
                    nextButton.setEnabled(false);
                }
            }
        });

        this.confirmTextfield.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE)) {
                    nextButton.performClick();
                }
                return false;
            }
        });

        this.nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Map<String, Object> data = new HashMap<String, Object>(3);

                data.put("phone", parentActivity.userData.phone);
                data.put("newPassword", passwordTextfield.getText().toString());
                data.put("confirm", confirmTextfield.getText().toString());

                FloozRestClient.getInstance().showLoadView();
                FloozRestClient.getInstance().updateUserPassword(data, new FloozHttpResponseHandler() {
                    @Override
                    public void success(Object response) {
                        FloozRestClient.getInstance().showLoadView();
                        FloozRestClient.getInstance().loginWithPseudoAndPassword(parentActivity.userData.phone, passwordTextfield.getText().toString(), new FloozHttpResponseHandler() {
                            @Override
                            public void success(Object response) {
                                if (parentActivity.userHasSecureCode)
                                    parentActivity.floozApp.displayMainView();
                                else
                                    parentActivity.changeCurrentPage(SigninActivity.SigninPageIdentifier.SigninCode, R.animator.slide_in_left, R.animator.slide_out_right);
                            }

                            @Override
                            public void failure(int statusCode, FLError error) {
                            }
                        });
                    }

                    @Override
                    public void failure(int statusCode, FLError error) {
                        Log.d("Reset Pass", "" + statusCode);
                    }
                });
            }
        });

        return view;
    }
}
