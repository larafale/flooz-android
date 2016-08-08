package me.flooz.app.Adapter;

import android.content.Context;
import android.media.Image;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;

import java.util.List;

import me.flooz.app.Model.FLTransaction;
import me.flooz.app.R;
import me.flooz.app.Utils.CustomFonts;

/**
 * Created by Flooz on 08/08/16.
 */
public class ScopePickerAdapter extends BaseAdapter {
    private LayoutInflater inflater;
    private Context context;

    public FLTransaction.TransactionScope currentScope = null;

    private Boolean isPot = false;
    private JSONArray limitedScopes;

    public ScopePickerAdapter(Context ctx, Boolean isPot, FLTransaction.TransactionScope currentScope, JSONArray limitedScopes) {
        this.inflater = LayoutInflater.from(ctx);
        this.context = ctx;

        this.isPot = isPot;
        this.currentScope = currentScope;
        this.limitedScopes = limitedScopes;
    }

    @Override
    public int getCount() {
        if (limitedScopes != null && limitedScopes.length() > 0)
            return limitedScopes.length();

        return 3;
    }

    @Override
    public FLTransaction.TransactionScope getItem(int i) {
        if (limitedScopes != null && limitedScopes.length() > 0)
            return FLTransaction.transactionScopeIDToScope(limitedScopes.optInt(i));

        switch (i) {
            case 0:
                return FLTransaction.TransactionScope.TransactionScopePublic;
            case 1:
                return FLTransaction.TransactionScope.TransactionScopeFriend;
            case 2:
                return FLTransaction.TransactionScope.TransactionScopePrivate;
        }

        return null;
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View convertView, ViewGroup parent) {
        final ViewHolder holder;

        if (convertView == null) {
            holder = new ViewHolder();
            convertView = inflater.inflate(R.layout.scope_picker_cell, parent, false);

            holder.image = (ImageView) convertView.findViewById(R.id.scope_picker_cell_image);
            holder.title = (TextView) convertView.findViewById(R.id.scope_picker_cell_title);
            holder.subtitle = (TextView) convertView.findViewById(R.id.scope_picker_cell_subtitle);
            holder.checkmark = (ImageView) convertView.findViewById(R.id.scope_picker_cell_check);

            holder.title.setTypeface(CustomFonts.customContentBold(context));
            holder.subtitle.setTypeface(CustomFonts.customContentRegular(context));

            holder.image.setColorFilter(context.getResources().getColor(android.R.color.white));
            holder.checkmark.setColorFilter(context.getResources().getColor(R.color.blue));

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        FLTransaction.TransactionScope scope = this.getItem(i);

        if (scope != null) {
            String title = null;
            String subtitle = null;

            holder.image.setImageDrawable(FLTransaction.transactionScopeToImage(scope));

            switch (scope) {
                case TransactionScopePublic:
                    title = context.getResources().getString(R.string.TRANSACTION_SCOPE_PUBLIC);
                    if (this.isPot)
                        subtitle = context.getResources().getString(R.string.TRANSACTION_SCOPE_SUB_POT_PUBLIC);
                    else
                        subtitle = context.getResources().getString(R.string.TRANSACTION_SCOPE_SUB_PUBLIC);

                    break;
                case TransactionScopeFriend:
                    title = context.getResources().getString(R.string.TRANSACTION_SCOPE_FRIEND);
                    if (this.isPot)
                        subtitle = context.getResources().getString(R.string.TRANSACTION_SCOPE_SUB_POT_FRIEND);
                    else
                        subtitle = context.getResources().getString(R.string.TRANSACTION_SCOPE_SUB_FRIEND);

                    break;
                case TransactionScopePrivate:
                    title = context.getResources().getString(R.string.TRANSACTION_SCOPE_PRIVATE);
                    if (this.isPot)
                        subtitle = context.getResources().getString(R.string.TRANSACTION_SCOPE_SUB_POT_PRIVATE);
                    else
                        subtitle = context.getResources().getString(R.string.TRANSACTION_SCOPE_SUB_PRIVATE);

                    break;
            }

            holder.title.setText(title);
            holder.subtitle.setText(subtitle);

            if (this.currentScope != null && this.currentScope == scope)
                holder.checkmark.setVisibility(View.VISIBLE);
            else
                holder.checkmark.setVisibility(View.INVISIBLE);
        }

        return convertView;
    }

    class ViewHolder {
        ImageView image;
        TextView title;
        TextView subtitle;
        ImageView checkmark;
    }
}
