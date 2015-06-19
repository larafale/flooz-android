package me.flooz.app.UI.Fragment.Home;

/**
 * Created by Flooz on 9/2/14.
 */

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.baoyz.widget.PullRefreshLayout;

import java.util.List;

import me.flooz.app.Adapter.FriendsListAdapter;
import me.flooz.app.Adapter.FriendsListAdapterDelegate;
import me.flooz.app.App.FloozApplication;
import me.flooz.app.Model.FLError;
import me.flooz.app.Model.FLUser;
import me.flooz.app.Network.FloozHttpResponseHandler;
import me.flooz.app.Network.FloozRestClient;
import me.flooz.app.R;
import me.flooz.app.UI.Activity.ShareAppActivity;
import me.flooz.app.Utils.CustomFonts;
import se.emilsjolander.stickylistheaders.StickyListHeadersListView;

public class FriendsFragment extends HomeBaseFragment implements FriendsListAdapterDelegate
{
    public Context context;

    private TextView searchTextfield;
    private ImageView clearSearchTextfieldButton;
    public PullRefreshLayout refreshContainer;

    private LinearLayout backgroundView;

    private FriendsListAdapter listAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.friends_fragment, null);

        this.context = inflater.getContext();

        this.refreshContainer = (PullRefreshLayout) view.findViewById(R.id.friends_refresh_container);
        this.searchTextfield = (TextView) view.findViewById(R.id.friends_search_textfield);
        this.clearSearchTextfieldButton = (ImageView) view.findViewById(R.id.friends_search_clear);
        this.backgroundView = (LinearLayout) view.findViewById(R.id.friends_empty_background);
        Button inviteFriends = (Button) view.findViewById(R.id.friends_invite_button);

        inviteFriends.setOnClickListener(v -> {
            Intent intentShare = new Intent(parentActivity, ShareAppActivity.class);
            parentActivity.startActivity(intentShare);
            parentActivity.overridePendingTransition(R.anim.slide_up, android.R.anim.fade_out);
        });

        this.searchTextfield.setTypeface(CustomFonts.customTitleLight(inflater.getContext()));

        this.refreshContainer.setOnRefreshListener(() -> {
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

                    listAdapter.reloadSuggestions((List<FLUser>)response);

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
                    handler.postDelayed(() -> listAdapter.searchUser(searchTextfield.getText().toString()), 300);
                } else {
                    clearSearchTextfieldButton.setVisibility(View.GONE);
                    listAdapter.searchUser(editable.toString());
                }
            }
        });

        this.clearSearchTextfieldButton.setOnClickListener(view1 -> {
            searchTextfield.setText("");
            listAdapter.searchUser("");
        });

        StickyListHeadersListView resultList = (StickyListHeadersListView) view.findViewById(R.id.friends_result_list);
        this.listAdapter = new FriendsListAdapter(inflater.getContext());
        this.listAdapter.delegate = this;
        resultList.setAdapter(this.listAdapter);

        resultList.setOnItemClickListener((adapterView, view1, position, l) -> {
            FLUser user = listAdapter.getItem(position);
            user.selectedCanal = FLUser.FLUserSelectedCanal.values()[(int) listAdapter.getHeaderId(position)];
            FloozApplication.getInstance().showUserActionMenu(user);
        });

        this.dataReloaded();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (this.listAdapter != null)
            this.listAdapter.loadBroadcastReceivers();
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
