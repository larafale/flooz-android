package me.flooz.app.UI.Activity.Settings;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Map;

import info.hoang8f.android.segmented.SegmentedGroup;
import me.flooz.app.App.FloozApplication;
import me.flooz.app.Model.FLTransaction;
import me.flooz.app.Network.FloozRestClient;
import me.flooz.app.R;
import me.flooz.app.Utils.CustomFonts;

/**
 * Created by Flooz on 3/10/15.
 */
public class PrivacySettingsActivity extends Activity {

    private PrivacySettingsActivity instance;
    private FloozApplication floozApp;
    private Boolean modal;

    private ImageView headerBackButton;
    private TextView headerTitle;
    private TextView infosText;
    private SegmentedGroup segmentedGroup;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.instance = this;
        this.floozApp = (FloozApplication) this.getApplicationContext();
        this.modal = getIntent().getBooleanExtra("modal", false);

        this.setContentView(R.layout.settings_privacy_fragment);

        this.headerBackButton = (ImageView) this.findViewById(R.id.settings_privacy_header_back);
        this.headerTitle = (TextView) this.findViewById(R.id.settings_privacy_header_title);
        this.infosText = (TextView) this.findViewById(R.id.settings_privacy_infos);
        this.segmentedGroup = (SegmentedGroup) this.findViewById(R.id.settings_privacy_segment);

        this.headerTitle.setTypeface(CustomFonts.customTitleExtraLight(this));
        this.infosText.setTypeface(CustomFonts.customContentRegular(this));

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

        this.segmentedGroup.setTintColor(this.getResources().getColor(R.color.blue));

        String scopeString = (String)((Map<String, Object>) FloozRestClient.getInstance().currentUser.settings.get("def")).get("scope");
        FLTransaction.TransactionScope scope = FLTransaction.transactionScopeParamToEnum(scopeString);

        switch (scope) {
            case TransactionScopePublic:
                this.segmentedGroup.check(R.id.settings_privacy_segment_public);
                break;
            case TransactionScopeFriend:
                this.segmentedGroup.check(R.id.settings_privacy_segment_friend);
                break;
            case TransactionScopePrivate:
                this.segmentedGroup.check(R.id.settings_privacy_segment_private);
                break;
        }

        this.segmentedGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {

                Map<String, Object> settings = new HashMap<>(FloozRestClient.getInstance().currentUser.settings);
                Map<String, Object> def = (Map<String, Object>)settings.get("def");

                switch(checkedId) {
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
            }
        });
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
