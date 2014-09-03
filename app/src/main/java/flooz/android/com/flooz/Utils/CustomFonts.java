package flooz.android.com.flooz.Utils;

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
        Typeface tf = Typeface.createFromAsset(context.getAssets(), fontPath);
        return tf;
    }

    public static Typeface customTitleLight(Context context)
    {
        String fontPath = "fonts/Rene Bieder - Gentona Light.otf";
        Typeface tf = Typeface.createFromAsset(context.getAssets(), fontPath);
        return tf;
    }

    public static Typeface customTitleExtraLight(Context context)
    {
        String fontPath = "fonts/Rene Bieder - Gentona ExtraLight.otf";
        Typeface tf = Typeface.createFromAsset(context.getAssets(), fontPath);
        return tf;
    }

    public static Typeface customTitleThin(Context context)
    {
        String fontPath = "fonts/Rene Bieder - Gentona Thin.otf";
        Typeface tf = Typeface.createFromAsset(context.getAssets(), fontPath);
        return tf;
    }

    public static Typeface customContentRegular(Context context)
    {
        String fontPath = "fonts/ProximaNova-Regular.otf";
        Typeface tf = Typeface.createFromAsset(context.getAssets(), fontPath);
        return tf;
    }

    public static Typeface customContentLight(Context context)
    {
        String fontPath = "fonts/ProximaNova-Light.otf";
        Typeface tf = Typeface.createFromAsset(context.getAssets(), fontPath);
        return tf;
    }

    public static Typeface customContentBold(Context context)
    {
        String fontPath = "fonts/ProximaNova-Semibold.otf";
        Typeface tf = Typeface.createFromAsset(context.getAssets(), fontPath);
        return tf;
    }
}
