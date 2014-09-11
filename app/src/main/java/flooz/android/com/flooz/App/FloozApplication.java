package flooz.android.com.flooz.App;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import flooz.android.com.flooz.Network.FloozRestClient;

/**
 * Created by Flooz on 9/8/14.
 */
public class FloozApplication extends Application
{
    private static Context context;

    public void onCreate(){
        super.onCreate();
        FloozApplication.context = getApplicationContext();


        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(context).build();
        ImageLoader.getInstance().init(config);

        FloozRestClient client = FloozRestClient.getInstance();
        client.loginQuick();
    }

    public static Context getAppContext() {
        return FloozApplication.context;
    }

    public static void performLocalNotification(Intent intent) {
        LocalBroadcastManager.getInstance(FloozApplication.context).sendBroadcast(intent);
    }
}
