package me.flooz.app.Adapter;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
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

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import me.flooz.app.Model.FLError;
import me.flooz.app.Model.FLTransaction;
import me.flooz.app.Model.FLUser;
import me.flooz.app.Network.FloozHttpResponseHandler;
import me.flooz.app.Network.FloozRestClient;
import me.flooz.app.R;
import me.flooz.app.UI.View.LoadingImageView;
import me.flooz.app.Utils.CustomFonts;
import me.flooz.app.Model.FLError;
import me.flooz.app.Model.FLTransaction;
import me.flooz.app.Network.FloozHttpResponseHandler;
import me.flooz.app.Network.FloozRestClient;
import me.flooz.app.Utils.FLHelper;

/**
 * Created by Flooz on 9/12/14.
 */
public class TimelineListAdapter extends BaseAdapter {

    private List<FLTransaction> transactions;
    private Context context;
    public TimelineListRowDelegate delegate;

    public Boolean hasNextURL;
    public Boolean loading;
    public Boolean showEmpty;

    public interface TimelineListRowDelegate {
        void ListItemClick(FLTransaction transac);
        void ListItemCommentClick(FLTransaction transac);
        void ListItemImageClick(String imgUrl);
        void ListItemUserClick(FLUser user);
    }

    public TimelineListAdapter(Context context, List<FLTransaction> values) {
        this.context = context;
        this.transactions = values;
        if (this.transactions == null)
            this.transactions = new ArrayList<>();
        hasNextURL = false;
        loading = false;
        showEmpty = false;
    }

    private static class ViewHolder {
        public TextView transactionText;
        public TextView transactionValue;
        public TextView transactionText3D;
        public TextView transactionWhen;
        public TextView transactionLocationText;
        public TextView transactionLikesButtonText;
        public TextView transactionCommentsButtonText;

        public ImageView transactionLocationImg;
        public RoundedImageView userPic;
        public LoadingImageView transactionPic;

        public ImageView transactionLikesButtonImg;
        public ImageView transactionCommentsButtonImg;
        public ImageView transactionShareButtonImg;
        public ImageView transactionMoreButtonImg;

        public RelativeLayout transactionRowContent;
        public LinearLayout transactionRowActionBar;
        public LinearLayout transactionLocationLayout;

        public LinearLayout transactionLikesButton;
        public LinearLayout transactionCommentsButton;
        public LinearLayout transactionShareButton;
        public LinearLayout transactionMoreButton;

    }

    public void setTransactions(List<FLTransaction> transactions){
        this.transactions = transactions;
    }

    @Override
    public int getCount() {
        if (this.transactions.size() == 0 && (this.loading || this.showEmpty))
            return 1;

        if (this.hasNextURL)
            return this.transactions.size() + 1;

        return this.transactions.size();
    }

