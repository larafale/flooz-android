package flooz.android.com.flooz.UI.Fragment.Home;

/**
 * Created by Flooz on 9/2/14.
 */

import android.content.Context;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.baoyz.widget.PullRefreshLayout;

import java.util.List;

import flooz.android.com.flooz.Adapter.FriendsListAdapter;
import flooz.android.com.flooz.Adapter.FriendsListAdapterDelegate;
import flooz.android.com.flooz.App.FloozApplication;
import flooz.android.com.flooz.Model.FLError;
import flooz.android.com.flooz.Model.FLUser;
import flooz.android.com.flooz.Network.FloozHttpResponseHandler;
import flooz.android.com.flooz.Network.FloozRestClient;
import flooz.android.com.flooz.R;
import flooz.android.com.flooz.Utils.CustomFonts;
import se.emilsjolander.stickylistheaders.StickyListHeadersListView;

public class FriendsFragment extends HomeBaseFragment implements FriendsListAdapterDelegate
{
    public Context context;

    private TextView searchTextfield;
    private ImageView clearSearchTextfieldButton;
    public PullRefreshLayout refreshContainer;

    private LinearLayout backgroundView;
    private Button inviteFriends;

    private StickyListHeadersListView resultList;

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
        this.inviteFriends = (Button) view.findViewById(R.id.friends_invite_button);

        this.inviteFriends.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                parentActivity.pushMainFragment("invite", R.animator.slide_up, android.R.animator.fade_out);
            }
        });

        this.searchTextfield.setTypeface(CustomFonts.customTitleLight(inflater.getContext()));

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
                if (editable.length() > 0)
                    clearSearchTextfieldButton.setVisibility(View.VISIBLE);
                else
                    clearSearchTextfieldButton.setVisibility(View.GONE);
                listAdapter.searchUser(editable.toString());
            }
        });

        this.clearSearchTextfieldButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listAdapter.stopSearch();
                searchTextfield.setText("");
                searchTextfield.clearFocus();
                clearSearchTextfieldButton.setVisibility(View.GONE);
                InputMethodManager imm = (InputMethodManager)inflater.getContext().getSystemService(
                        Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(parentActivity.getWindow().getDecorView().getRootView().getWindowToken(), 0);

                if (listAdapter.getCount() == 0)
                    backgroundView.setVisibility(View.VISIBLE);
                else
                    backgroundView.setVisibility(View.GONE);
            }
        });

        this.resultList = (StickyListHeadersListView) view.findViewById(R.id.friends_result_list);
        this.listAdapter = new FriendsListAdapter(inflater.getContext());
        this.listAdapter.delegate = this;
        this.resultList.setAdapter(this.listAdapter);

        this.resultList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                FloozApplication.getInstance().showUserActionMenu((FLUser)listAdapter.getItem(position));
            }
        });

        this.dataReloaded();

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

    }

    @Override
    public void dataReloaded() {
        if (listAdapter.getCount() == 0)
            backgroundView.setVisibility(View.VISIBLE);
        else
            backgroundView.setVisibility(View.GONE);
    }
}
