package me.flooz.app.Utils;

import android.content.Context;
import android.graphics.drawable.Drawable;

/**
 * Created by Flooz on 9/23/14.
 */
public class MenuItem {

    public String name;
    public Drawable image;
    public int  nbNotification = 0;

    public MenuItem(Context ctx, int idString, int idDrawable) {
        this.name = ctx.getResources().getString(idString);
        this.image = ctx.getResources().getDrawable(idDrawable);
    }

    public MenuItem(Context ctx, int idString, int idDrawable, int notifs) {
        this.name = ctx.getResources().getString(idString);
        this.image = ctx.getResources().getDrawable(idDrawable);
        this.nbNotification = notifs;
    }
}
