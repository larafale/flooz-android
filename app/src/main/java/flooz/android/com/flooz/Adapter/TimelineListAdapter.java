package flooz.android.com.flooz.Adapter;

import android.app.ActionBar;
import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.makeramen.RoundedImageView;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.List;

import flooz.android.com.flooz.Model.FLTransaction;
import flooz.android.com.flooz.R;
import flooz.android.com.flooz.Utils.CustomFonts;

/**
 * Created by Flooz on 9/12/14.
 */
public class TimelineListAdapter extends ArrayAdapter<FLTransaction> {

    private List<FLTransaction> transactions;
    private Context context;

    public TimelineListAdapter(Context context, List<FLTransaction> values) {
        super(context, R.layout.timeline_row, values);
        this.context = context;
        this.transactions = values;
    }

    private static class ViewHolder {
        public TextView transactionText;
        public TextView transactionValue;
        public TextView transactionText3D_1;
        public TextView transactionText3D_2;
        public TextView transactionText3D_3;
        public TextView transactionLikesText;
        public TextView transactionLikeButton;
        public TextView transactionCommentButton;
        public TextView transactionCommentsNumber;

        public RoundedImageView userPic;
        public RoundedImageView transactionPic;

        public LinearLayout transactionLikesContainer;
        public LinearLayout transactionCommentsContainer;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View rowView;
        ViewHolder holder;

        if (convertView == null) {
            rowView = inflater.inflate(R.layout.timeline_row, parent, false);

            holder = new ViewHolder();

            holder.transactionText = (TextView) rowView.findViewById(R.id.timelineTransactionText);
            holder.transactionValue = (TextView) rowView.findViewById(R.id.timelineTransactionValue);
            holder.transactionText3D_1 = (TextView) rowView.findViewById(R.id.timelineTransactionText3D1);
            holder.transactionText3D_2 = (TextView) rowView.findViewById(R.id.timelineTransactionText3D2);
            holder.transactionText3D_3 = (TextView) rowView.findViewById(R.id.timelineTransactionText3D3);
            holder.transactionLikesText = (TextView) rowView.findViewById(R.id.timelineTransactionLikesText);
            holder.transactionLikeButton = (TextView) rowView.findViewById(R.id.timelineTransactionLikeButton);
            holder.transactionCommentButton = (TextView) rowView.findViewById(R.id.timelineTransactionCommentButton);
            holder.transactionCommentsNumber = (TextView) rowView.findViewById(R.id.timelineTransactionCommentsNumber);

            holder.userPic = (RoundedImageView) rowView.findViewById(R.id.userTimelineTransactionPic);
            holder.transactionPic = (RoundedImageView) rowView.findViewById(R.id.timelineTransactionPic);

            holder.transactionLikesContainer = (LinearLayout) rowView.findViewById(R.id.timelineTransactionLikesLayout);
            holder.transactionCommentsContainer = (LinearLayout) rowView.findViewById(R.id.timelineTransactionCommentsLayout);

            holder.transactionText.setTypeface(CustomFonts.customContentLight(inflater.getContext()));
            holder.transactionValue.setTypeface(CustomFonts.customTitleExtraLight(inflater.getContext()), Typeface.BOLD);
            holder.transactionText3D_1.setTypeface(CustomFonts.customContentRegular(inflater.getContext()), Typeface.BOLD);
            holder.transactionText3D_2.setTypeface(CustomFonts.customContentRegular(inflater.getContext()), Typeface.BOLD);
            holder.transactionText3D_3.setTypeface(CustomFonts.customContentRegular(inflater.getContext()), Typeface.BOLD);
            holder.transactionLikesText.setTypeface(CustomFonts.customContentRegular(inflater.getContext()));
            holder.transactionLikeButton.setTypeface(CustomFonts.customContentRegular(inflater.getContext()));
            holder.transactionCommentButton.setTypeface(CustomFonts.customContentRegular(inflater.getContext()));
            holder.transactionCommentsNumber.setTypeface(CustomFonts.customContentRegular(inflater.getContext()));

            rowView.setTag(holder);
        }
        else {
            rowView = convertView;
            holder = (ViewHolder)rowView.getTag();
        }

        FLTransaction currentTransaction = this.transactions.get(position);

        holder.userPic.setImageDrawable(null);

        if (currentTransaction.avatarURL != null && !currentTransaction.avatarURL.isEmpty())
            ImageLoader.getInstance().displayImage(currentTransaction.avatarURL, holder.userPic);
        else
            holder.userPic.setImageDrawable(this.context.getResources().getDrawable(R.drawable.avatar_default));

        holder.transactionText3D_1.setText(currentTransaction.text3d.get(0).toString());
        holder.transactionText3D_2.setText(currentTransaction.text3d.get(1).toString());
        holder.transactionText3D_3.setText(currentTransaction.text3d.get(2).toString());

        if (!currentTransaction.content.isEmpty()) {
            holder.transactionText.setText(currentTransaction.content);
            holder.transactionText.setVisibility(View.VISIBLE);
        }
        else {
            holder.transactionText.setVisibility(View.INVISIBLE);
        }

        if (!currentTransaction.attachmentURL.isEmpty()) {
            holder.transactionPic.setImageDrawable(null);
            ImageLoader.getInstance().displayImage(currentTransaction.attachmentURL, holder.transactionPic);
            holder.transactionPic.setVisibility(View.VISIBLE);

            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams( holder.transactionPic.getLayoutParams());
            lp.setMargins(0, (int)inflater.getContext().getResources().getDimension(R.dimen.timeline_cell_block_margin), 0, 0);
            lp.height = ActionBar.LayoutParams.WRAP_CONTENT;
            holder.transactionPic.setLayoutParams(lp);
        }
        else {
            holder.transactionPic.setVisibility(View.INVISIBLE);

            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(holder.transactionPic.getLayoutParams());
            lp.setMargins(0, 0, 0, 0);
            lp.height = 0;
            holder.transactionPic.setLayoutParams(lp);
        }

        if (currentTransaction.social.likesCount.intValue() > 0) {
            holder.transactionLikesText.setText(currentTransaction.social.likeText);
            holder.transactionLikesContainer.setVisibility(View.VISIBLE);

            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams( holder.transactionLikesContainer.getLayoutParams());
            lp.setMargins(0, (int)inflater.getContext().getResources().getDimension(R.dimen.timeline_cell_block_margin), 0, 0);
            lp.height = ActionBar.LayoutParams.WRAP_CONTENT;
            holder.transactionLikesContainer.setLayoutParams(lp);
        }
        else {
            holder.transactionLikesContainer.setVisibility(View.INVISIBLE);

            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams( holder.transactionLikesContainer.getLayoutParams());
            lp.setMargins(0, 0, 0, 0);
            lp.height = 0;
            holder.transactionLikesContainer.setLayoutParams(lp);
        }

        if (currentTransaction.social.commentsCount.intValue() > 0) {
            holder.transactionCommentsNumber.setText(currentTransaction.social.commentsCount.toString());
            holder.transactionCommentsContainer.setVisibility(View.VISIBLE);
        }
        else
            holder.transactionCommentsContainer.setVisibility(View.INVISIBLE);

        holder.transactionValue.setText(currentTransaction.amountText);

        return rowView;
    }
}
