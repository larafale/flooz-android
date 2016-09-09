package me.flooz.app.UI.Controllers;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import me.flooz.app.Adapter.SettingsListAdapter;
import me.flooz.app.Adapter.SettingsListItem;
import me.flooz.app.Adapter.ShopHistoryAdapter;
import me.flooz.app.R;
import me.flooz.app.UI.Activity.AuthenticationActivity;
import me.flooz.app.UI.Activity.HomeActivity;
import me.flooz.app.UI.Activity.Settings.PasswordSettingsActivity;
import me.flooz.app.UI.Fragment.Home.TabFragments.PasswordFragment;
import me.flooz.app.UI.View.TimelineListView;

/**
 * Created by Flooz on 09/09/16.
 */
public class ShopHistoryController extends BaseController {

    private TimelineListView contentList;
    private ShopHistoryAdapter listAdapter;

    public ShopHistoryController(@NonNull View mainView, @NonNull Activity parentActivity, @NonNull ControllerKind kind) {
        super(mainView, parentActivity, kind);

        this.init();
    }

    public ShopHistoryController(@NonNull View mainView, @NonNull Activity parentActivity, @NonNull ControllerKind kind, @Nullable JSONObject data) {
        super(mainView, parentActivity, kind, data);

        this.init();
    }

    @Override
    protected void init() {
        super.init();

        this.contentList = (TimelineListView) this.currentView.findViewById(R.id.shop_history_list);

        this.listAdapter = new ShopHistoryAdapter(this.parentActivity);
        this.contentList.setAdapter(this.listAdapter);

        this.contentList.setOnTimelineListViewListener(new TimelineListView.OnTimelineListViewListener() {
            @Override
            public void onShowLastItem() {
                listAdapter.loadNextPage();
            }
        });
    }
}
