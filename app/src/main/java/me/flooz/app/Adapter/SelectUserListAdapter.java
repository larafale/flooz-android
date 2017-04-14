package me.flooz.app.Adapter;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.ActivityCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.makeramen.roundedimageview.RoundedImageView;
import com.nostra13.universalimageloader.core.ImageLoader;

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
import me.flooz.app.Utils.FLHelper;
import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;

/**
 * Created by Flooz on 10/2/14.
 */
public class SelectUserListAdapter extends BaseAdapter implements StickyListHeadersAdapter {

    private Context context;
    private LayoutInflater inflater;

    private List<FLUser> contactsFromAddressBook;
    private List<FLUser> contactsFiltered;

    private List<FLUser> friends;
    private List<FLUser> friendsRecent;

    private List<FLUser> filteredContacts;

    private Handler searchHandler;

    private String searchData = "";

    private Runnable searchRunnable = new Runnable() {
        @Override
        public void run() {
            if (!searchData.isEmpty()) {
                final List<FLUser> searchContacts = ContactsManager.searchContacts(searchData, 20);
                contactsFiltered = new ArrayList<>();

                for (FLUser contact : searchContacts) {
                    if (contact.fullname != null && contact.fullname.toLowerCase().indexOf(searchData.toLowerCase()) == 0)
                        contactsFiltered.add(contact);
                    else if (contact.firstname != null && contact.firstname.toLowerCase().indexOf(searchData.toLowerCase()) == 0)
                        contactsFiltered.add(contact);
                    else if (contact.lastname != null && contact.lastname.toLowerCase().indexOf(searchData.toLowerCase()) == 0)
                        contactsFiltered.add(contact);
                    else if (contact.phone != null && FLHelper.phoneMatch(contact.phone, searchData))
                        contactsFiltered.add(contact);
                }

                FloozRestClient.getInstance().searchUser(searchData, true, new FloozHttpResponseHandler() {
                    @Override
                    public void success(Object response) {
                        @SuppressWarnings("unchecked")
                        List<FLUser> searchList = (List<FLUser>) response;

                        filteredContacts = new ArrayList<>();
                        List<FLUser> clearSearchList = new ArrayList<>();
                        List<FLUser> clearContactList = new ArrayList<>();
                        List<FLUser> commonUsers = new ArrayList<>();

                        for (FLUser floozer : searchList) {
                            for (FLUser contact : contactsFiltered) {
                                Boolean common = false;
                                if (contact.phone.contentEquals(floozer.phone))
                                    common = true;

                                if (common && !commonUsers.contains(floozer))
                                    commonUsers.add(floozer);

                                if (common && !clearSearchList.contains(floozer))
                                    clearSearchList.add(contact);

                                if (common && !clearContactList.contains(floozer))
                                    clearContactList.add(contact);
                            }
                        }

                        contactsFiltered.removeAll(clearContactList);
                        searchList.removeAll(clearSearchList);

                        filteredContacts.addAll(commonUsers);
                        filteredContacts.addAll(contactsFiltered);
                        filteredContacts.addAll(searchList);

                        notifyDataSetChanged();
                    }

                    @Override
                    public void failure(int statusCode, FLError error) {

                    }
                });
            }
        }
    };

    public SelectUserListAdapter(Context context2) {
        this.inflater = LayoutInflater.from(context2);
        this.context = context2;
        this.searchHandler = new Handler(Looper.getMainLooper());

        this.friends = FloozRestClient.getInstance().currentUser.friends;
        this.friendsRecent = FloozRestClient.getInstance().currentUser.friendsRecent;

        this.loadContacts();

        this.filteredContacts = new ArrayList<>();

        this.filteredContacts.addAll(this.friendsRecent);
        this.filteredContacts.addAll(this.friends);
        this.filteredContacts.addAll(this.contactsFromAddressBook);
    }

