package flooz.android.com.flooz.UI.Tools;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import flooz.android.com.flooz.R;
import flooz.android.com.flooz.Utils.CustomFonts;

/**
 * Created by Flooz on 10/13/14.
 */
public class ActionSheetItem {

    public interface ActionSheetItemClickListener {
        public void onClick();
    }

    public Button itemLayout;
    public ActionSheetItemClickListener clickListener;

    public ActionSheetItem(Context ctx, String content, ActionSheetItemClickListener listener) {

        LayoutInflater inflater = LayoutInflater.from(ctx);
        this.itemLayout = (Button) inflater.inflate(R.layout.action_sheet_list_row, null);

        this.clickListener = listener;

        this.itemLayout.setTypeface(CustomFonts.customTitleExtraLight(ctx));
        this.itemLayout.setText(content);
    }

    public ActionSheetItem(Context ctx, int idRes, ActionSheetItemClickListener listener) {

        LayoutInflater inflater = LayoutInflater.from(ctx);
        this.itemLayout = (Button) inflater.inflate(R.layout.action_sheet_list_row, null);

        this.clickListener = listener;

        this.itemLayout.setTypeface(CustomFonts.customTitleExtraLight(ctx));
        this.itemLayout.setText(ctx.getResources().getString(idRes));
    }
}
