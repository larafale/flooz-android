package me.flooz.app.UI.Activity.Settings;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
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
import me.flooz.app.UI.Activity.AuthenticationActivity;
import me.flooz.app.Utils.CustomFonts;
import me.flooz.app.Utils.FLHelper;
import me.flooz.app.Utils.JSONHelper;
import me.flooz.app.Utils.ViewServer;

/**
 * Created by Flooz on 3/10/15.
 */
public class SecuritySettingsActivity extends Activity {

    private SecuritySettingsActivity instance;
    private FloozApplication floozApp;
    private Boolean modal;

    private ImageView headerBackButton;
    private TextView headerTitle;
    private ListView contentList;
    private SettingsListAdapter listAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (FLHelper.isDebuggable())
            ViewServer.get(this).addWindow(this);

        this.instance = this;
        this.floozApp = (FloozApplication) this.getApplicationContext();
        this.modal = getIntent().getBooleanExtra("modal", false);

        this.setContentView(R.layout.settings_security_fragment);

        this.headerBackButton = (ImageView) this.findViewById(R.id.settings_security_header_back);
        this.headerTitle = (TextView) this.findViewById(R.id.settings_security_header_title);
        this.contentList = (ListView) this.findViewById(R.id.settings_security_list);

        this.headerTitle.setTypeface(CustomFonts.customTitleExtraLight(this));

        this.headerBackButton.setOnClickListener(view -> {
            finish();
            if (modal)
                overridePendingTransition(android.R.anim.fade_in, R.anim.slide_down);
            else
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right);
        });
   }


    @Override
    public void onResume() {
        super.onResume();

        if (FLHelper.isDebuggable())
            ViewServer.get(this).setFocusedWindow(this);

        floozApp.setCurrentActivity(this);

        int securityNotif = 0;

        List missingFields;
        try {
            missingFields = JSONHelper.toList(FloozRestClient.getInstance().currentUser.json.optJSONArray("missingFields"));

            if (missingFields.contains("secret"))
                ++securityNotif;
        } catch (JSONException e) {
            e.printStackTrace();
        }

        List<SettingsListItem> itemList = new ArrayList<>();

        itemList.add(new SettingsListItem(this.getResources().getString(R.string.SETTINGS_CODE), (parent, view, position, id) -> {
            Intent intent = new Intent(instance, AuthenticationActivity.class);
            intent.putExtra("changeSecureCode", true);
            instance.startActivity(intent);
            instance.overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left);
        }));

        itemList.add(new SettingsListItem(this.getResources().getString(R.string.SETTINGS_PASSWORD), (parent, view, position, id) -> {
            Intent intent = new Intent(instance, PasswordSettingsActivity.class);
            instance.startActivity(intent);
            instance.overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left);
        }));

        this.listAdapter = new SettingsListAdapter(this, itemList, this.contentList);
    }

    @Override
    public void onPause() {
        clearReferences();
        super.onPause();
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
