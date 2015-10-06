package me.flooz.app.Utils;

import android.view.View;
import android.view.ViewGroup;

/**
 * Created by Wapazz on 23/09/15.
 */
public class MarginHelper {
    public static void setMargins (View v, int l, int t, int r, int b) {
        if (v.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
            p.setMargins(l, t, r, b);
            v.requestLayout();
        }
    }
}
