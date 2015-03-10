package me.flooz.app.UI.Activity.Settings;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import me.flooz.app.Adapter.SettingsListAdapter;
import me.flooz.app.Adapter.SettingsListItem;
import me.flooz.app.App.FloozApplication;
import me.flooz.app.R;
import me.flooz.app.UI.Fragment.Home.Authentication.AuthenticationFragment;
import me.flooz.app.Utils.CustomFonts;

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

        this.instance = this;
        this.floozApp = (FloozApplication) this.getApplicationContext();
        this.modal = getIntent().getBooleanExtra("modal", false);

        this.setContentView(R.layout.settings_security_fragment);

        this.headerBackButton = (ImageView) this.findViewById(R.id.settings_security_header_back);
        this.headerTitle = (TextView) this.findViewById(R.id.settings_security_header_title);
        this.contentList = (ListView) this.findViewById(R.id.settings_security_list);

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

        List<SettingsListItem> itemList = new ArrayList<>();

        itemList.add(new SettingsListItem(this.getResources().getString(R.string.SETTINGS_CODE), new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                ((AuthenticationFragment)parentActivity.contentFragments.get("authentication")).changeSecureCode = true;
//                parentActivity.pushMainFragment("authentication", R.animator.slide_in_left, R.animator.slide_out_right);
            }
        }));

        itemList.add(new SettingsListItem(this.getResources().getString(R.string.SETTINGS_PASSWORD), new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                parentActivity.pushMainFragment("settings_password", R.animator.slide_in_left, R.animator.slide_out_right);
            }
        }));

        this.listAdapter = new SettingsListAdapter(this, itemList, this.contentList);
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
