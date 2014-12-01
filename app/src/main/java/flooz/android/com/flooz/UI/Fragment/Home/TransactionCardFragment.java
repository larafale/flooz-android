package flooz.android.com.flooz.UI.Fragment.Home;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.makeramen.RoundedImageView;
import com.nostra13.universalimageloader.core.ImageLoader;

import flooz.android.com.flooz.Model.FLComment;
import flooz.android.com.flooz.Model.FLError;
import flooz.android.com.flooz.Model.FLTransaction;
import flooz.android.com.flooz.Network.FloozHttpResponseHandler;
import flooz.android.com.flooz.Network.FloozRestClient;
import flooz.android.com.flooz.R;
import flooz.android.com.flooz.UI.Activity.HomeActivity;
import flooz.android.com.flooz.UI.Fragment.Camera.ImageViewerFragment;
import flooz.android.com.flooz.Utils.CustomFonts;

/**
 * Created by Flooz on 9/25/14.
 */
public class TransactionCardFragment extends DialogFragment {

    public HomeActivity parentActivity;
    private Boolean viewCreated = false;

    public Boolean insertComment = false;

    private FLTransaction transaction = null;

    private RelativeLayout cardHeader;
    private ImageView cardHeaderScope;
    private TextView cardHeaderDate;
    private ImageView cardHeaderCloseButton;
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
    private TextView card3dText1;
    private TextView card3dText2;
    private TextView card3dText3;
    private TextView cardDesc;
    private RoundedImageView cardPic;
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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater2, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater2.inflate(R.layout.transaction_card_fragment, null);

        this.context = inflater2.getContext();
        this.inflater = inflater2;

        this.cardHeader = (RelativeLayout) view.findViewById(R.id.transac_card_header);
        this.cardHeaderScope = (ImageView) view.findViewById(R.id.transac_card_header_scope);
        this.cardHeaderDate = (TextView) view.findViewById(R.id.transac_card_header_date);
        this.cardHeaderCloseButton = (ImageView) view.findViewById(R.id.transac_card_header_close);
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
        this.card3dText1 = (TextView) view.findViewById(R.id.transac_card_3dText_1);
        this.card3dText2 = (TextView) view.findViewById(R.id.transac_card_3dText_2);
        this.card3dText3 = (TextView) view.findViewById(R.id.transac_card_3dText_3);
        this.cardDesc = (TextView) view.findViewById(R.id.transac_card_desc);
        this.cardPic = (RoundedImageView) view.findViewById(R.id.transac_card_pic);
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

        this.cardHeaderDate.setTypeface(CustomFonts.customTitleExtraLight(this.inflater.getContext()));
        this.cardFromUsername.setTypeface(CustomFonts.customTitleExtraLight(this.inflater.getContext()));
        this.cardFromFullname.setTypeface(CustomFonts.customTitleExtraLight(this.inflater.getContext()));
        this.cardToUsername.setTypeface(CustomFonts.customTitleExtraLight(this.inflater.getContext()));
        this.cardToFullname.setTypeface(CustomFonts.customTitleExtraLight(this.inflater.getContext()));
        this.cardValue.setTypeface(CustomFonts.customTitleExtraLight(this.inflater.getContext()), Typeface.BOLD);
        this.cardActionBarDecline.setTypeface(CustomFonts.customTitleLight(this.inflater.getContext()), Typeface.BOLD);
        this.cardActionBarAccept.setTypeface(CustomFonts.customTitleLight(this.inflater.getContext()), Typeface.BOLD);
        this.card3dText1.setTypeface(CustomFonts.customContentRegular(this.inflater.getContext()), Typeface.BOLD);
        this.card3dText2.setTypeface(CustomFonts.customContentRegular(this.inflater.getContext()));
        this.card3dText3.setTypeface(CustomFonts.customContentRegular(this.inflater.getContext()), Typeface.BOLD);
        this.cardDesc.setTypeface(CustomFonts.customContentLight(this.inflater.getContext()));
        this.cardLikesText.setTypeface(CustomFonts.customContentRegular(this.inflater.getContext()));
        this.cardCommentsNumber.setTypeface(CustomFonts.customContentRegular(this.inflater.getContext()));
        this.cardLikesButtonText.setTypeface(CustomFonts.customContentRegular(this.inflater.getContext()));
        this.cardCommentsButtonText.setTypeface(CustomFonts.customContentRegular(this.inflater.getContext()));
        this.cardCommentsSendButton.setTypeface(CustomFonts.customContentRegular(this.inflater.getContext()));
        this.cardCommentsTextfield.setTypeface(CustomFonts.customContentRegular(this.inflater.getContext()));

