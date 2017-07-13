package me.flooz.app.Utils;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.flooz.app.App.FloozApplication;
import me.flooz.app.Model.FLTrigger;
import me.flooz.app.UI.Activity.AdvancedPopupActivity;
import me.flooz.app.UI.Activity.AuthenticationActivity;
import me.flooz.app.UI.Activity.BaseActivity;
import me.flooz.app.UI.Activity.CashoutActivity;
import me.flooz.app.UI.Activity.CashoutHistoryActivity;
import me.flooz.app.UI.Activity.CollectActivity;
import me.flooz.app.UI.Activity.CollectParticipantActivity;
import me.flooz.app.UI.Activity.CollectParticipationActivity;
import me.flooz.app.UI.Activity.EditProfileActivity;
import me.flooz.app.UI.Activity.FriendRequestActivity;
import me.flooz.app.UI.Activity.HomeActivity;
import me.flooz.app.UI.Activity.NewCollectActivity;
import me.flooz.app.UI.Activity.NewTransactionActivity;
import me.flooz.app.UI.Activity.NotificationActivity;
import me.flooz.app.UI.Activity.PaymentAudiotelActivity;
import me.flooz.app.UI.Activity.PaymentSourceActivity;
import me.flooz.app.UI.Activity.RegisterCardActivity;
import me.flooz.app.UI.Activity.ScopePickerActivity;
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
import me.flooz.app.UI.Activity.ShareCollectAcivity;
import me.flooz.app.UI.Activity.ShopHistoryActivity;
import me.flooz.app.UI.Activity.ShopItemActivity;
import me.flooz.app.UI.Activity.ShopListActivity;
import me.flooz.app.UI.Activity.ShopParamActivity;
import me.flooz.app.UI.Activity.SponsorActivity;
import me.flooz.app.UI.Activity.TransactionActivity;
import me.flooz.app.UI.Activity.UserPickerActivity;
import me.flooz.app.UI.Activity.UserProfileActivity;
import me.flooz.app.UI.Activity.ValidateSMSActivity;
import me.flooz.app.UI.Activity.WebContentActivity;
import me.flooz.app.UI.Fragment.Home.TabFragments.BankFragment;
import me.flooz.app.UI.Fragment.Home.TabFragments.CashoutFragment;
import me.flooz.app.UI.Fragment.Home.TabFragments.CashoutHistoryFragment;
import me.flooz.app.UI.Fragment.Home.TabFragments.CollectFragment;
import me.flooz.app.UI.Fragment.Home.TabFragments.CollectParticipantFragment;
import me.flooz.app.UI.Fragment.Home.TabFragments.CollectParticipationFragment;
import me.flooz.app.UI.Fragment.Home.TabFragments.CreditCardFragment;
import me.flooz.app.UI.Fragment.Home.TabFragments.DocumentsFragment;
import me.flooz.app.UI.Fragment.Home.TabFragments.FriendRequestFragment;
import me.flooz.app.UI.Fragment.Home.TabFragments.IdentityFragment;
import me.flooz.app.UI.Fragment.Home.TabFragments.NotificationsFragment;
import me.flooz.app.UI.Fragment.Home.TabFragments.NotifsSettingsFragment;
import me.flooz.app.UI.Fragment.Home.TabFragments.PrivacyFragment;
import me.flooz.app.UI.Fragment.Home.TabFragments.ProfileCardFragment;
import me.flooz.app.UI.Fragment.Home.TabFragments.RegisterCardFragment;
import me.flooz.app.UI.Fragment.Home.TabFragments.SearchFragment;
import me.flooz.app.UI.Fragment.Home.TabFragments.ShareFragment;
import me.flooz.app.UI.Fragment.Home.TabFragments.ShopHistoryFragment;
import me.flooz.app.UI.Fragment.Home.TabFragments.SponsorFragment;
import me.flooz.app.UI.Fragment.Home.TabFragments.TabBarFragment;
import me.flooz.app.UI.Fragment.Home.TabFragments.TimelineFragment;
import me.flooz.app.UI.Fragment.Home.TabFragments.TransactionCardFragment;
import me.flooz.app.UI.Fragment.Home.TabFragments.WebFragment;
import me.flooz.app.Utils.TriggerTasks.ActionTask;
import me.flooz.app.Utils.TriggerTasks.AskTask;
import me.flooz.app.Utils.TriggerTasks.CallTask;
import me.flooz.app.Utils.TriggerTasks.ClearTask;
import me.flooz.app.Utils.TriggerTasks.HideTask;
import me.flooz.app.Utils.TriggerTasks.LoginTask;
import me.flooz.app.Utils.TriggerTasks.LogoutTask;
import me.flooz.app.Utils.TriggerTasks.OpenTask;
import me.flooz.app.Utils.TriggerTasks.PickerTask;
import me.flooz.app.Utils.TriggerTasks.SendTask;
import me.flooz.app.Utils.TriggerTasks.ShowTask;
import me.flooz.app.Utils.TriggerTasks.SyncTask;

