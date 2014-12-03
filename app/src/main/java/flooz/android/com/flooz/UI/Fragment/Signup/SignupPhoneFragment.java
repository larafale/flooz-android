package flooz.android.com.flooz.UI.Fragment.Signup;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import flooz.android.com.flooz.Network.FloozRestClient;
import flooz.android.com.flooz.R;
import flooz.android.com.flooz.UI.View.NumericKeyboard;
import flooz.android.com.flooz.Utils.CustomFonts;

/**
 * Created by Flooz on 11/12/14.
 */
public class SignupPhoneFragment extends SignupBaseFragment implements NumericKeyboard.NumericKeyboardDelegate {

    private TextView infoLabel;
    private Button nextButton;
    private EditText phoneTextfield;
    private NumericKeyboard keyboard;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.signup_phone_fragment, null);

        phoneTextfield = (EditText) view.findViewById(R.id.signup_phone_textfield);
        nextButton = (Button) view.findViewById(R.id.signup_phone_next);
        infoLabel = (TextView) view.findViewById(R.id.signup_phone_info_label);
        keyboard = (NumericKeyboard) view.findViewById(R.id.signup_phone_keypad);

        if (!parentActivity.startSignup)
            infoLabel.setVisibility(View.GONE);

        infoLabel.setTypeface(CustomFonts.customContentLight(inflater.getContext()));

        keyboard.delegate = this;

        phoneTextfield.requestFocus();
        phoneTextfield.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return true;
            }
        });
        phoneTextfield.setTypeface(CustomFonts.customContentLight(inflater.getContext()));

        if (parentActivity.userData.phone != null) {
            phoneTextfield.setText(parentActivity.userData.phone);
            if (phoneTextfield.getText().toString().startsWith("+"))
                keyboard.maxLenght = 13;
            else
                keyboard.maxLenght = 10;

            if (phoneTextfield.getText().length() >= 10)
                nextButton.setEnabled(true);

            keyboard.value = parentActivity.userData.phone;
            phoneTextfield.setSelection(keyboard.value.length());
        }

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FloozRestClient.getInstance().loginWithPhone(keyboard.value);
            }
        });

        return view;
    }

    @Override
    public void keyPressed() {
        phoneTextfield.setText(keyboard.value);
        if (keyboard.value.length() >= 10)
            nextButton.setEnabled(true);
        else
            nextButton.setEnabled(false);
        phoneTextfield.setSelection(keyboard.value.length());

        if (phoneTextfield.getText().toString().startsWith("+"))
            keyboard.maxLenght = 13;
        else
            keyboard.maxLenght = 10;
    }
}