package flooz.android.com.flooz.Adapter;

import android.content.Context;
import android.widget.AdapterView;

/**
 * Created by Flooz on 10/15/14.
 */

public class SettingsListItem {

    private String _title;
    private int _nbNotifs;
    private AdapterView.OnItemClickListener _clickListener;

    public SettingsListItem(Context context, String title, AdapterView.OnItemClickListener clickListener) {
        this._title = title;
        this._nbNotifs = 0;
        this._clickListener = clickListener;
    }

    public SettingsListItem(Context context, String title, int nbNotifs, AdapterView.OnItemClickListener clickListener) {
        this._title = title;
        this._nbNotifs = nbNotifs;
        this._clickListener = clickListener;
    }

}
