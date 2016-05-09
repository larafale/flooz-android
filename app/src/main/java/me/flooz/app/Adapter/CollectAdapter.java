package me.flooz.app.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.makeramen.roundedimageview.RoundedImageView;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
import java.util.List;

import me.flooz.app.App.FloozApplication;
import me.flooz.app.Model.FLComment;
import me.flooz.app.Model.FLSocial;
import me.flooz.app.Model.FLTransaction;
import me.flooz.app.Model.FLUser;
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

    public interface CollectSocialDelegate {
        void collectLikeClicked();
        void collectCommentClicked();
        void collectReport();
        void collectShowParticipants();
    }

    public CollectAdapter(Context ctx) {
        this.inflater = LayoutInflater.from(ctx);
        this.context = ctx;

        notifyDataSetChanged();
    }

    private void loadLikeCell(ViewGroup parent) {
        this.likeCellHolder = new LikeCellHolder();

        this.likeCellHolder.cellView = LayoutInflater.from(context).inflate(R.layout.comment_separator_row, null, false);

        this.likeCellHolder.likeLayout = (LinearLayout) this.likeCellHolder.cellView.findViewById(R.id.social_separator_like);
        this.likeCellHolder.likeImg = (ImageView) this.likeCellHolder.cellView.findViewById(R.id.social_separator_like_img);
        this.likeCellHolder.likeText = (TextView) this.likeCellHolder.cellView.findViewById(R.id.social_separator_like_text);
        this.likeCellHolder.commentLayout = (LinearLayout) this.likeCellHolder.cellView.findViewById(R.id.social_separator_comment);
        this.likeCellHolder.commentImg = (ImageView) this.likeCellHolder.cellView.findViewById(R.id.social_separator_comment_img);
        this.likeCellHolder.commentText = (TextView) this.likeCellHolder.cellView.findViewById(R.id.social_separator_comment_text);
        this.likeCellHolder.moreButton = (ImageView) this.likeCellHolder.cellView.findViewById(R.id.social_separator_more);

        this.likeCellHolder.likeText.setTypeface(CustomFonts.customContentRegular(this.context));
        this.likeCellHolder.commentText.setTypeface(CustomFonts.customContentRegular(this.context));

        this.likeCellHolder.moreButton.setColorFilter(this.context.getResources().getColor(R.color.placeholder));

        this.likeCellHolder.likeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (socialDelegate != null)
                    socialDelegate.collectLikeClicked();
            }
        });

        this.likeCellHolder.commentLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (socialDelegate != null)
                    socialDelegate.collectCommentClicked();
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

                    this.participantMasterCellHolder.participantText.setText(this.collect.participants.size() + " participant(s)");

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

                if (social.isLiked) {
                    this.likeCellHolder.likeText.setTextColor(this.context.getResources().getColor(R.color.pink));
                    this.likeCellHolder.likeImg.setColorFilter(this.context.getResources().getColor(R.color.pink));
                } else {
                    this.likeCellHolder.likeText.setTextColor(this.context.getResources().getColor(R.color.placeholder));
                    this.likeCellHolder.likeImg.setColorFilter(this.context.getResources().getColor(R.color.placeholder));
                }

                if (social.likesCount.longValue() > 0) {
                    this.likeCellHolder.likeText.setVisibility(View.VISIBLE);
                    this.likeCellHolder.likeText.setText(FLHelper.formatUserNumber(social.likesCount.longValue()));
                } else {
                    this.likeCellHolder.likeText.setVisibility(View.INVISIBLE);
                }

                if (social.isCommented) {
                    this.likeCellHolder.commentText.setTextColor(this.context.getResources().getColor(R.color.blue));
                    this.likeCellHolder.commentImg.setColorFilter(this.context.getResources().getColor(R.color.blue));
                } else {
                    this.likeCellHolder.commentText.setTextColor(this.context.getResources().getColor(R.color.placeholder));
                    this.likeCellHolder.commentImg.setColorFilter(this.context.getResources().getColor(R.color.placeholder));
                }

                if (social.commentsCount.longValue() > 0) {
                    this.likeCellHolder.commentText.setVisibility(View.VISIBLE);
                    this.likeCellHolder.commentText.setText(FLHelper.formatUserNumber(social.commentsCount.longValue()));
                } else {
                    this.likeCellHolder.commentText.setVisibility(View.INVISIBLE);
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
                return 2 + this.collect.social.commentsCount.intValue();
            }
            return 2;
        }
        return 0;
    }

    @Override
    public FLComment getItem(int i) {
        return (FLComment) this.collect.comments.get(i - 2);
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

        if (position == 0)
            return this.participantMasterCellHolder.cellView;
        else if (position == 1) {
            return this.likeCellHolder.cellView;
        } else {
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
    }

    class ViewHolder {
        TextView commentText;
        TextView commentInfos;
        RoundedImageView commentPic;
    }

    class LikeCellHolder {
        View cellView;

        LinearLayout likeLayout;
        TextView likeText;
        ImageView likeImg;

        LinearLayout commentLayout;
        TextView commentText;
        ImageView commentImg;

        ImageView moreButton;
    }

    class ParticipantMasterCellHolder {
        View cellView;

        TextView participantText;

        List<RoundedImageView> participantViews;

        ImageView arrow;
    }
}
