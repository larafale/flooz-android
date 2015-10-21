package me.flooz.app.UI.Controllers;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import me.flooz.app.Adapter.SettingsListAdapter;
import me.flooz.app.Adapter.SettingsListItem;
import me.flooz.app.R;
import me.flooz.app.UI.Activity.AuthenticationActivity;
import me.flooz.app.UI.Activity.HomeActivity;
import me.flooz.app.UI.Activity.Settings.PasswordSettingsActivity;
import me.flooz.app.UI.Fragment.Home.TabFragments.NotifsSettingsFragment;
import me.flooz.app.UI.Fragment.Home.TabFragments.PasswordFragment;
import me.flooz.app.Utils.CustomFonts;

/**
 * Created by Flooz on 9/2/15.
 */
public class SecurityController extends BaseController {

    private ImageView headerBackButton;
    private TextView headerTitle;
    private ListView contentList;
    private SettingsListAdapter listAdapter;

    public SecurityController(@NonNull View mainView, @NonNull final Activity parentActivity, @NonNull ControllerKind kind) {
        super(mainView, parentActivity, kind);

        this.headerBackButton = (ImageView) this.currentView.findViewById(R.id.header_item_left);
        this.headerTitle = (TextView) this.currentView.findViewById(R.id.header_title);
        this.contentList = (ListView) this.currentView.findViewById(R.id.settings_security_list);

        this.headerTitle.setTypeface(CustomFonts.customTitleExtraLight(this.parentActivity));

        if (currentKind == ControllerKind.FRAGMENT_CONTROLLER)
            this.headerBackButton.setImageDrawable(this.parentActivity.getResources().getDrawable(R.drawable.nav_back));

        this.headerBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentKind == ControllerKind.ACTIVITY_CONTROLLER) {
                    parentActivity.finish();
                    parentActivity.overridePendingTransition(android.R.anim.fade_in, R.anim.slide_down);
                } else {
                    ((HomeActivity) parentActivity).popFragmentInCurrentTab();
                }
            }
        });

        List<SettingsListItem> itemList = new ArrayList<>();

        itemList.add(new SettingsListItem(this.parentActivity.getResources().getString(R.string.SETTINGS_CODE), new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(parentActivity, AuthenticationActivity.class);
                intent.putExtra("changeSecureCode", true);
                parentActivity.startActivity(intent);
                parentActivity.overridePendingTransition(R.anim.slide_up, android.R.anim.fade_out);
            }
        }));

        itemList.add(new SettingsListItem(this.parentActivity.getResources().getString(R.string.SETTINGS_PASSWORD), new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (currentKind == ControllerKind.ACTIVITY_CONTROLLER) {
                    Intent intent = new Intent(parentActivity, PasswordSettingsActivity.class);
                    parentActivity.startActivity(intent);
                    parentActivity.overridePendingTransition(R.anim.slide_up, android.R.anim.fade_out);
                } else {
                    ((HomeActivity) parentActivity).pushFragmentInCurrentTab(new PasswordFragment());
                }
            }
        }));

        this.listAdapter = new SettingsListAdapter(this.parentActivity, itemList, this.contentList);
    }

    @Override
    public void onBackPressed() {
        this.headerBackButton.performClick();
    }
}
