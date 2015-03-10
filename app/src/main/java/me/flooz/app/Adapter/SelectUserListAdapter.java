package me.flooz.app.Adapter;

import android.content.Context;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.makeramen.RoundedImageView;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import me.flooz.app.Model.FLError;
import me.flooz.app.Model.FLUser;
import me.flooz.app.Network.FloozHttpResponseHandler;
import me.flooz.app.Network.FloozRestClient;
import me.flooz.app.R;
import me.flooz.app.Utils.ContactsManager;
import me.flooz.app.Utils.CustomFonts;
import me.flooz.app.Model.FLError;
import me.flooz.app.Model.FLUser;
import me.flooz.app.Network.FloozHttpResponseHandler;
import me.flooz.app.Network.FloozRestClient;
import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;

/**
 * Created by Flooz on 10/2/14.
 */
public class SelectUserListAdapter extends BaseAdapter implements StickyListHeadersAdapter {

    private Boolean isSearchActive = false;

    private Context context;
    private LayoutInflater inflater;

    private List<FLUser> recentFriendList;
    private List<FLUser> friendList;
    private List<FLUser> phoneContactList;

    private List<FLUser> searchPhoneContacts;
    private List<FLUser> searchFloozUsers;
    private List<FLUser> searchFloozFriends;

    public SelectUserListAdapter(Context context2) {
        this.inflater = LayoutInflater.from(context2);
        this.context = context2;

        this.searchPhoneContacts = new ArrayList<>(0);
        this.searchFloozUsers = new ArrayList<>(0);
        this.searchFloozFriends = new ArrayList<>(0);

        class DownloadContactsTask extends AsyncTask<URL, Integer, Long> {
            protected void onPreExecute() {
                phoneContactList = new ArrayList<>(0);
            }

            protected Long doInBackground(URL... urls) {
                phoneContactList = ContactsManager.getContactsList();
                return null;
            }

            protected void onPostExecute(Long result) {
                notifyDataSetChanged();
            }
        }

        new DownloadContactsTask().execute();

        this.recentFriendList = FloozRestClient.getInstance().currentUser.friendsRecent;
        this.friendList = FloozRestClient.getInstance().currentUser.friends;
    }

    @Override
    public int getCount() {
        if (!this.isSearchActive)
            return this.recentFriendList.size() + this.friendList.size() + this.phoneContactList.size();
        return this.searchFloozFriends.size() + this.searchFloozUsers.size() + this.searchPhoneContacts.size();
    }

