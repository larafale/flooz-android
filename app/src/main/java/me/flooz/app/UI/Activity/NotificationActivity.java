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

/**
 * Created by Flooz on 3/9/15.
 */
public class NotificationActivity extends Activity {

    private ImageView headerBackButton;
    private TextView headerTitle;
    private ListView contentList;
    private PullRefreshLayout contentContainer;
    private NotificationListAdapter listAdapter;
    private FloozApplication floozApp;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        floozApp = (FloozApplication)this.getApplicationContext();

        this.setContentView(R.layout.notification_fragment);

        this.headerBackButton = (ImageView) this.findViewById(R.id.notification_header_back);
        this.headerTitle = (TextView) this.findViewById(R.id.notification_header_title);
        this.contentList = (ListView) this.findViewById(R.id.notification_list);
        this.contentContainer = (PullRefreshLayout) this.findViewById(R.id.notification_container);

        this.headerTitle.setTypeface(CustomFonts.customTitleExtraLight(this));

        this.headerBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FloozRestClient.getInstance().readAllNotifications(null);
                finish();
                overridePendingTransition(android.R.anim.fade_in, R.anim.slide_down);
            }
        });

        this.listAdapter = new NotificationListAdapter(this);
        this.contentList.setAdapter(this.listAdapter);

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

        this.contentList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                FLNotification notif = (FLNotification)listAdapter.getItem(position);
                notif.isRead = true;
                for (int i = 0; i < notif.triggers.size(); i++) {
                    FloozRestClient.getInstance().handleTrigger(notif.triggers.get(i));
                }
                listAdapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        floozApp.setCurrentActivity(this);
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
