package flooz.android.com.flooz.UI.Fragment.Home.Settings;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Map;

import flooz.android.com.flooz.Model.FLTransaction;
import flooz.android.com.flooz.Network.FloozRestClient;
import flooz.android.com.flooz.R;
import flooz.android.com.flooz.UI.Fragment.Home.HomeBaseFragment;
import flooz.android.com.flooz.Utils.CustomFonts;
import info.hoang8f.android.segmented.SegmentedGroup;

/**
 * Created by Flooz on 12/15/14.
 */
public class PrivacySettingsFragment extends HomeBaseFragment {

    private ImageView headerBackButton;
    private TextView headerTitle;
    private TextView infosText;
    private SegmentedGroup segmentedGroup;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.settings_privacy_fragment, null);

        this.headerBackButton = (ImageView) view.findViewById(R.id.settings_privacy_header_back);
        this.headerTitle = (TextView) view.findViewById(R.id.settings_privacy_header_title);
        this.infosText = (TextView) view.findViewById(R.id.settings_privacy_infos);
        this.segmentedGroup = (SegmentedGroup) view.findViewById(R.id.settings_privacy_segment);

        this.headerTitle.setTypeface(CustomFonts.customTitleExtraLight(inflater.getContext()));
        this.infosText.setTypeface(CustomFonts.customContentRegular(inflater.getContext()));

        this.headerBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                parentActivity.popMainFragment(R.animator.slide_out_left, R.animator.slide_in_right);
            }
        });

        this.segmentedGroup.setTintColor(inflater.getContext().getResources().getColor(R.color.blue));

        String scopeString = (String)((Map<String, Object>)FloozRestClient.getInstance().currentUser.settings.get("def")).get("scope");
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

        return view;
    }

    @Override
    public void onBackPressed() {
        this.headerBackButton.performClick();
    }
}