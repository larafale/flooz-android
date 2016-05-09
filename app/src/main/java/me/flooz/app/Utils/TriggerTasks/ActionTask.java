package me.flooz.app.Utils.TriggerTasks;

import me.flooz.app.Model.FLTrigger;

/**
 * Created by Flooz on 2/8/16.
 */

public abstract class ActionTask implements Runnable {
    public ActionTask() {}

    public FLTrigger trigger;
    public abstract void run();
}
