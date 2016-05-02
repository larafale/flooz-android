package me.flooz.app.Utils;

import android.content.Context;
import android.content.res.Resources;
import android.util.DisplayMetrics;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;

import java.util.Map;
import java.util.NavigableMap;
import java.util.Random;
import java.util.TreeMap;

import me.flooz.app.App.FloozApplication;
import me.flooz.app.BuildConfig;
import me.flooz.app.Network.FloozRestClient;

/**
 * Created by Flooz on 1/8/15.
 */
public class FLHelper {


    public static int getResourceId(String pVariableName, String pResourcename, String pPackageName)
    {
        try {
            return FloozApplication.getAppContext().getResources().getIdentifier(pVariableName, pResourcename, pPackageName);
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    /**
     * This method converts dp unit to equivalent pixels, depending on device density.
     *
     * @param dp A value in dp (density independent pixels) unit. Which we need to convert into pixels
     * @param context Context to get resources and device specific display metrics
     * @return A float value to represent px equivalent to dp depending on device density
     */
    public static float convertDpToPixel(float dp, Context context){
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float px = dp * (metrics.densityDpi / 160f);
        return px;
    }

    /**
     * This method converts device specific pixels to density independent pixels.
     *
     * @param px A value in px (pixels) unit. Which we need to convert into db
     * @param context Context to get resources and device specific display metrics
     * @return A float value to represent dp equivalent to px value
     */
    public static float convertPixelsToDp(float px, Context context){
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float dp = px / (metrics.densityDpi / 160f);
        return dp;
    }

    public static boolean isDebuggable()
    {
        return BuildConfig.DEBUG_API || BuildConfig.LOCAL_API;
    }

    public static String generateRandomString() {
        return generateRandomString(16);
    }

    public static String generateRandomString(int length) {
        String letters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        String randomString = "";
        Random r = new Random();

        for (int i = 0; i < length; i++) {
            randomString += letters.charAt(r.nextInt(letters.length()));
        }

        return randomString;
    }

    public static String trimTrailingZeros(String number) {
        if(!number.contains(".")) {
            return number;
        }

        return number.replaceAll("\\.?0*$", "");
    }

    public static int numLength(int n)
    {
        int l;
        n = Math.abs(n);
        for (l = 0; n > 0; ++l)
            n /= 10;
        return l;
    }

    private static final NavigableMap<Long, String> suffixes = new TreeMap<>();
    static {
        suffixes.put(1_000L, "K");
        suffixes.put(1_000_000L, "M");
        suffixes.put(1_000_000_000L, "G");
        suffixes.put(1_000_000_000_000L, "T");
        suffixes.put(1_000_000_000_000_000L, "P");
        suffixes.put(1_000_000_000_000_000_000L, "E");
    }

    public static String formatUserNumber(long value) {
        //Long.MIN_VALUE == -Long.MIN_VALUE so we need an adjustment here
        if (value == Long.MIN_VALUE) return formatUserNumber(Long.MIN_VALUE + 1);
        if (value < 0) return "-" + formatUserNumber(-value);
        if (value < 10) return "0" + Long.toString(value);
        if (value < 1000) return Long.toString(value); //deal with easy case

        Map.Entry<Long, String> e = suffixes.floorEntry(value);
        Long divideBy = e.getKey();
        String suffix = e.getValue();

        long truncated = value / (divideBy / 10); //the number part of the output times 10
        boolean hasDecimal = truncated < 100 && (truncated / 10d) != (truncated / 10);
        return hasDecimal ? (truncated / 10d) + suffix : (truncated / 10) + suffix;
    }

    public static String formatedPhone(String phone) {
        String formatedPhone = phone.replace("", "").replace(".", "").replace(",", "").replace("-", "").replace(")", "").replace("(", "");

        PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();

        try {
            Phonenumber.PhoneNumber phoneNumber = phoneUtil.parse(formatedPhone, FloozRestClient.getInstance().currentUser.country.code);

            formatedPhone = phoneUtil.format(phoneNumber, PhoneNumberUtil.PhoneNumberFormat.E164);
            return formatedPhone;
        } catch (NumberParseException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static Boolean isValidPhoneNumber(String phone) {
        String formatedPhone = phone.replace("", "").replace(".", "").replace(",", "").replace("-", "").replace(")", "").replace("(", "");

        PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();

        try {
            Phonenumber.PhoneNumber phoneNumber = phoneUtil.parse(formatedPhone, FloozRestClient.getInstance().currentUser.country.code);

            return phoneUtil.isValidNumber(phoneNumber) && phoneUtil.getNumberType(phoneNumber) == PhoneNumberUtil.PhoneNumberType.MOBILE;
        } catch (NumberParseException e) {
            e.printStackTrace();
        }

        return false;
    }

    public static String fullPhone(String phone, String country) {
        String formatedPhone = phone.replace("", "").replace(".", "").replace(",", "").replace("-", "").replace(")", "").replace("(", "");

        PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();

        try {
            Phonenumber.PhoneNumber phoneNumber = phoneUtil.parse(formatedPhone, country);

            formatedPhone = phoneUtil.format(phoneNumber, PhoneNumberUtil.PhoneNumberFormat.E164);
            return formatedPhone;
        } catch (NumberParseException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static Boolean phoneMatch(String phone1, String phone2) {
        String formatedPhone1 = phone1.replace("", "").replace(".", "").replace(",", "").replace("-", "").replace(")", "").replace("(", "");
        String formatedPhone2 = phone2.replace("", "").replace(".", "").replace(",", "").replace("-", "").replace(")", "").replace("(", "");

        PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();

        try {
            Phonenumber.PhoneNumber phoneNumber1 = phoneUtil.parse(formatedPhone1, FloozRestClient.getInstance().currentUser.country.code);
            Phonenumber.PhoneNumber phoneNumber2 = phoneUtil.parse(formatedPhone2, FloozRestClient.getInstance().currentUser.country.code);

            formatedPhone1 = phoneUtil.format(phoneNumber1, PhoneNumberUtil.PhoneNumberFormat.E164);
            formatedPhone2 = phoneUtil.format(phoneNumber2, PhoneNumberUtil.PhoneNumberFormat.E164);

            return formatedPhone1.contains(formatedPhone2);
        } catch (NumberParseException e) {
            e.printStackTrace();
        }

        return false;
    }
}
