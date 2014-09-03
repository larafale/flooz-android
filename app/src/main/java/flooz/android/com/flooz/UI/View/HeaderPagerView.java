package flooz.android.com.flooz.UI.View;

/**
 * Created by Flooz on 9/2/14.
 */

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.TextView;
import android.graphics.Typeface;

import flooz.android.com.flooz.R;
import flooz.android.com.flooz.Utils.CustomFonts;

public class HeaderPagerView extends HorizontalScrollView
{
    private Delegate delegate;

    private View leftView;
    private View rightView;
    private TextView floozTextView;
    private TextView profileTextView;
    private TextView friendsTextView;

    public HeaderPagerView(Context context)
    {
        super(context);
        init(context);
    }

    public HeaderPagerView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        init(context);
    }

    public HeaderPagerView(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context)
    {
        View view = View.inflate(context, R.layout.header_pager_view, this);

        this.profileTextView = (TextView) view.findViewById(R.id.profile);
        this.floozTextView = (TextView) view.findViewById(R.id.flooz);
        this.friendsTextView = (TextView) view.findViewById(R.id.friends);
        this.leftView = view.findViewById(R.id.left_view);
        this.rightView = view.findViewById(R.id.right_view);

        this.floozTextView.setTypeface(CustomFonts.customTitleExtraLight(context));
        this.friendsTextView.setTypeface(CustomFonts.customTitleExtraLight(context));
        this.profileTextView.setTypeface(CustomFonts.customTitleExtraLight(context));

        this.setHorizontalScrollBarEnabled(false);

        this.floozTextView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                delegate.onHeaderButtonClick(1);
            }
        });

        OnClickListener profileClickListener = new OnClickListener() {
            @Override
            public void onClick(View view) {
                delegate.onHeaderButtonClick(0);
            }
        };

        this.leftView.setOnClickListener(profileClickListener);
        this.profileTextView.setOnClickListener(profileClickListener);

        OnClickListener friendsClickListener = new OnClickListener() {
            @Override
            public void onClick(View view) {
                delegate.onHeaderButtonClick(2);
            }
        };
        this.rightView.setOnClickListener(friendsClickListener);
        this.friendsTextView.setOnClickListener(friendsClickListener);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        return false;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return false;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int width = MeasureSpec.getSize(widthMeasureSpec);
        int part = width / 3;

        this.leftView.getLayoutParams().width = part;
        this.profileTextView.getLayoutParams().width = part;
        this.floozTextView.getLayoutParams().width = part * 2;
        this.friendsTextView.getLayoutParams().width = part;
        this.rightView.getLayoutParams().width = part;
    }

    private void scrollToPage(int page, boolean animated)
    {
        int x = (this.leftView.getLayoutParams().width * 2) * page;
        if (page > 0)
            x -= this.leftView.getLayoutParams().width / (3 - page);

        if (animated)
            this.smoothScrollTo(x, 0);
        else
            this.setScrollX(x);
    }

    public void setInitialScroll() {
        this.scrollToPage(1, false);
    }

    public void setDelegate(Delegate _delegate) {
        this.delegate = _delegate;
    }

    public interface Delegate
    {
        public void onHeaderButtonClick(int buttonId);
    }
}
