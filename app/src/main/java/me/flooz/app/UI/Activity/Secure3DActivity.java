package me.flooz.app.UI.Activity;

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
 * Created by Flooz on 7/6/15.
 */
public class Secure3DActivity extends Activity {

    public FloozApplication floozApp;
    private WebView webView;

    public String data = "";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (FLHelper.isDebuggable())
            ViewServer.get(this).addWindow(this);

        this.data = this.getIntent().getStringExtra("html");

        setContentView(R.layout.settings_3d_secure_fragment);
        floozApp = (FloozApplication)this.getApplicationContext();

        ((TextView)this.findViewById(R.id.settings_3ds_header_title)).setTypeface(CustomFonts.customTitleLight(this));

        this.findViewById(R.id.settings_3ds_header_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FloozRestClient.getInstance().showLoadView();
                FloozRestClient.getInstance().abort3DSecure();
            }
        });

        webView = (WebView) this.findViewById(R.id.settings_3ds_webview);
        webView.setLayerType(WebView.LAYER_TYPE_SOFTWARE, null);
        webView.setWebViewClient(new Callback());

        webView.getSettings().setJavaScriptEnabled(true);
        webView.loadDataWithBaseURL(null, this.data, "text/html", "utf-8", null);

        while (webView.zoomOut());
    }

    protected void onStart() {
        super.onStart();
    }

    protected void onResume() {
        super.onResume();

        if (FLHelper.isDebuggable())
            ViewServer.get(this).setFocusedWindow(this);

        floozApp.setCurrentActivity(this);
    }

    protected void onPause() {
        clearReferences();
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
        FloozRestClient.getInstance().showLoadView();
        FloozRestClient.getInstance().abort3DSecure();
    }

    private class Callback extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            return (false);
        }
    }
}
