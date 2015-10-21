package me.flooz.app.UI.Controllers;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import me.flooz.app.Adapter.SettingsListAdapter;
import me.flooz.app.Adapter.SettingsListItem;
import me.flooz.app.App.FloozApplication;
import me.flooz.app.Network.FloozRestClient;
import me.flooz.app.R;
import me.flooz.app.UI.Activity.HomeActivity;
import me.flooz.app.UI.Activity.Settings.NotificationsSettingsActivity;
import me.flooz.app.UI.Activity.Settings.PrivacySettingsActivity;
import me.flooz.app.UI.Fragment.Home.TabFragments.NotifsSettingsFragment;
import me.flooz.app.UI.Fragment.Home.TabFragments.PrivacyFragment;
import me.flooz.app.Utils.CustomFonts;
import me.flooz.app.Utils.CustomNotificationIntents;

/**
 * Created by Flooz on 9/1/15.
 */
public class PreferencesController extends BaseController {

    private Boolean viewVisible = false;
    private ImageView headerBackButton;
    private CheckBox fbSwitch;

    private BroadcastReceiver reloadCurrentUserDataReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            reloadView();
        }
    };

    public PreferencesController(@NonNull View mainView, @NonNull final Activity parentActivity, @NonNull ControllerKind kind) {
        super(mainView, parentActivity, kind);

        this.headerBackButton = (ImageView) this.currentView.findViewById(R.id.header_item_left);
        TextView headerTitle = (TextView) this.currentView.findViewById(R.id.header_title);
        TextView fbText = (TextView) this.currentView.findViewById(R.id.settings_preferences_fb_text);
        this.fbSwitch = (CheckBox) this.currentView.findViewById(R.id.settings_preferences_fb_toggle);
        ListView contentList = (ListView) this.currentView.findViewById(R.id.settings_preferences_list);

        headerTitle.setTypeface(CustomFonts.customTitleExtraLight(this.parentActivity));
        fbText.setTypeface(CustomFonts.customTitleExtraLight(this.parentActivity));

        List<SettingsListItem> itemList = new ArrayList<>();

        itemList.add(new SettingsListItem(this.parentActivity.getResources().getString(R.string.SETTINGS_NOTIFICATIONS), new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (currentKind == ControllerKind.ACTIVITY_CONTROLLER) {
                    Intent intent = new Intent(parentActivity, NotificationsSettingsActivity.class);
                    parentActivity.startActivity(intent);
                    parentActivity.overridePendingTransition(R.anim.slide_up, android.R.anim.fade_out);
                } else {
                    ((HomeActivity)parentActivity).pushFragmentInCurrentTab(new NotifsSettingsFragment());
                }
            }
        }));

        itemList.add(new SettingsListItem(this.parentActivity.getResources().getString(R.string.SETTINGS_PRIVACY), new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (currentKind == ControllerKind.ACTIVITY_CONTROLLER) {
                    Intent intent = new Intent(parentActivity, PrivacySettingsActivity.class);
                    parentActivity.startActivity(intent);
                    parentActivity.overridePendingTransition(R.anim.slide_up, android.R.anim.fade_out);
                } else {
                    ((HomeActivity) parentActivity).pushFragmentInCurrentTab(new PrivacyFragment());
                }
            }
        }));

        new SettingsListAdapter(this.parentActivity, itemList, contentList);

        this.reloadView();

        this.fbSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    if (!FloozRestClient.getInstance().isConnectedToFacebook()) {
                        FloozRestClient.getInstance().connectFacebook();
                        fbSwitch.setChecked(false);
                    }
                } else {
                    FloozRestClient.getInstance().disconnectFacebook();
                }
            }
        });

        if (currentKind == ControllerKind.FRAGMENT_CONTROLLER)
            this.headerBackButton.setImageDrawable(this.parentActivity.getResources().getDrawable(R.drawable.nav_back));

        this.headerBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentKind == ControllerKind.ACTIVITY_CONTROLLER) {
                    parentActivity.finish();
                    parentActivity.overridePendingTransition(android.R.anim.fade_in, R.anim.slide_down);
                } else {
                    ((HomeActivity) parentActivity).popFragmentInCurrentTab(R.animator.slide_in_right, R.animator.slide_out_left);
                }
            }
        });
    }


    @Override
    public void onResume() {
        this.viewVisible = true;
        this.reloadView();

        LocalBroadcastManager.getInstance(FloozApplication.getAppContext()).registerReceiver(reloadCurrentUserDataReceiver,
                CustomNotificationIntents.filterReloadCurrentUser());
    }

    @Override
    public void onPause() {
        LocalBroadcastManager.getInstance(FloozApplication.getAppContext()).unregisterReceiver(reloadCurrentUserDataReceiver);
    }

    private void reloadView() {
        if (this.viewVisible) {
            if (FloozRestClient.getInstance().isConnectedToFacebook())
                this.fbSwitch.setChecked(true);
            else
                this.fbSwitch.setChecked(false);
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        FloozRestClient.getInstance().fbLoginCallbackManager.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onBackPressed() {
        this.headerBackButton.performClick();
    }
}
