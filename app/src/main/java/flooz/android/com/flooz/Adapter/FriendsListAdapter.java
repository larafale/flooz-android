package flooz.android.com.flooz.Adapter;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.makeramen.RoundedImageView;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
import java.util.List;

import flooz.android.com.flooz.App.FloozApplication;
import flooz.android.com.flooz.Model.FLError;
import flooz.android.com.flooz.Model.FLUser;
import flooz.android.com.flooz.Network.FloozHttpResponseHandler;
import flooz.android.com.flooz.Network.FloozRestClient;
import flooz.android.com.flooz.R;
import flooz.android.com.flooz.Utils.CustomFonts;
import flooz.android.com.flooz.Utils.CustomNotificationIntents;
import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;

/**
 * Created by Flooz on 10/13/14.
 */
public class FriendsListAdapter extends BaseAdapter implements StickyListHeadersAdapter {

    private Boolean isSearchActive = false;

    private Context context;
    private LayoutInflater inflater;

    private List<FLUser> suggestionList = new ArrayList<FLUser>(0);
    private List<FLUser> friendList = new ArrayList<FLUser>(0);
    private List<FLUser> pendingList = new ArrayList<FLUser>(0);

    private List<FLUser> searchFloozUsers = new ArrayList<FLUser>(0);

    private BroadcastReceiver reloadFriendsReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            FLUser currentUser = FloozRestClient.getInstance().currentUser;

            pendingList = currentUser.friendsRequest;
            friendList = currentUser.friends;

            if (pendingList == null)
                pendingList = new ArrayList<FLUser>(0);

            if (friendList == null)
                friendList = new ArrayList<FLUser>(0);

            notifyDataSetChanged();
        }
    };

    private BroadcastReceiver reloadContent = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