    @Override
    public FLTransaction getItem(int position) {
        if (position < this.transactions.size())
            return this.transactions.get(position);
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (this.transactions.size() == 0 && this.loading)
            return LayoutInflater.from(context).inflate(R.layout.progress_row, parent, false);

        else if (this.transactions.size() == 0 && this.showEmpty) {
            View empty = LayoutInflater.from(context).inflate(R.layout.empty_row, parent, false);

            TextView emptyText = (TextView) empty.findViewById(R.id.empty_row_text);

            emptyText.setTypeface(CustomFonts.customContentRegular(context));
            emptyText.setText(context.getResources().getString(R.string.EMPTY_FLOOZ_CELL));

            return empty;
        }

        if (position == this.transactions.size())
            return LayoutInflater.from(context).inflate(R.layout.progress_row, parent, false);

        View rowView;
        final ViewHolder holder;

        if (convertView == null || convertView.getTag() == null) {
            rowView = LayoutInflater.from(context).inflate(R.layout.timeline_row, parent, false);

            holder = new ViewHolder();

            holder.transactionRowContent = (RelativeLayout) rowView.findViewById(R.id.timeline_row_content);
            holder.transactionRowActionBar = (LinearLayout) rowView.findViewById(R.id.timeline_row_actionBar);

            holder.transactionText = (TextView) rowView.findViewById(R.id.timelineTransactionText);
            holder.transactionValue = (TextView) rowView.findViewById(R.id.timelineTransactionValue);
            holder.transactionWhen = (TextView) rowView.findViewById(R.id.timelineTransactionWhen);
            holder.transactionText3D = (TextView) rowView.findViewById(R.id.timelineTransactionText3D);
            holder.transactionLocationText = (TextView) rowView.findViewById(R.id.timelineTransactionLocationText);
            holder.transactionLikesButtonText = (TextView) rowView.findViewById(R.id.timelineTransactionLikesButtonText);
            holder.transactionCommentsButtonText = (TextView) rowView.findViewById(R.id.timelineTransactionCommentsButtonText);

            holder.transactionLocationImg = (ImageView) rowView.findViewById(R.id.timelineTransactionLocationImg);
            holder.userPic = (RoundedImageView) rowView.findViewById(R.id.userTimelineTransactionPic);
            holder.transactionPic = (LoadingImageView) rowView.findViewById(R.id.timelineTransactionPic);

            holder.transactionShareButtonImg = (ImageView) rowView.findViewById(R.id.timelineTransactionShareButtonImg);
            holder.transactionCommentsButtonImg = (ImageView) rowView.findViewById(R.id.timelineTransactionCommentsButtonImg);
            holder.transactionLikesButtonImg = (ImageView) rowView.findViewById(R.id.timelineTransactionLikesButtonImg);
            holder.transactionMoreButtonImg = (ImageView) rowView.findViewById(R.id.timelineTransactionMoreButtonImg);

            holder.transactionLocationLayout = (LinearLayout) rowView.findViewById(R.id.timelineTransactionLocationLayout);

            holder.transactionShareButton = (LinearLayout) rowView.findViewById(R.id.timelineTransactionShareButton);
            holder.transactionCommentsButton = (LinearLayout) rowView.findViewById(R.id.timelineTransactionCommentsButton);
            holder.transactionLikesButton = (LinearLayout) rowView.findViewById(R.id.timelineTransactionLikesButton);
            holder.transactionMoreButton = (LinearLayout) rowView.findViewById(R.id.timelineTransactionMoreButton);

            holder.transactionLocationImg.setColorFilter(context.getResources().getColor(R.color.placeholder));
            holder.transactionShareButtonImg.setColorFilter(this.context.getResources().getColor(R.color.background_social_button));
            holder.transactionMoreButtonImg.setColorFilter(this.context.getResources().getColor(R.color.background_social_button));

            holder.transactionWhen.setTypeface(CustomFonts.customContentRegular(context));
            holder.transactionText.setTypeface(CustomFonts.customContentLight(context));
            holder.transactionValue.setTypeface(CustomFonts.customTitleExtraLight(context), Typeface.BOLD);
            holder.transactionText3D.setTypeface(CustomFonts.customContentRegular(context));
            holder.transactionLocationText.setTypeface(CustomFonts.customContentRegular(context));
            holder.transactionLikesButtonText.setTypeface(CustomFonts.customContentRegular(context));
            holder.transactionCommentsButtonText.setTypeface(CustomFonts.customContentRegular(context));

            rowView.setTag(holder);
        }
        else {
            rowView = convertView;
            holder = (ViewHolder)rowView.getTag();
        }

        final FLTransaction currentTransaction = this.transactions.get(position);

        holder.userPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (delegate != null)
                    delegate.ListItemUserClick(currentTransaction.starter);
            }
        });

        holder.transactionRowContent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (delegate != null)
                    delegate.ListItemClick(currentTransaction);
            }
        });

        holder.transactionCommentsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (delegate != null)
                    delegate.ListItemCommentClick(currentTransaction);
            }
        });

        holder.userPic.setImageDrawable(this.context.getResources().getDrawable(R.drawable.avatar_default));
        if (currentTransaction.avatarURL != null && !currentTransaction.avatarURL.isEmpty())
            ImageLoader.getInstance().displayImage(currentTransaction.avatarURL, holder.userPic);

        holder.transactionWhen.setText(currentTransaction.when);

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
            holder.transactionPic.setImageFromUrl(currentTransaction.attachmentURL);
            holder.transactionPic.setVisibility(View.VISIBLE);

            holder.transactionPic.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (delegate != null)
                        delegate.ListItemImageClick(currentTransaction.attachmentURL);
                }
            });
        }
        else {
            holder.transactionPic.setVisibility(View.GONE);
        }

        if (currentTransaction.location != null && !currentTransaction.location.isEmpty()) {
            holder.transactionLocationText.setText(currentTransaction.location.toCharArray(), 0, currentTransaction.location.length());
            holder.transactionLocationLayout.setVisibility(View.VISIBLE);
        } else {
            holder.transactionLocationLayout.setVisibility(View.GONE);
        }

