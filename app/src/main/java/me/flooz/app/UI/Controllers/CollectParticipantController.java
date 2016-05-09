package me.flooz.app.UI.Controllers;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import org.json.JSONObject;

import java.util.List;

import me.flooz.app.Adapter.CollectParticipantAdapter;
import me.flooz.app.App.FloozApplication;
import me.flooz.app.Model.FLError;
import me.flooz.app.Model.FLTransaction;
import me.flooz.app.Network.FloozHttpResponseHandler;
import me.flooz.app.Network.FloozRestClient;
import me.flooz.app.R;
import me.flooz.app.UI.Activity.CollectParticipationActivity;
import me.flooz.app.UI.Activity.HomeActivity;
import me.flooz.app.UI.Fragment.Home.TabFragments.CollectParticipantFragment;
import me.flooz.app.UI.Fragment.Home.TabFragments.CollectParticipationFragment;

/**
 * Created by Flooz on 08/05/16.
 */
public class CollectParticipantController extends BaseController {

    private FLTransaction collect;
    private CollectParticipantAdapter listAdapter;

    public CollectParticipantController(@NonNull View mainView, @NonNull Activity parentActivity, @NonNull ControllerKind kind) {
        super(mainView, parentActivity, kind);

        this.init();
    }

    public CollectParticipantController(@NonNull View mainView, @NonNull Activity parentActivity, @NonNull ControllerKind kind, @Nullable JSONObject data) {
        super(mainView, parentActivity, kind, data);

        this.init();
    }

    @Override
    protected void init() {
        super.init();

        ListView listView = (ListView) this.currentView.findViewById(R.id.participant_list);
        this.listAdapter = new CollectParticipantAdapter(parentActivity);

        if (this.collect != null)
            this.listAdapter.setCollect(this.collect);

        listView.setAdapter(this.listAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (listAdapter.getItem(position).countParticipations.intValue() > 1) {
                    if (currentKind == ControllerKind.ACTIVITY_CONTROLLER) {
                        Intent intent = new Intent(parentActivity, CollectParticipationActivity.class);
                        intent.putExtra("collectId", collect.transactionId);
                        intent.putExtra("userId", listAdapter.getItem(position).userId);
                        parentActivity.startActivity(intent);
                        parentActivity.overridePendingTransition(R.anim.slide_up, android.R.anim.fade_out);
                    } else {
                        CollectParticipationFragment fragment = new CollectParticipationFragment();
                        fragment.collectId = collect.transactionId;
                        fragment.userId = listAdapter.getItem(position).userId;
                        ((HomeActivity) parentActivity).pushFragmentInCurrentTab(fragment);
                    }
                } else {
                    String transactionId = listAdapter.getItem(position).participations.optJSONObject(0).optString("_id");
                    FloozRestClient.getInstance().showLoadView();
                    FloozRestClient.getInstance().transactionWithId(transactionId, new FloozHttpResponseHandler() {
                        @Override
                        public void success(Object response) {
                            FLTransaction transac = new FLTransaction(((JSONObject) response).optJSONObject("item"));
                            FloozApplication.getInstance().showTransactionCard(transac);
                        }

                        @Override
                        public void failure(int statusCode, FLError error) {

                        }
                    });
                }
            }
        });
    }

    public void setCollect(@NonNull  FLTransaction collect) {
        this.collect = collect;

        this.listAdapter.setCollect(collect);
    }
}
