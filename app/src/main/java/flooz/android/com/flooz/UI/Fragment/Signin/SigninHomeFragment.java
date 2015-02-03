package flooz.android.com.flooz.UI.Fragment.Signin;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import flooz.android.com.flooz.Model.FLError;
import flooz.android.com.flooz.Network.FloozHttpResponseHandler;
import flooz.android.com.flooz.Network.FloozRestClient;
import flooz.android.com.flooz.R;
import flooz.android.com.flooz.UI.Activity.SigninActivity;
import flooz.android.com.flooz.UI.Activity.SignupActivity;
import flooz.android.com.flooz.Utils.CustomFonts;

/**
 * Created by Flooz on 11/25/14.
 */
public class SigninHomeFragment extends SigninBaseFragment {

    private Button nextButton;
    private EditText phoneTextfield;
    private EditText passwordTextfield;
    private TextView forgetButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.signin_home_fragment, null);

        this.phoneTextfield = (EditText) view.findViewById(R.id.signin_home_phone);
        this.passwordTextfield = (EditText) view.findViewById(R.id.signin_home_password);
        this.nextButton = (Button) view.findViewById(R.id.signin_home_next);
        this.forgetButton = (TextView) view.findViewById(R.id.signin_home_pass_forget);

        this.phoneTextfield.setTypeface(CustomFonts.customContentLight(inflater.getContext()));
        this.passwordTextfield.setTypeface(CustomFonts.customContentLight(inflater.getContext()));
        this.forgetButton.setTypeface(CustomFonts.customContentLight(inflater.getContext()));

        if (this.parentActivity.userData.phone.isEmpty()) {
            this.phoneTextfield.requestFocus();
        }
        else {
            this.phoneTextfield.setText(this.parentActivity.userData.phone);
            this.passwordTextfield.requestFocus();
        }

        this.phoneTextfield.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (passwordTextfield.getText().length() >= 6 && s.length() >= 10) {
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
                if (s.length() >= 6 && phoneTextfield.getText().length() >= 8) {
                    nextButton.setEnabled(true);
                }
                else {
                    nextButton.setEnabled(false);
                }
            }
        });

        this.passwordTextfield.setOnEditorActionListener(new TextView.OnEditorActionListener() {
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
                FloozRestClient.getInstance().showLoadView();
                FloozRestClient.getInstance().loginWithPseudoAndPassword(phoneTextfield.getText().toString(), passwordTextfield.getText().toString(), new FloozHttpResponseHandler() {
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
        });

        this.forgetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String[] TO = {"support@flooz.me"};
                Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
                emailIntent.setData(Uri.parse("mailto:"));
                emailIntent.setType("text/plain");


                emailIntent.putExtra(Intent.EXTRA_EMAIL, TO);
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Mot de passe oublié");
                emailIntent.putExtra(Intent.EXTRA_TEXT, "J'ai oublié mon mot de passe. Je souhaite être contacté au " + phoneTextfield.getText().toString() + ".");

                try {
                    startActivity(Intent.createChooser(emailIntent, "Envoyer un mail..."));
                } catch (android.content.ActivityNotFoundException ex) {

                }
            }
        });

        return view;
    }
}