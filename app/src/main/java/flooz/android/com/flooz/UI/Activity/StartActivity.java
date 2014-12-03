package flooz.android.com.flooz.UI.Activity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

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

/**
 * Created by Flooz on 9/17/14.
 */
public class StartActivity extends Activity {

    public FloozApplication floozApp;

    private ListView timelineListView;
    private TimelineListAdapter timelineAdapter;

    private TextView signInButton;
    private TextView signUpButton;

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

        floozApp = (FloozApplication)this.getApplicationContext();

        setContentView(R.layout.start_activity);
        floozApp = (FloozApplication)this.getApplicationContext();
        this.transactions = new ArrayList<FLTransaction>(0);

        this.timelineAdapter = new TimelineListAdapter(this, this.transactions);
        this.timelineListView = (ListView) this.findViewById(R.id.start_timeline_list);
        this.timelineListView.setAdapter(this.timelineAdapter);

        this.signInButton = (TextView) this.findViewById(R.id.start_login_button);

        this.signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchSignIn();
            }
        });

        this.signUpButton = (TextView) this.findViewById(R.id.start_signup_button);
        this.signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchSignUp();
            }
        });

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

    protected void onResume() {
        super.onResume();
        this.refreshTransactions();
        floozApp.setCurrentActivity(this);
    }

    protected void onPause() {
        clearReferences();
        super.onPause();
    }
    protected void onDestroy() {
        clearReferences();
        super.onDestroy();
    }

    private void clearReferences(){
        Activity currActivity = floozApp.getCurrentActivity();
        if (currActivity != null && currActivity.equals(this))
            floozApp.setCurrentActivity(null);
    }

    private void launchSignIn() {
        Intent intent = new Intent(this, SignupActivity.class);

        intent.putExtra("page", SignupActivity.SignupPageIdentifier.SignupPhone.ordinal());
        intent.putExtra("signup", false);

        startActivity(intent);
    }

    private void launchSignUp() {
        Intent intent = new Intent(this, SignupActivity.class);

        intent.putExtra("page", SignupActivity.SignupPageIdentifier.SignupAccueil1.ordinal());
        intent.putExtra("signup", true);

        startActivity(intent);
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

    @Override
    public void onBackPressed() {

    }
}