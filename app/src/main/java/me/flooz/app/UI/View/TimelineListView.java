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
        void onShowLastItem();
    }

    private OnScrollListener onScrollListener = null;

    private OnTimelineListViewListener onTimelineListViewListener;
    private int lastPosition = -1;
    private int preLast;

     public TimelineListView(Context context) {
        this(context, null);
    }

    public TimelineListView(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.attr.listViewStyle);
    }

    public TimelineListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        super.setOnScrollListener(this);
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

    }

    public void setOnTimelineListViewListener(OnTimelineListViewListener onTimelineListViewListener) {
        this.onTimelineListViewListener = onTimelineListViewListener;
    }

    @Override
    public void setOnScrollListener(OnScrollListener onScrollListener) {
        this.onScrollListener = onScrollListener;
    }
}