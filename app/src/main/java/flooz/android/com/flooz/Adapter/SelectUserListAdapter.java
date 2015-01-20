package flooz.android.com.flooz.Adapter;

import android.content.Context;
import android.graphics.Typeface;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.makeramen.RoundedImageView;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
import java.util.List;

import flooz.android.com.flooz.Model.FLError;
import flooz.android.com.flooz.Model.FLUser;
import flooz.android.com.flooz.Network.FloozHttpResponseHandler;
import flooz.android.com.flooz.Network.FloozRestClient;
import flooz.android.com.flooz.R;
import flooz.android.com.flooz.Utils.ContactsManager;
import flooz.android.com.flooz.Utils.CustomFonts;
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

        this.searchPhoneContacts = new ArrayList<FLUser>(0);
        this.searchFloozUsers = new ArrayList<FLUser>(0);
        this.searchFloozFriends = new ArrayList<FLUser>(0);

        this.phoneContactList = ContactsManager.getContactsList();
        this.recentFriendList = FloozRestClient.getInstance().currentUser.friendsRecent;
        this.friendList = FloozRestClient.getInstance().currentUser.friends;
    }

    @Override
    public int getCount() {
        if (!this.isSearchActive)
            return this.recentFriendList.size() + this.friendList.size() + this.phoneContactList.size();
        return this.searchFloozUsers.size() + this.searchFloozFriends.size() + this.searchPhoneContacts.size();
    }

    @Override
    public Object getItem(int position) {
        if (!this.isSearchActive) {
            if (position < this.recentFriendList.size())
                return this.recentFriendList.get(position);
            else if (position >= this.recentFriendList.size() && position < this.recentFriendList.size() + this.friendList.size())
                return this.friendList.get(position - this.recentFriendList.size());
            else
                return this.phoneContactList.get(position - this.recentFriendList.size() - this.friendList.size());
        }
        else {
            if (position < this.searchFloozUsers.size())
                return this.searchFloozUsers.get(position);
            else if (position >= this.searchFloozUsers.size() && position < this.searchFloozUsers.size() + this.searchFloozFriends.size())
                return this.searchFloozFriends.get(position - this.searchFloozUsers.size());
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

            holder.username.setTypeface(CustomFonts.customTitleExtraLight(this.context));
            holder.fullname.setTypeface(CustomFonts.customTitleExtraLight(this.context), Typeface.BOLD);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        FLUser user = (FLUser)this.getItem(position);

        holder.fullname.setText(user.fullname);
        holder.username.setText("@" + user.username);

        if (user.avatarURL != null && !user.avatarURL.isEmpty()) {
            if (user.userKind == FLUser.UserKind.FloozUser)
                ImageLoader.getInstance().displayImage(user.avatarURL, holder.pic);
            else
                holder.pic.setImageURI(Uri.parse(user.avatarURL));
        }
        else
            holder.pic.setImageDrawable(this.context.getResources().getDrawable(R.drawable.avatar_default));

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
            if (position < this.searchFloozUsers.size())
                holder.text.setText(this.context.getResources().getString(R.string.FRIEND_PCIKER_SELECTION_CELL));
            else if (position >= this.searchFloozUsers.size() && position < this.searchFloozUsers.size() + this.searchFloozFriends.size())
                holder.text.setText(this.context.getResources().getString(R.string.FRIEND_PICKER_FRIENDS));
            else
                holder.text.setText(this.context.getResources().getString(R.string.FRIEND_PICKER_ADDRESS_BOOK));
        }

        return convertView;
    }

    @Override
    public long getHeaderId(int position) {
        if (!this.isSearchActive) {
            if (position < this.recentFriendList.size())
                return 0;
            else if (position >= this.recentFriendList.size() && position < this.recentFriendList.size() + this.friendList.size())
                return 1;
            else
                return 2;
        }
        else {
            if (position < this.searchFloozUsers.size())
                return 0;
            else if (position >= this.searchFloozUsers.size() && position < this.searchFloozUsers.size() + this.searchFloozFriends.size())
                return 1;
            else
                return 2;
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
