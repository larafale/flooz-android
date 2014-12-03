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

    private View rightView;
    private TextView homeTextView;
    private TextView publicTextView;
    private TextView privateTextView;

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

    private void init(Context context) {
        View view = View.inflate(context, R.layout.header_pager_view, this);

        this.homeTextView = (TextView) view.findViewById(R.id.header_home);
        this.publicTextView = (TextView) view.findViewById(R.id.header_public);
        this.privateTextView = (TextView) view.findViewById(R.id.header_private);
        this.rightView = view.findViewById(R.id.right_view);

        this.homeTextView.setTypeface(CustomFonts.customTitleExtraLight(context));
        this.publicTextView.setTypeface(CustomFonts.customTitleExtraLight(context));
        this.privateTextView.setTypeface(CustomFonts.customTitleExtraLight(context));

        this.setHorizontalScrollBarEnabled(false);
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

        final float scale = getContext().getResources().getDisplayMetrics().density;
        int pixels = (int) (width * scale);

        int offset = (int)(12 * scale);

        this.homeTextView.getLayoutParams().width = pixels + offset;
        this.publicTextView.getLayoutParams().width = pixels / 2 - offset;
        this.privateTextView.getLayoutParams().width = pixels + offset;
        this.rightView.getLayoutParams().width = pixels;
    }

    public void setDelegate(Delegate _delegate) {
        this.delegate = _delegate;
    }

    public interface Delegate
    {
        public void onHeaderButtonClick(int buttonId);
    }
}
