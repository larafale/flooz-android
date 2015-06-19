package me.flooz.app.UI.Activity.Settings;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import me.flooz.app.App.FloozApplication;
import me.flooz.app.Network.FloozRestClient;
import me.flooz.app.R;
import me.flooz.app.Utils.CustomFonts;
import me.flooz.app.Utils.FLHelper;
import me.flooz.app.Utils.ViewServer;

/**
 * Created by Flooz on 3/10/15.
 */
public class Secure3DSettingsActivity extends Activity {

    private FloozApplication floozApp;
    private Boolean modal;

    public String data = "";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (FLHelper.isDebuggable())
            ViewServer.get(this).addWindow(this);

        this.floozApp = (FloozApplication) this.getApplicationContext();
        this.modal = getIntent().getBooleanExtra("modal", true);

        this.setContentView(R.layout.settings_3d_secure_fragment);

        ((TextView)this.findViewById(R.id.settings_3ds_header_title)).setTypeface(CustomFonts.customTitleLight(this));

        this.findViewById(R.id.settings_3ds_header_back).setOnClickListener(v -> {
            FloozRestClient.getInstance().showLoadView();
            FloozRestClient.getInstance().abort3DSecure();
        });

        WebView webView = (WebView) this.findViewById(R.id.settings_3ds_webview);
        webView.setLayerType(WebView.LAYER_TYPE_SOFTWARE, null);
        webView.setWebViewClient(new Callback());

        webView.getSettings().setJavaScriptEnabled(true);
        webView.loadDataWithBaseURL(null, this.data, "text/html", "utf-8", null);
    }

    private class Callback extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            return (false);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if (FLHelper.isDebuggable())
            ViewServer.get(this).setFocusedWindow(this);

        floozApp.setCurrentActivity(this);
    }

    @Override
    public void onPause() {
        clearReferences();
        super.onPause();
    }

    @Override
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

    public void dismiss() {
        finish();
        if (modal)
            overridePendingTransition(android.R.anim.fade_in, R.anim.slide_down);
        else
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right);
    }

    @Override
    public void onBackPressed() {
        FloozRestClient.getInstance().showLoadView();
        FloozRestClient.getInstance().abort3DSecure();
    }
}
