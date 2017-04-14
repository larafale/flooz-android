package me.flooz.app.Adapter;

import android.content.Context;
import android.graphics.Typeface;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.makeramen.roundedimageview.RoundedImageView;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

import me.flooz.app.App.FloozApplication;
import me.flooz.app.Model.FLComment;
import me.flooz.app.Model.FLSocial;
import me.flooz.app.Model.FLTransaction;
import me.flooz.app.Model.FLUser;
import me.flooz.app.Network.FloozRestClient;
import me.flooz.app.R;
import me.flooz.app.Utils.CustomFonts;
import me.flooz.app.Utils.FLHelper;

/**
 * Created by Flooz on 30/04/16.
 */
public class CollectAdapter extends BaseAdapter {

    private Context context;
    private LayoutInflater inflater;

    private FLTransaction collect;
    public CollectSocialDelegate socialDelegate;

    private LikeCellHolder likeCellHolder;
    private ParticipantMasterCellHolder participantMasterCellHolder;
    private InvitedCellHolder invitedMasterCellHolder;

    public interface CollectSocialDelegate {
        void collectLikeClicked();
        void collectCommentClicked();
        void collectReport();
        void collectShowParticipants();
        void collectShareClicked();
        void collectShowLikes();
        void collectShowInvited();
    }

    public CollectAdapter(Context ctx) {
        this.inflater = LayoutInflater.from(ctx);
        this.context = ctx;

        notifyDataSetChanged();
    }

