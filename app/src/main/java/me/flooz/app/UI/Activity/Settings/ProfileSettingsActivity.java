package me.flooz.app.UI.Activity.Settings;

import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
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
import me.flooz.app.Utils.FLHelper;
import me.flooz.app.Utils.JSONHelper;
import me.flooz.app.Utils.ViewServer;

/**
 * Created by Flooz on 3/10/15.
 */
public class ProfileSettingsActivity extends Activity {

    private ProfileSettingsActivity instance;
    private FloozApplication floozApp;

    private ImageView headerBackButton;
    private ListView contentList;
    private LinearLayout infosContainer;

    private BroadcastReceiver reloadCurrentUserDataReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateList();
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (FLHelper.isDebuggable())
            ViewServer.get(this).addWindow(this);

        this.instance = this;
        this.floozApp = (FloozApplication) this.getApplicationContext();

        this.setContentView(R.layout.profile_settings);

        this.headerBackButton = (ImageView) this.findViewById(R.id.profile_settings_header_back);
        TextView headerTitle = (TextView) this.findViewById(R.id.profile_settings_header_title);
        this.contentList = (ListView) this.findViewById(R.id.profile_settings_list);
        this.infosContainer = (LinearLayout) this.findViewById(R.id.profile_settings_infos);
        TextView infosText = (TextView) this.findViewById(R.id.profile_settings_infos_text);
        TextView infosNotifsText = (TextView) this.findViewById(R.id.profile_settings_infos_notifs_text);

        headerTitle.setTypeface(CustomFonts.customTitleExtraLight(this));
        infosText.setTypeface(CustomFonts.customTitleLight(this));
        infosNotifsText.setTypeface(CustomFonts.customContentBold(this));

        this.headerBackButton.setOnClickListener(view -> {
            instance.finish();
            instance.overridePendingTransition(android.R.anim.fade_in, R.anim.slide_down);
        });

        this.updateList();
    }

    public void updateList() {
        int cardNotif = 0;
        int bankNotif = 0;
        int indentityNotif = 0;
        int coordsNotif = 0;
        int securityNotif = 0;

        if (FloozRestClient.getInstance().currentUser.creditCard == null || FloozRestClient.getInstance().currentUser.creditCard.cardId == null || FloozRestClient.getInstance().currentUser.creditCard.cardId.isEmpty())
            ++cardNotif;

        List<SettingsListItem> itemList = new ArrayList<>();
        List missingFields;
        try {
            missingFields = JSONHelper.toList(FloozRestClient.getInstance().currentUser.json.optJSONArray("missingFields"));

            if (missingFields.contains("sepa"))
                ++bankNotif;
            if (missingFields.contains("secret"))
                ++securityNotif;
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

        if (bankNotif + indentityNotif + coordsNotif + bankNotif + securityNotif > 0)
            this.infosContainer.setVisibility(View.VISIBLE);
        else
            this.infosContainer.setVisibility(View.GONE);

        itemList.add(new SettingsListItem(this.getResources().getString(R.string.SETTINGS_CARD), cardNotif, (parent, view, position, id) -> {
            Intent intent = new Intent(instance, CreditCardSettingsActivity.class);
            instance.startActivity(intent);
            instance.overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left);
        }));

        itemList.add(new SettingsListItem(this.getResources().getString(R.string.SETTINGS_RIB), bankNotif, (parent, view, position, id) -> {
            Intent intent = new Intent(instance, BankSettingsActivity.class);
            instance.startActivity(intent);
            instance.overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left);
        }));

        itemList.add(new SettingsListItem(this.getResources().getString(R.string.SETTINGS_IDENTITY), indentityNotif, (parent, view, position, id) -> {
            Intent intent = new Intent(instance, IdentitySettingsActivity.class);
            instance.startActivity(intent);
            instance.overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left);
        }));

        itemList.add(new SettingsListItem(this.getResources().getString(R.string.SETTINGS_COORD), coordsNotif, (parent, view, position, id) -> {
            Intent intent = new Intent(instance, CoordsSettingsActivity.class);
            instance.startActivity(intent);
            instance.overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left);
        }));

        itemList.add(new SettingsListItem(this.getResources().getString(R.string.SETTINGS_SECURITY), securityNotif, (parent, view, position, id) -> {
            Intent intent = new Intent(instance, SecuritySettingsActivity.class);
            instance.startActivity(intent);
            instance.overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left);
        }));

        itemList.add(new SettingsListItem(this.getResources().getString(R.string.SETTINGS_PREFERENCES), (parent, view, position, id) -> {
            Intent intent = new Intent(instance, PreferencesSettingsActivity.class);
            instance.startActivity(intent);
            instance.overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left);
        }));

        itemList.add(new SettingsListItem(this.getResources().getString(R.string.SETTINGS_LOGOUT), (parent, view, position, id) -> {
            final Dialog validationDialog = new Dialog(instance);

            validationDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            validationDialog.setContentView(R.layout.custom_dialog_validate_transaction);

            TextView text = (TextView) validationDialog.findViewById(R.id.dialog_validate_flooz_text);
            text.setText(instance.getResources().getString(R.string.LOGOUT_INFO));
            text.setTypeface(CustomFonts.customContentRegular(instance));

            Button decline = (Button) validationDialog.findViewById(R.id.dialog_validate_flooz_decline);
            Button accept = (Button) validationDialog.findViewById(R.id.dialog_validate_flooz_accept);

            decline.setOnClickListener(v -> validationDialog.dismiss());

            accept.setOnClickListener(v -> {
                validationDialog.dismiss();
                FloozRestClient.getInstance().logout();
            });

            validationDialog.setCanceledOnTouchOutside(false);
            validationDialog.show();
        }));

        new SettingsListAdapter(this, itemList, this.contentList);
    }

    @Override
    public void onResume() {
        super.onResume();
        floozApp.setCurrentActivity(this);

        if (FLHelper.isDebuggable())
            ViewServer.get(this).setFocusedWindow(this);

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
