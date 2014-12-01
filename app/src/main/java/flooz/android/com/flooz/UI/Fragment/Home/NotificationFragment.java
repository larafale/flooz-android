package flooz.android.com.flooz.UI.Fragment.Home;

import android.app.Fragment;
import android.app.Notification;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;

import java.util.List;
import java.util.Map;

import flooz.android.com.flooz.Adapter.NotificationListAdapter;
import flooz.android.com.flooz.Adapter.SettingsListAdapter;
import flooz.android.com.flooz.Model.FLError;
import flooz.android.com.flooz.Model.FLTransaction;
import flooz.android.com.flooz.Network.FloozHttpResponseHandler;
import flooz.android.com.flooz.Network.FloozRestClient;
import flooz.android.com.flooz.R;
import flooz.android.com.flooz.UI.Activity.HomeActivity;
import flooz.android.com.flooz.Utils.CustomFonts;

/**
 * Created by Flooz on 10/15/14.
 */
public class NotificationFragment extends Fragment {

    public HomeActivity parentActivity;

    private ImageView headerBackButton;
    private TextView headerTitle;
    private PullToRefreshListView contentList;
    private NotificationListAdapter listAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.notification_fragment, null);

        this.headerBackButton = (ImageView) view.findViewById(R.id.notification_header_back);
        this.headerTitle = (TextView) view.findViewById(R.id.notification_header_title);
        this.contentList = (PullToRefreshListView) view.findViewById(R.id.notification_list);

        this.headerTitle.setTypeface(CustomFonts.customTitleExtraLight(inflater.getContext()));

        this.headerBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FloozRestClient.getInstance().readAllNotifications(null);
                if (parentActivity != null)
                    parentActivity.popMainFragment(R.animator.slide_down, android.R.animator.fade_in);
            }
        });

        this.listAdapter = new NotificationListAdapter(inflater.getContext());
        this.contentList.setAdapter(this.listAdapter);

        this.contentList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

            }
        });

        this.contentList.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener<ListView>() {
            @Override
            public void onRefresh(PullToRefreshBase<ListView> refreshView) {
                FloozRestClient.getInstance().updateNotificationFeed(new FloozHttpResponseHandler() {
                    @Override
                    public void success(Object response) {
                        contentList.onRefreshComplete();
                    }

                    @Override
                    public void failure(int statusCode, FLError error) {
                        contentList.onRefreshComplete();
                    }
                });
            }
        });

        this.contentList.setOnLastItemVisibleListener(new PullToRefreshBase.OnLastItemVisibleListener() {
            @Override
            public void onLastItemVisible() {
                loadNextPage();
            }
        });

        return view;
    }

    private void loadNextPage() {
        FloozRestClient.getInstance().loadNextNotfificationFeed(null);
    }

}

