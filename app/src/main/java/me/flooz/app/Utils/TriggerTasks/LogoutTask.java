package me.flooz.app.Utils.TriggerTasks;

import me.flooz.app.Network.FloozRestClient;
import me.flooz.app.Utils.FLTriggerManager;
import me.flooz.app.Utils.TriggerTasks.ActionTask;

public class LogoutTask extends ActionTask {

    public LogoutTask() { super(); }

    @Override public void run() {
        if (this.trigger.category.contentEquals("auth")) {
            FloozRestClient.getInstance().logout();
            FLTriggerManager.getInstance().executeTriggerList(trigger.triggers);
        }
    }
}
