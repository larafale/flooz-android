package me.flooz.app.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.makeramen.roundedimageview.RoundedImageView;
import com.nostra13.universalimageloader.core.ImageLoader;

import me.flooz.app.App.FloozApplication;
import me.flooz.app.Model.FLComment;
import me.flooz.app.Model.FLTransaction;
import me.flooz.app.R;
import me.flooz.app.Utils.CustomFonts;

/**
 * Created by Flooz on 23/06/16.
 */
public class TransactionAdapter extends BaseAdapter {

    private Context context;
    private LayoutInflater inflater;

    private FLTransaction transaction;

    public TransactionAdapter(Context ctx) {
        this.inflater = LayoutInflater.from(ctx);
        this.context = ctx;

        notifyDataSetChanged();
    }

    public void setTransaction(FLTransaction transaction) {
        this.transaction = transaction;
        this.notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        if (this.transaction != null) {
            if (this.transaction.social.commentsCount.intValue() > 0) {
                return this.transaction.social.commentsCount.intValue();
            }
        }
        return 0;
    }

    @Override
    public FLComment getItem(int i) {
        return (FLComment) this.transaction.comments.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
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
}
