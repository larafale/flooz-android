package me.flooz.app.App;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import me.flooz.app.R;
import me.flooz.app.UI.Activity.LoadingActivity;

/**
 * Created by Flooz on 1/14/15.
 */
public class GcmIntentService extends IntentService {

    public static final int NOTIFICATION_ID = 1;

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
        SharedPreferences appSettings = FloozApplication.getAppContext().getSharedPreferences("FloozPrefs", Context.MODE_PRIVATE);
        String userId = appSettings.getString("userId", null);

        if (!FloozApplication.appInForeground && userId != null && (extras.getCharSequence("userId") == null || userId.contentEquals(extras.getCharSequence("userId")))) {
            NotificationManager notificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);

            String array = extras.getCharSequence("triggers", "").toString();

            Intent pendingIntentContent = new Intent(this, LoadingActivity.class);

            if (!array.isEmpty())
                pendingIntentContent.putExtra("triggers", array);

            PendingIntent contentIntent = PendingIntent.getActivity(this, 0, pendingIntentContent, PendingIntent.FLAG_UPDATE_CURRENT);

            NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                    .setContentTitle(extras.getCharSequence("title"))
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(extras.getCharSequence("text")))
                    .setContentText(extras.getCharSequence("text"))
                    .setSmallIcon(R.drawable.ic_stat_onesignal_default)
                    .setLargeIcon(BitmapFactory.decodeResource(FloozApplication.getAppContext().getResources(),
                            R.drawable.flooz_mini))
                    .setContentIntent(contentIntent)
                    .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                    .setAutoCancel(true)
                    .setExtras(extras)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setCategory(NotificationCompat.CATEGORY_SOCIAL);

            notificationManager.notify(NOTIFICATION_ID, builder.build());
        }
    }
}