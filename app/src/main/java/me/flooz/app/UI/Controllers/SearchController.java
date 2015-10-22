package me.flooz.app.UI.Controllers;

import android.app.Activity;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.baoyz.widget.PullRefreshLayout;

import java.util.List;

import me.flooz.app.Adapter.FriendsListAdapter;
import me.flooz.app.Adapter.FriendsListAdapterDelegate;
import me.flooz.app.Adapter.SearchListAdapter;
import me.flooz.app.Model.FLError;
import me.flooz.app.Model.FLUser;
import me.flooz.app.Network.FloozHttpResponseHandler;
import me.flooz.app.Network.FloozRestClient;
import me.flooz.app.R;
import me.flooz.app.UI.Activity.HomeActivity;
import se.emilsjolander.stickylistheaders.StickyListHeadersListView;

/**
 * Created by Wapazz on 12/10/15.
 */
public class SearchController extends BaseController implements FriendsListAdapterDelegate {

    private ImageView headerBackButton;
    private ImageView clearSearchButton;
    private EditText searchTextField;

    private PullRefreshLayout refreshContainer;
    private SearchListAdapter listAdapter;
    private StickyListHeadersListView resultList;

    public SearchController(@NonNull View mainView, @NonNull final Activity parentActivity, @NonNull ControllerKind kind) {
        super(mainView, parentActivity, kind);

        this.headerBackButton = (ImageView) mainView.findViewById(R.id.header_item_left);
        this.clearSearchButton = (ImageView) mainView.findViewById(R.id.friends_search_clear);
        this.searchTextField = (EditText) mainView.findViewById(R.id.friends_search_textfield);
        this.refreshContainer = (PullRefreshLayout) this.currentView.findViewById(R.id.friends_refresh_container);
        this.resultList = (StickyListHeadersListView) this.currentView.findViewById(R.id.friends_result_list);

        this.listAdapter = new SearchListAdapter(this.parentActivity);
        resultList.setAdapter(this.listAdapter);

        if (currentKind == ControllerKind.FRAGMENT_CONTROLLER)
            this.headerBackButton.setImageDrawable(this.parentActivity.getResources().getDrawable(R.drawable.nav_back));

        this.headerBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentKind == ControllerKind.ACTIVITY_CONTROLLER) {
                    parentActivity.finish();
                    parentActivity.overridePendingTransition(android.R.anim.fade_in, R.anim.slide_down);
                } else {
                    ((HomeActivity) parentActivity).popFragmentInCurrentTab();
                }
            }
        });

        this.clearSearchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchTextField.setText("");
            }
        });

        this.searchTextField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.length() > 0) {
                    clearSearchButton.setVisibility(View.VISIBLE);
                    listAdapter.searchUser(searchTextField.getText().toString());
                } else {
                    clearSearchButton.setVisibility(View.GONE);
                    listAdapter.searchUser(editable.toString());
                }
            }
        });

        this.refreshContainer.setOnRefreshListener(new PullRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                FloozRestClient.getInstance().loadFriendSuggestions(new FloozHttpResponseHandler() {
                    @Override
                    public void success(Object response) {
                        listAdapter.reloadSuggestions((List<FLUser>) response);
                        refreshContainer.setRefreshing(false);
                    }

                    @Override
                    public void failure(int statusCode, FLError error) {
                        refreshContainer.setRefreshing(false);
                    }
                });
            }
        });
    }

    @Override
    public void onBackPressed() {
        this.headerBackButton.performClick();
    }

    @Override
    public void dataReloaded() {

    }
}