    @Override
    public FLUser getItem(int position) {
        if (!this.isSearchActive) {
            if (position < this.recentFriendList.size())
                return this.recentFriendList.get(position);
            else if (position >= this.recentFriendList.size() && position < this.recentFriendList.size() + this.friendList.size())
                return this.friendList.get(position - this.recentFriendList.size());
            else
                return this.phoneContactList.get(position - this.recentFriendList.size() - this.friendList.size());
        }
        else {
            if (position < this.searchFloozFriends.size())
                return this.searchFloozFriends.get(position);
            else if (position >= this.searchFloozFriends.size() && position < this.searchFloozUsers.size() + this.searchFloozFriends.size())
                return this.searchFloozUsers.get(position - this.searchFloozFriends.size());
            else
                return this.searchPhoneContacts.get(position - this.searchFloozUsers.size() - this.searchFloozFriends.size());
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            holder = new ViewHolder();
            convertView = inflater.inflate(R.layout.user_list_row, parent, false);
            holder.username = (TextView) convertView.findViewById(R.id.user_list_row_username);
            holder.fullname = (TextView) convertView.findViewById(R.id.user_list_row_fullname);
            holder.pic = (RoundedImageView) convertView.findViewById(R.id.user_list_row_pic);

            holder.username.setTypeface(CustomFonts.customTitleExtraLight(this.context), Typeface.BOLD);
            holder.fullname.setTypeface(CustomFonts.customTitleExtraLight(this.context), Typeface.BOLD);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        FLUser user = this.getItem(position);

        holder.fullname.setText(user.fullname);
        if (user.userKind == FLUser.UserKind.PhoneUser)
            holder.username.setText(user.phone);
        else
            holder.username.setText("@" + user.username);

        holder.pic.setImageDrawable(this.context.getResources().getDrawable(R.drawable.avatar_default));
        if (user.avatarURL != null && !user.avatarURL.isEmpty()) {
            if (user.userKind == FLUser.UserKind.FloozUser)
                ImageLoader.getInstance().displayImage(user.avatarURL, holder.pic);
            else
                holder.pic.setImageURI(Uri.parse(user.avatarURL));
        }

        return convertView;
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
            if (position < this.recentFriendList.size())
                holder.text.setText(this.context.getResources().getString(R.string.FRIEND_PICKER_FRIENDS_RECENT));
            else if (position >= this.recentFriendList.size() && position < this.recentFriendList.size() + this.friendList.size())
                holder.text.setText(this.context.getResources().getString(R.string.FRIEND_PICKER_FRIENDS));
            else
                holder.text.setText(this.context.getResources().getString(R.string.FRIEND_PICKER_ADDRESS_BOOK));
        }
        else {
            if (position < this.searchFloozFriends.size())
                holder.text.setText(this.context.getResources().getString(R.string.FRIEND_PICKER_FRIENDS));
            else if (position >= this.searchFloozFriends.size() && position < this.searchFloozFriends.size() + this.searchFloozUsers.size())
                holder.text.setText(this.context.getResources().getString(R.string.FRIEND_PCIKER_SELECTION_CELL));
            else
                holder.text.setText(this.context.getResources().getString(R.string.FRIEND_PICKER_ADDRESS_BOOK));
        }

        return convertView;
    }

    @Override
    public long getHeaderId(int position) {
        if (!this.isSearchActive) {
            if (position < this.recentFriendList.size())
                return FLUser.FLUserSelectedCanal.RecentCanal.ordinal();
            else if (position >= this.recentFriendList.size() && position < this.recentFriendList.size() + this.friendList.size())
                return FLUser.FLUserSelectedCanal.FriendsCanal.ordinal();
            else
                return FLUser.FLUserSelectedCanal.ContactCanal.ordinal();
        }
        else {
            if (position < this.searchFloozFriends.size())
                return FLUser.FLUserSelectedCanal.FriendsCanal.ordinal();
            else if (position >= this.searchFloozFriends.size() && position < this.searchFloozFriends.size() + this.searchFloozUsers.size())
                return FLUser.FLUserSelectedCanal.SearchCanal.ordinal();
            else
                return FLUser.FLUserSelectedCanal.ContactCanal.ordinal();
        }
    }

    public void searchUser(String searchString) {

        if (searchString != null && searchString.length() > 0) {
            this.isSearchActive = true;

            this.searchFloozFriends.clear();
            for (int i = 0; i < this.friendList.size(); i++) {
                FLUser user = this.friendList.get(i);

                if (user.fullname.toLowerCase().contains(searchString.toLowerCase()) || user.username.toLowerCase().contains(searchString.toLowerCase()))
                    this.searchFloozFriends.add(user);
            }

            this.searchPhoneContacts.clear();
            for (int i = 0; i < this.phoneContactList.size(); i++) {
                FLUser user = this.phoneContactList.get(i);

                if (user.fullname.toLowerCase().contains(searchString.toLowerCase()) || user.username.toLowerCase().contains(searchString.toLowerCase()))
                    this.searchPhoneContacts.add(user);
            }

            this.searchFloozUsers.clear();

            this.notifyDataSetChanged();

            FloozRestClient.getInstance().searchUser(searchString, true, new FloozHttpResponseHandler() {
                @Override
                public void success(Object response) {
                    searchFloozUsers = new ArrayList<>((List<FLUser>)response);
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

        this.isSearchActive = false;
        this.notifyDataSetChanged();
    }

    class HeaderViewHolder {
        TextView text;
    }

    class ViewHolder {
        TextView fullname;
        TextView username;
        RoundedImageView pic;
    }
}
