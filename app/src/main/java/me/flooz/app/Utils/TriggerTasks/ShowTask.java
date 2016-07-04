package me.flooz.app.Utils.TriggerTasks;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;

import com.github.ajalt.reprint.core.AuthenticationFailureReason;
import com.github.ajalt.reprint.core.AuthenticationListener;
import com.github.ajalt.reprint.core.Reprint;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import me.flooz.app.App.FloozApplication;
import me.flooz.app.Model.FLError;
import me.flooz.app.Model.FLTransaction;
import me.flooz.app.Model.FLUser;
import me.flooz.app.Network.FloozHttpResponseHandler;
import me.flooz.app.Network.FloozRestClient;
import me.flooz.app.R;
import me.flooz.app.UI.Activity.HomeActivity;
import me.flooz.app.UI.Activity.StartActivity;
import me.flooz.app.UI.Fragment.Home.TabFragments.TabBarFragment;
import me.flooz.app.UI.Tools.ActionSheet;
import me.flooz.app.UI.Tools.ActionSheetItem;
import me.flooz.app.UI.Tools.CustomToast;
import me.flooz.app.UI.View.CustomDialog;
import me.flooz.app.Utils.FLTriggerManager;
import me.flooz.app.Utils.JSONHelper;
import me.flooz.app.Utils.TriggerTasks.ActionTask;

public class ShowTask extends ActionTask {

    public ShowTask() { super(); }

