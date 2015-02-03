package flooz.android.com.flooz.UI.Fragment.Home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.baoyz.widget.PullRefreshLayout;

import flooz.android.com.flooz.Adapter.NotificationListAdapter;
import flooz.android.com.flooz.Model.FLError;
import flooz.android.com.flooz.Model.FLNotification;
import flooz.android.com.flooz.Network.FloozHttpResponseHandler;
import flooz.android.com.flooz.Network.FloozRestClient;
import flooz.android.com.flooz.R;
import flooz.android.com.flooz.Utils.CustomFonts;

/**
 * Created by Flooz on 10/15/14.
 */
public class NotificationFragment extends HomeBaseFragment {

    private ImageView headerBackButton;
    private TextView headerTitle;
    private ListView contentList;
    private PullRefreshLayout contentContainer;
    private NotificationListAdapter listAdapter;

    public boolean showCross = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.notification_fragment, null);

        this.headerBackButton = (ImageView) view.findViewById(R.id.notification_header_back);
        this.headerTitle = (TextView) view.findViewById(R.id.notification_header_title);
        this.contentList = (ListView) view.findViewById(R.id.notification_list);
        this.contentContainer = (PullRefreshLayout) view.findViewById(R.id.notification_container);

        this.headerTitle.setTypeface(CustomFonts.customTitleExtraLight(inflater.getContext()));

        if (this.showCross)
            this.headerBackButton.setImageResource(R.drawable.nav_cross);
        else
            this.headerBackButton.setImageResource(R.drawable.nav_account_button);

        this.headerBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FloozRestClient.getInstance().readAllNotifications(null);
                parentActivity.popMainFragment(R.animator.slide_down, android.R.animator.fade_in);
                showCross = false;
            }
        });

        this.listAdapter = new NotificationListAdapter(inflater.getContext());
        this.contentList.setAdapter(this.listAdapter);

        this.contentContainer.setOnRefreshListener(new PullRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                FloozRestClient.getInstance().updateNotificationFeed(new FloozHttpResponseHandler() {
                    @Override
                    public void success(Object response) {
                        contentContainer.setRefreshing(false);
                    }

                    @Override
                    public void failure(int statusCode, FLError error) {
                        contentContainer.setRefreshing(false);
                    }
                });
            }
        });

        this.contentList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                FLNotification notif = (FLNotification)listAdapter.getItem(position);
                notif.isRead = true;
                for (int i = 0; i < notif.triggers.size(); i++) {
                    FloozRestClient.getInstance().handleTrigger(notif.triggers.get(i));
                }
                listAdapter.notifyDataSetChanged();
            }
        });

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        FloozRestClient.getInstance().updateNotificationFeed(null);
    }

    @Override
    public void onResume() {
        super.onResume();
        FloozRestClient.getInstance().updateNotificationFeed(null);
    }

    @Override
    public void onBackPressed() {
        this.headerBackButton.performClick();
    }
}

