package flooz.android.com.flooz.UI.Fragment.Home.Settings;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

import flooz.android.com.flooz.Adapter.SettingsListAdapter;
import flooz.android.com.flooz.Adapter.SettingsListItem;
import flooz.android.com.flooz.Network.FloozRestClient;
import flooz.android.com.flooz.R;
import flooz.android.com.flooz.UI.Fragment.Home.HomeBaseFragment;
import flooz.android.com.flooz.Utils.CustomFonts;
import flooz.android.com.flooz.Utils.JSONHelper;

/**
 * Created by Flooz on 12/15/14.
 */
public class SecuritySettingsFragment extends HomeBaseFragment {

    private ImageView headerBackButton;
    private TextView headerTitle;
    private ListView contentList;
    private SettingsListAdapter listAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.settings_security_fragment, null);

        this.headerBackButton = (ImageView) view.findViewById(R.id.settings_security_header_back);
        this.headerTitle = (TextView) view.findViewById(R.id.settings_security_header_title);
        this.contentList = (ListView) view.findViewById(R.id.settings_security_list);

        this.headerTitle.setTypeface(CustomFonts.customTitleExtraLight(inflater.getContext()));

        this.headerBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                parentActivity.popMainFragment(R.animator.slide_out_left, R.animator.slide_in_right);
            }
        });

        List<SettingsListItem> itemList = new ArrayList<>();

        itemList.add(new SettingsListItem(inflater.getContext().getResources().getString(R.string.SETTINGS_CODE), new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                parentActivity.pushMainFragment("settings_secure_code", R.animator.slide_in_left, R.animator.slide_out_right);
            }
        }));

        itemList.add(new SettingsListItem(inflater.getContext().getResources().getString(R.string.SETTINGS_PASSWORD), new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                parentActivity.pushMainFragment("settings_password", R.animator.slide_in_left, R.animator.slide_out_right);
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
