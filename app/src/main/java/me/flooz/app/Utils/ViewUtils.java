package me.flooz.app.Utils;

/**
 * Created by Flooz on 9/2/14.
 */

import android.os.Build;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;

public class ViewUtils
{
    public static boolean isEventInsideView(View v, MotionEvent event)
    {
        return (event.getX() > 0 && event.getX() < v.getWidth() && event.getY() > 0
                && event.getY() < v.getHeight());
    }

    public static void removeGlobalLayoutListener(View view, ViewTreeObserver.OnGlobalLayoutListener listener) {
        if (Build.VERSION.SDK_INT >= 16)
            view.getViewTreeObserver().removeOnGlobalLayoutListener(listener);
        else
            view.getViewTreeObserver().removeGlobalOnLayoutListener(listener);
    }
}
