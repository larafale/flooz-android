package me.flooz.app.UI.Fragment.Signin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Map;

import me.flooz.app.App.FloozApplication;
import me.flooz.app.Model.FLError;
import me.flooz.app.Network.FloozHttpResponseHandler;
import me.flooz.app.Network.FloozRestClient;
import me.flooz.app.R;
import me.flooz.app.UI.View.NumericKeyboard;
import me.flooz.app.Utils.CustomFonts;

/**
 * Created by Flooz on 11/27/14.
 */
public class SigninCodeFragment extends SigninBaseFragment  implements NumericKeyboard.NumericKeyboardDelegate {

    private LinearLayout codeContainer;
    private ImageView codeChar1;
    private ImageView codeChar2;
    private ImageView codeChar3;
    private ImageView codeChar4;
    private NumericKeyboard keyboard;

    private String currentCode = "";

    public Boolean confirmMode = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.signin_code_fragment, null);

        TextView hintText = (TextView) view.findViewById(R.id.signin_pass_hint);
        this.codeContainer = (LinearLayout) view.findViewById(R.id.signin_pass_container);
        this.codeChar1 = (ImageView) view.findViewById(R.id.signin_pass_char_1);
        this.codeChar2 = (ImageView) view.findViewById(R.id.signin_pass_char_2);
        this.codeChar3 = (ImageView) view.findViewById(R.id.signin_pass_char_3);
        this.codeChar4 = (ImageView) view.findViewById(R.id.signin_pass_char_4);
        this.keyboard = (NumericKeyboard) view.findViewById(R.id.signin_pass_keypad);

        hintText.setTypeface(CustomFonts.customContentRegular(inflater.getContext()));

        if (this.confirmMode)
            hintText.setText(R.string.SECORE_CODE_CHOOSE_CONFIRM);
        else
            hintText.setText(R.string.SECORE_CODE_CHOOSE);

        this.keyboard.delegate = this;
        this.keyboard.maxLenght = 4;

        if (!this.confirmMode) {
            this.parentActivity.userData.passCode = "";
            this.parentActivity.hideBackButton();
        }
        else {
            this.parentActivity.showBackButton();
        }

        return view;
    }

    private void refreshCodeContainer() {

        if (this.currentCode.length() >= 1)
            this.codeChar1.setSelected(true);
        else
            this.codeChar1.setSelected(false);

        if (this.currentCode.length() >= 2)
            this.codeChar2.setSelected(true);
        else
            this.codeChar2.setSelected(false);

        if (this.currentCode.length() >= 3)
            this.codeChar3.setSelected(true);
        else
            this.codeChar3.setSelected(false);

        if (this.currentCode.length() == 4) {
            this.codeChar4.setSelected(true);

            if (!this.confirmMode) {
                this.parentActivity.userData.passCode = this.currentCode;
                this.parentActivity.gotToNextPage();
            }
            else if (this.currentCode.contentEquals(this.parentActivity.userData.passCode)) {

                Map<String, Object> data = new HashMap<>(1);
                data.put("secureCode", this.currentCode);

                FloozRestClient.getInstance().showLoadView();
                FloozRestClient.getInstance().updateUser(data, new FloozHttpResponseHandler() {
                    @Override
                    public void success(Object response) {
                        parentActivity.floozApp.displayMainView();
                        FloozRestClient.getInstance().setSecureCode(currentCode);
                    }

                    @Override
                    public void failure(int statusCode, FLError error) {

                    }
                });
            }
            else {
                Animation shake = AnimationUtils.loadAnimation(FloozApplication.getInstance(), R.anim.secure_code_shake);
                shake.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) { }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        currentCode = "";
                        keyboard.value = "";
                        refreshCodeContainer();
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) { }
                });
                this.codeContainer.startAnimation(shake);
            }
        }
        else
            this.codeChar4.setSelected(false);
    }

    @Override
    public void keyPressed() {
        this.currentCode = keyboard.value;
        this.refreshCodeContainer();
    }
}
