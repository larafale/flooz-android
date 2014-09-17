package flooz.android.com.flooz.UI.Fragment;

/**
 * Created by Flooz on 9/2/14.
 */

import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;

import java.util.ArrayList;
import java.util.HashMap;
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
    private TimelineListAdapter timelineAdapter;

    private List<FLTransaction> transactions;
    private List<FLTransaction> transactionsLoaded;
    private Map<FLTransaction.TransactionScope, List<FLTransaction>> transactionsCache;
    private FLTransaction.TransactionScope currentScope;

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
                refreshTransactions();
            }
        });
        this.timelineListView.setOnLastItemVisibleListener(new PullToRefreshBase.OnLastItemVisibleListener() {
            @Override
            public void onLastItemVisible() {
                loadNextPage();
            }
        });


        this.publicTabElementView.select();
        this.currentScope = FLTransaction.TransactionScope.TransactionScopePublic;

        this.transactionsCache = new HashMap<FLTransaction.TransactionScope, List<FLTransaction>>(3);
        this.transactions = new ArrayList<FLTransaction>(0);
        this.transactionsLoaded = new ArrayList<FLTransaction>(0);

        this.timelineAdapter = new TimelineListAdapter(FloozApplication.getAppContext(), this.transactions);
        this.timelineListView.setAdapter(this.timelineAdapter);

        ImageView profileButton = (ImageView)view.findViewById(R.id.profile_header_button);

        profileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FloozApplication.performLocalNotification(CustomNotificationIntents.showSlidingLeftMenu());
            }
        });

        ImageView friendsButton = (ImageView)view.findViewById(R.id.friends_header_button);

        friendsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FloozApplication.performLocalNotification(CustomNotificationIntents.showSlidingRightMenu());
            }
        });

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

    private void loadNextPage() {
        if (nextPageUrl.isEmpty())
            return;

        FloozRestClient.getInstance().timelineNextPage(this.nextPageUrl, new FloozHttpResponseHandler() {
            @Override
            public void success(Object response) {
                Map<String, Object> responseMap = (Map<String, Object>)response;

                transactions.addAll((List<FLTransaction>)responseMap.get("transactions"));
                nextPageUrl = (String)responseMap.get("nextUrl");
                timelineAdapter.notifyDataSetChanged();
            }

            @Override
            public void failure(int statusCode, FLError error) {

            }
        });
    }

    private void refreshTransactions() {
        FloozRestClient.getInstance().timeline(this.currentScope, new FloozHttpResponseHandler() {
            @Override
            public void success(Object response) {
                Map<String, Object> responseMap = (Map<String, Object>)response;

                transactions.clear();
                transactions.addAll((List<FLTransaction>)responseMap.get("transactions"));
                transactionsCache.put(currentScope, new ArrayList<FLTransaction>((List<FLTransaction>)responseMap.get("transactions")));
                nextPageUrl = (String)responseMap.get("nextUrl");

                timelineAdapter.notifyDataSetChanged();
                timelineListView.onRefreshComplete();
            }

            @Override
            public void failure(int statusCode, FLError error) {

            }
        });
    }

    private void resetTransactionsLoaded() {
        this.transactionsLoaded.removeAll(this.transactionsLoaded);
    }

    private void didFilterTouch(FLTransaction.TransactionScope scope) {

        if (this.currentScope.equals(scope)) {
            this.timelineListView.getRefreshableView().smoothScrollToPosition(0);
            return;
        }

        this.currentScope = scope;

        this.timelineListView.getRefreshableView().smoothScrollToPosition(0);

        this.resetTransactionsLoaded();

        if (this.transactionsCache.containsKey(this.currentScope)
                && ((List<FLTransaction>)this.transactionsCache.get(this.currentScope)).size() > 0) {
            this.transactions.clear();
            this.transactions.addAll((List<FLTransaction>)this.transactionsCache.get(this.currentScope));
            this.nextPageUrl = null;
            this.didFilterChange();
        }
        else {
            this.transactions.clear();
            this.nextPageUrl = null;
            this.didFilterChange();
        }

        FloozRestClient.getInstance().timeline(this.currentScope, new FloozHttpResponseHandler() {
            @Override
            public void success(Object response) {

                Map<String, Object> responseMap = (Map<String, Object>)response;

                transactions.clear();
                transactions.addAll((List<FLTransaction>)responseMap.get("transactions"));
                transactionsCache.put(currentScope, new ArrayList<FLTransaction>((List<FLTransaction>)responseMap.get("transactions")));
                nextPageUrl = (String)responseMap.get("nextUrl");
                didFilterChange();
            }

            @Override
            public void failure(int statusCode, FLError error) {

            }
        });

    }

    private void didFilterChange() {

        timelineAdapter.notifyDataSetChanged();
        this.timelineListView.getRefreshableView().smoothScrollToPosition(0);
        timelineListView.onRefreshComplete();
    }

    @Override
    public void tabElementViewClick(FloozFilterTabItem.TabType tabType)
    {
        switch (tabType)
        {
            case PUBLIC:
                this.privateTabElementView.unselect();
                this.friendsTabElementView.unselect();
                this.didFilterTouch(FLTransaction.TransactionScope.TransactionScopePublic);
                break;
            case PRIVATE:
                this.publicTabElementView.unselect();
                this.friendsTabElementView.unselect();
                this.didFilterTouch(FLTransaction.TransactionScope.TransactionScopePrivate);
                break;
            case FRIENDS:
                this.privateTabElementView.unselect();
                this.publicTabElementView.unselect();
                this.didFilterTouch(FLTransaction.TransactionScope.TransactionScopeFriend);
                break;
        }
    }
}