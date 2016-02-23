package me.flooz.app.UI.Activity;

import android.app.Activity;

/**
 * Created by Flooz on 2/23/16.
 */
public class BaseActivity extends Activity {

    public enum FLActivityState {
        FLActivityStateCreated,
        FLActivityStateStarted,
        FLActivityStateResumed,
        FLActivityStatePaused,
        FLActivityStateStopped,
        FLActivityStateDestroyed
    };

    public FLActivityState currentState;
}
