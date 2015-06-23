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

import com.makeramen.RoundedImageView;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.List;

import me.flooz.app.Model.FLError;
import me.flooz.app.Model.FLTransaction;
import me.flooz.app.Network.FloozHttpResponseHandler;
import me.flooz.app.Network.FloozRestClient;
import me.flooz.app.R;
import me.flooz.app.UI.View.LoadingImageView;
import me.flooz.app.Utils.CustomFonts;
import me.flooz.app.Model.FLError;
import me.flooz.app.Model.FLTransaction;
import me.flooz.app.Network.FloozHttpResponseHandler;
import me.flooz.app.Network.FloozRestClient;

/**
 * Created by Flooz on 9/12/14.
 */
public class TimelineListAdapter extends BaseAdapter {

    private List<FLTransaction> transactions;
    private Context context;
    public TimelineListRowDelegate delegate;

    public interface TimelineListRowDelegate {
        void ListItemClick(FLTransaction transac);
        void ListItemCommentClick(FLTransaction transac);
        void ListItemImageClick(String imgUrl);
    }

    public TimelineListAdapter(Context context, List<FLTransaction> values) {
        this.context = context;
        this.transactions = values;
    }

    private static class ViewHolder {
        public TextView transactionText;
        public TextView transactionValue;
        public TextView transactionText3D;
        public TextView transactionLikesText;
        public TextView transactionLikesButtonText;
        public TextView transactionCommentsNumber;
        public TextView transactionCommentsButtonText;

        public RoundedImageView userPic;
        public LoadingImageView transactionPic;

        public RelativeLayout transactionRowContent;
        public LinearLayout transactionRowActionBar;
        public LinearLayout transactionSocialContainer;
    }

    @Override
    public int getCount() {
        return this.transactions.size();
    }

