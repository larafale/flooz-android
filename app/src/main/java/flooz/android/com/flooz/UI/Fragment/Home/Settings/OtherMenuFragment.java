package flooz.android.com.flooz.UI.Fragment.Home.Settings;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.content.Intent;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ImageView;
import android.widget.AdapterView;
import android.view.LayoutInflater;

import com.facebook.Settings;

import java.util.List;
import java.util.ArrayList;

import flooz.android.com.flooz.Adapter.SettingsListItem;
import flooz.android.com.flooz.R;
import flooz.android.com.flooz.Utils.CustomFonts;
import flooz.android.com.flooz.Network.FloozRestClient;
import flooz.android.com.flooz.Adapter.SettingsListAdapter;
import flooz.android.com.flooz.UI.Fragment.Home.HomeBaseFragment;
import flooz.android.com.flooz.UI.Fragment.Home.CustomWebViewFragment;

/**
 * Created by Flooz on 12/8/14.
 */
public class OtherMenuFragment extends HomeBaseFragment {

    private ImageView headerBackButton;
    private TextView headerTitle;
    private ListView contentList;
    private SettingsListAdapter listAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.other_menu_fragment, null);

        this.headerBackButton = (ImageView) view.findViewById(R.id.other_menu_header_back);
        this.headerTitle = (TextView) view.findViewById(R.id.other_menu_header_title);
        this.contentList = (ListView) view.findViewById(R.id.other_menu_list);

        this.headerTitle.setTypeface(CustomFonts.customTitleExtraLight(inflater.getContext()));

        this.headerBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (parentActivity != null)
                    parentActivity.popMainFragment(R.animator.slide_down, android.R.animator.fade_in);
            }
        });

        final List<SettingsListItem> list = new ArrayList<>();
        list.add(new SettingsListItem(inflater.getContext().getResources().getString(R.string.INFORMATIONS_FAQ), new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ((CustomWebViewFragment)parentActivity.contentFragments.get("custom_webview")).title = list.get(position).getTitle();
                ((CustomWebViewFragment)parentActivity.contentFragments.get("custom_webview")).url = "https://www.flooz.me/faq?layout=webview";
                parentActivity.pushMainFragment("custom_webview", R.animator.slide_in_left, R.animator.slide_out_right);
            }
        }));
        list.add(new SettingsListItem(inflater.getContext().getResources().getString(R.string.INFORMATIONS_TERMS), new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ((CustomWebViewFragment)parentActivity.contentFragments.get("custom_webview")).title = list.get(position).getTitle();
                ((CustomWebViewFragment)parentActivity.contentFragments.get("custom_webview")).url = "https://www.flooz.me/cgu?layout=webview";
                parentActivity.pushMainFragment("custom_webview", R.animator.slide_in_left, R.animator.slide_out_right);
            }
        }));
        list.add(new SettingsListItem(inflater.getContext().getResources().getString(R.string.INFORMATIONS_CONTACT), new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ((CustomWebViewFragment)parentActivity.contentFragments.get("custom_webview")).title = list.get(position).getTitle();
                ((CustomWebViewFragment)parentActivity.contentFragments.get("custom_webview")).url = "https://www.flooz.me/contact?layout=webview";
                parentActivity.pushMainFragment("custom_webview", R.animator.slide_in_left, R.animator.slide_out_right);
            }
        }));
        list.add(new SettingsListItem(inflater.getContext().getResources().getString(R.string.INFORMATIONS_REVIEW), new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = CustomWebViewFragment.newEmailIntent("hello@flooz.me", list.get(position).getTitle(), "Voici quelques idées pour améliorer l'application : ", "");
                parentActivity.startActivity(intent);
            }
        }));
        list.add(new SettingsListItem(inflater.getContext().getResources().getString(R.string.SETTINGS_LOGOUT), new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                FloozRestClient.getInstance().logout();
            }
        }));

        this.listAdapter = new SettingsListAdapter(inflater.getContext(), list, this.contentList);

        return view;
    }

    @Override
    public void onBackPressed() {
        this.headerBackButton.performClick();
    }
}
