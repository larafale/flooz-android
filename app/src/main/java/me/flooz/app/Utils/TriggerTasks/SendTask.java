package me.flooz.app.Utils.TriggerTasks;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;

import me.flooz.app.App.FloozApplication;
import me.flooz.app.Model.FLError;
import me.flooz.app.Network.FloozHttpResponseHandler;
import me.flooz.app.Network.FloozRestClient;
import me.flooz.app.UI.Activity.NewCollectActivity;
import me.flooz.app.UI.Activity.NewTransactionActivity;
import me.flooz.app.Utils.CustomNotificationIntents;
import me.flooz.app.Utils.FLTriggerManager;
import me.flooz.app.Utils.ImageHelper;
import me.flooz.app.Utils.TriggerTasks.ActionTask;

public class SendTask extends ActionTask {

    public SendTask() { super(); }

    @Override public void run() {
        if (this.trigger.categoryView.contentEquals("image:flooz") && this.trigger.data != null && this.trigger.data.has("_id")) {
            Activity currentActivity = FloozApplication.getInstance().getCurrentActivity();
            if (currentActivity instanceof NewTransactionActivity) {
                NewTransactionActivity transactionActivity = (NewTransactionActivity) currentActivity;

                if (transactionActivity.havePicture) {
                    final Bitmap currentImage = transactionActivity.currentPicture;

                    Handler mainHandler = new Handler(Looper.getMainLooper());
                    Runnable myRunnable = new Runnable() {
                        @Override
                        public void run() {
                            android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
                            FloozRestClient.getInstance().uploadTransactionPic(trigger.data.optString("_id"), ImageHelper.convertBitmapInFile(currentImage), new FloozHttpResponseHandler() {
                                @Override
                                public void success(Object response) {
                                    FLTriggerManager.getInstance().executeTriggerList(trigger.triggers);
                                }

                                @Override
                                public void failure(int statusCode, FLError error) {
                                    FLTriggerManager.getInstance().executeTriggerList(trigger.triggers);
                                }
                            });
                        }
                    };
                    mainHandler.post(myRunnable);
                }
            }
        } else if (this.trigger.categoryView.contentEquals("image:pot") && this.trigger.data != null && this.trigger.data.has("_id")) {
            Activity currentActivity = FloozApplication.getInstance().getCurrentActivity();
            if (currentActivity instanceof NewCollectActivity) {
                NewCollectActivity transactionActivity = (NewCollectActivity) currentActivity;

                if (transactionActivity.havePicture) {
                    final Bitmap currentImage = transactionActivity.currentPicture;

                    Handler mainHandler = new Handler(Looper.getMainLooper());
                    Runnable myRunnable = new Runnable() {
                        @Override
                        public void run() {
                            android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
                            FloozRestClient.getInstance().uploadTransactionPic(trigger.data.optString("_id"), ImageHelper.convertBitmapInFile(currentImage), new FloozHttpResponseHandler() {
                                @Override
                                public void success(Object response) {
                                    FLTriggerManager.getInstance().executeTriggerList(trigger.triggers);
                                }

                                @Override
                                public void failure(int statusCode, FLError error) {
                                    FLTriggerManager.getInstance().executeTriggerList(trigger.triggers);
                                }
                            });
                        }
                    };
                    mainHandler.post(myRunnable);
                }
            }
        } else if (this.trigger.categoryView.contentEquals("video:question")) {
            FloozApplication.performLocalNotification(CustomNotificationIntents.sendQuestionVideo());
        }
    }
}
