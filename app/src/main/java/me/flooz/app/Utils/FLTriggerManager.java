package me.flooz.app.Utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.SyncHttpClient;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.ByteArrayEntity;
import cz.msebera.android.httpclient.message.BasicHeader;
import cz.msebera.android.httpclient.protocol.HTTP;
import me.flooz.app.App.FloozApplication;
import me.flooz.app.Model.FLError;
import me.flooz.app.Model.FLTransaction;
import me.flooz.app.Model.FLTrigger;
import me.flooz.app.Model.FLUser;
import me.flooz.app.Network.FloozHttpResponseHandler;
import me.flooz.app.Network.FloozRestClient;
import me.flooz.app.R;
import me.flooz.app.UI.Activity.AuthenticationActivity;
import me.flooz.app.UI.Activity.BaseActivity;
import me.flooz.app.UI.Activity.CashoutActivity;
import me.flooz.app.UI.Activity.EditProfileActivity;
import me.flooz.app.UI.Activity.FriendRequestActivity;
import me.flooz.app.UI.Activity.HomeActivity;
import me.flooz.app.UI.Activity.NewCollectActivity;
import me.flooz.app.UI.Activity.NewTransactionActivity;
import me.flooz.app.UI.Activity.NotificationActivity;
import me.flooz.app.UI.Activity.SearchActivity;
import me.flooz.app.UI.Activity.Secure3DActivity;
import me.flooz.app.UI.Activity.Settings.BankSettingsActivity;
import me.flooz.app.UI.Activity.Settings.CreditCardSettingsActivity;
import me.flooz.app.UI.Activity.Settings.DocumentsSettingsActivity;
import me.flooz.app.UI.Activity.Settings.IdentitySettingsActivity;
import me.flooz.app.UI.Activity.Settings.NotificationsSettingsActivity;
import me.flooz.app.UI.Activity.Settings.PrivacySettingsActivity;
import me.flooz.app.UI.Activity.Settings.SetSecureCodeActivity;
import me.flooz.app.UI.Activity.ShareAppActivity;
import me.flooz.app.UI.Activity.SponsorActivity;
import me.flooz.app.UI.Activity.StartActivity;
import me.flooz.app.UI.Activity.TransactionActivity;
import me.flooz.app.UI.Activity.UserProfileActivity;
import me.flooz.app.UI.Activity.ValidateSMSActivity;
import me.flooz.app.UI.Activity.WebContentActivity;
import me.flooz.app.UI.Fragment.Home.TabFragments.BankFragment;
import me.flooz.app.UI.Fragment.Home.TabFragments.CashoutFragment;
import me.flooz.app.UI.Fragment.Home.TabFragments.CreditCardFragment;
import me.flooz.app.UI.Fragment.Home.TabFragments.DocumentsFragment;
import me.flooz.app.UI.Fragment.Home.TabFragments.FriendRequestFragment;
import me.flooz.app.UI.Fragment.Home.TabFragments.IdentityFragment;
import me.flooz.app.UI.Fragment.Home.TabFragments.NotificationsFragment;
import me.flooz.app.UI.Fragment.Home.TabFragments.NotifsSettingsFragment;
import me.flooz.app.UI.Fragment.Home.TabFragments.PrivacyFragment;
import me.flooz.app.UI.Fragment.Home.TabFragments.ProfileCardFragment;
import me.flooz.app.UI.Fragment.Home.TabFragments.SearchFragment;
import me.flooz.app.UI.Fragment.Home.TabFragments.ShareFragment;
import me.flooz.app.UI.Fragment.Home.TabFragments.SponsorFragment;
import me.flooz.app.UI.Fragment.Home.TabFragments.TabBarFragment;
import me.flooz.app.UI.Fragment.Home.TabFragments.TimelineFragment;
import me.flooz.app.UI.Fragment.Home.TabFragments.TransactionCardFragment;
import me.flooz.app.UI.Fragment.Home.TabFragments.WebFragment;
import me.flooz.app.UI.View.CustomDialog;

/**
 * Created by Flooz on 2/8/16.
 */
public class FLTriggerManager implements Application.ActivityLifecycleCallbacks {

    abstract class ActionTask implements Runnable {
        public FLTrigger trigger;
        public abstract void run();
    }

