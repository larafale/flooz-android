package me.flooz.app.UI.View.FLShopFields;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.TextView;

import com.gc.materialdesign.views.Switch;

import org.json.JSONObject;

import me.flooz.app.R;
import me.flooz.app.Utils.CustomFonts;

/**
 * Created by Flooz on 05/09/16.
 */
public class FLShopSwitchField extends FLShopField {

    private TextView titleView;
    private Switch switchView;

    public FLShopSwitchField(Context context, @NonNull JSONObject params, @NonNull FLShopFieldDelegate delegate) {
        super(context, params, delegate);

        View view = View.inflate(this.context, R.layout.shop_param_switch_field, this);

        this.titleView = (TextView) view.findViewById(R.id.shop_param_switch_title);
        this.switchView = (Switch) view.findViewById(R.id.shop_param_switch_switch);

        this.titleView.setTypeface(CustomFonts.customContentRegular(this.context));

        this.titleView.setText(this.params.optString("title"));

        if (this.params.has("default")) {
            this.switchView.setChecked(this.params.optBoolean("default"));
            this.delegate.valueChanged(this.params.optString("name"), this.params.optBoolean("default"));
        } else {
            this.switchView.setChecked(false);
            this.delegate.valueChanged(this.params.optString("name"), false);
        }

        this.switchView.setOncheckListener(new Switch.OnCheckListener() {
            @Override
            public void onCheck(Switch view, boolean check) {
                FLShopSwitchField.this.delegate.valueChanged(FLShopSwitchField.this.params.optString("name"), check);
            }
        });
    }
}
