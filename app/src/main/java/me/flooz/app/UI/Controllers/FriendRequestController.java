package me.flooz.app.UI.Controllers;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import me.flooz.app.Adapter.FriendRequestListAdapter;
import me.flooz.app.Model.FLError;
import me.flooz.app.Model.FLUser;
import me.flooz.app.Network.FloozHttpResponseHandler;
import me.flooz.app.Network.FloozRestClient;
import me.flooz.app.R;
import me.flooz.app.UI.Activity.HomeActivity;
import me.flooz.app.UI.Tools.ActionSheet;
import me.flooz.app.UI.Tools.ActionSheetItem;
import me.flooz.app.Utils.CustomFonts;

/**
 * Created by Flooz on 10/22/15.
 */
public class FriendRequestController extends BaseController {

    private ImageView headerBackButton;
    private ListView listView;
    private FriendRequestListAdapter listAdapter;

    public FriendRequestController(@NonNull View mainView, @NonNull final Activity parentActivity, @NonNull ControllerKind kind) {
        super(mainView, parentActivity, kind);

        this.headerBackButton = (ImageView) this.currentView.findViewById(R.id.header_item_left);
        TextView headerTitle = (TextView) this.currentView.findViewById(R.id.header_title);
        listView = (ListView) this.currentView.findViewById(R.id.friends_request_list);

        headerTitle.setTypeface(CustomFonts.customTitleExtraLight(this.parentActivity));

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

        this.listAdapter = new FriendRequestListAdapter(this.parentActivity);
        this.listView.setAdapter(this.listAdapter);

        this.listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                final FLUser user = listAdapter.getItem(position);

                List<ActionSheetItem> items = new ArrayList<>();

                items.add(new ActionSheetItem(parentActivity, R.string.MENU_ACCEPT_FRIENDS, new ActionSheetItem.ActionSheetItemClickListener() {
                    @Override
                    public void onClick() {
                        FloozRestClient.getInstance().showLoadView();
                        FloozRestClient.getInstance().performActionOnFriend(user.userId, FloozRestClient.FriendAction.Accept, new FloozHttpResponseHandler() {
                            @Override
                            public void success(Object response) {
                                FloozRestClient.getInstance().updateCurrentUser(new FloozHttpResponseHandler() {
                                    @Override
                                    public void success(Object response) {
                                        listAdapter.refreshFriendList();

                                        if (listAdapter.getCount() == 0)
                                            headerBackButton.performClick();
                                    }

                                    @Override
                                    public void failure(int statusCode, FLError error) {
                                        listAdapter.refreshFriendList();
                                    }
                                });
                            }

                            @Override
                            public void failure(int statusCode, FLError error) { }
                        });
                    }
                }));

                items.add(new ActionSheetItem(parentActivity, R.string.MENU_DECLINE_FRIENDS, new ActionSheetItem.ActionSheetItemClickListener() {
                    @Override
                    public void onClick() {
                        FloozRestClient.getInstance().showLoadView();
                        FloozRestClient.getInstance().performActionOnFriend(user.userId, FloozRestClient.FriendAction.Delete, new FloozHttpResponseHandler() {
                            @Override
                            public void success(Object response) {
                                FloozRestClient.getInstance().updateCurrentUser(new FloozHttpResponseHandler() {
                                    @Override
                                    public void success(Object response) {
                                        listAdapter.refreshFriendList();

                                        if (listAdapter.getCount() == 0)
                                            headerBackButton.performClick();
                                    }

                                    @Override
                                    public void failure(int statusCode, FLError error) {
                                        listAdapter.refreshFriendList();
                                    }
                                });
                            }

                            @Override
                            public void failure(int statusCode, FLError error) { }
                        });
                    }
                }));

                ActionSheet.showWithItems(parentActivity, items);
            }
        });

    }

    @Override
    public void onBackPressed() {
        this.headerBackButton.performClick();
    }
}
