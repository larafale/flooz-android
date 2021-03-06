package me.flooz.app.UI.Controllers;

import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.baoyz.widget.PullRefreshLayout;

import org.json.JSONObject;

import java.util.List;

import me.flooz.app.Adapter.FriendsListAdapter;
import me.flooz.app.Adapter.FriendsListAdapterDelegate;
import me.flooz.app.App.FloozApplication;
import me.flooz.app.Model.FLError;
import me.flooz.app.Model.FLUser;
import me.flooz.app.Network.FloozHttpResponseHandler;
import me.flooz.app.Network.FloozRestClient;
import me.flooz.app.R;
import me.flooz.app.UI.Activity.HomeActivity;
import me.flooz.app.UI.Activity.ShareAppActivity;
import me.flooz.app.Utils.CustomFonts;
import se.emilsjolander.stickylistheaders.StickyListHeadersListView;

/**
 * Created by Flooz on 8/31/15.
 */
public class FriendsController extends BaseController implements FriendsListAdapterDelegate {

    private TextView searchTextfield;
    private ImageView clearSearchTextfieldButton;
    public PullRefreshLayout refreshContainer;

    private LinearLayout backgroundView;

    private FriendsListAdapter listAdapter;

    public FriendsController(@NonNull View mainView, @NonNull Activity parentActivity, @NonNull ControllerKind kind) {
        super(mainView, parentActivity, kind);

        this.init();
    }

    public FriendsController(@NonNull View mainView, @NonNull Activity parentActivity, @NonNull ControllerKind kind, @Nullable JSONObject data) {
        super(mainView, parentActivity, kind, data);

        this.init();
    }

    @Override
    protected void init() {
        super.init();

        this.refreshContainer = (PullRefreshLayout) this.currentView.findViewById(R.id.friends_refresh_container);
        this.searchTextfield = (TextView) this.currentView.findViewById(R.id.friends_search_textfield);
        this.clearSearchTextfieldButton = (ImageView) this.currentView.findViewById(R.id.friends_search_clear);
        this.backgroundView = (LinearLayout) this.currentView.findViewById(R.id.friends_empty_background);
        Button inviteFriends = (Button) this.currentView.findViewById(R.id.friends_invite_button);

        inviteFriends.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentKind == ControllerKind.ACTIVITY_CONTROLLER) {
                    Intent intentShare = new Intent(parentActivity, ShareAppActivity.class);
                    parentActivity.startActivity(intentShare);
                    parentActivity.overridePendingTransition(R.anim.slide_up, android.R.anim.fade_out);
                } else
                    ((HomeActivity) parentActivity).changeCurrentTab(HomeActivity.TabID.SHARE_TAB);
            }
        });

        searchTextfield.setTypeface(CustomFonts.customContentRegular(this.parentActivity));

        this.refreshContainer.setOnRefreshListener(new PullRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                final Boolean[] validate = {false};

                FloozRestClient.getInstance().updateCurrentUser(new FloozHttpResponseHandler() {
                    @Override
                    public void success(Object response) {

                        listAdapter.reloadFriends();

                        if (validate[0])
                            refreshContainer.setRefreshing(false);
                        else
                            validate[0] = true;
                    }

                    @Override
                    public void failure(int statusCode, FLError error) {
                        if (validate[0])
                            refreshContainer.setRefreshing(false);
                        else
                            validate[0] = true;
                    }
                });

                FloozRestClient.getInstance().loadFriendSuggestions(new FloozHttpResponseHandler() {
                    @Override
                    public void success(Object response) {

                        listAdapter.reloadSuggestions((List<FLUser>) response);

                        if (validate[0])
                            refreshContainer.setRefreshing(false);
                        else
                            validate[0] = true;
                    }

                    @Override
                    public void failure(int statusCode, FLError error) {
                        if (validate[0])
                            refreshContainer.setRefreshing(false);
                        else
                            validate[0] = true;
                    }
                });
            }
        });

        this.searchTextfield.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.length() > 0) {
                    clearSearchTextfieldButton.setVisibility(View.VISIBLE);
                    Handler handler = new Handler(Looper.getMainLooper());
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            listAdapter.searchUser(searchTextfield.getText().toString());
                        }
                    }, 300);
                } else {
                    clearSearchTextfieldButton.setVisibility(View.GONE);
                    listAdapter.searchUser(editable.toString());
                }
            }
        });

        this.clearSearchTextfieldButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchTextfield.setText("");
                listAdapter.searchUser("");
            }
        });

        StickyListHeadersListView resultList = (StickyListHeadersListView) this.currentView.findViewById(R.id.friends_result_list);
        this.listAdapter = new FriendsListAdapter(this.parentActivity);
        this.listAdapter.delegate = this;
        resultList.setAdapter(this.listAdapter);

        resultList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                FLUser user = listAdapter.getItem(position);
                user.selectedCanal = FLUser.FLUserSelectedCanal.values()[(int) listAdapter.getHeaderId(position)];
//            FloozApplication.getInstance().showUserActionMenu(user);
                // TODO USERACTIONMENU PLUS LA
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (this.listAdapter != null) {
            this.listAdapter.notifyDataSetChanged();
            this.listAdapter.loadBroadcastReceivers();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (this.listAdapter != null)
            this.listAdapter.unloadBroadcastReceivers();
    }

    @Override
    public void dataReloaded() {
        if (listAdapter.getCount() == 0 && !listAdapter.isSearchActive)
            backgroundView.setVisibility(View.VISIBLE);
        else
            backgroundView.setVisibility(View.GONE);
    }
}
