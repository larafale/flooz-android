package me.flooz.app.UI.Activity.Settings;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

import me.flooz.app.Adapter.SettingsListAdapter;
import me.flooz.app.Adapter.SettingsListItem;
import me.flooz.app.App.FloozApplication;
import me.flooz.app.Network.FloozRestClient;
import me.flooz.app.R;
import me.flooz.app.Utils.CustomFonts;
import me.flooz.app.Utils.CustomNotificationIntents;
import me.flooz.app.Utils.JSONHelper;

/**
 * Created by Flooz on 3/10/15.
 */
public class ProfileSettingsActivity extends Activity {

    private ProfileSettingsActivity instance;
    private FloozApplication floozApp;

    private ImageView headerBackButton;
    private TextView headerTitle;
    private ListView contentList;
    private SettingsListAdapter listAdapter;
    private LinearLayout infosContainer;
    private TextView infosText;
    private TextView infosNotifsText;

    private BroadcastReceiver reloadCurrentUserDataReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateList();
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.instance = this;
        this.floozApp = (FloozApplication) this.getApplicationContext();

        this.setContentView(R.layout.profile_settings);

        this.headerBackButton = (ImageView) this.findViewById(R.id.profile_settings_header_back);
        this.headerTitle = (TextView) this.findViewById(R.id.profile_settings_header_title);
        this.contentList = (ListView) this.findViewById(R.id.profile_settings_list);
        this.infosContainer = (LinearLayout) this.findViewById(R.id.profile_settings_infos);
        this.infosText = (TextView) this.findViewById(R.id.profile_settings_infos_text);
        this.infosNotifsText = (TextView) this.findViewById(R.id.profile_settings_infos_notifs_text);

        this.headerTitle.setTypeface(CustomFonts.customTitleExtraLight(this));
        this.infosText.setTypeface(CustomFonts.customTitleLight(this));
        this.infosNotifsText.setTypeface(CustomFonts.customContentBold(this));

        this.headerBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
                overridePendingTransition(android.R.anim.fade_in, R.anim.slide_down);
            }
        });

        this.updateList();
    }

    public void updateList() {
        int cardNotif = 0;
        int bankNotif = 0;
        int indentityNotif = 0;
        int coordsNotif = 0;

        List<SettingsListItem> itemList = new ArrayList<>();

        if (FloozRestClient.getInstance().currentUser.creditCard == null || FloozRestClient.getInstance().currentUser.creditCard.cardId == null || FloozRestClient.getInstance().currentUser.creditCard.cardId.isEmpty())
            ++cardNotif;

        List missingFields;
        try {
            missingFields = JSONHelper.toList(FloozRestClient.getInstance().currentUser.json.optJSONArray("missingFields"));

            if (missingFields.contains("sepa"))
                ++bankNotif;
            if (missingFields.contains("cniRecto"))
                ++indentityNotif;
            if (missingFields.contains("cniVerso"))
                ++indentityNotif;
            if (missingFields.contains("justificatory"))
                ++coordsNotif;
            if (missingFields.contains("address"))
                ++coordsNotif;
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if (bankNotif + indentityNotif + coordsNotif + bankNotif > 0)
            this.infosContainer.setVisibility(View.VISIBLE);
        else
            this.infosContainer.setVisibility(View.GONE);

        itemList.add(new SettingsListItem(this.getResources().getString(R.string.SETTINGS_CARD), cardNotif, new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(instance, CreditCardSettingsActivity.class);
                instance.startActivity(intent);
                instance.overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left);
            }
        }));

        itemList.add(new SettingsListItem(this.getResources().getString(R.string.SETTINGS_RIB), bankNotif, new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(instance, BankSettingsActivity.class);
                instance.startActivity(intent);
                instance.overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left);
            }
        }));

        itemList.add(new SettingsListItem(this.getResources().getString(R.string.SETTINGS_IDENTITY), indentityNotif, new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(instance, IdentitySettingsActivity.class);
                instance.startActivity(intent);
                instance.overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left);
            }
        }));

        itemList.add(new SettingsListItem(this.getResources().getString(R.string.SETTINGS_COORD), coordsNotif, new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(instance, CoordsSettingsActivity.class);
                instance.startActivity(intent);
                instance.overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left);
            }
        }));

        itemList.add(new SettingsListItem(this.getResources().getString(R.string.SETTINGS_SECURITY), new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(instance, SecuritySettingsActivity.class);
                instance.startActivity(intent);
                instance.overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left);
            }
        }));

        itemList.add(new SettingsListItem(this.getResources().getString(R.string.SETTINGS_PREFERENCES), new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(instance, PreferencesSettingsActivity.class);
                instance.startActivity(intent);
                instance.overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left);
            }
        }));

        itemList.add(new SettingsListItem(this.getResources().getString(R.string.SETTINGS_PRIVACY), new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(instance, PrivacySettingsActivity.class);
                instance.startActivity(intent);
                instance.overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left);
            }
        }));

        this.listAdapter = new SettingsListAdapter(this, itemList, this.contentList);
    }

    @Override
    public void onResume() {
        super.onResume();
        floozApp.setCurrentActivity(this);
        this.updateList();

        LocalBroadcastManager.getInstance(FloozApplication.getAppContext()).registerReceiver(reloadCurrentUserDataReceiver,
                CustomNotificationIntents.filterReloadCurrentUser());
    }

    @Override
    public void onPause() {
        clearReferences();
        super.onPause();
        LocalBroadcastManager.getInstance(FloozApplication.getAppContext()).unregisterReceiver(reloadCurrentUserDataReceiver);
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
