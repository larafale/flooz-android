package flooz.android.com.flooz.UI.View;

/**
 * Created by Flooz on 9/2/14.
 */

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.LinearLayout;

import flooz.android.com.flooz.R;
import flooz.android.com.flooz.Utils.ViewUtils;

public class FloozFilterTabItem extends LinearLayout
{
    private ImageView iconImageView;
    private TextView textView;
    private View selectedBarView;
    private Delegate delegate;
    private TabType tabType;

    public FloozFilterTabItem(Context context)
    {
        super(context);
        init(context);
    }

    public FloozFilterTabItem(Context context, AttributeSet attrs)
    {
        super(context, attrs);

        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.FloozFilterTabItem, 0, 0);
        int tabType;

        try
        {
            tabType = a.getInteger(R.styleable.FloozFilterTabItem_tab_type, 0);
        }
        finally
        {
            a.recycle();
        }

        switch (tabType)
        {
            case 0:
                this.tabType = TabType.PUBLIC;
                break;
            case 1:
                this.tabType = TabType.PRIVATE;
                break;
            case 2:
                this.tabType = TabType.FRIENDS;
                break;
        }

        init(context);
    }

    public FloozFilterTabItem(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context)
    {
        View view = View.inflate(context, R.layout.flooz_filter_tab_item, this);
        this.iconImageView = (ImageView) view.findViewById(R.id.tab_icon);
        this.textView = (TextView) view.findViewById(R.id.tab_text);
        this.selectedBarView = view.findViewById(R.id.tab_selected_bar);

        int iconId = 0;
        int titleId = 0;
        switch (this.tabType)
        {
            case PUBLIC:
                iconId = R.drawable.scope_public;
                titleId = R.string.scope_public_title;
                break;
            case PRIVATE:
                iconId = R.drawable.scope_private;
                titleId = R.string.scope_private_title;
                break;
            case FRIENDS:
                iconId = R.drawable.scope_friend;
                titleId = R.string.scope_friends_title;
                break;
        }

        this.iconImageView.setImageDrawable(getResources().getDrawable(iconId));
        this.textView.setText(titleId);

        setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:

                        break;
                    case MotionEvent.ACTION_MOVE:
                        if (ViewUtils.isEventInsideView(FloozFilterTabItem.this, motionEvent)) {

                        } else {

                        }
                        break;
                    case MotionEvent.ACTION_UP:
                        if (ViewUtils.isEventInsideView(FloozFilterTabItem.this, motionEvent)) {
                            select();
                            delegate.tabElementViewClick(tabType);
                        } else {

                        }
                        break;
                    case MotionEvent.ACTION_CANCEL:

                        break;
                }

                return true;
            }
        });
    }

    public void select()
    {
        textView.setTextColor(Color.WHITE);
        selectedBarView.setVisibility(View.VISIBLE);
    }

    public void unselect()
    {
        textView.setTextColor(getResources().getColor(R.color.blue_light));
        this.selectedBarView.setVisibility(View.INVISIBLE);
    }

    public void setDelegate(Delegate _delegate) {
        this.delegate = _delegate;
    }

    public enum TabType
    {
        PRIVATE, PUBLIC, FRIENDS
    }

    public interface Delegate
    {
        public void tabElementViewClick(TabType tabType);
    }
}