    @Override
    public FLTransaction getItem(int position) {
        return this.transactions.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View rowView;
        final ViewHolder holder;

        if (convertView == null) {
            rowView = LayoutInflater.from(context).inflate(R.layout.timeline_row, parent, false);

            holder = new ViewHolder();

            holder.transactionRowContent = (RelativeLayout) rowView.findViewById(R.id.timeline_row_content);
            holder.transactionRowActionBar = (LinearLayout) rowView.findViewById(R.id.timeline_row_actionBar);

            holder.transactionText = (TextView) rowView.findViewById(R.id.timelineTransactionText);
            holder.transactionValue = (TextView) rowView.findViewById(R.id.timelineTransactionValue);
            holder.transactionText3D = (TextView) rowView.findViewById(R.id.timelineTransactionText3D);
            holder.transactionLikesText = (TextView) rowView.findViewById(R.id.timelineTransactionLikesText);
            holder.transactionLikesButtonText = (TextView) rowView.findViewById(R.id.timelineTransactionLikesButtonText);
            holder.transactionCommentsButtonText = (TextView) rowView.findViewById(R.id.timelineTransactionCommentsButtonText);
            holder.transactionCommentsNumber = (TextView) rowView.findViewById(R.id.timelineTransactionCommentsNumber);

            holder.userPic = (RoundedImageView) rowView.findViewById(R.id.userTimelineTransactionPic);
            holder.transactionPic = (LoadingImageView) rowView.findViewById(R.id.timelineTransactionPic);

            holder.transactionSocialContainer = (LinearLayout) rowView.findViewById(R.id.timelineTransactionSocialLayout);

            holder.transactionText.setTypeface(CustomFonts.customContentLight(context));
            holder.transactionValue.setTypeface(CustomFonts.customTitleExtraLight(context), Typeface.BOLD);
            holder.transactionText3D.setTypeface(CustomFonts.customContentRegular(context));
            holder.transactionCommentsNumber.setTypeface(CustomFonts.customContentRegular(context));
            holder.transactionLikesText.setTypeface(CustomFonts.customContentRegular(context));
            holder.transactionLikesButtonText.setTypeface(CustomFonts.customContentRegular(context));
            holder.transactionCommentsButtonText.setTypeface(CustomFonts.customContentRegular(context));

            rowView.setTag(holder);
        }
        else {
            rowView = convertView;
            holder = (ViewHolder)rowView.getTag();
        }

        final FLTransaction currentTransaction = this.transactions.get(position);

        holder.transactionRowContent.setOnClickListener(v -> {
            if (delegate != null)
                delegate.ListItemClick(currentTransaction);
        });

        holder.transactionCommentsButtonText.setOnClickListener(v -> {
            if (delegate != null)
                delegate.ListItemCommentClick(currentTransaction);
        });

        holder.userPic.setImageDrawable(this.context.getResources().getDrawable(R.drawable.avatar_default));
        if (currentTransaction.avatarURL != null && !currentTransaction.avatarURL.isEmpty())
            ImageLoader.getInstance().displayImage(currentTransaction.avatarURL, holder.userPic);

        if (currentTransaction.text3d != null) {

            final SpannableStringBuilder sb = new SpannableStringBuilder(currentTransaction.text3d.get(0).toString() + currentTransaction.text3d.get(1).toString() + currentTransaction.text3d.get(2).toString());

            final ForegroundColorSpan usernameColor = new ForegroundColorSpan(this.context.getResources().getColor(android.R.color.white));
            final ForegroundColorSpan username2Color = new ForegroundColorSpan(this.context.getResources().getColor(android.R.color.white));
            final ForegroundColorSpan textColor = new ForegroundColorSpan(this.context.getResources().getColor(R.color.placeholder));
            final StyleSpan usernameBold = new StyleSpan(Typeface.BOLD);
            final StyleSpan username2Bold = new StyleSpan(Typeface.BOLD);

            int pos = 0;

            sb.setSpan(usernameColor, pos, pos + currentTransaction.text3d.get(0).toString().length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
            sb.setSpan(usernameBold, pos, pos + currentTransaction.text3d.get(0).toString().length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);

            pos += currentTransaction.text3d.get(0).toString().length();

            sb.setSpan(textColor, pos, pos + currentTransaction.text3d.get(1).toString().length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);

            pos += currentTransaction.text3d.get(1).toString().length();

            sb.setSpan(username2Color, pos, pos + currentTransaction.text3d.get(2).toString().length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
            sb.setSpan(username2Bold, pos, pos + currentTransaction.text3d.get(2).toString().length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);

            holder.transactionText3D.setText(sb);
        } else {
            holder.transactionText3D.setText("");
        }

        if (currentTransaction.content != null && !currentTransaction.content.isEmpty()) {
            holder.transactionText.setText(currentTransaction.content.toCharArray(), 0, currentTransaction.content.length());
            holder.transactionText.setVisibility(View.VISIBLE);
        }
        else {
            holder.transactionText.setVisibility(View.GONE);
        }

        if (currentTransaction.attachmentURL != null &&!currentTransaction.attachmentURL.isEmpty()) {
            holder.transactionPic.setImageFromUrl(currentTransaction.attachmentThumbURL);
            holder.transactionPic.setVisibility(View.VISIBLE);

            holder.transactionPic.setOnClickListener(v -> {
                if (delegate != null)
                    delegate.ListItemImageClick(currentTransaction.attachmentURL);
            });
        }
        else {
            holder.transactionPic.setVisibility(View.GONE);
        }

        if ((currentTransaction.social != null && currentTransaction.social.likeText != null && currentTransaction.social.commentsCount != null)
                && (!currentTransaction.social.likeText.isEmpty() || currentTransaction.social.commentsCount.intValue() > 0)) {
            holder.transactionSocialContainer.setVisibility(View.VISIBLE);

            if (currentTransaction.social.commentsCount.intValue() > 0) {
                if (currentTransaction.social.commentsCount.intValue() < 10)
                    holder.transactionCommentsNumber.setText("0" + currentTransaction.social.commentsCount.toString());
                else
                    holder.transactionCommentsNumber.setText(currentTransaction.social.commentsCount.toString().toCharArray(), 0, currentTransaction.social.commentsCount.toString().length());
                holder.transactionCommentsNumber.setVisibility(View.VISIBLE);
            } else {
                holder.transactionCommentsNumber.setVisibility(View.GONE);
            }


            if (!currentTransaction.social.likeText.isEmpty()) {
                holder.transactionLikesText.setText(currentTransaction.social.likeText.toCharArray(), 0, currentTransaction.social.likeText.length());
                holder.transactionLikesText.setVisibility(View.VISIBLE);
            } else {
                holder.transactionLikesText.setVisibility(View.GONE);
            }
        }
        else
            holder.transactionSocialContainer.setVisibility(View.GONE);

        if (currentTransaction.social != null && currentTransaction.social.isLiked) {
            holder.transactionLikesButtonText.setBackground(this.context.getResources().getDrawable(R.drawable.timeline_row_action_button_background_selected));
            holder.transactionLikesButtonText.setTextColor(this.context.getResources().getColor(android.R.color.white));
            holder.transactionLikesButtonText.setCompoundDrawablesWithIntrinsicBounds(R.drawable.social_like_full, 0, 0, 0);
        }
        else {
            holder.transactionLikesButtonText.setBackground(this.context.getResources().getDrawable(R.drawable.timeline_row_action_button_background));
            holder.transactionLikesButtonText.setTextColor(this.context.getResources().getColor(R.color.placeholder));
            holder.transactionLikesButtonText.setCompoundDrawablesWithIntrinsicBounds(R.drawable.social_like, 0, 0, 0);
        }

        holder.transactionLikesButtonText.setOnClickListener(v -> {
            if (FloozRestClient.getInstance().currentUser != null) {
                FloozRestClient.getInstance().likeTransaction(currentTransaction.transactionId, new FloozHttpResponseHandler() {
                    @Override
                    public void success(Object response) {
                        currentTransaction.social.isLiked = !currentTransaction.social.isLiked;
                        currentTransaction.social.likeText = (String) response;

                        if (!currentTransaction.social.likeText.isEmpty() || currentTransaction.social.commentsCount.intValue() > 0) {
                            holder.transactionSocialContainer.setVisibility(View.VISIBLE);

                            if (currentTransaction.social.commentsCount.intValue() > 0) {
                                holder.transactionCommentsNumber.setText(currentTransaction.social.commentsCount.toString());
                                holder.transactionCommentsNumber.setVisibility(View.VISIBLE);
                            } else {
                                holder.transactionCommentsNumber.setVisibility(View.GONE);
                            }


                            if (!currentTransaction.social.likeText.isEmpty()) {
                                holder.transactionLikesText.setText(currentTransaction.social.likeText);
                                holder.transactionLikesText.setVisibility(View.VISIBLE);
                            } else {
                                holder.transactionLikesText.setVisibility(View.GONE);
                            }
                        } else
                            holder.transactionSocialContainer.setVisibility(View.GONE);

                        if (currentTransaction.social.isLiked) {
                            holder.transactionLikesButtonText.setBackground(context.getResources().getDrawable(R.drawable.timeline_row_action_button_background_selected));
                            holder.transactionLikesButtonText.setTextColor(context.getResources().getColor(android.R.color.white));
                            holder.transactionLikesButtonText.setCompoundDrawablesWithIntrinsicBounds(R.drawable.social_like_full, 0, 0, 0);
                        }
                        else {
                            holder.transactionLikesButtonText.setBackground(context.getResources().getDrawable(R.drawable.timeline_row_action_button_background));
                            holder.transactionLikesButtonText.setTextColor(context.getResources().getColor(R.color.placeholder));
                            holder.transactionLikesButtonText.setCompoundDrawablesWithIntrinsicBounds(R.drawable.social_like, 0, 0, 0);
                        }
                    }

                    @Override
                    public void failure(int statusCode, FLError error) {

                    }
                });
            } else {
                if (delegate != null)
                    delegate.ListItemCommentClick(currentTransaction);
            }
        });

        holder.transactionValue.setText(currentTransaction.amountText.toCharArray(), 0, currentTransaction.amountText.length());

        return rowView;
    }
}