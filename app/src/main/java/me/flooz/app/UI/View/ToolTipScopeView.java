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

public class ToolTipScopeView {

    public View view;

    private ImageView publicCheck;
    private ImageView friendCheck;
    private ImageView privateCheck;

    public ToolTipScopeViewDelegate delegate;

    public ToolTipScopeView(Context context) {

        this.view = LayoutInflater.from(context).inflate(R.layout.tooltip_scope_view, null);

        RelativeLayout publicLayout = (RelativeLayout) this.view.findViewById(R.id.tooltip_scope_public);
        RelativeLayout friendLayout = (RelativeLayout) this.view.findViewById(R.id.tooltip_scope_friends);
        RelativeLayout privateLayout = (RelativeLayout) this.view.findViewById(R.id.tooltip_scope_private);

        ImageView publicImg = (ImageView) this.view.findViewById(R.id.tooltip_scope_public_img);
        ImageView friendImg = (ImageView) this.view.findViewById(R.id.tooltip_scope_friends_img);
        ImageView privateImg = (ImageView) this.view.findViewById(R.id.tooltip_scope_private_img);

        TextView publicText = (TextView) this.view.findViewById(R.id.tooltip_scope_public_text);
        TextView friendText = (TextView) this.view.findViewById(R.id.tooltip_scope_friends_text);
        TextView privateText = (TextView) this.view.findViewById(R.id.tooltip_scope_private_text);

        this.publicCheck = (ImageView) this.view.findViewById(R.id.tooltip_scope_public_check);
        this.friendCheck = (ImageView) this.view.findViewById(R.id.tooltip_scope_friends_check);
        this.privateCheck = (ImageView) this.view.findViewById(R.id.tooltip_scope_private_check);

        publicImg.setColorFilter(context.getResources().getColor(android.R.color.black));
        friendImg.setColorFilter(context.getResources().getColor(android.R.color.black));
        privateImg.setColorFilter(context.getResources().getColor(android.R.color.black));

        this.publicCheck.setColorFilter(context.getResources().getColor(R.color.blue));
        this.friendCheck.setColorFilter(context.getResources().getColor(R.color.blue));
        this.privateCheck.setColorFilter(context.getResources().getColor(R.color.blue));

        publicText.setTypeface(CustomFonts.customContentRegular(context));
        friendText.setTypeface(CustomFonts.customContentRegular(context));
        privateText.setTypeface(CustomFonts.customContentRegular(context));

//        publicLayout.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (delegate != null)
//                    delegate.scopeChanged(FLTransaction.TransactionScope.TransactionScopePublic);
//                changeScope(FLTransaction.TransactionScope.TransactionScopePublic);
//            }
//        });
//
//        friendLayout.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (delegate != null)
//                    delegate.scopeChanged(FLTransaction.TransactionScope.TransactionScopeFriend);
//                changeScope(FLTransaction.TransactionScope.TransactionScopeFriend);
//            }
//        });
//
//        privateLayout.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (delegate != null)
//                    delegate.scopeChanged(FLTransaction.TransactionScope.TransactionScopePrivate);
//                changeScope(FLTransaction.TransactionScope.TransactionScopePrivate);
//            }
//        });
    }

    public void changeScope(FLScope scope) {

//        switch (scope) {
//            case TransactionScopePublic:
//                this.publicCheck.setVisibility(View.VISIBLE);
//                this.friendCheck.setVisibility(View.GONE);
//                this.privateCheck.setVisibility(View.GONE);
//                break;
//            case TransactionScopeFriend:
//                this.publicCheck.setVisibility(View.GONE);
//                this.friendCheck.setVisibility(View.VISIBLE);
//                this.privateCheck.setVisibility(View.GONE);
//                break;
//            case TransactionScopePrivate:
//                this.publicCheck.setVisibility(View.GONE);
//                this.friendCheck.setVisibility(View.GONE);
//                this.privateCheck.setVisibility(View.VISIBLE);
//                break;
//        }
    }
}
