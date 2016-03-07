package me.flooz.app.UI.Controllers;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.baoyz.widget.PullRefreshLayout;

import org.json.JSONObject;

import me.flooz.app.Adapter.NotificationListAdapter;
import me.flooz.app.App.FloozApplication;
import me.flooz.app.Model.FLError;
import me.flooz.app.Model.FLNotification;
import me.flooz.app.Network.FloozHttpResponseHandler;
import me.flooz.app.Network.FloozRestClient;
import me.flooz.app.R;
import me.flooz.app.Utils.CustomFonts;

/**
 * Created by Flooz on 8/25/15.
 */
public class NotificationsController extends BaseController {

    private PullRefreshLayout contentContainer;
    private NotificationListAdapter listAdapter;

    public NotificationsController(@NonNull View mainView, @NonNull Activity parentActivity, @NonNull ControllerKind kind) {
        super(mainView, parentActivity, kind);

        this.init();
    }

    public NotificationsController(@NonNull View mainView, @NonNull Activity parentActivity, @NonNull ControllerKind kind, @Nullable JSONObject data) {
        super(mainView, parentActivity, kind, data);

        this.init();
    }

    @Override
    protected void init() {
        super.init();

        ListView contentList = (ListView)  this.currentView.findViewById(R.id.notification_list);
        this.contentContainer = (PullRefreshLayout)  this.currentView.findViewById(R.id.notification_container);

        if (this.currentKind == ControllerKind.FRAGMENT_CONTROLLER)
            this.closeButton.setVisibility(View.GONE);

        this.listAdapter = new NotificationListAdapter(parentActivity);
        contentList.setAdapter(this.listAdapter);

        this.contentContainer.setOnRefreshListener(new PullRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                FloozRestClient.getInstance().updateNotificationFeed(new FloozHttpResponseHandler() {
                    @Override
                    public void success(Object response) {
                        contentContainer.setRefreshing(false);
                    }

                    @Override
                    public void failure(int statusCode, FLError error) {
                        contentContainer.setRefreshing(false);
                    }
                });
            }
        });

        contentList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                FLNotification notif = (FLNotification) listAdapter.getItem(position);
                notif.isRead = true;
                if (notif.triggers.size() > 0)
                    FloozRestClient.getInstance().handleRequestTriggers(notif.data);
                listAdapter.notifyDataSetChanged();
            }
        });
    }

    public void onResume() {
        if (this.listAdapter != null)
            this.listAdapter.loadBroadcastReceivers();
        this.contentContainer.setRefreshing(false);
        FloozRestClient.getInstance().updateNotificationFeed(null);
    }

    public void onPause() {
        if (this.listAdapter != null)
            this.listAdapter.unloadBroadcastReceivers();
        FloozRestClient.getInstance().readAllNotifications(null);
    }
}