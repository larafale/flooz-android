package me.flooz.app.UI.Controllers;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONObject;

import me.flooz.app.Adapter.NotificationSettingsListAdapter;
import me.flooz.app.R;
import me.flooz.app.UI.Activity.HomeActivity;
import me.flooz.app.Utils.CustomFonts;
import se.emilsjolander.stickylistheaders.StickyListHeadersListView;

/**
 * Created by Flooz on 9/2/15.
 */
public class NotifsSettingsController extends BaseController {

    public NotifsSettingsController(@NonNull View mainView, @NonNull Activity parentActivity, @NonNull ControllerKind kind) {
        super(mainView, parentActivity, kind);

        this.init();
    }

    public NotifsSettingsController(@NonNull View mainView, @NonNull Activity parentActivity, @NonNull ControllerKind kind, @Nullable JSONObject data) {
        super(mainView, parentActivity, kind, data);

        this.init();
    }

    @Override
    protected void init() {
        super.init();

        StickyListHeadersListView contentList = (StickyListHeadersListView) this.currentView.findViewById(R.id.settings_notifications_list);

        NotificationSettingsListAdapter listAdapter = new NotificationSettingsListAdapter(this.parentActivity);

        contentList.setAdapter(listAdapter);
    }
}
