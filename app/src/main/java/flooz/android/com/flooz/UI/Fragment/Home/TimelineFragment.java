package flooz.android.com.flooz.UI.Fragment.Home;

import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import flooz.android.com.flooz.Adapter.TimelineListAdapter;
import flooz.android.com.flooz.App.FloozApplication;
import flooz.android.com.flooz.Model.FLError;
import flooz.android.com.flooz.Model.FLTransaction;
import flooz.android.com.flooz.Network.FloozHttpResponseHandler;
import flooz.android.com.flooz.Network.FloozRestClient;
import flooz.android.com.flooz.R;
import flooz.android.com.flooz.Utils.CustomNotificationIntents;

/**
 * Created by Flooz on 9/23/14.
 */
public class TimelineFragment extends Fragment implements TimelineListAdapter.TimelineListRowDelegate {

    public interface TimelineFragmentDelegate {
        public void onItemSelected(FLTransaction transac);
        public void onItemCommentSelected(FLTransaction transac);
        public void onItemImageSelected(String imgUrl);
    }

    public TimelineFragmentDelegate delegate;
    public PullToRefreshListView timelineListView;
    private TimelineListAdapter timelineAdapter;

    public List<FLTransaction> transactions = null;
    public FLTransaction.TransactionScope currentScope;

    private String nextPageUrl;

    private BroadcastReceiver reloadTimelineReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            refreshTransactions();
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        LocalBroadcastManager.getInstance(FloozApplication.getAppContext()).registerReceiver(reloadTimelineReceiver,
                CustomNotificationIntents.filterReloadTimeline());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.timeline_fragment, null);

        this.timelineListView = (PullToRefreshListView) view.findViewById(R.id.timeline_list);
        this.timelineListView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener<ListView>() {
            @Override
            public void onRefresh(PullToRefreshBase<ListView> refreshView) {
                refreshTransactions();
            }
        });
        this.timelineListView.setOnLastItemVisibleListener(new PullToRefreshBase.OnLastItemVisibleListener() {
            @Override
            public void onLastItemVisible() {
                loadNextPage();
            }
        });

        if (this.transactions == null)
            this.transactions = new ArrayList<FLTransaction>(0);

        this.timelineAdapter = new TimelineListAdapter(FloozApplication.getAppContext(), this.transactions);
        this.timelineAdapter.delegate = this;
        this.timelineListView.setAdapter(this.timelineAdapter);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        this.refreshTransactions();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);

    }

    @Override
    public void ListItemClick(FLTransaction transac) {
        if (delegate != null)
            delegate.onItemSelected(transac);
    }

    @Override
    public void ListItemCommentClick(FLTransaction transac) {
        if (delegate != null)
            delegate.onItemCommentSelected(transac);
    }

    @Override
    public void ListItemImageClick(String imgUrl) {
        if (delegate != null)
            delegate.onItemImageSelected(imgUrl);
    }

    private void loadNextPage() {
        if (nextPageUrl.isEmpty())
            return;

        FloozRestClient.getInstance().timelineNextPage(this.nextPageUrl, new FloozHttpResponseHandler() {
            @Override
            public void success(Object response) {
                Map<String, Object> responseMap = (Map<String, Object>) response;

                transactions.addAll((List<FLTransaction>) responseMap.get("transactions"));
                nextPageUrl = (String) responseMap.get("nextUrl");
                timelineAdapter.notifyDataSetChanged();
            }

            @Override
            public void failure(int statusCode, FLError error) {

            }
        });
    }

    public void refreshTransactions() {
        if (!this.transactions.isEmpty())
            this.timelineAdapter.notifyDataSetChanged();

        FloozRestClient.getInstance().timeline(this.currentScope, new FloozHttpResponseHandler() {
            @Override
            public void success(Object response) {
                Map<String, Object> responseMap = (Map<String, Object>) response;

                transactions.clear();
                transactions.addAll((List<FLTransaction>) responseMap.get("transactions"));
                nextPageUrl = (String) responseMap.get("nextUrl");

                timelineAdapter.notifyDataSetChanged();
                timelineListView.onRefreshComplete();
            }

            @Override
            public void failure(int statusCode, FLError error) {

            }
        });
    }

    private void didFilterChange() {
        this.timelineAdapter.notifyDataSetChanged();
        this.timelineListView.getRefreshableView().smoothScrollToPosition(0);
        this.timelineListView.onRefreshComplete();
    }
}
