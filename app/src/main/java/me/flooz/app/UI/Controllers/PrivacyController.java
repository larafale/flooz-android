package me.flooz.app.UI.Controllers;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Map;

import info.hoang8f.android.segmented.SegmentedGroup;
import me.flooz.app.Model.FLTransaction;
import me.flooz.app.Network.FloozRestClient;
import me.flooz.app.R;
import me.flooz.app.UI.Activity.HomeActivity;
import me.flooz.app.Utils.CustomFonts;

/**
 * Created by Flooz on 9/2/15.
 */
public class PrivacyController extends BaseController {

    private ImageView headerBackButton;

    public PrivacyController(@NonNull View mainView, @NonNull Activity parentActivity, @NonNull ControllerKind kind) {
        super(mainView, parentActivity, kind);

        this.headerBackButton = (ImageView) this.currentView.findViewById(R.id.header_item_left);
        TextView headerTitle = (TextView) this.currentView.findViewById(R.id.header_title);
        TextView infosText = (TextView) this.currentView.findViewById(R.id.settings_privacy_infos);
        SegmentedGroup segmentedGroup = (SegmentedGroup) this.currentView.findViewById(R.id.settings_privacy_segment);

        headerTitle.setTypeface(CustomFonts.customTitleExtraLight(this.parentActivity));
        infosText.setTypeface(CustomFonts.customContentRegular(this.parentActivity));

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

        segmentedGroup.setTintColor(this.parentActivity.getResources().getColor(R.color.blue));

        String scopeString = (String)((Map<String, Object>) FloozRestClient.getInstance().currentUser.settings.get("def")).get("scope");
        FLTransaction.TransactionScope scope = FLTransaction.transactionScopeParamToEnum(scopeString);

        switch (scope) {
            case TransactionScopePublic:
                segmentedGroup.check(R.id.settings_privacy_segment_public);
                break;
            case TransactionScopeFriend:
                segmentedGroup.check(R.id.settings_privacy_segment_friend);
                break;
            case TransactionScopePrivate:
                segmentedGroup.check(R.id.settings_privacy_segment_private);
                break;
        }

        segmentedGroup.setOnCheckedChangeListener((group, checkedId) -> {

            Map<String, Object> settings = new HashMap<>(FloozRestClient.getInstance().currentUser.settings);
            Map<String, Object> def = (Map<String, Object>) settings.get("def");

            switch (checkedId) {
                case R.id.settings_privacy_segment_public:
                    def.put("scope", FLTransaction.transactionScopeToParams(FLTransaction.TransactionScope.TransactionScopePublic));
                    break;
                case R.id.settings_privacy_segment_friend:
                    def.put("scope", FLTransaction.transactionScopeToParams(FLTransaction.TransactionScope.TransactionScopeFriend));
                    break;
                case R.id.settings_privacy_segment_private:
                    def.put("scope", FLTransaction.transactionScopeToParams(FLTransaction.TransactionScope.TransactionScopePrivate));
                    break;
                default:
                    break;
            }

            Map<String, Object> params = new HashMap<>();
            params.put("settings", settings);

            FloozRestClient.getInstance().updateUser(params, null);
        });
    }

    @Override
    public void onBackPressed() {
        this.headerBackButton.performClick();
    }
}
