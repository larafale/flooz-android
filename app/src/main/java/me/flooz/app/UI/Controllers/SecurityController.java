package me.flooz.app.UI.Controllers;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONObject;

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

    private ListView contentList;
    private SettingsListAdapter listAdapter;

    public SecurityController(@NonNull View mainView, @NonNull Activity parentActivity, @NonNull ControllerKind kind) {
        super(mainView, parentActivity, kind);

        this.init();
    }

    public SecurityController(@NonNull View mainView, @NonNull Activity parentActivity, @NonNull ControllerKind kind, @Nullable JSONObject data) {
        super(mainView, parentActivity, kind, data);

        this.init();
    }

    @Override
    protected void init() {
        super.init();

        this.contentList = (ListView) this.currentView.findViewById(R.id.settings_security_list);

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
}
