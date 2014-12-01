package flooz.android.com.flooz.UI.Fragment.Home;

import android.app.Fragment;
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

import flooz.android.com.flooz.Network.FloozRestClient;
import flooz.android.com.flooz.R;
import flooz.android.com.flooz.UI.Activity.HomeActivity;
import flooz.android.com.flooz.Utils.CustomFonts;

/**
 * Created by Flooz on 10/9/14.
 */
public class SecureCodeFragment extends Fragment {

    private Context context;

    public HomeActivity parentActivity;

    private String currentCode = "";

    public SecureCodeDelegate delegate;

    private ImageView headerBack;
    private TextView infoText;
    private LinearLayout codeContainer;
    private ImageView codeChar1;
    private ImageView codeChar2;
    private ImageView codeChar3;
    private ImageView codeChar4;

    private Button codeKey1;
    private Button codeKey2;
    private Button codeKey3;
    private Button codeKey4;
    private Button codeKey5;
    private Button codeKey6;
    private Button codeKey7;
    private Button codeKey8;
    private Button codeKey9;
    private Button codeKey0;

    private TextView forgetCode;
    private TextView clearCode;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.security_code_fragment, null);

        this.context = inflater.getContext();

        this.headerBack = (ImageView) view.findViewById(R.id.secure_code_header_back);
        this.infoText = (TextView) view.findViewById(R.id.secure_code_infos);
        this.codeContainer = (LinearLayout) view.findViewById(R.id.secure_code_container);
        this.codeChar1 = (ImageView) view.findViewById(R.id.secure_code_char_1);
        this.codeChar2 = (ImageView) view.findViewById(R.id.secure_code_char_2);
        this.codeChar3 = (ImageView) view.findViewById(R.id.secure_code_char_3);
        this.codeChar4 = (ImageView) view.findViewById(R.id.secure_code_char_4);
        this.codeKey1 = (Button) view.findViewById(R.id.secure_code_keyboard_key_1);
        this.codeKey2 = (Button) view.findViewById(R.id.secure_code_keyboard_key_2);
        this.codeKey3 = (Button) view.findViewById(R.id.secure_code_keyboard_key_3);
        this.codeKey4 = (Button) view.findViewById(R.id.secure_code_keyboard_key_4);
        this.codeKey5 = (Button) view.findViewById(R.id.secure_code_keyboard_key_5);
        this.codeKey6 = (Button) view.findViewById(R.id.secure_code_keyboard_key_6);
        this.codeKey7 = (Button) view.findViewById(R.id.secure_code_keyboard_key_7);
        this.codeKey8 = (Button) view.findViewById(R.id.secure_code_keyboard_key_8);
        this.codeKey9 = (Button) view.findViewById(R.id.secure_code_keyboard_key_9);
        this.codeKey0 = (Button) view.findViewById(R.id.secure_code_keyboard_key_0);
        this.forgetCode = (TextView) view.findViewById(R.id.secure_code_forget);
        this.clearCode = (TextView) view.findViewById(R.id.secure_code_clear);

        this.infoText.setTypeface(CustomFonts.customContentRegular(inflater.getContext()));
        this.codeKey1.setTypeface(CustomFonts.customContentBold(inflater.getContext()));
        this.codeKey2.setTypeface(CustomFonts.customContentBold(inflater.getContext()));
        this.codeKey3.setTypeface(CustomFonts.customContentBold(inflater.getContext()));
        this.codeKey4.setTypeface(CustomFonts.customContentBold(inflater.getContext()));
        this.codeKey5.setTypeface(CustomFonts.customContentBold(inflater.getContext()));
        this.codeKey6.setTypeface(CustomFonts.customContentBold(inflater.getContext()));
        this.codeKey7.setTypeface(CustomFonts.customContentBold(inflater.getContext()));
        this.codeKey8.setTypeface(CustomFonts.customContentBold(inflater.getContext()));
        this.codeKey9.setTypeface(CustomFonts.customContentBold(inflater.getContext()));
        this.codeKey0.setTypeface(CustomFonts.customContentBold(inflater.getContext()));
        this.forgetCode.setTypeface(CustomFonts.customTitleLight(inflater.getContext()));
        this.clearCode.setTypeface(CustomFonts.customTitleLight(inflater.getContext()));

        this.codeKey0.setOnClickListener(keyClickListener);
        this.codeKey1.setOnClickListener(keyClickListener);
        this.codeKey2.setOnClickListener(keyClickListener);
        this.codeKey3.setOnClickListener(keyClickListener);
        this.codeKey4.setOnClickListener(keyClickListener);
        this.codeKey5.setOnClickListener(keyClickListener);
        this.codeKey6.setOnClickListener(keyClickListener);
        this.codeKey7.setOnClickListener(keyClickListener);
        this.codeKey8.setOnClickListener(keyClickListener);
        this.codeKey9.setOnClickListener(keyClickListener);

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
                if (delegate != null)
                    delegate.secureCodeValidated(false);
                if (parentActivity != null)
                    parentActivity.popMainFragment(R.animator.slide_down, android.R.animator.fade_in);
            }
        });

        return view;
    }

    View.OnClickListener keyClickListener = new View.OnClickListener() {
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

            if (FloozRestClient.getInstance().secureCodeIsValid(this.currentCode)) {
                if (delegate != null)
                    delegate.secureCodeValidated(true);
                if (parentActivity != null)
                    parentActivity.popMainFragment(R.animator.slide_down, android.R.animator.fade_in);

                this.currentCode = "";
                this.refreshCodeContainer();

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

    public interface SecureCodeDelegate {
        void secureCodeValidated(Boolean success);
    }
}