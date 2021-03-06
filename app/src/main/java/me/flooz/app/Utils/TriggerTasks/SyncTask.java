package me.flooz.app.Utils.TriggerTasks;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;

import org.json.JSONObject;

import me.flooz.app.App.FloozApplication;
import me.flooz.app.Model.FLError;
import me.flooz.app.Model.FLTransaction;
import me.flooz.app.Network.FloozHttpResponseHandler;
import me.flooz.app.Network.FloozRestClient;
import me.flooz.app.R;
import me.flooz.app.Utils.CustomNotificationIntents;
import me.flooz.app.Utils.FLTriggerManager;
import me.flooz.app.Utils.TriggerTasks.ActionTask;

public class SyncTask extends ActionTask {

    public SyncTask() {}

    @Override public void run() {
        if (this.trigger.view.length() == 0) {
            switch (this.trigger.category) {
                case "app":
                    if (this.trigger.data != null && this.trigger.data.has("url")) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(FloozApplication.getInstance().getCurrentActivity());
                        builder.setTitle(R.string.GLOBAL_UPDATE);
                        builder.setMessage(R.string.MSG_UPDATE);
                        builder.setPositiveButton(R.string.BTN_UPDATE, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Activity tmp = FloozApplication.getInstance().getCurrentActivity();
                                Intent i = new Intent(Intent.ACTION_VIEW);
                                i.setData(Uri.parse(trigger.data.optString("url")));
                                tmp.startActivity(i);
                                tmp.finish();
                            }
                        });
                        builder.setCancelable(false);
                        builder.show();

                        FLTriggerManager.getInstance().executeTriggerList(trigger.triggers);
                    }
                    break;
                case "timeline":
                    FloozApplication.performLocalNotification(CustomNotificationIntents.reloadTimeline());
                    FLTriggerManager.getInstance().executeTriggerList(trigger.triggers);
                    break;
                case "invitation":
                    FloozRestClient.getInstance().getInvitationText(new FloozHttpResponseHandler() {
                        @Override
                        public void success(Object response) {
                            FLTriggerManager.getInstance().executeTriggerList(trigger.triggers);
                        }

                        @Override
                        public void failure(int statusCode, FLError error) {
                            FLTriggerManager.getInstance().executeTriggerList(trigger.triggers);
                        }
                    });
                    break;
                case "flooz":
                    final Intent intent = CustomNotificationIntents.reloadTransaction();

                    intent.putExtra("id", trigger.data.optString("_id"));

                    if (trigger.data.has("commentId"))
                        intent.putExtra("commentId", trigger.data.optString("commentId"));

                    if (trigger.data.has("flooz")) {
                        intent.putExtra("flooz", trigger.data.optJSONObject("flooz").toString());

                        FloozApplication.performLocalNotification(intent);
                        FLTriggerManager.getInstance().executeTriggerList(trigger.triggers);
                    } else {
                        FloozRestClient.getInstance().transactionWithId(trigger.data.optString("_id"), new FloozHttpResponseHandler() {
                            @Override
                            public void success(Object response) {
                                    intent.putExtra("flooz", ((JSONObject) response).optJSONObject("item").toString());


                                FloozApplication.performLocalNotification(intent);
                                FLTriggerManager.getInstance().executeTriggerList(trigger.triggers);
                            }

                            @Override
                            public void failure(int statusCode, FLError error) {

                            }
                        });
                    }
                    break;
                case "pot":
                    final Intent intentCollect = CustomNotificationIntents.reloadCollect();

                    intentCollect.putExtra("id", trigger.data.optString("_id"));

                    if (trigger.data.has("commentId"))
                        intentCollect.putExtra("commentId", trigger.data.optString("commentId"));

                    if (trigger.data.has("flooz")) {
                        intentCollect.putExtra("flooz", trigger.data.optJSONObject("flooz").toString());

                        FloozApplication.performLocalNotification(intentCollect);
                        FLTriggerManager.getInstance().executeTriggerList(trigger.triggers);
                    } else {
                        FloozRestClient.getInstance().transactionWithId(trigger.data.optString("_id"), new FloozHttpResponseHandler() {
                            @Override
                            public void success(Object response) {
                                intentCollect.putExtra("flooz", ((JSONObject) response).optJSONObject("item").toString());


                                FloozApplication.performLocalNotification(intentCollect);
                                FLTriggerManager.getInstance().executeTriggerList(trigger.triggers);
                            }

                            @Override
                            public void failure(int statusCode, FLError error) {

                            }
                        });
                    }
                    break;
                case "profile":
                    FloozRestClient.getInstance().updateCurrentUser(new FloozHttpResponseHandler() {
                        @Override
                        public void success(Object response) {
                            FLTriggerManager.getInstance().executeTriggerList(trigger.triggers);
                        }

                        @Override
                        public void failure(int statusCode, FLError error) {
                            FLTriggerManager.getInstance().executeTriggerList(trigger.triggers);
                        }
                    });
                    break;
                case "notifs":
                    FloozRestClient.getInstance().updateNotificationFeed(new FloozHttpResponseHandler() {
                        @Override
                        public void success(Object response) {
                            FLTriggerManager.getInstance().executeTriggerList(trigger.triggers);
                        }

                        @Override
                        public void failure(int statusCode, FLError error) {
                            FLTriggerManager.getInstance().executeTriggerList(trigger.triggers);
                        }
                    });
                    break;
                default:
                    break;
            }
        } else if (this.trigger.categoryView.contentEquals("cashin:audiotel")) {
            Intent intentCollect = new Intent(this.trigger.key);

            intentCollect.putExtra("code", trigger.data.optString("code"));

            FloozApplication.performLocalNotification(intentCollect);
            FLTriggerManager.getInstance().executeTriggerList(trigger.triggers);
        }
    }
}
