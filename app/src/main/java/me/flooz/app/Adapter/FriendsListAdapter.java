package me.flooz.app.Adapter;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.content.LocalBroadcastManager;
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
import me.flooz.app.Utils.ContactsManager;
import me.flooz.app.Utils.CustomFonts;
import me.flooz.app.Utils.CustomNotificationIntents;
import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;

public class FriendsListAdapter extends BaseAdapter implements StickyListHeadersAdapter {

    public Boolean isSearchActive = false;

    private Context context;
    private LayoutInflater inflater;

    public FriendsListAdapterDelegate delegate;

    public List<FLUser> suggestionList = new ArrayList<>(0);
    public List<FLUser> friendList = new ArrayList<>(0);
    public List<FLUser> pendingList = new ArrayList<>(0);

    public List<FLUser> searchFloozUsers = new ArrayList<>(0);
    private Handler searchHandler;

    private String searchData;

    private BroadcastReceiver reloadFriendsReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            FLUser currentUser = FloozRestClient.getInstance().currentUser;

            pendingList = currentUser.friendsRequest;
            friendList = currentUser.friends;

            if (pendingList == null)
                pendingList = new ArrayList<>(0);

            if (friendList == null)
                friendList = new ArrayList<>(0);

            notifyDataSetChanged();
        }
    };

    private BroadcastReceiver reloadSuggest = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            reloadSuggestions();
        }
    };

    private Runnable searchRunnable = new Runnable() {
        @Override
        public void run() {
            if (!searchData.isEmpty()) {
                isSearchActive = true;

                FloozRestClient.getInstance().searchUser(searchData, false, new FloozHttpResponseHandler() {
                    @Override
                    public void success(Object response) {
                        searchFloozUsers = new ArrayList<>((List<FLUser>)response);
                        if (isSearchActive)
                            notifyDataSetChanged();
                    }

                    @Override
                    public void failure(int statusCode, FLError error) {
                        searchFloozUsers.clear();
                        notifyDataSetChanged();
                    }
                });
            }
        }
    };

    public FriendsListAdapter(Context ctx) {
        this.inflater = LayoutInflater.from(ctx);
        this.context = ctx;

        this.reloadSuggestions();
        this.searchHandler = new Handler(Looper.getMainLooper());

        FLUser currentUser = FloozRestClient.getInstance().currentUser;

        pendingList = currentUser.friendsRequest;
        friendList = currentUser.friends;

        if (pendingList == null)
            pendingList = new ArrayList<>(0);

        if (friendList == null)
            friendList = new ArrayList<>(0);

        notifyDataSetChanged();
    }

    public void loadBroadcastReceivers() {
        LocalBroadcastManager.getInstance(FloozApplication.getAppContext()).registerReceiver(reloadFriendsReceiver,
                CustomNotificationIntents.filterReloadCurrentUser());

        LocalBroadcastManager.getInstance(FloozApplication.getAppContext()).registerReceiver(reloadSuggest,
                CustomNotificationIntents.filterReloadFriends());
    }

    public void unloadBroadcastReceivers() {
        LocalBroadcastManager.getInstance(FloozApplication.getAppContext()).unregisterReceiver(reloadFriendsReceiver);
        LocalBroadcastManager.getInstance(FloozApplication.getAppContext()).unregisterReceiver(reloadSuggest);
    }

    public void reloadSuggestions() {
        FloozRestClient.getInstance().loadFriendSuggestions(new FloozHttpResponseHandler() {
            @Override
            public void success(Object response) {
                suggestionList.clear();
                suggestionList.addAll((List<FLUser>) response);
                notifyDataSetChanged();
            }

            @Override
            public void failure(int statusCode, FLError error) {

            }
        });
    }

    public void reloadFriends() {
        FLUser currentUser = FloozRestClient.getInstance().currentUser;

        pendingList = currentUser.friendsRequest;
        friendList = currentUser.friends;

        if (pendingList == null)
            pendingList = new ArrayList<>(0);

        if (friendList == null)
            friendList = new ArrayList<>(0);

        notifyDataSetChanged();
    }

    public void reloadSuggestions(List<FLUser> friends) {
        suggestionList.clear();
        suggestionList.addAll(friends);
        notifyDataSetChanged();
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();

        if (this.delegate != null)
            this.delegate.dataReloaded();
    }

    @Override
    public View getHeaderView(int position, View convertView, ViewGroup parent) {
        HeaderViewHolder holder;

        if (convertView == null || convertView.getTag() == null) {
            holder = new HeaderViewHolder();
            convertView = inflater.inflate(R.layout.account_menu_header, parent, false);
            holder.text = (TextView) convertView.findViewById(R.id.account_menu_header_title);

            holder.text.setTypeface(CustomFonts.customContentBold(this.context));

            convertView.setTag(holder);
        } else {
            holder = (HeaderViewHolder) convertView.getTag();
        }

        if (!this.isSearchActive) {
            if (position < this.suggestionList.size())
                holder.text.setText(this.context.getResources().getString(R.string.FRIENDS_FRIENDS_SUGGESTION));
            else if (position >= this.suggestionList.size() && position < this.pendingList.size() + this.suggestionList.size())
                holder.text.setText(this.context.getResources().getString(R.string.FRIENDS_FRIENDS_REQUEST) + " (" + this.pendingList.size() + ")");
            else
                holder.text.setText(this.context.getResources().getString(R.string.FRIENDS_FRIENDS) + " (" + this.friendList.size() + ")");
        }
        else
            holder.text.setText(this.context.getResources().getString(R.string.FRIEND_PCIKER_SELECTION_CELL) + " (" + this.searchFloozUsers.size() + ")");

        return convertView;
    }

    @Override
    public long getHeaderId(int i) {
        if (!this.isSearchActive) {
            if (i < this.suggestionList.size())
                return FLUser.FLUserSelectedCanal.SuggestionCanal.ordinal();
            else if (i >= this.suggestionList.size() && i < this.pendingList.size() + this.suggestionList.size())
                return FLUser.FLUserSelectedCanal.PendingCanal.ordinal();
            else
                return FLUser.FLUserSelectedCanal.FriendsCanal.ordinal();
        }
        else
            return FLUser.FLUserSelectedCanal.SearchCanal.ordinal();
    }

    @Override
    public int getCount() {
        if (!this.isSearchActive)
            return this.suggestionList.size() + this.friendList.size() + this.pendingList.size();
        return this.searchFloozUsers.size();
    }

    @Override
    public FLUser getItem(int i) {
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

        if (!this.isSearchActive && position < this.suggestionList.size()) {
            holder.button.setImageResource(R.drawable.friends_add);
            holder.button.setVisibility(View.VISIBLE);

            holder.button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    holder.button.setOnClickListener(null);
                    holder.button.setImageResource(R.drawable.friends_accepted);
                    user.selectedCanal = FLUser.FLUserSelectedCanal.SuggestionCanal;
                    FloozRestClient.getInstance().sendFriendRequest(user.userId, user.getSelectedCanal(), new FloozHttpResponseHandler() {
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
                public void onClick(View v) {
                    holder.button.setOnClickListener(null);
                    holder.button.setImageResource(R.drawable.friends_accepted);
                    user.selectedCanal = FLUser.FLUserSelectedCanal.SearchCanal;
                    FloozRestClient.getInstance().sendFriendRequest(user.userId, user.getSelectedCanal(), new FloozHttpResponseHandler() {
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

    public void searchUser(String searchString) {
        this.searchHandler.removeCallbacks(searchRunnable);
        this.searchData = searchString;
        if (searchString.isEmpty() || searchString.length() < 3) {
            this.stopSearch();
        } else {
            this.searchHandler.postDelayed(searchRunnable, 500);
        }
    }

    public void stopSearch() {
        this.searchData = "";
        this.searchHandler.removeCallbacks(searchRunnable);
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
