package me.flooz.app.UI.View;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

/**
 * Created by Flooz on 14/06/16.
 */
public class SlidableLinearLayout extends LinearLayout {

    private static final String TAG = SlidableLinearLayout.class.getName();

    public SlidableLinearLayout(Context context) {
        super(context);
    }

    public SlidableLinearLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SlidableLinearLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public float getXFraction() {
        return getX() / getWidth();
    }

    public void setXFraction(float xFraction) {
        final int width = getWidth();
        setX((width > 0) ? (xFraction * width) : -9999);
    }

    public float getYFraction() {
        return getY() / getHeight();
    }

    public void setYFraction(float yFraction) {
        final int height = getHeight();
        setX((height > 0) ? (yFraction * height) : -9999);
    }

}
