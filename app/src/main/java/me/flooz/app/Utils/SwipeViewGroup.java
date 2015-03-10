package me.flooz.app.Utils;

import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.LinearLayout;

/**
 * Created by Flooz on 9/22/14.
 */
public class SwipeViewGroup extends LinearLayout {

    public interface SwipeEvents {
        public void onSwipeRight();
        public void onSwipeLeft();
        public void onSwipeTop();
        public void onSwipeBottom();
    }

    public GestureDetector gestureDetector;

    private SwipeEvents eventHandler;

    public void setSwipeHandler(SwipeEvents handler) {
        this.eventHandler = handler;
    }

    public SwipeViewGroup(Context context) {
        super(context);
        this.init(context);
    }

    public SwipeViewGroup(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.init(context);
    }

    public SwipeViewGroup(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.init(context);
    }

    private void init(Context context)
    {
        if (!this.isInEditMode()) {
            this.gestureDetector = new GestureDetector(context, new GestureListener() {
                @Override
                public void onSwipeRight() {
                    if (eventHandler != null)
                        eventHandler.onSwipeRight();
                }

                @Override
                public void onSwipeLeft() {
                    if (eventHandler != null)
                        eventHandler.onSwipeLeft();
                }

                @Override
                public void onSwipeBottom() {
                    if (eventHandler != null)
                        eventHandler.onSwipeBottom();
                }

                @Override
                public void onSwipeTop() {
                    if (eventHandler != null)
                        eventHandler.onSwipeTop();
                }
            });
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        this.gestureDetector.onTouchEvent(ev);
        return false;
    }
}
