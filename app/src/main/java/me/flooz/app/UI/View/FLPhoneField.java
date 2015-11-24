package me.flooz.app.UI.View;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;

import me.flooz.app.Model.FLCountry;
import me.flooz.app.Model.FLError;
import me.flooz.app.Network.FloozHttpResponseHandler;
import me.flooz.app.Network.FloozRestClient;
import me.flooz.app.R;
import me.flooz.app.Utils.CustomFonts;

/**
 * Created by Flooz on 11/23/15.
 */
public class FLPhoneField extends LinearLayout {

    public interface FLPhoneFieldDelegate {
        void phoneChanged(String phone, FLCountry country);
        void phoneNext();
    }

    private Context context;

    private EditText textfield;
    private ImageView flagImageView;
    private TextView indicatifLabel;

    private FLCountry currentCountry;
    private String currentPhone;

    public FLPhoneFieldDelegate delegate;

    public FLPhoneField(Context context) {
        super(context);
        init(context);
    }

    public FLPhoneField(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {

        this.context = context;

        this.currentPhone = "";

        final String locale = context.getResources().getConfiguration().locale.getCountry();

        View view = View.inflate(context, R.layout.phone_textfield, this);

        this.textfield = (EditText) view.findViewById(R.id.phone_edit_text);
        this.flagImageView = (ImageView) view.findViewById(R.id.phone_flag);
        this.indicatifLabel = (TextView) view.findViewById(R.id.phone_indic);

        this.textfield.setTypeface(CustomFonts.customContentRegular(context));
        this.indicatifLabel.setTypeface(CustomFonts.customContentRegular(context));

        this.textfield.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                int size = 10;

                if (!s.toString().startsWith("0"))
                    size = 9;

                if (s.length() > size)
                    s.delete(size, s.length() - 1);

                currentPhone = s.toString();

                if (delegate != null) {
                    delegate.phoneChanged(currentPhone, currentCountry);


                    PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
                    try {
                        Phonenumber.PhoneNumber phoneNumber = phoneUtil.parse(currentPhone, currentCountry.code);
                        if (phoneUtil.isValidNumber(phoneNumber))
                            delegate.phoneNext();
                    } catch (NumberParseException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        LinearLayout countrySide = (LinearLayout) view.findViewById(R.id.phone_country_side);
        countrySide.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        if (FloozRestClient.getInstance().currentTexts != null) {
            this.setCountry(FLCountry.countryFromCode(locale));
        } else {
            FloozRestClient.getInstance().textObjectFromApi(new FloozHttpResponseHandler() {
                @Override
                public void success(Object response) {
                    setCountry(FLCountry.countryFromCode(locale));
                }

                @Override
                public void failure(int statusCode, FLError error) {
                    setCountry(FLCountry.countryFromCode(locale));
                }
            });
        }
    }

    public void setCountry(FLCountry country) {
        this.currentCountry = country;

        if (this.currentCountry == null)
            this.currentCountry = FLCountry.defaultCountry();

        if (delegate != null) {
            delegate.phoneChanged(currentPhone, currentCountry);
        }

        this.flagImageView.setImageDrawable(context.getResources().getDrawable(this.currentCountry.imageID));
        this.indicatifLabel.setText(this.currentCountry.indicatif);
    }

    public void setPhoneNumber(String phone) {

    }
}