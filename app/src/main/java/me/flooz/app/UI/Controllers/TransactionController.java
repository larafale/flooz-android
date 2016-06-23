package me.flooz.app.UI.Controllers;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.makeramen.roundedimageview.RoundedImageView;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import me.flooz.app.Adapter.CollectAdapter;
import me.flooz.app.Adapter.CollectParticipantAdapter;
import me.flooz.app.Adapter.TransactionAdapter;
import me.flooz.app.App.FloozApplication;
import me.flooz.app.Model.FLError;
import me.flooz.app.Model.FLTransaction;
import me.flooz.app.Network.FloozHttpResponseHandler;
import me.flooz.app.Network.FloozRestClient;
import me.flooz.app.R;
import me.flooz.app.UI.Activity.HomeActivity;
import me.flooz.app.UI.Activity.ShareCollectAcivity;
import me.flooz.app.UI.Fragment.Home.TabFragments.CollectParticipantFragment;
import me.flooz.app.UI.Tools.ActionSheet;
import me.flooz.app.UI.Tools.ActionSheetItem;
import me.flooz.app.UI.Tools.CustomImageViewer;
import me.flooz.app.UI.View.LoadingImageView;
import me.flooz.app.Utils.CustomFonts;
import me.flooz.app.Utils.CustomNotificationIntents;
import me.flooz.app.Utils.FLHelper;
import me.flooz.app.Utils.FLTriggerManager;

/**
 * Created by Flooz on 23/06/16.
 */
public class TransactionController extends BaseController {

    private Boolean viewCreated = false;

    public Boolean insertComment = false;

    private FLTransaction transaction = null;

    private Boolean isCommenting = false;

    private ListView listView;
    private View headerListView;

    private LinearLayout navbarCenterItem;
    private TextView navbarLabel;
    private RoundedImageView navbarCreatorAvatar;
    private TextView navbarCreatorUsername;
    private ImageView navbarScope;

    private RoundedImageView fromPic;
    private RoundedImageView toPic;
    private TextView fromUsername;
    private TextView fromFullname;
    private ImageView fromCertified;
    private TextView toUsername;
    private TextView toFullname;
    private ImageView toCertified;
    private TextView amount;
    private TextView text3d;
    private TextView desc;
    private LoadingImageView pic;

    private TextView locationText;
    private ImageView locationImg;
    private LinearLayout locationLayout;

    private ImageView socialLikesImg;
    private LinearLayout socialLikesLayout;

    private LinearLayout socialCommentButton;
    private ImageView socialCommentButtonImg;

    private LinearLayout socialShareButton;
    private ImageView socialShareButtonImg;

    private LinearLayout socialMoreButton;
    private ImageView socialMoreButtonImg;

    private RelativeLayout toolbar;
    private ImageView closeCommentButton;
    private ImageView shareButton;
    private EditText commentTextField;
    private TextView sendCommentButton;
    private ImageView commentButton;
    private LinearLayout actionLayout;
    private Button participateButton;
    private Button closeCollectButton;
    private View actionSeparator;

    private TransactionAdapter listAdapter;

