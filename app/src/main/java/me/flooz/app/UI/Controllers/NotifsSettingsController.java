package me.flooz.app.UI.Controllers;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import me.flooz.app.Adapter.NotificationSettingsListAdapter;
import me.flooz.app.R;
import me.flooz.app.UI.Activity.HomeActivity;
import me.flooz.app.Utils.CustomFonts;
import se.emilsjolander.stickylistheaders.StickyListHeadersListView;

/**
 * Created by Flooz on 9/2/15.
 */
public class NotifsSettingsController extends BaseController {

    private ImageView headerBackButton;

    public NotifsSettingsController(@NonNull View mainView, @NonNull Activity parentActivity, @NonNull ControllerKind kind) {
        super(mainView, parentActivity, kind);

        this.headerBackButton = (ImageView) this.currentView.findViewById(R.id.header_item_left);
        TextView headerTitle = (TextView) this.currentView.findViewById(R.id.header_title);
        StickyListHeadersListView contentList = (StickyListHeadersListView) this.currentView.findViewById(R.id.settings_notifications_list);

        headerTitle.setTypeface(CustomFonts.customTitleExtraLight(this.parentActivity));

        if (currentKind == ControllerKind.FRAGMENT_CONTROLLER)
            this.headerBackButton.setImageDrawable(this.parentActivity.getResources().getDrawable(R.drawable.nav_back));

        this.headerBackButton.setOnClickListener(view -> {
            if (currentKind == ControllerKind.ACTIVITY_CONTROLLER) {
                parentActivity.finish();
                parentActivity.overridePendingTransition(android.R.anim.fade_in, R.anim.slide_down);
            } else {
                ((HomeActivity)this.parentActivity).popFragmentInCurrentTab(R.animator.slide_in_right, R.animator.slide_out_left);
            }
        });

        NotificationSettingsListAdapter listAdapter = new NotificationSettingsListAdapter(this.parentActivity);

        contentList.setAdapter(listAdapter);
    }

    @Override
    public void onBackPressed() {
        this.headerBackButton.performClick();
    }

}
