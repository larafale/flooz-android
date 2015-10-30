package me.flooz.app.UI.Controllers;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.view.View;

/**
 * Created by Flooz on 8/25/15.
 */
public class BaseController {

    public enum ControllerKind {
        ACTIVITY_CONTROLLER,
        FRAGMENT_CONTROLLER
    }

    public ControllerKind currentKind;
    public View currentView;
    public Activity parentActivity;


    public BaseController(@NonNull View mainView, @NonNull Activity parentActivity, @NonNull ControllerKind kind) {
        this.parentActivity = parentActivity;
        this.currentView = mainView;
        this.currentKind = kind;
    }

    public void onStart() {

    }

    public void onResume() {

    }

    public void onPause() {

    }

    public void onDestroy() {

    }

    public void onBackPressed() {

    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {

    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

    }
}