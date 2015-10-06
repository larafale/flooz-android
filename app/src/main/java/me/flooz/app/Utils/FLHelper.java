package me.flooz.app.Utils;

import java.util.Random;

import me.flooz.app.BuildConfig;

/**
 * Created by Flooz on 1/8/15.
 */
public class FLHelper {

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
}
