package me.flooz.app.Utils.TriggerTasks;

import me.flooz.app.Network.FloozRestClient;
import me.flooz.app.Utils.FLTriggerManager;
import me.flooz.app.Utils.TriggerTasks.ActionTask;

public class ClearTask extends ActionTask {

    public ClearTask() { super(); }

    @Override public void run() {
        if (this.trigger.category.contentEquals("code")) {
            FloozRestClient.getInstance().clearSecureCode();
            FLTriggerManager.getInstance().executeTriggerList(trigger.triggers);
        }
    }
}
