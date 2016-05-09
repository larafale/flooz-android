package me.flooz.app.Utils.TriggerTasks;

import android.content.Intent;
import android.net.Uri;

import me.flooz.app.App.FloozApplication;
import me.flooz.app.Utils.FLTriggerManager;
import me.flooz.app.Utils.TriggerTasks.ActionTask;

public class OpenTask extends ActionTask {

    public OpenTask() { super(); }

    @Override public void run() {
        if (this.trigger.category.contentEquals("web")) {
            if (this.trigger.data != null && this.trigger.data.has("url")) {
                FloozApplication.getInstance().getCurrentActivity().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(this.trigger.data.optString("url"))));
                FLTriggerManager.getInstance().executeTriggerList(trigger.triggers);
            }
        }
    }
}
