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

/**
 * Created by Flooz on 2/8/16.
 */
public class FLTriggerManager implements Application.ActivityLifecycleCallbacks {

    abstract class ActionTask implements Runnable {
        public FLTrigger trigger;
        public abstract void run();
    }

    private Map<FLTrigger.FLTriggerAction, ActionTask> binderActionFunction;
    private static FLTriggerManager instance;

    public static FLTriggerManager getInstance()
    {
        if (instance == null)
            instance = new FLTriggerManager();
        return instance;
    }

    private FLTriggerManager() {
        this.loadBinderActionFunction();
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

        }
    };

    private ActionTask callActionHandler = new ActionTask() {
        @Override
        public void run() {

        }
    };

    private ActionTask clearActionHandler = new ActionTask() {
        @Override
        public void run() {

        }
    };

    private ActionTask hideActionHandler = new ActionTask() {
        @Override
        public void run() {

        }
    }
            ;
    private ActionTask loginActionHandler = new ActionTask() {
        @Override
        public void run() {

        }
    };

    private ActionTask logoutActionHandler = new ActionTask() {
        @Override
        public void run() {

        }
    };

    private ActionTask openActionHandler = new ActionTask() {
        @Override
        public void run() {

        }
    };

    private ActionTask showActionHandler = new ActionTask() {
        @Override
        public void run() {

        }
    };

    private ActionTask syncActionHandler = new ActionTask() {
        @Override
        public void run() {

        }
    };

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

    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

    }

    @Override
    public void onActivityDestroyed(Activity activity) {

    }
}
