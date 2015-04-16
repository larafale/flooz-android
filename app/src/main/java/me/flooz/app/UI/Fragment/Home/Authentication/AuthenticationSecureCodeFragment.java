package me.flooz.app.UI.Fragment.Home.Authentication;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import me.flooz.app.Model.FLError;
import me.flooz.app.Network.FloozHttpResponseHandler;
import me.flooz.app.Network.FloozRestClient;
import me.flooz.app.R;
import me.flooz.app.Utils.CustomFonts;

/**
 * Created by Flooz on 12/12/14.
 */
public class AuthenticationSecureCodeFragment extends AuthenticationBaseFragment {

    private Context context;

    private String currentCode = "";

    private ImageView headerBack;
    private LinearLayout codeContainer;
    private ImageView codeChar1;
    private ImageView codeChar2;
    private ImageView codeChar3;
    private ImageView codeChar4;

    private TextView clearCode;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.authentication_secure_code_fragment, null);

        this.context = inflater.getContext();

        this.headerBack = (ImageView) view.findViewById(R.id.secure_code_header_back);
        TextView infoText = (TextView) view.findViewById(R.id.secure_code_infos);
        this.codeContainer = (LinearLayout) view.findViewById(R.id.secure_code_container);
        this.codeChar1 = (ImageView) view.findViewById(R.id.secure_code_char_1);
        this.codeChar2 = (ImageView) view.findViewById(R.id.secure_code_char_2);
        this.codeChar3 = (ImageView) view.findViewById(R.id.secure_code_char_3);
        this.codeChar4 = (ImageView) view.findViewById(R.id.secure_code_char_4);
        Button codeKey1 = (Button) view.findViewById(R.id.secure_code_keyboard_key_1);
        Button codeKey2 = (Button) view.findViewById(R.id.secure_code_keyboard_key_2);
        Button codeKey3 = (Button) view.findViewById(R.id.secure_code_keyboard_key_3);
        Button codeKey4 = (Button) view.findViewById(R.id.secure_code_keyboard_key_4);
        Button codeKey5 = (Button) view.findViewById(R.id.secure_code_keyboard_key_5);
        Button codeKey6 = (Button) view.findViewById(R.id.secure_code_keyboard_key_6);
        Button codeKey7 = (Button) view.findViewById(R.id.secure_code_keyboard_key_7);
        Button codeKey8 = (Button) view.findViewById(R.id.secure_code_keyboard_key_8);
        Button codeKey9 = (Button) view.findViewById(R.id.secure_code_keyboard_key_9);
        Button codeKey0 = (Button) view.findViewById(R.id.secure_code_keyboard_key_0);
        TextView forgetCode = (TextView) view.findViewById(R.id.secure_code_forget);
        this.clearCode = (TextView) view.findViewById(R.id.secure_code_clear);

        infoText.setTypeface(CustomFonts.customContentRegular(inflater.getContext()));
        codeKey1.setTypeface(CustomFonts.customContentBold(inflater.getContext()));
        codeKey2.setTypeface(CustomFonts.customContentBold(inflater.getContext()));
        codeKey3.setTypeface(CustomFonts.customContentBold(inflater.getContext()));
        codeKey4.setTypeface(CustomFonts.customContentBold(inflater.getContext()));
        codeKey5.setTypeface(CustomFonts.customContentBold(inflater.getContext()));
        codeKey6.setTypeface(CustomFonts.customContentBold(inflater.getContext()));
        codeKey7.setTypeface(CustomFonts.customContentBold(inflater.getContext()));
        codeKey8.setTypeface(CustomFonts.customContentBold(inflater.getContext()));
        codeKey9.setTypeface(CustomFonts.customContentBold(inflater.getContext()));
        codeKey0.setTypeface(CustomFonts.customContentBold(inflater.getContext()));
        forgetCode.setTypeface(CustomFonts.customTitleLight(inflater.getContext()));
        this.clearCode.setTypeface(CustomFonts.customTitleLight(inflater.getContext()));

        codeKey0.setOnClickListener(keyClickListener);
        codeKey1.setOnClickListener(keyClickListener);
        codeKey2.setOnClickListener(keyClickListener);
        codeKey3.setOnClickListener(keyClickListener);
        codeKey4.setOnClickListener(keyClickListener);
        codeKey5.setOnClickListener(keyClickListener);
        codeKey6.setOnClickListener(keyClickListener);
        codeKey7.setOnClickListener(keyClickListener);
        codeKey8.setOnClickListener(keyClickListener);
        codeKey9.setOnClickListener(keyClickListener);

        this.clearCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                currentCode = "";
                refreshCodeContainer();
            }
        });

        this.headerBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                parentActivity.dismissView(false);
            }
        });

        forgetCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                parentActivity.gotToNextPage();
            }
        });

        return view;
    }

    private View.OnClickListener keyClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            TextView key = (TextView) view;
            addKeyToCode(key.getText().toString());
        }
    };

    private void refreshCodeContainer() {
        if (this.currentCode.length() == 0)
            this.clearCode.setVisibility(View.GONE);
        else
            this.clearCode.setVisibility(View.VISIBLE);

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

            if (FloozRestClient.getInstance().getSecureCode() != null && FloozRestClient.getInstance().secureCodeIsValid(this.currentCode)) {
                parentActivity.validateTransaction();

                this.currentCode = "";
                this.refreshCodeContainer();
            } else if (FloozRestClient.getInstance().getSecureCode() == null) {
                FloozRestClient.getInstance().showLoadView();
                FloozRestClient.getInstance().checkSecureCodeForUser(this.currentCode, new FloozHttpResponseHandler() {
                    @Override
                    public void success(Object response) {
                        parentActivity.validateTransaction();
                        currentCode = "";
                        refreshCodeContainer();
                    }

                    @Override
                    public void failure(int statusCode, FLError error) {
                        Animation shake = AnimationUtils.loadAnimation(context, R.anim.secure_code_shake);
                        shake.setAnimationListener(new Animation.AnimationListener() {
                            @Override
                            public void onAnimationStart(Animation animation) { }

                            @Override
                            public void onAnimationEnd(Animation animation) {
                                currentCode = "";
                                refreshCodeContainer();
                            }

                            @Override
                            public void onAnimationRepeat(Animation animation) { }
                        });
                        codeContainer.startAnimation(shake);
                    }
                });
            } else {
                Animation shake = AnimationUtils.loadAnimation(this.context, R.anim.secure_code_shake);
                shake.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) { }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        currentCode = "";
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

    private void addKeyToCode(String key) {
        if (this.currentCode.length() < 4)
            this.currentCode += key;
        this.refreshCodeContainer();
    }

    @Override
    public void onBackPressed() {
        this.headerBack.performClick();
    }
}
