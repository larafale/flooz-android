package me.flooz.app.Utils.TriggerTasks;

import me.flooz.app.Network.FloozRestClient;
import me.flooz.app.Utils.FLTriggerManager;

public class AskTask extends ActionTask {

    public AskTask() { super(); }

    @Override public void run() {
        if (this.trigger.category.contentEquals("fb")) {
            FloozRestClient.getInstance().connectFacebook();
            FLTriggerManager.getInstance().executeTriggerList(this.trigger.triggers);
        }
    }
}
