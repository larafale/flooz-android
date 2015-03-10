package me.flooz.app.UI.Activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import me.flooz.app.Adapter.TimelineListAdapter;
import me.flooz.app.App.FloozApplication;
import me.flooz.app.Model.FLError;
import me.flooz.app.Model.FLTransaction;
import me.flooz.app.Network.FloozHttpResponseHandler;
import me.flooz.app.Network.FloozRestClient;
import me.flooz.app.R;
import me.flooz.app.Utils.CustomFonts;

/**
 * Created by Flooz on 9/17/14.
 */
public class StartActivity extends Activity implements TimelineListAdapter.TimelineListRowDelegate {

    public FloozApplication floozApp;

    private ListView timelineListView;
    private TimelineListAdapter timelineAdapter;

    private Button loginButton;
    private Button signupButton;

    private List<FLTransaction> transactions;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.start_activity);
        floozApp = (FloozApplication)this.getApplicationContext();
        this.transactions = new ArrayList<>(0);

        this.timelineAdapter = new TimelineListAdapter(this, this.transactions);
        this.timelineAdapter.delegate = this;
        this.timelineListView = (ListView) this.findViewById(R.id.start_timeline_list);
        this.timelineListView.setAdapter(this.timelineAdapter);

        this.loginButton = (Button) this.findViewById(R.id.start_login_button);
        this.signupButton = (Button) this.findViewById(R.id.start_signup_button);

        this.loginButton.setTypeface(CustomFonts.customTitleLight(this));
        this.signupButton.setTypeface(CustomFonts.customTitleLight(this), Typeface.BOLD);

        this.loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchLogin();
            }
        });
        this.signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchLogin();
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

    private void launchLogin() {
        Intent intent = new Intent(this, SignupActivity.class);

        intent.putExtra("page", SignupActivity.SignupPageIdentifier.SignupPhone.ordinal());

        this.startActivity(intent);
        this.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        this.finish();
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
        this.finish();
    }

    public void ListItemClick(FLTransaction transac) {

    }

    public void ListItemCommentClick(FLTransaction transac) {
        this.launchLogin();
    }

    public void ListItemImageClick(String imgUrl) {

    }
}