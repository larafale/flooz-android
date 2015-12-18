package me.flooz.app.UI.View;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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

import me.flooz.app.Adapter.CountryListAdapter;
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
    private LinearLayout countrySide;

    public FLCountry currentCountry;
    public String currentPhone;

    public FLPhoneFieldDelegate delegate;

    private CountryListAdapter countryAdapter;

    public FLPhoneField(Context context) {
        super(context);
        init(context);
    }

    public FLPhoneField(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(final Context context) {

        this.context = context;

        this.currentPhone = "";

        this.countryAdapter = new CountryListAdapter(context);

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
                int size = currentCountry.maxLength;

                if (s.toString().startsWith("0"))
                    size += 1;

                if (s.length() > size)
                    s.delete(size, s.length());

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

        this.countrySide = (LinearLayout) view.findViewById(R.id.phone_country_side);
        this.countrySide.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setAdapter(countryAdapter, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        setCountry(countryAdapter.getItem(which));
                    }
                });
                builder.setCancelable(true);
                builder.setNeutralButton(context.getResources().getString(R.string.GLOBAL_CANCEL), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builder.setTitle(context.getResources().getString(R.string.COUNTRY_PICKER_TITLE));
                builder.show();
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
        currentPhone = phone;

        if (delegate != null) {
            delegate.phoneChanged(currentPhone, currentCountry);
        }

        this.textfield.setText(currentPhone);
    }

    public void setEnable(Boolean enable) {
        this.textfield.setFocusable(enable);
        this.textfield.setFocusableInTouchMode(enable);
        this.textfield.setCursorVisible(enable);
        this.textfield.setClickable(enable);
        this.countrySide.setClickable(enable);
    }
}