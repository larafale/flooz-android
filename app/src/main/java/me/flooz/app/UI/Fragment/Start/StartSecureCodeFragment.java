package me.flooz.app.UI.Fragment.Start;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONObject;
import org.w3c.dom.Text;

import me.flooz.app.App.FloozApplication;
import me.flooz.app.Model.FLError;
import me.flooz.app.Network.FloozHttpResponseHandler;
import me.flooz.app.Network.FloozRestClient;
import me.flooz.app.R;
import me.flooz.app.UI.View.NumericKeyboard;
import me.flooz.app.Utils.CustomFonts;
import me.flooz.app.Utils.ImageHelper;

/**
 * Created by Flooz on 6/15/15.
 */
public class StartSecureCodeFragment extends StartBaseFragment implements NumericKeyboard.NumericKeyboardDelegate {

    private LinearLayout codeContainer;
    private ImageView codeChar1;
    private ImageView codeChar2;
    private ImageView codeChar3;
    private ImageView codeChar4;
    private NumericKeyboard keyboard;

    private String currentCode = "";

    public Boolean confirmMode = false;
    public Boolean needConfirm = true;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.start_secure_code_fragment, null);

        TextView title = (TextView) view.findViewById(R.id.start_secure_code_title);
        TextView hintText = (TextView) view.findViewById(R.id.start_secure_code_hint);
        this.codeContainer = (LinearLayout) view.findViewById(R.id.start_secure_code_container);
        this.codeChar1 = (ImageView) view.findViewById(R.id.start_secure_code_char_1);
        this.codeChar2 = (ImageView) view.findViewById(R.id.start_secure_code_char_2);
        this.codeChar3 = (ImageView) view.findViewById(R.id.start_secure_code_char_3);
        this.codeChar4 = (ImageView) view.findViewById(R.id.start_secure_code_char_4);
        this.keyboard = (NumericKeyboard) view.findViewById(R.id.start_secure_code_keypad);

        title.setTypeface(CustomFonts.customTitleLight(inflater.getContext()));
        hintText.setTypeface(CustomFonts.customContentRegular(inflater.getContext()));

        if (this.initData != null && this.initData.has("confirm"))
            this.needConfirm = this.initData.optBoolean("confirm");

        if (this.confirmMode)
            hintText.setText(R.string.SECORE_CODE_CHOOSE_CONFIRM);
        else
            hintText.setText(R.string.SECORE_CODE_CHOOSE);

        this.keyboard.delegate = this;
        this.keyboard.maxLenght = 4;

        if (!this.confirmMode)
            this.parentActivity.signupData.put("secureCode", "");

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

            if (this.needConfirm && !this.confirmMode) {
                this.parentActivity.signupData.put("secureCode", this.currentCode);
                StartSecureCodeFragment nextFragment = new StartSecureCodeFragment();
                nextFragment.setInitData(this.initData);
                nextFragment.confirmMode = true;
                parentActivity.changeCurrentPage(nextFragment, R.animator.slide_in_left, R.animator.slide_out_right, R.animator.slide_in_right, R.animator.slide_out_left, true);
            }
            else if (!this.needConfirm || this.currentCode.contentEquals((String) this.parentActivity.signupData.get("secureCode"))) {
                this.parentActivity.signupData.put("secureCode", this.currentCode);
                FloozRestClient.getInstance().showLoadView();
                FloozRestClient.getInstance().signupPassStep("secureCode", parentActivity.signupData, new FloozHttpResponseHandler() {
                    @Override
                    public void success(Object response) {
                        JSONObject responseObject = (JSONObject) response;
                        StartBaseFragment.handleStepResponse(responseObject, parentActivity);
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
