package me.flooz.app.UI.Controllers;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;

import org.json.JSONObject;

import java.util.List;

import me.flooz.app.Adapter.CollectInvitedAdapter;
import me.flooz.app.Adapter.FriendRequestListAdapter;
import me.flooz.app.App.FloozApplication;
import me.flooz.app.Model.FLError;
import me.flooz.app.Model.FLTransaction;
import me.flooz.app.Model.FLUser;
import me.flooz.app.Network.FloozHttpResponseHandler;
import me.flooz.app.Network.FloozRestClient;
import me.flooz.app.R;
import me.flooz.app.UI.Activity.ShareCollectAcivity;

/**
 * Created by Flooz on 28/06/16.
 */
public class CollectInvitedController extends BaseController {
    public FLTransaction collect;


    private ImageView shareButton;

    private ListView listView;
    private CollectInvitedAdapter listAdapter;

    private List<FLUser> invited;

    public CollectInvitedController(@NonNull View mainView, @NonNull Activity parentActivity, @NonNull ControllerKind kind) {
        super(mainView, parentActivity, kind);

        this.init();
    }

    public CollectInvitedController(@NonNull View mainView, @NonNull Activity parentActivity, @NonNull ControllerKind kind, @Nullable JSONObject data) {
        super(mainView, parentActivity, kind, data);

        this.init();
    }

    @Override
    protected void init() {
        super.init();

        this.listView = (ListView) this.currentView.findViewById(R.id.collect_invited_list);
        this.shareButton = (ImageView) this.currentView.findViewById(R.id.header_item_right);

        this.listAdapter = new CollectInvitedAdapter(this.parentActivity);
        this.listView.setAdapter(this.listAdapter);

        this.listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                final FLUser user = listAdapter.getItem(position);

                FloozApplication.getInstance().showUserProfile(user);
            }
        });

        this.shareButton.setColorFilter(this.parentActivity.getResources().getColor(R.color.blue));

        this.shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(parentActivity, ShareCollectAcivity.class);
                intent.putExtra("potId", collect.transactionId);
                parentActivity.startActivity(intent);
                parentActivity.overridePendingTransition(R.anim.slide_up, android.R.anim.fade_out);
            }
        });
    }

    @Override
    public void onResume() {

        if (FloozRestClient.getInstance().currentUser.userId.contentEquals(this.collect.creator.userId) && this.collect.status != FLTransaction.TransactionStatus.TransactionStatusPending)
            this.shareButton.setVisibility(View.VISIBLE);
        else
            this.shareButton.setVisibility(View.GONE);

        FloozRestClient.getInstance().collectInvitations(collect.transactionId, new FloozHttpResponseHandler() {
            @Override
            public void success(Object response) {
                invited = (List<FLUser>) response;
                listAdapter.pendingList = invited;
                listAdapter.notifyDataSetChanged();
            }

            @Override
            public void failure(int statusCode, FLError error) {

            }
        });
    }
}
