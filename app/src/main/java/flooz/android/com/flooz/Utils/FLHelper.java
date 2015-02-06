package flooz.android.com.flooz.Utils;

import java.util.Random;

/**
 * Created by Flooz on 1/8/15.
 */
public class FLHelper {

    public static String generateRandomString() {
        return generateRandomString(16);
    }

    public static String generateRandomString(int lenght) {
        String letters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        String randomString = "";
        Random r = new Random();

        for (int i = 0; i < lenght; i++) {
            randomString += String.format("%C", letters.charAt(r.nextInt(letters.length())));
        }

        return randomString;
    }

    public static String trimTrailingZeros(String number) {
        if(!number.contains(".")) {
            return number;
        }

        return number.replaceAll("\\.?0*$", "");
    }
}
