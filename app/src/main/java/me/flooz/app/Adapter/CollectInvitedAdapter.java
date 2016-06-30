package me.flooz.app.Adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
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

import me.flooz.app.Model.FLError;
import me.flooz.app.Model.FLUser;
import me.flooz.app.Network.FloozHttpResponseHandler;
import me.flooz.app.Network.FloozRestClient;
import me.flooz.app.R;
import me.flooz.app.UI.Tools.ActionSheet;
import me.flooz.app.UI.Tools.ActionSheetItem;
import me.flooz.app.Utils.CustomFonts;

/**
 * Created by Flooz on 29/06/16.
 */
public class CollectInvitedAdapter extends BaseAdapter {
    private Context context;
    private LayoutInflater inflater;

    public List<FLUser> pendingList = new ArrayList<>(0);

    public CollectInvitedAdapter(Context ctx) {
        this.inflater = LayoutInflater.from(ctx);
        this.context = ctx;

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

    private void setFriendButton(ImageView button, boolean isFriend) {
        if (!isFriend) {
            button.setImageResource(R.drawable.follow);
            button.setColorFilter(context.getResources().getColor(R.color.blue));
            button.setBackgroundResource(R.drawable.frame_action_button);
        }
        else {
            button.setImageResource(R.drawable.unfollow);
            button.setColorFilter(Color.WHITE);
            button.setBackgroundResource(R.drawable.background_action_button);
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;

        if (convertView == null) {
            holder = new ViewHolder();
            convertView = inflater.inflate(R.layout.user_list_row, parent, false);
            holder.container = (LinearLayout) convertView.findViewById(R.id.user_list_row_layout);
            holder.username = (TextView) convertView.findViewById(R.id.user_list_row_username);
            holder.fullname = (TextView) convertView.findViewById(R.id.user_list_row_fullname);
            holder.pic = (RoundedImageView) convertView.findViewById(R.id.user_list_row_pic);
            holder.button = (ImageView) convertView.findViewById(R.id.user_list_row_button);

            holder.button.setColorFilter(context.getResources().getColor(R.color.blue));
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
            ImageLoader.getInstance().displayImage(user.avatarURL, holder.pic);

        if (!user.isFriendable)
            holder.button.setVisibility(View.GONE);
        else {
            this.setFriendButton(holder.button, user.isFriend);
            holder.button.setVisibility(View.VISIBLE);
        }

        holder.button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (user.isFriendable) {
                    if (!user.isFriend) {
                        user.isFriend = true;
                        notifyDataSetChanged();
                        FloozRestClient.getInstance().sendFriendRequest(user.userId, user.getSelectedCanal(), new FloozHttpResponseHandler() {
                            @Override
                            public void success(Object response) {
                                FloozRestClient.getInstance().updateCurrentUser(null);
                            }

                            @Override
                            public void failure(int statusCode, FLError error) {
                                user.isFriendable = true;
                                notifyDataSetChanged();
                            }
                        });
                    } else {
                        List<ActionSheetItem> items = new ArrayList<>();
                        items.add(new ActionSheetItem(context, R.string.MENU_REMOVE_FRIENDS, new ActionSheetItem.ActionSheetItemClickListener() {
                            @Override
                            public void onClick() {
                                user.isFriend = false;
                                notifyDataSetChanged();
                                FloozRestClient.getInstance().performActionOnFriend(user.userId, FloozRestClient.FriendAction.Delete, new FloozHttpResponseHandler() {
                                    @Override
                                    public void success(Object response) {
                                        FloozRestClient.getInstance().updateCurrentUser(null);
                                    }

                                    @Override
                                    public void failure(int statusCode, FLError error) {
                                        user.isFriend = true;
                                        notifyDataSetChanged();
                                    }
                                });
                            }
                        }));

                        ActionSheet.showWithItems(context, items);
                    }
                }
            }
        });

        return convertView;
    }

    class ViewHolder {
        LinearLayout container;
        TextView fullname;
        TextView username;
        RoundedImageView pic;
        ImageView button;
    }
}
