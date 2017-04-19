package me.flooz.app.UI.Controllers;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.Gravity;
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

import java.text.SimpleDateFormat;
import java.util.Locale;

import me.flooz.app.Adapter.TransactionAdapter;
import me.flooz.app.App.FloozApplication;
import me.flooz.app.Model.FLError;
import me.flooz.app.Model.FLTransaction;
import me.flooz.app.Network.FloozHttpResponseHandler;
import me.flooz.app.Network.FloozRestClient;
import me.flooz.app.R;
import me.flooz.app.UI.Activity.HomeActivity;
import me.flooz.app.UI.Activity.SocialLikesActivity;
import me.flooz.app.UI.Fragment.Home.TabFragments.SocialLikesFragment;
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

    private TextView cardHeaderDate;
    private ImageView cardHeaderScope;

    private ImageView arrowPic;
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

    private RelativeLayout socialLabelsContainer;
    private TextView socialLikesLabel;
    private TextView socialCommentsLabel;

    private LinearLayout socialLikesButton;
    private ImageView socialLikesButtonImg;

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
    private Button acceptButton;
    private Button declineButton;
    private View actionSeparator;

    private TransactionAdapter listAdapter;

    private BroadcastReceiver reloadTransaction = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, final Intent intent) {
            if (intent.hasExtra("id") && intent.getStringExtra("id").contentEquals(transaction.transactionId)) {
                if (intent.hasExtra("flooz")) {
                    try {
                        JSONObject floozData = new JSONObject(intent.getStringExtra("flooz"));
                        FLTransaction transac = new FLTransaction(floozData);
                        setTransaction(transac);

                        if (intent.hasExtra("commentId")) {
                            scrollListViewToBottom();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        FloozRestClient.getInstance().transactionWithId(transaction.transactionId, new FloozHttpResponseHandler() {
                            @Override
                            public void success(Object response) {
                                FLTransaction transac = new FLTransaction(((JSONObject) response).optJSONObject("item"));
                                setTransaction(transac);

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
                            setTransaction(transac);

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

    private BroadcastReceiver reloadTransactionReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            FloozRestClient.getInstance().transactionWithId(transaction.transactionId, new FloozHttpResponseHandler() {
                @Override
                public void success(Object response) {
                    FLTransaction transac = new FLTransaction(((JSONObject) response).optJSONObject("item"));
                    setTransaction(transac);
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

        headerListView = parentActivity.getLayoutInflater().inflate(R.layout.transaction_card_header_view, null);

        this.cardHeaderScope = (ImageView) this.currentView.findViewById(R.id.transac_card_header_scope);
        this.cardHeaderDate = (TextView) this.currentView.findViewById(R.id.transac_card_header_date);

        this.arrowPic = (ImageView) this.headerListView.findViewById(R.id.transac_card_arrow);
        this.fromPic = (RoundedImageView) this.headerListView.findViewById(R.id.transac_card_from_pic);
        this.toPic = (RoundedImageView) this.headerListView.findViewById(R.id.transac_card_to_pic);
        this.fromUsername = (TextView) this.headerListView.findViewById(R.id.transac_card_from_username);
        this.fromFullname = (TextView) this.headerListView.findViewById(R.id.transac_card_from_fullname);
        this.fromCertified = (ImageView) this.headerListView.findViewById(R.id.transac_card_from_certified);
        this.toUsername = (TextView) this.headerListView.findViewById(R.id.transac_card_to_username);
        this.toFullname = (TextView) this.headerListView.findViewById(R.id.transac_card_to_fullname);
        this.toCertified = (ImageView) this.headerListView.findViewById(R.id.transac_card_to_certified);
        this.amount = (TextView) this.headerListView.findViewById(R.id.transac_card_amount);
        this.text3d = (TextView) this.headerListView.findViewById(R.id.transac_card_3dText);
        this.desc = (TextView) this.headerListView.findViewById(R.id.transac_card_desc);
        this.pic = (LoadingImageView) this.headerListView.findViewById(R.id.transac_card_pic);
        this.locationImg = (ImageView) this.headerListView.findViewById(R.id.transac_card_location_img);
        this.locationLayout = (LinearLayout) this.headerListView.findViewById(R.id.transac_card_location);
        this.locationText = (TextView) this.headerListView.findViewById(R.id.transac_card_location_text);

        this.socialLabelsContainer = (RelativeLayout) this.headerListView.findViewById(R.id.transac_card_social_labels);
        this.socialLikesLabel = (TextView) this.headerListView.findViewById(R.id.transac_card_social_like_label);
        this.socialCommentsLabel = (TextView) this.headerListView.findViewById(R.id.transac_card_social_comment_label);

        this.socialLikesButton = (LinearLayout) this.headerListView.findViewById(R.id.transac_card_social_like);
        this.socialLikesButtonImg = (ImageView) this.headerListView.findViewById(R.id.transac_card_social_like_img);

        this.socialCommentButton = (LinearLayout) this.headerListView.findViewById(R.id.transac_card_social_comment);
        this.socialCommentButtonImg = (ImageView) this.headerListView.findViewById(R.id.transac_card_social_comment_img);

        this.socialShareButton = (LinearLayout) this.headerListView.findViewById(R.id.transac_card_social_share);
        this.socialShareButtonImg = (ImageView) this.headerListView.findViewById(R.id.transac_card_social_share_img);

        this.socialMoreButton = (LinearLayout) this.headerListView.findViewById(R.id.transac_card_social_more);
        this.socialMoreButtonImg = (ImageView) this.headerListView.findViewById(R.id.transac_card_social_more_img);

        this.toolbar = (RelativeLayout) this.currentView.findViewById(R.id.transaction_view_toolbar);
        this.closeCommentButton = (ImageView) this.currentView.findViewById(R.id.transaction_view_comment_close);
        this.shareButton = (ImageView) this.currentView.findViewById(R.id.transaction_view_share);
        this.commentTextField = (EditText) this.currentView.findViewById(R.id.transaction_view_comment_textfield);
        this.sendCommentButton = (TextView) this.currentView.findViewById(R.id.transaction_view_comment_send);
        this.commentButton = (ImageView) this.currentView.findViewById(R.id.transaction_view_comment);
        this.actionLayout = (LinearLayout) this.currentView.findViewById(R.id.transaction_view_action);
        this.acceptButton = (Button) this.currentView.findViewById(R.id.transaction_view_accept_button);
        this.declineButton = (Button) this.currentView.findViewById(R.id.transaction_view_decline_button);
        this.actionSeparator = this.currentView.findViewById(R.id.transaction_view_action_separator);

        this.cardHeaderDate.setTypeface(CustomFonts.customTitleExtraLight(this.parentActivity));
        this.fromUsername.setTypeface(CustomFonts.customTitleExtraLight(this.parentActivity), Typeface.BOLD);
        this.fromFullname.setTypeface(CustomFonts.customTitleExtraLight(this.parentActivity));
        this.toUsername.setTypeface(CustomFonts.customTitleExtraLight(this.parentActivity), Typeface.BOLD);
        this.toFullname.setTypeface(CustomFonts.customTitleExtraLight(this.parentActivity));
        this.amount.setTypeface(CustomFonts.customContentBold(this.parentActivity), Typeface.BOLD);
        this.text3d.setTypeface(CustomFonts.customContentRegular(this.parentActivity));
        this.desc.setTypeface(CustomFonts.customContentLight(this.parentActivity));
        this.locationText.setTypeface(CustomFonts.customContentRegular(this.parentActivity));
        this.socialLikesLabel.setTypeface(CustomFonts.customContentRegular(this.parentActivity));
        this.socialCommentsLabel.setTypeface(CustomFonts.customContentRegular(this.parentActivity));
        this.commentTextField.setTypeface(CustomFonts.customContentRegular(this.parentActivity));
        this.sendCommentButton.setTypeface(CustomFonts.customContentLight(this.parentActivity));
        this.acceptButton.setTypeface(CustomFonts.customContentRegular(this.parentActivity));
        this.declineButton.setTypeface(CustomFonts.customContentRegular(this.parentActivity));

        this.arrowPic.setColorFilter(this.parentActivity.getResources().getColor(R.color.background));
        this.cardHeaderScope.setColorFilter(Color.WHITE);
        this.locationImg.setColorFilter(this.parentActivity.getResources().getColor(R.color.placeholder));
        this.socialLikesButtonImg.setColorFilter(this.parentActivity.getResources().getColor(R.color.background_social_button));
        this.socialCommentButtonImg.setColorFilter(this.parentActivity.getResources().getColor(R.color.background_social_button));
        this.socialShareButtonImg.setColorFilter(this.parentActivity.getResources().getColor(R.color.background_social_button));
        this.socialMoreButtonImg.setColorFilter(this.parentActivity.getResources().getColor(R.color.background_social_button));
        this.closeCommentButton.setColorFilter(Color.WHITE);
        this.commentButton.setColorFilter(Color.WHITE);
        this.shareButton.setColorFilter(Color.WHITE);

        listView.addHeaderView(headerListView);
        listView.setAdapter(listAdapter);

        this.fromPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FloozApplication.getInstance().showUserProfile(transaction.from);
            }
        });

        this.toPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (transaction.to.isPot) {
                    FloozRestClient.getInstance().showLoadView();
                    FloozRestClient.getInstance().transactionWithId(transaction.to.userId, new FloozHttpResponseHandler() {
                        @Override
                        public void success(Object response) {
                            FLTransaction transac = new FLTransaction(((JSONObject) response).optJSONObject("item"));
                            FloozApplication.getInstance().showCollect(transac);
                        }

                        @Override
                        public void failure(int statusCode, FLError error) {

                        }
                    });
                } else {
                    FloozApplication.getInstance().showUserProfile(transaction.to);
                }
            }
        });

        this.pic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CustomImageViewer.start(parentActivity, transaction.attachmentURL, transaction.attachmentType);
            }
        });

        this.socialLikesLabel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentKind == ControllerKind.FRAGMENT_CONTROLLER) {
                    SocialLikesFragment fragment = new SocialLikesFragment();
                    fragment.transaction = transaction;

                    ((HomeActivity)parentActivity).pushFragmentInCurrentTab(fragment);
                } else {
                    Intent likeIntent = new Intent(parentActivity, SocialLikesActivity.class);
                    likeIntent.putExtra("transaction", transaction.json.toString());
                    parentActivity.startActivity(likeIntent);
                    parentActivity.overridePendingTransition(R.anim.slide_up, android.R.anim.fade_out);
                }
            }
        });

        this.socialLikesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                transactionLikeClicked();
            }
        });

        this.socialCommentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                transactionCommentClicked();
            }
        });

        this.socialShareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent share = new Intent(Intent.ACTION_SEND);
                share.setType("text/plain");

                share.putExtra(Intent.EXTRA_TEXT, "https://www.flooz.me/flooz/" + transaction.transactionId);

                parentActivity.startActivity(Intent.createChooser(share, parentActivity.getResources().getString(R.string.SHARE_FLOOZ)));            }
        });

        this.socialMoreButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                transactionReport();
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
                            scrollListViewToBottom();
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

        acceptButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FLTriggerManager.getInstance().executeTriggerList(FLTriggerManager.convertTriggersJSONArrayToList(transaction.actions.optJSONArray("accept")));
            }
        });

        declineButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FLTriggerManager.getInstance().executeTriggerList(FLTriggerManager.convertTriggersJSONArrayToList(transaction.actions.optJSONArray("decline")));
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
                Intent share = new Intent(Intent.ACTION_SEND);
                share.setType("text/plain");

                share.putExtra(Intent.EXTRA_TEXT, "https://www.flooz.me/flooz/" + transaction.transactionId);

                parentActivity.startActivity(Intent.createChooser(share, parentActivity.getResources().getString(R.string.SHARE_FLOOZ)));
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

        this.transaction.scope.displayImage(this.cardHeaderScope);

        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM à HH:mm", Locale.FRANCE);

        this.cardHeaderDate.setText(sdf.format(this.transaction.date.getDate()));

        this.fromPic.setImageDrawable(null);
        if (this.transaction.from.avatarURL != null && !this.transaction.from.avatarURL.isEmpty())
            ImageLoader.getInstance().displayImage(this.transaction.from.avatarURL, this.fromPic);
        else
            this.fromPic.setImageDrawable(this.parentActivity.getResources().getDrawable(R.drawable.avatar_default));

        if (this.transaction.from.username != null) {
            this.fromUsername.setVisibility(View.VISIBLE);
            this.fromUsername.setText("@" + this.transaction.from.username);
        }
        else {
            this.fromUsername.setVisibility(View.INVISIBLE);
        }

        this.fromFullname.setText(this.transaction.from.fullname);

        if (this.transaction.from.isCertified) {
            this.fromCertified.setVisibility(View.VISIBLE);
        } else {
            this.fromCertified.setVisibility(View.GONE);
        }

        if (this.transaction.to.isPot) {
            this.toUsername.setVisibility(View.INVISIBLE);
            this.toPic.setImageDrawable(parentActivity.getResources().getDrawable(R.drawable.default_pot_avatar));
        } else {
            if (this.transaction.to.username != null) {
                this.toUsername.setVisibility(View.VISIBLE);
                this.toUsername.setText("@" + this.transaction.to.username);
            }
            else {
                this.toUsername.setVisibility(View.INVISIBLE);
            }

            this.toPic.setImageDrawable(null);
            if (this.transaction.to.avatarURL != null && !this.transaction.to.avatarURL.isEmpty())
                ImageLoader.getInstance().displayImage(this.transaction.to.avatarURL, this.toPic);
            else
                this.toPic.setImageDrawable(this.parentActivity.getResources().getDrawable(R.drawable.avatar_default));
        }

        this.toFullname.setText(this.transaction.to.fullname);

        if (this.transaction.to.isCertified) {
            this.toCertified.setVisibility(View.VISIBLE);
        } else {
            this.toCertified.setVisibility(View.GONE);
        }

        if (this.transaction.amountText != null && !this.transaction.amountText.isEmpty()) {
            this.amount.setText(this.transaction.amountText);
            this.amount.setVisibility(View.VISIBLE);
        }
        else {
            this.amount.setText("");
            this.amount.setVisibility(View.GONE);
        }

        final SpannableStringBuilder sb = new SpannableStringBuilder(this.transaction.text3d.get(0).toString() + this.transaction.text3d.get(1).toString() + this.transaction.text3d.get(2).toString());

        final ForegroundColorSpan usernameColor = new ForegroundColorSpan(this.parentActivity.getResources().getColor(android.R.color.white));
        final ForegroundColorSpan username2Color = new ForegroundColorSpan(this.parentActivity.getResources().getColor(android.R.color.white));
        final ForegroundColorSpan textColor = new ForegroundColorSpan(this.parentActivity.getResources().getColor(R.color.placeholder));
        final StyleSpan usernameBold = new StyleSpan(Typeface.BOLD);
        final StyleSpan username2Bold = new StyleSpan(Typeface.BOLD);

        int pos = 0;

        sb.setSpan(usernameColor, pos, pos + this.transaction.text3d.get(0).toString().length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        sb.setSpan(usernameBold, pos, pos + this.transaction.text3d.get(0).toString().length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);

        pos += this.transaction.text3d.get(0).toString().length();

        sb.setSpan(textColor, pos, pos + this.transaction.text3d.get(1).toString().length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);

        pos += this.transaction.text3d.get(1).toString().length();

        sb.setSpan(username2Color, pos, pos + this.transaction.text3d.get(2).toString().length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        sb.setSpan(username2Bold, pos, pos + this.transaction.text3d.get(2).toString().length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);

        this.text3d.setText(sb);

        this.desc.setText(this.transaction.content);

        if (this.transaction.attachmentURL != null && !this.transaction.attachmentURL.isEmpty()) {
            this.pic.setImageFromUrl(this.transaction.attachmentURL);
            this.pic.setVisibility(View.VISIBLE);
        } else
            this.pic.setVisibility(View.GONE);

        if (this.transaction.location != null && !this.transaction.location.isEmpty()) {
            this.locationText.setText(this.transaction.location.toCharArray(), 0, this.transaction.location.length());
            this.locationLayout.setVisibility(View.VISIBLE);
        } else {
            this.locationLayout.setVisibility(View.GONE);
        }

        if (transaction.social != null) {
            if ((transaction.social.likesCount.intValue() > 0 && transaction.options.likeEnabled) || (transaction.social.commentsCount.intValue() > 0 && transaction.options.commentEnabled)) {
                this.socialLabelsContainer.setVisibility(View.VISIBLE);

                if (transaction.social.likesCount.intValue() > 0 && transaction.options.likeEnabled) {
                    this.socialLikesLabel.setVisibility(View.VISIBLE);

                    String number = FLHelper.formatUserNumber(this.transaction.social.likesCount.longValue());
                    String likeText = " J'AIME";

                    final SpannableStringBuilder likeSb = new SpannableStringBuilder(number + likeText);

                    final ForegroundColorSpan likeNumberColor = new ForegroundColorSpan(this.parentActivity.getResources().getColor(android.R.color.white));
                    final ForegroundColorSpan likeTextColor = new ForegroundColorSpan(this.parentActivity.getResources().getColor(R.color.placeholder));
                    final StyleSpan likeStyle = new StyleSpan(Typeface.BOLD);

                    int likePos = 0;

                    likeSb.setSpan(likeNumberColor, likePos, likePos + number.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                    likeSb.setSpan(likeStyle, likePos, likePos + number.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);

                    likePos += number.length();

                    likeSb.setSpan(likeTextColor, likePos, likePos + likeText.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);

                    this.socialLikesLabel.setText(likeSb);
                } else {
                    this.socialLikesLabel.setVisibility(View.GONE);
                }

                if (transaction.social.commentsCount.intValue() > 0 && transaction.options.commentEnabled) {
                    this.socialCommentsLabel.setVisibility(View.VISIBLE);

                    final ForegroundColorSpan commentNumberColor = new ForegroundColorSpan(this.parentActivity.getResources().getColor(android.R.color.white));
                    final ForegroundColorSpan commentTextColor = new ForegroundColorSpan(this.parentActivity.getResources().getColor(R.color.placeholder));
                    final StyleSpan commentStyle = new StyleSpan(Typeface.BOLD);

                    String number = FLHelper.formatUserNumber(this.transaction.social.commentsCount.longValue());

                    String commentText = " COMMENTAIRE";

                    if (transaction.social.commentsCount.intValue() > 1)
                        commentText = " COMMENTAIRES";

                    final SpannableStringBuilder commentSb = new SpannableStringBuilder(number + commentText);

                    int commentPos = 0;

                    commentSb.setSpan(commentNumberColor, commentPos, commentPos + number.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                    commentSb.setSpan(commentStyle, commentPos, commentPos + number.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);

                    commentPos += number.length();

                    commentSb.setSpan(commentTextColor, commentPos, commentPos + commentText.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);

                    this.socialCommentsLabel.setText(commentSb);
                } else {
                    this.socialCommentsLabel.setVisibility(View.GONE);
                }
            } else {
                this.socialLabelsContainer.setVisibility(View.GONE);
            }

            if (transaction.social.isLiked && transaction.options.likeEnabled) {
                socialLikesButton.setVisibility(View.VISIBLE);
                socialLikesButtonImg.setColorFilter(this.parentActivity.getResources().getColor(R.color.pink));
            } else if (transaction.options.likeEnabled) {
                socialLikesButton.setVisibility(View.VISIBLE);
                socialLikesButtonImg.setColorFilter(this.parentActivity.getResources().getColor(R.color.background_social_button));
            } else {
                socialLikesButton.setVisibility(View.GONE);
            }

            if (transaction.social.isCommented && transaction.options.commentEnabled) {
                socialCommentButton.setVisibility(View.VISIBLE);
                socialCommentButtonImg.setColorFilter(this.parentActivity.getResources().getColor(R.color.blue));
            } else if (transaction.options.commentEnabled) {
                socialCommentButton.setVisibility(View.VISIBLE);
                socialCommentButtonImg.setColorFilter(this.parentActivity.getResources().getColor(R.color.background_social_button));
            } else {
                socialCommentButton.setVisibility(View.GONE);
            }
        } else {
            this.socialLabelsContainer.setVisibility(View.GONE);

            if (transaction.options.likeEnabled) {
                socialLikesButton.setVisibility(View.VISIBLE);
                socialLikesButtonImg.setColorFilter(this.parentActivity.getResources().getColor(R.color.background_social_button));
            } else {
                socialLikesButton.setVisibility(View.GONE);
            }

            if (transaction.options.commentEnabled) {
                socialCommentButton.setVisibility(View.VISIBLE);
                socialCommentButtonImg.setColorFilter(this.parentActivity.getResources().getColor(R.color.background_social_button));
            } else {
                socialCommentButton.setVisibility(View.GONE);
            }
        }

        if (transaction.options.shareEnabled) {
            socialShareButton.setVisibility(View.VISIBLE);

            if (!transaction.options.likeEnabled && !transaction.options.commentEnabled) {
                socialShareButton.setGravity(Gravity.LEFT);
            } else if (!transaction.options.likeEnabled || !transaction.options.commentEnabled) {
                socialShareButton.setGravity(Gravity.CENTER_HORIZONTAL);
            } else {
                socialShareButton.setGravity(Gravity.RIGHT);
            }
        } else {
            socialShareButton.setVisibility(View.GONE);
        }

        if (isCommenting) {
            commentTextField.setVisibility(View.VISIBLE);
            actionLayout.setVisibility(View.GONE);
            closeCommentButton.setVisibility(View.VISIBLE);
            sendCommentButton.setVisibility(View.VISIBLE);
            commentButton.setVisibility(View.INVISIBLE);
            shareButton.setVisibility(View.INVISIBLE);
        } else {
            if (this.transaction.isAcceptable && this.transaction.isCancelable) {
                actionLayout.setVisibility(View.VISIBLE);
                acceptButton.setVisibility(View.VISIBLE);
                declineButton.setVisibility(View.VISIBLE);
                actionSeparator.setVisibility(View.VISIBLE);
                commentTextField.setVisibility(View.GONE);
                closeCommentButton.setVisibility(View.INVISIBLE);
                sendCommentButton.setVisibility(View.INVISIBLE);

                if (this.transaction.options.commentEnabled)
                    commentButton.setVisibility(View.VISIBLE);
                else
                    commentButton.setVisibility(View.INVISIBLE);

                if (this.transaction.options.shareEnabled)
                    shareButton.setVisibility(View.VISIBLE);
                else
                    shareButton.setVisibility(View.INVISIBLE);

                acceptButton.setTextSize(16);
                declineButton.setTextSize(16);
            } else if (this.transaction.isAvailable) {
                actionLayout.setVisibility(View.VISIBLE);
                acceptButton.setVisibility(View.VISIBLE);
                declineButton.setVisibility(View.GONE);
                actionSeparator.setVisibility(View.GONE);
                commentTextField.setVisibility(View.GONE);
                closeCommentButton.setVisibility(View.INVISIBLE);
                sendCommentButton.setVisibility(View.INVISIBLE);

                if (this.transaction.options.commentEnabled)
                    commentButton.setVisibility(View.VISIBLE);
                else
                    commentButton.setVisibility(View.INVISIBLE);

                if (this.transaction.options.shareEnabled)
                    shareButton.setVisibility(View.VISIBLE);
                else
                    shareButton.setVisibility(View.INVISIBLE);

                acceptButton.setTextSize(20);
                declineButton.setTextSize(20);
            } else if (this.transaction.isClosable) {
                actionLayout.setVisibility(View.VISIBLE);
                acceptButton.setVisibility(View.GONE);
                declineButton.setVisibility(View.VISIBLE);
                actionSeparator.setVisibility(View.GONE);
                commentTextField.setVisibility(View.GONE);

                if (this.transaction.options.commentEnabled)
                    commentButton.setVisibility(View.VISIBLE);
                else
                    commentButton.setVisibility(View.INVISIBLE);

                if (this.transaction.options.shareEnabled)
                    shareButton.setVisibility(View.VISIBLE);
                else
                    shareButton.setVisibility(View.INVISIBLE);

                acceptButton.setTextSize(20);
                declineButton.setTextSize(20);
            } else {
                actionLayout.setVisibility(View.GONE);

                if (isCommenting) {
                    closeCommentButton.setVisibility(View.VISIBLE);
                    shareButton.setVisibility(View.INVISIBLE);
                } else {
                    closeCommentButton.setVisibility(View.INVISIBLE);

                    if (this.transaction.options.shareEnabled)
                        shareButton.setVisibility(View.VISIBLE);
                    else
                        shareButton.setVisibility(View.INVISIBLE);
                }

                if (!this.transaction.options.commentEnabled) {
                    this.toolbar.setVisibility(View.GONE);
                } else {
                    this.toolbar.setVisibility(View.VISIBLE);
                    this.commentTextField.setVisibility(View.VISIBLE);
                    this.sendCommentButton.setVisibility(View.VISIBLE);
                    this.commentButton.setVisibility(View.INVISIBLE);
                }
            }
        }

        this.listAdapter.notifyDataSetChanged();
    }

    @Override
    public void onStart() {
        super.onStart();

        LocalBroadcastManager.getInstance(FloozApplication.getAppContext()).registerReceiver(reloadTransactionReceiver,
                CustomNotificationIntents.filterReloadTimeline());

        LocalBroadcastManager.getInstance(FloozApplication.getAppContext()).registerReceiver(reloadTransaction,
                CustomNotificationIntents.filterReloadTransaction());

        LocalBroadcastManager.getInstance(FloozApplication.getAppContext()).registerReceiver(reloadTransaction,
                CustomNotificationIntents.filterReloadCollect());
    }

    @Override
    public void onResume() {
        if (this.insertComment) {
            reloadView();

            commentTextField.requestFocus();
            InputMethodManager imm = (InputMethodManager) parentActivity.getSystemService(
                    Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(commentTextField, InputMethodManager.SHOW_FORCED);

            this.insertComment = false;
        }

        FloozRestClient.getInstance().transactionWithId(transaction.transactionId, new FloozHttpResponseHandler() {
            @Override
            public void success(Object response) {
                FLTransaction transac = new FLTransaction(((JSONObject) response).optJSONObject("item"));
                setTransaction(transac);
            }

            @Override
            public void failure(int statusCode, FLError error) {

            }
        });
    }

    public void transactionLikeClicked() {
        if (this.transaction.social.isLiked) {
            this.transaction.social.isLiked = false;
            this.transaction.social.likesCount = this.transaction.social.likesCount.intValue() - 1;

            this.reloadView();
        }
        else {
            this.transaction.social.isLiked = true;
            this.transaction.social.likesCount = this.transaction.social.likesCount.intValue() + 1;

            this.reloadView();
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

                    reloadView();
                }
                else {
                    transaction.social.isLiked = true;
                    transaction.social.likesCount = transaction.social.likesCount.intValue() + 1;

                    reloadView();
                }
            }
        });
    }

    public void transactionCommentClicked() {
        commentButton.performClick();
    }

    public void transactionReport() {
        FLTriggerManager.getInstance().executeTriggerList(FLTriggerManager.convertTriggersJSONArrayToList(transaction.settings));
    }

    private void scrollListViewToBottom() {
        listView.post(new Runnable() {
            @Override
            public void run() {
                listView.setSelection(listAdapter.getCount() - 1);
            }
        });
    }

    public void setTransaction(@NonNull  FLTransaction transaction) {
        this.transaction = transaction;

        this.listAdapter.setTransaction(transaction);

        this.reloadView();
    }
}
