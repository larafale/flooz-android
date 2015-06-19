package me.flooz.app.UI.Activity;

import android.app.Activity;
import android.content.Intent;
import android.net.MailTo;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import me.flooz.app.App.FloozApplication;
import me.flooz.app.R;
import me.flooz.app.Utils.CustomFonts;
import me.flooz.app.Utils.FLHelper;
import me.flooz.app.Utils.ViewServer;

/**
 * Created by Flooz on 3/10/15.
 */
public class WebContentActivity extends Activity {

    private FloozApplication floozApp;
    private Boolean modal;

    private ImageView headerBackButton;
    private ProgressBar progressBar;

    public String title;
    public String url;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (FLHelper.isDebuggable())
            ViewServer.get(this).addWindow(this);

        this.floozApp = (FloozApplication) this.getApplicationContext();

        this.modal = getIntent().getBooleanExtra("modal", false);

        this.title = getIntent().getStringExtra("title");
        this.url = getIntent().getStringExtra("url");

        this.setContentView(R.layout.custom_webview_fragment);
        this.headerBackButton = (ImageView) this.findViewById(R.id.custom_webview_header_back);
        TextView headerTitle = (TextView) this.findViewById(R.id.custom_webview_header_title);

        headerTitle.setTypeface(CustomFonts.customTitleExtraLight(this));
        headerTitle.setText(this.title);

        this.headerBackButton.setOnClickListener(view -> {
            finish();
            if (modal)
                overridePendingTransition(android.R.anim.fade_in, R.anim.slide_down);
            else
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right);
        });

        this.progressBar = (ProgressBar) this.findViewById(R.id.custom_webview_progress_bar);

        WebView webView = (WebView) this.findViewById(R.id.custom_webview_webview);
        webView.setBackgroundColor(getResources().getColor(android.R.color.transparent));
        webView.setLayerType(WebView.LAYER_TYPE_SOFTWARE, null);

        WebViewClient client = new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url.startsWith("tel:")) {
                    Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse(url));
                    startActivity(intent);
                }
                else if (url.startsWith("mailto:")) {
                    MailTo mt = MailTo.parse(url);
                    Intent i = newEmailIntent(mt.getTo(), mt.getSubject(), mt.getBody(), mt.getCc());
                    startActivity(Intent.createChooser(i, "Envoyer un mail..."));
                }
                else if (url.startsWith("http:") || url.startsWith("https:")) {
                    view.loadUrl(url);
                }
                return true;
            }

        };

        webView.setWebViewClient(client);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.loadUrl(this.url);

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

    public static Intent newEmailIntent(String address, String subject, String body, String cc) {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:"));
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{address});
        intent.putExtra(Intent.EXTRA_TEXT, body);
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        intent.putExtra(Intent.EXTRA_CC, cc);
        return intent;
    }

    @Override
    public void onBackPressed() {
        this.headerBackButton.performClick();
    }
}