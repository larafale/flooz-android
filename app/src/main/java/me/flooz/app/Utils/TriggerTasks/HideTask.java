package me.flooz.app.Utils.TriggerTasks;

import android.app.Activity;

import org.json.JSONException;
import org.json.JSONObject;

import me.flooz.app.App.FloozApplication;
import me.flooz.app.Model.FLTrigger;
import me.flooz.app.R;
import me.flooz.app.UI.Activity.HomeActivity;
import me.flooz.app.UI.Fragment.Home.TabFragments.TabBarFragment;
import me.flooz.app.Utils.FLTriggerManager;
import me.flooz.app.Utils.TriggerTasks.ActionTask;

public class HideTask extends ActionTask {

    public HideTask() { super(); }

    @Override public void run() {

        if (this.trigger.categoryView.contentEquals("app:flooz")) {
            try {
                JSONObject tmp = trigger.jsonData;
                tmp.put("key", "popup:advanced:hide");

                JSONObject tmpData = tmp.optJSONObject("data");
                if (tmpData == null)
                    tmpData = new JSONObject();

                tmpData.put("noAnim", true);

                tmp.put("data", tmpData);

                FLTrigger newTrigger = new FLTrigger(tmp);
                FLTriggerManager.getInstance().executeTrigger(newTrigger);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        Activity currentActivity = FloozApplication.getInstance().getCurrentActivity();

        if (currentActivity != null) {
            if (currentActivity instanceof HomeActivity) {
                TabBarFragment currentFragment = ((HomeActivity)currentActivity).currentFragment;

                if (FLTriggerManager.getInstance().binderKeyFragment.containsKey(this.trigger.categoryView)) {
                    Class wantedFragmentClass = FLTriggerManager.getInstance().binderKeyFragment.get(this.trigger.categoryView);

                    if (currentFragment.getClass().equals(wantedFragmentClass) && ((HomeActivity)currentActivity).currentTabHistory.size() > 1) {
                        ((HomeActivity)currentActivity).popFragmentInCurrentTab(new Runnable() {
                            @Override
                            public void run() {
                                FLTriggerManager.getInstance().executeTriggerList(trigger.triggers);
                            }
                        });
                    } else
                        FLTriggerManager.getInstance().executeTriggerList(this.trigger.triggers);
                } else
                    FLTriggerManager.getInstance().executeTriggerList(this.trigger.triggers);
            } else if (FLTriggerManager.getInstance().binderKeyActivity.containsKey(this.trigger.categoryView)) {
                Class wantedActivityClass = FLTriggerManager.getInstance().binderKeyActivity.get(this.trigger.categoryView);

                if (currentActivity.getClass().equals(wantedActivityClass)) {
                    FLTriggerManager.getInstance().pendingHideTrigger = this.trigger;
                    currentActivity.finish();

                    if (this.trigger.data != null && this.trigger.data.has("noAnim") && this.trigger.data.optBoolean("noAnim"))
                        return;

                    currentActivity.overridePendingTransition(android.R.anim.fade_in, R.anim.slide_down);
                } else
                    FLTriggerManager.getInstance().executeTriggerList(this.trigger.triggers);
            }
        }
    }
}