//            reloadSuggestions();
            FloozRestClient.getInstance().updateCurrentUser(null);
        }
    };

    public FriendsListAdapter(Context ctx) {
        this.inflater = LayoutInflater.from(ctx);
        this.context = ctx;

        LocalBroadcastManager.getInstance(FloozApplication.getAppContext()).registerReceiver(reloadFriendsReceiver,
                CustomNotificationIntents.filterReloadCurrentUser());

        LocalBroadcastManager.getInstance(FloozApplication.getAppContext()).registerReceiver(reloadContent,
                CustomNotificationIntents.filterShowSlidingRightMenu());

        this.reloadSuggestions();
    }

    public void reloadSuggestions() {
        FloozRestClient.getInstance().loadFriendSuggestions(new FloozHttpResponseHandler() {
            @Override
            public void success(Object response) {
                suggestionList.clear();
                suggestionList.addAll((List<FLUser>)response);
                notifyDataSetChanged();
            }

            @Override
            public void failure(int statusCode, FLError error) {

            }
        });
    }

    @Override
    public View getHeaderView(int position, View convertView, ViewGroup parent) {
        HeaderViewHolder holder;

        if (convertView == null) {
            holder = new HeaderViewHolder();
            convertView = inflater.inflate(R.layout.user_list_header, parent, false);
            holder.text = (TextView) convertView.findViewById(R.id.user_list_header_text);

            holder.text.setTypeface(CustomFonts.customTitleExtraLight(this.context));

            convertView.setTag(holder);
        } else {
            holder = (HeaderViewHolder) convertView.getTag();
        }

        if (!this.isSearchActive) {
            if (position < this.suggestionList.size())
                holder.text.setText(this.context.getResources().getString(R.string.FRIENDS_FRIENDS_SUGGESTION));
            else if (position >= this.suggestionList.size() && position < this.pendingList.size() + this.suggestionList.size())
                holder.text.setText(this.context.getResources().getString(R.string.FRIENDS_FRIENDS_REQUEST));
            else
                holder.text.setText(this.context.getResources().getString(R.string.FRIENDS_FRIENDS));
        }
        else
            holder.text.setText(this.context.getResources().getString(R.string.FRIEND_PCIKER_SELECTION_CELL));

        return convertView;
    }

    @Override
    public long getHeaderId(int i) {
        if (!this.isSearchActive) {
            if (i < this.suggestionList.size())
                return 0;
            else if (i >= this.suggestionList.size() && i < this.pendingList.size() + this.suggestionList.size())
                return 1;
            else
                return 2;
        }
        else
            return 0;
    }

    @Override
    public int getCount() {
        if (!this.isSearchActive)
            return this.suggestionList.size() + this.friendList.size() + this.pendingList.size();
        return this.searchFloozUsers.size();
    }

    @Override
    public Object getItem(int i) {
        if (!this.isSearchActive) {
            if (i < this.suggestionList.size())
                return this.suggestionList.get(i);
            else if (i >= this.suggestionList.size() && i < this.pendingList.size() + this.suggestionList.size())
                return this.pendingList.get(i - this.suggestionList.size());
            else
                return this.friendList.get(i - this.suggestionList.size() - this.pendingList.size());
        }
        else
            return this.searchFloozUsers.get(i);
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

            holder.username.setTypeface(CustomFonts.customTitleExtraLight(this.context));
            holder.fullname.setTypeface(CustomFonts.customTitleExtraLight(this.context));

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        final FLUser user = (FLUser)this.getItem(position);

        holder.fullname.setText(user.fullname);
        holder.username.setText("@" + user.username);

        if (user.avatarURL != null && !user.avatarURL.isEmpty()) {
            ImageLoader.getInstance().displayImage(user.avatarURL, holder.pic);
        }
        else
            holder.pic.setImageDrawable(this.context.getResources().getDrawable(R.drawable.avatar_default));

        if (!this.isSearchActive && position < this.suggestionList.size()) {
            holder.button.setImageResource(R.drawable.friends_add);
            holder.button.setVisibility(View.VISIBLE);

            holder.button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    holder.button.setOnClickListener(null);
                    holder.button.setImageResource(R.drawable.friends_accepted);
                    FloozRestClient.getInstance().sendFriendRequest(user.userId, new FloozHttpResponseHandler() {
                        @Override
                        public void success(Object response) {
                            reloadSuggestions();
                            FloozRestClient.getInstance().updateCurrentUser(null);
                        }

                        @Override
                        public void failure(int statusCode, FLError error) {

                        }
                    });
                }
            });
        }
        else if (this.isSearchActive && FloozRestClient.getInstance().currentUser.userIsAFriend(user)) {
            holder.button.setImageResource(R.drawable.friends_accepted);
            holder.button.setVisibility(View.VISIBLE);
            holder.button.setOnClickListener(null);
        }
        else if (this.isSearchActive) {
            holder.button.setImageResource(R.drawable.friends_add);
            holder.button.setVisibility(View.VISIBLE);

            holder.button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    holder.button.setOnClickListener(null);
                    holder.button.setImageResource(R.drawable.friends_accepted);
                    FloozRestClient.getInstance().sendFriendRequest(user.userId, new FloozHttpResponseHandler() {
                        @Override
                        public void success(Object response) {
                            FloozRestClient.getInstance().updateCurrentUser(null);
                        }

                        @Override
                        public void failure(int statusCode, FLError error) {

                        }
                    });
                }
            });
        }
        else
            holder.button.setVisibility(View.GONE);

        return convertView;
    }

    public FriendKind getUserKind(int position) {
        if (!this.isSearchActive) {
            if (position < this.suggestionList.size())
                return FriendKind.SUGGESTION;
            else if (position >= this.suggestionList.size() && position < this.pendingList.size() + this.suggestionList.size())
                return FriendKind.REQUEST;
            else
                return FriendKind.FRIEND;
        }
        else
            return FriendKind.SEARCH;
    }

    public void searchUser(String searchString) {

        if (searchString != null && searchString.length() > 0) {
            this.isSearchActive = true;

            this.searchFloozUsers.clear();

            this.notifyDataSetChanged();

            FloozRestClient.getInstance().searchUser(searchString, false, new FloozHttpResponseHandler() {
                @Override
                public void success(Object response) {
                    searchFloozUsers = new ArrayList<FLUser>((List<FLUser>)response);
                    if (isSearchActive)
                        notifyDataSetChanged();
                }

                @Override
                public void failure(int statusCode, FLError error) {

                }
            });
        }
        else
            this.stopSearch();
    }

    public void stopSearch() {
        this.reloadSuggestions();
        this.isSearchActive = false;
        this.notifyDataSetChanged();
    }

    public enum FriendKind {
        SUGGESTION,
        REQUEST,
        FRIEND,
        SEARCH
    }

    class HeaderViewHolder {
        TextView text;
    }

    class ViewHolder {
        TextView fullname;
        TextView username;
        RoundedImageView pic;
        ImageView button;
    }
}
