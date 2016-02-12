package me.flooz.app.Utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.SyncHttpClient;

import org.json.JSONArray;
import org.json.JSONException;

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
import me.flooz.app.Model.FLTrigger;
import me.flooz.app.Network.FloozHttpResponseHandler;
import me.flooz.app.Network.FloozRestClient;
import me.flooz.app.R;
import me.flooz.app.UI.Activity.AuthenticationActivity;
import me.flooz.app.UI.Activity.CashoutActivity;
import me.flooz.app.UI.Activity.EditProfileActivity;
import me.flooz.app.UI.Activity.HomeActivity;
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
                FLTrigger tmp = new FLTrigger(json.optJSONObject(i));

                if (tmp.valid)
                    ret.add(tmp);
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
                                FLTriggerManager.this.executeTriggerList(trigger.triggers);
                            }

                            @Override
                            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                                FLTriggerManager.this.executeTriggerList(trigger.triggers);
                            }
                        };

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

                        if (this.trigger.data.has("body")) {
                            try {
                                param = JSONHelper.toMap(this.trigger.data.optJSONObject("body"));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        String url = this.trigger.data.optString("url");

                        if (url.charAt(0) != '/')
                            url = "/" + url;

                        FloozRestClient.getInstance().request(url, method, param, new FloozHttpResponseHandler() {
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
            if (this.trigger.category.contentEquals("app")) {
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
            if (this.trigger.category.contentEquals("http")) {
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

    private ActionTask showActionHandler = new ActionTask() {
        @Override
        public void run() {
            if (this.trigger.categoryView.contentEquals("app:signup")) {
                if (FloozApplication.getInstance().getCurrentActivity() instanceof StartActivity) {
                    StartActivity activity = (StartActivity) FloozApplication.getInstance().getCurrentActivity();

                    try {
                        Map<String, Object> userData = JSONHelper.toMap(this.trigger.data);

                        if (userData.containsKey("fb") && ((Map)userData.get("fb")).containsKey("id"))
                            userData.put("avatarURL", "https://graph.facebook.com/" + ((Map)userData.get("fb")).get("id") + "/picture");

                        activity.updateUserData(userData);

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

            } else if (this.trigger.categoryView.contentEquals("auth:code")) {

            } else if (this.trigger.categoryView.contentEquals("profile:user")) {

            } else if (this.trigger.categoryView.contentEquals("timeline:flooz")) {

            } else {

            }


//            if ([trigger.viewCaregory isEqualToString:@"app:signup"]) {
//                [appDelegate showSignupWithUser:trigger.data];
//                [self executeTriggerList:trigger.triggers];
//            } else if ([trigger.viewCaregory isEqualToString:@"app:popup"]) {
//                if (trigger.data) {
//                    [[[FLPopupTrigger alloc] initWithData:trigger.data] show:^{
//                        [self executeTriggerList:trigger.triggers];
//                    }];
//                }
//            } else if ([trigger.viewCaregory isEqualToString:@"app:sms"]) {
//                if (trigger.data && trigger.data[@"recipients"] && trigger.data[@"body"]) {
//                    if ([MFMessageComposeViewController canSendText]) {
//                        [[Flooz sharedInstance] showLoadView];
//                        MFMessageComposeViewController *message = [[MFMessageComposeViewController alloc] init];
//                        message.messageComposeDelegate = self;
//
//                        [message setRecipients:trigger.data[@"recipients"]];
//                        [message setBody:trigger.data[@"body"]];
//
//                        message.modalPresentationStyle = UIModalPresentationPageSheet;
//                        UIViewController *tmp = [appDelegate myTopViewController];
//
//                        self.smsTrigger = trigger;
//
//                        [tmp presentViewController:message animated:YES completion:^{
//                            [[Flooz sharedInstance] hideLoadView];
//                        }];
//                    } else {
//                        if (trigger.data[@"failureTriggers"]) {
//                            [self executeTriggerList:[FLTriggerManager convertDataInList:trigger.data[@"failureTriggers"]]];
//                        }
//                    }
//                }
//            } else if ([trigger.viewCaregory isEqualToString:@"auth:code"]) {
//                [[Flooz sharedInstance] showLoadView];
//
//                CompleteBlock completeBlock = ^{
//                    dispatch_async(dispatch_get_main_queue(), ^{
//                            [[Flooz sharedInstance] hideLoadView];
//
//                    [self executeTriggerList:trigger.triggers];
//
//                    if (trigger.data && trigger.data[@"successTriggers"]) {
//
//                        if ([trigger.data[@"successTriggers"] isKindOfClass:[NSArray class]]) {
//                            [self executeTriggerList:[self.class convertDataInList:trigger.data[@"successTriggers"]]];
//                        } else if ([trigger.data[@"successTriggers"] isKindOfClass:[NSDictionary class]]) {
//                            FLTrigger *tmp = [[FLTrigger alloc] initWithJson:trigger.data[@"successTriggers"]];
//
//                            if (tmp) {
//                                [self executeTrigger:tmp];
//                            }
//                        }
//                    }
//                    });
//                };
//
//                if ([SecureCodeViewController canUseTouchID])
//                [SecureCodeViewController useToucheID:completeBlock passcodeCallback:^{
//                    dispatch_async(dispatch_get_main_queue(), ^{
//                            SecureCodeViewController *controller = [SecureCodeViewController new];
//                    controller.completeBlock = completeBlock;
//                    [[appDelegate myTopViewController] presentViewController:[[UINavigationController alloc] initWithRootViewController:controller] animated:YES completion:^{
//                        [[Flooz sharedInstance] hideLoadView];
//                        [self executeTriggerList:trigger.triggers];
//                    }];
//                    });
//                } cancelCallback:^{
//                    dispatch_async(dispatch_get_main_queue(), ^{
//                            [self executeTriggerList:trigger.triggers];
//                    [[Flooz sharedInstance] hideLoadView];
//                    });
//                }];
//                else {
//                    dispatch_async(dispatch_get_main_queue(), ^{
//                            SecureCodeViewController *controller = [SecureCodeViewController new];
//                    controller.completeBlock = completeBlock;
//                    [[appDelegate myTopViewController] presentViewController:[[UINavigationController alloc] initWithRootViewController:controller] animated:YES completion:^{
//                        [[Flooz sharedInstance] hideLoadView];
//                        [self executeTriggerList:trigger.triggers];
//                    }];
//                    });
//                }
//            } else if ([trigger.viewCaregory isEqualToString:@"profile:user"]) {
//                if ([trigger.data objectForKey:@"nick"]) {
//                    FLUser *user = [[FLUser alloc] initWithJSON:trigger.data];
//                    [appDelegate showUser:user inController:nil completion:^{
//                        [self executeTriggerList:trigger.triggers];
//                    }];
//                } else if ([trigger.data objectForKey:@"_id"]) {
//                    [[Flooz sharedInstance] showLoadView];
//                    [[Flooz sharedInstance] getUserProfile:[trigger.data objectForKey:@"_id"] success:^(FLUser *result) {
//                        if (result) {
//                            [appDelegate showUser:result inController:nil completion:^{
//                                [self executeTriggerList:trigger.triggers];
//                            }];
//                        }
//                    } failure:nil];
//                }
//            } else if ([trigger.viewCaregory isEqualToString:@"timeline:flooz"]) {
//                NSString *resourceID = trigger.data[@"_id"];
//
//                if (resourceID) {
//                    [[Flooz sharedInstance] showLoadView];
//                    [[Flooz sharedInstance] transactionWithId:resourceID success: ^(id result) {
//                        FLTransaction *transaction = [[FLTransaction alloc] initWithJSON:[result objectForKey:@"item"]];
//                        [appDelegate showTransaction:transaction inController:appDelegate.currentController withIndexPath:nil focusOnComment:NO completion:^{
//                            [self executeTriggerList:trigger.triggers];
//                        }];
//                    }];
//                }
//
//            } else if ([self isTriggerKeyView:trigger]) {
//                Class controllerClass = [self.binderKeyView objectForKey:trigger.viewCaregory];
//
//                if ([self isTriggerKeyViewRoot:trigger]) {
//                    NSInteger rootId = [self isViewClassRoot:controllerClass];
//
//                    if (rootId >= 0) {
//                        FLTabBarController *tabBar = [appDelegate tabBarController];
//
//                        if (tabBar) {
//                            [appDelegate dismissControllersAnimated:YES completion:^{
//                                [tabBar setSelectedIndex:rootId];
//                                UINavigationController *navigationController = [[tabBar viewControllers] objectAtIndex:rootId];
//                                [navigationController popToRootViewControllerAnimated:YES];
//                                [self executeTriggerList:trigger.triggers];
//                            }];
//                        }
//                    } else {
//                        UIViewController *controller = [[controllerClass alloc] initWithTriggerData:trigger.data];
//
//                        FLNavigationController *navController = [[FLNavigationController alloc] initWithRootViewController:controller];
//
//                        UIViewController *tmp = [appDelegate myTopViewController];
//
//                        [tmp presentViewController:navController animated:YES completion:^{
//                            [self executeTriggerList:trigger.triggers];
//                        }];
//                    }
//                } else if ([self isTriggerKeyViewPush:trigger]) {
//                    UIViewController *tmp = [appDelegate myTopViewController];
//                    FLNavigationController *navController;
//
//                    if ([tmp isKindOfClass:[FLTabBarController class]]) {
//                        navController = [(FLTabBarController *)tmp selectedViewController];
//                    } else if ([tmp isKindOfClass:[FLNavigationController class]]) {
//                        navController = (FLNavigationController *)tmp;
//                    } else if ([tmp navigationController]) {
//                        navController = (FLNavigationController *)tmp.navigationController;
//                    }
//
//                    if (navController) {
//                        [navController pushViewController:[[controllerClass alloc] initWithTriggerData:trigger.data] animated:YES completion:^{
//                            [self executeTriggerList:trigger.triggers];
//                        }];
//                    }
//                } else if ([self isTriggerKeyViewModal:trigger]) {
//                    UIViewController *controller = [[controllerClass alloc] initWithTriggerData:trigger.data];
//
//                    FLNavigationController *navController = [[FLNavigationController alloc] initWithRootViewController:controller];
//
//                    UIViewController *tmp = [appDelegate myTopViewController];
//
//                    [tmp presentViewController:navController animated:YES completion:^{
//                        [self executeTriggerList:trigger.triggers];
//                    }];
//                }
//            }

        }
    };

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
            put("app:promo", SponsorActivity.class);
            put("app:search", SearchActivity.class);
            put("app:notifs", NotificationActivity.class);
            put("app:invitation", ShareAppActivity.class);
            put("app:profile", UserProfileActivity.class);
            put("app:timeline", HomeActivity.class);
            put("auth:code", AuthenticationActivity.class);
            put("card:3ds", Secure3DActivity.class);
            put("card:card", CreditCardSettingsActivity.class);
            put("code:set", HomeActivity.class);
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
//            put("code:set", .class);
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

    }

    @Override
    public void onActivityStarted(Activity activity) {

    }

    @Override
    public void onActivityResumed(Activity activity) {

    }

    @Override
    public void onActivityPaused(Activity activity) {

    }

    @Override
    public void onActivityStopped(Activity activity) {
        if (this.pendingHideTrigger != null) {
            if (FLTriggerManager.this.binderKeyActivity.containsKey(this.pendingHideTrigger.categoryView)) {
                Class wantedActivityClass = FLTriggerManager.this.binderKeyActivity.get(this.pendingHideTrigger.categoryView);

                if (activity.getClass().equals(wantedActivityClass)) {
                    FLTriggerManager.this.executeTriggerList(this.pendingHideTrigger.triggers);
                }
            }
        }
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

    }

    @Override
    public void onActivityDestroyed(Activity activity) {

    }
}
