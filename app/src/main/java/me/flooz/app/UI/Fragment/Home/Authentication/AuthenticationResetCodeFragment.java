package me.flooz.app.UI.Fragment.Home.Authentication;

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
 * Created by Flooz on 12/12/14.
 */
public class AuthenticationResetCodeFragment extends AuthenticationBaseFragment implements NumericKeyboard.NumericKeyboardDelegate {
    private LinearLayout codeContainer;
    private ImageView codeChar1;
    private ImageView codeChar2;
    private ImageView codeChar3;
    private ImageView codeChar4;
    private NumericKeyboard keyboard;

    public String lastCode = "";
    public String currentCode = "";
    public Boolean confirmMode = false;

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.authentication_reset_secure_code_fragment, null);

        TextView hintText = (TextView) view.findViewById(R.id.authentication_reset_secure_code_hint);
        this.codeContainer = (LinearLayout) view.findViewById(R.id.authentication_reset_secure_code_container);
        this.codeChar1 = (ImageView) view.findViewById(R.id.authentication_reset_secure_code_char_1);
        this.codeChar2 = (ImageView) view.findViewById(R.id.authentication_reset_secure_code_char_2);
        this.codeChar3 = (ImageView) view.findViewById(R.id.authentication_reset_secure_code_char_3);
        this.codeChar4 = (ImageView) view.findViewById(R.id.authentication_reset_secure_code_char_4);
        this.keyboard = (NumericKeyboard) view.findViewById(R.id.authentication_reset_secure_code_keypad);

        hintText.setTypeface(CustomFonts.customContentRegular(inflater.getContext()));

        if (this.confirmMode)
            hintText.setText(R.string.SECORE_CODE_CHOOSE_CONFIRM);
        else
            hintText.setText(R.string.SECORE_CODE_CHOOSE);

        this.keyboard.delegate = this;
        this.keyboard.maxLenght = 4;

        if (this.parentActivity.changeSecureCode) {
            this.parentActivity.showBackButton();
        } else {
            if (!this.confirmMode) {
                this.parentActivity.hideBackButton();
            } else {
                this.parentActivity.showBackButton();
            }
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
                this.parentActivity.gotToNextPage();
            }
            else if (this.currentCode.contentEquals(this.lastCode)) {

                Map<String, Object> data = new HashMap<>(1);
                data.put("secureCode", this.currentCode);

                FloozRestClient.getInstance().showLoadView();
                FloozRestClient.getInstance().updateUser(data, new FloozHttpResponseHandler() {
                    @Override
                    public void success(Object response) {
                        parentActivity.validateTransaction();
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
