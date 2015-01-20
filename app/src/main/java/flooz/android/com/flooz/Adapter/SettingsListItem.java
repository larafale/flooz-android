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

    public SettingsListItem(String title, AdapterView.OnItemClickListener clickListener) {
        this._title = title;
        this._nbNotifs = 0;
        this._clickListener = clickListener;
    }

    public SettingsListItem(String title, int nbNotifs, AdapterView.OnItemClickListener clickListener) {
        this._title = title;
        this._nbNotifs = nbNotifs;
        this._clickListener = clickListener;
    }

    public String getTitle() {
        return this._title;
    }

    public int getNbNotifs() {
        return this._nbNotifs;
    }

    public boolean hasNotifs() {
        return this._nbNotifs > 0;
    }

    public AdapterView.OnItemClickListener getItemClickListener() {
        return this._clickListener;
    }
}
