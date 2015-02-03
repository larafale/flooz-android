package flooz.android.com.flooz.App;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import org.json.JSONArray;
import org.json.JSONException;

import flooz.android.com.flooz.R;
import flooz.android.com.flooz.UI.Activity.HomeActivity;

/**
 * Created by Flooz on 1/14/15.
 */
public class GcmIntentService extends IntentService {

    public static final int NOTIFICATION_ID = 1;
    private NotificationManager notificationManager;

    public GcmIntentService() {
        super("GcmIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
        String messageType = gcm.getMessageType(intent);

        if (!extras.isEmpty()) {
            if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)) {
                sendNotification(extras);
            }
        }
        GcmBroadcastReceiver.completeWakefulIntent(intent);
    }

    private void sendNotification(Bundle extras) {
        this.notificationManager = (NotificationManager)this.getSystemService(Context.NOTIFICATION_SERVICE);

        String array = extras.getCharSequence("triggers").toString();

        Intent pendingIntentContent = new Intent(this, HomeActivity.class);
        pendingIntentContent.putExtra("triggers", array);

        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, pendingIntentContent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setContentTitle(extras.getCharSequence("title"))
                .setStyle(new NotificationCompat.BigTextStyle().bigText(extras.getCharSequence("text")))
                .setContentText(extras.getCharSequence("text"))
                .setSmallIcon(R.drawable.flooz_icon_white)
                .setLargeIcon(BitmapFactory.decodeResource(FloozApplication.getAppContext().getResources(),
                        R.drawable.flooz))
                .setContentIntent(contentIntent)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setAutoCancel(true)
                .setExtras(extras)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_SOCIAL);

//        if (!FloozApplication.appInForeground)
            this.notificationManager.notify(NOTIFICATION_ID, builder.build());
    }
}