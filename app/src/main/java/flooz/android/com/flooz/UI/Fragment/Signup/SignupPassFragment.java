package flooz.android.com.flooz.UI.Fragment.Signup;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import flooz.android.com.flooz.App.FloozApplication;
import flooz.android.com.flooz.Model.FLError;
import flooz.android.com.flooz.Network.FloozHttpResponseHandler;
import flooz.android.com.flooz.Network.FloozRestClient;
import flooz.android.com.flooz.R;
import flooz.android.com.flooz.UI.View.NumericKeyboard;
import flooz.android.com.flooz.Utils.CustomFonts;
import flooz.android.com.flooz.Utils.ImageHelper;

/**
 * Created by Flooz on 11/19/14.
 */
public class SignupPassFragment extends SignupBaseFragment implements NumericKeyboard.NumericKeyboardDelegate {

    private TextView hintText;
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
        View view = inflater.inflate(R.layout.signup_pass_fragment, null);

        this.hintText = (TextView) view.findViewById(R.id.signup_pass_hint);
        this.codeContainer = (LinearLayout) view.findViewById(R.id.signup_pass_container);
        this.codeChar1 = (ImageView) view.findViewById(R.id.signup_pass_char_1);
        this.codeChar2 = (ImageView) view.findViewById(R.id.signup_pass_char_2);
        this.codeChar3 = (ImageView) view.findViewById(R.id.signup_pass_char_3);
        this.codeChar4 = (ImageView) view.findViewById(R.id.signup_pass_char_4);
        this.keyboard = (NumericKeyboard) view.findViewById(R.id.signup_pass_keypad);

        this.hintText.setTypeface(CustomFonts.customContentRegular(inflater.getContext()));

        if (this.confirmMode)
            this.hintText.setText(R.string.SECORE_CODE_CHOOSE_CONFIRM);
        else
            this.hintText.setText(R.string.SECORE_CODE_CHOOSE);

        this.keyboard.delegate = this;
        this.keyboard.maxLenght = 4;

        if (!this.confirmMode)
            this.parentActivity.userData.passCode = "";

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
            else if (this.currentCode.equals(this.parentActivity.userData.passCode)) {
                FloozRestClient.getInstance().showLoadView();
                FloozRestClient.getInstance().signup(this.parentActivity.userData.getParamsForStep(true), new FloozHttpResponseHandler() {
                    @Override
                    public void success(Object response) {
                        if (parentActivity.userData.avatarData != null) {
                            Handler handler = new Handler();
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    FloozRestClient.getInstance().uploadDocument("picId", ImageHelper.convertBitmapInFile(parentActivity.userData.avatarData), new FloozHttpResponseHandler() {
                                        @Override
                                        public void success(Object response) {
                                        }

                                        @Override
                                        public void failure(int statusCode, FLError error) {

                                        }
                                    });
                                }
                            });
                        }
                        FloozRestClient.getInstance().setSecureCode(currentCode);
                        parentActivity.floozApp.displayMainView();
                        FloozRestClient.getInstance().sendUserContacts();
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