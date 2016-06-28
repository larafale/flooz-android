package me.flooz.app.UI.Controllers;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import org.json.JSONObject;

import me.flooz.app.Adapter.FriendRequestListAdapter;
import me.flooz.app.App.FloozApplication;
import me.flooz.app.Model.FLUser;
import me.flooz.app.R;

/**
 * Created by Flooz on 28/06/16.
 */
public class CollectInvitedController extends BaseController {
    public String collectId;

    private ListView listView;
    private FriendRequestListAdapter listAdapter;

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

        listView = (ListView) this.currentView.findViewById(R.id.collect_invited_list);

        this.listAdapter = new FriendRequestListAdapter(this.parentActivity);
        this.listView.setAdapter(this.listAdapter);

        this.listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                final FLUser user = listAdapter.getItem(position);

                FloozApplication.getInstance().showUserProfile(user);
            }
        });

    }

    @Override
    public void onResume() {

    }
}