    private void loadLikeCell(ViewGroup parent) {
        this.likeCellHolder = new LikeCellHolder();

        this.likeCellHolder.cellView = LayoutInflater.from(context).inflate(R.layout.comment_separator_row, null, false);

        this.likeCellHolder.labelsContainer = (RelativeLayout) this.likeCellHolder.cellView.findViewById(R.id.social_separator_labels);
        this.likeCellHolder.likesLabel = (TextView) this.likeCellHolder.cellView.findViewById(R.id.social_separator_like_label);
        this.likeCellHolder.commentsLabel = (TextView) this.likeCellHolder.cellView.findViewById(R.id.social_separator_comment_label);

        this.likeCellHolder.likesButton = (LinearLayout) this.likeCellHolder.cellView.findViewById(R.id.social_separator_like);
        this.likeCellHolder.likesButtonImg = (ImageView) this.likeCellHolder.cellView.findViewById(R.id.social_separator_like_img);

        this.likeCellHolder.commentButton = (LinearLayout) this.likeCellHolder.cellView.findViewById(R.id.social_separator_comment);
        this.likeCellHolder.commentButtonImg = (ImageView) this.likeCellHolder.cellView.findViewById(R.id.social_separator_comment_img);

        this.likeCellHolder.shareButton = (LinearLayout) this.likeCellHolder.cellView.findViewById(R.id.social_separator_share);
        this.likeCellHolder.shareButtonImg = (ImageView) this.likeCellHolder.cellView.findViewById(R.id.social_separator_share_img);

        this.likeCellHolder.moreButton = (LinearLayout) this.likeCellHolder.cellView.findViewById(R.id.social_separator_more);
        this.likeCellHolder.moreButtonImg = (ImageView) this.likeCellHolder.cellView.findViewById(R.id.social_separator_more_img);

        this.likeCellHolder.likesLabel.setTypeface(CustomFonts.customContentRegular(this.context));
        this.likeCellHolder.commentsLabel.setTypeface(CustomFonts.customContentRegular(this.context));

        this.likeCellHolder.likesButtonImg.setColorFilter(this.context.getResources().getColor(R.color.background_social_button));
        this.likeCellHolder.commentButtonImg.setColorFilter(this.context.getResources().getColor(R.color.background_social_button));
        this.likeCellHolder.shareButtonImg.setColorFilter(this.context.getResources().getColor(R.color.background_social_button));
        this.likeCellHolder.moreButtonImg.setColorFilter(this.context.getResources().getColor(R.color.background_social_button));

        this.likeCellHolder.likesLabel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (socialDelegate != null)
                    socialDelegate.collectShowLikes();
            }
        });

        this.likeCellHolder.likesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (socialDelegate != null)
                    socialDelegate.collectLikeClicked();
            }
        });

        this.likeCellHolder.commentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (socialDelegate != null)
                    socialDelegate.collectCommentClicked();
            }
        });

        this.likeCellHolder.shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (socialDelegate != null)
                    socialDelegate.collectShareClicked();
            }
        });

        this.likeCellHolder.moreButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (socialDelegate != null)
                    socialDelegate.collectReport();
            }
        });
    }

    private void loadParticipantMastercell(ViewGroup parent) {
        this.participantMasterCellHolder = new ParticipantMasterCellHolder();

        this.participantMasterCellHolder.cellView = LayoutInflater.from(context).inflate(R.layout.collect_participant_mastercell, parent, false);

        this.participantMasterCellHolder.participantText = (TextView) this.participantMasterCellHolder.cellView.findViewById(R.id.participant_mastercell_text);
        this.participantMasterCellHolder.arrow = (ImageView) this.participantMasterCellHolder.cellView.findViewById(R.id.participant_mastercell_arrow);

        this.participantMasterCellHolder.participantViews = new ArrayList<>(5);
        this.participantMasterCellHolder.participantViews.add((RoundedImageView) this.participantMasterCellHolder.cellView.findViewById(R.id.participant_mastercell_user_5));
        this.participantMasterCellHolder.participantViews.add((RoundedImageView) this.participantMasterCellHolder.cellView.findViewById(R.id.participant_mastercell_user_4));
        this.participantMasterCellHolder.participantViews.add((RoundedImageView) this.participantMasterCellHolder.cellView.findViewById(R.id.participant_mastercell_user_3));
        this.participantMasterCellHolder.participantViews.add((RoundedImageView) this.participantMasterCellHolder.cellView.findViewById(R.id.participant_mastercell_user_2));
        this.participantMasterCellHolder.participantViews.add((RoundedImageView) this.participantMasterCellHolder.cellView.findViewById(R.id.participant_mastercell_user_1));

        this.participantMasterCellHolder.participantText.setTypeface(CustomFonts.customContentRegular(context));

        this.participantMasterCellHolder.cellView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (socialDelegate != null)
                    socialDelegate.collectShowParticipants();
            }
        });
    }

    private void loadInvitedMastercell(ViewGroup parent) {
        this.invitedMasterCellHolder = new InvitedCellHolder();

        this.invitedMasterCellHolder.cellView = LayoutInflater.from(context).inflate(R.layout.collect_invited_cell, parent, false);

        this.invitedMasterCellHolder.invitedText = (TextView) this.invitedMasterCellHolder.cellView.findViewById(R.id.invited_cell_text);
        this.invitedMasterCellHolder.arrow = (ImageView) this.invitedMasterCellHolder.cellView.findViewById(R.id.invited_cell_arrow);
        this.invitedMasterCellHolder.inviteButton = (TextView) this.invitedMasterCellHolder.cellView.findViewById(R.id.invited_cell_button);

        this.invitedMasterCellHolder.invitedText.setTypeface(CustomFonts.customContentRegular(context));
        this.invitedMasterCellHolder.inviteButton.setTypeface(CustomFonts.customContentRegular(context));

        this.invitedMasterCellHolder.cellView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (socialDelegate != null)
                    socialDelegate.collectShowInvited();
            }
        });

        this.invitedMasterCellHolder.inviteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (socialDelegate != null)
                    socialDelegate.collectShareClicked();
            }
        });
    }

    public void reload() {

        if (this.collect != null) {
            if (this.participantMasterCellHolder != null) {
                if (this.collect.participants == null || this.collect.participants.size() == 0) {
                    this.participantMasterCellHolder.participantText.setText("0 participant");

                    this.participantMasterCellHolder.arrow.setVisibility(View.INVISIBLE);

                    for (RoundedImageView imageView: this.participantMasterCellHolder.participantViews) {
                        imageView.setVisibility(View.INVISIBLE);
                    }
                } else {
                    this.participantMasterCellHolder.arrow.setVisibility(View.VISIBLE);

                    if (this.collect.participants.size() > 1) {
                        this.participantMasterCellHolder.participantText.setText(this.collect.participants.size() + " participants");
                    } else {
                        this.participantMasterCellHolder.participantText.setText(this.collect.participants.size() + " participant");
                    }

                    for (RoundedImageView imageView: this.participantMasterCellHolder.participantViews) {
                        imageView.setVisibility(View.INVISIBLE);
                    }

                    int nbSubviews = 5;

                    if (nbSubviews > this.collect.participants.size())
                        nbSubviews = this.collect.participants.size();

                    for (int i = nbSubviews - 1; i >= 0 ; i--) {
                        FLUser participant = this.collect.participants.get(i);

                        this.participantMasterCellHolder.participantViews.get(i).setVisibility(View.VISIBLE);
                        this.participantMasterCellHolder.participantViews.get(i).setImageDrawable(context.getResources().getDrawable(R.drawable.avatar_default));

                        if (participant.avatarURL != null && !participant.avatarURL.isEmpty())
                            ImageLoader.getInstance().displayImage(participant.avatarURL, this.participantMasterCellHolder.participantViews.get(i));
                    }
                }
            }

            if (this.likeCellHolder != null) {
                FLSocial social = this.collect.social;

                if (social != null) {
                    if ((social.likesCount.intValue() > 0 && this.collect.options.likeEnabled) || (social.commentsCount.intValue() > 0 && this.collect.options.commentEnabled)) {
                        this.likeCellHolder.labelsContainer.setVisibility(View.VISIBLE);

                        if (social.likesCount.intValue() > 0 && this.collect.options.likeEnabled) {
                            this.likeCellHolder.likesLabel.setVisibility(View.VISIBLE);

                            String number = FLHelper.formatUserNumber(social.likesCount.longValue());
                            String likeText = " J'AIME";

                            final SpannableStringBuilder likeSb = new SpannableStringBuilder(number + likeText);

                            final ForegroundColorSpan likeNumberColor = new ForegroundColorSpan(context.getResources().getColor(android.R.color.white));
                            final ForegroundColorSpan likeTextColor = new ForegroundColorSpan(context.getResources().getColor(R.color.placeholder));
                            final StyleSpan likeStyle = new StyleSpan(Typeface.BOLD);

                            int likePos = 0;

                            likeSb.setSpan(likeNumberColor, likePos, likePos + number.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                            likeSb.setSpan(likeStyle, likePos, likePos + number.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);

                            likePos += number.length();

                            likeSb.setSpan(likeTextColor, likePos, likePos + likeText.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);

                            this.likeCellHolder.likesLabel.setText(likeSb);
                        } else {
                            this.likeCellHolder.likesLabel.setVisibility(View.GONE);
                        }

                        if (social.commentsCount.intValue() > 0 && this.collect.options.commentEnabled) {
                            this.likeCellHolder.commentsLabel.setVisibility(View.VISIBLE);

                            final ForegroundColorSpan commentNumberColor = new ForegroundColorSpan(this.context.getResources().getColor(android.R.color.white));
                            final ForegroundColorSpan commentTextColor = new ForegroundColorSpan(this.context.getResources().getColor(R.color.placeholder));
                            final StyleSpan commentStyle = new StyleSpan(Typeface.BOLD);

                            String number = FLHelper.formatUserNumber(social.commentsCount.longValue());

                            String commentText = " COMMENTAIRE";

                            if (social.commentsCount.intValue() > 1)
                                commentText = " COMMENTAIRES";

                            final SpannableStringBuilder commentSb = new SpannableStringBuilder(number + commentText);

                            int commentPos = 0;

                            commentSb.setSpan(commentNumberColor, commentPos, commentPos + number.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                            commentSb.setSpan(commentStyle, commentPos, commentPos + number.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);

                            commentPos += number.length();

                            commentSb.setSpan(commentTextColor, commentPos, commentPos + commentText.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);

                            this.likeCellHolder.commentsLabel.setText(commentSb);
                        } else {
                            this.likeCellHolder.commentsLabel.setVisibility(View.GONE);
                        }
                    } else {
                        this.likeCellHolder.labelsContainer.setVisibility(View.GONE);
                    }

                    if (social.isLiked && this.collect.options.likeEnabled) {
                        likeCellHolder.likesButton.setVisibility(View.VISIBLE);
                        likeCellHolder.likesButtonImg.setColorFilter(this.context.getResources().getColor(R.color.pink));
                    } else if (this.collect.options.likeEnabled) {
                        likeCellHolder.likesButton.setVisibility(View.VISIBLE);
                        likeCellHolder.likesButtonImg.setColorFilter(this.context.getResources().getColor(R.color.background_social_button));
                    } else {
                        likeCellHolder.likesButton.setVisibility(View.GONE);
                    }

                    if (social.isCommented && this.collect.options.commentEnabled) {
                        likeCellHolder.commentButton.setVisibility(View.VISIBLE);
                        likeCellHolder.commentButtonImg.setColorFilter(this.context.getResources().getColor(R.color.blue));
                    } else if (this.collect.options.commentEnabled) {
                        likeCellHolder.commentButton.setVisibility(View.VISIBLE);
                        likeCellHolder.commentButtonImg.setColorFilter(this.context.getResources().getColor(R.color.background_social_button));
                    } else {
                        likeCellHolder.commentButton.setVisibility(View.GONE);
                    }
                } else {
                    this.likeCellHolder.labelsContainer.setVisibility(View.GONE);

                    if (this.collect.options.likeEnabled) {
                        likeCellHolder.likesButton.setVisibility(View.VISIBLE);
                        likeCellHolder.likesButtonImg.setColorFilter(this.context.getResources().getColor(R.color.background_social_button));
                    } else {
                        likeCellHolder.likesButton.setVisibility(View.GONE);
                    }

                    if (this.collect.options.commentEnabled) {
                        likeCellHolder.commentButton.setVisibility(View.VISIBLE);
                        likeCellHolder.commentButtonImg.setColorFilter(this.context.getResources().getColor(R.color.background_social_button));
                    } else {
                        likeCellHolder.commentButton.setVisibility(View.GONE);
                    }
                }

                if (this.collect.options.shareEnabled) {
                    likeCellHolder.shareButton.setVisibility(View.VISIBLE);
                } else {
                    likeCellHolder.shareButton.setVisibility(View.GONE);
                }
            }

            if (this.invitedMasterCellHolder != null) {
                if (this.collect.invitations == null || this.collect.invitations.size() == 0) {
                    this.invitedMasterCellHolder.invitedText.setText("0 invité");

                    this.invitedMasterCellHolder.arrow.setVisibility(View.INVISIBLE);

                    if (FloozRestClient.getInstance().currentUser.userId.contentEquals(this.collect.creator.userId))
                        this.invitedMasterCellHolder.inviteButton.setVisibility(View.VISIBLE);
                    else
                        this.invitedMasterCellHolder.inviteButton.setVisibility(View.GONE);
                } else {
                    this.invitedMasterCellHolder.arrow.setVisibility(View.VISIBLE);
                    this.invitedMasterCellHolder.inviteButton.setVisibility(View.GONE);

                    if (this.collect.invitations.size() > 1) {
                        this.invitedMasterCellHolder.invitedText.setText(this.collect.invitations.size() + " invités");
                    } else {
                        this.invitedMasterCellHolder.invitedText.setText(this.collect.invitations.size() + " invité");
                    }
                }
            }
        }

        this.notifyDataSetChanged();
    }

    public void setCollect(FLTransaction collect) {
        this.collect = collect;
        this.notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        if (this.collect != null) {
            if (this.collect.social.commentsCount.intValue() > 0) {
                if (this.collect.status == FLTransaction.TransactionStatus.TransactionStatusPending)
                    return 2 + this.collect.social.commentsCount.intValue();
                return 3 + this.collect.social.commentsCount.intValue();
            }

            if (this.collect.status == FLTransaction.TransactionStatus.TransactionStatusPending)
                return 2;
            return 3;
        }
        return 0;
    }

    @Override
    public FLComment getItem(int i) {
        if (this.collect.status == FLTransaction.TransactionStatus.TransactionStatusPending)
            return (FLComment) this.collect.comments.get(i - 2);

        return (FLComment) this.collect.comments.get(i - 3);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (this.participantMasterCellHolder == null) {
            this.loadParticipantMastercell(parent);
            this.reload();
        }

        if (this.likeCellHolder == null) {
            this.loadLikeCell(parent);
            this.reload();
        }

        if (this.invitedMasterCellHolder == null) {
            this.loadInvitedMastercell(parent);
            this.reload();
        }

        if (this.collect.status == FLTransaction.TransactionStatus.TransactionStatusPending) {
            if (position == 0) {
                return this.participantMasterCellHolder.cellView;
            } else if (position == 1) {
                return this.likeCellHolder.cellView;
            }
        } else {
            if (position == 0) {
                return this.participantMasterCellHolder.cellView;
            } else if (position == 1) {
                return this.invitedMasterCellHolder.cellView;
            } else if (position == 2) {
                return this.likeCellHolder.cellView;
            }
        }

        final FLComment comment = this.getItem(position);

        final ViewHolder holder;

        if (convertView == null || convertView.getTag() == null) {
            holder = new ViewHolder();
            convertView = inflater.inflate(R.layout.transaction_card_comment_row, parent, false);
            holder.commentPic = (RoundedImageView) convertView.findViewById(R.id.card_comment_row_img);
            holder.commentText = (TextView) convertView.findViewById(R.id.card_comment_row_text);
            holder.commentInfos = (TextView) convertView.findViewById(R.id.card_comment_row_infos);

            holder.commentText.setTypeface(CustomFonts.customContentRegular(this.context));
            holder.commentInfos.setTypeface(CustomFonts.customContentLight(this.context));
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        if (comment.user.avatarURL != null && !comment.user.avatarURL.isEmpty())
            ImageLoader.getInstance().displayImage(comment.user.avatarURL, holder.commentPic);
        else
            holder.commentPic.setImageDrawable(this.context.getResources().getDrawable(R.drawable.avatar_default));

        holder.commentText.setText(comment.content);
        holder.commentInfos.setText("@" + comment.user.username + " - " + comment.dateText);

        holder.commentPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FloozApplication.getInstance().showUserProfile(comment.user);
            }
        });

        return convertView;
    }

    class ViewHolder {
        TextView commentText;
        TextView commentInfos;
        RoundedImageView commentPic;
    }

    class LikeCellHolder {
        View cellView;

        RelativeLayout labelsContainer;
        TextView likesLabel;
        TextView commentsLabel;

        LinearLayout likesButton;
        ImageView likesButtonImg;

        LinearLayout commentButton;
        ImageView commentButtonImg;

        LinearLayout shareButton;
        ImageView shareButtonImg;

        LinearLayout moreButton;
        ImageView moreButtonImg;
    }

    class ParticipantMasterCellHolder {
        View cellView;

        TextView participantText;

        List<RoundedImageView> participantViews;

        ImageView arrow;
    }

    class InvitedCellHolder {
        View cellView;

        TextView invitedText;

        TextView inviteButton;

        ImageView arrow;
    }
}
