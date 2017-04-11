package me.flooz.app.UI.View.FLShopFields;

import android.content.Context;
import android.support.annotation.NonNull;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.View;

import net.qiujuer.genius.ui.widget.EditText;

import org.json.JSONObject;

import me.flooz.app.Model.FLUser;
import me.flooz.app.R;
import me.flooz.app.Utils.CustomFonts;

/**
 * Created by Flooz on 05/09/16.
 */
public class FLShopTextField extends FLShopField {

    private enum  FLTextFieldType {
        FLTextFieldTypeDate,
        FLTextFieldTypeNumber,
        FLTextFieldTypeFloatNumber,
        FLTextFieldTypeText,
        FLTextFieldTypePassword,
        FLTextFieldTypeEmail,
        FLTextFieldTypeURL
    }

    public EditText editText;

    private int maxLenght = -1;
    private FLTextFieldType type = FLTextFieldType.FLTextFieldTypeText;


    public FLShopTextField(Context context, @NonNull final JSONObject params, @NonNull final FLShopFieldDelegate delegate) {
        super(context, params, delegate);

        View view = View.inflate(context, R.layout.shop_param_text_field, this);

        this.editText = (EditText) view.findViewById(R.id.shop_param_text_text);


        String typeData = this.params.optString("type").replace("textfield:", "");

        switch (typeData) {
            case "date":
                this.type = FLTextFieldType.FLTextFieldTypeDate;
                this.editText.setInputType(InputType.TYPE_CLASS_DATETIME | InputType.TYPE_DATETIME_VARIATION_DATE);
                break;
            case "integer":
                this.type = FLTextFieldType.FLTextFieldTypeNumber;
                this.editText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_NORMAL);
                break;
            case "float":
                this.type = FLTextFieldType.FLTextFieldTypeFloatNumber;
                this.editText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
                break;
            case "text":
                this.type = FLTextFieldType.FLTextFieldTypeText;
                this.editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS | InputType.TYPE_TEXT_VARIATION_NORMAL);
                break;
            case "password":
                this.type = FLTextFieldType.FLTextFieldTypePassword;
                this.editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                break;
            case "email":
                this.type = FLTextFieldType.FLTextFieldTypeEmail;
                this.editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
                break;
            case "url":
                this.type = FLTextFieldType.FLTextFieldTypeURL;
                this.editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS | InputType.TYPE_TEXT_VARIATION_URI);
                break;
        }

        this.editText.setTypeface(CustomFonts.customContentRegular(this.context));

        this.editText.setHint(this.params.optString("placeholder"));

        if (this.params.has("default")) {
            this.editText.setText(this.params.optString("default"));
            this.delegate.valueChanged(params.optString("name"), this.params.optString("default"));
        }

        if (this.params.has("maxLenght")) {
            this.maxLenght = this.params.optInt("maxLenght");

            InputFilter[] filterArray = new InputFilter[1];
            filterArray[0] = new InputFilter.LengthFilter(this.maxLenght);
            editText.setFilters(filterArray);
        }

        this.editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (FLShopTextField.this.type == FLTextFieldType.FLTextFieldTypeDate) {
                    if (editable.length() == 3 && editable.charAt(2) != '/')
                        editable.insert(2, "/");
                    if (editable.length() == 6 && editable.charAt(5) != '/')
                        editable.insert(5, "/");

                    if (editable.length() == 1 && editable.charAt(0) > '3')
                        editable.insert(0, "0");

                    if (editable.length() == 4 && editable.charAt(3) > '1')
                        editable.insert(3, "0");

                    if (editable.length() == 2 && editable.charAt(0) == '3' && editable.charAt(1) > '1')
                        editable.delete(1, 2);

                    if (editable.length() == 5 && editable.charAt(3) == '1' && editable.charAt(4) > '2')
                        editable.delete(4, 5);

                    if (editable.length() == 7 && editable.charAt(6) > '2') {
                        InputFilter[] filterArray = new InputFilter[1];
                        filterArray[0] = new InputFilter.LengthFilter(8);
                        editText.setFilters(filterArray);
                    }
                    else if (editable.length() == 7) {
                        InputFilter[] filterArray = new InputFilter[1];
                        filterArray[0] = new InputFilter.LengthFilter(10);
                        editText.setFilters(filterArray);
                    }

                    delegate.valueChanged(params.optString("name"), FLUser.formattedBirthdate(editable.toString()));

                } else {
                    delegate.valueChanged(params.optString("name"), editable.toString());
                }
            }
        });
    }
}
