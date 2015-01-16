package flooz.android.com.flooz.UI.Fragment.Home.Settings;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import flooz.android.com.flooz.Adapter.SettingsListAdapter;
import flooz.android.com.flooz.R;
import flooz.android.com.flooz.UI.Activity.HomeActivity;
import flooz.android.com.flooz.Utils.CustomFonts;

/**
 * Created by Flooz on 10/15/14.
 */
public class ProfileSettingsFragment extends Fragment {

    public HomeActivity parentActivity;

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
        View view = inflater.inflate(R.layout.profile_settings, null);

        this.headerBackButton = (ImageView) view.findViewById(R.id.profile_settings_header_back);
        this.headerTitle = (TextView) view.findViewById(R.id.profile_settings_header_title);
        this.contentList = (ListView) view.findViewById(R.id.profile_settings_list);

        this.headerTitle.setTypeface(CustomFonts.customTitleExtraLight(inflater.getContext()));

        this.headerBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (parentActivity != null)
                    parentActivity.popMainFragment(R.animator.slide_down, android.R.animator.fade_in);
            }
        });

        List<String> list = new ArrayList<String>();
        list.add(inflater.getContext().getResources().getString(R.string.SETTINGS_CARD));
        list.add(inflater.getContext().getResources().getString(R.string.SETTINGS_RIB));
        list.add(inflater.getContext().getResources().getString(R.string.SETTINGS_IDENTITY));
        list.add(inflater.getContext().getResources().getString(R.string.SETTINGS_COORD));
        list.add(inflater.getContext().getResources().getString(R.string.SETTINGS_SECURITY));
        list.add(inflater.getContext().getResources().getString(R.string.SETTINGS_PREFERENCES));

        this.listAdapter = new SettingsListAdapter(inflater.getContext(), list);

        this.contentList.setAdapter(this.listAdapter);

        this.contentList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

            }
        });

        return view;
    }
}