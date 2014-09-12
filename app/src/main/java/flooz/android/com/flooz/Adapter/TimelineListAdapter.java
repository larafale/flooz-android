package flooz.android.com.flooz.Adapter;

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

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.timeline_row, parent, false);

        FLTransaction currentTransaction = this.transactions.get(position);

        TextView transactionText = (TextView) rowView.findViewById(R.id.timelineTransactionText);
        TextView transactionValue = (TextView) rowView.findViewById(R.id.timelineTransactionValue);
        TextView transactionText3D_1 = (TextView) rowView.findViewById(R.id.timelineTransactionText3D1);
        TextView transactionText3D_2 = (TextView) rowView.findViewById(R.id.timelineTransactionText3D2);
        TextView transactionText3D_3 = (TextView) rowView.findViewById(R.id.timelineTransactionText3D3);
        TextView transactionLikesText = (TextView) rowView.findViewById(R.id.timelineTransactionLikesText);
        TextView transactionLikeButton = (TextView) rowView.findViewById(R.id.timelineTransactionLikeButton);
        TextView transactionCommentButton = (TextView) rowView.findViewById(R.id.timelineTransactionCommentButton);
        TextView transactionCommentsNumber = (TextView) rowView.findViewById(R.id.timelineTransactionCommentsNumber);

        RoundedImageView userPic = (RoundedImageView) rowView.findViewById(R.id.userTimelineTransactionPic);
        RoundedImageView transactionPic = (RoundedImageView) rowView.findViewById(R.id.timelineTransactionPic);

        LinearLayout transactionLikesContainer = (LinearLayout) rowView.findViewById(R.id.timelineTransactionLikesLayout);
        LinearLayout transactionCommentsContainer = (LinearLayout) rowView.findViewById(R.id.timelineTransactionCommentsLayout);

        transactionText.setTypeface(CustomFonts.customContentLight(inflater.getContext()));
        transactionValue.setTypeface(CustomFonts.customTitleExtraLight(inflater.getContext()), Typeface.BOLD);
        transactionText3D_1.setTypeface(CustomFonts.customContentRegular(inflater.getContext()), Typeface.BOLD);
        transactionText3D_2.setTypeface(CustomFonts.customContentRegular(inflater.getContext()), Typeface.BOLD);
        transactionText3D_3.setTypeface(CustomFonts.customContentRegular(inflater.getContext()), Typeface.BOLD);
        transactionLikesText.setTypeface(CustomFonts.customContentRegular(inflater.getContext()));
        transactionLikeButton.setTypeface(CustomFonts.customContentRegular(inflater.getContext()));
        transactionCommentButton.setTypeface(CustomFonts.customContentRegular(inflater.getContext()));
        transactionCommentsNumber.setTypeface(CustomFonts.customContentRegular(inflater.getContext()));

        if (currentTransaction.avatarURL != null)
            ImageLoader.getInstance().displayImage(currentTransaction.avatarURL, userPic);
        else
            userPic.setImageDrawable(this.context.getResources().getDrawable(R.drawable.avatar_default));

        transactionText3D_1.setText(currentTransaction.text3d.get(0).toString());
        transactionText3D_2.setText(currentTransaction.text3d.get(1).toString());
        transactionText3D_3.setText(currentTransaction.text3d.get(2).toString());

        if (!currentTransaction.content.isEmpty())
            transactionText.setText(currentTransaction.content);
        else {
            transactionText.setVisibility(View.INVISIBLE);
        }

        if (!currentTransaction.attachmentThumbURL.isEmpty())
            ImageLoader.getInstance().displayImage(currentTransaction.attachmentThumbURL, transactionPic);
        else {
            transactionPic.setVisibility(View.INVISIBLE);

            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(transactionPic.getLayoutParams());
            lp.setMargins(0, 0, 0, 0);
            lp.height = 0;
            transactionPic.setLayoutParams(lp);
        }

        if (currentTransaction.social.likesCount.intValue() > 0)
            transactionLikesText.setText(currentTransaction.social.likeText);
        else {
            transactionLikesContainer.setVisibility(View.INVISIBLE);

            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(transactionLikesContainer.getLayoutParams());
            lp.setMargins(0, 0, 0, 0);
            lp.height = 0;
            transactionLikesContainer.setLayoutParams(lp);
        }

        if (currentTransaction.social.commentsCount.intValue() > 0)
            transactionCommentsNumber.setText(currentTransaction.social.commentsCount.toString());
        else
            transactionCommentsContainer.setVisibility(View.INVISIBLE);

        transactionValue.setText(currentTransaction.amountText);

        return rowView;
    }
}
