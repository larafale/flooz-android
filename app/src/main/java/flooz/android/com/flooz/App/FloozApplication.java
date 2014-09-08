package flooz.android.com.flooz.App;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

/**
 * Created by Flooz on 9/8/14.
 */
public class FloozApplication extends Application
{
    private static Context context;

    public void onCreate(){
        super.onCreate();
        FloozApplication.context = getApplicationContext();
    }

    public static Context getAppContext() {
        return FloozApplication.context;
    }

    public static void performLocalNotification(Intent intent) {
        LocalBroadcastManager.getInstance(FloozApplication.context).sendBroadcast(intent);
    }
}