    @Override public void run() {
        if (this.trigger.categoryView.contentEquals("app:signup")) {
            if (FloozApplication.getInstance().getCurrentActivity() instanceof StartActivity) {
                StartActivity activity = (StartActivity) FloozApplication.getInstance().getCurrentActivity();

                try {
                    Map<String, Object> userData = JSONHelper.toMap(this.trigger.data);

                    if (userData.containsKey("fb") && ((Map) userData.get("fb")).containsKey("id"))
                        userData.put("avatarURL", "https://graph.facebook.com/" + ((Map) userData.get("fb")).get("id") + "/picture");

                    activity.updateUserData(userData);

                    FLTriggerManager.getInstance().executeTriggerList(trigger.triggers);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        } else if (this.trigger.categoryView.contentEquals("app:popup")) {
            CustomDialog.show(FloozApplication.getInstance().getCurrentActivity(), this.trigger.data, new DialogInterface.OnShowListener() {
                @Override
                public void onShow(DialogInterface dialog) {
                    FLTriggerManager.getInstance().executeTriggerList(trigger.triggers);
                }
            }, null);
        } else if (this.trigger.categoryView.contentEquals("app:alert")) {
            FLError errorContent = new FLError(trigger.data);
            CustomToast.show(FloozApplication.getAppContext(), errorContent);
            FLTriggerManager.getInstance().executeTriggerList(trigger.triggers);
        } else if (this.trigger.categoryView.contentEquals("app:list")) {
            List<ActionSheetItem> items = new ArrayList<>();

            JSONArray itemArray = trigger.data.optJSONArray("items");

            for (int i = 0; i < itemArray.length(); i++) {
                final JSONObject itemData = itemArray.optJSONObject(i);

                items.add(new ActionSheetItem(FloozApplication.getAppContext(), itemData.optString("name"), new ActionSheetItem.ActionSheetItemClickListener() {
                    @Override
                    public void onClick() {
                        FLTriggerManager.getInstance().executeTriggerList(FLTriggerManager.convertTriggersJSONArrayToList(itemData.optJSONArray("triggers")));
                    }
                }));
            }

            ActionSheet.showWithItems(FloozApplication.getInstance().getCurrentActivity(), items);
        } else if (this.trigger.categoryView.contentEquals("app:sms")) {
            if (this.trigger.data != null && this.trigger.data.has("recipients") && this.trigger.data.has("body")) {
                String uri = "smsto:";

                for (int i = 0; i < this.trigger.data.optJSONArray("recipients").length(); i++) {
                    if (i > 0) {
                        uri += ";";
                    }
                    uri += this.trigger.data.optJSONArray("recipients").optString(i);
                }

                Intent sendIntent = new Intent(Intent.ACTION_SENDTO);
                sendIntent.setData(Uri.parse(uri));
                sendIntent.putExtra("sms_body", this.trigger.data.optString("body"));
                if (sendIntent.resolveActivity(FloozApplication.getInstance().getCurrentActivity().getPackageManager()) != null) {
                    FloozApplication.getInstance().getCurrentActivity().startActivity(sendIntent);
                } else {
                    if (this.trigger.data.has("failure")) {
                        FLTriggerManager.getInstance().executeTriggerList(FLTriggerManager.convertTriggersJSONArrayToList(this.trigger.data.optJSONArray("failure")));
                    }
                }
            }
        } else if (this.trigger.categoryView.contentEquals("profile:user")) {
            if (this.trigger.data != null) {
                if (this.trigger.data.has("nick")) {
                    FLUser user = new FLUser(this.trigger.data);
                    FLTriggerManager.getInstance().pendingShowTrigger = trigger;
                    FloozApplication.getInstance().showUserProfile(user, new Runnable() {
                        @Override
                        public void run() {
                            FLTriggerManager.getInstance().executeTriggerList(trigger.triggers);
                            FLTriggerManager.getInstance().pendingShowTrigger = null;
                        }
                    });
                } else if (this.trigger.data.has("_id")) {
                    FloozRestClient.getInstance().showLoadView();
                    FloozRestClient.getInstance().getFullUser(this.trigger.data.optString("_id"), new FloozHttpResponseHandler() {
                        @Override
                        public void success(Object response) {
                            FLTriggerManager.getInstance().pendingShowTrigger = trigger;
                            FloozApplication.getInstance().showUserProfile((FLUser) response, new Runnable() {
                                @Override
                                public void run() {
                                    FLTriggerManager.getInstance().executeTriggerList(trigger.triggers);
                                    FLTriggerManager.getInstance().pendingShowTrigger = null;
                                }
                            });
                        }

                        @Override
                        public void failure(int statusCode, FLError error) {

                        }
                    });
                }
            }
        } else if (this.trigger.categoryView.contentEquals("timeline:flooz")) {
            if (this.trigger.data != null && this.trigger.data.has("_id")) {
                FloozRestClient.getInstance().showLoadView();
                FloozRestClient.getInstance().transactionWithId(this.trigger.data.optString("_id"), new FloozHttpResponseHandler() {
                    @Override
                    public void success(Object response) {
                        FLTransaction transac = new FLTransaction(((JSONObject) response).optJSONObject("item"));
                        FLTriggerManager.getInstance().pendingShowTrigger = trigger;
                        FloozApplication.getInstance().showTransactionCard(transac, new Runnable() {
                            @Override
                            public void run() {
                                FLTriggerManager.getInstance().executeTriggerList(trigger.triggers);
                                FLTriggerManager.getInstance().pendingShowTrigger = null;
                            }
                        });
                    }

                    @Override
                    public void failure(int statusCode, FLError error) {
                    }
                });
            }
        } else if (this.trigger.categoryView.contentEquals("timeline:pot")) {
            if (this.trigger.data != null && this.trigger.data.has("_id")) {
                FloozRestClient.getInstance().showLoadView();
                FloozRestClient.getInstance().transactionWithId(this.trigger.data.optString("_id"), new FloozHttpResponseHandler() {
                    @Override
                    public void success(Object response) {
                        FLTransaction transac = new FLTransaction(((JSONObject) response).optJSONObject("item"));
                        FLTriggerManager.getInstance().pendingShowTrigger = trigger;
                        FloozApplication.getInstance().showCollect(transac, new Runnable() {
                            @Override
                            public void run() {
                                FLTriggerManager.getInstance().executeTriggerList(trigger.triggers);
                                FLTriggerManager.getInstance().pendingShowTrigger = null;
                            }
                        });
                    }

                    @Override
                    public void failure(int statusCode, FLError error) {
                    }
                });
            }
        } else if (this.trigger.categoryView.contentEquals("auth:code")) {
            final Class activityClass = FLTriggerManager.getInstance().binderKeyActivity.get(this.trigger.categoryView);
            final Activity currentActivity = FloozApplication.getInstance().getCurrentActivity();

            if (Reprint.hasFingerprintRegistered() && Reprint.isHardwarePresent()) {
                Reprint.authenticate(new AuthenticationListener() {
                    @Override
                    public void onSuccess(int moduleTag) {
                        if (trigger.data != null && trigger.data.has("success")) {
                            FLTriggerManager.getInstance().executeTriggerList(FLTriggerManager.convertTriggersJSONArrayToList(trigger.data.optJSONArray("success")));
                        }
                    }

                    @Override
                    public void onFailure(AuthenticationFailureReason failureReason, boolean fatal, CharSequence errorMessage, int moduleTag, int errorCode) {
                        if (fatal) {
                            if (currentActivity != null) {
                                FLTriggerManager.getInstance().pendingShowTrigger = trigger;
                                Intent intent = new Intent();
                                intent.setClass(FloozApplication.getAppContext(), activityClass);

                                if (trigger.data != null)
                                    intent.putExtra("triggerData", trigger.data.toString());

                                currentActivity.startActivity(intent);
                                currentActivity.overridePendingTransition(R.anim.slide_up, android.R.anim.fade_out);
                            } else {
                                FLTriggerManager.getInstance().pendingShowTrigger = trigger;
                                Intent intent = new Intent();
                                intent.setClass(FloozApplication.getAppContext(), activityClass);

                                if (trigger.data != null)
                                    intent.putExtra("triggerData", trigger.data.toString());

                                FloozApplication.getInstance().startActivity(intent);
                            }
                        }
                    }
                });
            } else {
                if (currentActivity != null) {
                    FLTriggerManager.getInstance().pendingShowTrigger = this.trigger;
                    Intent intent = new Intent();
                    intent.setClass(FloozApplication.getAppContext(), activityClass);

                    if (this.trigger.data != null)
                        intent.putExtra("triggerData", this.trigger.data.toString());

                    currentActivity.startActivity(intent);
                    currentActivity.overridePendingTransition(R.anim.slide_up, android.R.anim.fade_out);
                } else {
                    FLTriggerManager.getInstance().pendingShowTrigger = this.trigger;
                    Intent intent = new Intent();
                    intent.setClass(FloozApplication.getAppContext(), activityClass);

                    if (this.trigger.data != null)
                        intent.putExtra("triggerData", this.trigger.data.toString());

                    FloozApplication.getInstance().startActivity(intent);
                }
            }


//                FloozRestClient.getInstance().showLoadView();
//                FloozRestClient.getInstance().transactionWithId(this.trigger.data.optString("_id"), new FloozHttpResponseHandler() {
//                    @Override
//                    public void success(Object response) {
//                        FLTransaction transac = new FLTransaction(((JSONObject) response).optJSONObject("item"));
//                        FLTriggerManager.getInstance().pendingShowTrigger = trigger;
//                        FloozApplication.getInstance().showCollect(transac, new Runnable() {
//                            @Override
//                            public void run() {
//                                FLTriggerManager.getInstance().executeTriggerList(trigger.triggers);
//                                FLTriggerManager.getInstance().pendingShowTrigger = null;
//                            }
//                        });
//                    }
//
//                    @Override
//                    public void failure(int statusCode, FLError error) {
//                    }
//                });
        } else if (FLTriggerManager.getInstance().isTriggerKeyView(this.trigger)) {
            Class activityClass = FLTriggerManager.getInstance().binderKeyActivity.get(this.trigger.categoryView);
            Class fragmentClass = FLTriggerManager.getInstance().binderKeyFragment.get(this.trigger.categoryView);
            Activity currentActivity = FloozApplication.getInstance().getCurrentActivity();

            if (FLTriggerManager.getInstance().isTriggerKeyViewRoot(this.trigger)) {
                Integer rootId = FLTriggerManager.getInstance().isViewClassRoot(fragmentClass);

                if (rootId >= 0) {
                    if (currentActivity != null && currentActivity instanceof HomeActivity) {
                        HomeActivity homeActivity = (HomeActivity) currentActivity;

                        HomeActivity.TabID tabId = HomeActivity.tabIDFromIndex(rootId);
                        if (tabId != HomeActivity.TabID.NONE) {
                            homeActivity.changeCurrentTab(tabId, true, new Runnable() {
                                @Override
                                public void run() {
                                    FLTriggerManager.getInstance().executeTriggerList(trigger.triggers);
                                }
                            });
                        }
                    } else if (currentActivity != null) {
                        FLTriggerManager.getInstance().pendingShowTrigger = this.trigger;
                        Intent intent = new Intent();
                        intent.setClass(currentActivity, HomeActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        currentActivity.startActivity(intent);
                        currentActivity.overridePendingTransition(R.anim.slide_up, android.R.anim.fade_out);
                    } else {
                        FLTriggerManager.getInstance().pendingShowTrigger = this.trigger;
                        Intent intent = new Intent();
                        intent.setClass(FloozApplication.getAppContext(), HomeActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        FloozApplication.getInstance().startActivity(intent);
                    }
                } else if (currentActivity != null) {
                    FLTriggerManager.getInstance().pendingShowTrigger = this.trigger;
                    Intent intent = new Intent();
                    intent.setClass(FloozApplication.getAppContext(), activityClass);

                    if (this.trigger.data != null)
                        intent.putExtra("triggerData", this.trigger.data.toString());

                    currentActivity.startActivity(intent);
                    currentActivity.overridePendingTransition(R.anim.slide_up, android.R.anim.fade_out);
                } else {
                    FLTriggerManager.getInstance().pendingShowTrigger = this.trigger;
                    Intent intent = new Intent();
                    intent.setClass(FloozApplication.getAppContext(), activityClass);

                    if (this.trigger.data != null)
                        intent.putExtra("triggerData", this.trigger.data.toString());

                    FloozApplication.getInstance().startActivity(intent);
                }
            } else if (FLTriggerManager.getInstance().isTriggerKeyViewPush(this.trigger) && fragmentClass != null) {
                if (currentActivity != null && currentActivity instanceof HomeActivity) {
                    HomeActivity homeActivity = (HomeActivity) currentActivity;

                    try {
                        TabBarFragment newFragment = (TabBarFragment) fragmentClass.newInstance();
                        newFragment.triggerData = this.trigger.data;

                        homeActivity.pushFragmentInCurrentTab(newFragment, new Runnable() {
                            @Override
                            public void run() {
                                FLTriggerManager.getInstance().executeTriggerList(trigger.triggers);
                            }
                        });
                    } catch (InstantiationException e) {
                        e.printStackTrace();
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                } else {
                    if (currentActivity != null) {
                        FLTriggerManager.getInstance().pendingShowTrigger = this.trigger;
                        Intent intent = new Intent();
                        intent.setClass(FloozApplication.getAppContext(), activityClass);

                        if (this.trigger.data != null)
                            intent.putExtra("triggerData", this.trigger.data.toString());

                        currentActivity.startActivity(intent);
                        currentActivity.overridePendingTransition(R.anim.slide_up, android.R.anim.fade_out);
                    } else {
                        FLTriggerManager.getInstance().pendingShowTrigger = this.trigger;
                        Intent intent = new Intent();
                        intent.setClass(FloozApplication.getAppContext(), activityClass);

                        if (this.trigger.data != null)
                            intent.putExtra("triggerData", this.trigger.data.toString());

                        FloozApplication.getInstance().startActivity(intent);
                    }
                }
            } else if (FLTriggerManager.getInstance().isTriggerKeyModal(this.trigger)) {
                if (currentActivity != null) {
                    FLTriggerManager.getInstance().pendingShowTrigger = this.trigger;
                    Intent intent = new Intent();
                    intent.setClass(FloozApplication.getAppContext(), activityClass);

                    if (this.trigger.data != null)
                        intent.putExtra("triggerData", this.trigger.data.toString());

                    currentActivity.startActivity(intent);
                    currentActivity.overridePendingTransition(R.anim.slide_up, android.R.anim.fade_out);
                } else {
                    FLTriggerManager.getInstance().pendingShowTrigger = this.trigger;
                    Intent intent = new Intent();
                    intent.setClass(FloozApplication.getAppContext(), activityClass);

                    if (this.trigger.data != null)
                        intent.putExtra("triggerData", this.trigger.data.toString());

                    FloozApplication.getInstance().startActivity(intent);
                }
            }
        }
    }
}
