package me.flooz.app.UI.Controllers;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import info.hoang8f.android.segmented.SegmentedGroup;
import me.flooz.app.Model.FLScope;
import me.flooz.app.Model.FLTransaction;
import me.flooz.app.Network.FloozRestClient;
import me.flooz.app.R;
import me.flooz.app.UI.Activity.HomeActivity;
import me.flooz.app.Utils.CustomFonts;

/**
 * Created by Flooz on 9/2/15.
 */
public class PrivacyController extends BaseController {

    public PrivacyController(@NonNull View mainView, @NonNull Activity parentActivity, @NonNull ControllerKind kind) {
        super(mainView, parentActivity, kind);

        this.init();
    }

    public PrivacyController(@NonNull View mainView, @NonNull Activity parentActivity, @NonNull ControllerKind kind, @Nullable JSONObject data) {
        super(mainView, parentActivity, kind, data);

        this.init();
    }

    @Override
    protected void init() {
        super.init();

        TextView infosText = (TextView) this.currentView.findViewById(R.id.settings_privacy_infos);
        SegmentedGroup segmentedGroup = (SegmentedGroup) this.currentView.findViewById(R.id.settings_privacy_segment);

        infosText.setTypeface(CustomFonts.customContentRegular(this.parentActivity));

        segmentedGroup.setTintColor(this.parentActivity.getResources().getColor(R.color.blue));

        final List<FLScope> scopes = FLScope.defaultScopeList();
        String scopeString = (String)((Map<String, Object>) FloozRestClient.getInstance().currentUser.settings.get("def")).get("scope");

        int i = 0;
        for (FLScope scope: scopes) {
            RadioButton rB = new RadioButton(this.parentActivity, null, R.style.RadioButton);
            rB.setId(i);
            LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            p.weight = 1;

            rB.setLayoutParams(p);
            rB.setText(scope.shortDesc);

            segmentedGroup.addView(rB);

            if (scopeString != null && scope.keyString.equals(scopeString)) {
                segmentedGroup.check(i);
            }

            i++;
        }

        segmentedGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                Map<String, Object> settings = new HashMap<>(FloozRestClient.getInstance().currentUser.settings);
                Map<String, Object> def = (Map<String, Object>) settings.get("def");

                def.put("scope", scopes.get(checkedId).keyString);

                Map<String, Object> params = new HashMap<>();
                params.put("settings", settings);

                FloozRestClient.getInstance().updateUser(params, null);
            }
        });
    }
}
