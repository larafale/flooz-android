package me.flooz.app.UI.Activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import com.facebook.AppEventsLogger;

import me.flooz.app.App.FloozApplication;
import me.flooz.app.BuildConfig;
import me.flooz.app.Network.FloozRestClient;
import me.flooz.app.R;
import me.flooz.app.Utils.CustomFonts;
import me.flooz.app.Utils.FLHelper;
import me.flooz.app.Utils.ViewServer;

/**
 * Created by Flooz on 11/6/14.
 */
public class LoadingActivity extends Activity {

    public FloozApplication floozApp;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Crashlytics.start(this);

        if (FLHelper.isDebuggable())
            ViewServer.get(this).addWindow(this);

        floozApp = (FloozApplication)this.getApplicationContext();

        this.setContentView(R.layout.loading_activity);

        TextView loadingText = (TextView)this.findViewById(R.id.loading_text);
        loadingText.setTypeface(CustomFonts.customContentRegular(this.getApplicationContext()));

        if (!BuildConfig.LOCAL_API) {
            if (!FloozRestClient.getInstance().autologin()) {
                FloozApplication.getInstance().displayStartView();
                this.finish();
            }
        } else {
            AlertDialog.Builder alert = new AlertDialog.Builder(this);

            alert.setTitle("Local Ip Address");
            alert.setCancelable(false);

            final EditText input = new EditText(this);
            input.setText("192.168.1.");
            alert.setView(input);

            alert.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    FloozRestClient.customIpAdress = "http://" + input.getText().toString();
                    if (!FloozRestClient.getInstance().autologin()) {
                        FloozApplication.getInstance().displayStartView();
                        finish();
                    }
                }
            });

            alert.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    if (!FloozRestClient.getInstance().autologin()) {
                        FloozApplication.getInstance().displayStartView();
                        finish();
                    }
                }
            });

            alert.show();
        }
    }

    protected void onStart() {
        super.onStart();
        floozApp.setCurrentActivity(this);
        AppEventsLogger.activateApp(this);
    }

    protected void onResume() {
        super.onResume();

        if (FLHelper.isDebuggable())
            ViewServer.get(this).setFocusedWindow(this);

        floozApp.setCurrentActivity(this);
        AppEventsLogger.activateApp(this);
    }

    protected void onPause() {
        clearReferences();
        AppEventsLogger.deactivateApp(this);
        super.onPause();
    }

    protected void onDestroy() {
        clearReferences();

        if (FLHelper.isDebuggable())
            ViewServer.get(this).removeWindow(this);

        super.onDestroy();
    }

    private void clearReferences(){
        Activity currActivity = floozApp.getCurrentActivity();
        if (currActivity != null && currActivity.equals(this))
            floozApp.setCurrentActivity(null);
    }

    @Override
    public void onBackPressed() {
        this.finish();
    }
}