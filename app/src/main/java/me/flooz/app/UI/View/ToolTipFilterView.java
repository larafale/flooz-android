package me.flooz.app.UI.View;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import me.flooz.app.Model.FLScope;
import me.flooz.app.Model.FLTransaction;
import me.flooz.app.R;
import me.flooz.app.Utils.CustomFonts;

/**
 * Created by Flooz on 8/24/15.
 */
public class ToolTipFilterView {
    public View view;

    private ImageView allCheck;
    private ImageView friendCheck;
    private ImageView privateCheck;

    public ToolTipFilterViewDelegate delegate;

    public ToolTipFilterView(Context context) {

        this.view = LayoutInflater.from(context).inflate(R.layout.timeline_filter_tooltip, null);

        RelativeLayout allLayout = (RelativeLayout) this.view.findViewById(R.id.tooltip_filter_all);
        RelativeLayout friendLayout = (RelativeLayout) this.view.findViewById(R.id.tooltip_filter_friends);
        RelativeLayout privateLayout = (RelativeLayout) this.view.findViewById(R.id.tooltip_filter_private);

        TextView title = (TextView) this.view.findViewById(R.id.tooltip_filter_title);
        TextView allText = (TextView) this.view.findViewById(R.id.tooltip_filter_all_text);
        TextView friendText = (TextView) this.view.findViewById(R.id.tooltip_filter_friends_text);
        TextView privateText = (TextView) this.view.findViewById(R.id.tooltip_filter_private_text);

        this.allCheck = (ImageView) this.view.findViewById(R.id.tooltip_filter_all_check);
        this.friendCheck = (ImageView) this.view.findViewById(R.id.tooltip_filter_friends_check);
        this.privateCheck = (ImageView) this.view.findViewById(R.id.tooltip_filter_private_check);

        this.allCheck.setColorFilter(context.getResources().getColor(R.color.blue));
        this.friendCheck.setColorFilter(context.getResources().getColor(R.color.blue));
        this.privateCheck.setColorFilter(context.getResources().getColor(R.color.blue));

        title.setTypeface(CustomFonts.customContentBold(context));
        allText.setTypeface(CustomFonts.customContentRegular(context));
        friendText.setTypeface(CustomFonts.customContentRegular(context));
        privateText.setTypeface(CustomFonts.customContentRegular(context));

//        allLayout.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (delegate != null)
//                    delegate.filterChanged(FLTransaction.TransactionScope.TransactionScopePublic);
//                changeFilter(FLTransaction.TransactionScope.TransactionScopeAll);
//            }
//        });
//
//        friendLayout.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (delegate != null)
//                    delegate.filterChanged(FLTransaction.TransactionScope.TransactionScopeFriend);
//                changeFilter(FLTransaction.TransactionScope.TransactionScopeFriend);
//            }
//        });
//
//        privateLayout.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (delegate != null)
//                    delegate.filterChanged(FLTransaction.TransactionScope.TransactionScopePrivate);
//                changeFilter(FLTransaction.TransactionScope.TransactionScopePrivate);
//            }
//        });
    }

    public void changeFilter(FLScope scope) {

//        switch (scope) {
//            case TransactionScopeAll:
//                this.allCheck.setVisibility(View.VISIBLE);
//                this.friendCheck.setVisibility(View.GONE);
//                this.privateCheck.setVisibility(View.GONE);
//                break;
//            case TransactionScopeFriend:
//                this.allCheck.setVisibility(View.GONE);
//                this.friendCheck.setVisibility(View.VISIBLE);
//                this.privateCheck.setVisibility(View.GONE);
//                break;
//            case TransactionScopePrivate:
//                this.allCheck.setVisibility(View.GONE);
//                this.friendCheck.setVisibility(View.GONE);
//                this.privateCheck.setVisibility(View.VISIBLE);
//                break;
//        }
    }
}
