package me.flooz.app.UI.View;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import me.flooz.app.Model.FLUser;
import me.flooz.app.R;
import me.flooz.app.UI.View.TokenPickerLibrary.TokenCompleteTextView;
import me.flooz.app.Utils.CustomFonts;

/**
 * Created by Flooz on 3/13/15.
 */
public class ContactPickerView extends TokenCompleteTextView {
    public ContactPickerView(Context context, AttributeSet attrs) {
        super(context, attrs);

    }

    @Override
    protected View getViewForObject(Object object) {
        if (object != null) {
            LayoutInflater l = (LayoutInflater) getContext().getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            LinearLayout view = (LinearLayout) l.inflate(R.layout.contact_picker_contact_view, (ViewGroup) ContactPickerView.this.getParent(), false);

            ((TextView) view.findViewById(R.id.name)).setTypeface(CustomFonts.customContentRegular(getContext()));
            ((TextView) view.findViewById(R.id.name)).setText(object.toString());

            return view;
        }
        return null;
    }

    @Override
    protected Object defaultObject(String completionText) {
        return null;
    }
}
