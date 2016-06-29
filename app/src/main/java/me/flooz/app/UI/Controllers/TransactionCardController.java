package me.flooz.app.UI.Controllers;

import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.makeramen.roundedimageview.RoundedImageView;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Locale;

import me.flooz.app.App.FloozApplication;
import me.flooz.app.Model.FLComment;
import me.flooz.app.Model.FLError;
import me.flooz.app.Model.FLTransaction;
import me.flooz.app.Model.FLUser;
import me.flooz.app.Network.FloozHttpResponseHandler;
import me.flooz.app.Network.FloozRestClient;
import me.flooz.app.R;
import me.flooz.app.UI.Activity.AuthenticationActivity;
import me.flooz.app.UI.Activity.HomeActivity;
import me.flooz.app.UI.Fragment.Home.TabFragments.ProfileCardFragment;
import me.flooz.app.UI.Tools.CustomImageViewer;
import me.flooz.app.UI.View.LoadingImageView;
import me.flooz.app.Utils.CustomFonts;
import me.flooz.app.Utils.CustomNotificationIntents;
import me.flooz.app.Utils.FLHelper;
import me.flooz.app.Utils.FLTriggerManager;

/**
 * Created by Wapazz on 17/09/15.
 */
public class TransactionCardController extends BaseController {

    private Boolean viewCreated = false;

    public Boolean insertComment = false;

    private FLTransaction transaction = null;

    public ScrollView cardScroll;
    private RelativeLayout cardHeader;
    private ImageView cardHeaderScope;
    private TextView cardHeaderDate;
    private ImageView cardHeaderReportButton;
    private RoundedImageView cardFromPic;
    private RoundedImageView cardToPic;
    private TextView cardFromUsername;
    private TextView cardFromFullname;
    private TextView cardToUsername;
    private TextView cardToFullname;
    private TextView cardValue;
    private LinearLayout cardActionBar;
    private TextView cardActionBarDecline;
    private TextView cardActionBarAccept;
    private TextView card3dText;
    private TextView cardDesc;
    private LoadingImageView cardPic;
    private LinearLayout cardLikesButton;
    private TextView cardLikesButtonText;
    private ImageView cardLikesButtonPicto;
    private LinearLayout cardCommentsContainer;
    private LinearLayout cardCommentsButton;
    private ImageView cardCommentsButtonPicto;
    private TextView cardCommentsButtonText;
    private TextView cardCommentsSendButton;
    private EditText cardCommentsTextfield;
    private TextView cardLocationText;
    private ImageView cardLocationImg;
    private LinearLayout cardLocationLayout;

    private TextView cardLikesText;
    private ImageView cardLikesImg;
    private LinearLayout cardLikesLayout;

    private LinearLayout cardShareButton;
    private ImageView cardShareButtonImg;

    private LinearLayout cardMoreButton;
    private ImageView cardMoreButtonImg;


    private Boolean transactionPending = false;
    private Boolean dialogIsShowing = false;

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

    public TransactionCardController(@NonNull View mainView, @NonNull Activity parentActivity, @NonNull ControllerKind kind) {
        super(mainView, parentActivity, kind);

        this.init();
    }

    public TransactionCardController(@NonNull View mainView, @NonNull Activity parentActivity, @NonNull ControllerKind kind, @Nullable JSONObject data) {
        super(mainView, parentActivity, kind, data);

        this.init();
    }

