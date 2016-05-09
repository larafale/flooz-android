package me.flooz.app.Utils.TriggerTasks;

import me.flooz.app.App.FloozApplication;
import me.flooz.app.Model.FLError;
import me.flooz.app.Network.FloozHttpResponseHandler;
import me.flooz.app.Network.FloozRestClient;
import me.flooz.app.Utils.FLTriggerManager;
import me.flooz.app.Utils.TriggerTasks.ActionTask;

public class LoginTask extends ActionTask {

    public LoginTask() { super(); }

    @Override public void run() {
        if (this.trigger.category.contentEquals("auth")) {
            if (this.trigger.data != null && this.trigger.data.has("token")) {
                FloozRestClient.getInstance().loginWithToken(this.trigger.data.optString("token"), new FloozHttpResponseHandler() {
                    @Override
                    public void success(Object response) {
                        FLTriggerManager.getInstance().executeTriggerList(trigger.triggers);
                    }

                    @Override
                    public void failure(int statusCode, FLError error) {
                        FLTriggerManager.getInstance().executeTriggerList(trigger.triggers);
                    }
                });
            } else {
                FloozApplication.getInstance().displayMainView();
                FLTriggerManager.getInstance().executeTriggerList(trigger.triggers);
            }
        }
    }
}