public class FLTriggerManager implements Application.ActivityLifecycleCallbacks {

    public Map<FLTrigger.FLTriggerAction, Class> binderActionFunction;
    public Map<String, Class> binderKeyActivity;
    public Map<String, Class> binderKeyFragment;
    public Map<String, String> binderKeyType;

    public FLTrigger pendingHideTrigger;
    public FLTrigger pendingShowTrigger;

    private static FLTriggerManager instance;

    public static FLTriggerManager getInstance() {
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
            ActionTask runnable;
            try {
                Class taskClass = this.binderActionFunction.get(trigger.action);
                runnable = (ActionTask) taskClass.newInstance();

                runnable.trigger = trigger;

                if (trigger.delay.doubleValue() > 0) {
                    Handler handler = new Handler(Looper.getMainLooper());
                    handler.postDelayed(runnable, (int) (trigger.delay.doubleValue() * 1000));
                } else {
                    Handler handler = new Handler(Looper.getMainLooper());
                    handler.post(runnable);
                }
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    public void loadBinderActionFunction() {
        this.binderActionFunction = new HashMap<FLTrigger.FLTriggerAction, Class>() {{
            put(FLTrigger.FLTriggerAction.FLTriggerActionAsk, AskTask.class);
            put(FLTrigger.FLTriggerAction.FLTriggerActionCall, CallTask.class);
            put(FLTrigger.FLTriggerAction.FLTriggerActionClear, ClearTask.class);
            put(FLTrigger.FLTriggerAction.FLTriggerActionHide, HideTask.class);
            put(FLTrigger.FLTriggerAction.FLTriggerActionLogin, LoginTask.class);
            put(FLTrigger.FLTriggerAction.FLTriggerActionLogout, LogoutTask.class);
            put(FLTrigger.FLTriggerAction.FLTriggerActionOpen, OpenTask.class);
            put(FLTrigger.FLTriggerAction.FLTriggerActionSend, SendTask.class);
            put(FLTrigger.FLTriggerAction.FLTriggerActionShow, ShowTask.class);
            put(FLTrigger.FLTriggerAction.FLTriggerActionSync, SyncTask.class);
            put(FLTrigger.FLTriggerAction.FLTriggerActionPicker, PickerTask.class);
        }};
    }

    public Boolean isTriggerKeyView(FLTrigger trigger) {
        return (trigger != null && trigger.categoryView != null && this.binderKeyActivity.containsKey(trigger.categoryView));
    }

    public Boolean isTriggerKeyModal(FLTrigger trigger) {
        return (trigger != null && trigger.categoryView != null && this.binderKeyActivity.containsKey(trigger.categoryView) && this.binderKeyType.get(trigger.categoryView).contentEquals("modal"));
    }

    public Boolean isTriggerKeyViewPush(FLTrigger trigger) {
        return (trigger != null && trigger.categoryView != null && this.binderKeyFragment.containsKey(trigger.categoryView) && this.binderKeyType.get(trigger.categoryView).contentEquals("push"));
    }

    public Boolean isTriggerKeyViewRoot(FLTrigger trigger) {
        return (trigger != null && trigger.categoryView != null && this.binderKeyFragment.containsKey(trigger.categoryView) && this.binderKeyType.get(trigger.categoryView).contentEquals("root"));
    }

    public Integer isViewClassRoot(Class viewClass) {
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

    public void loadBinderKeyActivity() {
        this.binderKeyActivity = new HashMap<String, Class>() {{
            put("app:cashout", CashoutActivity.class);
            put("app:flooz", NewTransactionActivity.class);
//            put("app:cashin", CashinActivity.class);
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
//            put("cashin:card", CashinCreditCardActivity.class);
//            put("cashin:audiotel", CashinAudiotelActivity.class);
            put("cashout:history", CashoutHistoryActivity.class);
            put("code:set", SetSecureCodeActivity.class);
            put("friend:pending", FriendRequestActivity.class);
            put("pay:source", PaymentSourceActivity.class);
            put("pay:audiotel", PaymentAudiotelActivity.class);
            put("pay:card", CreditCardSettingsActivity.class);
            put("profile:user", UserProfileActivity.class);
            put("profile:edit", EditProfileActivity.class);
            put("settings:iban", BankSettingsActivity.class);
            put("settings:identity", IdentitySettingsActivity.class);
            put("settings:notifs", NotificationsSettingsActivity.class);
            put("settings:privacy", PrivacySettingsActivity.class);
            put("settings:documents", DocumentsSettingsActivity.class);
            put("timeline:flooz", TransactionActivity.class);
            put("timeline:pot", CollectActivity.class);
            put("web:web", WebContentActivity.class);
            put("phone:validate", ValidateSMSActivity.class);
            put("pot:invitation", ShareCollectAcivity.class);
            put("pot:participant", CollectParticipantActivity.class);
            put("pot:participation", CollectParticipationActivity.class);
            put("user:picker", UserPickerActivity.class);
            put("scope:picker", ScopePickerActivity.class);
            put("popup:advanced", AdvancedPopupActivity.class);
            put("shop:list", ShopListActivity.class);
            put("shop:item", ShopItemActivity.class);
            put("shop:param", ShopParamActivity.class);
            put("shop:history", ShopHistoryActivity.class);
            put("web:psp", RegisterCardActivity.class);
        }};
    }

    public void loadBinderKeyFragment() {
        this.binderKeyFragment = new HashMap<String, Class>() {{
            put("app:cashout", CashoutFragment.class);
            put("app:promo", SponsorFragment.class);
            put("app:search", SearchFragment.class);
            put("app:notifs", NotificationsFragment.class);
            put("app:invitation", ShareFragment.class);
            put("app:profile", ProfileCardFragment.class);
            put("app:timeline", TimelineFragment.class);
            put("card:card", CreditCardFragment.class);
            put("cashout:history", CashoutHistoryFragment.class);
            put("friend:pending", FriendRequestFragment.class);
            put("profile:user", ProfileCardFragment.class);
            put("settings:iban", BankFragment.class);
            put("settings:identity", IdentityFragment.class);
            put("settings:notifs", NotifsSettingsFragment.class);
            put("settings:privacy", PrivacyFragment.class);
            put("settings:documents", DocumentsFragment.class);
            put("timeline:flooz", TransactionCardFragment.class);
            put("timeline:pot", CollectFragment.class);
            put("web:web", WebFragment.class);
            put("pot:participant", CollectParticipantFragment.class);
            put("pot:participation", CollectParticipationFragment.class);
            put("shop:history", ShopHistoryFragment.class);
            put("web:psp", RegisterCardFragment.class);
        }};
    }

    public void loadBinderKeyType() {
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
            put("cashout:history", "push");
            put("code:set", "modal");
            put("friend:pending", "modal");
            put("pay:source", "modal");
            put("pay:audiotel", "modal");
            put("pay:card", "modal");
            put("profile:user", "push");
            put("profile:edit", "modal");
            put("settings:iban", "modal");
            put("settings:identity", "modal");
            put("settings:notifs", "modal");
            put("settings:privacy", "modal");
            put("settings:documents", "modal");
            put("timeline:flooz", "push");
            put("timeline:pot", "push");
            put("web:web", "modal");
            put("phone:validate", "modal");
            put("pot:invitation", "modal");
            put("pot:participant", "push");
            put("pot:participation", "push");
            put("user:picker", "modal");
            put("scope:picker", "modal");
            put("popup:advanced", "modal");
            put("shop:list", "modal");
            put("shop:item", "modal");
            put("shop:param", "modal");
            put("shop:history", "push");
            put("web:psp", "modal");
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
