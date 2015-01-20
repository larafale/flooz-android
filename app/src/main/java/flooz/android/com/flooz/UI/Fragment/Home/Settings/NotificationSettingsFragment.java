package flooz.android.com.flooz.UI.Fragment.Home.Settings;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import flooz.android.com.flooz.Adapter.NotificationSettingsListAdapter;
import flooz.android.com.flooz.Adapter.SettingsListAdapter;
import flooz.android.com.flooz.R;
import flooz.android.com.flooz.UI.Fragment.Home.HomeBaseFragment;
import flooz.android.com.flooz.Utils.CustomFonts;
import se.emilsjolander.stickylistheaders.StickyListHeadersListView;

/**
 * Created by Flooz on 12/15/14.
 */
public class NotificationSettingsFragment extends HomeBaseFragment {

    private ImageView headerBackButton;
    private TextView headerTitle;
    private StickyListHeadersListView contentList;
    private NotificationSettingsListAdapter listAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.settings_notifications_fragment, null);

        this.headerBackButton = (ImageView) view.findViewById(R.id.settings_notifications_header_back);
        this.headerTitle = (TextView) view.findViewById(R.id.settings_notifications_header_title);
        this.contentList = (StickyListHeadersListView) view.findViewById(R.id.settings_notifications_list);

        this.headerTitle.setTypeface(CustomFonts.customTitleExtraLight(inflater.getContext()));

        this.headerBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (parentActivity != null)
                    parentActivity.popMainFragment(R.animator.slide_out_left, R.animator.slide_in_right);
            }
        });

        this.listAdapter = new NotificationSettingsListAdapter(inflater.getContext());

        this.contentList.setAdapter(this.listAdapter);

        return view;
    }

    @Override
    public void onBackPressed() {
        this.headerBackButton.performClick();
    }
}
