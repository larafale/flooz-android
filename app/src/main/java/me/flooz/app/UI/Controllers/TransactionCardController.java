package me.flooz.app.UI.Controllers;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
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

import com.makeramen.RoundedImageView;
import com.nostra13.universalimageloader.core.ImageLoader;

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
import me.flooz.app.UI.Fragment.Home.ProfileCardFragment;
import me.flooz.app.UI.Tools.CustomImageViewer;
import me.flooz.app.UI.View.LoadingImageView;
import me.flooz.app.Utils.CustomFonts;

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
    private ImageView cardHeaderCloseButton;
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
    private LinearLayout cardLikesContainer;
    private TextView cardLikesText;
    private LinearLayout cardLikesButton;
    private TextView cardLikesButtonText;
    private ImageView cardLikesButtonPicto;
    private LinearLayout cardCommentsContainer;
    private LinearLayout cardCommentsButton;
    private ImageView cardCommentsButtonPicto;
    private TextView cardCommentsButtonText;
    private TextView cardCommentsSendButton;
    private EditText cardCommentsTextfield;
    private LinearLayout cardSocialContainer;
    private LinearLayout cardCommentsNumberContainer;
    private TextView cardCommentsNumber;

    private Context context;
    private LayoutInflater inflater;

    private Boolean transactionPending = false;
    private Boolean dialogIsShowing = false;

    public TransactionCardController(@NonNull View mainView, @NonNull Activity parentActivity, @NonNull BaseController.ControllerKind kind) {
        super(mainView, parentActivity, kind);

        View view = this.currentView;

        this.cardScroll = (ScrollView) view.findViewById(R.id.card_scroll);
        this.cardHeader = (RelativeLayout) view.findViewById(R.id.transac_card_header);
        this.cardHeaderScope = (ImageView) view.findViewById(R.id.transac_card_header_scope);
        this.cardHeaderDate = (TextView) view.findViewById(R.id.transac_card_header_date);
        this.cardHeaderCloseButton = (ImageView) view.findViewById(R.id.transac_card_header_close);
        this.cardHeaderReportButton = (ImageView) view.findViewById(R.id.transac_card_header_report);
        this.cardFromPic = (RoundedImageView) view.findViewById(R.id.transac_card_from_pic);
        this.cardToPic = (RoundedImageView) view.findViewById(R.id.transac_card_to_pic);
        this.cardFromUsername = (TextView) view.findViewById(R.id.transac_card_from_username);
        this.cardFromFullname = (TextView) view.findViewById(R.id.transac_card_from_fullname);
        this.cardToUsername = (TextView) view.findViewById(R.id.transac_card_to_username);
        this.cardToFullname = (TextView) view.findViewById(R.id.transac_card_to_fullname);
        this.cardValue = (TextView) view.findViewById(R.id.transac_card_value);
        this.cardActionBar = (LinearLayout) view.findViewById(R.id.transac_card_actionBar);
        this.cardActionBarDecline = (TextView) view.findViewById(R.id.transac_card_actionBar_decline);
        this.cardActionBarAccept = (TextView) view.findViewById(R.id.transac_card_actionBar_accept);
        this.card3dText = (TextView) view.findViewById(R.id.transac_card_3dText);
        this.cardDesc = (TextView) view.findViewById(R.id.transac_card_desc);
        this.cardPic = (LoadingImageView) view.findViewById(R.id.transac_card_pic);
        this.cardLikesContainer = (LinearLayout) view.findViewById(R.id.transac_card_likes_container);
        this.cardCommentsButton = (LinearLayout) view.findViewById(R.id.transac_card_comments_button);
        this.cardCommentsButtonText = (TextView) view.findViewById(R.id.transac_card_comments_button_text);
        this.cardCommentsButtonPicto = (ImageView) view.findViewById(R.id.transac_card_comments_button_picto);
        this.cardLikesButton = (LinearLayout) view.findViewById(R.id.transac_card_likes_button);
        this.cardLikesButtonText = (TextView) view.findViewById(R.id.transac_card_likes_button_text);
        this.cardLikesButtonPicto = (ImageView) view.findViewById(R.id.transac_card_likes_button_picto);
        this.cardLikesText = (TextView) view.findViewById(R.id.transac_card_likes_text);
        this.cardCommentsContainer = (LinearLayout) view.findViewById(R.id.transac_card_comments_container);
        this.cardCommentsSendButton = (TextView) view.findViewById(R.id.transac_card_comments_send);
        this.cardCommentsTextfield = (EditText) view.findViewById(R.id.transac_card_comments_textfield);
        this.cardSocialContainer = (LinearLayout) view.findViewById(R.id.transac_card_social_container);
        this.cardCommentsNumberContainer = (LinearLayout) view.findViewById(R.id.transac_card_comments_number_container);
        this.cardCommentsNumber = (TextView) view.findViewById(R.id.transac_card_comments_number);

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
        this.cardLikesText.setTypeface(CustomFonts.customContentRegular(this.parentActivity));
        this.cardCommentsNumber.setTypeface(CustomFonts.customContentRegular(this.parentActivity));
        this.cardLikesButtonText.setTypeface(CustomFonts.customContentRegular(this.parentActivity));
        this.cardCommentsButtonText.setTypeface(CustomFonts.customContentRegular(this.parentActivity));
        this.cardCommentsSendButton.setTypeface(CustomFonts.customContentRegular(this.parentActivity));
        this.cardCommentsTextfield.setTypeface(CustomFonts.customContentRegular(this.parentActivity));

        if (currentKind == ControllerKind.FRAGMENT_CONTROLLER)
            this.cardHeaderCloseButton.setImageDrawable(this.parentActivity.getResources().getDrawable(R.drawable.nav_back));

        this.cardHeaderCloseButton.setOnClickListener(v -> {
            if (currentKind == ControllerKind.ACTIVITY_CONTROLLER) {
                parentActivity.finish();
                parentActivity.overridePendingTransition(android.R.anim.fade_in, R.anim.slide_down);
            } else {
                ((HomeActivity) this.parentActivity).popFragmentInCurrentTab();
            }
        });

        this.cardHeaderScope.setColorFilter(this.parentActivity.getResources().getColor(android.R.color.white));

        this.cardActionBarDecline.setOnClickListener(v -> FloozRestClient.getInstance().updateTransaction(transaction, FLTransaction.TransactionStatus.TransactionStatusRefused, new FloozHttpResponseHandler() {
            @Override
            public void success(Object response) {
                setTransaction(new FLTransaction(((JSONObject) response).optJSONObject("item")));
            }

            @Override
            public void failure(int statusCode, FLError error) {
            }
        }));

        this.cardActionBarAccept.setOnClickListener(v -> FloozRestClient.getInstance().updateTransactionValidate(transaction, FLTransaction.TransactionStatus.TransactionStatusAccepted, new FloozHttpResponseHandler() {
            @Override
            public void success(Object response) {
                showValidationDialog(((JSONObject) response).optString("confirmationText"));
            }

            @Override
            public void failure(int statusCode, FLError error) {
            }
        }));

        this.cardFromPic.setOnClickListener(v -> {
            transaction.to.selectedCanal = FLUser.FLUserSelectedCanal.TimelineCanal;
            ProfileCardFragment profileCardFragment = new ProfileCardFragment();
            if (this.cardFromUsername.getText().toString().contentEquals("@" + FloozRestClient.getInstance().currentUser.username))
                profileCardFragment.user = FloozRestClient.getInstance().currentUser;
            else
                profileCardFragment.user = transaction.from;
            ((HomeActivity)parentActivity).pushFragmentInCurrentTab(profileCardFragment);
        });

        this.cardToPic.setOnClickListener(v -> {
            transaction.to.selectedCanal = FLUser.FLUserSelectedCanal.TimelineCanal;
            ProfileCardFragment profileCardFragment = new ProfileCardFragment();
            if (this.cardToUsername.getText().toString().contentEquals("@" + FloozRestClient.getInstance().currentUser.username))
                profileCardFragment.user = FloozRestClient.getInstance().currentUser;
            else
                profileCardFragment.user = transaction.to;
            ((HomeActivity)parentActivity).pushFragmentInCurrentTab(profileCardFragment);
        });

        this.cardPic.setOnClickListener(v -> CustomImageViewer.start(this.parentActivity, transaction.attachmentURL));

        this.cardLikesButton.setOnClickListener(v -> FloozRestClient.getInstance().likeTransaction(transaction.transactionId, new FloozHttpResponseHandler() {
            @Override
            public void success(Object response) {
                transaction.social.isLiked = !transaction.social.isLiked;
                transaction.social.likeText = (String) response;

                if (!transaction.social.likeText.isEmpty() || transaction.social.commentsCount.intValue() > 0) {
                    cardSocialContainer.setVisibility(View.VISIBLE);

                    if (transaction.social.commentsCount.intValue() > 0) {
                        if (transaction.social.commentsCount.intValue() < 10)
                            cardCommentsNumber.setText("0" + transaction.social.commentsCount.toString());
                        else
                            cardCommentsNumber.setText(transaction.social.commentsCount.toString());
                        cardCommentsNumberContainer.setVisibility(View.VISIBLE);
                    } else {
                        cardCommentsNumberContainer.setVisibility(View.GONE);
                    }


                    if (!transaction.social.likeText.isEmpty()) {
                        cardLikesText.setText(transaction.social.likeText);
                        cardLikesContainer.setVisibility(View.VISIBLE);
                    } else {
                        cardLikesContainer.setVisibility(View.GONE);
                    }
                }
                else
                    cardSocialContainer.setVisibility(View.GONE);

                if (transaction.social.isLiked) {
                    cardLikesButton.setBackground(parentActivity.getResources().getDrawable(R.drawable.timeline_row_action_button_background_selected));
                    cardLikesButtonText.setTextColor(parentActivity.getResources().getColor(android.R.color.white));
                    cardLikesButtonPicto.setImageDrawable(parentActivity.getResources().getDrawable(R.drawable.social_like_full));
                }
                else {
                    cardLikesButton.setBackground(parentActivity.getResources().getDrawable(R.drawable.timeline_row_action_button_background));
                    cardLikesButtonText.setTextColor(parentActivity.getResources().getColor(R.color.placeholder));
                    cardLikesButtonPicto.setImageDrawable(parentActivity.getResources().getDrawable(R.drawable.social_like));
                }
            }

            @Override
            public void failure(int statusCode, FLError error) {

            }
        }));

        this.cardCommentsButton.setOnClickListener(v -> {
            cardCommentsTextfield.requestFocus();
            InputMethodManager imm = (InputMethodManager) parentActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(cardCommentsTextfield, InputMethodManager.SHOW_IMPLICIT);
        });

        this.viewCreated = true;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (this.insertComment) {
            cardCommentsTextfield.requestFocus();
            InputMethodManager imm = (InputMethodManager) parentActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(cardCommentsTextfield, InputMethodManager.SHOW_IMPLICIT);
        }

        // TODO Check plutot URL ?
        FLUser self = FloozRestClient.getInstance().currentUser;
        if (this.cardToUsername.getText().toString().contentEquals("@" + self.username) && self.avatarURL != null && !self.avatarURL.isEmpty())
            ImageLoader.getInstance().displayImage(self.avatarURL, this.cardToPic);
        if (this.cardFromUsername.getText().toString().contentEquals("@" + self.username) && self.avatarURL != null && !self.avatarURL.isEmpty())
            ImageLoader.getInstance().displayImage(self.avatarURL, this.cardFromPic);

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

    private void reloadView() {

        this.cardHeaderScope.setImageDrawable(FLTransaction.transactionScopeToImage(this.transaction.scope));

        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM à HH:mm", Locale.FRANCE);

        this.cardHeaderDate.setText(sdf.format(this.transaction.date.getDate()));

        this.cardFromPic.setImageDrawable(null);
        if (this.transaction.from.avatarURL != null && !this.transaction.from.avatarURL.isEmpty())
            ImageLoader.getInstance().displayImage(this.transaction.from.avatarURL, this.cardFromPic);
        else
            this.cardFromPic.setImageDrawable(this.parentActivity.getResources().getDrawable(R.drawable.avatar_default));

        this.cardToPic.setImageDrawable(null);
        if (this.transaction.to.avatarURL != null && !this.transaction.to.avatarURL.isEmpty())
            ImageLoader.getInstance().displayImage(this.transaction.to.avatarURL, this.cardToPic);
        else
            this.cardToPic.setImageDrawable(this.parentActivity.getResources().getDrawable(R.drawable.avatar_default));

        if (this.transaction.from.username != null) {
            this.cardFromUsername.setVisibility(View.VISIBLE);
            this.cardFromUsername.setText("@" + this.transaction.from.username);
        }
        else {
            this.cardFromUsername.setVisibility(View.INVISIBLE);
        }

        this.cardFromFullname.setText(this.transaction.from.fullname);

        if (this.transaction.to.username != null) {
            this.cardToUsername.setVisibility(View.VISIBLE);
            this.cardToUsername.setText("@" + this.transaction.to.username);
        }
        else {
            this.cardToUsername.setVisibility(View.INVISIBLE);
        }

        this.cardToFullname.setText(this.transaction.to.fullname);

        if (this.transaction.amountText != null && !this.transaction.amountText.isEmpty()) {
            this.cardValue.setText(this.transaction.amountText);
            this.cardValue.setVisibility(View.VISIBLE);
        }
        else {
            this.cardValue.setText("");
            this.cardValue.setVisibility(View.GONE);
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

        if (this.transaction.attachmentThumbURL != null && !this.transaction.attachmentThumbURL.isEmpty()) {
            this.cardPic.setImageFromUrl(this.transaction.attachmentThumbURL);
            this.cardPic.setVisibility(View.VISIBLE);
        }
        else
            this.cardPic.setVisibility(View.GONE);

        if (!this.transaction.social.likeText.isEmpty() || this.transaction.social.commentsCount.intValue() > 0) {
            this.cardSocialContainer.setVisibility(View.VISIBLE);

            if (this.transaction.social.commentsCount.intValue() > 0) {
                if (this.transaction.social.commentsCount.intValue() < 10)
                    this.cardCommentsNumber.setText("0" + this.transaction.social.commentsCount.toString());
                else
                    this.cardCommentsNumber.setText(this.transaction.social.commentsCount.toString());
                this.cardCommentsNumberContainer.setVisibility(View.VISIBLE);
            } else {
                this.cardCommentsNumberContainer.setVisibility(View.GONE);
            }

            if (!this.transaction.social.likeText.isEmpty()) {
                this.cardLikesText.setText(this.transaction.social.likeText);
                this.cardLikesContainer.setVisibility(View.VISIBLE);
            } else {
                this.cardLikesContainer.setVisibility(View.GONE);
            }
        }
        else
            this.cardSocialContainer.setVisibility(View.GONE);

        if (this.transaction.social.isLiked) {
            this.cardLikesButton.setBackground(this.parentActivity.getResources().getDrawable(R.drawable.timeline_row_action_button_background_selected));
            this.cardLikesButtonText.setTextColor(this.parentActivity.getResources().getColor(android.R.color.white));
            this.cardLikesButtonPicto.setImageDrawable(this.parentActivity.getResources().getDrawable(R.drawable.social_like_full));
        }
        else {
            this.cardLikesButton.setBackground(this.parentActivity.getResources().getDrawable(R.drawable.timeline_row_action_button_background));
            this.cardLikesButtonText.setTextColor(this.parentActivity.getResources().getColor(R.color.placeholder));
            this.cardLikesButtonPicto.setImageDrawable(this.parentActivity.getResources().getDrawable(R.drawable.social_like));
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

        this.cardCommentsTextfield.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                cardCommentsSendButton.performClick();
            }
            return false;
        });

        this.cardCommentsSendButton.setOnClickListener(v -> {
            if (cardCommentsTextfield.getText().length() > 0 && cardCommentsTextfield.getText().length() < 140) {
                FloozRestClient.getInstance().commentTransaction(transaction.transactionId, cardCommentsTextfield.getText().toString(), new FloozHttpResponseHandler() {
                    @Override
                    public void success(Object response) {
                        FLComment com = (FLComment) response;

                        transaction.comments.add(com);
                        transaction.social.isCommented = true;
                        transaction.social.commentsCount = transaction.comments.size();

                        if (!transaction.social.likeText.isEmpty() || transaction.social.commentsCount.intValue() > 0) {
                            cardSocialContainer.setVisibility(View.VISIBLE);

                            if (transaction.social.commentsCount.intValue() > 0) {
                                if (transaction.social.commentsCount.intValue() < 10)
                                    cardCommentsNumber.setText("0" + transaction.social.commentsCount.toString());
                                else
                                    cardCommentsNumber.setText(transaction.social.commentsCount.toString());
                                cardCommentsNumberContainer.setVisibility(View.VISIBLE);
                            } else {
                                cardCommentsNumberContainer.setVisibility(View.GONE);
                            }

                            if (!transaction.social.likeText.isEmpty()) {
                                cardLikesText.setText(transaction.social.likeText);
                                cardLikesContainer.setVisibility(View.VISIBLE);
                            } else {
                                cardLikesContainer.setVisibility(View.GONE);
                            }
                        }
                        else
                            cardSocialContainer.setVisibility(View.GONE);

                        cardCommentsContainer.addView(createCommentRowView(com));
                    }

                    @Override
                    public void failure(int statusCode, FLError error) {

                    }
                });

                cardCommentsTextfield.setText("");
                cardCommentsTextfield.clearFocus();
                InputMethodManager imm = (InputMethodManager) parentActivity.getSystemService(
                        Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(parentActivity.getWindow().getDecorView().getRootView().getWindowToken() , 0);
            }
        });
    }

    public void updateComment() {
        if (this.insertComment) {
            this.cardScroll.post(() -> cardScroll.fullScroll(ScrollView.FOCUS_DOWN));
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
        commentInfos.setText("@" + comment.user.username + " - " + comment.date.fromNow());

        commentPic.setOnClickListener(v -> {
            comment.user.selectedCanal = FLUser.FLUserSelectedCanal.TimelineCanal;
            ProfileCardFragment profileCardFragment = new ProfileCardFragment();
            profileCardFragment.user = comment.user;
            ((HomeActivity)parentActivity).pushFragmentInCurrentTab(profileCardFragment);
        });

        return commentRow;
    }

    public void setTransaction(FLTransaction transac) {
        this.transaction = transac;
        if (this.viewCreated)
            this.reloadView();
    }

    private void showValidationDialog(String content) {
        if (!dialogIsShowing) {
            dialogIsShowing = true;
            final Dialog dialog = new Dialog(this.parentActivity);

            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.custom_dialog_validate_transaction);

            TextView text = (TextView) dialog.findViewById(R.id.dialog_validate_flooz_text);
            text.setText(content);
            text.setTypeface(CustomFonts.customContentRegular(this.parentActivity));

            Button decline = (Button) dialog.findViewById(R.id.dialog_validate_flooz_decline);
            Button accept = (Button) dialog.findViewById(R.id.dialog_validate_flooz_accept);

            decline.setOnClickListener(v -> {
                dialog.dismiss();
                dialogIsShowing = false;
            });

            accept.setOnClickListener(v -> {
                dialogIsShowing = false;
                dialog.dismiss();
                Intent intentNotifs = new Intent(parentActivity, AuthenticationActivity.class);
                parentActivity.startActivityForResult(intentNotifs, AuthenticationActivity.RESULT_AUTHENTICATION_ACTIVITY);
                parentActivity.overridePendingTransition(R.anim.slide_up, android.R.anim.fade_out);
            });

            dialog.show();
        }
    }

    public void authenticationValidated() {
        this.transactionPending = true;
        FloozRestClient.getInstance().updateTransaction(transaction, FLTransaction.TransactionStatus.TransactionStatusAccepted, new FloozHttpResponseHandler() {
            @Override
            public void success(Object response) {
                setTransaction(new FLTransaction(((JSONObject)response).optJSONObject("item")));
                transactionPending = false;
            }

            @Override
            public void failure(int statusCode, FLError error) {
                transactionPending = false;
            }
        });
    }

    public void authenticationFailed() {

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == AuthenticationActivity.RESULT_AUTHENTICATION_ACTIVITY) {
            if (resultCode == Activity.RESULT_OK)
                authenticationValidated();
        }
    }

    @Override
    public void onBackPressed() {
        this.cardHeaderCloseButton.performClick();
    }
}
