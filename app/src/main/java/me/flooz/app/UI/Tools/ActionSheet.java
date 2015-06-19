package me.flooz.app.UI.Tools;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

import me.flooz.app.R;
import me.flooz.app.Utils.CustomFonts;

/**
 * Created by Flooz on 10/13/14.
 */
public class ActionSheet {

    public static void showWithItems(Context context, final List<ActionSheetItem> items) {

        LayoutInflater inflater = LayoutInflater.from(context);

        final Dialog dialog = new Dialog(context, R.style.action_sheet_style);

        dialog.setContentView(R.layout.action_sheet_layout);
        dialog.setCanceledOnTouchOutside(true);

        LinearLayout actionList = (LinearLayout) dialog.findViewById(R.id.action_sheet_list);

        for (int i = 0; i < items.size(); i++) {
            if (i > 0)
                actionList.addView(inflater.inflate(R.layout.action_sheet_list_separator, null));

            final int pos = i;

            items.get(i).itemLayout.setOnClickListener(view -> {
                if (items.get(pos).clickListener != null)
                    items.get(pos).clickListener.onClick();
                dialog.dismiss();
            });
            actionList.addView(items.get(i).itemLayout);
        }

        TextView cancelButton = (TextView) dialog.findViewById(R.id.action_sheet_cancel);
        cancelButton.setTypeface(CustomFonts.customTitleExtraLight(context), Typeface.BOLD);
        cancelButton.setOnClickListener(view -> dialog.dismiss());

        dialog.show();

        dialog.getWindow().setGravity(Gravity.BOTTOM);
    }
}
