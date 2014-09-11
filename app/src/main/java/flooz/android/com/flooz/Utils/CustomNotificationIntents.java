package flooz.android.com.flooz.Utils;

import android.content.Intent;
import android.content.IntentFilter;

/**
 * Created by Flooz on 9/8/14.
 */
public class CustomNotificationIntents
{
    public static Intent reloadCurrentUser() {return new Intent("NotificationReloadCurrentUser");}

    public static IntentFilter filterReloadCurrentUser() {return new IntentFilter("NotificationReloadCurrentUser");}

    public static Intent reloadTimeline() {return new Intent("NotificationReloadTimeline");}

    public static IntentFilter filterReloadTimeline() {return new IntentFilter("NotificationReloadTimeline");}
}
