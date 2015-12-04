package me.flooz.app.UI.Activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.EditText;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import com.facebook.appevents.AppEventsLogger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import io.branch.referral.Branch;
import io.branch.referral.BranchError;
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

        final Intent intent = getIntent();

        Branch branch = Branch.getInstance(getApplicationContext());
        branch.initSession(new Branch.BranchReferralInitListener() {
            @Override
            public void onInitFinished(JSONObject referringParams, BranchError error) {
                if (error == null) {
                    FloozApplication.getInstance().branchParams = referringParams;

                    String data = referringParams.optString("data");

                    if (data != null && !data.isEmpty()) {
                        try {
                            JSONObject dataObject = new JSONObject(data);

                            if (dataObject.has("login"))
                                FloozRestClient.getInstance().setNewAccessToken(dataObject.optString("login"));

                            if (dataObject.has("triggers"))
                                FloozApplication.getInstance().pendingTriggers = dataObject.optJSONArray("triggers");

                            if (dataObject.has("popup"))
                                FloozApplication.getInstance().pendingPopup = dataObject.optJSONObject("popup");

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }, this.getIntent().getData(), this);

        JSONArray triggersArray = null;
        if (intent != null && intent.getExtras() != null) {
            String message = intent.getStringExtra("triggers");
            if (message != null && !message.isEmpty()) {
                try {
                    triggersArray = new JSONArray(message);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                if (triggersArray != null) {
                    FloozApplication.getInstance().pendingTriggers = triggersArray;
                }
            } else if (intent.getData() != null) {
                String path = null;
                try {
                    path = URLDecoder.decode(intent.getData().toString(), "UTF-8");
                    path = path.replace("flooz://", "");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }

                if (path != null) {
                    try {
                        JSONObject obj = new JSONObject(path);

                        JSONObject data = obj.optJSONObject("data");

                        if (data != null) {
                            if (data.has("login"))
                                FloozRestClient.getInstance().setNewAccessToken(data.optString("login"));

                            if (data.has("triggers"))
                                FloozApplication.getInstance().pendingTriggers = data.optJSONArray("triggers");

                            if (data.has("popup"))
                                FloozApplication.getInstance().pendingPopup = data.optJSONObject("popup");
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

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
            input.setText("172.20.113.");
            alert.setView(input);

            alert.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    FloozRestClient.customIpAdress = "http://" + input.getText().toString();
                    if (!FloozRestClient.getInstance().autologin()) {
                        FloozApplication.getInstance().displayStartView();
                        finish();
                    }
                }
            });

            alert.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (!FloozRestClient.getInstance().autologin()) {
                        FloozApplication.getInstance().displayStartView();
                        finish();
                    }
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