    private Map<FLTrigger.FLTriggerAction, ActionTask> binderActionFunction;
    private Map<String, Class> binderKeyActivity;
    private Map<String, Class> binderKeyFragment;
    private Map<String, String> binderKeyType;

    private FLTrigger pendingHideTrigger;
    private FLTrigger pendingShowTrigger;

    private static FLTriggerManager instance;

    public static FLTriggerManager getInstance()
    {
        if (instance == null)
            instance = new FLTriggerManager();
        return instance;
    }

    private FLTriggerManager() {
        this.loadBinderActionFunction();
        this.loadBinderKeyActivity();
        this.loadBinderKeyFragment();
        this.loadBinderKeyType();
    }

    public static ArrayList<FLTrigger> convertTriggersJSONArrayToList(JSONArray json) {
        ArrayList<FLTrigger> ret = new ArrayList<>();

        if (json != null) {
            for (int i = 0; i < json.length(); i++) {
                if (json.optJSONObject(i) != null) {
                    FLTrigger tmp = new FLTrigger(json.optJSONObject(i));

                    if (tmp.valid)
                        ret.add(tmp);
                }
            }
        }

        return ret;
    }

    public void executeTriggerList(List<FLTrigger> triggers) {
        if (triggers != null) {
            for (FLTrigger trigger : triggers) {
                if (trigger.valid) {
                    this.executeTrigger(trigger);
                }
            }
        }
    }

    public void executeTrigger(FLTrigger trigger) {
        if (FloozApplication.appInForeground && trigger != null && trigger.valid && this.binderActionFunction.containsKey(trigger.action)) {
            ActionTask runnable = this.binderActionFunction.get(trigger.action);
            runnable.trigger = trigger;

            if (trigger.delay.doubleValue() > 0) {
                Handler handler = new Handler(Looper.getMainLooper());
                handler.postDelayed(runnable, (int) (trigger.delay.doubleValue() * 1000));
            } else {
                Handler handler = new Handler(Looper.getMainLooper());
                handler.post(runnable);
            }
        }
    }

    private void loadBinderActionFunction() {
        this.binderActionFunction = new HashMap<FLTrigger.FLTriggerAction, ActionTask>() {{
            put(FLTrigger.FLTriggerAction.FLTriggerActionAsk, askActionHandler);
            put(FLTrigger.FLTriggerAction.FLTriggerActionCall, callActionHandler);
            put(FLTrigger.FLTriggerAction.FLTriggerActionClear, clearActionHandler);
            put(FLTrigger.FLTriggerAction.FLTriggerActionHide, hideActionHandler);
            put(FLTrigger.FLTriggerAction.FLTriggerActionLogin, loginActionHandler);
            put(FLTrigger.FLTriggerAction.FLTriggerActionLogout, logoutActionHandler);
            put(FLTrigger.FLTriggerAction.FLTriggerActionOpen, openActionHandler);
            put(FLTrigger.FLTriggerAction.FLTriggerActionSend, sendActionHandler);
            put(FLTrigger.FLTriggerAction.FLTriggerActionShow, showActionHandler);
            put(FLTrigger.FLTriggerAction.FLTriggerActionSync, syncActionHandler);
        }};
    }

    private ActionTask askActionHandler = new ActionTask() {
        @Override
        public void run() {
            if (this.trigger.category.contentEquals("fb")) {
                FloozRestClient.getInstance().connectFacebook();
                FLTriggerManager.this.executeTriggerList(this.trigger.triggers);
            }
        }
    };

