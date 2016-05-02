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

    public static Intent reloadNotifications() {return new Intent("NotificationReloadNotifications");}
    public static IntentFilter filterReloadNotifications() {return new IntentFilter("NotificationReloadNotifications");}

    public static Intent reloadFriends() {return new Intent("NotificationReloadFriends");}
    public static IntentFilter filterReloadFriends() {return new IntentFilter("NotificationReloadFriends");}

    public static Intent reloadText() {return new Intent("NotificationReloadText");}
    public static IntentFilter filterReloadText() {return new IntentFilter("NotificationReloadText");}

    public static Intent reloadInvitation() {return new Intent("NotificationReloadInvitation");}
    public static IntentFilter filterReloadInvitation() {return new IntentFilter("NotificationReloadInvitation");}

    public static Intent reloadCollect() {return new Intent("NotificationReloadCollect");}
    public static IntentFilter filterReloadCollect() {return new IntentFilter("NotificationReloadCollect");}

    public static Intent reloadTransaction() {return new Intent("NotificationReloadTransaction");}
    public static IntentFilter filterReloadTransaction() {return new IntentFilter("NotificationReloadTransaction");}
}
