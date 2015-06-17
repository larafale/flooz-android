package me.flooz.app.Adapter;

import android.content.Context;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
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

/**
 * Created by Flooz on 10/2/14.
 */
public class SelectUserListAdapter extends BaseAdapter {

    private Context context;
    private LayoutInflater inflater;

    private List<FLUser> contactList;

    private List<FLUser> recentFriendList;
    private List<FLUser> phoneContactList;
    private List<FLUser> fullPhoneContactList;

    private Handler searchHandler;

    private String searchData = "";

    private Runnable searchRunnable = new Runnable() {
        @Override
        public void run() {
            if (!searchData.isEmpty()) {
                phoneContactList = ContactsManager.searchContacts(searchData, 20);
                final List<FLUser> contactsFiltered = new ArrayList<>();

                for (FLUser contact : phoneContactList) {
                    if (contact.fullname != null && contact.fullname.toLowerCase().indexOf(searchData.toLowerCase()) == 0)
                        contactsFiltered.add(contact);
                    else if (contact.firstname != null && contact.firstname.toLowerCase().indexOf(searchData.toLowerCase()) == 0)
                        contactsFiltered.add(contact);
                    else if (contact.lastname != null && contact.lastname.toLowerCase().indexOf(searchData.toLowerCase()) == 0)
                        contactsFiltered.add(contact);
                    else if (contact.phone != null) {
                        String clearPhone = contact.phone;

                        if (clearPhone.indexOf("+33") == 0)
                            clearPhone = clearPhone.replace("+33", "0");

                        if (clearPhone.toLowerCase().contains(searchData.toLowerCase()))
                            contactsFiltered.add(contact);
                        else if (contact.phone.toLowerCase().contains(searchData.toLowerCase()))
                            contactsFiltered.add(contact);
                    }
                }

                FloozRestClient.getInstance().searchUser(searchData, true, new FloozHttpResponseHandler() {
                    @Override
                    public void success(Object response) {
                        @SuppressWarnings("unchecked")
                        List<FLUser> searchList = (List<FLUser>)response;

                        contactList = new ArrayList<>();
                        List<FLUser> clearList = new ArrayList<>();

                        for (FLUser floozer : searchList) {
                            for (FLUser contact : contactsFiltered) {
                                if (contact.phone.contentEquals(floozer.phone)) {
                                    clearList.add(contact);
                                }
                            }
                            contactsFiltered.removeAll(clearList);
                            clearList.clear();
                        }

                        contactList.addAll(contactsFiltered);
                        contactList.addAll(searchList);

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

        this.recentFriendList = FloozRestClient.getInstance().currentUser.friendsRecent;
        this.contactList = this.recentFriendList;
        this.fullPhoneContactList = new ArrayList<>();
        this.fullPhoneContactList = ContactsManager.getAllContacts();

        if (this.contactList.isEmpty())
            this.contactList = this.fullPhoneContactList;
    }

    @Override
    public int getCount() {
        return this.contactList.size() > 0 ? this.contactList.size() : 1;
    }

    @Override
    public FLUser getItem(int position) {
        if (this.contactList.size() > 0)
            return this.contactList.get(position);
        else
            return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (this.contactList.size() > 0) {
            ViewHolder holder;

            if (convertView == null || !(convertView.getTag() instanceof ViewHolder)) {
                holder = new ViewHolder();
                convertView = inflater.inflate(R.layout.user_picker_row, parent, false);
                holder.username = (TextView) convertView.findViewById(R.id.user_list_row_username);
                holder.fullname = (TextView) convertView.findViewById(R.id.user_list_row_fullname);
                holder.pic = (RoundedImageView) convertView.findViewById(R.id.user_list_row_pic);

                holder.username.setTypeface(CustomFonts.customContentBold(this.context));
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
        if (searchString.length() == 0) {
            this.contactList = this.recentFriendList;
            if (this.contactList.isEmpty())
                this.contactList = this.fullPhoneContactList;
            this.notifyDataSetChanged();
        } else {
            this.searchHandler.postDelayed(searchRunnable, 500);
        }
    }

    class ViewHolder {
        TextView fullname;
        TextView username;
        RoundedImageView pic;
    }
}
