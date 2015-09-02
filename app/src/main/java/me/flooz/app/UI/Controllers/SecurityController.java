package me.flooz.app.UI.Controllers;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.view.View;
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
import me.flooz.app.Utils.CustomFonts;

/**
 * Created by Flooz on 9/2/15.
 */
public class SecurityController extends BaseController {

    private ImageView headerBackButton;
    private TextView headerTitle;
    private ListView contentList;
    private SettingsListAdapter listAdapter;

    public SecurityController(@NonNull View mainView, @NonNull Activity parentActivity, @NonNull ControllerKind kind) {
        super(mainView, parentActivity, kind);

        this.headerBackButton = (ImageView) this.currentView.findViewById(R.id.header_item_left);
        this.headerTitle = (TextView) this.currentView.findViewById(R.id.header_title);
        this.contentList = (ListView) this.currentView.findViewById(R.id.settings_security_list);

        this.headerTitle.setTypeface(CustomFonts.customTitleExtraLight(this.parentActivity));

        if (currentKind == ControllerKind.FRAGMENT_CONTROLLER)
            this.headerBackButton.setImageDrawable(this.parentActivity.getResources().getDrawable(R.drawable.nav_back));

        this.headerBackButton.setOnClickListener(view -> {
            if (currentKind == ControllerKind.ACTIVITY_CONTROLLER) {
                parentActivity.finish();
                parentActivity.overridePendingTransition(android.R.anim.fade_in, R.anim.slide_down);
            } else {
                ((HomeActivity)this.parentActivity).popFragmentInCurrentTab();
            }
        });

        List<SettingsListItem> itemList = new ArrayList<>();

        itemList.add(new SettingsListItem(this.parentActivity.getResources().getString(R.string.SETTINGS_CODE), (parent, view, position, id) -> {
                Intent intent = new Intent(this.parentActivity, AuthenticationActivity.class);
                intent.putExtra("changeSecureCode", true);
                this.parentActivity.startActivity(intent);
                this.parentActivity.overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left);
        }));

        itemList.add(new SettingsListItem(this.parentActivity.getResources().getString(R.string.SETTINGS_PASSWORD), (parent, view, position, id) -> {
            if (currentKind == ControllerKind.ACTIVITY_CONTROLLER) {
                Intent intent = new Intent(this.parentActivity, PasswordSettingsActivity.class);
                this.parentActivity.startActivity(intent);
                this.parentActivity.overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left);
            } else {
                ((HomeActivity)this.parentActivity).pushFragmentInCurrentTab(new NotifsSettingsFragment());
            }
        }));

        this.listAdapter = new SettingsListAdapter(this.parentActivity, itemList, this.contentList);
    }

    @Override
    public void onBackPressed() {
        this.headerBackButton.performClick();
    }
}
