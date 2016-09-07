package me.flooz.app.UI.View.FLShopFields;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.RelativeLayout;

import org.json.JSONObject;


/**
 * Created by Flooz on 05/09/16.
 */
public class FLShopField extends RelativeLayout {

    public interface FLShopFieldDelegate {
        void valueChanged(String key, Object value);
    }

    protected Context context;
    protected JSONObject params;
    protected FLShopFieldDelegate delegate;


    public FLShopField(Context context, @NonNull JSONObject params, @NonNull FLShopFieldDelegate delegate) {
        super(context);

        this.context = context;
        this.params = params;
        this.delegate = delegate;
    }



}
