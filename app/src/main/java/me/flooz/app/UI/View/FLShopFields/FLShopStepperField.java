package me.flooz.app.UI.View.FLShopFields;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONObject;

import me.flooz.app.R;
import me.flooz.app.Utils.CustomFonts;

/**
 * Created by Flooz on 05/09/16.
 */
public class FLShopStepperField extends FLShopField {

    private TextView titleView;
    private TextView valueLabel;
    private Button minusButton;
    private Button plusButton;

    private int minValue = 0;
    private int maxValue = -1;
    private int value = 0;

    public FLShopStepperField(Context context, @NonNull JSONObject params, @NonNull FLShopFieldDelegate delegate) {
        super(context, params, delegate);

        View view = View.inflate(context, R.layout.shop_param_stepper_field, this);

        this.titleView = (TextView) view.findViewById(R.id.shop_param_stepper_title);
        this.valueLabel = (TextView) view.findViewById(R.id.shop_param_stepper_value);
        this.minusButton = (Button) view.findViewById(R.id.shop_param_stepper_minus);
        this.plusButton = (Button) view.findViewById(R.id.shop_param_stepper_plus);

        this.titleView.setTypeface(CustomFonts.customContentRegular(this.context));
        this.valueLabel.setTypeface(CustomFonts.customContentBold(this.context));

        this.minusButton.setTypeface(CustomFonts.customContentRegular(this.context));
        this.plusButton.setTypeface(CustomFonts.customContentRegular(this.context));

        this.titleView.setText(this.params.optString("title"));

        if (this.params.has("min"))
            this.minValue = this.params.optInt("min");

        if (this.params.has("max"))
            this.maxValue = this.params.optInt("max");

        if (this.params.has("default"))
            this.value = this.params.optInt("default");

        this.minusButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                --value;
                updateView();
            }
        });

        this.plusButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                ++value;
                updateView();
            }
        });

        updateView();
    }

    private void updateView() {
        if (value == minValue)
            this.minusButton.setEnabled(false);
        else
            this.minusButton.setEnabled(true);

        if (maxValue > 0 && value == maxValue)
            this.plusButton.setEnabled(false);
        else
            this.plusButton.setEnabled(true);

        this.valueLabel.setText("" + value);

        this.delegate.valueChanged(this.params.optString("name"), this.value);
    }
}
