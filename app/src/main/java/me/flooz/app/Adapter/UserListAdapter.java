package me.flooz.app.Adapter;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.makeramen.roundedimageview.RoundedImageView;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
import java.util.List;

import me.flooz.app.Model.FLTransaction;
import me.flooz.app.Model.FLUser;
import me.flooz.app.Network.FloozRestClient;
import me.flooz.app.R;
import me.flooz.app.UI.Activity.HomeActivity;
import me.flooz.app.UI.Fragment.Home.ProfileCardFragment;
import me.flooz.app.Utils.CustomFonts;

/**
 * Created by Wapazz on 25/09/15.
 */
public class UserListAdapter extends BaseAdapter {

    private Context context;
    private LayoutInflater inflater;

    private FLUser currentUser;
    public List<FLUser> userList = new ArrayList<>(0);

    public UserListAdapter(Context ctx, FLUser user) {
        this.inflater = LayoutInflater.from(ctx);
        this.context = ctx;

        this.currentUser = user;
        if (this.currentUser != null)
            this.userList = currentUser.friends;

        if (this.userList == null)
            this.userList = new ArrayList<>(0);

        notifyDataSetChanged();
    }

    public void setCurrentUser(FLUser currentUser) {
        this.currentUser = currentUser;
        if (this.currentUser != null)
            this.userList = currentUser.friends;
    }

    @Override
    public int getCount() {
        if (userList.size() > 0)
            return userList.size();
        return 1;
    }

    @Override
    public FLUser getItem(int i) {
        return userList.get(i);
    }

    public FLUser getItem(String name) {
        FLUser user;
        for (int i = 0; i < userList.size(); i++) {
            if ((user = userList.get(i)).fullname.contentEquals(name)) {
                return user;
            }
        }
        return null;
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View convertView, ViewGroup parent) {
        final ViewHolder holder;

        if (this.userList.size() == 0) {
            View empty = LayoutInflater.from(context).inflate(R.layout.empty_row, parent, false);

            TextView emptyText = (TextView) empty.findViewById(R.id.empty_row_text);

            emptyText.setTypeface(CustomFonts.customContentRegular(context));
            emptyText.setText(context.getResources().getString(R.string.EMPTY_FRIEND_CELL));

            return empty;
        }

        if (convertView == null) {
            holder = new ViewHolder();
            convertView = inflater.inflate(R.layout.user_list_row, parent, false);
            holder.username = (TextView) convertView.findViewById(R.id.user_list_row_username);
            holder.fullname = (TextView) convertView.findViewById(R.id.user_list_row_fullname);
            holder.pic = (RoundedImageView) convertView.findViewById(R.id.user_list_row_pic);
            holder.username.setTypeface(CustomFonts.customTitleExtraLight(this.context), Typeface.BOLD);
            holder.fullname.setTypeface(CustomFonts.customContentRegular(this.context));

            convertView.setTag(holder);
        }
        else {
            holder = (ViewHolder) convertView.getTag();
        }

        final FLUser user = (FLUser) this.getItem(i);

        holder.fullname.setText(user.fullname);
        holder.username.setText("@" + user.username);

        holder.pic.setImageDrawable(this.context.getResources().getDrawable(R.drawable.avatar_default));
        if (user.avatarURL != null && !user.avatarURL.isEmpty())
            ImageLoader.getInstance().displayImage(user.avatarURL, holder.pic);

        return convertView;
    }

    public class ViewHolder {
        TextView fullname;
        TextView username;
        RoundedImageView pic;
    }
}


