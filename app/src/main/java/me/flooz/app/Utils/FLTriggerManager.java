package me.flooz.app.Utils;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

/**
 * Created by Flooz on 2/8/16.
 */
public class FLTriggerManager implements Application.ActivityLifecycleCallbacks {


    private static FLTriggerManager instance;

    public static FLTriggerManager getInstance()
    {
        if (instance == null)
            instance = new FLTriggerManager();
        return instance;
    }


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
