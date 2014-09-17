package flooz.android.com.flooz.UI.Activity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import flooz.android.com.flooz.Adapter.TimelineListAdapter;
import flooz.android.com.flooz.Model.FLError;
import flooz.android.com.flooz.Model.FLTransaction;
import flooz.android.com.flooz.Network.FloozHttpResponseHandler;
import flooz.android.com.flooz.Network.FloozRestClient;
import flooz.android.com.flooz.R;

/**
 * Created by Flooz on 9/17/14.
 */
public class StartActivity extends Activity {

    private ListView timelineListView;
    private TimelineListAdapter timelineAdapter;

    private List<FLTransaction> transactions;

    private BroadcastReceiver reloadTimelineReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            refreshTransactions();
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.start_activity);

        this.transactions = new ArrayList<FLTransaction>(0);
        this.timelineAdapter = new TimelineListAdapter(this, this.transactions);

        this.timelineListView = (ListView) this.findViewById(R.id.start_timeline_list);
        this.timelineListView.setAdapter(this.timelineAdapter);

        ImageView title = (ImageView) this.findViewById(R.id.start_flooz_title);
        title.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                timelineListView.smoothScrollToPosition(0);
            }
        });

        this.refreshTransactions();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

    }

    @Override
    public void onResume() {
        super.onResume();
        this.refreshTransactions();
    }

    private void refreshTransactions() {
        FloozRestClient.getInstance().timeline(FLTransaction.TransactionScope.TransactionScopePublic, new FloozHttpResponseHandler() {
            @Override
            public void success(Object response) {
                Map<String, Object> responseMap = (Map<String, Object>)response;

                transactions.clear();
                transactions.addAll((List<FLTransaction>)responseMap.get("transactions"));

                timelineAdapter.notifyDataSetChanged();
            }

            @Override
            public void failure(int statusCode, FLError error) {

            }
        });
    }

}