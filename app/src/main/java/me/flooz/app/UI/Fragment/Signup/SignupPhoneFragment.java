package me.flooz.app.UI.Fragment.Signup;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import me.flooz.app.Network.FloozRestClient;
import me.flooz.app.R;
import me.flooz.app.UI.View.NumericKeyboard;
import me.flooz.app.Utils.CustomFonts;

/**
 * Created by Flooz on 11/12/14.
 */
public class SignupPhoneFragment extends SignupBaseFragment implements NumericKeyboard.NumericKeyboardDelegate {

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
        keyboard = (NumericKeyboard) view.findViewById(R.id.signup_phone_keypad);

        keyboard.delegate = this;

        phoneTextfield.setTypeface(CustomFonts.customContentLight(inflater.getContext()));

        phoneTextfield.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                v.onTouchEvent(event);
                InputMethodManager imm = (InputMethodManager)v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
                return true;
            }
        });

        if (parentActivity.userData.phone != null) {
            if (parentActivity.userData.phone.startsWith("+33"))
                parentActivity.userData.phone = parentActivity.userData.phone.replace("+33", "0");

            keyboard.maxLenght = 10;

            keyboard.value = parentActivity.userData.phone;
            phoneTextfield.setText(parentActivity.userData.phone);
            phoneTextfield.setSelection(keyboard.value.length());

            if (phoneTextfield.getText().length() >= 10)
                nextButton.setEnabled(true);
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