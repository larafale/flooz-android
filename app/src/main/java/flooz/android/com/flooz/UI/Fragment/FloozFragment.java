package flooz.android.com.flooz.UI.Fragment;

/**
 * Created by Flooz on 9/2/14.
 */

import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;

import java.util.List;
import java.util.Map;

import flooz.android.com.flooz.Adapter.TimelineListAdapter;
import flooz.android.com.flooz.App.FloozApplication;
import flooz.android.com.flooz.Model.FLError;
import flooz.android.com.flooz.Model.FLTransaction;
import flooz.android.com.flooz.Network.FloozHttpResponseHandler;
import flooz.android.com.flooz.Network.FloozRestClient;
import flooz.android.com.flooz.R;
import flooz.android.com.flooz.UI.View.FloozFilterTabItem;
import flooz.android.com.flooz.Utils.CustomNotificationIntents;

public class FloozFragment extends Fragment implements FloozFilterTabItem.Delegate
{
    private FloozFilterTabItem publicTabElementView;
    private FloozFilterTabItem privateTabElementView;
    private FloozFilterTabItem friendsTabElementView;

    private PullToRefreshListView timelineListView;

    private List<FLTransaction> transactions;
    private Map<FLTransaction.TransactionScope, List<FLTransaction>> transactionsCache;
    private String nextPageUrl;
    private FLTransaction.TransactionScope currentScope;

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
        setRetainInstance(true);

        LocalBroadcastManager.getInstance(FloozApplication.getAppContext()).registerReceiver(reloadTimelineReceiver,
                CustomNotificationIntents.filterReloadTimeline());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.flooz_fragment, null);

        this.publicTabElementView = (FloozFilterTabItem) view.findViewById(R.id.public_tab);
        this.privateTabElementView = (FloozFilterTabItem) view.findViewById(R.id.private_tab);
        this.friendsTabElementView = (FloozFilterTabItem) view.findViewById(R.id.friends_tab);

        this.publicTabElementView.setDelegate(this);
        this.privateTabElementView.setDelegate(this);
        this.friendsTabElementView.setDelegate(this);

        this.timelineListView = (PullToRefreshListView) view.findViewById(R.id.timeline_list);
        this.timelineListView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener<ListView>() {
            @Override
            public void onRefresh(PullToRefreshBase<ListView> refreshView) {
                new Handler().postAtTime(new Runnable() {
                    @Override
                    public void run() {
                        timelineListView.onRefreshComplete();
                    }
                }, 10000);
            }
        });

        this.publicTabElementView.select();
        this.currentScope = FLTransaction.TransactionScope.TransactionScopePublic;

        return view;
    }

    private void refreshTransactions() {
        FloozRestClient.getInstance().timeline(this.currentScope, new FloozHttpResponseHandler() {
            @Override
            public void success(Object response) {
                Map<String, Object> responseMap = (Map<String, Object>)response;

                transactions = (List)responseMap.get("transactions");
                nextPageUrl = (String)responseMap.get("nextUrl");

                timelineListView.setAdapter(new TimelineListAdapter(FloozApplication.getAppContext(), transactions));
            }

            @Override
            public void failure(int statusCode, FLError error) {

            }
        });
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);


    }

    @Override
    public void tabElementViewClick(FloozFilterTabItem.TabType tabType)
    {
        switch (tabType)
        {
            case PUBLIC:
                this.privateTabElementView.unselect();
                this.friendsTabElementView.unselect();
                break;
            case PRIVATE:
                this.publicTabElementView.unselect();
                this.friendsTabElementView.unselect();
                break;
            case FRIENDS:
                this.privateTabElementView.unselect();
                this.publicTabElementView.unselect();
                break;
        }
    }
}