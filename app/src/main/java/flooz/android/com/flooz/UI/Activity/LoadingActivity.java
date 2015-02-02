package flooz.android.com.flooz.UI.Activity;

import android.app.Activity;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.widget.TextView;

import com.facebook.AppEventsLogger;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import flooz.android.com.flooz.App.FloozApplication;
import flooz.android.com.flooz.Network.FloozRestClient;
import flooz.android.com.flooz.R;
import flooz.android.com.flooz.Utils.CustomFonts;

/**
 * Created by Flooz on 11/6/14.
 */
public class LoadingActivity extends Activity {

    public FloozApplication floozApp;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        floozApp = (FloozApplication)this.getApplicationContext();

        this.setContentView(R.layout.loading_activity);

        TextView loadingText = (TextView)this.findViewById(R.id.loading_text);
        loadingText.setTypeface(CustomFonts.customContentRegular(this.getApplicationContext()));

        if (!FloozRestClient.getInstance().autologin()) {
            FloozApplication.getInstance().displayStartView();
            this.finish();
        }

    }

    protected void onStart() {
        super.onStart();
        floozApp.setCurrentActivity(this);
        AppEventsLogger.activateApp(this);
    }

    protected void onResume() {
        super.onResume();
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