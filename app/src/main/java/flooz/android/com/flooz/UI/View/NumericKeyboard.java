package flooz.android.com.flooz.UI.View;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TableLayout;

import flooz.android.com.flooz.R;
import flooz.android.com.flooz.Utils.CustomFonts;

/**
 * Created by Flooz on 9/22/14.
 */
public class NumericKeyboard extends TableLayout {

    private Button key1;
    private Button key2;
    private Button key3;
    private Button key4;
    private Button key5;
    private Button key6;
    private Button key7;
    private Button key8;
    private Button key9;
    private Button key0;
    private ImageButton keyClear;

    public String value = "";

    public NumericKeyboardDelegate delegate = null;

    public int maxLenght = 0;

    public interface NumericKeyboardDelegate {
        public void keyPressed();
    }

    public NumericKeyboard(Context context) {
        super(context);
        init(context);
    }

    public NumericKeyboard(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        View view = View.inflate(context, R.layout.numeric_keyboard, this);

        key1 = (Button) view.findViewById(R.id.numeric_keyboard_key_1);
        key2 = (Button) view.findViewById(R.id.numeric_keyboard_key_2);
        key3 = (Button) view.findViewById(R.id.numeric_keyboard_key_3);
        key4 = (Button) view.findViewById(R.id.numeric_keyboard_key_4);
        key5 = (Button) view.findViewById(R.id.numeric_keyboard_key_5);
        key6 = (Button) view.findViewById(R.id.numeric_keyboard_key_6);
        key7 = (Button) view.findViewById(R.id.numeric_keyboard_key_7);
        key8 = (Button) view.findViewById(R.id.numeric_keyboard_key_8);
        key9 = (Button) view.findViewById(R.id.numeric_keyboard_key_9);
        key0 = (Button) view.findViewById(R.id.numeric_keyboard_key_0);
        keyClear = (ImageButton) view.findViewById(R.id.numeric_keyboard_key_backspace);

        key1.setTypeface(CustomFonts.customContentLight(context));
        key2.setTypeface(CustomFonts.customContentLight(context));
        key3.setTypeface(CustomFonts.customContentLight(context));
        key4.setTypeface(CustomFonts.customContentLight(context));
        key5.setTypeface(CustomFonts.customContentLight(context));
        key6.setTypeface(CustomFonts.customContentLight(context));
        key7.setTypeface(CustomFonts.customContentLight(context));
        key8.setTypeface(CustomFonts.customContentLight(context));
        key9.setTypeface(CustomFonts.customContentLight(context));
        key0.setTypeface(CustomFonts.customContentLight(context));

        key1.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (maxLenght == 0 || value.length() < maxLenght) {
                    value += "1";
                    if (delegate != null)
                        delegate.keyPressed();
                }
            }
        });

        key2.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (maxLenght == 0 || value.length() < maxLenght) {
                    value += "2";
                    if (delegate != null)
                        delegate.keyPressed();
                }
            }
        });

        key3.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view){
                if (maxLenght == 0 || value.length() < maxLenght) {
                    value += "3";
                    if (delegate != null)
                        delegate.keyPressed();
                }
            }
        });

        key4.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (maxLenght == 0 || value.length() < maxLenght) {
                    value += "4";
                    if (delegate != null)
                        delegate.keyPressed();
                }
            }
        });

        key5.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view){
                if (maxLenght == 0 || value.length() < maxLenght) {
                    value += "5";
                    if (delegate != null)
                        delegate.keyPressed();
                }
            }
        });

        key6.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (maxLenght == 0 || value.length() < maxLenght) {
                    value += "6";
                    if (delegate != null)
                        delegate.keyPressed();
                }
            }
        });

        key7.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view){
                if (maxLenght == 0 || value.length() < maxLenght) {
                    value += "7";
                    if (delegate != null)
                        delegate.keyPressed();
                }
            }
        });

        key8.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (maxLenght == 0 || value.length() < maxLenght) {
                    value += "8";
                    if (delegate != null)
                        delegate.keyPressed();
                }
            }
        });

        key9.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view){
                if (maxLenght == 0 || value.length() < maxLenght) {
                    value += "9";
                    if (delegate != null)
                        delegate.keyPressed();
                }
            }
        });

        key0.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (maxLenght == 0 || value.length() < maxLenght) {
                    value += "0";
                    if (delegate != null)
                        delegate.keyPressed();
                }
            }
        });

        keyClear.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (value.length() > 0) {
                    value = value.substring(0, value.length() - 1);
                    if (delegate != null)
                        delegate.keyPressed();
                }
            }
        });
    }
}