    @Override
    protected void init() {
        super.init();

        this.cardShareButton = (LinearLayout) this.currentView.findViewById(R.id.transac_card_social_share);
        this.cardShareButtonImg = (ImageView) this.currentView.findViewById(R.id.transac_card_social_share_img);

        this.cardMoreButton = (LinearLayout) this.currentView.findViewById(R.id.transac_card_social_more);
        this.cardMoreButtonImg = (ImageView) this.currentView.findViewById(R.id.transac_card_social_more_img);

        this.cardHeaderDate.setTypeface(CustomFonts.customTitleExtraLight(this.parentActivity));
        this.cardFromUsername.setTypeface(CustomFonts.customTitleExtraLight(this.parentActivity), Typeface.BOLD);
        this.cardFromFullname.setTypeface(CustomFonts.customTitleExtraLight(this.parentActivity));
        this.cardToUsername.setTypeface(CustomFonts.customTitleExtraLight(this.parentActivity), Typeface.BOLD);
        this.cardToFullname.setTypeface(CustomFonts.customTitleExtraLight(this.parentActivity));
        this.cardValue.setTypeface(CustomFonts.customTitleExtraLight(this.parentActivity), Typeface.BOLD);
        this.cardActionBarDecline.setTypeface(CustomFonts.customTitleLight(this.parentActivity), Typeface.BOLD);
        this.cardActionBarAccept.setTypeface(CustomFonts.customTitleLight(this.parentActivity), Typeface.BOLD);
        this.card3dText.setTypeface(CustomFonts.customContentRegular(this.parentActivity));
        this.cardDesc.setTypeface(CustomFonts.customContentLight(this.parentActivity));
        this.cardLikesButtonText.setTypeface(CustomFonts.customContentRegular(this.parentActivity));
        this.cardCommentsButtonText.setTypeface(CustomFonts.customContentRegular(this.parentActivity));
        this.cardCommentsSendButton.setTypeface(CustomFonts.customContentRegular(this.parentActivity));
        this.cardCommentsTextfield.setTypeface(CustomFonts.customContentRegular(this.parentActivity));
        this.cardLocationText.setTypeface(CustomFonts.customContentRegular(this.parentActivity));
        this.cardLikesText.setTypeface(CustomFonts.customContentRegular(this.parentActivity));

        this.cardHeaderReportButton.setColorFilter(this.parentActivity.getResources().getColor(R.color.blue));
        this.cardLocationImg.setColorFilter(this.parentActivity.getResources().getColor(R.color.placeholder));
        this.cardLikesImg.setColorFilter(this.parentActivity.getResources().getColor(R.color.placeholder));
        this.cardLikesButtonPicto.setColorFilter(this.parentActivity.getResources().getColor(R.color.background_social_button));
        this.cardCommentsButtonPicto.setColorFilter(this.parentActivity.getResources().getColor(R.color.background_social_button));
        this.cardShareButtonImg.setColorFilter(this.parentActivity.getResources().getColor(R.color.background_social_button));
        this.cardMoreButtonImg.setColorFilter(this.parentActivity.getResources().getColor(R.color.background_social_button));

        this.cardHeaderReportButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FloozApplication.getInstance().showReportActionMenu(transaction);
            }
        });

        this.cardHeaderScope.setColorFilter(this.parentActivity.getResources().getColor(android.R.color.white));

        this.cardActionBarDecline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FLTriggerManager.getInstance().executeTriggerList(FLTriggerManager.convertTriggersJSONArrayToList(transaction.actions.optJSONArray("decline")));
            }
        });

        this.cardActionBarAccept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FLTriggerManager.getInstance().executeTriggerList(FLTriggerManager.convertTriggersJSONArrayToList(transaction.actions.optJSONArray("accept")));
            }
        });

        this.cardFromPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FloozApplication.getInstance().showUserProfile(transaction.from);
            }
        });

        this.cardToPic.setOnClickListener(new View.OnClickListener() {
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

        this.cardPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CustomImageViewer.start(parentActivity, transaction.attachmentURL);
            }
        });



        this.cardLikesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (transaction.social.isLiked) {
                    transaction.social.isLiked = false;
                    transaction.social.likesCount = transaction.social.likesCount.intValue() - 1;

                    if (transaction.social.likesCount.intValue() > 0) {
                        cardLikesButtonText.setVisibility(View.VISIBLE);
                        cardLikesButtonText.setText(FLHelper.formatUserNumber(transaction.social.likesCount.longValue()));
                    } else {
                        cardLikesButtonText.setVisibility(View.INVISIBLE);
                    }

                    cardLikesButtonText.setTextColor(parentActivity.getResources().getColor(R.color.background_social_button));
                    cardLikesButtonPicto.setColorFilter(parentActivity.getResources().getColor(R.color.background_social_button));
                }
                else {
                    transaction.social.isLiked = true;
                    transaction.social.likesCount = transaction.social.likesCount.intValue() + 1;

                    if (transaction.social.likesCount.intValue() > 0) {
                        cardLikesButtonText.setVisibility(View.VISIBLE);
                        cardLikesButtonText.setText(FLHelper.formatUserNumber(transaction.social.likesCount.longValue()));
                    } else {
                        cardLikesButtonText.setVisibility(View.INVISIBLE);
                    }

                    cardLikesButtonText.setTextColor(parentActivity.getResources().getColor(R.color.pink));
                    cardLikesButtonPicto.setColorFilter(parentActivity.getResources().getColor(R.color.pink));
                }

                FloozRestClient.getInstance().likeTransaction(transaction.transactionId, new FloozHttpResponseHandler() {
                    @Override
                    public void success(Object response) {
                        transaction.setJson((JSONObject) response);
                        reloadView();
                    }

                    @Override
                    public void failure(int statusCode, FLError error) {

                    }
                });
            }
        });

        this.cardCommentsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cardCommentsTextfield.requestFocus();
                InputMethodManager imm = (InputMethodManager) parentActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(cardCommentsTextfield, InputMethodManager.SHOW_IMPLICIT);
            }
        });

        this.cardShareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent share = new Intent(Intent.ACTION_SEND);
                share.setType("text/plain");

                share.putExtra(Intent.EXTRA_TEXT, "https://www.flooz.me/flooz/" + transaction.transactionId);

                parentActivity.startActivity(Intent.createChooser(share, parentActivity.getResources().getString(R.string.SHARE_FLOOZ)));
            }
        });

        this.cardMoreButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        this.cardCommentsSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (cardCommentsTextfield.getText().length() > 0 && cardCommentsTextfield.getText().length() < 140) {
                    FloozRestClient.getInstance().commentTransaction(transaction.transactionId, cardCommentsTextfield.getText().toString(), new FloozHttpResponseHandler() {
                        @Override
                        public void success(Object response) {
                            transaction.setJson((JSONObject) response);
                            reloadView();
                        }

                        @Override
                        public void failure(int statusCode, FLError error) {

                        }
                    });

                    cardCommentsTextfield.setText("");
                    cardCommentsTextfield.clearFocus();
                    InputMethodManager imm = (InputMethodManager) parentActivity.getSystemService(
                            Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(parentActivity.getWindow().getDecorView().getRootView().getWindowToken(), 0);
                }
            }
        });

        this.viewCreated = true;
    }

    public void acceptTransaction() {
        FloozRestClient.getInstance().updateTransactionValidate(transaction, FLTransaction.TransactionStatus.TransactionStatusAccepted, new FloozHttpResponseHandler() {
            @Override
            public void success(Object response) {

            }

            @Override
            public void failure(int statusCode, FLError error) {
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        if (this.insertComment) {
            cardCommentsTextfield.requestFocus();
            InputMethodManager imm = (InputMethodManager) parentActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(cardCommentsTextfield, InputMethodManager.SHOW_IMPLICIT);
        }

        FLUser self = FloozRestClient.getInstance().currentUser;
        if (this.cardToUsername.getText().toString().contentEquals("@" + self.username) && self.avatarURL != null && !self.avatarURL.isEmpty())
            ImageLoader.getInstance().displayImage(self.avatarURL, this.cardToPic);
        if (this.cardFromUsername.getText().toString().contentEquals("@" + self.username) && self.avatarURL != null && !self.avatarURL.isEmpty())
            ImageLoader.getInstance().displayImage(self.avatarURL, this.cardFromPic);

        LocalBroadcastManager.getInstance(FloozApplication.getAppContext()).registerReceiver(reloadTransactionReceiver,
                CustomNotificationIntents.filterReloadCurrentUser());

        LocalBroadcastManager.getInstance(FloozApplication.getAppContext()).registerReceiver(reloadTransactionReceiver,
                CustomNotificationIntents.filterReloadTimeline());

        LocalBroadcastManager.getInstance(FloozApplication.getAppContext()).registerReceiver(reloadTransaction,
                CustomNotificationIntents.filterReloadTransaction());
    }

    @Override
    public void onResume() {
        super.onResume();

        if (this.insertComment) {
            cardCommentsTextfield.requestFocus();
            InputMethodManager imm = (InputMethodManager) parentActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(cardCommentsTextfield, InputMethodManager.SHOW_IMPLICIT);
        }
        if (this.transactionPending)
            FloozRestClient.getInstance().showLoadView();
    }

    @Override
    public void onPause() {
        super.onPause();

    }

    @Override
    public void onStop() {
        super.onStop();

        LocalBroadcastManager.getInstance(FloozApplication.getAppContext()).unregisterReceiver(reloadTransactionReceiver);
    }

    private void reloadView() {

        this.cardHeaderScope.setImageDrawable(FLTransaction.transactionScopeToImage(this.transaction.scope));

        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM à HH:mm", Locale.FRANCE);

        this.cardHeaderDate.setText(sdf.format(this.transaction.date.getDate()));

        this.cardFromPic.setImageDrawable(null);
        if (this.transaction.from.avatarURL != null && !this.transaction.from.avatarURL.isEmpty())
            ImageLoader.getInstance().displayImage(this.transaction.from.avatarURL, this.cardFromPic);
        else
            this.cardFromPic.setImageDrawable(this.parentActivity.getResources().getDrawable(R.drawable.avatar_default));

        if (this.transaction.from.username != null) {
            this.cardFromUsername.setVisibility(View.VISIBLE);
            this.cardFromUsername.setText("@" + this.transaction.from.username);
        }
        else {
            this.cardFromUsername.setVisibility(View.INVISIBLE);
        }

        this.cardFromFullname.setText(this.transaction.from.fullname);

        if (this.transaction.to.isPot) {
            this.cardToUsername.setVisibility(View.INVISIBLE);
            this.cardToPic.setImageDrawable(parentActivity.getResources().getDrawable(R.drawable.default_pot_avatar));
        } else {
            if (this.transaction.to.username != null) {
                this.cardToUsername.setVisibility(View.VISIBLE);
                this.cardToUsername.setText("@" + this.transaction.to.username);
            }
            else {
                this.cardToUsername.setVisibility(View.INVISIBLE);
            }

            this.cardToPic.setImageDrawable(null);
            if (this.transaction.to.avatarURL != null && !this.transaction.to.avatarURL.isEmpty())
                ImageLoader.getInstance().displayImage(this.transaction.to.avatarURL, this.cardToPic);
            else
                this.cardToPic.setImageDrawable(this.parentActivity.getResources().getDrawable(R.drawable.avatar_default));
        }

        this.cardToFullname.setText(this.transaction.to.fullname);

        if (this.transaction.amountText != null && !this.transaction.amountText.isEmpty()) {
            this.cardValue.setText(this.transaction.amountText);
            this.cardValue.setVisibility(View.VISIBLE);
        }
        else {
            this.cardValue.setText("");
            this.cardValue.setVisibility(View.INVISIBLE);
        }

        if (this.transaction.isAcceptable || this.transaction.isCancelable) {
            this.cardActionBar.setVisibility(View.VISIBLE);
        }
        else {
            this.cardActionBar.setVisibility(View.GONE);
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

        this.card3dText.setText(sb);

        this.cardDesc.setText(this.transaction.content);

        if (this.transaction.attachmentURL != null && !this.transaction.attachmentURL.isEmpty()) {
            this.cardPic.setImageFromUrl(this.transaction.attachmentURL);
            this.cardPic.setVisibility(View.VISIBLE);
        } else
            this.cardPic.setVisibility(View.GONE);

        if (this.transaction.location != null && !this.transaction.location.isEmpty()) {
            this.cardLocationText.setText(this.transaction.location.toCharArray(), 0, this.transaction.location.length());
            this.cardLocationLayout.setVisibility(View.VISIBLE);
        } else {
            this.cardLocationLayout.setVisibility(View.GONE);
        }

        if (transaction.social != null && transaction.social.likesCount.intValue() > 0) {
            cardLikesText.setText(transaction.social.likeText);
            cardLikesLayout.setVisibility(View.VISIBLE);

            cardLikesButtonText.setVisibility(View.VISIBLE);
            cardLikesButtonText.setText(FLHelper.formatUserNumber(transaction.social.likesCount.longValue()));

            if (transaction.social.isLiked) {
                cardLikesButtonText.setTextColor(this.parentActivity.getResources().getColor(R.color.pink));
                cardLikesButtonPicto.setColorFilter(this.parentActivity.getResources().getColor(R.color.pink));
            } else {
                cardLikesButtonText.setTextColor(this.parentActivity.getResources().getColor(R.color.background_social_button));
                cardLikesButtonPicto.setColorFilter(this.parentActivity.getResources().getColor(R.color.background_social_button));
            }
        } else {
            cardLikesLayout.setVisibility(View.GONE);
            cardLikesButtonText.setVisibility(View.INVISIBLE);
            cardLikesButtonPicto.setColorFilter(this.parentActivity.getResources().getColor(R.color.background_social_button));
        }

        if (transaction.social != null && transaction.social.commentsCount.intValue() > 0) {
            cardCommentsButtonText.setVisibility(View.VISIBLE);
            cardCommentsButtonText.setText(FLHelper.formatUserNumber(transaction.social.commentsCount.longValue()));

            if (transaction.social.isCommented) {
                cardCommentsButtonText.setTextColor(this.parentActivity.getResources().getColor(R.color.blue));
                cardCommentsButtonPicto.setColorFilter(this.parentActivity.getResources().getColor(R.color.blue));
            } else {
                cardCommentsButtonText.setTextColor(this.parentActivity.getResources().getColor(R.color.background_social_button));
                cardCommentsButtonPicto.setColorFilter(this.parentActivity.getResources().getColor(R.color.background_social_button));
            }
        } else {
            cardCommentsButtonText.setVisibility(View.INVISIBLE);
            cardCommentsButtonPicto.setColorFilter(this.parentActivity.getResources().getColor(R.color.background_social_button));
        }

        this.cardCommentsContainer.removeAllViews();
        for (int i = 0; i < this.transaction.comments.size(); i++) {
            View commentRow;
            FLComment comment;

            comment = (FLComment) this.transaction.comments.get(i);
            commentRow = this.createCommentRowView(comment);

            this.cardCommentsContainer.addView(commentRow);
        }

        this.cardCommentsTextfield.setText("");

        this.cardCommentsTextfield.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    cardCommentsSendButton.performClick();
                }
                return false;
            }
        });
    }

    public void updateComment() {
        if (this.insertComment) {
            this.cardScroll.post(new Runnable() {
                @Override
                public void run() {
                    cardScroll.fullScroll(ScrollView.FOCUS_DOWN);
                }
            });
            this.cardCommentsTextfield.requestFocus();
            InputMethodManager imm = (InputMethodManager) parentActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(this.cardCommentsTextfield, InputMethodManager.SHOW_IMPLICIT);
        }
    }

    private View createCommentRowView(final FLComment comment) {
        View commentRow;
        TextView commentText;
        TextView commentInfos;
        RoundedImageView commentPic;

        commentRow = this.parentActivity.getLayoutInflater().inflate(R.layout.transaction_card_comment_row, null);
        commentPic = (RoundedImageView) commentRow.findViewById(R.id.card_comment_row_img);
        commentText = (TextView) commentRow.findViewById(R.id.card_comment_row_text);
        commentInfos = (TextView) commentRow.findViewById(R.id.card_comment_row_infos);

        commentText.setTypeface(CustomFonts.customContentRegular(this.parentActivity));
        commentInfos.setTypeface(CustomFonts.customContentLight(this.parentActivity));


        if (comment.user.avatarURL != null && !comment.user.avatarURL.isEmpty())
            ImageLoader.getInstance().displayImage(comment.user.avatarURL, commentPic);
        else
            commentPic.setImageDrawable(this.parentActivity.getResources().getDrawable(R.drawable.avatar_default));

        commentText.setText(comment.content);
        commentInfos.setText("@" + comment.user.username + " - " + comment.dateText);

        commentPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                comment.user.selectedCanal = FLUser.FLUserSelectedCanal.TimelineCanal;
                ProfileCardFragment profileCardFragment = new ProfileCardFragment();
                profileCardFragment.user = comment.user;
                ((HomeActivity) parentActivity).pushFragmentInCurrentTab(profileCardFragment);
            }
        });

        return commentRow;
    }

    public void setTransaction(FLTransaction transac) {
        this.transaction = transac;
        if (this.viewCreated)
            this.reloadView();
    }

    private void scrollListViewToBottom() {
        cardScroll.post(new Runnable() {
            @Override
            public void run() {
                cardScroll.fullScroll(View.FOCUS_DOWN);
            }
        });
    }
}
