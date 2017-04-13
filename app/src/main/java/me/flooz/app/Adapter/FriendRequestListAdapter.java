package me.flooz.app.Adapter;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.makeramen.roundedimageview.RoundedImageView;

import java.util.ArrayList;
import java.util.List;

import me.flooz.app.App.FloozApplication;
import me.flooz.app.Model.FLError;
import me.flooz.app.Model.FLUser;
import me.flooz.app.Network.FloozHttpResponseHandler;
import me.flooz.app.Network.FloozRestClient;
import me.flooz.app.R;
import me.flooz.app.Utils.CustomFonts;

/**
 * Created by Flooz on 10/22/15.
 */
public class FriendRequestListAdapter extends BaseAdapter {
    private Context context;
    private LayoutInflater inflater;

    public List<FLUser> pendingList = new ArrayList<>(0);

    public FriendRequestListAdapter(Context ctx) {
        this.inflater = LayoutInflater.from(ctx);
        this.context = ctx;

        FLUser currentUser = FloozRestClient.getInstance().currentUser;

        pendingList = currentUser.friendsRequest;

        if (pendingList == null)
            pendingList = new ArrayList<>(0);

        notifyDataSetChanged();
    }

    public void refreshFriendList() {
        FLUser currentUser = FloozRestClient.getInstance().currentUser;

        pendingList = currentUser.friendsRequest;

        if (pendingList == null)
            pendingList = new ArrayList<>(0);

        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return this.pendingList.size();
    }

    @Override
    public FLUser getItem(int i) {
        return this.pendingList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;

        if (convertView == null) {
            holder = new ViewHolder();
            convertView = inflater.inflate(R.layout.user_list_row, parent, false);
            holder.username = (TextView) convertView.findViewById(R.id.user_list_row_username);
            holder.fullname = (TextView) convertView.findViewById(R.id.user_list_row_fullname);
            holder.pic = (RoundedImageView) convertView.findViewById(R.id.user_list_row_pic);
            holder.button = (ImageView) convertView.findViewById(R.id.user_list_row_button);

            holder.button.setVisibility(View.GONE);

            holder.username.setTypeface(CustomFonts.customTitleExtraLight(this.context), Typeface.BOLD);
            holder.fullname.setTypeface(CustomFonts.customContentRegular(this.context));

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        final FLUser user = this.getItem(position);

        holder.fullname.setText(user.fullname);
        holder.username.setText("@" + user.username);

        holder.pic.setImageDrawable(this.context.getResources().getDrawable(R.drawable.avatar_default));
        if (user.avatarURL != null && !user.avatarURL.isEmpty())
            FloozApplication.getInstance().imageFetcher.attachImage(user.avatarURL, holder.pic);

        return convertView;
    }

    class ViewHolder {
        TextView fullname;
        TextView username;
        RoundedImageView pic;
        ImageView button;
    }
}
