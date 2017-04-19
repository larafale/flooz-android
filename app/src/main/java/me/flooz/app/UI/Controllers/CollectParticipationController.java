package me.flooz.app.UI.Controllers;

import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;

import com.baoyz.widget.PullRefreshLayout;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import me.flooz.app.Adapter.TimelineListAdapter;
import me.flooz.app.App.FloozApplication;
import me.flooz.app.Model.FLError;
import me.flooz.app.Model.FLTransaction;
import me.flooz.app.Model.FLUser;
import me.flooz.app.Network.FloozHttpResponseHandler;
import me.flooz.app.Network.FloozRestClient;
import me.flooz.app.R;
import me.flooz.app.UI.Tools.CustomImageViewer;
import me.flooz.app.UI.View.TimelineListView;

/**
 * Created by Flooz on 08/05/16.
 */
public class CollectParticipationController extends BaseController implements TimelineListAdapter.TimelineListRowDelegate {

    public String userId;
    public String collectId;

    public PullRefreshLayout refreshContainer;
    public TimelineListView timelineListView;
    private TimelineListAdapter timelineAdapter;

    public List<FLTransaction> transactions = null;

    private String nextPageUrl;

    private boolean starter = true;

    public CollectParticipationController(@NonNull View mainView, @NonNull Activity parentActivity, @NonNull ControllerKind kind) {
        super(mainView, parentActivity, kind);

        this.init();
    }

    public CollectParticipationController(@NonNull View mainView, @NonNull Activity parentActivity, @NonNull ControllerKind kind, @Nullable JSONObject data) {
        super(mainView, parentActivity, kind, data);

        this.init();
    }

    @Override
    protected void init() {
        super.init();

        this.refreshContainer = (PullRefreshLayout) currentView.findViewById(R.id.participation_refresh_container);
        this.timelineListView = (TimelineListView) currentView.findViewById(R.id.participation_list);

        this.refreshContainer.setOnRefreshListener(new PullRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshTransactions();
            }
        });

        this.timelineListView.setOnTimelineListViewListener(new TimelineListView.OnTimelineListViewListener() {
            @Override
            public void onShowLastItem() {
                loadNextPage();
            }
        });

        if (this.transactions == null)
            this.transactions = new ArrayList<>(0);

        if (this.timelineAdapter == null) {
            this.timelineAdapter = new TimelineListAdapter(FloozApplication.getAppContext(), this.transactions);
            this.timelineAdapter.delegate = this;
        }

        this.timelineListView.setAdapter(this.timelineAdapter);

        if (starter) {
            starter = !starter;
            this.refreshContainer.setRefreshing(true);
            this.refreshTransactions();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        this.refreshTransactions();
    }

    @Override
    public void ListItemClick(FLTransaction transac) {
        if (transac.isCollect)
            FloozApplication.getInstance().showCollect(transac);
        else
            FloozApplication.getInstance().showTransactionCard(transac);
    }

    @Override
    public void ListItemCommentClick(FLTransaction transac) {
        FloozApplication.getInstance().showTransactionCard(transac, true);

    }

    @Override
    public void ListItemImageClick(String imgUrl) {
        CustomImageViewer.start(this.parentActivity, imgUrl, FLTransaction.TransactionAttachmentType.TransactionAttachmentImage);

    }

    @Override
    public void ListItemVideoClick(String videoUrl) {
        CustomImageViewer.start(this.parentActivity, videoUrl, FLTransaction.TransactionAttachmentType.TransactionAttachmentVideo);
    }

    @Override
    public void ListItemUserClick(FLUser user) {
        FloozApplication.getInstance().showUserProfile(user);
    }

    @Override
    public void ListItemShareClick(FLTransaction transac) {
        Intent share = new Intent(android.content.Intent.ACTION_SEND);
        share.setType("text/plain");

        share.putExtra(Intent.EXTRA_TEXT, "https://www.flooz.me/flooz/" + transac.transactionId);

        parentActivity.startActivity(Intent.createChooser(share, parentActivity.getResources().getString(R.string.SHARE_FLOOZ)));
    }

    private void loadNextPage() {
        if (nextPageUrl == null || nextPageUrl.isEmpty())
            return;

        FloozRestClient.getInstance().collectTimelineForUserNextPage(this.nextPageUrl, this.userId, this.collectId, new FloozHttpResponseHandler() {
            @Override
            public void success(Object response) {
                @SuppressWarnings("unchecked")
                Map<String, Object> responseMap = (Map<String, Object>) response;

                transactions.addAll((List<FLTransaction>) responseMap.get("transactions"));
                nextPageUrl = (String) responseMap.get("nextUrl");

                if (nextPageUrl == null || nextPageUrl.isEmpty())
                    timelineAdapter.hasNextURL = false;
                else
                    timelineAdapter.hasNextURL = true;

                timelineAdapter.notifyDataSetChanged();
            }

            @Override
            public void failure(int statusCode, FLError error) {

            }
        });
    }

    public void refreshTransactions() {
        if (this.userId != null && this.collectId != null && !this.collectId.isEmpty() && !this.userId.isEmpty()) {
            FloozRestClient.getInstance().collectTimelineForUser(this.userId, this.collectId, new FloozHttpResponseHandler() {
                @Override
                public void success(Object response) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> responseMap = (Map<String, Object>) response;

                    if (responseMap.containsKey("transactions") && responseMap.get("transactions") != null && responseMap.get("transactions") instanceof List) {
                        if (transactions != null)
                            transactions.clear();
                        else
                            transactions = new ArrayList<>();

                        transactions.addAll((List<FLTransaction>) responseMap.get("transactions"));
                        nextPageUrl = (String) responseMap.get("nextUrl");

                        if (timelineAdapter == null) {
                            timelineAdapter = new TimelineListAdapter(FloozApplication.getAppContext(), transactions);
                            timelineAdapter.delegate = CollectParticipationController.this;
                        }

                        if (nextPageUrl == null || nextPageUrl.isEmpty())
                            timelineAdapter.hasNextURL = false;
                        else
                            timelineAdapter.hasNextURL = true;

                        timelineAdapter.notifyDataSetChanged();

                        refreshContainer.setRefreshing(false);
                    } else {
                        refreshContainer.setRefreshing(false);
                    }
                }

                @Override
                public void failure(int statusCode, FLError error) {
                    refreshContainer.setRefreshing(false);
                }
            });
        }
    }
}