    private ActionTask callActionHandler = new ActionTask() {
        @Override
        public void run() {
            if (this.trigger.category.contentEquals("http")) {
                if (this.trigger.data != null && this.trigger.data.has("url") && this.trigger.data.has("method")) {
                    if (this.trigger.data.has("external") && this.trigger.data.optBoolean("external")) {
                        AsyncHttpClient httpClient;

                        if (Looper.myLooper() == null) {
                            httpClient = new SyncHttpClient();
                        } else {
                            httpClient = new AsyncHttpClient();
                        }

                        httpClient.addHeader("Accept", "*/*");

                        ByteArrayEntity entity = null;

                        try {
                            entity = new ByteArrayEntity(this.trigger.data.optJSONObject("body").toString().getBytes("UTF-8"));
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }

                        String contentType = "application/json";

                        if (this.trigger.data.has("type") && this.trigger.data.optString("type").contentEquals("urlencoded")) {
                            httpClient.setURLEncodingEnabled(true);
                            contentType = "application/x-www-form-urlencoded";
                        } else {
                            httpClient.setURLEncodingEnabled(false);

                            if (entity != null)
                                entity.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
                        }

                        AsyncHttpResponseHandler responseHandler = new AsyncHttpResponseHandler() {
                            @Override
                            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                                if (trigger.data.has("lock") && trigger.data.optBoolean("lock"))
                                    FloozRestClient.getInstance().hideLoadView();

                                FLTriggerManager.this.executeTriggerList(trigger.triggers);

                                if (trigger.data.has("success")) {
                                    FLTriggerManager.this.executeTriggerList(FLTriggerManager.convertTriggersJSONArrayToList(trigger.data.optJSONArray("success")));
                                }
                            }

                            @Override
                            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                                if (trigger.data.has("lock") && trigger.data.optBoolean("lock"))
                                    FloozRestClient.getInstance().hideLoadView();

                                FLTriggerManager.this.executeTriggerList(trigger.triggers);

                                if (trigger.data.has("failure")) {
                                    FLTriggerManager.this.executeTriggerList(FLTriggerManager.convertTriggersJSONArrayToList(trigger.data.optJSONArray("failure")));
                                }
                            }
                        };

                        if (trigger.data.has("lock") && trigger.data.optBoolean("lock"))
                            FloozRestClient.getInstance().showLoadView();

                        switch (this.trigger.data.optString("method").toUpperCase()) {
                            case "GET":
                                httpClient.get(FloozApplication.getAppContext(), this.trigger.data.optString("url"), entity, contentType, responseHandler);
                                break;
                            case "POST":
                                httpClient.post(FloozApplication.getAppContext(), this.trigger.data.optString("url"), entity, contentType, responseHandler);
                                break;
                            case "PUT":
                                httpClient.put(FloozApplication.getAppContext(), this.trigger.data.optString("url"), entity, contentType, responseHandler);
                                break;
                            case "DELETE":
                                httpClient.delete(FloozApplication.getAppContext(), this.trigger.data.optString("url"), entity, contentType, responseHandler);
                                break;
                            default:
                                break;
                        }
                    } else {
                        FloozRestClient.HttpRequestType method;

                        switch (this.trigger.data.optString("method").toUpperCase()) {
                            case "GET":
                                method = FloozRestClient.HttpRequestType.GET;
                                break;
                            case "POST":
                                method = FloozRestClient.HttpRequestType.POST;
                                break;
                            case "PUT":
                                method = FloozRestClient.HttpRequestType.PUT;
                                break;
                            case "DELETE":
                                method = FloozRestClient.HttpRequestType.DELETE;
                                break;
                            default:
                                method = FloozRestClient.HttpRequestType.GET;
                                break;
                        }

                        Map param = null;

                        if (this.trigger.data.has("body") && this.trigger.data.opt("body") instanceof JSONObject) {
                            try {
                                param = JSONHelper.toMap(this.trigger.data.optJSONObject("body"));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        else if (this.trigger.data.has("body") && this.trigger.data.opt("body") instanceof String) {
                            try {
                                param = JSONHelper.toMap(new JSONObject(this.trigger.data.optString("body")));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        String url = this.trigger.data.optString("url");

                        if (url.charAt(0) != '/')
                            url = "/" + url;

                        if (trigger.data.has("lock") && trigger.data.optBoolean("lock"))
                            FloozRestClient.getInstance().showLoadView();

                        FloozRestClient.getInstance().request(url, method, param, new FloozHttpResponseHandler() {
                            @Override
                            public void success(Object response) {
                                FLTriggerManager.this.executeTriggerList(trigger.triggers);

                                if (trigger.data.has("success")) {
                                    FLTriggerManager.this.executeTriggerList(FLTriggerManager.convertTriggersJSONArrayToList(trigger.data.optJSONArray("success")));
                                }
                            }

                            @Override
                            public void failure(int statusCode, FLError error) {
                                FLTriggerManager.this.executeTriggerList(trigger.triggers);

                                if (trigger.data.has("failure")) {
                                    FLTriggerManager.this.executeTriggerList(FLTriggerManager.convertTriggersJSONArrayToList(trigger.data.optJSONArray("failure")));
                                }
                            }
                        });
                    }
                }
            }
        }
    };

    private ActionTask clearActionHandler = new ActionTask() {
        @Override
        public void run() {
            if (this.trigger.category.contentEquals("code")) {
                FloozRestClient.getInstance().clearSecureCode();
                FLTriggerManager.this.executeTriggerList(trigger.triggers);
            }
        }
    };

    private ActionTask hideActionHandler = new ActionTask() {
        @Override
        public void run() {
            Activity currentActivity = FloozApplication.getInstance().getCurrentActivity();

            if (currentActivity != null) {
                if (currentActivity instanceof HomeActivity) {
                    TabBarFragment currentFragment = ((HomeActivity)currentActivity).currentFragment;

                    if (FLTriggerManager.this.binderKeyFragment.containsKey(this.trigger.categoryView)) {
                        Class wantedFragmentClass = FLTriggerManager.this.binderKeyFragment.get(this.trigger.categoryView);

                        if (currentFragment.getClass().equals(wantedFragmentClass) && ((HomeActivity)currentActivity).currentTabHistory.size() > 1) {
                            ((HomeActivity)currentActivity).popFragmentInCurrentTab(new Runnable() {
                                @Override
                                public void run() {
                                    FLTriggerManager.this.executeTriggerList(trigger.triggers);
                                }
                            });
                        }
                    }
                } else if (FLTriggerManager.this.binderKeyActivity.containsKey(this.trigger.categoryView)) {
                    Class wantedActivityClass = FLTriggerManager.this.binderKeyActivity.get(this.trigger.categoryView);

                    if (currentActivity.getClass().equals(wantedActivityClass)) {
                        FLTriggerManager.this.pendingHideTrigger = this.trigger;
                        currentActivity.finish();
                        currentActivity.overridePendingTransition(android.R.anim.fade_in, R.anim.slide_down);
                    }
                }
            }
        }
    };

    private ActionTask loginActionHandler = new ActionTask() {
        @Override
        public void run() {
            if (this.trigger.category.contentEquals("auth")) {
                if (this.trigger.data != null && this.trigger.data.has("token")) {
                    FloozRestClient.getInstance().loginWithToken(this.trigger.data.optString("token"), new FloozHttpResponseHandler() {
                        @Override
                        public void success(Object response) {
                            FLTriggerManager.this.executeTriggerList(trigger.triggers);
                        }

                        @Override
                        public void failure(int statusCode, FLError error) {
                            FLTriggerManager.this.executeTriggerList(trigger.triggers);
                        }
                    });
                } else {
                    FloozApplication.getInstance().displayMainView();
                    FLTriggerManager.this.executeTriggerList(trigger.triggers);
                }
            }
        }
    };

    private ActionTask logoutActionHandler = new ActionTask() {
        @Override
        public void run() {
            if (this.trigger.category.contentEquals("auth")) {
                FloozRestClient.getInstance().logout();
                FLTriggerManager.this.executeTriggerList(trigger.triggers);
            }
        }
    };

    private ActionTask openActionHandler = new ActionTask() {
        @Override
        public void run() {
            if (this.trigger.category.contentEquals("web")) {
                if (this.trigger.data != null && this.trigger.data.has("url")) {
                    FloozApplication.getInstance().getCurrentActivity().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(this.trigger.data.optString("url"))));
                    FLTriggerManager.this.executeTriggerList(trigger.triggers);
                }
            }
        }
    };

