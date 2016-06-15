package me.flooz.app.UI.Activity;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import me.flooz.app.Adapter.ShareCollectAdapter;
import me.flooz.app.App.FloozApplication;
import me.flooz.app.Model.FLError;
import me.flooz.app.Model.FLUser;
import me.flooz.app.Network.FloozHttpResponseHandler;
import me.flooz.app.Network.FloozRestClient;
import me.flooz.app.R;
import me.flooz.app.UI.View.ContactPickerView;
import me.flooz.app.UI.View.TokenPickerLibrary.TokenCompleteTextView;
import me.flooz.app.Utils.CustomFonts;
import me.flooz.app.Utils.FLHelper;
import me.flooz.app.Utils.ViewServer;
import se.emilsjolander.stickylistheaders.StickyListHeadersListView;

/**
 * Created by Flooz on 09/05/16.
 */
public class ShareCollectAcivity extends BaseActivity {

    private FloozApplication floozApp;

    private ImageView headerBackButton;
    private ContactPickerView searchField;
    private StickyListHeadersListView listView;
    private Button sendButton;
    private ShareCollectAdapter listAdapter;

    public List<FLUser> selectedUsers = new ArrayList<>();

    private String collectId;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (FLHelper.isDebuggable())
            ViewServer.get(this).addWindow(this);

        JSONObject triggerData = null;
        if (getIntent() != null && getIntent().hasExtra("triggerData"))
            try {
                triggerData = new JSONObject(getIntent().getStringExtra("triggerData"));
            } catch (JSONException e) {
                e.printStackTrace();
            }

        floozApp = (FloozApplication) this.getApplicationContext();
        this.setContentView(R.layout.share_collect_activity);

        TextView title = (TextView) this.findViewById(R.id.header_title);
        this.headerBackButton = (ImageView) this.findViewById(R.id.header_item_left);
        this.searchField = (ContactPickerView) this.findViewById(R.id.share_collect_search);
        this.listView = (StickyListHeadersListView) this.findViewById(R.id.share_collect_list);
        this.sendButton = (Button) this.findViewById(R.id.share_collect_send);

        title.setTypeface(CustomFonts.customTitleLight(this));
        this.searchField.setTypeface(CustomFonts.customContentRegular(this));
        this.sendButton.setTypeface(CustomFonts.customContentRegular(this));

        if (triggerData != null) {
            if (triggerData.has("title") && !triggerData.optString("title").isEmpty())
                title.setText(triggerData.optString("title"));

            if (triggerData.has("close") && !triggerData.optBoolean("close")) {
                this.headerBackButton.setVisibility(View.GONE);
            }

            if (triggerData.has("_id"))
                this.collectId = triggerData.optString("_id");
        }

        if (getIntent() != null && getIntent().hasExtra("potId"))
            this.collectId = getIntent().getStringExtra("potId");

        this.headerBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (headerBackButton.getVisibility() == View.VISIBLE) {
                    finish();
                    overridePendingTransition(android.R.anim.fade_in, R.anim.slide_down);
                }
            }
        });

        this.searchField.setTokenListener(new TokenCompleteTextView.TokenListener() {
            @Override
            public void onTokenAdded(Object token) {

            }

            @Override
            public void onTokenRemoved(Object token) {
                FLUser user = (FLUser) token;

                if (selectedUsers.contains(user)) {
                    selectedUsers.remove(user);
                    searchField.removeObject(user);
                }

                if (selectedUsers.size() > 0) {
                    sendButton.setEnabled(true);
                    sendButton.setText("Inviter (" + selectedUsers.size() + ")");
                } else {
                    sendButton.setEnabled(false);
                    sendButton.setText("Inviter");
                }
            }
        });

        this.listAdapter = new ShareCollectAdapter(this);
        this.listAdapter.selectedUsers = this.selectedUsers;

        this.listView.setAdapter(this.listAdapter);

        this.searchField.setPrefix(this.getResources().getString(R.string.TRANSACTION_CONTACT_PICKER_PREFIX));
        this.searchField.setTokenClickStyle(TokenCompleteTextView.TokenClickStyle.Select);
        this.searchField.setDeletionStyle(TokenCompleteTextView.TokenDeleteStyle.Clear);
        this.searchField.allowCollapse(false);
        this.searchField.setAdapter(new ArrayAdapter<>(this, 0));

        this.searchField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String searchString = s.toString();
                searchString = searchString.replace(getResources().getString(R.string.TRANSACTION_CONTACT_PICKER_PREFIX), "");
                searchString = searchString.replace(",, ", "");
                searchString = searchString.replace(searchField.getHint(), "");
                listAdapter.searchUser(searchString);
            }
        });

        this.listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                FLUser user = listAdapter.getItem(position);

                if (selectedUsers.contains(user)) {
                    selectedUsers.remove(user);
                    searchField.removeObject(user);
                } else {
                    searchField.clearText();
                    selectedUsers.add(user);
                    searchField.addObject(user);
                    listAdapter.searchUser("");
                }

                if (selectedUsers.size() > 0) {
                    sendButton.setEnabled(true);
                    sendButton.setText("Inviter (" + selectedUsers.size() + ")");
                } else {
                    sendButton.setEnabled(false);
                    sendButton.setText("Inviter");
                }
            }
        });

        this.sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchField.clearFocus();
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(getWindow().getDecorView().getRootView().getWindowToken(), 0);

                List<String> sendArray = new ArrayList<>();

                for (FLUser user : selectedUsers) {
                    if (user.userKind == FLUser.UserKind.FloozUser) {
                        sendArray.add(user.userId);
                    } else {
                        sendArray.add(user.phone);
                    }
                }

                FloozRestClient.getInstance().showLoadView();
                FloozRestClient.getInstance().collectInvite(collectId, sendArray, new FloozHttpResponseHandler() {
                    @Override
                    public void success(Object response) {
                        selectedUsers.clear();
                        searchField.clear();
                        searchField.clearText();
                        listAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void failure(int statusCode, FLError error) {

                    }
                });
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (FLHelper.isDebuggable())
            ViewServer.get(this).setFocusedWindow(this);
        floozApp.setCurrentActivity(this);
    }

    @Override
    public void onPause() {
        clearReferences();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        clearReferences();
        if (FLHelper.isDebuggable())
            ViewServer.get(this).removeWindow(this);

        super.onDestroy();
    }

    private void clearReferences() {
        Activity currActivity = floozApp.getCurrentActivity();
        if (currActivity != null && currActivity.equals(this))
            floozApp.setCurrentActivity(null);
    }
}
