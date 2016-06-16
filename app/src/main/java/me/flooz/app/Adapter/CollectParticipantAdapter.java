package me.flooz.app.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.makeramen.roundedimageview.RoundedImageView;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.Locale;

import me.flooz.app.Model.FLTransaction;
import me.flooz.app.Model.FLUser;
import me.flooz.app.R;
import me.flooz.app.Utils.CustomFonts;
import me.flooz.app.Utils.FLHelper;

/**
 * Created by Flooz on 08/05/16.
 */
public class CollectParticipantAdapter extends BaseAdapter {
    private Context context;
    private LayoutInflater inflater;

    private FLTransaction collect;

    public CollectParticipantAdapter(Context ctx) {
        this.inflater = LayoutInflater.from(ctx);
        this.context = ctx;

        notifyDataSetChanged();
    }

    public void setCollect(FLTransaction collect) {
        this.collect = collect;

        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        if (this.collect != null) {
            return this.collect.participants.size();
        }
        return 0;
    }

    @Override
    public FLUser getItem(int i) {
        return this.collect.participants.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (this.collect != null) {

            final FLUser user = this.getItem(position);

            final ViewHolder holder;

            if (convertView == null || convertView.getTag() == null) {
                holder = new ViewHolder();
                convertView = inflater.inflate(R.layout.collect_participant_row, parent, false);

                holder.username = (TextView) convertView.findViewById(R.id.participant_row_username);
                holder.fullname = (TextView) convertView.findViewById(R.id.participant_row_fullname);
                holder.amount = (TextView) convertView.findViewById(R.id.participant_row_amount);
                holder.arrow = (ImageView) convertView.findViewById(R.id.participant_row_arrow);
                holder.certified = (ImageView) convertView.findViewById(R.id.participant_row_certified);
                holder.avatar = (RoundedImageView) convertView.findViewById(R.id.participant_row_pic);

                holder.username.setTypeface(CustomFonts.customContentRegular(context));
                holder.fullname.setTypeface(CustomFonts.customContentRegular(context));
                holder.amount.setTypeface(CustomFonts.customContentBold(context));

                holder.arrow.setColorFilter(context.getResources().getColor(R.color.placeholder));

                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            holder.fullname.setText(user.fullname);

            if (user.isCactus)
                holder.username.setText("");
            else
                holder.username.setText("@" + user.username);

            if (user.isCertified)
                holder.certified.setVisibility(View.VISIBLE);
            else
                holder.certified.setVisibility(View.GONE);

            holder.avatar.setImageDrawable(this.context.getResources().getDrawable(R.drawable.avatar_default));
            if (user.avatarURL != null && !user.avatarURL.isEmpty())
                ImageLoader.getInstance().displayImage(user.avatarURL, holder.avatar);


            if (user.totalParticipations != null && user.totalParticipations.intValue() > 0) {
                holder.amount.setVisibility(View.VISIBLE);
                holder.amount.setText(FLHelper.trimTrailingZeros(String.format(Locale.US, "%.2f", user.totalParticipations.floatValue())) + " €");

            } else {
                holder.amount.setVisibility(View.GONE);
            }

            return convertView;
        }

        return convertView;
    }

    class ViewHolder {
        TextView username;
        TextView fullname;
        TextView amount;
        ImageView arrow;
        ImageView certified;
        RoundedImageView avatar;
    }
}
