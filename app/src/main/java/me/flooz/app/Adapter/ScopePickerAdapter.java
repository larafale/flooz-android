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

import me.flooz.app.Model.FLScope;
import me.flooz.app.Model.FLTransaction;
import me.flooz.app.R;
import me.flooz.app.Utils.CustomFonts;

/**
 * Created by Flooz on 08/08/16.
 */
public class ScopePickerAdapter extends BaseAdapter {
    private LayoutInflater inflater;
    private Context context;

    public FLScope currentScope = null;

    private Boolean isPot = false;
    private JSONArray limitedScopes;

    public ScopePickerAdapter(Context ctx, Boolean isPot, FLScope currentScope, JSONArray limitedScopes) {
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
    public FLScope getItem(int i) {
        if (limitedScopes != null && limitedScopes.length() > 0)
            return FLScope.scopeFromObject(limitedScopes.opt(i));

        return FLScope.scopeFromID(i);
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

        FLScope scope = this.getItem(i);

        if (scope != null) {
            holder.image.setImageDrawable(scope.image);
            holder.title.setText(scope.name);
            holder.subtitle.setText(scope.desc);

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