    private BroadcastReceiver reloadCollect = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, final Intent intent) {
            if (intent.hasExtra("id") && intent.getStringExtra("id").contentEquals(transaction.transactionId)) {
                if (intent.hasExtra("flooz")) {
                    try {
                        JSONObject floozData = new JSONObject(intent.getStringExtra("flooz"));
                        FLTransaction transac = new FLTransaction(floozData);
                        setCollect(transac);

                        if (intent.hasExtra("commentId")) {
                            scrollListViewToBottom();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        FloozRestClient.getInstance().transactionWithId(transaction.transactionId, new FloozHttpResponseHandler() {
                            @Override
                            public void success(Object response) {
                                FLTransaction transac = new FLTransaction(((JSONObject) response).optJSONObject("item"));
                                setCollect(transac);

                                if (intent.hasExtra("commentId")) {
                                    scrollListViewToBottom();
                                }
                            }

                            @Override
                            public void failure(int statusCode, FLError error) {

                            }
                        });
                    }
                } else {
                    FloozRestClient.getInstance().transactionWithId(transaction.transactionId, new FloozHttpResponseHandler() {
                        @Override
                        public void success(Object response) {
                            FLTransaction transac = new FLTransaction(((JSONObject) response).optJSONObject("item"));
                            setCollect(transac);

                            if (intent.hasExtra("commentId")) {
                                scrollListViewToBottom();
                            }
                        }

                        @Override
                        public void failure(int statusCode, FLError error) {

                        }
                    });
                }
            }
        }
    };

    private BroadcastReceiver reloadCollectReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            FloozRestClient.getInstance().transactionWithId(transaction.transactionId, new FloozHttpResponseHandler() {
                @Override
                public void success(Object response) {
                    FLTransaction transac = new FLTransaction(((JSONObject) response).optJSONObject("item"));
                    setCollect(transac);
                }

                @Override
                public void failure(int statusCode, FLError error) {

                }
            });
        }
    };

    public TransactionController(@NonNull View mainView, @NonNull Activity parentActivity, @NonNull ControllerKind kind) {
        super(mainView, parentActivity, kind);

        this.init();
    }

    public TransactionController(@NonNull View mainView, @NonNull Activity parentActivity, @NonNull ControllerKind kind, @Nullable JSONObject data) {
        super(mainView, parentActivity, kind, data);

        this.init();
    }

    @Override
    protected void init() {
        super.init();

        listView = (ListView) currentView.findViewById(R.id.transaction_comment_list);
        listAdapter = new TransactionAdapter(parentActivity);

        headerListView = parentActivity.getLayoutInflater().inflate(R.layout.transaction_header_view, null);

        navbarCenterItem = (LinearLayout) currentView.findViewById(R.id.header_item_center);
        navbarLabel = (TextView) currentView.findViewById(R.id.header_item_center_label);
        navbarCreatorAvatar = (RoundedImageView) currentView.findViewById(R.id.header_item_center_avatar);
        navbarCreatorUsername = (TextView) currentView.findViewById(R.id.header_item_center_username);
        navbarScope = (ImageView) currentView.findViewById(R.id.header_item_right);

        headerTitle = (TextView) headerListView.findViewById(R.id.transaction_header_view_title);
        headerAmount = (TextView) headerListView.findViewById(R.id.transaction_header_view_amount);
        headerCurrency = (TextView) headerListView.findViewById(R.id.transaction_header_view_currency);
        headerCollectedLabel = (TextView) headerListView.findViewById(R.id.transaction_header_view_transactioned_label);
        contentCloseLabel = (TextView) headerListView.findViewById(R.id.transaction_header_view_close);
        contentDescriptionHint = (TextView) headerListView.findViewById(R.id.transaction_header_view_description_hint);
        contentDescription = (TextView) headerListView.findViewById(R.id.transaction_header_view_description);
        contentLocation = (TextView) headerListView.findViewById(R.id.transaction_header_view_location_text);
        contentAttachmentView = (LoadingImageView) headerListView.findViewById(R.id.transaction_header_view_attachment);
        contentLocationView = (LinearLayout) headerListView.findViewById(R.id.transaction_header_view_location_layout);
        contentLocationIcon = (ImageView) headerListView.findViewById(R.id.transaction_header_view_location_img);

        toolbar = (RelativeLayout) currentView.findViewById(R.id.transaction_view_toolbar);
        closeCommentButton = (ImageView) currentView.findViewById(R.id.transaction_view_comment_close);
        shareButton = (ImageView) currentView.findViewById(R.id.transaction_view_share);
        commentTextField = (EditText) currentView.findViewById(R.id.transaction_view_comment_textfield);
        sendCommentButton = (TextView) currentView.findViewById(R.id.transaction_view_comment_send);
        commentButton = (ImageView) currentView.findViewById(R.id.transaction_view_comment);
        actionLayout = (LinearLayout) currentView.findViewById(R.id.transaction_view_action);
        participateButton = (Button) currentView.findViewById(R.id.transaction_view_participate_button);
        closeCollectButton = (Button) currentView.findViewById(R.id.transaction_view_close_button);
        actionSeparator = currentView.findViewById(R.id.transaction_view_action_separator);

        navbarLabel.setTypeface(CustomFonts.customTitleLight(parentActivity));
        navbarCreatorUsername.setTypeface(CustomFonts.customTitleLight(parentActivity));
        headerTitle.setTypeface(CustomFonts.customContentRegular(parentActivity));
        headerAmount.setTypeface(CustomFonts.customContentBold(parentActivity));
        headerCurrency.setTypeface(CustomFonts.customContentBold(parentActivity));
        headerCollectedLabel.setTypeface(CustomFonts.customContentRegular(parentActivity));
        contentCloseLabel.setTypeface(CustomFonts.customContentBold(parentActivity));
        contentDescriptionHint.setTypeface(CustomFonts.customContentBold(parentActivity));
        contentDescription.setTypeface(CustomFonts.customContentRegular(parentActivity));
        contentLocation.setTypeface(CustomFonts.customContentRegular(parentActivity));
        commentTextField.setTypeface(CustomFonts.customContentRegular(parentActivity));
        sendCommentButton.setTypeface(CustomFonts.customContentLight(parentActivity));
        participateButton.setTypeface(CustomFonts.customContentRegular(parentActivity));
        closeCollectButton.setTypeface(CustomFonts.customContentRegular(parentActivity));

        contentLocationIcon.setColorFilter(parentActivity.getResources().getColor(R.color.placeholder));
        navbarScope.setColorFilter(parentActivity.getResources().getColor(android.R.color.white));

        closeCommentButton.setColorFilter(parentActivity.getResources().getColor(android.R.color.white));
        commentButton.setColorFilter(parentActivity.getResources().getColor(android.R.color.white));
        shareButton.setColorFilter(parentActivity.getResources().getColor(android.R.color.white));

        navbarCenterItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)  {
                FloozApplication.getInstance().showUserProfile(transaction.creator);
            }
        });

        listView.addHeaderView(headerListView);
        listView.setAdapter(listAdapter);

        this.contentAttachmentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CustomImageViewer.start(parentActivity, transaction.attachmentURL);
            }
        });

        this.commentTextField.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    isCommenting = true;
                    reloadView();
                } else {
                    isCommenting = false;
                    reloadView();
                }
            }
        });

        this.sendCommentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (commentTextField.getText().length() > 0 && commentTextField.getText().length() < 140) {
                    FloozRestClient.getInstance().commentTransaction(transaction.transactionId, commentTextField.getText().toString(), new FloozHttpResponseHandler() {
                        @Override
                        public void success(Object response) {
                            transaction.setJson((JSONObject) response);
                            reloadView();
                        }

                        @Override
                        public void failure(int statusCode, FLError error) {

                        }
                    });

                    commentTextField.setText("");
                    commentTextField.clearFocus();
                    InputMethodManager imm = (InputMethodManager) parentActivity.getSystemService(
                            Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(parentActivity.getWindow().getDecorView().getRootView().getWindowToken(), 0);
                }
            }
        });

        participateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FLTriggerManager.getInstance().executeTriggerList(FLTriggerManager.convertTriggersJSONArrayToList(transaction.actions.optJSONArray("participate")));
            }
        });

        closeCollectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FLTriggerManager.getInstance().executeTriggerList(FLTriggerManager.convertTriggersJSONArrayToList(transaction.actions.optJSONArray("close")));
            }
        });

        commentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isCommenting = true;
                reloadView();

                commentTextField.requestFocus();
                InputMethodManager imm = (InputMethodManager) parentActivity.getSystemService(
                        Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(commentTextField, InputMethodManager.SHOW_FORCED);
            }
        });

        shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (transaction.actions == null || transaction.actions.length() == 0) {
                    Intent share = new Intent(Intent.ACTION_SEND);
                    share.setType("text/plain");

                    share.putExtra(Intent.EXTRA_TEXT, "https://www.flooz.me/pot/" + transaction.transactionId);

                    parentActivity.startActivity(Intent.createChooser(share, parentActivity.getResources().getString(R.string.SHARE_COLLECT)));
                } else {
                    List<ActionSheetItem> items = new ArrayList<>();
                    items.add(new ActionSheetItem(parentActivity, "Inviter vos amis", new ActionSheetItem.ActionSheetItemClickListener() {
                        @Override
                        public void onClick() {
                            Intent intent = new Intent(parentActivity, ShareCollectAcivity.class);
                            intent.putExtra("potId", transaction.transactionId);
                            parentActivity.startActivity(intent);
                            parentActivity.overridePendingTransition(R.anim.slide_up, android.R.anim.fade_out);
                        }
                    }));

                    items.add(new ActionSheetItem(parentActivity, "Partager le lien", new ActionSheetItem.ActionSheetItemClickListener() {
                        @Override
                        public void onClick() {
                            Intent share = new Intent(Intent.ACTION_SEND);
                            share.setType("text/plain");

                            share.putExtra(Intent.EXTRA_TEXT, "https://www.flooz.me/pot/" + transaction.transactionId);

                            parentActivity.startActivity(Intent.createChooser(share, parentActivity.getResources().getString(R.string.SHARE_COLLECT)));
                        }
                    }));

                    ActionSheet.showWithItems(parentActivity, items);
                }
            }
        });

        closeCommentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                commentTextField.setText("");
                commentTextField.clearFocus();
                InputMethodManager imm = (InputMethodManager) parentActivity.getSystemService(
                        Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(parentActivity.getWindow().getDecorView().getRootView().getWindowToken(), 0);
            }
        });

        this.viewCreated = true;

        if (this.transaction != null)
            this.reloadView();
    }

    private void reloadView() {
        if (!this.viewCreated)
            return;

        navbarCreatorAvatar.setImageDrawable(parentActivity.getResources().getDrawable(R.drawable.avatar_default));
        if (this.transaction.creator.avatarURL != null && !this.transaction.creator.avatarURL.isEmpty())
            ImageLoader.getInstance().displayImage(this.transaction.creator.avatarURL, navbarCreatorAvatar);

        navbarCreatorUsername.setText("@" + this.transaction.creator.username);

        headerTitle.setText(this.transaction.name);
        headerAmount.setText(FLHelper.trimTrailingZeros(String.format(Locale.US, "%.2f", this.transaction.amount.floatValue())));

        this.navbarScope.setImageDrawable(FLTransaction.transactionScopeToImage(this.transaction.scope));

        if (this.transaction.attachmentURL != null &&!this.transaction.attachmentURL.isEmpty()) {
            this.contentAttachmentView.setImageFromUrl(this.transaction.attachmentURL);
            this.contentAttachmentView.setVisibility(View.VISIBLE);
        }
        else {
            this.contentAttachmentView.setVisibility(View.GONE);
        }

        if (this.transaction.status == FLTransaction.TransactionStatus.TransactionStatusPending) {
            this.contentCloseLabel.setVisibility(View.VISIBLE);
        } else {
            this.contentCloseLabel.setVisibility(View.GONE);
        }

        this.contentDescription.setText(this.transaction.content);

        if (this.transaction.location != null && !this.transaction.location.isEmpty()) {
            contentLocationView.setVisibility(View.VISIBLE);
            contentLocation.setText(this.transaction.location);
        } else {
            contentLocationView.setVisibility(View.GONE);
        }

        if (isCommenting) {
            commentTextField.setVisibility(View.VISIBLE);
            actionLayout.setVisibility(View.GONE);
            closeCommentButton.setVisibility(View.VISIBLE);
            sendCommentButton.setVisibility(View.VISIBLE);
            commentButton.setVisibility(View.INVISIBLE);
            shareButton.setVisibility(View.INVISIBLE);
        } else {
            if (this.transaction.isAvailable && this.transaction.isClosable) {
                actionLayout.setVisibility(View.VISIBLE);
                participateButton.setVisibility(View.VISIBLE);
                closeCollectButton.setVisibility(View.VISIBLE);
                actionSeparator.setVisibility(View.VISIBLE);
                commentTextField.setVisibility(View.GONE);
                closeCommentButton.setVisibility(View.INVISIBLE);
                sendCommentButton.setVisibility(View.INVISIBLE);
                commentButton.setVisibility(View.VISIBLE);
                shareButton.setVisibility(View.VISIBLE);

                participateButton.setTextSize(16);
                closeCollectButton.setTextSize(16);
            } else if (this.transaction.isAvailable) {
                actionLayout.setVisibility(View.VISIBLE);
                participateButton.setVisibility(View.VISIBLE);
                closeCollectButton.setVisibility(View.GONE);
                actionSeparator.setVisibility(View.GONE);
                commentTextField.setVisibility(View.GONE);
                closeCommentButton.setVisibility(View.INVISIBLE);
                sendCommentButton.setVisibility(View.INVISIBLE);
                commentButton.setVisibility(View.VISIBLE);
                shareButton.setVisibility(View.VISIBLE);

                participateButton.setTextSize(20);
                closeCollectButton.setTextSize(20);
            } else if (this.transaction.isClosable) {
                actionLayout.setVisibility(View.VISIBLE);
                participateButton.setVisibility(View.GONE);
                closeCollectButton.setVisibility(View.VISIBLE);
                actionSeparator.setVisibility(View.GONE);
                commentTextField.setVisibility(View.GONE);
                closeCommentButton.setVisibility(View.INVISIBLE);
                sendCommentButton.setVisibility(View.INVISIBLE);
                commentButton.setVisibility(View.VISIBLE);
                shareButton.setVisibility(View.VISIBLE);

                participateButton.setTextSize(20);
                closeCollectButton.setTextSize(20);
            } else {
                actionLayout.setVisibility(View.GONE);

                if (isCommenting) {
                    closeCommentButton.setVisibility(View.VISIBLE);
                    shareButton.setVisibility(View.INVISIBLE);
                } else {
                    closeCommentButton.setVisibility(View.INVISIBLE);
                    shareButton.setVisibility(View.VISIBLE);
                }

                commentTextField.setVisibility(View.VISIBLE);
                sendCommentButton.setVisibility(View.VISIBLE);
                commentButton.setVisibility(View.INVISIBLE);
            }
        }

        this.listAdapter.reload();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (this.insertComment) {
            commentButton.performClick();
        }

        LocalBroadcastManager.getInstance(FloozApplication.getAppContext()).registerReceiver(reloadCollectReceiver,
                CustomNotificationIntents.filterReloadTimeline());

        LocalBroadcastManager.getInstance(FloozApplication.getAppContext()).registerReceiver(reloadCollect,
                CustomNotificationIntents.filterReloadTransaction());

        LocalBroadcastManager.getInstance(FloozApplication.getAppContext()).registerReceiver(reloadCollect,
                CustomNotificationIntents.filterReloadCollect());
    }

    public void transactionLikeClicked() {
        if (this.transaction.social.isLiked) {
            this.transaction.social.isLiked = false;
            this.transaction.social.likesCount = this.transaction.social.likesCount.intValue() - 1;

            this.listAdapter.reload();
        }
        else {
            this.transaction.social.isLiked = true;
            this.transaction.social.likesCount = this.transaction.social.likesCount.intValue() + 1;

            this.listAdapter.reload();
        }

        FloozRestClient.getInstance().likeTransaction(this.transaction.transactionId, new FloozHttpResponseHandler() {
            @Override
            public void success(Object response) {
                transaction.setJson((JSONObject) response);
                reloadView();
            }

            @Override
            public void failure(int statusCode, FLError error) {
                if (transaction.social.isLiked) {
                    transaction.social.isLiked = false;
                    transaction.social.likesCount = transaction.social.likesCount.intValue() - 1;

                    listAdapter.reload();
                }
                else {
                    transaction.social.isLiked = true;
                    transaction.social.likesCount = transaction.social.likesCount.intValue() + 1;

                    listAdapter.reload();
                }
            }
        });
    }

    public void transactionCommentClicked() {
        commentButton.performClick();
    }

    public void transactionReport() {
        FloozApplication.getInstance().showReportActionMenu(this.transaction);
    }

    private void scrollListViewToBottom() {
        listView.post(new Runnable() {
            @Override
            public void run() {
                listView.setSelection(listAdapter.getCount() - 1);
            }
        });
    }

    public void setCollect(@NonNull  FLTransaction transaction) {
        this.transaction = transaction;

        this.listAdapter.setCollect(transaction);

        this.reloadView();
    }
}
