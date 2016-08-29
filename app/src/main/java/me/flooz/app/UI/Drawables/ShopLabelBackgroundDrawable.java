package me.flooz.app.UI.Drawables;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;

import me.flooz.app.App.FloozApplication;
import me.flooz.app.R;

/**
 * Created by Flooz on 25/08/16.
 */
public class ShopLabelBackgroundDrawable extends Drawable {

    private Paint paint;

    public ShopLabelBackgroundDrawable() {
        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(FloozApplication.getAppContext().getResources().getColor(R.color.blue));
    }

    @Override
    public void draw(Canvas canvas) {
        int height = getBounds().height();
        int width = getBounds().width();

        Path path = new Path();

        path.moveTo(0, 0);
        path.lineTo(width, 0);
        path.lineTo(width, height);
        path.lineTo(0, height);
        path.lineTo(height / 2 - 5, height / 2);
        path.close();

        canvas.drawPath(path, paint);
    }

    @Override
    public void setAlpha(int alpha) {
        paint.setAlpha(alpha);
    }

    @Override
    public void setColorFilter(ColorFilter cf) {
        paint.setColorFilter(cf);
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }
}
