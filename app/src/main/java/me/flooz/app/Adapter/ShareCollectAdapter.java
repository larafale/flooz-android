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
import me.flooz.app.Utils.FLHelper;
import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;

/**
 * Created by Flooz on 09/05/16.
 */
public class ShareCollectAdapter extends BaseAdapter implements StickyListHeadersAdapter {
    private Context context;
    private LayoutInflater inflater;

    private List<FLUser> contactsFromAddressBook;
    private List<FLUser> contactsFiltered;

    private List<FLUser> friends;

    private List<FLUser> filteredContacts;

    private Handler searchHandler;

    private String searchData = "";

    public List<FLUser> selectedUsers;


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

    public ShareCollectAdapter(Context context2) {
        this.inflater = LayoutInflater.from(context2);
        this.context = context2;
        this.searchHandler = new Handler(Looper.getMainLooper());

        this.friends = FloozRestClient.getInstance().currentUser.friends;

        this.loadContacts();

        this.filteredContacts = new ArrayList<>();

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

                this.filteredContacts.addAll(this.friends);
                this.filteredContacts.addAll(this.contactsFromAddressBook);
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
                convertView = inflater.inflate(R.layout.share_collect_row, parent, false);
                holder.username = (TextView) convertView.findViewById(R.id.share_collect_row_username);
                holder.fullname = (TextView) convertView.findViewById(R.id.share_collect_row_fullname);
                holder.pic = (ImageView) convertView.findViewById(R.id.share_collect_row_pic);

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

            if (this.selectedUsers.contains(user)) {
                holder.pic.setImageDrawable(context.getResources().getDrawable(R.drawable.check_on));
            } else {
                holder.pic.setImageDrawable(context.getResources().getDrawable(R.drawable.check_off));
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
            if (position < this.friends.size())
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
            if (this.contactsFromAddressBook.size() == 0 && this.friends.size() == 0) {
                return 0;
            } else {
                if (i < this.friends.size())
                    return 1;
                else
                    return 2;
            }
        }
        else
            return 3;
    }

    class HeaderViewHolder {
        TextView text;
    }

    class ViewHolder {
        TextView fullname;
        TextView username;
        ImageView pic;
    }
}
