package me.flooz.app.UI.View;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

/**
 * Created by Flooz on 14/06/16.
 */
public class SlidableRelativeLayout extends RelativeLayout {

    private static final String TAG = SlidableRelativeLayout.class.getName();

    public SlidableRelativeLayout(Context context) {
        super(context);
    }

    public SlidableRelativeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SlidableRelativeLayout(Context context, AttributeSet attrs, int defStyleAttr) {
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