    private ActionTask sendActionHandler = new ActionTask() {
        @Override
        public void run() {
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
                                        FLTriggerManager.this.executeTriggerList(trigger.triggers);
                                    }

                                    @Override
                                    public void failure(int statusCode, FLError error) {
                                        FLTriggerManager.this.executeTriggerList(trigger.triggers);
                                    }
                                });
                            }
                        };
                        mainHandler.post(myRunnable);
                    }
                }
            }
        }
    };

    private ActionTask showActionHandler = new ActionTask() {
        @Override
        public void run() {
            if (this.trigger.categoryView.contentEquals("app:signup")) {
                if (FloozApplication.getInstance().getCurrentActivity() instanceof StartActivity) {
                    StartActivity activity = (StartActivity) FloozApplication.getInstance().getCurrentActivity();

                    try {
                        Map<String, Object> userData = JSONHelper.toMap(this.trigger.data);

                        if (userData.containsKey("fb") && ((Map) userData.get("fb")).containsKey("id"))
                            userData.put("avatarURL", "https://graph.facebook.com/" + ((Map) userData.get("fb")).get("id") + "/picture");

                        activity.updateUserData(userData);

                        FLTriggerManager.this.executeTriggerList(trigger.triggers);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            } else if (this.trigger.categoryView.contentEquals("app:popup")) {
                CustomDialog.show(FloozApplication.getInstance().getCurrentActivity(), this.trigger.data, new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(DialogInterface dialog) {
                        FLTriggerManager.this.executeTriggerList(trigger.triggers);
                    }
                }, null);
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
                        pendingShowTrigger = trigger;
                        FloozApplication.getInstance().showUserProfile(user, new Runnable() {
                            @Override
                            public void run() {
                                FLTriggerManager.this.executeTriggerList(trigger.triggers);
                                pendingShowTrigger = null;
                            }
                        });
                    } else if (this.trigger.data.has("_id")) {
                        FloozRestClient.getInstance().showLoadView();
                        FloozRestClient.getInstance().getFullUser(this.trigger.data.optString("_id"), new FloozHttpResponseHandler() {
                            @Override
                            public void success(Object response) {
                                pendingShowTrigger = trigger;
                                FloozApplication.getInstance().showUserProfile((FLUser) response, new Runnable() {
                                    @Override
                                    public void run() {
                                        FLTriggerManager.this.executeTriggerList(trigger.triggers);
                                        pendingShowTrigger = null;
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
                            pendingShowTrigger = trigger;
                            FloozApplication.getInstance().showTransactionCard(transac, new Runnable() {
                                @Override
                                public void run() {
                                    FLTriggerManager.this.executeTriggerList(trigger.triggers);
                                    pendingShowTrigger = null;
                                }
                            });
                        }

                        @Override
                        public void failure(int statusCode, FLError error) {
                        }
                    });
                }
            } else if (FLTriggerManager.this.isTriggerKeyView(this.trigger)) {
                Class activityClass = FLTriggerManager.this.binderKeyActivity.get(this.trigger.categoryView);
                Class fragmentClass = FLTriggerManager.this.binderKeyFragment.get(this.trigger.categoryView);
                Activity currentActivity = FloozApplication.getInstance().getCurrentActivity();

                if (FLTriggerManager.this.isTriggerKeyViewRoot(this.trigger)) {
                    Integer rootId = FLTriggerManager.this.isViewClassRoot(fragmentClass);

                    if (rootId >= 0) {
                        if (currentActivity != null && currentActivity instanceof HomeActivity) {
                            HomeActivity homeActivity = (HomeActivity) currentActivity;

                            HomeActivity.TabID tabId = HomeActivity.tabIDFromIndex(rootId);
                            if (tabId != HomeActivity.TabID.NONE) {
                                homeActivity.changeCurrentTab(tabId, true, new Runnable() {
                                    @Override
                                    public void run() {
                                        FLTriggerManager.this.executeTriggerList(trigger.triggers);
                                    }
                                });
                            }
                        } else if (currentActivity != null) {
                            pendingShowTrigger = this.trigger;
                            Intent intent = new Intent();
                            intent.setClass(currentActivity, HomeActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            currentActivity.startActivity(intent);
                            currentActivity.overridePendingTransition(R.anim.slide_up, android.R.anim.fade_out);
                        } else {
                            pendingShowTrigger = this.trigger;
                            Intent intent = new Intent();
                            intent.setClass(FloozApplication.getAppContext(), HomeActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            FloozApplication.getInstance().startActivity(intent);
                        }
                    } else if (currentActivity != null) {
                        pendingShowTrigger = this.trigger;
                        Intent intent = new Intent();
                        intent.setClass(FloozApplication.getAppContext(), activityClass);

                        if (this.trigger.data != null)
                            intent.putExtra("triggerData", this.trigger.data.toString());

                        currentActivity.startActivity(intent);
                        currentActivity.overridePendingTransition(R.anim.slide_up, android.R.anim.fade_out);
                    } else {
                        pendingShowTrigger = this.trigger;
                        Intent intent = new Intent();
                        intent.setClass(FloozApplication.getAppContext(), activityClass);

                        if (this.trigger.data != null)
                            intent.putExtra("triggerData", this.trigger.data.toString());

                        FloozApplication.getInstance().startActivity(intent);
                    }
                } else if (FLTriggerManager.this.isTriggerKeyViewPush(this.trigger) && fragmentClass != null) {
                    if (currentActivity != null && currentActivity instanceof HomeActivity) {
                        HomeActivity homeActivity = (HomeActivity) currentActivity;

                        try {
                            TabBarFragment newFragment = (TabBarFragment) fragmentClass.newInstance();
                            newFragment.triggerData = this.trigger.data;

                            homeActivity.pushFragmentInCurrentTab(newFragment, new Runnable() {
                                @Override
                                public void run() {
                                    FLTriggerManager.this.executeTriggerList(trigger.triggers);
                                }
                            });
                        } catch (InstantiationException e) {
                            e.printStackTrace();
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        }
                    } else {
                        if (currentActivity != null) {
                            pendingShowTrigger = this.trigger;
                            Intent intent = new Intent();
                            intent.setClass(FloozApplication.getAppContext(), activityClass);

                            if (this.trigger.data != null)
                                intent.putExtra("triggerData", this.trigger.data.toString());

                            currentActivity.startActivity(intent);
                            currentActivity.overridePendingTransition(R.anim.slide_up, android.R.anim.fade_out);
                        } else {
                            pendingShowTrigger = this.trigger;
                            Intent intent = new Intent();
                            intent.setClass(FloozApplication.getAppContext(), activityClass);

                            if (this.trigger.data != null)
                                intent.putExtra("triggerData", this.trigger.data.toString());

                            FloozApplication.getInstance().startActivity(intent);
                        }
                    }
                } else if (FLTriggerManager.this.isTriggerKeyModal(this.trigger)) {
                    if (currentActivity != null) {
                        pendingShowTrigger = this.trigger;
                        Intent intent = new Intent();
                        intent.setClass(FloozApplication.getAppContext(), activityClass);

                        if (this.trigger.data != null)
                            intent.putExtra("triggerData", this.trigger.data.toString());

                        currentActivity.startActivity(intent);
                        currentActivity.overridePendingTransition(R.anim.slide_up, android.R.anim.fade_out);
                    } else {
                        pendingShowTrigger = this.trigger;
                        Intent intent = new Intent();
                        intent.setClass(FloozApplication.getAppContext(), activityClass);

                        if (this.trigger.data != null)
                            intent.putExtra("triggerData", this.trigger.data.toString());

                        FloozApplication.getInstance().startActivity(intent);
                    }
                }
            }
        }
    };

    private Boolean isTriggerKeyView(FLTrigger trigger) {
        return (trigger != null && trigger.categoryView != null && this.binderKeyActivity.containsKey(trigger.categoryView));
    }

    private Boolean isTriggerKeyModal(FLTrigger trigger) {
        return (trigger != null && trigger.categoryView != null && this.binderKeyActivity.containsKey(trigger.categoryView) && this.binderKeyType.get(trigger.categoryView).contentEquals("modal"));
    }

    private Boolean isTriggerKeyViewPush(FLTrigger trigger) {
        return (trigger != null && trigger.categoryView != null && this.binderKeyFragment.containsKey(trigger.categoryView) && this.binderKeyType.get(trigger.categoryView).contentEquals("push"));
    }

    private Boolean isTriggerKeyViewRoot(FLTrigger trigger) {
        return (trigger != null && trigger.categoryView != null && this.binderKeyFragment.containsKey(trigger.categoryView) && this.binderKeyType.get(trigger.categoryView).contentEquals("root"));
    }

    private Integer isViewClassRoot(Class viewClass) {
        HomeActivity activity = HomeActivity.getInstance();

        if (activity != null) {
            int i = 0;
            for (ArrayList<TabBarFragment> historyTab : activity.fullTabHistory) {
                TabBarFragment fragment = historyTab.get(0);

                if (fragment.getClass().equals(viewClass))
                    return i;

                i++;
            }
        }

        return -1;
    }

    private ActionTask syncActionHandler = new ActionTask() {
        @Override
        public void run() {
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

                        FLTriggerManager.this.executeTriggerList(trigger.triggers);
                    }
                    break;
                case "timeline":
                    FloozApplication.performLocalNotification(CustomNotificationIntents.reloadTimeline());
                    FLTriggerManager.this.executeTriggerList(trigger.triggers);
                    break;
                case "invitation":
                    FloozRestClient.getInstance().getInvitationText(new FloozHttpResponseHandler() {
                        @Override
                        public void success(Object response) {
                            FLTriggerManager.this.executeTriggerList(trigger.triggers);
                        }

                        @Override
                        public void failure(int statusCode, FLError error) {
                            FLTriggerManager.this.executeTriggerList(trigger.triggers);
                        }
                    });
                    break;
                case "flooz":
                    FloozApplication.performLocalNotification(CustomNotificationIntents.reloadTimeline());
                    FLTriggerManager.this.executeTriggerList(trigger.triggers);
                    break;
                case "profile":
                    FloozRestClient.getInstance().updateCurrentUser(new FloozHttpResponseHandler() {
                        @Override
                        public void success(Object response) {
                            FLTriggerManager.this.executeTriggerList(trigger.triggers);
                        }

                        @Override
                        public void failure(int statusCode, FLError error) {
                            FLTriggerManager.this.executeTriggerList(trigger.triggers);
                        }
                    });
                    break;
                case "notifs":
                    FloozRestClient.getInstance().updateNotificationFeed(new FloozHttpResponseHandler() {
                        @Override
                        public void success(Object response) {
                            FLTriggerManager.this.executeTriggerList(trigger.triggers);
                        }

                        @Override
                        public void failure(int statusCode, FLError error) {
                            FLTriggerManager.this.executeTriggerList(trigger.triggers);
                        }
                    });
                    break;
                default:
                    break;
            }
        }
    };

    private void loadBinderKeyActivity() {
        this.binderKeyActivity = new HashMap<String, Class>() {{
            put("app:cashout", CashoutActivity.class);
            put("app:flooz", NewTransactionActivity.class);
            put("app:pot", NewCollectActivity.class);
            put("app:promo", SponsorActivity.class);
            put("app:search", SearchActivity.class);
            put("app:notifs", NotificationActivity.class);
            put("app:invitation", ShareAppActivity.class);
            put("app:profile", UserProfileActivity.class);
            put("app:timeline", HomeActivity.class);
            put("auth:code", AuthenticationActivity.class);
            put("card:3ds", Secure3DActivity.class);
            put("card:card", CreditCardSettingsActivity.class);
            put("code:set", SetSecureCodeActivity.class);
            put("friend:pending", FriendRequestActivity.class);
            put("profile:user", UserProfileActivity.class);
            put("profile:edit", EditProfileActivity.class);
            put("settings:iban", BankSettingsActivity.class);
            put("settings:identity", IdentitySettingsActivity.class);
            put("settings:notifs", NotificationsSettingsActivity.class);
            put("settings:privacy", PrivacySettingsActivity.class);
            put("settings:documents", DocumentsSettingsActivity.class);
            put("timeline:flooz", TransactionActivity.class);
            put("web:web", WebContentActivity.class);
            put("phone:validate", ValidateSMSActivity.class);
        }};
    }

    private void loadBinderKeyFragment() {
        this.binderKeyFragment = new HashMap<String, Class>() {{
            put("app:cashout", CashoutFragment.class);
            put("app:promo", SponsorFragment.class);
            put("app:search", SearchFragment.class);
            put("app:notifs", NotificationsFragment.class);
            put("app:invitation", ShareFragment.class);
            put("app:profile", ProfileCardFragment.class);
            put("app:timeline", TimelineFragment.class);
            put("card:card", CreditCardFragment.class);
            put("friend:pending", FriendRequestFragment.class);
            put("profile:user", ProfileCardFragment.class);
            put("settings:iban", BankFragment.class);
            put("settings:identity", IdentityFragment.class);
            put("settings:notifs", NotifsSettingsFragment.class);
            put("settings:privacy", PrivacyFragment.class);
            put("settings:documents", DocumentsFragment.class);
            put("timeline:flooz", TransactionCardFragment.class);
            put("web:web", WebFragment.class);
        }};
    }

    private void loadBinderKeyType() {
        this.binderKeyType = new HashMap<String, String>() {{
            put("app:cashout", "modal");
            put("app:flooz", "modal");
            put("app:pot", "modal");
            put("app:promo", "modal");
            put("app:search", "modal");
            put("app:notifs", "root");
            put("app:invitation", "root");
            put("app:profile", "root");
            put("app:timeline", "root");
            put("auth:code", "modal");
            put("card:3ds", "modal");
            put("card:card", "modal");
            put("code:set", "modal");
            put("friend:pending", "modal");
            put("profile:user", "push");
            put("profile:edit", "modal");
            put("settings:iban", "modal");
            put("settings:identity", "modal");
            put("settings:notifs", "modal");
            put("settings:privacy", "modal");
            put("settings:documents", "modal");
            put("timeline:flooz", "push");
            put("web:web", "modal");
            put("phone:validate", "modal");
        }};
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        if (activity instanceof BaseActivity) {
            ((BaseActivity)activity).currentState = BaseActivity.FLActivityState.FLActivityStateCreated;
        }
    }

    @Override
    public void onActivityStarted(Activity activity) {
        if (activity instanceof BaseActivity) {
            ((BaseActivity)activity).currentState = BaseActivity.FLActivityState.FLActivityStateStarted;
        }

        if (this.pendingShowTrigger != null) {
            if (FLTriggerManager.this.binderKeyActivity.containsKey(this.pendingShowTrigger.categoryView)) {
                Class wantedActivityClass = FLTriggerManager.this.binderKeyActivity.get(this.pendingShowTrigger.categoryView);

                if (activity.getClass().equals(wantedActivityClass)) {
                    FLTriggerManager.this.executeTriggerList(this.pendingShowTrigger.triggers);
                    this.pendingShowTrigger = null;
                } else if (activity instanceof HomeActivity) {
                    if (FLTriggerManager.this.isTriggerKeyViewRoot(this.pendingShowTrigger)) {
                        Class wantedFragmentClass = FLTriggerManager.this.binderKeyFragment.get(this.pendingShowTrigger.categoryView);
                        Integer rootId = FLTriggerManager.this.isViewClassRoot(wantedFragmentClass);

                        if (rootId >= 0) {
                            HomeActivity homeActivity = (HomeActivity) activity;

                            HomeActivity.TabID tabId = HomeActivity.tabIDFromIndex(rootId);
                            if (tabId != HomeActivity.TabID.NONE) {
                                homeActivity.changeCurrentTab(tabId, true, new Runnable() {
                                    @Override
                                    public void run() {
                                        FLTriggerManager.this.executeTriggerList(pendingShowTrigger.triggers);
                                        pendingShowTrigger = null;
                                    }
                                });
                            } else
                                this.pendingShowTrigger = null;
                        } else
                            this.pendingShowTrigger = null;
                    } else
                        this.pendingShowTrigger = null;
                } else
                    this.pendingShowTrigger = null;
            } else
                this.pendingShowTrigger = null;
        }
    }

    @Override
    public void onActivityResumed(Activity activity) {
        if (activity instanceof BaseActivity) {
            ((BaseActivity)activity).currentState = BaseActivity.FLActivityState.FLActivityStateResumed;
        }
    }

    @Override
    public void onActivityPaused(Activity activity) {
        if (activity instanceof BaseActivity) {
            ((BaseActivity)activity).currentState = BaseActivity.FLActivityState.FLActivityStatePaused;
        }
    }

    @Override
    public void onActivityStopped(Activity activity) {
        if (activity instanceof BaseActivity) {
            ((BaseActivity)activity).currentState = BaseActivity.FLActivityState.FLActivityStateStopped;
        }

        if (this.pendingHideTrigger != null) {
            if (FLTriggerManager.this.binderKeyActivity.containsKey(this.pendingHideTrigger.categoryView)) {
                Class wantedActivityClass = FLTriggerManager.this.binderKeyActivity.get(this.pendingHideTrigger.categoryView);

                if (activity.getClass().equals(wantedActivityClass)) {
                    FLTriggerManager.this.executeTriggerList(this.pendingHideTrigger.triggers);

                    if (activity instanceof AuthenticationActivity && this.pendingShowTrigger.categoryView.contentEquals("auth:code")) {
                        AuthenticationActivity authActivity = (AuthenticationActivity) activity;
                    }
                }
            }
            this.pendingHideTrigger = null;
        }
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

    }

    @Override
    public void onActivityDestroyed(Activity activity) {
        if (activity instanceof BaseActivity) {
            ((BaseActivity)activity).currentState = BaseActivity.FLActivityState.FLActivityStateDestroyed;
        }
    }
}