//        if ((currentTransaction.social != null && currentTransaction.social.likeText != null && currentTransaction.social.commentsCount != null)
//                && (!currentTransaction.social.likeText.isEmpty() || currentTransaction.social.commentsCount.intValue() > 0)) {
//            holder.transactionSocialContainer.setVisibility(View.VISIBLE);
//
//            if (!currentTransaction.social.commentText.isEmpty()) {
//                holder.transactionCommentsText.setText(currentTransaction.social.commentText.toCharArray(), 0, currentTransaction.social.commentText.length());
//                holder.transactionCommentsText.setVisibility(View.VISIBLE);
//            } else {
//                holder.transactionCommentsText.setVisibility(View.GONE);
//            }
//
//            if (!currentTransaction.social.likeText.isEmpty()) {
//                holder.transactionLikesText.setText(currentTransaction.social.likeText.toCharArray(), 0, currentTransaction.social.likeText.length());
//                holder.transactionLikesText.setVisibility(View.VISIBLE);
//            } else {
//                holder.transactionLikesText.setVisibility(View.GONE);
//            }
//        }
//        else
//            holder.transactionSocialContainer.setVisibility(View.GONE);

        if (currentTransaction.social != null && currentTransaction.social.likesCount.intValue() > 0) {
            holder.transactionLikesButtonText.setVisibility(View.VISIBLE);
            holder.transactionLikesButtonText.setText(FLHelper.formatUserNumber(currentTransaction.social.likesCount.longValue()));

            if (currentTransaction.social.isLiked) {
                holder.transactionLikesButtonText.setTextColor(this.context.getResources().getColor(R.color.pink));
                holder.transactionLikesButtonImg.setColorFilter(this.context.getResources().getColor(R.color.pink));
            } else {
                holder.transactionLikesButtonText.setTextColor(this.context.getResources().getColor(R.color.background_social_button));
                holder.transactionLikesButtonImg.setColorFilter(this.context.getResources().getColor(R.color.background_social_button));
            }
        } else {
            holder.transactionLikesButtonText.setVisibility(View.INVISIBLE);
            holder.transactionLikesButtonImg.setColorFilter(this.context.getResources().getColor(R.color.background_social_button));
        }

        if (currentTransaction.social != null && currentTransaction.social.commentsCount.intValue() > 0) {
            holder.transactionCommentsButtonText.setVisibility(View.VISIBLE);
            holder.transactionCommentsButtonText.setText(FLHelper.formatUserNumber(currentTransaction.social.commentsCount.longValue()));

            if (currentTransaction.social.isCommented) {
                holder.transactionCommentsButtonText.setTextColor(this.context.getResources().getColor(R.color.blue));
                holder.transactionCommentsButtonImg.setColorFilter(this.context.getResources().getColor(R.color.blue));
            } else {
                holder.transactionCommentsButtonText.setTextColor(this.context.getResources().getColor(R.color.background_social_button));
                holder.transactionCommentsButtonImg.setColorFilter(this.context.getResources().getColor(R.color.background_social_button));
            }
        } else {
            holder.transactionCommentsButtonText.setVisibility(View.INVISIBLE);
            holder.transactionCommentsButtonImg.setColorFilter(this.context.getResources().getColor(R.color.background_social_button));
        }


        holder.transactionLikesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (FloozRestClient.getInstance().currentUser != null) {
                    if (currentTransaction.social.isLiked) {
                        currentTransaction.social.isLiked = false;
                        currentTransaction.social.likesCount = currentTransaction.social.likesCount.intValue() - 1;

                        if (currentTransaction.social.likesCount.intValue() > 0) {
                            holder.transactionLikesButtonText.setVisibility(View.VISIBLE);
                            holder.transactionLikesButtonText.setText(FLHelper.formatUserNumber(currentTransaction.social.likesCount.longValue()));
                        } else {
                            holder.transactionLikesButtonText.setVisibility(View.INVISIBLE);
                        }

                        holder.transactionLikesButtonText.setTextColor(context.getResources().getColor(R.color.background_social_button));
                        holder.transactionLikesButtonImg.setColorFilter(context.getResources().getColor(R.color.background_social_button));
                    }
                    else {
                        currentTransaction.social.isLiked = true;
                        currentTransaction.social.likesCount = currentTransaction.social.likesCount.intValue() + 1;

                        if (currentTransaction.social.likesCount.intValue() > 0) {
                            holder.transactionLikesButtonText.setVisibility(View.VISIBLE);
                            holder.transactionLikesButtonText.setText(FLHelper.formatUserNumber(currentTransaction.social.likesCount.longValue()));
                        } else {
                            holder.transactionLikesButtonText.setVisibility(View.INVISIBLE);
                        }

                        holder.transactionLikesButtonText.setTextColor(context.getResources().getColor(R.color.pink));
                        holder.transactionLikesButtonImg.setColorFilter(context.getResources().getColor(R.color.pink));
                    }


                    FloozRestClient.getInstance().likeTransaction(currentTransaction.transactionId, new FloozHttpResponseHandler() {
                        @Override
                        public void success(Object response) {
                            currentTransaction.setJson((JSONObject)response);
                            notifyDataSetChanged();
                        }

                        @Override
                        public void failure(int statusCode, FLError error) {

                        }
                    });
                } else {
                    if (delegate != null)
                        delegate.ListItemCommentClick(currentTransaction);
                }
            }
        });

        holder.transactionValue.setText(currentTransaction.amountText.toCharArray(), 0, currentTransaction.amountText.length());

        return rowView;
    }
}