    public void loadContacts() {
        if (ActivityCompat.checkSelfPermission(FloozApplication.getAppContext(), Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {
            this.contactsFromAddressBook = new ArrayList<>();
        } else {
            this.contactsFromAddressBook = ContactsManager.getContactsList();

            this.searchHandler.removeCallbacks(searchRunnable);
            if (this.searchData.length() == 0) {
                this.filteredContacts = new ArrayList<>();

                if (this.friendsRecent.size() == 0 && this.contactsFromAddressBook.size() == 0)
                    this.filteredContacts.addAll(this.friends);
                else {
                    this.filteredContacts.addAll(this.friendsRecent);
                    this.filteredContacts.addAll(this.contactsFromAddressBook);
                }
                this.notifyDataSetChanged();
            } else {
                this.searchHandler.post(searchRunnable);
            }
        }
    }

    @Override
    public int getCount() {
        return this.filteredContacts.size() > 0 ? this.filteredContacts.size() : 1;
    }

    @Override
    public FLUser getItem(int position) {
        if (this.filteredContacts.size() > 0)
            return this.filteredContacts.get(position);
        else
            return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (this.filteredContacts.size() > 0) {
            ViewHolder holder;

            if (convertView == null || !(convertView.getTag() instanceof ViewHolder)) {
                holder = new ViewHolder();
                convertView = inflater.inflate(R.layout.user_list_row, parent, false);
                holder.username = (TextView) convertView.findViewById(R.id.user_list_row_username);
                holder.fullname = (TextView) convertView.findViewById(R.id.user_list_row_fullname);
                holder.pic = (RoundedImageView) convertView.findViewById(R.id.user_list_row_pic);

                holder.username.setTypeface(CustomFonts.customTitleExtraLight(this.context), Typeface.BOLD);
                holder.fullname.setTypeface(CustomFonts.customContentRegular(this.context));

                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            FLUser user = this.getItem(position);

            holder.fullname.setText(user.fullname);
            if (user.userKind == FLUser.UserKind.PhoneUser || user.userKind == FLUser.UserKind.CactusUser)
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
        } else {
            convertView = inflater.inflate(R.layout.user_picker_row_empty, parent, false);
            ((TextView) convertView.findViewById(R.id.contact_list_row_empty_text_1)).setTypeface(CustomFonts.customContentRegular(this.context));
            ((TextView) convertView.findViewById(R.id.contact_list_row_empty_text_2)).setTypeface(CustomFonts.customContentRegular(this.context));
        }
        return convertView;
    }

    public void searchUser(String searchString) {
        this.searchHandler.removeCallbacks(searchRunnable);
        this.searchData = searchString;
        if (searchString.length() < 3) {
            this.filteredContacts = new ArrayList<>();

            this.filteredContacts.addAll(this.friendsRecent);
            this.filteredContacts.addAll(this.friends);
            this.filteredContacts.addAll(this.contactsFromAddressBook);

            this.notifyDataSetChanged();
        } else {
            this.searchHandler.postDelayed(searchRunnable, 500);
        }
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

        if (this.searchData.isEmpty()) {
            if (position < this.friendsRecent.size())
                holder.text.setText(this.context.getResources().getString(R.string.FRIEND_PICKER_FRIENDS_RECENT));
            else if (position < this.friendsRecent.size() + this.friends.size())
                holder.text.setText(this.context.getResources().getString(R.string.FRIEND_PICKER_FRIENDS));
            else
                holder.text.setText(this.context.getResources().getString(R.string.FRIEND_PICKER_ADDRESS_BOOK));
        }
        else
            holder.text.setText(this.context.getResources().getString(R.string.FRIEND_PICKER_RESULT));

        return convertView;
    }

    @Override
    public long getHeaderId(int i) {
        if (this.searchData.isEmpty()) {
            if (this.friendsRecent.size() == 0 && this.contactsFromAddressBook.size() == 0 && this.friends.size() == 0) {
                return 0;
            } else {
                if (i < this.friendsRecent.size())
                    return 1;
                else if (i < this.friendsRecent.size() + this.friends.size())
                    return 2;
                else
                    return 3;
            }
        }
        else
            return 4;
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
