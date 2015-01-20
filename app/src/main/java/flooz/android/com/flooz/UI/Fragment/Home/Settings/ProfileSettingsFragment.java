package flooz.android.com.flooz.UI.Fragment.Home.Settings;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.facebook.Settings;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import flooz.android.com.flooz.Adapter.SettingsListAdapter;
import flooz.android.com.flooz.Adapter.SettingsListItem;
import flooz.android.com.flooz.Network.FloozRestClient;
import flooz.android.com.flooz.R;
import flooz.android.com.flooz.UI.Activity.HomeActivity;
import flooz.android.com.flooz.UI.Fragment.Home.HomeBaseFragment;
import flooz.android.com.flooz.Utils.CustomFonts;
import flooz.android.com.flooz.Utils.JSONHelper;

/**
 * Created by Flooz on 10/15/14.
 */
public class ProfileSettingsFragment extends HomeBaseFragment {

    private ImageView headerBackButton;
    private TextView headerTitle;
    private ListView contentList;
    private SettingsListAdapter listAdapter;
    private LinearLayout infosContainer;
    private TextView infosText;
    private TextView infosNotifsText;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.profile_settings, null);

        this.headerBackButton = (ImageView) view.findViewById(R.id.profile_settings_header_back);
        this.headerTitle = (TextView) view.findViewById(R.id.profile_settings_header_title);
        this.contentList = (ListView) view.findViewById(R.id.profile_settings_list);
        this.infosContainer = (LinearLayout) view.findViewById(R.id.profile_settings_infos);
        this.infosText = (TextView) view.findViewById(R.id.profile_settings_infos_text);
        this.infosNotifsText = (TextView) view.findViewById(R.id.profile_settings_infos_notifs_text);

        this.headerTitle.setTypeface(CustomFonts.customTitleExtraLight(inflater.getContext()));
        this.infosText.setTypeface(CustomFonts.customTitleLight(inflater.getContext()));
        this.infosNotifsText.setTypeface(CustomFonts.customContentBold(inflater.getContext()));

        this.headerBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (parentActivity != null)
                    parentActivity.popMainFragment(R.animator.slide_down, android.R.animator.fade_in);
            }
        });

        int cardNotif = 0;
        int bankNotif = 0;
        int indentityNotif = 0;
        int coordsNotif = 0;

        List<SettingsListItem> itemList = new ArrayList<>();

        if (FloozRestClient.getInstance().currentUser.creditCard == null || FloozRestClient.getInstance().currentUser.creditCard.cardId == null || FloozRestClient.getInstance().currentUser.creditCard.cardId.isEmpty())
            ++cardNotif;

        List missingFields;
        try {
            missingFields = JSONHelper.toList(FloozRestClient.getInstance().currentUser.json.optJSONArray("missingFields"));

            if (missingFields.contains("iban"))
                ++bankNotif;
            if (missingFields.contains("cniRecto"))
                ++indentityNotif;
            if (missingFields.contains("cniVerso"))
                ++indentityNotif;
            if (missingFields.contains("justificatory"))
                ++coordsNotif;
            if (missingFields.contains("address"))
                ++coordsNotif;
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if (bankNotif + indentityNotif + coordsNotif + bankNotif > 0)
            this.infosContainer.setVisibility(View.VISIBLE);
        else
            this.infosContainer.setVisibility(View.GONE);

        itemList.add(new SettingsListItem(inflater.getContext().getResources().getString(R.string.SETTINGS_CARD), cardNotif, new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                parentActivity.pushMainFragment("settings_credit_card", R.animator.slide_in_left, R.animator.slide_out_right);
            }
        }));

        itemList.add(new SettingsListItem(inflater.getContext().getResources().getString(R.string.SETTINGS_RIB), bankNotif, new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                parentActivity.pushMainFragment("settings_bank", R.animator.slide_in_left, R.animator.slide_out_right);
            }
        }));

        itemList.add(new SettingsListItem(inflater.getContext().getResources().getString(R.string.SETTINGS_IDENTITY), indentityNotif, new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                parentActivity.pushMainFragment("settings_identity", R.animator.slide_in_left, R.animator.slide_out_right);
            }
        }));

        itemList.add(new SettingsListItem(inflater.getContext().getResources().getString(R.string.SETTINGS_COORD), coordsNotif, new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                parentActivity.pushMainFragment("settings_coords", R.animator.slide_in_left, R.animator.slide_out_right);
            }
        }));

        itemList.add(new SettingsListItem(inflater.getContext().getResources().getString(R.string.SETTINGS_SECURITY), new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                parentActivity.pushMainFragment("settings_security", R.animator.slide_in_left, R.animator.slide_out_right);
            }
        }));

        itemList.add(new SettingsListItem(inflater.getContext().getResources().getString(R.string.SETTINGS_PREFERENCES), new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                parentActivity.pushMainFragment("settings_preferences", R.animator.slide_in_left, R.animator.slide_out_right);
            }
        }));

        itemList.add(new SettingsListItem(inflater.getContext().getResources().getString(R.string.SETTINGS_PRIVACY), new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                parentActivity.pushMainFragment("settings_privacy", R.animator.slide_in_left, R.animator.slide_out_right);
            }
        }));

        this.listAdapter = new SettingsListAdapter(inflater.getContext(), itemList, this.contentList);

        return view;
    }

    @Override
    public void onBackPressed() {
        this.headerBackButton.performClick();
    }
}