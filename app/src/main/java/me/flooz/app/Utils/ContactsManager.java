package me.flooz.app.Utils;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds;
import android.provider.ContactsContract.Contacts;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;

import java.util.ArrayList;
import java.util.List;

import me.flooz.app.App.FloozApplication;
import me.flooz.app.Model.FLUser;
import me.flooz.app.Network.FloozRestClient;

/**
 * Created by Flooz on 10/2/14.
 */
public class ContactsManager {

    public static List<FLUser> allContacts;

    public static List<FLUser> searchContacts(String searchString, int limit) {

        List<FLUser> resultList = new ArrayList<>(0);

        final String sa1 = "%" + searchString.toLowerCase() + "%"; // contains searchString

        String[] projection = new String[]{
                Contacts._ID,
                Contacts.PHOTO_URI,
                Contacts.DISPLAY_NAME,
                Contacts.IN_VISIBLE_GROUP,
                CommonDataKinds.Phone.NUMBER,
                CommonDataKinds.Phone.TYPE,
                CommonDataKinds.StructuredName.GIVEN_NAME,
                CommonDataKinds.StructuredName.FAMILY_NAME};

        String selection = Contacts.HAS_PHONE_NUMBER + "=1 AND " + Contacts.IN_VISIBLE_GROUP + "=1 AND (lower(" + Contacts.DISPLAY_NAME + ") LIKE ? OR replace(replace(replace(replace(" + CommonDataKinds.Phone.NUMBER + ", ' ', ''), '+33', '0'), '(', ''), ')', '') LIKE ?)";

        String[] selectionArgs = new String[]{sa1, sa1};

        Uri contentURI = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;

        String order = Contacts.DISPLAY_NAME + " ASC LIMIT " + limit;

        ContentResolver contentResolver = FloozApplication.getAppContext().getContentResolver();
        Cursor contactCursor = contentResolver.query(contentURI, projection, selection, selectionArgs, order);

        if (contactCursor != null && contactCursor.getCount() > 0) {
            while (contactCursor.moveToNext()) {
                FLUser user = new FLUser();

                user.userId = contactCursor.getString(contactCursor.getColumnIndex(ContactsContract.Contacts._ID));
                user.fullname = contactCursor.getString(contactCursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                user.avatarURL = contactCursor.getString(contactCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_URI));
                String phoneNumber = contactCursor.getString(contactCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

                user.username = ContactsManager.validatePhoneNumber(phoneNumber);
                user.phone = user.username;

                if (user.username != null && !user.username.isEmpty()) {
                    boolean alreadyExist = false;
                    for (int i = 0; i < resultList.size(); i++) {
                        FLUser tmpUser = resultList.get(i);
                        if (tmpUser.fullname.contentEquals(user.fullname) && tmpUser.phone.contentEquals(user.phone)) {
                            alreadyExist = true;
                            break;
                        }
                    }
                    if (!alreadyExist)
                        resultList.add(user);
                }

            }
        }

        assert contactCursor != null;
        contactCursor.close();
        return resultList;
    }

    public static List<FLUser> getAllContacts() {

        List<FLUser> resultList = new ArrayList<>(0);

        String[] projection = new String[]{
                Contacts._ID,
                Contacts.PHOTO_URI,
                Contacts.DISPLAY_NAME,
                Contacts.IN_VISIBLE_GROUP,
                CommonDataKinds.Phone.NUMBER,
                CommonDataKinds.Phone.TYPE};

        String selection = Contacts.HAS_PHONE_NUMBER + "=1 AND " + Contacts.IN_VISIBLE_GROUP + "=1";

        String[] selectionArgs = new String[]{};

        Uri contentURI = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;

        String order = Contacts.DISPLAY_NAME + " ASC";

        ContentResolver contentResolver = FloozApplication.getAppContext().getContentResolver();
        Cursor contactCursor = contentResolver.query(contentURI, projection, selection, selectionArgs, order);

        if (contactCursor != null && contactCursor.getCount() > 0) {
            while (contactCursor.moveToNext()) {
                FLUser user = new FLUser();

                user.userId = contactCursor.getString(contactCursor.getColumnIndex(ContactsContract.Contacts._ID));
                user.fullname = contactCursor.getString(contactCursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                user.avatarURL = contactCursor.getString(contactCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_URI));
                String phoneNumber = contactCursor.getString(contactCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

                user.username = ContactsManager.validatePhoneNumber(phoneNumber);
                user.phone = user.username;

                if (user.fullname != null && !user.fullname.isEmpty() && user.username != null && !user.username.isEmpty()) {
                    boolean alreadyExist = false;
                    for (int i = 0; i < resultList.size(); i++) {
                        FLUser tmpUser = resultList.get(i);
                        if (tmpUser.fullname.contentEquals(user.fullname) && tmpUser.username.contentEquals(user.username)) {
                            alreadyExist = true;
                            break;
                        }
                    }
                    if (!alreadyExist)
                        resultList.add(user);
                }

            }
        }

        assert contactCursor != null;
        contactCursor.close();
        return resultList;
    }

    private static String validatePhoneNumber(String number) {

        PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
        try {
            Phonenumber.PhoneNumber numberProto = phoneUtil.parse(number, FloozRestClient.getInstance().currentUser.country.code);

            if (phoneUtil.isValidNumber(numberProto) && phoneUtil.getNumberType(numberProto) == PhoneNumberUtil.PhoneNumberType.MOBILE)
                return phoneUtil.format(numberProto, PhoneNumberUtil.PhoneNumberFormat.E164);
            else
                return null;

        } catch (NumberParseException e) {
            System.err.println("NumberParseException was thrown: " + e.toString());
            return null;
        }
    }

    public static FLUser fillUserInformations(FLUser user) {

        String whereName = ContactsContract.Data.MIMETYPE + " = ? AND " + CommonDataKinds.StructuredName.DISPLAY_NAME + " = ?";
        String[] whereNameParams = new String[] { ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE, user.fullname };

        ContentResolver contentResolver = FloozApplication.getAppContext().getContentResolver();
        Cursor nameCur = contentResolver.query(ContactsContract.Data.CONTENT_URI, null, whereName, whereNameParams, ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME);

        while (nameCur.moveToNext()) {
            user.firstname = nameCur.getString(nameCur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME));
            user.lastname = nameCur.getString(nameCur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME));
            if (user.firstname != null && !user.firstname.isEmpty() && user.lastname != null && !user.lastname.isEmpty())
                break;
        }

        nameCur.close();

        return user;
    }

    public static List<FLUser> getContactsList() {
        if (allContacts != null)
            return allContacts;

        allContacts = ContactsManager.getAllContacts();

        return allContacts;
    }
}
