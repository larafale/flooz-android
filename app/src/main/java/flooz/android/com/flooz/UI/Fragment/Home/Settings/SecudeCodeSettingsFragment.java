package flooz.android.com.flooz.UI.Fragment.Home.Settings;

import android.content.Context;
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

import flooz.android.com.flooz.Model.FLError;
import flooz.android.com.flooz.Network.FloozHttpResponseHandler;
import flooz.android.com.flooz.Network.FloozRestClient;
import flooz.android.com.flooz.R;
import flooz.android.com.flooz.UI.Fragment.Home.HomeBaseFragment;
import flooz.android.com.flooz.UI.View.NumericKeyboard;
import flooz.android.com.flooz.Utils.CustomFonts;

/**
 * Created by Flooz on 12/15/14.
 */
public class SecudeCodeSettingsFragment extends HomeBaseFragment implements NumericKeyboard.NumericKeyboardDelegate {

    private Context context;

    private String currentCode = "";
    private String previousCode = "";
    private boolean authenticate = false;
    private boolean confirmMode = false;

    private ImageView headerBackButton;

    private TextView infoText;
    private LinearLayout codeContainer;
    private ImageView codeChar1;
    private ImageView codeChar2;
    private ImageView codeChar3;
    private ImageView codeChar4;
    private NumericKeyboard keyboard;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.settings_secure_code_fragment, null);

        this.context = inflater.getContext();

        this.headerBackButton = (ImageView) view.findViewById(R.id.settings_secure_code_header_back);
        this.infoText = (TextView) view.findViewById(R.id.settings_secure_code_infos);
        this.codeContainer = (LinearLayout) view.findViewById(R.id.settings_secure_code_container);
        this.codeChar1 = (ImageView) view.findViewById(R.id.settings_secure_code_char_1);
        this.codeChar2 = (ImageView) view.findViewById(R.id.settings_secure_code_char_2);
        this.codeChar3 = (ImageView) view.findViewById(R.id.settings_secure_code_char_3);
        this.codeChar4 = (ImageView) view.findViewById(R.id.settings_secure_code_char_4);

        this.keyboard = (NumericKeyboard) view.findViewById(R.id.settings_secure_code_keypad);

        this.infoText.setTypeface(CustomFonts.customTitleLight(this.context));

        this.keyboard.delegate = this;
        this.keyboard.maxLenght = 4;

        this.headerBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!confirmMode) {
                    parentActivity.popMainFragment(R.animator.slide_out_left, R.animator.slide_in_right);
                } else {
                    confirmMode = false;
                    previousCode = "";
                    refreshContent();
                }
            }
        });

        this.currentCode = "";
        this.previousCode = "";
        this.keyboard.value = "";
        this.authenticate = false;
        this.confirmMode = false;

        return view;
    }


    @Override
    public void keyPressed() {
        addKeyToCode(String.format("%c", keyboard.value.toCharArray()[keyboard.value.length() - 1]));
    }

    private void addKeyToCode(String key) {
        if (this.currentCode.length() < 4)
            this.currentCode += key;
        this.refreshCodeContainer();
    }

    public void refreshContent() {
        if (authenticate && confirmMode) {
            this.infoText.setText(R.string.SECORE_CODE_TEXT_CONFIRM);
        } else if (authenticate) {
            this.infoText.setText(R.string.SECORE_CODE_TEXT_NEW);
        } else {
            this.infoText.setText(R.string.SECORE_CODE_TEXT_CURRENT);
        }

        this.currentCode = "";
        this.keyboard.value = "";

        this.refreshCodeContainer();
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

            if (!authenticate) {
                if (FloozRestClient.getInstance().getSecureCode() != null && FloozRestClient.getInstance().secureCodeIsValid(this.currentCode)) {
                    this.authenticate = true;
                    this.refreshContent();
                } else if (FloozRestClient.getInstance().getSecureCode() == null) {
                    FloozRestClient.getInstance().checkSecureCodeForUser(this.currentCode, new FloozHttpResponseHandler() {
                        @Override
                        public void success(Object response) {
                            authenticate = true;
                            refreshContent();
                        }

                        @Override
                        public void failure(int statusCode, FLError error) {
                            errorAnimation();
                        }
                    });
                } else {
                    this.errorAnimation();
                }
            } else if (!confirmMode) {
                this.previousCode = this.currentCode;
                this.confirmMode = true;
                this.refreshContent();
            } else {
                if (this.currentCode.contentEquals(this.previousCode)) {
                    FloozRestClient.getInstance().showLoadView();

                    Map<String, Object> params = new HashMap<>();
                    params.put("secureCode", this.currentCode);

                    FloozRestClient.getInstance().updateUser(params, new FloozHttpResponseHandler() {
                        @Override
                        public void success(Object response) {
                            FloozRestClient.getInstance().setSecureCode(currentCode);
                            parentActivity.popMainFragment(R.animator.slide_out_left, R.animator.slide_in_right);
                        }

                        @Override
                        public void failure(int statusCode, FLError error) {
                        }
                    });
                } else {
                    this.errorAnimation();
                }
            }
        }
        else
            this.codeChar4.setSelected(false);
    }

    public void errorAnimation() {
        Animation shake = AnimationUtils.loadAnimation(this.context, R.anim.secure_code_shake);
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

    @Override
    public void onBackPressed() {
        this.headerBackButton.performClick();
    }
}
