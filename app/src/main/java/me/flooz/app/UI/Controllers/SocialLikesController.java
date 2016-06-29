package me.flooz.app.UI.Controllers;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import org.json.JSONObject;

import me.flooz.app.Adapter.SocialLikesAdapter;
import me.flooz.app.App.FloozApplication;
import me.flooz.app.Model.FLTransaction;
import me.flooz.app.Model.FLUser;
import me.flooz.app.R;
import me.flooz.app.Utils.FLHelper;

/**
 * Created by Flooz on 28/06/16.
 */
public class SocialLikesController extends BaseController {

    public FLTransaction transaction;

    private ListView listView;
    private SocialLikesAdapter listAdapter;

    public SocialLikesController(@NonNull View mainView, @NonNull Activity parentActivity, @NonNull ControllerKind kind) {
        super(mainView, parentActivity, kind);

        this.init();
    }

    public SocialLikesController(@NonNull View mainView, @NonNull Activity parentActivity, @NonNull ControllerKind kind, @Nullable JSONObject data) {
        super(mainView, parentActivity, kind, data);

        this.init();
    }

    @Override
    protected void init() {
        super.init();

        listView = (ListView) this.currentView.findViewById(R.id.social_likes_list);

        this.listAdapter = new SocialLikesAdapter(this.parentActivity);

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
        this.listAdapter.pendingList = this.transaction.social.likes;
        this.listAdapter.notifyDataSetChanged();

        this.titleLabel.setText(FLHelper.formatUserNumber(this.transaction.social.likesCount.longValue()) + " J'AIME");
    }
}
