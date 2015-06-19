package me.flooz.app.UI.View;

import android.content.Context;
import android.graphics.Canvas;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ListView;

import me.flooz.app.R;

/**
 * Created by Flooz on 12/10/14.
 */
public class TimelineListView extends ListView implements OnScrollListener {

    public interface OnTimelineListViewListener {

        void onPositionChanged(TimelineListView listView, int position, View scrollBarPanel);
        void onShowLastItem();

    }

    private OnScrollListener onScrollListener = null;

    private View scrollBarPanel = null;
    private int scrollBarPanelPosition = 0;

    private OnTimelineListViewListener onTimelineListViewListener;
    private int lastPosition = -1;
    private int preLast;

    private Animation inAnimation = null;
    private Animation outAnimation = null;

    private final Handler handler = new Handler();

    private final Runnable scrollBarPanelFadeRunnable = () -> {
        if (outAnimation != null) {
            scrollBarPanel.startAnimation(outAnimation);
        }
    };

    private int widthMeasureSpec;
    private int heightMeasureSpec;

    public TimelineListView(Context context) {
        this(context, null);
    }

    public TimelineListView(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.attr.listViewStyle);
    }

    public TimelineListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        super.setOnScrollListener(this);

        final int scrollBarPanelLayoutId = R.layout.timeline_scrollbar_panel;
        final int scrollBarPanelInAnimation = R.anim.scrollbar_panel_anim_in;
        final int scrollBarPanelOutAnimation = R.anim.scrollbar_panel_anim_out;

        setScrollBarPanel(scrollBarPanelLayoutId);

        final int scrollBarPanelFadeDuration = ViewConfiguration.getScrollBarFadeDuration();

        inAnimation = AnimationUtils.loadAnimation(getContext(), scrollBarPanelInAnimation);

        outAnimation = AnimationUtils.loadAnimation(getContext(), scrollBarPanelOutAnimation);
        outAnimation.setDuration(scrollBarPanelFadeDuration);

        outAnimation.setAnimationListener(new AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                if (scrollBarPanel != null) {
                    scrollBarPanel.setVisibility(View.GONE);
                }
            }
        });
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        if (onScrollListener != null) {
            onScrollListener.onScrollStateChanged(view, scrollState);
        }
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

        if (null != onTimelineListViewListener) {
            final int lastItem = firstVisibleItem + visibleItemCount;
            if (lastItem == totalItemCount) {
                if (preLast != lastItem) {
                    this.onTimelineListViewListener.onShowLastItem();
                    preLast = lastItem;
                }
            }
        }

        if (null != onTimelineListViewListener && null != scrollBarPanel) {
            if (totalItemCount > 0) {
                final int thickness = getVerticalScrollbarWidth();
                int height = Math.round((float) getMeasuredHeight() * computeVerticalScrollExtent() / computeVerticalScrollRange());
                int thumbOffset = Math.round((float) (getMeasuredHeight() - height) * computeVerticalScrollOffset() / (computeVerticalScrollRange() - computeVerticalScrollExtent()));
                final int minLength = thickness * 2;
                if (height < minLength) {
                    height = minLength;
                }
                thumbOffset += height / 2;

                final int count = getChildCount();
                for (int i = 0; i < count; ++i) {
                    final View childView = getChildAt(i);
                    if (childView != null) {
                        if (thumbOffset > childView.getTop() && thumbOffset < childView.getBottom()) {
                            if (lastPosition != firstVisibleItem + i) {
                                lastPosition = firstVisibleItem + i;
                                onTimelineListViewListener.onPositionChanged(this, lastPosition, scrollBarPanel);
                                measureChild(scrollBarPanel, widthMeasureSpec, heightMeasureSpec);
                            }
                            break;
                        }
                    }
                }

                scrollBarPanelPosition = thumbOffset - scrollBarPanel.getMeasuredHeight() / 2;
                final int x = getMeasuredWidth() - scrollBarPanel.getMeasuredWidth() - getVerticalScrollbarWidth();
                scrollBarPanel.layout(x, scrollBarPanelPosition, x + scrollBarPanel.getMeasuredWidth(),
                        scrollBarPanelPosition + scrollBarPanel.getMeasuredHeight());
            }
        }

        if (onScrollListener != null) {
            onScrollListener.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
        }
    }

    public void setOnTimelineListViewListener(OnTimelineListViewListener onTimelineListViewListener) {
        this.onTimelineListViewListener = onTimelineListViewListener;
    }

    @Override
    public void setOnScrollListener(OnScrollListener onScrollListener) {
        this.onScrollListener = onScrollListener;
    }

    public void setScrollBarPanel(View scrollBarPanel) {
        this.scrollBarPanel = scrollBarPanel;
        this.scrollBarPanel.setVisibility(View.GONE);
        requestLayout();
    }

    public void setScrollBarPanel(int resId) {
        setScrollBarPanel(LayoutInflater.from(getContext()).inflate(resId, this, false));
    }

    public View getScrollBarPanel() {
        return scrollBarPanel;
    }

    @Override
    protected boolean awakenScrollBars(int startDelay, boolean invalidate) {
        final boolean isAnimationPlayed = super.awakenScrollBars(startDelay, invalidate);

        if (isAnimationPlayed == true && scrollBarPanel != null && getAdapter().getCount() > 0) {
            if (scrollBarPanel.getVisibility() == View.GONE) {
                scrollBarPanel.setVisibility(View.VISIBLE);
                if (inAnimation != null) {
                    scrollBarPanel.startAnimation(inAnimation);
                }
            }

            handler.removeCallbacks(scrollBarPanelFadeRunnable);
            handler.postAtTime(scrollBarPanelFadeRunnable, AnimationUtils.currentAnimationTimeMillis() + startDelay);
        }

        return isAnimationPlayed;
    }

    @Override
    protected void onMeasure(int _widthMeasureSpec, int _heightMeasureSpec) {
        super.onMeasure(_widthMeasureSpec, _heightMeasureSpec);

        if (scrollBarPanel != null && getAdapter() != null) {
            this.widthMeasureSpec = _widthMeasureSpec;
            this.heightMeasureSpec = _heightMeasureSpec;
            measureChild(scrollBarPanel, _widthMeasureSpec, _widthMeasureSpec);
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        if (scrollBarPanel != null) {
            final int x = getMeasuredWidth() - scrollBarPanel.getMeasuredWidth() - getVerticalScrollbarWidth();
            scrollBarPanel.layout(x, scrollBarPanelPosition, x + scrollBarPanel.getMeasuredWidth(),
                    scrollBarPanelPosition + scrollBarPanel.getMeasuredHeight());
        }
    }

    @Override
    protected void dispatchDraw(@NonNull Canvas canvas) {
        super.dispatchDraw(canvas);

        if (scrollBarPanel != null && scrollBarPanel.getVisibility() == View.VISIBLE) {
            drawChild(canvas, scrollBarPanel, getDrawingTime());
        }
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        handler.removeCallbacks(scrollBarPanelFadeRunnable);
    }
}