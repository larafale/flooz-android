package me.flooz.app.UI.Activity;

import android.app.Activity;
import android.os.Bundle;
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
import me.flooz.app.Utils.FLHelper;
import me.flooz.app.Utils.ViewServer;

/**
 * Created by Flooz on 3/9/15.
 */
public class NotificationActivity extends Activity {

    private ImageView headerBackButton;
    private PullRefreshLayout contentContainer;
    private NotificationListAdapter listAdapter;
    private FloozApplication floozApp;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (FLHelper.isDebuggable())
            ViewServer.get(this).addWindow(this);

        floozApp = (FloozApplication)this.getApplicationContext();

        this.setContentView(R.layout.notification_fragment);

        this.headerBackButton = (ImageView) this.findViewById(R.id.notification_header_back);
        TextView headerTitle = (TextView) this.findViewById(R.id.notification_header_title);
        ListView contentList = (ListView) this.findViewById(R.id.notification_list);
        this.contentContainer = (PullRefreshLayout) this.findViewById(R.id.notification_container);

        headerTitle.setTypeface(CustomFonts.customTitleExtraLight(this));

        this.headerBackButton.setOnClickListener(view -> {
            FloozRestClient.getInstance().readAllNotifications(null);
            finish();
            overridePendingTransition(android.R.anim.fade_in, R.anim.slide_down);
        });

        this.listAdapter = new NotificationListAdapter(this);
        contentList.setAdapter(this.listAdapter);

        this.contentContainer.setOnRefreshListener(() -> FloozRestClient.getInstance().updateNotificationFeed(new FloozHttpResponseHandler() {
            @Override
            public void success(Object response) {
                contentContainer.setRefreshing(false);
            }

            @Override
            public void failure(int statusCode, FLError error) {
                contentContainer.setRefreshing(false);
            }
        }));

        contentList.setOnItemClickListener((parent, view, position, id) -> {
            FLNotification notif = (FLNotification) listAdapter.getItem(position);
            notif.isRead = true;
            if (notif.triggers.size() > 0)
                FloozRestClient.getInstance().handleRequestTriggers(notif.data);
            listAdapter.notifyDataSetChanged();
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        floozApp.setCurrentActivity(this);

        if (FLHelper.isDebuggable())
            ViewServer.get(this).setFocusedWindow(this);

        if (this.listAdapter != null)
            this.listAdapter.loadBroadcastReceivers();
        FloozRestClient.getInstance().updateNotificationFeed(null);
    }

    @Override
    public void onPause() {
        clearReferences();
        super.onPause();
        if (this.listAdapter != null)
            this.listAdapter.unloadBroadcastReceivers();
    }

    @Override
    protected void onDestroy() {
        clearReferences();

        if (FLHelper.isDebuggable())
            ViewServer.get(this).removeWindow(this);

        super.onDestroy();
    }


    private void clearReferences(){
        Activity currActivity = floozApp.getCurrentActivity();
        if (currActivity != null && currActivity.equals(this))
            floozApp.setCurrentActivity(null);
    }

    @Override
    public void onBackPressed() {
        this.headerBackButton.performClick();
    }

}
