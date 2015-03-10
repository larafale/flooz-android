package me.flooz.app.UI.Fragment.Signup;

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
import android.widget.TextView;

import me.flooz.app.Model.FLError;
import me.flooz.app.Model.FLUser;
import me.flooz.app.Network.FloozHttpResponseHandler;
import me.flooz.app.Network.FloozRestClient;
import me.flooz.app.R;
import me.flooz.app.UI.Activity.SignupActivity;
import me.flooz.app.Utils.CustomFonts;

/**
 * Created by Flooz on 11/18/14.
 */
public class SignupInfosFragment extends SignupBaseFragment {

    private boolean allInstanciated = false;

    public EditText firtnameTextfield;
    public EditText lastnameTextfield;
    public EditText emailTextfield;
    public EditText birthdateTextfield;
    public EditText passwordTextfield;
    public TextView bithdateHint;
    public TextView passwordHint;
    public Button nextButton;
    public TextView cguLabel;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.signup_infos_fragment, null);

        this.firtnameTextfield = (EditText)view.findViewById(R.id.signup_infos_firstname);
        this.lastnameTextfield = (EditText)view.findViewById(R.id.signup_infos_lastname);
        this.emailTextfield = (EditText)view.findViewById(R.id.signup_infos_email);
        this.birthdateTextfield = (EditText)view.findViewById(R.id.signup_infos_birthdate);
        this.passwordTextfield = (EditText)view.findViewById(R.id.signup_infos_password);
        this.bithdateHint = (TextView)view.findViewById(R.id.signup_infos_birthdate_hint);
        this.passwordHint = (TextView)view.findViewById(R.id.signup_infos_password_hint);
        this.nextButton = (Button)view.findViewById(R.id.signup_infos_next);
        this.cguLabel = (TextView)view.findViewById(R.id.signup_infos_cgu);

        this.firtnameTextfield.setTypeface(CustomFonts.customContentLight(inflater.getContext()));
        this.lastnameTextfield.setTypeface(CustomFonts.customContentLight(inflater.getContext()));
        this.emailTextfield.setTypeface(CustomFonts.customContentLight(inflater.getContext()));
        this.birthdateTextfield.setTypeface(CustomFonts.customContentLight(inflater.getContext()));
        this.passwordTextfield.setTypeface(CustomFonts.customContentLight(inflater.getContext()));
        this.bithdateHint.setTypeface(CustomFonts.customContentLight(inflater.getContext()));
        this.passwordHint.setTypeface(CustomFonts.customContentLight(inflater.getContext()));
        this.cguLabel.setTypeface(CustomFonts.customContentLight(inflater.getContext()));

        InputFilter[] filterArray = new InputFilter[1];
        filterArray[0] = new InputFilter.LengthFilter(10);
        this.birthdateTextfield.setFilters(filterArray);

        this.firtnameTextfield.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {}

            @Override
            public void afterTextChanged(Editable editable) {
                validateForm();
            }
        });

        this.lastnameTextfield.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {}

            @Override
            public void afterTextChanged(Editable editable) {
                validateForm();
            }
        });

        this.emailTextfield.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {}

            @Override
            public void afterTextChanged(Editable editable) {
                validateForm();
            }
        });

        this.birthdateTextfield.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                if (charSequence.length() > 0 && bithdateHint.getVisibility() != View.GONE)
                    bithdateHint.setVisibility(View.GONE);
                if (charSequence.length() == 0 && bithdateHint.getVisibility() != View.VISIBLE)
                    bithdateHint.setVisibility(View.VISIBLE);

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.length() == 3 && editable.charAt(2) != '/')
                    editable.insert(2, "/");
                if (editable.length() == 6 && editable.charAt(5) != '/')
                    editable.insert(5, "/");

                if (editable.length() == 1 && editable.charAt(0) > '3')
                    editable.insert(0, "0");

                if (editable.length() == 4 && editable.charAt(3) > '1')
                    editable.insert(3, "0");

                if (editable.length() == 2 && editable.charAt(0) == '3' && editable.charAt(1) > '1')
                    editable.delete(1, 2);

                if (editable.length() == 5 && editable.charAt(3) == '1' && editable.charAt(4) > '2')
                    editable.delete(4, 5);

                if (editable.length() == 7 && editable.charAt(6) > '2') {
                    InputFilter[] filterArray = new InputFilter[1];
                    filterArray[0] = new InputFilter.LengthFilter(8);
                    birthdateTextfield.setFilters(filterArray);
                }
                else if (editable.length() == 7) {
                    InputFilter[] filterArray = new InputFilter[1];
                    filterArray[0] = new InputFilter.LengthFilter(10);
                    birthdateTextfield.setFilters(filterArray);
                }

                if ((editable.length() == 8 && editable.charAt(6) > '2') || editable.length() == 10)
                    passwordTextfield.requestFocus();

                validateForm();
            }
        });

        this.passwordTextfield.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                if (charSequence.length() > 0 && passwordHint.getVisibility() != View.GONE)
                    passwordHint.setVisibility(View.GONE);
                if (charSequence.length() == 0 && passwordHint.getVisibility() != View.VISIBLE)
                    passwordHint.setVisibility(View.VISIBLE);

            }

            @Override
            public void afterTextChanged(Editable editable) {
                validateForm();
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
            public void onClick(View view) {
                FloozRestClient.getInstance().showLoadView();
                FloozRestClient.getInstance().signupPassStep("infos", parentActivity.userData.getParamsForStep(false), new FloozHttpResponseHandler() {
                    @Override
                    public void success(Object response) {
                        parentActivity.gotToNextPage();
                    }

                    @Override
                    public void failure(int statusCode, FLError error) { }
                });
            }
        });

        this.cguLabel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                parentActivity.changeCurrentPage(SignupActivity.SignupPageIdentifier.SignupCGU, R.animator.slide_in_left, R.animator.slide_out_right);
            }
        });

        FLUser user = parentActivity.userData;

        if (user.firstname != null) {
            this.firtnameTextfield.setText(user.firstname);
            this.firtnameTextfield.setSelection(user.firstname.length());
        }

        if (user.lastname != null) {
            this.lastnameTextfield.setText(user.lastname);
            this.lastnameTextfield.setSelection(user.lastname.length());
        }

        if (user.email != null) {
            this.emailTextfield.setText(user.email);
            this.emailTextfield.setSelection(user.email.length());
        }

        if (user.birthdate != null) {
            this.birthdateTextfield.setText(user.birthdate);
            this.birthdateTextfield.setSelection(user.birthdate.length());
        }

        if (user.password != null) {
            this.passwordTextfield.setText(user.password);
            this.passwordTextfield.setSelection(user.password.length());
        }

        validateForm();

        this.allInstanciated = true;

        return view;
    }

    private boolean validateForm() {
        if (this.allInstanciated) {
            FLUser user = parentActivity.userData;

            user.firstname = this.firtnameTextfield.getText().toString();
            user.lastname = this.lastnameTextfield.getText().toString();
            user.email = this.emailTextfield.getText().toString();
            user.birthdate = this.birthdateTextfield.getText().toString();
            user.password = this.passwordTextfield.getText().toString();
        }

        return true;
    }
}
