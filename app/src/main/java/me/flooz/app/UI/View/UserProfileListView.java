package me.flooz.app.UI.View;

import android.content.Context;
import android.graphics.Canvas;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;
import android.widget.ListView;

import me.flooz.app.R;

/**
 * Created by Flooz on 10/19/15.
 */
public class UserProfileListView extends ListView implements AbsListView.OnScrollListener {

    public interface OnUserProfileListViewListener {
        void onShowLastItem();
    }

    private OnScrollListener onScrollListener = null;

    private OnUserProfileListViewListener onUserProfileListViewListener;
    public int preLast;

    public UserProfileListView(Context context) {
        super(context);

        super.setOnScrollListener(this);
    }

    public UserProfileListView(Context context, AttributeSet attrs) {
        super(context, attrs);

        super.setOnScrollListener(this);
    }

    public UserProfileListView(Context context, AttributeSet attrs, int defStyle) {
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

        if (null != onUserProfileListViewListener) {
            final int lastItem = firstVisibleItem + visibleItemCount;
            if (lastItem == totalItemCount) {
                if (preLast != lastItem) {
                    this.onUserProfileListViewListener.onShowLastItem();
                    preLast = lastItem;
                }
            }
        }

        if (onScrollListener != null) {
            onScrollListener.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
        }
    }

    public void setOnUserProfileListViewListener(OnUserProfileListViewListener onUserProfileListViewListener) {
        this.onUserProfileListViewListener = onUserProfileListViewListener;
    }

    @Override
    public void setOnScrollListener(OnScrollListener onScrollListener) {
        this.onScrollListener = onScrollListener;
    }
}
