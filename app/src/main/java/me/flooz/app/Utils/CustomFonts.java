package me.flooz.app.Utils;

import android.graphics.Typeface;
import android.content.Context;

/**
 * Created by Flooz on 9/3/14.
 */
public class CustomFonts
{
    public static Typeface customTitleBook(Context context)
    {
        String fontPath = "fonts/Rene Bieder - Gentona Book.otf";
        return Typeface.createFromAsset(context.getAssets(), fontPath);
    }

    public static Typeface customTitleLight(Context context)
    {
        String fontPath = "fonts/Rene Bieder - Gentona Light.otf";
        return Typeface.createFromAsset(context.getAssets(), fontPath);
    }

    public static Typeface customTitleExtraLight(Context context)
    {
        String fontPath = "fonts/Rene Bieder - Gentona ExtraLight.otf";
        return Typeface.createFromAsset(context.getAssets(), fontPath);
    }

    public static Typeface customTitleThin(Context context)
    {
        String fontPath = "fonts/Rene Bieder - Gentona Thin.otf";
        return Typeface.createFromAsset(context.getAssets(), fontPath);
    }

    public static Typeface customContentRegular(Context context)
    {
        String fontPath = "fonts/ProximaNova-Regular.otf";
        return Typeface.createFromAsset(context.getAssets(), fontPath);
    }

    public static Typeface customContentLight(Context context)
    {
        String fontPath = "fonts/ProximaNova-Light.otf";
        return Typeface.createFromAsset(context.getAssets(), fontPath);
    }

    public static Typeface customContentBold(Context context)
    {
        String fontPath = "fonts/ProximaNova-Semibold.otf";
        return Typeface.createFromAsset(context.getAssets(), fontPath);
    }

    public static Typeface customCreditCard(Context context)
    {
        String fontPath = "fonts/ocra.otf";
        return Typeface.createFromAsset(context.getAssets(), fontPath);
    }
}
