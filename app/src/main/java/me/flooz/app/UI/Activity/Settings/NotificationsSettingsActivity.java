package me.flooz.app.UI.Activity.Settings;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import me.flooz.app.Adapter.NotificationSettingsListAdapter;
import me.flooz.app.App.FloozApplication;
import me.flooz.app.R;
import me.flooz.app.Utils.CustomFonts;
import se.emilsjolander.stickylistheaders.StickyListHeadersListView;

/**
 * Created by Flooz on 3/10/15.
 */
public class NotificationsSettingsActivity extends Activity {

    private NotificationsSettingsActivity instance;
    private FloozApplication floozApp;
    private Boolean modal;

    private ImageView headerBackButton;
    private TextView headerTitle;
    private StickyListHeadersListView contentList;
    private NotificationSettingsListAdapter listAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.instance = this;
        this.floozApp = (FloozApplication) this.getApplicationContext();
        this.modal = getIntent().getBooleanExtra("modal", false);

        this.setContentView(R.layout.settings_notifications_fragment);

        this.headerBackButton = (ImageView) this.findViewById(R.id.settings_notifications_header_back);
        this.headerTitle = (TextView) this.findViewById(R.id.settings_notifications_header_title);
        this.contentList = (StickyListHeadersListView) this.findViewById(R.id.settings_notifications_list);

        this.headerTitle.setTypeface(CustomFonts.customTitleExtraLight(this));

        this.headerBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
                if (modal)
                    overridePendingTransition(android.R.anim.fade_in, R.anim.slide_down);
                else
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right);
            }
        });

        this.listAdapter = new NotificationSettingsListAdapter(this);

        this.contentList.setAdapter(this.listAdapter);
    }


    @Override
    public void onResume() {
        super.onResume();
        floozApp.setCurrentActivity(this);
    }

    @Override
    public void onPause() {
        clearReferences();
        super.onPause();
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
