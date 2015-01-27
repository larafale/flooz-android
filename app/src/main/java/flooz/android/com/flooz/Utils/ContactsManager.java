package flooz.android.com.flooz.Utils;

import android.content.ContentResolver;
import android.database.Cursor;
import android.provider.ContactsContract;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;

import java.util.ArrayList;
import java.util.List;

import flooz.android.com.flooz.App.FloozApplication;
import flooz.android.com.flooz.Model.FLUser;

/**
 * Created by Flooz on 10/2/14.
 */
public class ContactsManager {

    public static List<FLUser> searchContacts(String searchString) {

        List<FLUser> resultList = new ArrayList<>(0);

        ContentResolver contentResolver = FloozApplication.getAppContext().getContentResolver();
        Cursor contactCursor = contentResolver.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, ContactsContract.Contacts.DISPLAY_NAME + " ASC");
        if (contactCursor.getCount() > 0) {
            while (contactCursor.moveToNext()) {
                if (Integer.parseInt(contactCursor.getString(contactCursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
                    Cursor phoneCursor = contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?", new String[] { contactCursor.getString(contactCursor.getColumnIndex(ContactsContract.Contacts._ID)) }, null);

                    while (phoneCursor.moveToNext()) {
                        FLUser user = new FLUser();

                        user.userId = contactCursor.getString(contactCursor.getColumnIndex(ContactsContract.Contacts._ID));
                        user.fullname = contactCursor.getString(contactCursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                        user.avatarURL = contactCursor.getString(contactCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_URI));

                        String phoneNumber = phoneCursor.getString(phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

                        user.username = ContactsManager.validatePhoneNumber(phoneNumber);
                        user.phone = user.username;

                        if (user.username != null && !user.username.isEmpty() && user.fullname.toLowerCase().contains(searchString.toLowerCase())) {
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
                    phoneCursor.close();
                }
            }
        }
        contactCursor.close();
        return resultList;
    }

    private static String validatePhoneNumber(String number) {
        String nb = new String(number);

        PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
        try {
            Phonenumber.PhoneNumber numberProto = phoneUtil.parse(nb, "FR");

            if (phoneUtil.isValidNumberForRegion(numberProto, "FR") && phoneUtil.getNumberType(numberProto) == PhoneNumberUtil.PhoneNumberType.MOBILE)
                return phoneUtil.format(numberProto, PhoneNumberUtil.PhoneNumberFormat.E164);
            else
                return null;

        } catch (NumberParseException e) {
            System.err.println("NumberParseException was thrown: " + e.toString());
            return null;
        }
    }

    public static List<FLUser> getContactsList() {
        return ContactsManager.searchContacts("");
    }

}