        this.cardHeaderCloseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                insertComment = false;
                cardCommentsTextfield.setText("");
                if (cardCommentsTextfield.isFocused()) {
                    cardCommentsTextfield.clearFocus();
                    InputMethodManager imm = (InputMethodManager) context.getSystemService(
                            Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(parentActivity.getWindow().getDecorView().getRootView().getWindowToken(), 0);
                }

//                if (parentActivity != null)
//                    parentActivity.popMainFragment(R.animator.slide_down, android.R.animator.fade_in);
                dismiss();
            }
        });

        this.cardActionBarDecline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        this.cardActionBarAccept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        this.cardPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((ImageViewerFragment)parentActivity.contentFragments.get("img")).setImage(transaction.attachmentURL);
                parentActivity.pushMainFragment("img", R.animator.slide_up, android.R.animator.fade_out);
            }
        });

        this.cardLikesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FloozRestClient.getInstance().likeTransaction(transaction.transactionId, new FloozHttpResponseHandler() {
                    @Override
                    public void success(Object response) {
                        transaction.social.isLiked = !transaction.social.isLiked;
                        transaction.social.likeText = (String) response;

                        if (!transaction.social.likeText.isEmpty() || transaction.social.commentsCount.intValue() > 0) {
                            cardSocialContainer.setVisibility(View.VISIBLE);

                            if (transaction.social.commentsCount.intValue() > 0) {
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
                            cardLikesButton.setBackground(context.getResources().getDrawable(R.drawable.timeline_row_action_button_background_selected));
                            cardLikesButtonText.setTextColor(context.getResources().getColor(android.R.color.white));
                            cardLikesButtonPicto.setImageDrawable(context.getResources().getDrawable(R.drawable.social_like_full));
                        }
                        else {
                            cardLikesButton.setBackground(context.getResources().getDrawable(R.drawable.timeline_row_action_button_background));
                            cardLikesButtonText.setTextColor(context.getResources().getColor(R.color.placeholder));
                            cardLikesButtonPicto.setImageDrawable(context.getResources().getDrawable(R.drawable.social_like));
                        }
                    }

                    @Override
                    public void failure(int statusCode, FLError error) {

                    }
                });
            }});

        this.cardCommentsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cardCommentsTextfield.requestFocus();
                InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(cardCommentsTextfield, InputMethodManager.SHOW_IMPLICIT);
            }
        });

        this.viewCreated = true;

        if (this.transaction != null)
            this.reloadView();

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (this.insertComment) {
            cardCommentsTextfield.requestFocus();
            InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(cardCommentsTextfield, InputMethodManager.SHOW_IMPLICIT);
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog tmp = super.onCreateDialog(savedInstanceState);
        tmp.requestWindowFeature(Window.FEATURE_NO_TITLE);
        return tmp;
    }

    private void reloadView() {

        this.cardHeaderScope.setImageDrawable(FLTransaction.transactionScopeToImage(this.transaction.scope));

        this.cardHeaderDate.setText(DateFormat.format("dd MMM à hh:mm", this.transaction.date));

        if (this.transaction.from.avatarURL != null && !this.transaction.from.avatarURL.isEmpty())
            ImageLoader.getInstance().displayImage(this.transaction.from.avatarURL, this.cardFromPic);
        else
            this.cardFromPic.setImageDrawable(this.context.getResources().getDrawable(R.drawable.avatar_default));

        if (this.transaction.to.avatarURL != null && !this.transaction.to.avatarURL.isEmpty())
            ImageLoader.getInstance().displayImage(this.transaction.to.avatarURL, this.cardToPic);
        else
            this.cardToPic.setImageDrawable(this.context.getResources().getDrawable(R.drawable.avatar_default));

        this.cardFromUsername.setText("@" + this.transaction.from.username);
        this.cardFromFullname.setText(this.transaction.from.fullname);
        this.cardToUsername.setText("@" + this.transaction.to.username);
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

        this.card3dText1.setText(this.transaction.text3d.get(0).toString());
        this.card3dText2.setText(this.transaction.text3d.get(1).toString());
        this.card3dText3.setText(this.transaction.text3d.get(2).toString());

        this.cardDesc.setText(this.transaction.content);

        if (this.transaction.attachmentThumbURL != null && !this.transaction.attachmentThumbURL.isEmpty()) {
            ImageLoader.getInstance().displayImage(this.transaction.attachmentThumbURL, this.cardPic);
            this.cardPic.setVisibility(View.VISIBLE);
        }
        else
            this.cardPic.setVisibility(View.GONE);

        if (!this.transaction.social.likeText.isEmpty() || this.transaction.social.commentsCount.intValue() > 0) {
            this.cardSocialContainer.setVisibility(View.VISIBLE);

            if (this.transaction.social.commentsCount.intValue() > 0) {
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
            this.cardLikesButton.setBackground(this.context.getResources().getDrawable(R.drawable.timeline_row_action_button_background_selected));
            this.cardLikesButtonText.setTextColor(this.context.getResources().getColor(android.R.color.white));
            this.cardLikesButtonPicto.setImageDrawable(this.context.getResources().getDrawable(R.drawable.social_like_full));
        }
        else {
            this.cardLikesButton.setBackground(this.context.getResources().getDrawable(R.drawable.timeline_row_action_button_background));
            this.cardLikesButtonText.setTextColor(this.context.getResources().getColor(R.color.placeholder));
            this.cardLikesButtonPicto.setImageDrawable(this.context.getResources().getDrawable(R.drawable.social_like));
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

        this.cardCommentsSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (cardCommentsTextfield.getText().length() > 0 && cardCommentsTextfield.getText().length() < 140) {
                    FloozRestClient.getInstance().commentTransaction(transaction.transactionId, cardCommentsTextfield.getText().toString(), new FloozHttpResponseHandler() {
                        @Override
                        public void success(Object response) {
                            FLComment com = (FLComment) response;

                            transaction.comments.add(com);
                            transaction.social.isCommented = true;
                            transaction.social.commentsCount = transaction.comments.size();

                            cardCommentsContainer.addView(createCommentRowView(com));
                        }

                        @Override
                        public void failure(int statusCode, FLError error) {

                        }
                    });

                    cardCommentsTextfield.setText("");
                    cardCommentsTextfield.clearFocus();
                    InputMethodManager imm = (InputMethodManager) context.getSystemService(
                            Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(parentActivity.getWindow().getDecorView().getRootView().getWindowToken() , 0);
                }
            }
        });
    }

    private View createCommentRowView(FLComment comment) {
        View commentRow;
        TextView commentText;
        TextView commentInfos;
        RoundedImageView commentPic;

        commentRow = this.inflater.inflate(R.layout.transaction_card_comment_row, null);
        commentPic = (RoundedImageView) commentRow.findViewById(R.id.card_comment_row_img);
        commentText = (TextView) commentRow.findViewById(R.id.card_comment_row_text);
        commentInfos = (TextView) commentRow.findViewById(R.id.card_comment_row_infos);

        commentText.setTypeface(CustomFonts.customContentRegular(this.inflater.getContext()));
        commentInfos.setTypeface(CustomFonts.customContentLight(this.inflater.getContext()));


        if (comment.user.avatarURL != null && !comment.user.avatarURL.isEmpty())
            ImageLoader.getInstance().displayImage(comment.user.avatarURL, commentPic);
        else
            commentPic.setImageDrawable(this.context.getResources().getDrawable(R.drawable.avatar_default));

        commentText.setText(comment.content);
        commentInfos.setText("@" + comment.user.username + " - " + DateFormat.format("dd MMM à hh:mm", comment.date));

        return commentRow;
    }

    public void setTransaction(FLTransaction transac) {
        this.transaction = transac;
        if (this.viewCreated)
            this.reloadView();
    }
}