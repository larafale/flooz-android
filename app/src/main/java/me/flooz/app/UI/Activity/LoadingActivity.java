package me.flooz.app.UI.Activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.EditText;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import com.facebook.AppEventsLogger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import io.branch.referral.Branch;
import io.fabric.sdk.android.Fabric;
import me.flooz.app.App.FloozApplication;
import me.flooz.app.BuildConfig;
import me.flooz.app.Model.FLError;
import me.flooz.app.Model.FLTrigger;
import me.flooz.app.Network.FloozRestClient;
import me.flooz.app.R;
import me.flooz.app.UI.Tools.CustomToast;
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
        Fabric.with(this, new Crashlytics());

        if (FLHelper.isDebuggable())
            ViewServer.get(this).addWindow(this);

        floozApp = (FloozApplication)this.getApplicationContext();

        this.setContentView(R.layout.loading_activity);

        TextView loadingText = (TextView)this.findViewById(R.id.loading_text);
        loadingText.setTypeface(CustomFonts.customContentRegular(this.getApplicationContext()));
    }

    protected void onStart() {
        super.onStart();

        Branch branch = Branch.getInstance(getApplicationContext());
        branch.initSession((referringParams, error) -> {
            if (error == null) {
                String referrer = referringParams.optString("referrer");
            }
        }, this.getIntent().getData(), this);

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

            alert.setPositiveButton(android.R.string.ok, (dialog, whichButton) -> {
                FloozRestClient.customIpAdress = "http://" + input.getText().toString();
                if (!FloozRestClient.getInstance().autologin()) {
                    FloozApplication.getInstance().displayStartView();
                    finish();
                }
            });

            alert.setNegativeButton(android.R.string.cancel, (dialog, whichButton) -> {
                if (!FloozRestClient.getInstance().autologin()) {
                    FloozApplication.getInstance().displayStartView();
                    finish();
                }
            });

            alert.show();
        }
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