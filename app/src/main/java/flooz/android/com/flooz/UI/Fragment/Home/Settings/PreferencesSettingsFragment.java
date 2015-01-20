package flooz.android.com.flooz.UI.Fragment.Home.Settings;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.gc.materialdesign.views.Switch;

import flooz.android.com.flooz.Network.FloozRestClient;
import flooz.android.com.flooz.R;
import flooz.android.com.flooz.UI.Fragment.Home.HomeBaseFragment;
import flooz.android.com.flooz.Utils.CustomFonts;

/**
 * Created by Flooz on 12/15/14.
 */
public class PreferencesSettingsFragment extends HomeBaseFragment {

    private ImageView headerBackButton;
    private TextView headerTitle;
    private TextView fbText;
    private TextView notifText;
    private Switch fbSwitch;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.settings_preferences_fragment, null);

        this.headerBackButton = (ImageView) view.findViewById(R.id.settings_preferences_header_back);
        this.headerTitle = (TextView) view.findViewById(R.id.settings_preferences_header_title);
        this.fbText = (TextView) view.findViewById(R.id.settings_preferences_fb_text);
        this.notifText = (TextView) view.findViewById(R.id.settings_preferences_notif_text);
        this.fbSwitch = (Switch) view.findViewById(R.id.settings_preferences_fb_toggle);

        this.headerTitle.setTypeface(CustomFonts.customTitleExtraLight(inflater.getContext()));
        this.notifText.setTypeface(CustomFonts.customTitleExtraLight(inflater.getContext()));
        this.fbText.setTypeface(CustomFonts.customTitleExtraLight(inflater.getContext()));

        view.findViewById(R.id.settings_preferences_notif).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                parentActivity.pushMainFragment("settings_notifications", R.animator.slide_in_left, R.animator.slide_out_right);
            }
        });

        if (FloozRestClient.getInstance().isConnectedToFacebook())
            this.fbSwitch.setChecked(true);
        else
            this.fbSwitch.setChecked(false);

        this.fbSwitch.setOncheckListener(new Switch.OnCheckListener() {
            @Override
            public void onCheck(boolean check) {
                if (check) {
                    FloozRestClient.getInstance().connectFacebook();
                } else {
                    FloozRestClient.getInstance().disconnectFacebook();
                }
            }
        });

        this.headerBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (parentActivity != null)
                    parentActivity.popMainFragment(R.animator.slide_out_left, R.animator.slide_in_right);
            }
        });

        return view;
    }

    @Override
    public void onBackPressed() {
        this.headerBackButton.performClick();
    }
}
