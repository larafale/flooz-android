package me.flooz.app.Utils;

import android.content.Context;
import android.content.res.Resources;
import android.util.DisplayMetrics;

import java.util.Random;

import me.flooz.app.BuildConfig;

/**
 * Created by Flooz on 1/8/15.
 */
public class FLHelper {

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
}
