package me.flooz.app.UI.Controllers;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.baoyz.widget.PullRefreshLayout;

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

    private ImageView headerBackButton;
    private PullRefreshLayout contentContainer;
    private NotificationListAdapter listAdapter;

    public NotificationsController(@NonNull View mainView, @NonNull final Activity parentActivity, @NonNull BaseController.ControllerKind kind) {
        super(mainView, parentActivity, kind);

        this.headerBackButton = (ImageView) this.currentView.findViewById(R.id.header_item_left);
        TextView headerTitle = (TextView)  this.currentView.findViewById(R.id.header_title);
        ListView contentList = (ListView)  this.currentView.findViewById(R.id.notification_list);
        this.contentContainer = (PullRefreshLayout)  this.currentView.findViewById(R.id.notification_container);

        headerTitle.setTypeface(CustomFonts.customTitleExtraLight(this.parentActivity));

        this.headerBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FloozRestClient.getInstance().readAllNotifications(null);
                parentActivity.finish();
                parentActivity.overridePendingTransition(android.R.anim.fade_in, R.anim.slide_down);
            }
        });

        if (this.currentKind == ControllerKind.FRAGMENT_CONTROLLER)
            this.headerBackButton.setVisibility(View.GONE);

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
        FloozRestClient.getInstance().updateNotificationFeed(null);
    }

    public void onPause() {
        if (this.listAdapter != null)
            this.listAdapter.unloadBroadcastReceivers();
        FloozRestClient.getInstance().readAllNotifications(null);
    }

    public void onBackPressed() {
        if (this.currentKind == ControllerKind.ACTIVITY_CONTROLLER)
            this.headerBackButton.performClick();
    }
}