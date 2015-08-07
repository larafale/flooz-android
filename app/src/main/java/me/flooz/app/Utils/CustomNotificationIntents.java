package me.flooz.app.Utils;

import android.content.Intent;
import android.content.IntentFilter;

/**
 * Created by Flooz on 9/8/14.
 */
public class CustomNotificationIntents
{
    public static Intent connectFacebook() {return new Intent("NotificationFacebookConnect");}
    public static IntentFilter filterFacebookConnect() {return new IntentFilter("NotificationFacebookConnect");}

    public static Intent reloadCurrentUser() {return new Intent("NotificationReloadCurrentUser");}
    public static IntentFilter filterReloadCurrentUser() {return new IntentFilter("NotificationReloadCurrentUser");}

    public static Intent reloadTimeline() {return new Intent("NotificationReloadTimeline");}
    public static IntentFilter filterReloadTimeline() {return new IntentFilter("NotificationReloadTimeline");}

    public static Intent showSlidingLeftMenu() {return new Intent("NotificationShowLeftMenu");}
    public static IntentFilter filterShowSlidingLeftMenu() {return new IntentFilter("NotificationShowLeftMenu");}

    public static Intent showSlidingRightMenu() {return new Intent("NotificationShowRightMenu");}
    public static IntentFilter filterShowSlidingRightMenu() {return new IntentFilter("NotificationShowRightMenu");}

    public static Intent disableSlidingMenu() {return new Intent("NotificationDisableMenu");}
    public static IntentFilter filterDisableSlidingMenu() {return new IntentFilter("NotificationDisableMenu");}

    public static Intent enableSlidingMenu() {return new Intent("NotificationEnableMenu");}
    public static IntentFilter filterEnableSlidingMenu() {return new IntentFilter("NotificationEnableMenu");}

    public static Intent reloadNotifications() {return new Intent("NotificationReloadNotifications");}
    public static IntentFilter filterReloadNotifications() {return new IntentFilter("NotificationReloadNotifications");}

    public static Intent reloadFriends() {return new Intent("NotificationReloadFriends");}
    public static IntentFilter filterReloadFriends() {return new IntentFilter("NotificationReloadFriends");}
}
