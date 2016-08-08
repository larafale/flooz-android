package me.flooz.app.Utils.TriggerTasks;

import android.app.Activity;
import android.content.Intent;

import me.flooz.app.App.FloozApplication;
import me.flooz.app.R;
import me.flooz.app.UI.Activity.ImagePickerActivity;

/**
 * Created by Flooz on 08/08/16.
 */
public class PickerTask extends ActionTask {

    public PickerTask() { super(); }

    @Override
    public void run() {
        if (trigger.category.contentEquals("image")) {
            if (trigger.view.contentEquals("gif")) {
                Activity currentActivity = FloozApplication.getInstance().getCurrentActivity();

                Intent intent = new Intent();
                intent.setClass(FloozApplication.getAppContext(), ImagePickerActivity.class);

                intent.putExtra("type", "gif");

                if (this.trigger.data != null)
                    intent.putExtra("triggerData", this.trigger.data.toString());

                currentActivity.startActivity(intent);
                currentActivity.overridePendingTransition(R.anim.slide_up, android.R.anim.fade_out);
            } else if (trigger.view.contentEquals("web")) {
                Activity currentActivity = FloozApplication.getInstance().getCurrentActivity();

                Intent intent = new Intent();
                intent.setClass(FloozApplication.getAppContext(), ImagePickerActivity.class);

                intent.putExtra("type", "web");

                if (this.trigger.data != null)
                    intent.putExtra("triggerData", this.trigger.data.toString());

                currentActivity.startActivity(intent);
                currentActivity.overridePendingTransition(R.anim.slide_up, android.R.anim.fade_out);
            }
        }
    }
}
